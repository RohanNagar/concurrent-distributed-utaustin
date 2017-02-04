public class MonitorCyclicBarrier {
  private final int parties;
  private int count;
  private int resets;

  public MonitorCyclicBarrier(int parties) {
    this.parties = parties;
    this.count = parties;
    this.resets = 0;
  }

  public synchronized int await() throws InterruptedException {
    count--;
    int index = count;

    if (index == 0) {
      // If we are at zero, all threads have arrived
      // Increment reset count and notify other threads
      count = parties;
      resets++;
      notifyAll();

      return 0;
    } else {
      // Else, we need to wait
      int r = resets;

      while (r == resets) {
        // Resets variable will have changed if we are actually done waiting
        // Otherwise we were woken up on accident, need to keep waiting
        wait();
      }

      return index;
    }
  }
}
