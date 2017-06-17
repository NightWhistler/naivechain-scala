package net.nightwhistler.nwcsc.p2p

import net.nightwhistler.nwcsc.p2p.PeerManagement.Peer

/**
  * Created by alex on 17-6-17.
  */
object PeerManagement {
  case class Peer(address: String)
}

trait PeerManagement {

  var peers: Seq[Peer]



}
