package script

import akka.actor._
import slack.rtm.SlackRtmClient

class WatchMaintenanceProductsActor(val client: SlackRtmClient) extends Actor with ActorLogging {
    def receive = {
        case (set: Set[String]) => {
            val list = WatchMaintenanceProducts.getList
            for {
                l <- list
                if !set.contains(l._1)
            } {
                val id = client.state.getChannelIdForName("slack-bot").get
                // client.sendMessage(id, l._2)
                log.info(s"new ${l._1}")
            }
            Thread.sleep(60000) // TODO akka schedule
            self ! set ++ list.keys.toSet
        }
    }
}