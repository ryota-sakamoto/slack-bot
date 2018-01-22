package actors

import java.util.Date

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class NotificationActor extends Actor with ActorLogging {
    import Notification._

    override def receive: Receive = {
        // date = hour + minutes
        case Set(date, message) => {
            val now_date = "%tT".format(new Date()).split(":")
            val s = (date.toInt - (now_date(0) + now_date(1)).toInt) * 60 - now_date(2).toInt
            log.info(s"$date $message")
            context.system.scheduler.scheduleOnce(s.seconds) {
                self ! Message(message)
            }
        }
        case Message(message) => log.info(message)
//        case Cancel() =>
    }
}

object Notification {
    sealed trait Command
    case class Set(date: String, message: String) extends Command
    case class Message(message: String) extends Command
    //case class Cancel() extends Command
}