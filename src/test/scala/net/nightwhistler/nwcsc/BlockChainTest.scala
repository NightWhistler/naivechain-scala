package net.nightwhistler.nwcsc

import com.typesafe.scalalogging.Logger
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties}
import org.scalatest.FlatSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks


/**
  * Created by alex on 13-6-17.
  */
class BlockChainTest extends FlatSpec with GeneratorDrivenPropertyChecks {

  implicit val arbitraryChain: Arbitrary[BlockChain] = Arbitrary {
    for {
      length <- Gen.choose(0, 30)
      text <- Gen.listOfN(length, Gen.alphaNumStr)
    } yield {
      val chain = BlockChain()
      text.foreach { data => chain.addBlock(chain.generateNextBlock(data)) }
      chain
    }
  }

  "Generated chains" should "always be correct" in forAll { chain: BlockChain =>
    assert( chain.isValidChain( chain.blocks ) )
  }

  "Adding an invalid block" should "never work" in forAll { (chain: BlockChain, firstName: String, secondName: String) =>

    val firstNewBlock = chain.generateNextBlock(firstName)
    val secondNewBlock = chain.generateNextBlock(secondName)

    val currentBlockLength = chain.blocks.length

    chain.addBlock(firstNewBlock)

    assert( ! chain.isValidBlock(secondNewBlock) )
    chain.addBlock(secondNewBlock)

    assertResult(chain.blocks.length)(currentBlockLength +1)
    assertResult(chain.getLatestBlock)(firstNewBlock)
  }
}
