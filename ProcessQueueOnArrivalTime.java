
/* this is a class that contains a queue of processes based on their arrival time.
 * it is to solve the problem that processes in the file might not be listed in the order based on arrival time.
 * it reads the whole file to determine which process arrives first and add it to the queue.
 * if the system time counter from Driver class hits a specific number that matches our process arrival time, 
 * such process(es)(with the same arrival time) will be popped by calling popQueue()
 * */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class ProcessQueueOnArrivalTime {

	// create a priority queue based on process's arrival time
	static Queue<ProcessControlBlock> initialQueue = new PriorityQueue<>(new ArrivalTimeComparator());

	/* read the file line by line to extract process info to create a PCB object */
	public static LinkedList<ProcessControlBlock> extractProcesses(File fileName) throws FileNotFoundException {
		LinkedList<ProcessControlBlock> pcbList = new LinkedList<ProcessControlBlock>();
		Scanner myReader = null;
		try {
			myReader = new Scanner(fileName);

			while (myReader.hasNextLine()) {
				String ProcessPerLine = myReader.nextLine();
				ProcessControlBlock pcb = parseProcessComponents(ProcessPerLine);
				pcbList.add(pcb);
				initialQueue.add(pcb);
			}
		} catch (Exception e) {
			throw new FileNotFoundException("Invalid file content: "+ fileName.getAbsolutePath());
		} finally {
			if (myReader != null)
				myReader.close();
		}
		return pcbList;
	}


	public static ProcessControlBlock parseProcessComponents(String line) throws Exception {
		ArrayList<Integer> cpuBurstsList = new ArrayList<Integer>();
		ArrayList<Integer> ioBurstsList = new ArrayList<Integer>();
		int sumCPUbursts = 0;
		int sumIObursts = 0;

		// split each line by space and strip extra spaces
		String[] tokens = line.trim().split("\\s* \\s*");

		// has to end with a burst making the total tokens even
		// it has at least a process name, arrival time, priority, and one burst
		if(tokens.length % 2 == 1 || tokens.length < 4) throw new Exception("Bad File");

		String processName = tokens[0];
		int arrivalTime = Integer.parseInt(tokens[1]);
		int priority = Integer.parseInt(tokens[2]);

		for (int i = 3; i < tokens.length; i++) {
			int temp = Integer.parseInt(tokens[i]);

			if (i % 2 == 0) {// add all ios to the list and sum them
				ioBurstsList.add(temp);
				sumIObursts += temp;
			} else {// add all cpuBursts to the list and sum them
				cpuBurstsList.add(temp);
				sumCPUbursts += temp;
			}

		}
		// create a PCB object
		return new ProcessControlBlock(processName, arrivalTime, priority, cpuBurstsList, ioBurstsList,
				sumCPUbursts, sumIObursts);

	}

	public static void clearQueue() {
		initialQueue.clear();
	}

	public static boolean isQueueEmpty() {

		return initialQueue.isEmpty();
	}

	public static ProcessControlBlock popQueue() {
		if (isQueueEmpty())
			return null;
		return initialQueue.remove();
	}

	public static boolean queueHasJobOnArrivalTime(int arrivalTime) {

		if (isQueueEmpty())
			return false;
		return (initialQueue.peek().getArrival() == arrivalTime);

	}
	

}
