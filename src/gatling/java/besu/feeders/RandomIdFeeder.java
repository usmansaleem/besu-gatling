package besu.feeders;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;

public class RandomIdFeeder {
  public java.util.Iterator<Map<String, Object>> getRandomIdFeeder() {
    return Stream.generate(
            (Supplier<Map<String, Object>>) () -> Map.of("id", RandomUtils.nextLong()))
        .iterator();
  }
}
