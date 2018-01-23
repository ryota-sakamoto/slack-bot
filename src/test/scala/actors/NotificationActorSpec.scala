package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

class NotificationActorSpec extends TestKit(ActorSystem())
    with FlatSpecLike
    with BeforeAndAfterAll {

    private val id = "id"
    private val m = "message"
    private val invalid_request = SendMessage(id, "Invalid Request")
    override def afterAll: Unit = TestKit.shutdownActorSystem(system)

    def createActor: ActorRef = {
        system.actorOf(Props(classOf[NotificationActor], testActor, "Asia/Tokyo"))
    }

    "ReceiveMessage" should "Invalid Request" in {
        val actor = createActor

        actor ! ReceiveMessage(id, s"0000")
        expectMsg(invalid_request)

        actor ! ReceiveMessage(id, s"1159_ $m")
        expectMsg(invalid_request)

        actor ! ReceiveMessage(id, s"9999 $m")
        expectMsg(invalid_request)

        actor ! ReceiveMessage(id, s"2959 $m")
        expectMsg(invalid_request)

        actor ! ReceiveMessage(id, s"2459 $m")
        expectMsg(invalid_request)

        actor ! ReceiveMessage(id, s"3000 $m")
        expectMsg(invalid_request)
    }

    it should "Success Request" in {
        val actor = createActor

        actor ! ReceiveMessage(id, s"0000 $m")
        expectMsg(SendMessage(id, m))

        actor ! ReceiveMessage(id, s"2359 $m")
        // TODO schedule test
        // expectMsg(SendMessage(id, m))
    }
}