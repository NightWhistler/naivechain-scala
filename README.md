Naivechain implementation in Scala
==================================

This is a very loose Scala port of [naivechain]( https://github.com/lhartikk/naivechain ), which also takes some inspiration from [legion](https://github.com/aviaviavi/legion).

It is mostly meant as an exercise for me to get a better grasp of how blockchains work, and at the same time sharpen my Scala coding and testing skills a bit.

It uses Akka and Akka-http. Peer to peer communication between nodes is straight akka remoting, and akka-http is used for a simple rest interface.

Nodes spin up a peer-to-peer network: connecting a node to another node will cause it to connect to all the nodes in the network.


Building, running, etc
----------------------

The app can be run using docker-compose.

Start by building the docker container

    sbt docker:publishLocal

Then run

    docker-compose up

This will start 4 nodes, which will first connect to node1 (the seed node) and then proceed to build a P2P network.

Then mine a block:

    curl -X POST -d "My data for the block" http://localhost:9000/mineBlock

All nodes should show the block:

    curl http://localhost:9000/blocks                                                                                              <<<
    curl http://localhost:9001/blocks                                                                                              <<<
    curl http://localhost:9002/blocks                                                                                              <<<
    curl http://localhost:9003/blocks                                                                                              <<<

Testing and code coverage
-------------------------

Code coverage is done using scoverage.

Run

    sbt coverage test

to measure, and then

    sbt coverageReport

to generate a report.


