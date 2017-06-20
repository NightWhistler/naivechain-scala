package net.nightwhistler.nwcsc.p2p

import akka.actor.{Actor, ActorSelection}
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.p2p.PeerToPeer.{AddPeer, GetPeers, HandShake, Peers}

/**
  * Created by alex on 17-6-17.
  */
object PeerToPeer {

  case class AddPeer( address: String )

  case class Peers( peers: Seq[String] )

  case object GetPeers

  case object HandShake
}

trait PeerToPeer {
  this: Actor =>

  val logger: Logger
  var peers: Set[ActorSelection] = Set.empty

  def broadcast(message: Any ): Unit = {
    peers.foreach( _ ! message )
  }

  def receivePeerToPeer: Receive = {

    case AddPeer(peerAddress) =>
      val selection = context.actorSelection(peerAddress)
      logger.debug(s"Got request to add peer ${peerAddress}")

      if ( ! peers.contains(selection) ) {
        //Introduce ourselves
        selection ! HandShake

        //Ask for its friends
        selection ! GetPeers

        //Tell our existing peers
        peers.foreach( peer => peer ! AddPeer(peerAddress))

        //Add to the current list of peers
        peers += context.actorSelection(peerAddress)
      } else logger.debug("We already know this peer, discarding")

    case Peers(peers) => peers.foreach( self ! AddPeer(_))

    case HandShake =>
      logger.debug(s"Received a handshake from ${sender().path.toStringWithoutAddress}")
      peers += context.actorSelection(sender().path)

    case GetPeers => sender() ! Peers(peers.toSeq.map(_.toSerializationFormat))

  }

}
