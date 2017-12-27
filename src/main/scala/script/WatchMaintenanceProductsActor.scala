package script

import akka.actor._
import akka.persistence.PersistentActor
import slack.rtm.SlackRtmClient

class WatchMaintenanceProductsActor(val client: SlackRtmClient) extends PersistentActor with ActorLogging {
    override def persistenceId = "WatchMaintenanceProductsId"

    private var set = Set[String]()

    override def receiveRecover: Receive = {
        case mac_id: String => {
            log.info(s"recover $mac_id")
            set += mac_id
        }
    }

    override def receiveCommand: Receive = {
        case _ => {
            val list = WatchMaintenanceProducts.getList

            list.foreach { product =>
                if (!set.contains(product.id)) {
                    val id = client.state.getChannelIdForName("slack-bot").get
                    client.sendMessage(id,
                        s"""
                           ${product.specs}
                           ${product.url}
                           ${product.price}
                         """)
                    log.info(s"new ${product.id}")

                    persist(product.id) { mac_id =>
                        set += mac_id
                    }
                }
            }
        }
    }
}