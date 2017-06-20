package net.nightwhistler.nwcsc.actor

import akka.actor.{Actor, Props}
import net.nightwhistler.nwcsc.blockchain.{BlockChain, BlockChainCommunication, Mining}
import net.nightwhistler.nwcsc.p2p.PeerToPeer

/**
  * Actor-based implementation of PeerToPeerCommunication
  */
object BlockChainActor {
  def props: Props = Props[BlockChainActor]
}

class BlockChainActor extends Actor with BlockChainCommunication
  with PeerToPeer with Mining {

  override var blockChain: BlockChain = BlockChain()

  override def receive: Receive = {
    receiveBlockChainMessage
        .orElse( receivePeerToPeer )
          .orElse( receiveMining )
  }

}
