package fabuco.impl.util.counter;

public class LongCounterAnalyzer {

  private final short maxTickNumber;
  private final long minThreshold;
  private final long[] decCounters;
  private long counter = 0;
  private short decCounterPos = 0;
  private long counterThreshold;
  private long decCounterTick = 0;
  private short tickNumber = 0;

  public LongCounterAnalyzer(short maxTickNumber, long minThreshold) {
    this.maxTickNumber = maxTickNumber;
    this.minThreshold = counterThreshold = minThreshold;
    decCounters = new long[maxTickNumber];
  }

  public void inc() {
    counter++;
  }

  public void add(long value) {
    counter += value;
  }

  public boolean incAndCheckForExceedingThreshold() {
    inc();
    return checkForExceedingThreshold();
  }

  public boolean addAndCheckForExceedingThreshold(long value) {
    add(value);
    return checkForExceedingThreshold();
  }

  public void dec() {
    counter--;
    decCounterTick++;
  }

  public void deduct(long value) {
    counter -= value;
    decCounterTick += value;
  }

  public boolean decAndCheckForExceedingThreshold() {
    dec();
    return checkForExceedingThreshold();
  }

  public boolean deductAndCheckForExceedingThreshold(long value) {
    deduct(value);
    return checkForExceedingThreshold();
  }

  public boolean checkForExceedingThreshold() {
    return counter >= counterThreshold;
  }

  public boolean tickAndCheckForExceedingThreshold() {
    if (tickNumber == maxTickNumber) {
      counterThreshold += decCounterTick;
      counterThreshold -= decCounters[decCounterPos];
    } else {
      if (++tickNumber == maxTickNumber) {
        counterThreshold = 0;
        for (long decCounter : decCounters) {
          counterThreshold += decCounter;
        }
      }
    }

    if (counterThreshold < minThreshold) {
      counterThreshold = minThreshold;
    }

    decCounters[decCounterPos] = decCounterTick;
    decCounterTick = 0;

    if (++decCounterPos == maxTickNumber) {
      decCounterPos = 0;
    }

    return checkForExceedingThreshold();
  }
}
