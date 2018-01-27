package actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import scala.concurrent.duration._

class ScheduleActorSpec extends TestKit(ActorSystem())
    with FlatSpecLike
    with BeforeAndAfterAll {

    override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

    private def callback(message: String): () => Unit = () => testActor ! message

    "Once" should "Send Request" in {
        val actor = system.actorOf(Props(classOf[ScheduleActor]))
        actor ! Schedule.Once(callback("1"), 1)
        actor ! Schedule.Once(callback("2"), 2)
        actor ! Schedule.Once(callback("3"), 3)
        actor ! Schedule.Once(callback("10"), 10)
        expectMsg(1.1.seconds, "1")
        expectMsg(2.1.seconds, "2")
        expectMsg(3.1.seconds, "3")
        expectMsg(10.1.seconds, "10")
    }

    it should "Wait Request" in {
        val actor = system.actorOf(Props(classOf[ScheduleActor]))
        actor ! Schedule.Once(callback("1"), 1)
        actor ! Schedule.Once(callback("2"), 2)
        actor ! Schedule.Once(callback("3"), 3)
        actor ! Schedule.Once(callback("10"), 10)
        assert(receiveOne(1.seconds) != "1")
        assert(receiveOne(2.seconds) != "2")
        assert(receiveOne(3.seconds) != "3")
        assert(receiveOne(10.seconds) != "10")
    }

    // TODO repeat test
}