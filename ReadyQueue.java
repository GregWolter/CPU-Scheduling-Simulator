
// The Class level lock prevents multiple threads to enter in a synchronized block in any of all available instances on runtime.
// make instance level data thread safe.

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class ReadyQueue implements Serializable{
	static Queue<ProcessControlBlock> readyQ;

	public static void initializeReadyQueue(Queue<ProcessControlBlock> q) {
		readyQ = q;
	}

	public static void clearQueue() {
		if (readyQ != null && !readyQ.isEmpty())
			readyQ.clear();
	}

	public synchronized static ProcessControlBlock popReadyQ() {

		if (readyQ == null || readyQ.isEmpty())
			return null;

		ProcessControlBlock pcb = readyQ.remove();
		pcb.setState(ProcessControlBlock.RUNNING);
		return pcb;
	}

	public synchronized static void pushReadyQ(ProcessControlBlock pcb) {// synchronized to make sure only one thread
		// can access this method at a time
		if (readyQ != null && pcb != null) {
			pcb.setState(ProcessControlBlock.READY);// the process is ready to be executed by the CPU but is currently in the ready
			// queue
			readyQ.add(pcb);
		}
	}

	// check if the current process running in the CPU needs to get preempted for PS
	// and SJF
	public synchronized static ProcessControlBlock preemption(ProcessControlBlock _pcb) {

		if (readyQ != null && !readyQ.isEmpty()) {

			ProcessControlBlock pcb = readyQ.peek();// look and get the top element of the queue

			if(CPUData.executePriorityAlgorithm()) {
				if (CPUData.getPriorityAlgorithm() == 1) {// PS
					if (pcb.getPriority() < _pcb.getPriority()) {
						pushReadyQ(_pcb);// place it to the tail of ready queue
						return popReadyQ();// preemptive, pop it from the ready queue to replace the one running in the cpu
					}
				} else if (CPUData.getPriorityAlgorithm() == 2) {// SJF
					if (pcb.getRemainingTotalCPUbursts() < _pcb.getRemainingTotalCPUbursts()) {
						//Driver.output.append("\n" + _pcb.getProcessName() + " was preempted");
						pushReadyQ(_pcb);
						return popReadyQ();
					}
				}
			}
		}
		return _pcb;// if not preempted, just return the current pcb that we pass in
	}


	public synchronized static Queue<ProcessControlBlock> cloneReadyQueue() {	
		return (Queue<ProcessControlBlock>) deepCopy(readyQ);
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
		Object[] pcbs= readyQ.toArray();
		for (int i = 0; i<pcbs.length; i++) { 
			((ProcessControlBlock) pcbs[i]).incrementReadyQueueWaitTime();
		}


	}

}