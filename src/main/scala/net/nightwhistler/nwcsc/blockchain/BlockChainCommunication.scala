package net.nightwhistler.nwcsc.blockchain

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.p2p.PeerToPeer

import scala.util.{Failure, Success}

/**
  * Created by alex on 14-6-17.
  */

object BlockChainCommunication {

  case object QueryLatest
  case object QueryAll

  case class ResponseBlockChain(blockChain: BlockChain)
  case class ResponseBlock(block: Block)

}

trait BlockChainCommunication {
  this: PeerToPeer with Actor =>

  import BlockChainCommunication._

  val logger = Logger("PeerToPeerCommunication")

  var blockChain: BlockChain

  def receiveBlockChainMessage: Receive = {
    case QueryLatest => sender() ! responseLatest
    case QueryAll => sender() ! reponseBlockChain

    //FIXME: This is inefficient
    case ResponseBlock(block) => handleBlockChainResponse(Seq(block))
    case ResponseBlockChain(blockChain) => handleBlockChainResponse(blockChain.blocks)
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
            broadcast(QueryAll)

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

  def responseLatest = ResponseBlock(blockChain.latestBlock)

  def reponseBlockChain = ResponseBlockChain(blockChain)

}


