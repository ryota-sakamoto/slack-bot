package actors

import akka.actor._
import akka.persistence.PersistentActor
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import net.ruippeixotog.scalascraper.dsl.DSL._
import slack.rtm.SlackRtmClient

class WatchMaintenanceProductsActor(client: SlackRtmClient, channel_name: String) extends PersistentActor with ActorLogging {
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
                    val id = client.state.getChannelIdForName(channel_name).get
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

private case class Product(id: String, specs: String, price: String, url: String)

private object WatchMaintenanceProducts {
    private val prefix = "https://www.apple.com"
    private val url = s"$prefix/jp/shop/browse/home/specialdeals/mac/macbook_pro"
    private val regex = "(/jp/shop/product/)(.*?)/A/.*".r

    def getList: List[Product] = {
        val browser = JsoupBrowser()
        val doc = browser.get(url)

        for {
            product <- doc >> elementList(".product")
            specs <- product >> elementList("h3 a")
            info <- product >> elementList(".purchase-info")
            price <- info >> elementList(".current_price")
        } yield {
            specs.attr("href") match {
                case regex(p, id) => {
                    val url = prefix + p + id
                    Product(
                        id = id,
                        specs = specs.text,
                        price = price.text,
                        url = url
                    )
                }
            }
        }
    }
}