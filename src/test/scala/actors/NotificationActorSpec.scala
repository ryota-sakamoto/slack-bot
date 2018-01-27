package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

class NotificationActorSpec extends TestKit(ActorSystem())
    with FlatSpecLike
    with BeforeAndAfterAll {

    private val id = "id"
    private val m = "message"
    private val invalid_request = SendMessage(id, "Invalid Request")
    private val probe = TestProbe()
    override def afterAll: Unit = TestKit.shutdownActorSystem(system)

    def createActor(sender: ActorRef): ActorRef = {
        system.actorOf(Props(classOf[NotificationActor], testActor, sender, "Asia/Tokyo"))
    }

    "ReceiveMessage" should "Invalid Request" in {
        val actor = createActor(probe.ref)

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

    // TODO test
    it should "Success Request" in {
        val testProbe = new TestProbe(system) {
            def expectSchedule(s: Schedule.ScheduleTrait) = {
                expectMsgPF() {
                    case Schedule.Once(callback, second) if callback.toString.contains("NotificationActor") && second < s.second => true
                }
            }
        }

        val actor = createActor(testProbe.ref)

        actor ! ReceiveMessage(id, s"0000 $m")
        testProbe.expectSchedule(Schedule.Once(() => {}, 0))

        actor ! ReceiveMessage(id, s"2359 $m")
        // TODO schedule test
        // expectMsg(SendMessage(id, m))
    }
}