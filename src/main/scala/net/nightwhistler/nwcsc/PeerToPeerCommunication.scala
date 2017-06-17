package net.nightwhistler.nwcsc

import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.PeerToPeerCommunication.MessageType.{QueryAll, QueryLatest, ResponseBlockChain}
import net.nightwhistler.nwcsc.blockchain.{Block, BlockChain}

import scala.util.{Failure, Success}

/**
  * Created by alex on 14-6-17.
  */

object PeerToPeerCommunication {

  object MessageType extends Enumeration {
    type MessageType = Value
    val QueryLatest, QueryAll, ResponseBlockChain = Value
  }

  case class PeerMessage(messageType: MessageType.MessageType, blocks: Seq[Block] = Nil)

}

trait PeerToPeerCommunication {

  import PeerToPeerCommunication._

  val logger = Logger("PeerToPeerCommunication")

  var blockChain: BlockChain

  def broadcast( peerMessage: PeerMessage )

  def handleMessage( message: PeerMessage)(reply: PeerMessage => Unit ): Unit = message match {
    case PeerMessage(QueryLatest, _) => reply(responseLatest)
    case PeerMessage(QueryAll, _) => reply(reponseBlockChain)
    case PeerMessage(ResponseBlockChain, chain) => handleBlockChainResponse(chain)
    case _ => logger.error("Got an unexpected peer-message, discarding")
  }

  def handleBlockChainResponse( receivedBlocks: Seq[Block] ): Unit = {
    val localLatestBlock = blockChain.latestBlock
    logger.info(s"${receivedBlocks.length} blocks received.")

    receivedBlocks match {
      case Nil => logger.warn("Received an empty block list, discarding")

      case latestReceivedBlock :: _ if latestReceivedBlock.index <= localLatestBlock.index =>
        logger.debug("received blockchain is not longer than received blockchain. Do nothing")

      case latestReceivedBlock :: Nil if latestReceivedBlock.previousHash == localLatestBlock.hash =>
         logger.info("We can append the received block to our chain.")
            blockChain.addBlock(latestReceivedBlock) match {
              case Success(newChain) =>
                blockChain = newChain
                broadcast(responseLatest)
              case Failure(e) => logger.error("Refusing to add new block", e)
            }
      case _ :: Nil =>
            logger.info("We have to query the chain from our peer")
            broadcast(PeerMessage(QueryAll))

      case _ =>
            logger.info("Received blockchain is longer than the current blockchain")
            BlockChain(receivedBlocks) match {
              case Success(newChain) =>
                blockChain = newChain
                broadcast(responseLatest)
              case Failure(s) => logger.error("Rejecting received chain.", s)
            }
    }
  }

  def responseLatest = PeerMessage(ResponseBlockChain, Seq(blockChain.latestBlock))

  def reponseBlockChain = PeerMessage(ResponseBlockChain, blockChain.blocks)

}


