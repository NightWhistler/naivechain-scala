package net.nightwhistler.nwcsc

import net.nightwhistler.nwcsc.PeerToPeerCommunication.MessageType.{QueryAll, ResponseBlockChain}
import net.nightwhistler.nwcsc.PeerToPeerCommunication.{MessageType, PeerMessage}
import net.nightwhistler.nwcsc.blockchain.BlockChain
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, GivenWhenThen}

/**
  * Created by alex on 15-6-17.
  */
class PeerToPeerCommunicationTest extends FlatSpec with GivenWhenThen with MockFactory {

  trait StubbedBroadcast {
    val broadcastStub = stubFunction[PeerMessage, Unit]
    def broadcast(peerMessage: PeerMessage): Unit = broadcastStub(peerMessage)
  }

  abstract class SimplePeerToPeerCommunication(var blockChain: BlockChain) extends PeerToPeerCommunication

  trait SingleBlockTest {
    Given("a basic blockchain with 1 block")
    val peerToPeerCommunication = new SimplePeerToPeerCommunication(BlockChain()) with StubbedBroadcast
    val reply = stubFunction[PeerMessage, Unit]
    peerToPeerCommunication.blockChain = peerToPeerCommunication.blockChain.addBlock("My test data")
  }

  "An incoming PeerMessage " should "lead to a reply of the full blockchain if it contains a QueryAll request" in new SingleBlockTest {
    When("we query for the full chain")
    peerToPeerCommunication.handleMessage(PeerMessage(QueryAll))(reply)

    Then("We expect the full blockchain back")
    reply.verify(PeerMessage(ResponseBlockChain, peerToPeerCommunication.blockChain.blocks))
  }

  it should "yield the latest block, and nothing more for a QueryLatest request" in new SingleBlockTest {
    When("we query for a single block")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.QueryLatest))(reply)

    Then("we only expect the latest block")
    reply.verify( PeerMessage(ResponseBlockChain, Seq(peerToPeerCommunication.blockChain.latestBlock)))
  }

  it should "not cause any issues for a null request, but simply discard it" in new SingleBlockTest {
    When("we pass in a null message")
    peerToPeerCommunication.handleMessage(null)(reply)

    Then("We expect the message to be discarded")
    reply.verify(*).never()
    peerToPeerCommunication.broadcastStub.verify(*).never()
  }

  "An incoming BlockChain message " should "cause the block to be attached to the current chain if it has exactly 1 new block" in new SingleBlockTest {

    Given("and a new chain containing an extra block")
    val nextBlock = peerToPeerCommunication.blockChain.generateNextBlock("Some more data")
    val oldBlocks = peerToPeerCommunication.blockChain.blocks

    When("we receive a message with the longer chain")
    peerToPeerCommunication.handleMessage(PeerMessage(ResponseBlockChain, Seq(nextBlock)))(reply)

    Then("the new block should be added to the chain, and a broadcast should be sent")
    assertResult( nextBlock +: oldBlocks )( peerToPeerCommunication.blockChain.blocks )
    peerToPeerCommunication.broadcastStub.verify(PeerMessage(ResponseBlockChain, Seq(nextBlock)))
    reply.verify(*).never()
  }

  it should "replace the chain if more than 1 new block is received" in new SingleBlockTest {
    Given("A blockchain with 3 new blocks")
    val blockData = Seq("aap", "noot", "mies")
    val longerChain = blockData.foldLeft(peerToPeerCommunication.blockChain) { case (chain, data) =>
      chain.addBlock(data)
    }

    When("we receive this longer chain")
    peerToPeerCommunication.handleMessage(PeerMessage(ResponseBlockChain, longerChain.blocks))(reply)

    Then("The chain should be replaced, and a broadcast should be sent")
    assertResult( longerChain.blocks )( peerToPeerCommunication.blockChain.blocks )
    peerToPeerCommunication.broadcastStub.verify(PeerMessage(ResponseBlockChain, Seq(longerChain.latestBlock)))
    reply.verify(*).never()

  }

  it should "do nothing if the received chain is empty" in new SingleBlockTest {
    When("we receive an empty block list")
    peerToPeerCommunication.handleMessage(PeerMessage(ResponseBlockChain, Nil))(reply)

    Then("the blockchain should be unchanged")
    assertResult(2)(peerToPeerCommunication.blockChain.blocks.length)

    Then("none of the methods should be called")
    reply.verify(*).never()
    peerToPeerCommunication.broadcastStub.verify(*).never()
  }

  it should "also do nothing if the received chain is valid but shorter than the current one" in new SingleBlockTest {
    Given("the old blockchain and a current blockchain which is longer")
    val oldBlockChain = peerToPeerCommunication.blockChain
    val newBlockChain = oldBlockChain .addBlock("Some new data") .addBlock("And more")

    peerToPeerCommunication.blockChain = newBlockChain

    When("we receive the old blockchain")
    peerToPeerCommunication.handleMessage(PeerMessage(ResponseBlockChain, oldBlockChain.blocks))(reply)

    Then("We expect the message to be discarded")
    reply.verify(*).never()
    peerToPeerCommunication.broadcastStub.verify(*).never()
    assertResult(newBlockChain)(peerToPeerCommunication.blockChain)
  }

  it should "query for the full chain when we receive a single block that is further ahead in the chain" in new SingleBlockTest {
    Given("a later version of the blockchain which is 2 blocks ahead")
    val oldBlockChain = peerToPeerCommunication.blockChain
    val newBlockChain = peerToPeerCommunication.blockChain
      .addBlock("Some new data") .addBlock("And more")

    When("we receive the head of this blockchain")
    peerToPeerCommunication.handleMessage(PeerMessage(ResponseBlockChain, Seq(newBlockChain.latestBlock)))(reply)

    Then("expect a query for the full blockchain")
    reply.verify(*).never()
    peerToPeerCommunication.broadcastStub.verify(PeerMessage(QueryAll)).once()

    Then("we expect the blockchain to be unchanged")
    assertResult(oldBlockChain)(peerToPeerCommunication.blockChain)

  }

}
