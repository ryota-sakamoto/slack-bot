package actors

import akka.actor._
import akka.persistence.PersistentActor
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import net.ruippeixotog.scalascraper.dsl.DSL._
import slack.rtm.SlackRtmClient
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class WatchMaintenanceProductsSupervisor(ref: ActorRef, client: SlackRtmClient, channel_name: String) extends Actor {
    implicit val timeout = Timeout(60.seconds)
    private val actor = context.actorOf(Props(classOf[WatchMaintenanceProductsActor], WatchMaintenanceProductsImpl))
    private val channel_id = client.state.getChannelIdForName(channel_name).get

    def receive = {
        case _: Run => {
            val data = (actor ? Run()).asInstanceOf[Future[List[String]]]
            data.foreach(_.foreach(d => ref ! SendMessage(channel_id, d)))
        }
    }
}

class WatchMaintenanceProductsActor(w: WatchMaintenanceProducts) extends PersistentActor with ActorLogging {
    override def persistenceId = "WatchMaintenanceProductsId"

    private var set = Set[String]()

    override def receiveRecover: Receive = {
        case a: Add => add(a)
        case r: Remove => remove(r)
    }

    override def receiveCommand: Receive = {
        case _: Run => {
            val list = w.getList

            val add_list = for {
                product <- list
                if !set.contains(product.id)
            } yield {
                self ! Add(product.id)

                s"""
                    ${product.specs}
                    ${product.url}
                    ${product.price}
                    """
            }

            for {
                id <- set.toList diff list.map(_.id)
            } self ! Remove(id)

            sender() ! add_list
        }
        case a: Add => persist(a)(add)
        case r: Remove => persist(r)(remove)
    }

    private def add(a: Add): Unit = {
        log.info(s"add ${a.id}")
        set += a.id
    }
    private def remove(r: Remove): Unit = {
        log.info(s"remove ${r.id}")
        set -= r.id
    }
}

case class Product(id: String, specs: String, price: String, url: String)

sealed trait Command
case class Run() extends Command
case class Add(id: String) extends Command
case class Remove(id: String) extends Command

private object WatchMaintenanceProductsImpl extends WatchMaintenanceProducts {
    private val prefix = "https://www.apple.com"
    private val url = s"$prefix/jp/shop/browse/home/specialdeals/mac/macbook_pro"
    private val regex = "(/jp/shop/product/)(.*?)/A/.*".r

    def getList(): List[Product] = {
        val browser = JsoupBrowser()
        val doc = browser.get(url)

        for {
            product <- doc >> elementList(".product")
            specs <- product >> elementList("h3 a")
            info <- product >> elementList(".purchase-info")
            price <- info >> elementList(".current_price")
        } yield {
            specs.attr("href") match {
                case regex(p, id) => {
                    val url = prefix + p + id
                    Product(
                        id = id,
                        specs = specs.text,
                        price = price.text,
                        url = url
                    )
                }
            }
        }
    }
}

trait WatchMaintenanceProducts {
    def getList(): List[Product]
}