package net.nightwhistler.nwcsc.rest

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import net.nightwhistler.nwcsc.blockchain.BlockChainCommunication.{QueryAll, ResponseBlock, ResponseBlockChain}
import net.nightwhistler.nwcsc.blockchain.Mining.MineBlock
import net.nightwhistler.nwcsc.blockchain.{Block, GenesisBlock}
import net.nightwhistler.nwcsc.p2p.PeerToPeer.{AddPeer, GetPeers, Peers}
import org.json4s.{DefaultFormats, Formats, native}

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by alex on 16-6-17.
  */
trait RestInterface extends Json4sSupport {

  val blockChainActor: ActorRef
  val logger: Logger

  implicit val serialization = native.Serialization
  implicit val stringUnmarshallers = PredefinedFromEntityUnmarshallers.stringUnmarshaller

  implicit def json4sFormats: Formats = DefaultFormats

  implicit val executionContext: ExecutionContext

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val routes =
   path("mineBlock") {
        post {
          entity(as[String]) { data =>
            logger.info(s"Got request to add new block $data")
            complete((blockChainActor ? MineBlock(data)).mapTo[ResponseBlock].map {
              case ResponseBlock(block) => block
            })
          }
        }
    }~
    path("blocks") {
        get {
          val chain: Future[Seq[Block]] = (blockChainActor ? QueryAll).map {
            //This is a bit of a hack, since JSON4S doesn't serialize case objects well
            case ResponseBlockChain(blockChain) => blockChain.blocks.slice(0, blockChain.blocks.length -1) :+ GenesisBlock.copy()
          }
          complete(chain)
        }
      }~
    path("peers") {
        get {
          complete( (blockChainActor ? GetPeers).mapTo[Peers] )
        }
      }~
    path("addPeer") {
        post {
          entity(as[String]) { peerAddress =>
            logger.info(s"Got request to add new peer $peerAddress")
            blockChainActor ! AddPeer(peerAddress)
            complete(s"Added peer $peerAddress")
          }
        }
    }


}
