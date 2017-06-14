package net.nightwhistler.nwcsc

import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success}

/**
  * Created by alex on 14-6-17.
  */
trait PeerToPeerCommunication {

  val logger = Logger("PeerToPeerCommunication")

  def blockChain: BlockChain

  object MessageType extends Enumeration {
    type MessageType = Value
    val QueryLatest, QueryAll, ResponseBlockChain = Value
  }

  def broadcast( peerMessage: PeerMessage )

  case class PeerMessage( messageType: MessageType.MessageType, blocks: Seq[Block])

  def handleMessage( message: PeerMessage, reply: PeerMessage => Unit) = message match {
    case PeerMessage(MessageType.QueryLatest, _) => reply(responseLatest)
    case PeerMessage(MessageType.QueryAll, _) => reply(reponseBlockChain)
    case PeerMessage(MessageType.ResponseBlockChain, chain) => handleBlockChainResponse(chain)
    case _ => logger.error("Got an unexpected peer-message, discarding")
  }

  def handleBlockChainResponse( receivedBlocks: Seq[Block] ) = receivedBlocks match {
    case Nil => logger.warn("Received an empty block list, discarding")
    case latestBlock :: _ =>
      val localLatestBlock = blockChain.getLatestBlock
      if ( latestBlock.index > localLatestBlock.index ) {
        logger.info(s"Blockchain possibly behind. We got: ${localLatestBlock.index} peer got: ${latestBlock.index}")
        if ( localLatestBlock.hash == latestBlock.hash ) {
          logger.info("We can append the received block to our chain.")
          blockChain.addBlock(latestBlock)
        } else if (receivedBlocks.length == 1) {
          logger.info("We have to query the chain from our peer")
          broadcast(PeerMessage(MessageType.QueryAll, Nil))
        }
        else {
          logger.info("Received blockchain is longer than the current blockchain")
          blockChain.replaceChain(receivedBlocks) match {
            case Success(_) => broadcast(responseLatest)
            case Failure(s) => logger.error("Not replacing the chain", s)
          }
        }
      } else {
        logger.debug("received blockchain is not longer than received blockchain. Do nothing")
      }
  }

  def responseLatest = PeerMessage(MessageType.ResponseBlockChain, Seq(blockChain.getLatestBlock))

  def reponseBlockChain = PeerMessage(MessageType.ResponseBlockChain, blockChain.blocks)

}


