package net.nightwhistler.nwcsc

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, GivenWhenThen}

/**
  * Created by alex on 15-6-17.
  */
class PeerToPeerCommunicationTest extends FunSuite with GivenWhenThen with MockFactory {

  trait StubbedBroadcast {
    val broadcastStub = stubFunction[PeerMessage, Unit]
    def broadcast(peerMessage: PeerMessage): Unit = broadcastStub(peerMessage)
  }

  abstract class SimplePeerToPeerCommunication(val blockChain: BlockChain) extends PeerToPeerCommunication

  test("A query all should lead to a reply of the full blockchain") {
    Given("a basic blockchain with 1 block")
    val blockChain = BlockChain()
    val peerToPeerCommunication = new SimplePeerToPeerCommunication(blockChain) with StubbedBroadcast

    val reply = stubFunction[PeerMessage, Unit]
    blockChain.addBlock("My test data")

    When("we query for the full chain")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.QueryAll), reply)

    Then("We expect the full blockchain back")
    reply.verify(PeerMessage(MessageType.ResponseBlockChain, blockChain.blocks))
  }

  test("Querying for the latest block should yield the latest block, and nothing more.") {
    Given("a basic blockchain with 1 block")
    val blockChain = BlockChain()
    val peerToPeerCommunication = new SimplePeerToPeerCommunication(blockChain) with StubbedBroadcast

    val reply = stubFunction[PeerMessage, Unit]
    blockChain.addBlock("My test data")

    When("we query for a single block")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.QueryLatest), reply)

    Then("We only expect the latest block")
    reply.verify( PeerMessage(MessageType.ResponseBlockChain, Seq(blockChain.getLatestBlock)))
  }
}
