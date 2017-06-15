package net.nightwhistler.nwcsc

import net.nightwhistler.nwcsc.BlockChain.validChain
import org.scalacheck.{Arbitrary, Gen}
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
    assert( validChain( chain.blocks ) )
  }

  "For any given chain, the first block" must "be the Genesis block" in forAll { chain: BlockChain =>
    assertResult(GenesisBlock)(chain.firstBlock)
  }

  "Adding an invalid block" should "never work" in forAll { (chain: BlockChain, firstName: String, secondName: String) =>

    val firstNewBlock = chain.generateNextBlock(firstName)
    val secondNewBlock = chain.generateNextBlock(secondName)

    val currentBlockLength = chain.blocks.length

    val newChain = chain.addBlock(firstNewBlock).getOrElse( throw new IllegalStateException("Should succeed"))

    assert( ! newChain.validBlock(secondNewBlock) )
    newChain.addBlock(secondNewBlock)

    assertResult(newChain.blocks.length)(currentBlockLength +1)
    assertResult(newChain.latestBlock)(firstNewBlock)
  }
}
