/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.util.ArrayList;
import java.util.List;

public class FairReadWriteLock {
  private boolean inWrite = false;
  private int readCount = 0;

  private final List<Long> timestamps = new ArrayList<>();
  private final List<Long> writeTimestamps = new ArrayList<>();

  public synchronized void beginRead() {
    // Acquire timestamp
    Long timestamp = System.currentTimeMillis();
    timestamps.add(timestamp);

    while (inWrite || (writeTimestamps.size() > 0 && writeTimestamps.get(0) < timestamp)) {
      try {
        wait();
      } catch (InterruptedException e) {
        System.out.println("Error in beginRead.");
      }
    }

    // Enter read section, remove self from timestamps list
    timestamps.remove(timestamp);
    readCount++;
  }

  public synchronized void endRead() {
    readCount--;
    notifyAll();
  }

  public synchronized void beginWrite() {
    // Acquire timestamp
    Long timestamp = System.currentTimeMillis();
    timestamps.add(timestamp);
    writeTimestamps.add(timestamp);

    while (readCount != 0 || inWrite || !timestamps.get(0).equals(timestamp)) {
      try {
        wait();
      } catch (InterruptedException e) {
        System.out.println("Error in beginWrite.");
      }
    }

    // We can now enter the write - remove ourselves from the lists used for waiting conditions
    timestamps.remove(0);
    writeTimestamps.remove(0);
    inWrite = true;
  }

  public synchronized void endWrite() {
    inWrite = false;
    notifyAll();
  }
}
