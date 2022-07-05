Gatling project for Besu JSON-RPC
=============================================

A sample Gatling application to test and measure Besu JSON-RPC. To run the tests:

```bash
./gradlew gatlingRun -Dbesu-rpc-host=localhost -Dbesu-rpc-port=8545
```
Note: Make sure to use JDK 17+ is available on path or set as JAVA_HOME environment variable.

See `src/gatling/java/besu/GetBlockByNumberSimulation.java` for scenario setup.