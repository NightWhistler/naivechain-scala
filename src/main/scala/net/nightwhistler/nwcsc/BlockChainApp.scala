package net.nightwhistler.nwcsc

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.actor.BlockChainActor
import net.nightwhistler.nwcsc.blockchain.BlockChain
import net.nightwhistler.nwcsc.p2p.PeerToPeer.AddPeer
import net.nightwhistler.nwcsc.rest.RestInterface


object BlockChainApp extends App with RestInterface {

  implicit val system = ActorSystem("BlockChain")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val blockChainActor = system.actorOf(BlockChainActor.props(BlockChain()), "blockChainActor")

  val config = ConfigFactory.load()
  val logger = Logger("WebServer")

  val seedHost = config.getString("blockchain.seedHost")

  if ( ! seedHost.isEmpty ) {
    logger.info(s"Attempting to connect to seed-host ${seedHost}")
    blockChainActor ! AddPeer(seedHost)
  } else {
    logger.info("No seed host configured, waiting for messages.")
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
