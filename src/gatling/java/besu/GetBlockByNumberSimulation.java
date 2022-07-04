package besu;

import static besu.JsonApiCalls.ethGetBlockByNumberLatest;
import static besu.JsonApiCalls.ethGetBlockByNumberRandom;
import static besu.JsonApiCalls.ethGetBlockNumber;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import java.math.BigInteger;
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

  final AtomicLong blockNumber = new AtomicLong();

  // feed random id and block number
  final Iterator<Map<String, Object>> feeder =
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
          .exec(
              http("eth_blockNumber")
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

  final ScenarioBuilder getLatestBlock =
      scenario("Get Latest Block")
          .feed(feeder)
          .exec(
              http("get latest block")
                  .post("/")
                  .body(StringBody(ethGetBlockByNumberLatest))
                  .asJson()
                  .check(status().is(200))
                  .check(jsonPath("$.id").isEL("#{id}")));

  final ScenarioBuilder getRandomBlock =
      scenario("Get Random Block")
          .feed(feeder)
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
                    getLatestBlock.injectOpen(constantUsersPerSec(2).during(15)),
                    getRandomBlock.injectOpen(constantUsersPerSec(2).during(15))))
        .protocols(
            http.baseUrl(baseUrl)
                .acceptHeader("*/*")
                .acceptLanguageHeader("en-US,en;q=0.5")
                .acceptEncodingHeader("gzip, deflate, br")
                .contentTypeHeader("application/json")
                .userAgentHeader("Gatling Test"));
  }
}
