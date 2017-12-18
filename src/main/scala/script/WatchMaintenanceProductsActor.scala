package script

import akka.actor._
import scala.collection.immutable.HashSet

class WatchMaintenanceProductsActor extends Actor {
    def receive = {
        case (set: HashSet[String]) => {
            val list = WatchMaintenanceProducts.getList
            for {
                l <- list
                if !set.contains(l._1)
            } {}
        }
    }
}