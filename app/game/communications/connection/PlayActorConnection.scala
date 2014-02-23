package game.communications.connection

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import game.events.Event
import play.api.libs.iteratee.Enumerator
import akka.actor.Props
import play.api.libs.iteratee.Concurrent.Channel
import game.communications.commands.PlayerCommand

object PlayActorConnection {
  def props( player: ActorRef, channel: Channel[ String ] ) = Props( classOf[ PlayActorConnection ], player, channel )

  // Received Messages
  case object GetEnum

  // Sent Messages
  case class ReturnEnum( enum: Enumerator[ String ] )
}

/**
 * A RetryingActorConnection that implements toPlayer(String).
 */
class PlayActorConnection( val player: ActorRef, val channel: Channel[ String ] )
    extends PlayConnection with RetryingActorConnection {
  import PlayActorConnection._

  override def toPlayer( pc:PlayerCommand ) { context.parent ! pc }
  override def receive = super.retrying

  override def postStop {
    toClient( s"""{"id":$count, "message": { "type":"quit", "message":"later!" } } """ )
  }
}