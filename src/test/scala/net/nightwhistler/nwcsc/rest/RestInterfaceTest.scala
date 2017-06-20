package net.nightwhistler.nwcsc.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestKitBase, TestProbe}
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.blockchain.BlockChainCommunication.{QueryAll, QueryLatest, ResponseBlock, ResponseBlockChain}
import net.nightwhistler.nwcsc.blockchain.Mining.MineBlock
import net.nightwhistler.nwcsc.blockchain.{Block, BlockChain, GenesisBlock}
import net.nightwhistler.nwcsc.p2p.PeerToPeer.{AddPeer, GetPeers, Peers}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

/**
  * Created by alex on 17-6-17.
  */

class RestInterfaceTest extends FlatSpec with ScalatestRouteTest with TestKitBase
  with Matchers {

  trait RestInterfaceFixture extends RestInterface {
    val testProbe = TestProbe()

    override val blockChainActor: ActorRef = testProbe.ref
    override val logger = Logger("TestLogger")
    override implicit val executionContext: ExecutionContext = ExecutionContext.global
  }

  "A route " should "get the blockchain from the blockchain actor for /blocks" in new RestInterfaceFixture {

    testProbe.setAutoPilot { (sender: ActorRef, msg: Any) => msg match {
      case QueryAll =>
        sender ! ResponseBlockChain(BlockChain())
        TestActor.NoAutoPilot
      }
    }

    Get("/blocks") ~> routes ~> check {
      responseAs[Seq[Block]] shouldEqual Seq(GenesisBlock)
    }
  }

  it should "return the latest block for /latestBlock" in new RestInterfaceFixture {
    testProbe.setAutoPilot { (sender: ActorRef, msg: Any) => msg match {
      case QueryLatest =>
        sender ! ResponseBlock(GenesisBlock)
        TestActor.NoAutoPilot
      }
    }
    Get("/latestBlock") ~> routes ~> check {
      responseAs[Block] shouldEqual GenesisBlock
    }
  }

  it should "retrieve all peers for /peers" in new RestInterfaceFixture {
    testProbe.setAutoPilot { (sender: ActorRef, msg: Any) => msg match {

      case GetPeers =>
        sender ! Peers(Seq("PeerOne"))
        TestActor.NoAutoPilot
      }
    }

    Get("/peers") ~> routes ~> check {
      responseAs[Peers] shouldEqual Peers(Seq("PeerOne"))
    }
  }

  it should "add a new peer for /addPeer" in new RestInterfaceFixture {
    Post("/addPeer", HttpEntity(ContentTypes.`text/html(UTF-8)`, "TestPeer")) ~> routes ~> check {
      testProbe.expectMsg(AddPeer("TestPeer"))
    }
  }


  it should "add a new block for /addBlock" in new RestInterfaceFixture {
    testProbe.setAutoPilot { (sender: ActorRef, msg: Any) => msg match {
      case MineBlock(data) =>
        sender ! ResponseBlock(Block(0, "", 0, data, ""))
        TestActor.NoAutoPilot
      }
    }

    Post("/mineBlock", HttpEntity(ContentTypes.`text/html(UTF-8)`, "MyBlock")) ~> routes ~> check {
      responseAs[Block] shouldEqual(Block(0, "", 0, "MyBlock", ""))
    }
  }

}


