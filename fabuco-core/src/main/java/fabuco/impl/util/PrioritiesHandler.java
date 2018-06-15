package fabuco.impl.util;

import java.util.stream.IntStream;

public class PrioritiesHandler {

  private PriorityInfo[] priorities;

  public PrioritiesHandler(int maxPriority) {
    priorities = IntStream.rangeClosed(1, maxPriority)
        .mapToObj(i -> new PriorityInfo(i)).toArray(PriorityInfo[]::new);
  }

  public long getIndex(int priority, long lastGlobalIndex) {
    return priorities[priority].getIndex(lastGlobalIndex);
  }

  static class PriorityInfo {

    int priority;
    int coef;
    int offset;
    long currentIndex = 0;

    PriorityInfo(int priority) {
      this.priority = priority;
      coef = 1 << (priority - 1);
      offset = 1 << priority;
    }

    long getIndex(long lastGlobalIndex) {
      if (lastGlobalIndex > currentIndex) {
        calcCurrentIndex(lastGlobalIndex);
      } else {
        currentIndex += offset;
      }
      return currentIndex;
    }

    void calcCurrentIndex(long lastGlobalIndex) {
      long val = lastGlobalIndex >> priority << priority;
      if (lastGlobalIndex - val >= coef) {
        val += offset;
      }
      currentIndex = val + coef;
    }

  }
}
