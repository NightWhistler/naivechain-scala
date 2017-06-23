package net.nightwhistler.nwcsc.p2p

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSelection, Terminated}
import akka.pattern.pipe
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.actor.CompositeActor
import net.nightwhistler.nwcsc.p2p.PeerToPeer._

import scala.concurrent.duration.Duration

/**
  * Created by alex on 17-6-17.
  */
object PeerToPeer {

  case class AddPeer( address: String )

  case class ResolvedPeer( actorRef: ActorRef )

  case class Peers( peers: Seq[String] )

  case object GetPeers

  case object HandShake
}

trait PeerToPeer {
  this: CompositeActor =>

  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))
  implicit val executionContext = context.system.dispatcher

  val logger: Logger
  var peers: Set[ActorRef] = Set.empty

  def broadcast(message: Any ): Unit = {
    peers.foreach( _ ! message )
  }

  receiver {

    case AddPeer(peerAddress) =>
      logger.debug(s"Got request to add peer ${peerAddress}")
      context.actorSelection(peerAddress).resolveOne().map( ResolvedPeer(_) ).pipeTo(self)

    case ResolvedPeer(newPeerRef: ActorRef) =>
      context.watch(newPeerRef)

      if ( ! peers.contains(newPeerRef) ) {
        //Introduce ourselves
        newPeerRef ! HandShake

        //Ask for its friends
        newPeerRef ! GetPeers

        //Tell our existing peers
        broadcast(AddPeer(newPeerRef.path.toSerializationFormat))

        //Add to the current list of peers
        peers += newPeerRef
      } else logger.debug("We already know this peer, discarding")

    case Peers(peers) => peers.foreach( self ! AddPeer(_))

    case HandShake =>
      logger.debug(s"Received a handshake from ${sender().path.toStringWithoutAddress}")
      peers += sender()

    case GetPeers => sender() ! Peers(peers.toSeq.map(_.path.toSerializationFormat))

    case Terminated(actorRef) =>
      logger.debug(s"Peer ${actorRef} has terminated. Removing it from the list.")
      peers -= actorRef

  }

}
