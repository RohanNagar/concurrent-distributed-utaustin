import java.util.Arrays;

public class SimpleTest {
  public static void main (String[] args) {
	//Pass in arguments to the main thread if you so wish but beautiful tests have been implemented
    int[] A1 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
    verifyParallelSort(A1);
    //But if you must, test away
    int[] A2 = {1, 3, 5, 7, 9};
    verifyParallelSort(A2);
    //These first 3 are part of the original simple test. they only test for one case of our QuickSort implementation 
    int[] A3 = {13, 59, 24, 18, 33, 20, 11, 11, 13, 50, 10999, 97};
    verifyParallelSort(A3);
    //Negatives just for shits and a side of giggles
	int[] A4 = {-3450, 6, 0, 0, 0, 6000000}; // test some crazy absurd number ranges
	verifyParallelSort(A4);
	//what only one greater
	int[] A5 = {0, 0, 0, 0, 0, 1}; // last comment preaches
	verifyParallelSort(A5);
	//I'm now realizing there could more tests
	int[] A6 = {-1, 0, 0, 0, 0, 0}; // round 3 of yours truly
	verifyParallelSort(A6);
	
    
  }

  static void verifyParallelSort(int[] A) {
    int[] B = new int[A.length];
    System.arraycopy(A, 0, B, 0, A.length);

    System.out.println("Verify Parallel Sort for array: ");
    printArray(A);

    Arrays.sort(A);
    PSort.parallelSort(B, 0, B.length);
   
    boolean isSuccess = true;
    for (int i = 0; i < A.length; i++) {
      if (A[i] != B[i]) {
        System.out.println("Your parallel sorting algorithm is not correct");
        System.out.println("Expect:");
        printArray(A);
        System.out.println("Your results:");
        printArray(B);
        isSuccess = false;
        break;
      }
    }

    if (isSuccess) {
      System.out.println("Great, your sorting algorithm works for this test case");
    }
    System.out.print(Arrays.toString(B));
    System.out.println("");
    System.out.println("=========================================================");
  }

  public static void printArray(int[] A) {
    for (int i = 0; i < A.length; i++) {
      if (i != A.length - 1) {
        System.out.print(A[i] + " ");
      } else {
        System.out.print(A[i]);
      }
    }
    System.out.println();
  }
}
