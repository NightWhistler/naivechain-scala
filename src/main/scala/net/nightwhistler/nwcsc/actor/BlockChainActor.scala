package net.nightwhistler.nwcsc.actor

import akka.actor.{Actor, ActorRef, ActorSelection}
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
}

class BlockChainActor extends Actor with PeerToPeerCommunication {

  override var blockChain: BlockChain = BlockChain()

  var peers: Seq[ActorSelection] = Nil

  override def receive: Receive = {
    //  val remote = context.actorFor("akka://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")
    case AddPeer(peerAddress) => peers :+= context.actorSelection(peerAddress)

    case GetPeers => sender() ! Peers(peers.map(_.toSerializationFormat))

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
