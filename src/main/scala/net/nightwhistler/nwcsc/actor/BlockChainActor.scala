package net.nightwhistler.nwcsc.actor

import akka.actor.{Actor, ActorRef}
import net.nightwhistler.nwcsc.actor.BlockChainActor.{AddPeer, GetPeers, MineBlock, Peers}
import net.nightwhistler.nwcsc.blockchain.BlockChain
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.MessageType.ResponseBlockChain
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.PeerMessage

/**
  * Actor-based implementation of PeerToPeerCommunication
  */
object BlockChainActor {
  case class MineBlock( data: String )

  case class AddPeer( peer: ActorRef )

  case class Peers( peers: Seq[ActorRef])

  case object GetPeers
}

class BlockChainActor extends Actor with PeerToPeerCommunication {

  override var blockChain: BlockChain = BlockChain()

  var peers: Seq[ActorRef] = Nil

  override def receive: Receive = {

    case AddPeer(peer) => peers :+= peer

    case GetPeers => sender() ! Peers(peers)

    case MineBlock(data) =>
      blockChain = blockChain.addBlock(data)
      broadcast(PeerMessage(ResponseBlockChain, Seq(blockChain.latestBlock)))

    case p@PeerMessage(_,_) =>
      val replyTo = sender()
      handleMessage(p) { peerReply => replyTo ! peerReply }
  }

  override def broadcast(peerMessage: PeerMessage): Unit = {
    peers.foreach( peer => peer ! peerMessage)
  }
}
