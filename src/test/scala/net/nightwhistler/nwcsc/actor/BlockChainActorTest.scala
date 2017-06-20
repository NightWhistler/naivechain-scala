package net.nightwhistler.nwcsc.actor

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import net.nightwhistler.nwcsc.actor.BlockChainActor._
import net.nightwhistler.nwcsc.blockchain.GenesisBlock
import net.nightwhistler.nwcsc.p2p.BlockChainCommunication.MessageType.ResponseBlockChain
import net.nightwhistler.nwcsc.p2p.BlockChainCommunication.{MessageType, PeerMessage}
import org.scalatest._

/**
  * Created by alex on 17-6-17.
  */
class BlockChainActorTest extends TestKit(ActorSystem("BlockChain")) with FlatSpecLike
  with ImplicitSender with GivenWhenThen with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  trait BlockChainActorTest {
    val blockChainActor = system.actorOf(BlockChainActor.props)
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
    val probe = TestProbe()
    blockChainActor ! AddPeer(probe.ref.path.toStringWithoutAddress)
    blockChainActor ! MineBlock("testBlock")

    probe.expectMsg(HandShake)
    probe.expectMsg(GetPeers)

    probe.expectMsgPF(){
      case PeerMessage(ResponseBlockChain, Seq(block)) => assert(block.data == "testBlock")
    }

    expectMsgPF() {
      case PeerMessage(ResponseBlockChain, Seq(block)) => assert(block.data == "testBlock")
    }
  }

  it should "add us as a peer when we send a handshake" in new BlockChainActorTest {
    blockChainActor ! HandShake
    blockChainActor ! MineBlock("testBlock")

    val pf: PartialFunction[Any, Boolean] = { case PeerMessage(ResponseBlockChain, _) => true}

    //We should get it twice: once as the sender and once as a peer
    expectMsgPF()(pf)
    expectMsgPF()(pf)
  }

  it should "reply with the new block when a mining request is finished" in new BlockChainActorTest {

    blockChainActor ! MineBlock("testBlock")

    expectMsgPF() {
      case PeerMessage(ResponseBlockChain, Seq(block)) => assert(block.data == "testBlock")
    }

  }

  it should "handle a list of peers by adding them one by one" in new BlockChainActorTest {

    Given("an initial peer")
    val peerProbe = TestProbe()

    blockChainActor ! AddPeer(peerProbe.ref.path.toStringWithoutAddress)
    peerProbe.expectMsg(HandShake)
    peerProbe.expectMsg(GetPeers)

    When("we register 2 new peers")
    val probes = Seq(TestProbe(), TestProbe()).map(_.ref.path.toStringWithoutAddress)
    blockChainActor ! Peers(probes)

    Then("the original peer should receive a notification for each one")
    peerProbe.expectMsg(AddPeer(probes(0)))
    peerProbe.expectMsg(AddPeer(probes(1)))

  }

  it should "send the blockchain to anybody that requests it" in new BlockChainActorTest {
    blockChainActor ! PeerMessage(MessageType.QueryAll)
    expectMsg(PeerMessage(ResponseBlockChain, Seq(GenesisBlock)))
  }

}
