NightWhistler's Completely Stupid Coin
======================================

This is a _very_ naive crypto-coin implementation, written in Scala.

It is mostly based on naivechain [https://github.com/lhartikk/naivechain], but the aim is to add full cryptocurrency support.

It is mostly meant as an exercise for me to get a better grasp of how cryptocurrencies work, and at the same time test my understanding of Scala code and testing.

Once we're functionally on par with naivechain, we'll move on to naivecoin[https://github.com/conradoqg/naivecoin]

Building, running, etc
----------------------

There is not much to run yet, but unit tests can be run using

    sbt test

Code coverage is done using scoverage.

Run
    sbt coverage test

to measure, and then

    sbt coverageReport

to generate a report.

Docker
------

To build the Docker container, run

    sbt docker:publishLocal
