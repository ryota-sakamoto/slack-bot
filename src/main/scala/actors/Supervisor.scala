package actors

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import slack.models.Message
import slack.rtm.SlackRtmClient
import scala.concurrent.duration._

class Supervisor extends Actor {
    private implicit val system = context.system
    private val config = ConfigFactory.load()
    private val token = config.getString("slack.api_key")
    private val timezone = config.getString("slack.timezone")
    private val maintenance_channel_name = config.getString("maintenance.channel_name")
    private val format = config.getString("maintenance.format")
    private val client = SlackRtmClient(token, 60.seconds)

    actorStart()

    override def receive: Receive = {
        case _ =>
    }

    def actorStart(): Unit = {
        val slack_actor = context.actorOf(Props(classOf[SlackActor], client), "SlackActor")
        val wandbox_actor = context.actorOf(Props(classOf[WandboxActor], slack_actor), "WandboxActor")
        val train_actor = context.actorOf(Props(classOf[TrainActor], slack_actor), "TrainActor")
        val mac_actor = context.actorOf(Props(classOf[WatchMaintenanceProductsSupervisor], slack_actor, client, maintenance_channel_name, format), "WatchMaintenanceProductsSupervisor")
        val schedule_actor = context.actorOf(Props(classOf[ScheduleActor]))
        schedule_actor ! Schedule.Repeat(() => mac_actor ! Run())
        val notification_actor = context.actorOf(Props(classOf[NotificationActor], slack_actor, schedule_actor, timezone), "NotificationActor")

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

object Schedule {
    sealed trait ScheduleTrait {
        val callback: () => Unit
        val second: Int
    }
    case class Once(callback: () => Unit, second: Int = 60) extends ScheduleTrait
    case class Repeat(callback: () => Unit, second: Int = 60) extends ScheduleTrait
}