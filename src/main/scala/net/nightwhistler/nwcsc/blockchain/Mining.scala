package net.nightwhistler.nwcsc.blockchain

import akka.actor.Actor
import akka.stream.stage.GraphStageLogic.StageActorRef.Receive
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
  this: BlockChainCommunication with PeerToPeer with Actor =>

  def receiveMining: Receive = {
        case MineBlock(data) =>
          blockChain = blockChain.addBlock(data)
          val peerMessage = ResponseBlock(blockChain.latestBlock)
          broadcast(peerMessage)
          sender() ! peerMessage
  }

}
