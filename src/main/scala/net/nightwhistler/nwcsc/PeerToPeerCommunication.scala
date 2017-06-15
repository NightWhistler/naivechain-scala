package net.nightwhistler.nwcsc

import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success}

/**
  * Created by alex on 14-6-17.
  */
object MessageType extends Enumeration {
    type MessageType = Value
    val QueryLatest, QueryAll, ResponseBlockChain = Value
}

case class PeerMessage( messageType: MessageType.MessageType, blocks: Seq[Block] = Nil)

trait PeerToPeerCommunication {

  val logger = Logger("PeerToPeerCommunication")

  var blockChain: BlockChain

  def broadcast( peerMessage: PeerMessage )

  def handleMessage( message: PeerMessage, reply: PeerMessage => Unit): Unit = message match {
    case PeerMessage(MessageType.QueryLatest, _) => reply(responseLatest)
    case PeerMessage(MessageType.QueryAll, _) => reply(reponseBlockChain)
    case PeerMessage(MessageType.ResponseBlockChain, chain) => handleBlockChainResponse(chain)
    case _ => logger.error("Got an unexpected peer-message, discarding")
  }

  def handleBlockChainResponse( receivedBlocks: Seq[Block] ): Unit = receivedBlocks match {
    case Nil => logger.warn("Received an empty block list, discarding")
    case latestReceivedBlock :: _ =>
      val localLatestBlock = blockChain.latestBlock
      if ( latestReceivedBlock.index > localLatestBlock.index ) {
        logger.info(s"Blockchain possibly behind. We got: ${localLatestBlock.index} peer got: ${latestReceivedBlock.index}")
        if ( localLatestBlock.hash == latestReceivedBlock.previousHash ) {
          logger.info("We can append the received block to our chain.")
          blockChain.addBlock(latestReceivedBlock) match {
            case Success(newChain) =>
              blockChain = newChain
              broadcast(responseLatest)
            case Failure(e) => logger.error("Refusing to add new block", e)
          }
        } else if (receivedBlocks.length == 1) {
          logger.info("We have to query the chain from our peer")
          broadcast(PeerMessage(MessageType.QueryAll))
        } else {
          logger.info("Received blockchain is longer than the current blockchain")
          BlockChain(receivedBlocks) match {
            case Success(newChain) =>
              blockChain = newChain
              broadcast(responseLatest)
            case Failure(s) => logger.error("Rejecting received chain.", s)
          }
        }
      } else {
        logger.debug("received blockchain is not longer than received blockchain. Do nothing")
      }
  }

  def responseLatest = PeerMessage(MessageType.ResponseBlockChain, Seq(blockChain.latestBlock))

  def reponseBlockChain = PeerMessage(MessageType.ResponseBlockChain, blockChain.blocks)

}


