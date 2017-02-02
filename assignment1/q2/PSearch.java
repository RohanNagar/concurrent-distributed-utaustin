// UT-EID: ran679, kmg2969

import java.util.*;
import java.util.concurrent.*;

public class PSearch {

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

  private static class Searcher implements Callable<Integer> {
    private final int startIndex;
    private final int x;
    private final int[] A;

    Searcher(int startIndex, int x, int[] A) {
      this.startIndex = startIndex;
      this.x = x;
      this.A = A;
    }

    public Integer call() {
      for (int i = 0; i < A.length; i++) {
        if (A[i] == x) return startIndex + i;
      }

      return -1;
    }
  }

}
