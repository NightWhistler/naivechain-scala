package net.nightwhistler.nwcsc.rest

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.MessageType.QueryAll
import net.nightwhistler.nwcsc.actor.BlockChainActor.{GetPeers, MineBlock, Peers}
import net.nightwhistler.nwcsc.blockchain.{Block, GenesisBlock}
import net.nightwhistler.nwcsc.p2p.PeerToPeerCommunication.PeerMessage
import org.json4s.{DefaultFormats, Formats, native}

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by alex on 16-6-17.
  */
trait RestInterface extends Json4sSupport {

  val blockChainActor: ActorRef

  implicit val serialization = native.Serialization
  implicit val stringUnmarshallers = PredefinedFromEntityUnmarshallers.stringUnmarshaller

  implicit def json4sFormats: Formats = DefaultFormats

  implicit val executionContext: ExecutionContext

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val routes =
     path("mineBlock") {
        post {
          entity(as[String]) { data =>
            blockChainActor ! MineBlock(data)
            complete("Hallo")
          }
        }
    }~
    path("blocks") {
        get {
          val chain: Future[Seq[Block]] = (blockChainActor ? PeerMessage(QueryAll)).map {
            //This is a bit of a hack, since JSON4S doesn't serialize case objects well
            case PeerMessage(_, blocks) => blocks.slice(0, blocks.length -1) :+ GenesisBlock.copy()
          }
          complete(chain)
        }
      }~
    path("peers") {
        get {
          val peerAddresses: Future[Seq[String]] = (blockChainActor ? GetPeers).map {
            //This is a bit of a hack, since JSON4S doesn't serialize case objects well
            case Peers(peers) => peers.map( ref => ref.path.toStringWithoutAddress )
          }
          complete(peerAddresses)
        }
      }


}
