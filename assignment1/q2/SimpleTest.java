import java.util.Arrays;

public class SimpleTest {
  public static void main (String[] args) {
    
    int[] A1 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
    int x1 = 7;
    int numThread1 = 1;
    verifyParallelSearch(x1, A1, numThread1);
    
    int[] A2 = {1, 3, 5, 7, 9};
    int x2 = 8;
    int numThread2 = 2;
    verifyParallelSearch(x2, A2, numThread2);
    
    int[] A3 = {13, 59, 24, 18, 33, 20, 11, 50, 10999, 97};
    int x3 = 50;
    int numThread3 = 32;
    verifyParallelSearch(x3, A3, numThread3);
    
    int[] A4 = {};
    int x4 = -1;
    int numThread4 = 32;
    verifyParallelSearch(x4, A4, numThread3);
    
    int[] A5 = {50, 50, 50, 50, 50, 50, 50, 50, -1, -1};
    int x5 = 50;
    int numThread5 = 4;
    verifyParallelSearch(x5, A5, numThread3);
    
    int[] A6 = {-1};
    int x6 = -1;
    int numThread6 = 64;
    verifyParallelSearch(x6, A6, numThread3);
  }

  static void verifyParallelSearch(int x, int[] A, int numThread) {

    System.out.print("Verify Parallel Search for x: " + x + " in array: ");
    printArray(A);

    int idx = -1;
    for (int i = 0; i < A.length; i++) {
      if (A[i] == x) {
        idx = i;
        break;
      }
    }

    int pIdx = PSearch.parallelSearch(x, A, numThread);
    if(pIdx != -1){
    	System.out.println("Value:" + Integer.toString(A[pIdx]));
    }else{
    	System.out.println("Index is -1");
    }
    
    if (pIdx != idx) {
      System.out.println("Your parallel search algorithm is not correct");
      System.out.println("Expect: " + idx);
      System.out.println("Your results: " + pIdx);
    } else {
      System.out.println("Great, your search algorithm works for this test case");
    }
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
