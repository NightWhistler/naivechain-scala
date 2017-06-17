NightWhistler's Completely Stupid Coin
======================================

This is a _very_ naive crypto-coin implementation, written in Scala.

It is mostly based on naivechain [https://github.com/lhartikk/naivechain], but the aim is to add full cryptocurrency support.

It is mostly meant as an exercise for me to get a better grasp of how cryptocurrencies work, and at the same time test my understanding of Scala code and testing.

Once we're functionally on par with naivechain, we'll move on to naivecoin[https://github.com/conradoqg/naivecoin]

It uses Akka and Akka-http. Peer to peer communication between nodes is straight akka remoting, and akka-http is used for simple rest interface.

Building, running, etc
----------------------

The app can be run using docker-compose:

Start by building the docker container

  sbt docker:publishLocal

Then run

  docker-compose up

This will start 2 nodes.

Link the first node to the second node:

    curl -X POST -d "akka.tcp://BlockChain@172.18.0.2:2552/user/blockChainActor" http://localhost:9000/addPeer

Then mine a block:

    curl -X POST -d "My data for the block" http://localhost:9000/mineBlock

Now both nodes should show the block:

    curl http://localhost:9001/blocks                                                                                              <<<
    curl http://localhost:9000/blocks                                                                                              <<<

Testing and code coverage
-------------------------

Code coverage is done using scoverage.

Run
    sbt coverage test

to measure, and then

    sbt coverageReport

to generate a report.


