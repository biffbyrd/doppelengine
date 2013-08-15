package game.mobile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.Cancellable
import game.EventModule
import game.world.RoomModule

/**
 * Defines the behavior for all mobile entities (Players and NPCs)
 */
trait MobileModule extends EventModule {
  this: RoomModule ⇒

  // Define events:
  case class Invalid( msg: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class MoveAttempt( dir: Int ) extends Event

  trait Mobile {
    val name: String
    var xpos: Int = 0
    var ypos: Int = 0
  }

  /** An EventHandling Mobile object */
  trait EHMobile extends EventHandler with Mobile {
    private var moveScheduler: Cancellable = _
    val xspeed = 1
    val yspeed = 0

    /** a Mobile that is standing still */
    def standing: Handle

    /** Represents the state of a moving Mobile */
    def moving: Handle = {
      case Moved( ar, xdir ) if ar == self ⇒
        this.xpos = this.xpos + xdir
        println( this.xpos );
      case KeyUp( c ) if List( 65, 68, 37, 39 ) contains c ⇒
        moveScheduler.cancel
        this.handle = standing ~ this.default
    }

    /** Starts moving this mobile */
    def move( dir: Int ) {
      this.handle = moving ~ default
      this.moveScheduler = system.scheduler.schedule( 0 millis, 80 millis )( this emit MoveAttempt( dir ) )
    }

    def moveLeft = move( -xspeed )
    def moveRight = move( xspeed )

  }
}