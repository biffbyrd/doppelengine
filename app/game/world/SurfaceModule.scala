package game.world

import game.EventModule
import akka.actor.ActorRef
import game.mobile.MobileModule

/**
 * A surface is an object with length, slope, and position. A surface
 * can be either a Wall or a Floor.
 *
 * A Wall always has a vertical (undefined) slope, while a Surface
 * always has a Defined slope.
 */
trait SurfaceModule {
  this: RoomModule with EventModule with MobileModule ⇒

  case class Landed( ar: ActorRef ) extends Event

  class UndefinedSlopeException extends Exception

  trait Slope {
    def dx: Int
    def dy: Int
  }

  abstract class Defined( val dx: Int, val dy: Int ) extends Slope {
    lazy val m = dy.toDouble / dx.toDouble
  }

  case class Slant( _x: Int, _y: Int ) extends Defined( _x, _y )

  case object Flat extends Defined( 1, 0 )

  case object Undefined extends Slope {
    val dx = 0
    def dy = throw new UndefinedSlopeException
  }

  /**
   * A Surface is essentially just a line with a length, position (x,y), and a slope.
   * Surfaces are owned by Room objects, and can supply Adjusts to modify certain Events.
   */
  trait Surface extends AdjustSupplier {
    val xpos: Int
    val ypos: Int
    val length: Int
    val slope: Slope
  }

  trait Floor extends Surface {
    val slope: Defined
    lazy val b = slope.dy - ( slope.m * slope.dx )

    val stopDown: Adjust = {
      case Moved( ar, p, Movement( xspeed, yspeed ) ) if landing( p.feet, yspeed ) && inBounds( p ) ⇒
        val yintersect = slope.m * p.x + b
        Moved( ar, Position( p.x, yintersect.toInt + 2 ), Movement( xspeed, 0 ) )
    }

    def landing( feet: ( Int, Int ), yspeed: Int ) = {
      val ( xfeet, yfeet ) = feet
      val yintersect = slope.m * xfeet + b
      yspeed < 0 &&
        yfeet >= yintersect &&
        ( yfeet + yspeed ) < yintersect
    }

    def inBounds( p: Position ) = {
      true
    }

    adjusts = adjusts :+ stopDown
  }

  case class Wall( val xpos: Int,
                   val ypos: Int,
                   val length: Int ) extends Surface {
    val slope = Undefined
    val ytop = ypos + ( length / 2 )
    val ybottom = ypos - ( length / 2 )
    def inBounds( p: Position ) = {
      val (xhead, yhead) = p.head
      val (xfeet, yfeet) = p.feet
      ( yhead > ybottom && yhead < ytop ) || ( yfeet < ytop && yfeet > ybottom )
    }

    val stopLeft: Adjust = {
      case Moved( ar, p, m ) if p.left._1 == this.xpos && m.x < 0 && inBounds( p ) ⇒
        Moved( ar, p, Movement( 0, m.y ) )
    }

    val stopRight: Adjust = {
      case Moved( ar, p, m ) if p.right._2 == this.xpos && m.x > 0 && inBounds( p ) ⇒
        Moved( ar, p, Movement( 0, m.y ) )
    }

    adjusts = adjusts ::: List( stopLeft, stopRight )
  }

  case class SingleSided( val xpos: Int,
                          val ypos: Int,
                          val length: Int,
                          val slope: Defined ) extends Floor

  case class DoubleSided( val xpos: Int,
                          val ypos: Int,
                          val length: Int,
                          val slope: Defined ) extends Floor

}