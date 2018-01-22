import slack.rtm.SlackRtmClient
import slack.models.Message
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config._
import actors._

import scala.concurrent.Future

object Main {
    def main(args: Array[String]): Unit = {
        implicit val system = ActorSystem("slack")
        implicit val timeout = Timeout(60.seconds)

        val config = ConfigFactory.load()
        val token = config.getString("slack.api_key")
        val maintenance_channel_name = config.getString("slack.maintenance_channel_name")
        val client = SlackRtmClient(token, 60.seconds)

        val wandbox_actor = system.actorOf(Props(classOf[WandboxActor]), "WandboxActor")
        val train_actor = system.actorOf(Props(classOf[TrainActor]), "TrainActor")
        val mac_actor = system.actorOf(Props(classOf[WatchMaintenanceProductsSupervisor], client, maintenance_channel_name), "WatchMaintenanceProductsSupervisor")
        val notification_actor = system.actorOf(Props(classOf[NotificationActor]), "NotificationActor")

        // TODO
        client.onMessage { implicit message =>
            val response = (if (check("scala:")) {
                Some(wandbox_actor ? message.text.replaceFirst("scala:", ""))
            } else if (check("train:")) {
                Some(train_actor ? message.text.replaceFirst("train:", ""))
            } else if (check("set:")) {
                val m = message.text.replaceFirst("set:", "")
                val pos = m.indexOf(" ")
                val (date, mm) = m.replaceFirst(" ", "").splitAt(pos)
                notification_actor ! actors.Notification.Set(date, mm)
                None
            } else {
                None
            }).asInstanceOf[Option[Future[String]]]

            for {
                r <- response
                m <- r
            } client.sendMessage(message.channel, m)
        }

        system.scheduler.schedule(0.seconds, 60.seconds, mac_actor, Run())
    }

    private def check(t: String)(implicit message: Message): Boolean = {
        message.text.indexOf(t) == 0
    }
}
