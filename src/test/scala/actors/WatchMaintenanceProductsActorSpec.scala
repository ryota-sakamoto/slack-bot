package actors

import akka.actor._
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest._
import scala.concurrent.duration._

class WatchMaintenanceProductsActorSpec extends TestKit(ActorSystem())
    with FlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

    override def afterAll: Unit = {
        TestKit.shutdownActorSystem(system)
    }


    implicit val timeout = Timeout(60.seconds)
    val mock: (List[Product]) => WatchMaintenanceProducts = (list) => {
        new WatchMaintenanceProducts {
            override def getList(): List[Product] = list
            def s: String = ""
        }
    }

    class TestWatchMaintenanceProductsActor(w: WatchMaintenanceProducts) extends WatchMaintenanceProductsActor(w) {

    }

    val p: (String) => Product = (id) =>Product(id, "", "", "")

    "WatchMaintenanceProductsActor" should "getList" in {
        val actor = system.actorOf(Props(classOf[TestWatchMaintenanceProductsActor], mock(List(p("1")))))
        actor ! Run()
        Thread.sleep(3000)
        system.stop(actor)

//        val actor2 = system.actorOf(Props(classOf[WatchMaintenanceProductsActor], mock(List(p("1"), p("2")))))
//        actor2 ! Run()
//        Thread.sleep(3000)
//        system.stop(actor2)
//
//        val actor3 = system.actorOf(Props(classOf[WatchMaintenanceProductsActor], mock(List(p("3")))))
//        actor3 ! Run()
//        Thread.sleep(3000)
//        system.stop(actor3)
    }
}