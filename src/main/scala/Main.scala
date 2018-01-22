import akka.actor._
import actors._

object Main {
    def main(args: Array[String]): Unit = {
        implicit val system = ActorSystem("slack")
        system.actorOf(Props(classOf[Supervisor]))
    }
}
