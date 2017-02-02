// UT-EID: ran679, kmg2969

import java.util.*;
import java.util.concurrent.*;

public class PSearch {

  /**
   * Performs a search in parallel for an integer in an array.
   *
   * @param k The integer to search for
   * @param A The array to search in
   * @param numThreads The number of threads to use simultaneously
   * @return The index where the number is located. Returns -1 if not found.
   */
  public static int parallelSearch(int k, int[] A, int numThreads) {
    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    Set<Future<Integer>> futures = new HashSet<>();
    int subarraySize = A.length / numThreads;
    int remainder = A.length % numThreads;
    int startIndex = 0;

    // Submit tasks
    for (int i = 0; i < numThreads; i++) {
      int extra = i < remainder ? 1 : 0;

      Callable<Integer> callable
          = new Searcher(startIndex, k, Arrays.copyOfRange(A, startIndex, startIndex + subarraySize + extra));
      Future<Integer> future = executorService.submit(callable);
      futures.add(future);

      startIndex += (subarraySize + extra);
    }

    // Determine if a thread found it
    int index = -1;
    for (Future<Integer> f : futures) {
      try {
        int temp = f.get();
        if (temp != -1) index = temp;
      } catch (InterruptedException | ExecutionException e) {
        System.out.println("A concurrent exception occurred: " + e.getMessage());
      }
    }

    executorService.shutdown();
    return index;
  }

  /**
   * A class to perform a search over a given sub-array. This class should be
   * used in a thread pool.
   */
  private static class Searcher implements Callable<Integer> {
    private final int startIndex;
    private final int x;
    private final int[] A;

    /**
     * Constructs a new Searcher object.
     *
     * @param startIndex The index of the original array that this sub-array starts at
     * @param x The integer to search for
     * @param A The sub-array to search in
     */
    Searcher(int startIndex, int x, int[] A) {
      this.startIndex = startIndex;
      this.x = x;
      this.A = A;
    }

    /**
     * Performs the search for the provided integer.
     *
     * @return The index of the integer in the original array, not the sub-array. Returns
     *  -1 if not found.
     */
    @Override
    public Integer call() {
      for (int i = 0; i < A.length; i++) {
        if (A[i] == x) return startIndex + i;
      }

      return -1;
    }
  }

}
