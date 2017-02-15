/*
 * EIDs of group members
 * ran679, kmg2969
 */

public class FairReadWriteLock {
  private boolean inWrite = false;
  private int readCount = 0;
  private int writeCount = 0;

  public synchronized void beginRead() {
    while (inWrite || writeCount != 0) {
      try {
        wait();
      } catch (InterruptedException e) {
        System.out.println("Error in beginRead.");
      }
    }
    readCount++;

  }

  public synchronized void endRead() {
    readCount--;
    notifyAll();
  }

  public synchronized void beginWrite() {
    writeCount++;
    while (readCount != 0 || inWrite) {
      try {
        wait();
      } catch (InterruptedException e) {
        System.out.println("Error in beginWrite.");
      }
    }
    writeCount--;
    inWrite = true;
  }

  public synchronized void endWrite() {
    inWrite = false;
    notifyAll();
  }
}
