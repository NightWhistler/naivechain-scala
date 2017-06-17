package net.nightwhistler.nwcsc

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.actor.BlockChainActor
import net.nightwhistler.nwcsc.rest.RestInterface


object BlockChainApp extends App with RestInterface {

  implicit val system = ActorSystem("BlockChain")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val blockChainActor = system.actorOf(BlockChainActor.props, "blockChainActor")

  val config = ConfigFactory.load()
  val logger = Logger("WebServer")

  logger.debug(s"Path for actor is ${blockChainActor.path}")

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
