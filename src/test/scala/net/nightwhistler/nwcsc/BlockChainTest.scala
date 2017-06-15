package net.nightwhistler.nwcsc

import com.typesafe.scalalogging.Logger
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}


/**
  * Created by alex on 13-6-17.
  */
object BlockChainTest extends Properties("BlockChain") {

  val logger = Logger("BlockChainTest")

  val blockChainGen: Gen[BlockChain] = for {
    length <- Gen.choose(0, 30)
    text <- Gen.listOfN(length, Gen.alphaNumStr)
  } yield {
    val chain = new BlockChain {}
    text.foreach { data => chain.addBlock(chain.generateNextBlock(data)) }
    chain
  }

  property("Generated chains should always be correct") = forAll(blockChainGen) { chain =>
    chain.isValidChain( chain.blocks )
  }

  property("Adding an invalid block should never work") = forAll(
    for {
      blockChain <- blockChainGen
      firstName <- Gen.alphaNumStr
      secondName <- Gen.alphaNumStr
    } yield ((blockChain, firstName, secondName))) { case (chain, firstName, secondName) =>

    val firstNewBlock = chain.generateNextBlock(firstName)
    val secondNewBlock = chain.generateNextBlock(secondName)

    val currentBlockLength = chain.blocks.length

    chain.addBlock(firstNewBlock)
    chain.addBlock(secondNewBlock)

    chain.blocks.length == currentBlockLength +1 && chain.getLatestBlock == firstNewBlock
  }
}
