public class GardenTest {
  private static final int NUM_PlANTS = 10;

  public static void main(String[] args) {
    Garden garden = new Garden();

    Thread mary = new Thread(new Mary(garden));
    Thread benjamin = new Thread(new Benjamin(garden));
    Thread newton = new Thread(new Newton(garden));

    mary.start();
    benjamin.start();
    newton.start();
  }

  /**
   * An implementation of the Newton thread.
   */
  static class Newton implements Runnable {
    private final Garden garden;

    public Newton(Garden garden) {
      this.garden = garden;
    }

    @Override
    public void run() {
      while (garden.totalHolesDugByNewton() < NUM_PlANTS) {
        try {
          garden.startDigging();
          dig();
        } catch (Exception e) {
          System.out.println("Error in Newton Thread.");
        } finally  {
          garden.doneDigging();
        }
      }
    }

    private void dig() throws InterruptedException {
      System.out.println("Newton is digging.\nStats before digging:\n" + garden + "\n");
    }
  }

  /**
   * An implementation of the Benjamin thread.
   */
  protected static class Benjamin implements Runnable {
    private final Garden garden;

    public Benjamin(Garden garden) {
      this.garden = garden;
    }

    @Override
    public void run() {
      while (garden.totalHolesSeededByBenjamin() < NUM_PlANTS) {
        try  {
          garden.startSeeding();
          plantSeed();
        } catch (Exception e) {
          System.out.println("Error in Benjamin Thread.");
        } finally {
          garden.doneSeeding();
        }
      }
    }

    private void plantSeed() throws InterruptedException {
      System.out.println("Benjamin is seeding.\nStats before seeding:\n" + garden + "\n");
    }
  }

  /**
   * An implementation of the Mary thread.
   */
  protected static class Mary implements Runnable {
    private final Garden garden;

    public Mary(Garden garden) {
      this.garden = garden;
    }

    @Override
    public void run() {
      while (garden.totalHolesFilledByMary() < NUM_PlANTS) {
        try {
          garden.startFilling();
          fill();
        } catch (Exception e) {
          System.out.println("Error in Mary Thread.");
        } finally {
          garden.doneFilling();
        }
      }
    }

    private void fill(){
      System.out.println("Mary is filling.\nStats before filling:\n" + garden + "\n");
    }
  }

}