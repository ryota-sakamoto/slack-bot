import slack.rtm.SlackRtmClient
import akka.actor._
import dispatch._
import dispatch.Defaults._
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.typesafe.config._
import train.Train
import compile.Compile
import script.WatchMaintenanceProductsActor

object Main {
    private val wandbox_url = "https://wandbox.org/api/compile.json"

    def main(args: Array[String]): Unit = {
        implicit val system = ActorSystem("slack")
        implicit val formats = DefaultFormats

        val token = ConfigFactory.load().getString("slack.api_key")
        val client = SlackRtmClient(token)

        val check: (String, String) => Boolean = (s, t) => {
            s.indexOf(t) == 0
        }

        client.onMessage { message =>
            if (check(message.text, "scala:")) {
                val body = compact(render(Map(
                    "code" -> Compile.convert(message.text.replaceFirst("scala:", "")),
                    "compiler" -> "scala-2.12.x"
                )))
                println(body)
                val request = url(wandbox_url).POST.setHeader("Content-Type", "application/json") << body
                Http(request OK as.String).onComplete {
                    case Success(_f) => client.sendMessage(message.channel, _f)
                    case Failure(t) => println(t)
                }
            } else if (check(message.text, "train:")) {
                val s = Train.parseTrain(message.text.replaceFirst("train:", ""))
                client.sendMessage(message.channel, s match {
                    case Left(m) => m.mkString("\n")
                    case Right(train) => Train.getDescriptions(train)
                })
            }
        }

        val mac_actor = system.actorOf(Props(classOf[WatchMaintenanceProductsActor], client))
        mac_actor ! Set[String]()
    }
}
