package besu;

public class JsonApiCalls {
  public static final String ethGetBlockNumber =
      """
            {
                "jsonrpc": "2.0",
                "method": "eth_blockNumber",
                "params": [],
                "id": #{id}
            }
            """;
  public static final String ethGetBlockByNumberLatest =
      """
            {
                "jsonrpc": "2.0",
                "method": "eth_getBlockByNumber",
                "params": [
                    "latest",
                    true
                ],
                "id": #{id}
            }
            """;

  public static final String ethGetBlockByNumberRandom =
      """
            {
                "jsonrpc": "2.0",
                "method": "eth_getBlockByNumber",
                "params": [
                    "#{blockNumber}",
                    true
                ],
                "id": #{id}
            }
            """;
}
