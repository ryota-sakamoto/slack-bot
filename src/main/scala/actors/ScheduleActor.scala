package actors

import akka.actor.{Actor, ActorLogging}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ScheduleActor extends Actor with ActorLogging {
    import actors.Schedule._

    override def receive: Receive = {
        case Once(callback, second) =>
            log.info("once")
            context.system.scheduler.scheduleOnce(second.seconds)(callback())
        case Repeat(callback, second) =>
            log.info("repeat")
            context.system.scheduler.schedule(0.second, second.seconds)(callback())
    }
}