package train

import dispatch._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{allText, elementList}
import net.ruippeixotog.scalascraper.dsl.DSL._

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global

case class Station(id: String, name: String)
case class Train(from_id: String, to_id: String, via: Option[String])
case class Description()

object Train {
    private val navitime_station_search = "https://www.navitime.co.jp/ajax/transport/getStationList?word=%s"
    private val navitime_search = "https://www.navitime.co.jp/transfer/searchlist?orvStationCode=%s&dnvStationCode=%s&basis=1&wspeed=100&airplane=1&sprexprs="

    private implicit val formats = DefaultFormats

    def parseTrain(message: String): Either[List[String], Train] = {
        val s = message.split('-').toList
        val stations = s.zip(s match {
            case List(_, _, _) | List(_, _) => {
                s.map(getStations)
            }
            case _ => List.empty
        })

        val no_data = stations.filter(_._2.isEmpty)
        if (no_data.nonEmpty) {
            Left(no_data.map(e => s"${e._1} is not found"))
        } else {
            val pickup = (n: Int) => {
                stations(n)._2.head
            }

            stations.length match {
                case 2 => {
                    val from = pickup(0)
                    val to = pickup(1)

                    Right(Train(from.id, to.id, None))
                }
                case 3 => {
                    val from = pickup(0)
                    val via = pickup(1)
                    val to = pickup(2)

                    Right(Train(from.id, to.id, Some(via.id)))
                }
                case _ => Left(List())
            }
        }
    }

    def getStations(name: String): List[Station] = {
        val request = url(navitime_station_search.format(name)).GET
        val f = Http(request OK as.String)
        parse(f()).camelizeKeys.extract[List[Station]]
    }

    @deprecated
    def getDescriptions(train: Train): String = {
        val browser = JsoupBrowser()
        val via = train.via match {
            case Some(v) => s"&thrStationCode1=$v"
            case None => ""
        }

        val doc = browser.get(navitime_search.format(train.from_id, train.to_id) + via)
        val items = doc >> elementList(".route_detail .section_detail_frame").map(_ >> elementList(".section_station_frame"))
        val times = items.map(_ >> allText(".sgk_time"))
        val station_names = items.map(_ >> elementList("a")).map(_.flatten.filter(_.attr("href").contains("poi?node"))).map(_.map(_.text))
        times.zip(station_names).map(x => x._1.zip(x._2)).map(_.map(x => s"${x._1} ${x._2}").mkString("\n")).mkString("\n\n")
    }
}