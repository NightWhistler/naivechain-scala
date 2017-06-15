package net.nightwhistler.nwcsc

import java.util.Date

import com.roundeights.hasher.Implicits._
import com.typesafe.scalalogging.Logger

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * Created by alex on 13-6-17.
  */
case class Block(index: Int, previousHash: String, timestamp: Long, data: String, hash: String)

object GenesisBlock extends Block(0, "0", 1497359352, "Genesis block", "ccce7d8349cf9f5d9a9c8f9293756f584d02dfdb953361c5ee36809aa0f560b4")

object BlockChain {
  def apply(): BlockChain = new BlockChain(Seq(GenesisBlock))

  def apply(blocks: Seq[Block]): Try[BlockChain] = {
    if ( validChain(blocks) ) Success(new BlockChain(blocks))
    else Failure(new IllegalArgumentException("Invalid chain specified."))
  }

  def validChain( chain: Seq[Block] ): Boolean = {
    chain match {
      case singleBlock :: Nil if singleBlock == GenesisBlock => true
      case head :: beforeHead :: tail if validBlock(head, beforeHead) => validChain(beforeHead :: tail)
      case _ => false
    }
  }

  def validBlock(newBlock: Block, previousBlock: Block) =
    previousBlock.index + 1 == newBlock.index &&
    previousBlock.hash == newBlock.previousHash &&
    calculateHashForBlock(newBlock) == newBlock.hash

  def calculateHashForBlock( block: Block ) = calculateHash(block.index, block.previousHash, block.timestamp, block.data)

  def calculateHash(index: Int, previousHash: String, timestamp: Long, data: String) =
    s"$index:$previousHash:$timestamp:$data".sha256.hex
}

class BlockChain private( val blocks: Seq[Block] ) {

  import BlockChain._

  val logger = Logger("BlockChain")

  def addBlock( data: String ): Try[BlockChain] = addBlock(generateNextBlock(data))

  def addBlock( block: Block ): Try[ BlockChain ] =
    if ( validBlock(block) ) Success( new BlockChain(block +: blocks ))
    else Failure( new IllegalArgumentException("Invalid block added"))

  def firstBlock: Block = blocks(blocks.length -1)
  def latestBlock: Block = blocks.head

  def generateNextBlock( blockData: String ): Block = {
    val previousBlock = latestBlock
    val nextIndex = previousBlock.index + 1
    val nextTimestamp = new Date().getTime() / 1000
    val nextHash = calculateHash(nextIndex, previousBlock.hash, nextTimestamp, blockData)

    Block(nextIndex, previousBlock.hash, nextTimestamp, blockData, nextHash)
  }

  def validBlock( newBlock: Block ): Boolean = BlockChain.validBlock(newBlock, latestBlock)

  override def toString: String = s"$blocks"

}



