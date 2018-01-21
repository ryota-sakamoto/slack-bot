package actors

import akka.actor._
import train._

class TrainActor extends Actor with ActorLogging {
    def receive = {
        case message: String => {
            log.info(message)
            Train.parseTrain(message) match {
                case Left(m) => m.mkString("\n")
                case Right(train) =>
                    val description = Train.getDescriptions(train)
                    log.info(description)

                    sender ! description
            }
        }
    }
}