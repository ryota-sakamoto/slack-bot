package actors

import akka.actor.{Actor, ActorLogging}
import slack.rtm.SlackRtmClient

class SlackActor(client: SlackRtmClient) extends Actor with ActorLogging {
    override def receive: Receive = {
        case SendMessage(channel_id, message) => client.sendMessage(channel_id, message)
        case _ =>
    }
}