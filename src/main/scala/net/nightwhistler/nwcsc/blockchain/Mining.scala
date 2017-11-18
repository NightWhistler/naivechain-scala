package net.nightwhistler.nwcsc.blockchain

import net.nightwhistler.nwcsc.actor.CompositeActor
import net.nightwhistler.nwcsc.blockchain.BlockChainCommunication.ResponseBlock
import net.nightwhistler.nwcsc.blockchain.Mining.MineBlock
import net.nightwhistler.nwcsc.p2p.PeerToPeer

/**
  * Created by alex on 20-6-17.
  */
object Mining {
  case class MineBlock( data: String )
}


trait Mining {
  this: BlockChainCommunication with PeerToPeer with CompositeActor =>

  receiver {
    case MineBlock(data) =>
      blockChain = blockChain.addBlock(data)
      val peerMessage = ResponseBlock(blockChain.latestBlock)
      broadcast(peerMessage)
      sender() ! peerMessage
  }
}
