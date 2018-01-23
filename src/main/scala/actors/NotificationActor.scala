package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.joda.time.{DateTime, DateTimeZone, Seconds}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class NotificationActor(ref: ActorRef, timezone: String) extends Actor with ActorLogging {
    private val timezone_id = DateTimeZone.forID(timezone)

    override def receive: Receive = {
        case ReceiveMessage(id, message) => {
            val pos = message.indexOf(" ")
            val (time, m) = message.replaceFirst(" ", "").splitAt(pos)
            if (!time.matches("^([01][0-9]|2[0-3])[0-5][0-9]$")) {
                ref ! SendMessage(id, "Invalid Request")
            } else {
                val datetime = new DateTime(timezone_id)
                    .withHourOfDay(time.slice(0, 2).toInt)
                    .withMinuteOfHour(time.slice(2, 4).toInt)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)

                log.info(s"$message")
                val s = Seconds.secondsBetween(new DateTime(timezone_id), datetime).getSeconds
                context.system.scheduler.scheduleOnce(s.seconds) {
                    ref ! SendMessage(id, m)
                }
            }
        }
    }
}