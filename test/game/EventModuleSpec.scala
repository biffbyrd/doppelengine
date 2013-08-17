package game

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class EventModuleSpec
    extends EventModule
    with Specification {

  implicit val system: ActorSystem = ActorSystem( "EventModuleSpec" )

  val adj0: Adjust = { case x ⇒ x }
  val adj1: Adjust = { case Test( i ) ⇒ Test( 2 * i ) }
  val adj2: Adjust = { case Test( i ) ⇒ Test( 3 * i ) }
  val adj3: Adjust = { case Test( i ) if i % 7 == 0 ⇒ Test( 0 ) }

  val adjs = List( adj0, adj1, adj2, adj3 )

  case class Test( v: Int ) extends Event

  trait TestEventHandler extends GenericEventHandler {
    adjusts = adjs
    def default: Handle = { case _ ⇒ }
    protected def emit( e: Event ): Unit = {}
  }

  "GenericEventHandler#remove" should {
    "remove an Adjust from 'adjusts' if it is contained within the list" in {
      new TestEventHandler { def test( a: Adjust ) = remove( a ) }
        .test( adj0 ) === List( adj1, adj2, adj3 )
    }
  }

  "GenericEventHandler#removeAll" should {
    "remove a subset of Adjusts from 'adjusts'" in {
      new TestEventHandler { def test( as: List[ Adjust ] ) = removeAll( as ) }
        .test( List( adj0, adj2 ) ) === List( adj1, adj3 )
    }
  }

  "EventHandler#adjust(Event)" should {
    "pipe an Event through a List[ Adjust ] and skip those for which it's not defined at" in {
      new TestEventHandler { def test( e: Event ) = adjust( e ) }
        .test( Test( 5 ) ) === Test( 30 )
    }
  }

}