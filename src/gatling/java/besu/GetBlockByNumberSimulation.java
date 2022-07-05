package besu;

import static besu.JsonApiCalls.ethGetBlockByNumberLatest;
import static besu.JsonApiCalls.ethGetBlockByNumberRandom;
import static besu.JsonApiCalls.ethGetBlockNumber;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.*;

import besu.feeders.RandomBlockNumberFeeder;
import besu.feeders.RandomIdFeeder;
import io.gatling.javaapi.core.*;
import java.math.BigInteger;
import java.time.Duration;

public class GetBlockByNumberSimulation extends Simulation {
  final String host = System.getProperty("besu-rpc-host", "localhost");
  final Integer port = Integer.getInteger("besu-rpc-port", 8545);
  final String baseUrl = "http://" + host + ":" + port;

  // will be updated on the first call of getBlockNumber scenario
  final RandomBlockNumberFeeder randomBlockNumberFeeder = new RandomBlockNumberFeeder();

  // fetch the latest block number at start of test execution
  final ScenarioBuilder getBlockNumber =
      scenario("Get Block Number")
          .feed(new RandomIdFeeder().getRandomIdFeeder())
          .exec(
              http("Get Block Number (start)")
                  .post("/")
                  .body(StringBody(ethGetBlockNumber))
                  .asJson()
                  .check(status().is(200))
                  .check(jsonPath("$.result").saveAs("blockNumber")))
          .exec(
              session -> {
                randomBlockNumberFeeder.setHighestBlockNumber(
                    new BigInteger(session.get("blockNumber").toString().substring(2), 16)
                        .longValue());
                return session;
              });

  final ScenarioBuilder healthCheck =
      scenario("Health Check")
          .feed(new RandomIdFeeder().getRandomIdFeeder())
          .exec(
              http("Get Block Number (Health check)")
                  .post("/")
                  .body(StringBody(ethGetBlockNumber))
                  .asJson()
                  .check(status().is(200)));

  final ScenarioBuilder getLatestBlock =
      scenario("Get Latest Block")
          .feed(new RandomIdFeeder().getRandomIdFeeder())
          .exec(
              http("get latest block")
                  .post("/")
                  .body(StringBody(ethGetBlockByNumberLatest))
                  .asJson()
                  .check(status().is(200))
                  .check(jsonPath("$.id").isEL("#{id}")));

  final ScenarioBuilder getRandomBlock =
      scenario("Get Random Block")
          .feed(randomBlockNumberFeeder.getRandomBlockFeeder())
          .exec(
              http("get random block")
                  .post("/")
                  .body(StringBody(ethGetBlockByNumberRandom))
                  .asJson()
                  .check(status().is(200))
                  .check(jsonPath("$.id").isEL("#{id}")));

  {
    System.out.println("Running Gatling Scenarios on " + baseUrl);
    setUp(
            getBlockNumber
                .injectOpen(atOnceUsers(1))
                .andThen(
                    healthCheck.injectOpen(constantUsersPerSec(1).during(Duration.ofMinutes(5))),
                    getLatestBlock.injectOpen(constantUsersPerSec(2).during(Duration.ofMinutes(5))),
                    getRandomBlock.injectOpen(
                        constantUsersPerSec(2).during(Duration.ofMinutes(5)))))
        .protocols(
            http.baseUrl(baseUrl)
                .acceptHeader("*/*")
                .acceptLanguageHeader("en-US,en;q=0.5")
                .acceptEncodingHeader("gzip, deflate, br")
                .contentTypeHeader("application/json")
                .userAgentHeader("Gatling Test"));
  }
}
