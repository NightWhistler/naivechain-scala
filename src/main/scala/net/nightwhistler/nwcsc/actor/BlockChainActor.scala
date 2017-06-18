package net.nightwhistler.nwcsc.actor

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import net.nightwhistler.nwcsc.actor.BlockChainActor._
import net.nightwhistler.nwcsc.blockchain.BlockChain
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.MessageType.ResponseBlockChain
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.PeerMessage

/**
  * Actor-based implementation of PeerToPeerCommunication
  */
object BlockChainActor {
  case class MineBlock( data: String )

  case class AddPeer( address: String )

  case class Peers( peers: Seq[String] )

  case object GetPeers

  case object HandShake

  def props: Props = Props[BlockChainActor]
}

class BlockChainActor extends Actor with PeerToPeerCommunication {

  override var blockChain: BlockChain = BlockChain()

  var peers: Set[ActorSelection] = Set.empty

  override def receive: Receive = {
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

    case MineBlock(data) =>
      blockChain = blockChain.addBlock(data)
      val peerMessage = PeerMessage(ResponseBlockChain, Seq(blockChain.latestBlock))
      broadcast(peerMessage)
      sender() ! peerMessage

    case p@PeerMessage(_,_) =>
      val replyTo = sender()
      handleMessage(p) { peerReply => replyTo ! peerReply }
  }

  override def broadcast(peerMessage: PeerMessage): Unit = {
    peers.foreach( peer => peer ! peerMessage)
  }
}
