//UT-EID=KMG2969_


import java.util.*;
import java.util.concurrent.*;

public class PSort{
	
  public static void parallelSort(int[] A, int begin, int end){
	 ExecutorService es = Executors.newSingleThreadExecutor();
	 QuickSort sort = new QuickSort(A, begin, end);
	 Future future = es.submit(sort);
	 while(!future.isDone());
  }
  private static class QuickSort implements Runnable{
	  public static ExecutorService threadPool = Executors.newCachedThreadPool();
	  int begin;
	  int end;
	  static int[] A;
	  int n;
	  QuickSort(int[] A, int begin, int end){
		  this.begin = begin;
		  this.end = end;
		  this.A = A;
		  this.n = begin - end;
	  }
	  public void run(){
		  if(n <= 4){
			  int [] subA = Arrays.copyOfRange(A, begin, end);
			  Arrays.sort(subA);
			  for(int i = begin; i < end; i++){
				  A[i] = subA[i];
			  }
		  }else{
			  int pivot = A[begin];
			  int open = begin + 1;
			  boolean hasRight = false;
			  boolean hasLeft = false;
			  int current;
			  for(int i = begin+1; i < end; i++){
				  current = A[i];
				  if(current < pivot){
					  if(!hasLeft){ // makes sure quick sort configures for the case where there are 
						  hasLeft = true; // values to the left of(less than) the pivot.
					  }
					  A[i] = A[open];
					  A[open] = current;
					  open++;
				  }else{
					 if(!hasRight){  //found the first value greater than or equal to pivot
						 hasRight = true;
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
				  A[begin] = A[open-1];
				  A[open-1] = pivot;
				  pivotid = open -1;
				  Future f1 = threadPool.submit(new QuickSort(A,begin,pivotid));
				  Future f2 = threadPool.submit(new QuickSort(A,pivotid+1,end));
				  
				  try {
					  f1.get();
					  f2.get();
				  } catch (Exception e) {
					  System.out.println("Exception");
				  }
				  
				  return;
			  }else if(hasRight){
				  Future f1 = threadPool.submit(new QuickSort(A,pivotid+1,end));
				  
				  try {
					  f1.get();
				  } catch (Exception e) {
					  System.out.println("Exception");
				  }
				  
				  return;
			  }else if(hasLeft){
				  Future f1 = threadPool.submit(new QuickSort(A,begin,pivotid));

				  try {
					  f1.get();
				  } catch (Exception e) {
					  System.out.println("Exception");
				  }
				  
				  return;
			  }
		  }
		  
		  
	  }
  }
  
}
