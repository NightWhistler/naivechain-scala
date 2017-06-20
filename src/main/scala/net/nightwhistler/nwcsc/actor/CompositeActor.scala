package net.nightwhistler.nwcsc.actor

import akka.actor.Actor
import akka.actor.Actor.emptyBehavior

/**
  * Created by alex on 20-6-17.
  */
class CompositeActor extends Actor {
  var receivers: Receive = emptyBehavior
  def receiver(next: Receive) { receivers = receivers orElse next }
  final def receive = receivers
}
