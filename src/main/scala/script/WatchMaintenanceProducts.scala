package script

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{allText, elementList}
import net.ruippeixotog.scalascraper.model._
import net.ruippeixotog.scalascraper.dsl.DSL._

case class Product(id: String, specs: String, price: String, url: String)

object WatchMaintenanceProducts {
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