package besu.feeders;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;

public class RandomBlockNumberFeeder {
  private final AtomicLong highestBlockNumber = new AtomicLong();

  public void setHighestBlockNumber(final long blockNumber) {
    highestBlockNumber.set(blockNumber);
  }

  public Iterator<Map<String, Object>> getRandomBlockFeeder() {
    return Stream.generate(
            (Supplier<Map<String, Object>>)
                () ->
                    Map.of(
                        "id",
                        RandomUtils.nextLong(),
                        "blockNumber",
                        RandomUtils.nextLong(0, highestBlockNumber.get())))
        .iterator();
  }
}
