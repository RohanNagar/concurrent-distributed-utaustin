// UT-EID: ran679, kmg2969

import java.util.*;
import java.util.concurrent.*;

public class PSort {

  public static void parallelSort(int[] A, int begin, int end) {
    ExecutorService es = Executors.newSingleThreadExecutor();

    QuickSort sort = new QuickSort(A, begin, end);
    Future future = es.submit(sort);
    while (!future.isDone());

    es.shutdown();
    sort.threadPool.shutdown();
  }
  private static class QuickSort implements Runnable {
	  public static ExecutorService threadPool = Executors.newCachedThreadPool();

	  private int begin;
	  private int end;
	  private static int[] A;
	  private int n;

	  QuickSort(int[] A, int begin, int end) {
		  this.begin = begin;
		  this.end = end;
		  this.A = A;
		  this.n = begin - end;
	  }

	  public void run() {
		  if(n <= 4) {
			  int [] subA = Arrays.copyOfRange(A, begin, end);
			  Arrays.sort(subA);
			  for(int i = begin; i < end; i++) {
				  A[i] = subA[i];
			  }
		  } else {
			  int pivot = A[begin];
			  int open = begin + 1;
			  boolean openGreat = false;
			  boolean hasRight = false;
			  boolean hasLeft = false;
			  int current;
			  for(int i = begin+1; i < end; i++){
				  current = A[i];
				  if(current < pivot){
					  A[i] = A[open];
					  A[open] = current;
					  if(!hasRight){
						  open++;
					  }
				  }else{
					 if(!hasRight){  //found the first value greater than or equal to pivot
						 openGreat = true;
						 open = i;
					 }
				  }
			  }
			  int pivotid;
			  if(hasRight){ // case where there is a left and right side
				  A[begin] = A[open-1];
				  A[open-1] = pivot;
				  pivotid = open -1;
				  
			  }else{ // case where there is only values less than the pivot
				  A[begin] = A[open];
				  A[open] = pivot;
				  pivotid = open;
			  }
			  // the array can be divided into 2 arrays by quick sort
			  // so it submits two
			  if(hasRight && hasLeft){
				  Future f1 = threadPool.submit(new QuickSort(A,begin,pivotid));
				  Future f2 = threadPool.submit(new QuickSort(A,pivotid+1,end));
				  if(f1.isDone() && f2.isDone()){
					  return;
				  }
			  }else if(hasRight){
				  Future f1 = threadPool.submit(new QuickSort(A,pivotid+1,end));
				  if(f1.isDone()){
					  return;
				  }
			  }else if(hasLeft){
				  Future f1 = threadPool.submit(new QuickSort(A,begin,pivotid));
				  if(f1.isDone()){
					  return;
				  }
			  }
		  }
		  
		  
	  }
  }
  
}
