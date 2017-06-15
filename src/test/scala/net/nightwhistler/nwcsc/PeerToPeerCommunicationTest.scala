package net.nightwhistler.nwcsc

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, FunSuite, GivenWhenThen, fixture}

/**
  * Created by alex on 15-6-17.
  */
class PeerToPeerCommunicationTest extends FlatSpec with GivenWhenThen with MockFactory {

  trait StubbedBroadcast {
    val broadcastStub = stubFunction[PeerMessage, Unit]
    def broadcast(peerMessage: PeerMessage): Unit = broadcastStub(peerMessage)
  }

  abstract class SimplePeerToPeerCommunication(val blockChain: BlockChain) extends PeerToPeerCommunication

  trait SingleBlockTest {
    Given("a basic blockchain with 1 block")
    val blockChain = BlockChain()
    val peerToPeerCommunication = new SimplePeerToPeerCommunication(blockChain) with StubbedBroadcast
    val reply = stubFunction[PeerMessage, Unit]
    blockChain.addBlock("My test data")
  }

  "An incoming PeerMessage " should "lead to a reply of the full blockchain if it contains a QueryAll request" in new SingleBlockTest {
    When("we query for the full chain")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.QueryAll), reply)

    Then("We expect the full blockchain back")
    reply.verify(PeerMessage(MessageType.ResponseBlockChain, blockChain.blocks))
  }

  it should "yield the latest block, and nothing more for a QueryLatest request" in new SingleBlockTest {
    When("we query for a single block")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.QueryLatest), reply)

    Then("we only expect the latest block")
    reply.verify( PeerMessage(MessageType.ResponseBlockChain, Seq(blockChain.getLatestBlock)))
  }

  it should "not cause any issues for a null request, but simply discard it" in new SingleBlockTest {
    When("we pass in a null message")
    peerToPeerCommunication.handleMessage(null, reply)

    Then("We expect the message to be discarded")
    reply.verify(*).never()
    peerToPeerCommunication.broadcastStub.verify(*).never()
  }

  "An incoming BlockChain message " should "cause the block to be attached to the current chain if it has exactly 1 new block" in new SingleBlockTest {

    Given("and a new chain containing an extra block")
    val nextBlock = blockChain.generateNextBlock("Some more data")
    val longerChain = nextBlock +: blockChain.blocks

    When("we receive a message with the longer chain")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.ResponseBlockChain, longerChain), reply)

    Then("the new block should be added to the chain, and a broadcast should be sent")
    assertResult( longerChain )( blockChain.blocks )
    peerToPeerCommunication.broadcastStub.verify(PeerMessage(MessageType.ResponseBlockChain, Seq(nextBlock)))
    reply.verify(*).never()
  }

  it should "do nothing if the received chain is empty" in new SingleBlockTest {
    When("we receive an empty block list")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.ResponseBlockChain, Nil), reply)

    Then("the blockchain should be unchanged")
    assertResult(2)(blockChain.blocks.length)

    Then("none of the methods should be called")
    reply.verify(*).never()
    peerToPeerCommunication.broadcastStub.verify(*).never()
  }

}
