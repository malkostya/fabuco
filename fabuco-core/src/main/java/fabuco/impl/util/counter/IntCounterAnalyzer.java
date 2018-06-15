package fabuco.impl.util.counter;

public class IntCounterAnalyzer {

  private final short maxTickNumber;
  private final int minThreshold;
  private final int[] decCounters;
  private int counter = 0;
  private short decCounterPos = 0;
  private int counterThreshold;
  private int decCounterTick = 0;
  private short tickNumber = 0;

  public IntCounterAnalyzer(short maxTickNumber, int minThreshold) {
    this.maxTickNumber = maxTickNumber;
    this.minThreshold = counterThreshold = minThreshold;
    decCounters = new int[maxTickNumber];
  }

  public void inc() {
    counter++;
  }

  public void add(int value) {
    counter += value;
  }

  public boolean incAndCheckForExceedingThreshold() {
    inc();
    return checkForExceedingThreshold();
  }

  public boolean addAndCheckForExceedingThreshold(int value) {
    add(value);
    return checkForExceedingThreshold();
  }

  public void dec() {
    counter--;
    decCounterTick++;
  }

  public void deduct(int value) {
    counter -= value;
    decCounterTick += value;
  }

  public boolean decAndCheckForExceedingThreshold() {
    dec();
    return checkForExceedingThreshold();
  }

  public boolean deductAndCheckForExceedingThreshold(int value) {
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
        for (int decCounter : decCounters) {
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
