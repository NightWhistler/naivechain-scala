package net.nightwhistler.nwcsc.actor

import akka.actor.Props
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.blockchain.{BlockChain, BlockChainCommunication, Mining}
import net.nightwhistler.nwcsc.p2p.PeerToPeer

object BlockChainActor {
  def props( blockChain: BlockChain ): Props = Props(classOf[BlockChainActor], blockChain)
}

class BlockChainActor( var blockChain: BlockChain ) extends CompositeActor with PeerToPeer
  with BlockChainCommunication with Mining {
  override val logger = Logger("BlockChainActor")
}





