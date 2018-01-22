package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.joda.time.{DateTime, Seconds}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class NotificationActor(ref: ActorRef) extends Actor with ActorLogging {
    override def receive: Receive = {
        case ReceiveMessage(id, message) => {
            val pos = message.indexOf(" ")
            val (time, m) = message.replaceFirst(" ", "").splitAt(pos)
            if (!time.matches("^[012][01234][0-5][0-9]$")) {
                ref ! SendMessage(id, "Invalid Request")
            } else {
                val datetime = new DateTime()
                    .withHourOfDay(time.slice(0, 2).toInt)
                    .withMinuteOfHour(time.slice(2, 4).toInt)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)

                log.info(s"$message")
                val s = Seconds.secondsBetween(new DateTime(), datetime).getSeconds
                context.system.scheduler.scheduleOnce(s.seconds) {
                    ref ! SendMessage(id, m)
                }
            }
        }
    }
}