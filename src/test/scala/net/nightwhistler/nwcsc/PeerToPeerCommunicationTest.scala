package net.nightwhistler.nwcsc

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, GivenWhenThen}

/**
  * Created by alex on 15-6-17.
  */
class PeerToPeerCommunicationTest extends FunSuite with GivenWhenThen with MockFactory {

  trait MockedBroadcast {
    val broadcastStub = mockFunction[PeerMessage, Unit]
    def broadcast(peerMessage: PeerMessage): Unit = broadcastStub(peerMessage)
  }

  test("A query all should lead to a reply of the full blockchain") {
    Given("a basic blockchain with 1 block")
    val peerToPeerCommunication = new PeerToPeerCommunication with MockedBroadcast {
      override def blockChain: BlockChain = BlockChain()
    }
    val reply = stubFunction[PeerMessage, Unit]
    peerToPeerCommunication.blockChain.addBlock("My test data")

    When("we query for the full chain")
    peerToPeerCommunication.handleMessage(PeerMessage(MessageType.QueryAll), reply)

    Then("We expect the full blockchain back")
    reply.verify(PeerMessage(MessageType.ResponseBlockChain, peerToPeerCommunication.blockChain.blocks))

  }
}
