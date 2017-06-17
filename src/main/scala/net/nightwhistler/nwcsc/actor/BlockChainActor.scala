package net.nightwhistler.nwcsc.actor

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
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

  case class AddPeer( address: String )

  case class Peers( peers: Seq[String] )

  case object GetPeers

  def props: Props = Props[BlockChainActor]
}

class BlockChainActor extends Actor with PeerToPeerCommunication {

  override var blockChain: BlockChain = BlockChain()

  var peers: Seq[ActorSelection] = Nil

  override def receive: Receive = {
    case AddPeer(peerAddress) => peers :+= context.actorSelection(peerAddress)

    case GetPeers => sender() ! Peers(peers.map(_.toSerializationFormat))

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
