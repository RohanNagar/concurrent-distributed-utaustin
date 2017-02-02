// UT-EID: ran679, kmg2969

import java.util.*;
import java.util.concurrent.*;

public class PSort {

  /**
   * Performs a sort on the given array in parallel.
   *
   * @param A The array to sort
   * @param begin The index to begin sorting at
   * @param end The index to stop sorting at
   */
  public static void parallelSort(int[] A, int begin, int end) {
    ExecutorService es = Executors.newSingleThreadExecutor();

    // Create the initial thread
    QuickSort sort = new QuickSort(A, begin, end);
    Future future = es.submit(sort);
    while (!future.isDone()); // Wait for QuickSort to finish

    // Shutdown
    es.shutdown();
    sort.threadPool.shutdown();
  }

  /**
   * A class to perform the QuickSort algorithm.
   */
  private static class QuickSort implements Runnable {
	  public static ExecutorService threadPool = Executors.newCachedThreadPool();

    private static int[] A;

	  private final int begin;
	  private final int end;
	  private final int n;

    /**
     * Constructs a new QuickSort object.
     *
     * @param A The array to sort
     * @param begin The index to start sorting at
     * @param end The index to finish sorting at
     */
	  QuickSort(int[] A, int begin, int end) {
		  this.begin = begin;
		  this.end = end;
		  this.A = A;
		  this.n = begin - end;
	  }

    /**
     * Runs the QuickSort algorithm for the provided parameters.
     */
	  @Override
	  public void run() {
		  // Do simple sorting with 4 or fewer elements
	    if (n <= 4) {
			  int[] subA = Arrays.copyOfRange(A, begin, end);
			  Arrays.sort(subA);
			  for (int i = begin; i < end; i++) {
				  A[i] = subA[i];
			  }
		  } else {
        int pivot = A[begin];
        int open = begin + 1;
        boolean openGreat = false;
        boolean hasRight = false;
        boolean hasLeft = false;
        int current;

        for (int i = begin + 1; i < end; i++) {
          current = A[i];
          if (current < pivot) {
            A[i] = A[open];
            A[open] = current;
            if (!hasRight) {
              open++;
            }
          } else {
            if (!hasRight) {  //found the first value greater than or equal to pivot
              openGreat = true;
              open = i;
            }
          }
        }

        int pivotid;
        if (hasRight) { // case where there is a left and right side
          A[begin] = A[open - 1];
          A[open - 1] = pivot;
          pivotid = open - 1;
        } else { // case where there is only values less than the pivot
          A[begin] = A[open];
          A[open] = pivot;
          pivotid = open;
        }

        // the array can be divided into 2 arrays by quick sort
        // so it submits two
        if (hasRight && hasLeft) {
          Future f1 = threadPool.submit(new QuickSort(A, begin, pivotid));
          Future f2 = threadPool.submit(new QuickSort(A, pivotid + 1, end));
          if (f1.isDone() && f2.isDone()) {
            return;
          }
        } else if (hasRight) {
          Future f1 = threadPool.submit(new QuickSort(A, pivotid + 1, end));
          if (f1.isDone()) {
            return;
          }
        } else if (hasLeft) {
          Future f1 = threadPool.submit(new QuickSort(A, begin, pivotid));
          if (f1.isDone()) {
            return;
          }
        }
      }
		  
	  }
  }
  
}
