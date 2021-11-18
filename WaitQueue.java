
/* Contain extra methods that we might not need*/
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;

public class WaitQueue {
	static Queue<ProcessControlBlock> waitQ;

	public static void initializeWaitQueue(Queue<ProcessControlBlock> q) {
		waitQ = q;
	}

	public static void clearQueue() {
		if (!isQueueEmpty())
			waitQ.clear();
	}

	public synchronized static boolean isQueueEmpty() {
		if (waitQ != null)
			return waitQ.isEmpty();
		return true;
	}

	public synchronized static ProcessControlBlock popWaitQ() {
		if (isQueueEmpty())
			return null;
		return waitQ.remove();
	}

	public synchronized static void pushWaitQ(ProcessControlBlock pcb) {

		if (waitQ != null && pcb != null) {
			pcb.setState(ProcessControlBlock.WAITING);// the process is in the I/O queue to wait for the I/O device
			waitQ.add(pcb);
		}
	}
	
	public synchronized static Queue<ProcessControlBlock> cloneWaitQueue() {
		
		return (Queue<ProcessControlBlock>) deepCopy(waitQ);
	}
	
	/**
	 * Makes a deep copy of any Java object that is passed.
	 * 
	 * Reference: https://www.journaldev.com/17129/java-deep-copy-object
	 */
	 private static Object deepCopy(Object object) {
	   try {
	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	     ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
	     outputStrm.writeObject(object);
	     ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	     ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
	     return objInputStream.readObject();
	   }
	   catch (Exception e) {
	     e.printStackTrace();
	     return null;
	   }
	 }
	 
	 public static void incrementWaitTime() {
			Object[] pcbs= waitQ.toArray();
			for (int i = 0; i<pcbs.length; i++) { 
				((ProcessControlBlock) pcbs[i]).incrementIOWaitTime();
			}
			
			
		}

}
