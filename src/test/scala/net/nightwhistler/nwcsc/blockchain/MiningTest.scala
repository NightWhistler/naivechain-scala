package net.nightwhistler.nwcsc.blockchain

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import net.nightwhistler.nwcsc.actor.CompositeActor
import net.nightwhistler.nwcsc.blockchain.BlockChainCommunication.ResponseBlock
import net.nightwhistler.nwcsc.blockchain.Mining.MineBlock
import net.nightwhistler.nwcsc.p2p.PeerToPeer
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, GivenWhenThen}

/**
  * Created by alex on 20-6-17.
  */

class TestMiningActor extends CompositeActor with Mining with PeerToPeer with BlockChainCommunication {
  var blockChain = BlockChain()
}

class MiningTest extends TestKit(ActorSystem("BlockChain")) with FlatSpecLike
  with ImplicitSender with GivenWhenThen with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  trait WithMiningActor {
    val miningActor = system.actorOf(Props[TestMiningActor])
  }

  "A Mining actor" should "reply with the new block when a mining request is finished" in new WithMiningActor {

    miningActor ! MineBlock("testBlock")

    expectMsgPF() {
      case ResponseBlock(block) => assert(block.data == "testBlock")
    }

  }



}
