package actors

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import slack.models.Message
import slack.rtm.SlackRtmClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Supervisor extends Actor {
    private implicit val system = context.system
    private val config = ConfigFactory.load()
    private val token = config.getString("slack.api_key")
    private val maintenance_channel_name = config.getString("slack.maintenance_channel_name")
    private val client = SlackRtmClient(token, 60.seconds)

    actorStart()

    override def receive: Receive = {
        case _ =>
    }

    def actorStart(): Unit = {
        val slack_actor = context.actorOf(Props(classOf[SlackActor], client), "SlackActor")
        val wandbox_actor = context.actorOf(Props(classOf[WandboxActor], slack_actor), "WandboxActor")
        val train_actor = context.actorOf(Props(classOf[TrainActor], slack_actor), "TrainActor")
        val mac_actor = context.actorOf(Props(classOf[WatchMaintenanceProductsSupervisor], slack_actor, client, maintenance_channel_name), "WatchMaintenanceProductsSupervisor")
        context.system.scheduler.schedule(0.seconds, 60.seconds, mac_actor, Run())
        val notification_actor = context.actorOf(Props(classOf[NotificationActor], slack_actor), "NotificationActor")

        client.onMessage { implicit message =>
            if (check("scala:")) {
                wandbox_actor ! replace("scala:")
            } else if (check("train:")) {
                train_actor ! replace("train:")
            } else if (check("set:")) {
                notification_actor ! replace("set:")
            }
        }
    }

    private def check(t: String)(implicit message: Message): Boolean = {
        message.text.indexOf(t) == 0
    }

    private def replace(target: String)(implicit message: Message): ReceiveMessage = {
        ReceiveMessage(message.channel, message.text.replaceFirst(target, ""))
    }
}

case class ReceiveMessage(sender: String, message: String)
case class SendMessage(sender: String, message: String)