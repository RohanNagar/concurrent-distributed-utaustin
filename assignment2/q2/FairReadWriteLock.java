
public class FairReadWriteLock {
	
	boolean inWrite = false;
    int readCount = 0;
    int writeCount = 0;
    
	public synchronized void beginRead() {
		while(inWrite || writeCount != 0){
			try{
				wait();
			} catch(InterruptedException e){}
		}
		readCount++;
		
	}
	
	public synchronized void endRead() {
		readCount--;
		notifyAll();
	}
	
	public synchronized void beginWrite() {
		writeCount++;
		while(readCount != 0 || inWrite){
			try{
				wait();
			} catch(InterruptedException e){}
		}
		writeCount--;
		inWrite = true;
	}
	public synchronized void endWrite() {
		inWrite = false; 
		notifyAll();
	}
	
	
}
