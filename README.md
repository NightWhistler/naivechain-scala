Naivechain implementation in Scala
==================================

This is a very loose Scala port of [naivechain]( https://github.com/lhartikk/naivechain ).

It is mostly meant as an exercise for me to get a better grasp of how blockchains work, and at the same time sharpen my Scala coding and testing skills a bit.

It uses Akka and Akka-http. Peer to peer communication between nodes is straight akka remoting, and akka-http is used for a simple rest interface.

Building, running, etc
----------------------

The app can be run using docker-compose.

Start by building the docker container

  sbt docker:publishLocal

Then run

    docker-compose up

This will start 2 nodes.

Link the first node to the second node:

    curl -X POST -d "akka.tcp://BlockChain@172.18.0.2:2552/user/blockChainActor" http://localhost:9000/addPeer

NOTE: The IP address should the IP of node2, check the logging output to verify.

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


