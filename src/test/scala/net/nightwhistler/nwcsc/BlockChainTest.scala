package net.nightwhistler.nwcsc

import com.typesafe.scalalogging.Logger
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}


/**
  * Created by alex on 13-6-17.
  */
object BlockChainTest extends Properties("Block") {

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
    logger.debug(s"Validating chain of length ${chain.blocks.length}")
    chain.isValidChain( chain.blocks )
  }
}
