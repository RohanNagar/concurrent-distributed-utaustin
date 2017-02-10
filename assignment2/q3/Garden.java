import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Garden {
  private static final int UNFILLED_MAX = 8;
  private static final int UNSEEDED_MAX = 4;

  private final ReentrantLock seedLock;
  private final ReentrantLock shovelLock;

  private final Condition emptyHole;
  private final Condition seededHole;
  private final Condition holesAhead;

  private final AtomicInteger holesDug;
  private final AtomicInteger holesSeeded;
  private final AtomicInteger holesFilled;

  public Garden() {
    this.seedLock = new ReentrantLock();
    this.shovelLock = new ReentrantLock();

    this.emptyHole = seedLock.newCondition();
    this.seededHole = shovelLock.newCondition();
    this.holesAhead = shovelLock.newCondition();

    this.holesDug = new AtomicInteger();
    this.holesSeeded = new AtomicInteger();
    this.holesFilled = new AtomicInteger();
  }

  public void startDigging() throws InterruptedException {
    shovelLock.lock();
    while (holesDug.get() - holesFilled.get() >= UNFILLED_MAX
        || holesDug.get() - holesSeeded.get() >= UNSEEDED_MAX) {
      // While there are UNFILLED_MAX or more unfilled holes
      // or there are UNSEEDED_MAX or more unseeded holes, wait to dig more
      holesAhead.await();
    }
  }

  public void doneDigging() {
    // Increment number of holes dug and let go of shovel
    int dug = holesDug.incrementAndGet();
    int seeded = holesSeeded.get();

    shovelLock.unlock();

    if (dug > seeded) {
      // We need to notify Benjamin to start seeding
      seedLock.lock();
      try {
        emptyHole.signal();
      } finally {
        seedLock.unlock();
      }
    }
  }

  public void startSeeding() throws InterruptedException {
    seedLock.lock();
    while (holesDug.get() <= holesSeeded.get()) {
      // While there are no dug holes to seed, wait to seed more
      emptyHole.await();
    }
  }

  public void doneSeeding() {
    // Increment number of holes seeded and let go of seed lock
    int seeded = holesSeeded.incrementAndGet();
    int filled = holesFilled.get();
    int dug = holesDug.get();

    seedLock.unlock();
    shovelLock.lock();

    try {
      // We need to notify Mary to start filling
      if (seeded > filled) {
        seededHole.signal();
      }

      // Also notify Newton to check if he can dig more (he has to wait for less than 4 seeded)
      if (dug - seeded < UNSEEDED_MAX) {
        holesAhead.signal();
      }

    } finally {
      shovelLock.unlock();
    }
  }

  public void startFilling() throws InterruptedException {
    shovelLock.lock();

    while (holesSeeded.get() <= holesFilled.get()) {
      // While there are no holes to fill, wait to fill more
      seededHole.await();
    }
  }

  public void doneFilling() {
    holesFilled.incrementAndGet();
    holesAhead.signal();
    shovelLock.unlock();
  }

  /*
  * The following methods return the total number of holes dug, seeded or
  * filled by Newton, Benjamin or Mary at the time the methods' are
  * invoked on the garden class. */
  public synchronized int totalHolesDugByNewton() {
    return holesDug.get();
  }

  public synchronized int totalHolesSeededByBenjamin() {
    return holesSeeded.get();
  }

  public synchronized int totalHolesFilledByMary() {
    return holesFilled.get();
  }

  @Override
  public String toString() {
    return "holes dug: " + holesDug
        + "\nholes seeded: " + holesSeeded
        + "\nholes filled: " + holesFilled;
  }
}
