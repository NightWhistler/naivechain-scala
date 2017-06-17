package net.nightwhistler.nwcsc

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import net.nightwhistler.nwcsc.actor.BlockChainActor
import net.nightwhistler.nwcsc.rest.RestInterface


object WebServer extends App with RestInterface {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val blockChainActor = system.actorOf(Props[BlockChainActor])

  val config = ConfigFactory.load()
  val logger = Logger("WebServer")

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
