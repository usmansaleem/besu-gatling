Gatling project for Besu JSON-RPC
=============================================

A sample Gatling application to test and measure Besu JSON-RPC. To run the tests:

```bash
./gradlew gatlingRun -Dbesu-rpc-host=localhost -Dbesu-rpc-port=8545
```

## Scenario `GetBlockByNumberSimulation` Total 10 users per seconds for 5 minutes
1. Use `eth_blockNumber` to obtain the latest block number and then perform following two scenarios
   (change the code to change the user rates and duration if required.)
2. `eth_getBlockByNumber` using `latest` (5 users per seconds for 5 minutes)
3. `eth_getBlockByNumber` using random block number between 0 and latest block number calculated in first step. (5 users per seconds for 5 minutes)

