package actors

import akka.actor._
import train._

class TrainActor(ref: ActorRef) extends Actor with ActorLogging {
    def receive = {
        case ReceiveMessage(id, message) => {
            log.info(message)
            val response = Train.parseTrain(message) match {
                case Left(m) => m.mkString("\n")
                case Right(train) =>
                    val description = Train.getDescriptions(train)
                    log.info(description)
                    description
            }

            ref ! SendMessage(id, response)
        }
    }
}