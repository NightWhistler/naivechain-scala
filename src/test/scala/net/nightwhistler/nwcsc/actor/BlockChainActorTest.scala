package net.nightwhistler.nwcsc.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import net.nightwhistler.nwcsc.actor.BlockChainActor.{AddPeer, GetPeers, MineBlock, Peers}
import net.nightwhistler.nwcsc.blockchain.GenesisBlock
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.MessageType.ResponseBlockChain
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.{MessageType, PeerMessage}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, FlatSpecLike, GivenWhenThen}

/**
  * Created by alex on 17-6-17.
  */
class BlockChainActorTest extends TestKit(ActorSystem("BlockChain")) with FlatSpecLike
  with ImplicitSender with GivenWhenThen with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  trait BlockChainActorTest {
    val blockChainActor = system.actorOf(Props[BlockChainActor])
  }

  "A BlockChainActor " should " start with an empty set of peers" in new BlockChainActorTest {
      blockChainActor ! GetPeers
      expectMsg(Peers(Nil))
  }

  it should "register new peers" in new BlockChainActorTest {
    blockChainActor ! AddPeer("MyTestPeer")
    blockChainActor ! GetPeers
    expectMsgPF() {
      case Peers(Seq(address)) => assert(address.endsWith("MyTestPeer"))
    }
  }

  it should "start sending broadcast to a peer after it is registered" in new BlockChainActorTest {
    blockChainActor ! AddPeer(testActor.path.toStringWithoutAddress)
    blockChainActor ! MineBlock("testBlock")
    expectMsgPF() {
      case PeerMessage(ResponseBlockChain, Seq(block)) => assert(block.data == "testBlock")
    }
  }

  it should "send the blockchain to anybody that requests it" in new BlockChainActorTest {
    blockChainActor ! PeerMessage(MessageType.QueryAll)
    expectMsg(PeerMessage(ResponseBlockChain, Seq(GenesisBlock)))
  }

}
