package actors

import akka.actor._
import dispatch.Defaults._
import dispatch._
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}

class WandboxActor extends Actor with ActorLogging {
    private val wandbox_url = "https://wandbox.org/api/compile.json"

    def receive = {
        case code: String => {
            log.info(code)
            val body = Map(
                "code" -> convert(code),
                "compiler" -> "scala-2.12.x"
            )

            val response = Request(wandbox_url).post(body)
            log.info(response)

            sender ! response
        }
    }

    private def convert(code: String): String = {
        code.replace("&amp;", "&").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "'")
    }
}

private case class Request(request_url: String) {
    implicit val formats = DefaultFormats

    def post(body: Map[String, String]): String = {
        val request = url(request_url).POST.setHeader("Content-Type", "application/json") << compact(render(body))

        val f = Http(request OK as.String)
        f()
    }
}