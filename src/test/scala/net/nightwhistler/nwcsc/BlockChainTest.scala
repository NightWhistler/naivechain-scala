package net.nightwhistler.nwcsc

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import Arbitrary.arbitrary


/**
  * Created by alex on 13-6-17.
  */
object BlockChainTest extends Properties("Block") {

  val blockChain = new BlockChain {}

  val palindromeGen: Gen[Seq[Block]] = for {
    length <- Gen.choose(0, 30)
    text <- Gen.listOfN(length, Gen.alphaNumStr)
  } yield {
    text.foldLeft( Seq(blockChain.genesisBlock) ) {
      case (blocks, data) => blockChain.generateNextBlock(data, blocks.head) +: blocks
    }
  }

  property("Generated chains should always be correct") = forAll(palindromeGen) { chain =>
    blockChain.isValidChain(chain)
  }
}
