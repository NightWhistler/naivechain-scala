package net.nightwhistler.nwcsc.actor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import net.nightwhistler.nwcsc.PeerToPeerCommunication
import net.nightwhistler.nwcsc.PeerToPeerCommunication.MessageType.ResponseBlockChain
import net.nightwhistler.nwcsc.PeerToPeerCommunication.{MessageType, PeerMessage}
import net.nightwhistler.nwcsc.actor.BlockChainActor.MineBlock
import net.nightwhistler.nwcsc.blockchain.BlockChain

/**
  * Actor-based implementation of PeerToPeerCommunication
  */
object BlockChainActor {
  case class MineBlock( data: String )
}

class BlockChainActor extends Actor with PeerToPeerCommunication {

  override var blockChain: BlockChain = BlockChain()

  override def receive: Receive = {

    case MineBlock(data) =>
      blockChain = blockChain.addBlock(data)
      broadcast(PeerMessage(ResponseBlockChain, Seq(blockChain.latestBlock)))

    case p@PeerMessage(_,_) =>
      val replyTo = sender()
      handleMessage(p) { peerReply =>
        replyTo ! peerReply
      }
  }

  override def broadcast(peerMessage: PeerMessage): Unit = {}
}
