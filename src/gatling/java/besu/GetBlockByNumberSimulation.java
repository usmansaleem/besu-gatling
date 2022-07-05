package besu;

import static besu.JsonApiCalls.ethGetBlockByNumberLatest;
import static besu.JsonApiCalls.ethGetBlockByNumberRandom;
import static besu.JsonApiCalls.ethGetBlockNumber;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;

public class GetBlockByNumberSimulation extends Simulation {
  final String host = System.getProperty("besu-rpc-host", "localhost");
  final Integer port = Integer.getInteger("besu-rpc-port", 8545);
  final String baseUrl = "http://" + host + ":" + port;

  // will be updated on the first call of getBlockNumber scenario
  final AtomicLong blockNumber = new AtomicLong();

  // feed random id and block number
  final Iterator<Map<String, Object>> latestBlockFeeder =
      Stream.generate((Supplier<Map<String, Object>>) () -> Map.of("id", RandomUtils.nextLong()))
          .iterator();

  final Iterator<Map<String, Object>> healthCheckFeeder =
      Stream.generate((Supplier<Map<String, Object>>) () -> Map.of("id", RandomUtils.nextLong()))
          .iterator();

  final Iterator<Map<String, Object>> getBlockNumberFeed =
      Stream.generate((Supplier<Map<String, Object>>) () -> Map.of("id", RandomUtils.nextLong()))
          .iterator();

  final Iterator<Map<String, Object>> randomBlockFeeder =
      Stream.generate(
              (Supplier<Map<String, Object>>)
                  () ->
                      Map.of(
                          "id",
                          RandomUtils.nextLong(),
                          "blockNumber",
                          RandomUtils.nextLong(0, blockNumber.get())))
          .iterator();

  // fetch the latest block number at start of test execution
  final ScenarioBuilder getBlockNumber =
      scenario("Get Block Number")
          .feed(getBlockNumberFeed)
          .exec(
              http("Get Block Number (start)")
                  .post("/")
                  .body(StringBody(ethGetBlockNumber))
                  .asJson()
                  .check(status().is(200))
                  .check(jsonPath("$.result").saveAs("blockNumber")))
          .exec(
              session -> {
                blockNumber.set(
                    new BigInteger(session.get("blockNumber").toString().substring(2), 16)
                        .longValue());
                System.out.println("Block Number:" + blockNumber.get());
                return session;
              });

  final ScenarioBuilder healthCheck =
      scenario("Health Check")
          .feed(healthCheckFeeder)
          .exec(
              http("Get Block Number (Health check)")
                  .post("/")
                  .body(StringBody(ethGetBlockNumber))
                  .asJson()
                  .check(status().is(200)));

  final ScenarioBuilder getLatestBlock =
      scenario("Get Latest Block")
          .feed(latestBlockFeeder)
          .exec(
              http("get latest block")
                  .post("/")
                  .body(StringBody(ethGetBlockByNumberLatest))
                  .asJson()
                  .check(status().is(200))
                  .check(jsonPath("$.id").isEL("#{id}")));

  final ScenarioBuilder getRandomBlock =
      scenario("Get Random Block")
          .feed(randomBlockFeeder)
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
