package net.nightwhistler.nwcsc

import java.util.Date

import com.roundeights.hasher.Implicits._
import com.typesafe.scalalogging.Logger

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * Created by alex on 13-6-17.
  */

object BlockChain {
  def apply(): BlockChain = new BlockChain()
}

class BlockChain {

  val logger = Logger("BlockChain")

  val genesisBlock = Block(0, "0", 1497359352, "Genesis block", "ccce7d8349cf9f5d9a9c8f9293756f584d02dfdb953361c5ee36809aa0f560b4")

  var blocks: Seq[Block] = Seq(genesisBlock)

  def addBlock( data: String ): Unit = addBlock(generateNextBlock(data))

  def addBlock( block: Block ): Unit = {
    if (isValidBlock(block, getLatestBlock)) {
      blocks = Seq(block) ++ blocks
    }
  }

  def replaceChain( newChain: Seq[Block] ): Try[Unit] = {
    if ( isValidChain(newChain) ) {
      blocks = newChain
      Success()
    } else {
      Failure(new IllegalArgumentException("Invalid chain provided"))
    }
  }

  def getFirstBlock: Block = blocks(blocks.length -1)
  def getLatestBlock: Block = blocks.head

  def calculateHashForBlock( block: Block ) = calculateHash(block.index, block.previousHash, block.timestamp, block.data)

  def calculateHash(index: Int, previousHash: String, timestamp: Long, data: String) =
    s"$index:$previousHash:$timestamp:$data".sha256.hex

  def generateNextBlock( blockData: String ) = {
    val previousBlock = getLatestBlock
    val nextIndex = previousBlock.index + 1
    val nextTimestamp = new Date().getTime() / 1000
    val nextHash = calculateHash(nextIndex, previousBlock.hash, nextTimestamp, blockData)

    Block(nextIndex, previousBlock.hash, nextTimestamp, blockData, nextHash)
  }

  def isValidBlock( newBlock: Block, previousBlock: Block ): Boolean =
    previousBlock.index +1 == newBlock.index &&
    previousBlock.hash == newBlock.previousHash &&
    calculateHashForBlock(newBlock) == newBlock.hash

  def isValidChain( chain: Seq[Block] ): Boolean = {
    chain match {
      case singleBlock :: Nil if singleBlock == genesisBlock => true
      case head :: before :: tail if isValidBlock(head, before) => isValidChain(before :: tail)
      case _ => false
    }
  }

}

case class Block(index: Int, previousHash: String, timestamp: Long, data: String, hash: String)


