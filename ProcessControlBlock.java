
import java.io.Serializable;
import java.util.ArrayList;

public class ProcessControlBlock implements Serializable{

	/* ********** Global Static variables ********************** */
	public static final String TERMINATED = "TERMINATED";
	public static final String READY = "READY";
	public static final String RUNNING = "RUNNING";
	public static final String WAITING = "WAITING";
	public static final String NEW = "NEW";
	public static final String TERMINATING = "TERMINATING";
	public static final String[] PCB_INFO_ARRAY = { "PID", "Process Name", "Arrival","Priority",
			"CPUBurst","IOBurst","Start","Finish",
			"Status","RQWaitT","IOWaitTime" }; 
	public static final String[] BURSTS_INFO_COLUMN_NAME = {"Process Name", "Arrival","Priority","CPUBurst","IOBurst"};



	private static int pidNext = 1; // incremented pid counter
	private static int finishedPCBCount = 0;

	/* ********** Dynamic variables ********************** */
	private String processName;
	private final int pid;// an unique process id
	private int arrivalTime;// the system time that the process is created
	private int priority;
	private String state;

	private ArrayList<Integer> cpuBurstsList;// all the cpubursts values will be stored to an arraylist
	private ArrayList<Integer> ioBurstsList;// all the ioBursts values will be stored to an arraylist.

	private final ArrayList<Integer> initialCPUBurstsList;
	private final ArrayList<Integer> initialIOBurstsList;

	private int remainingTotalCPUbursts;// use it to figure out the shortest remaining running time
	private int remainingTotalIObursts;

	private int startTime = -1;// the time that the process is run in cpu
	private int finishTime = -1;
	private int readyQueueWaitTime = 0; // the total time the process needs to wait in the ready queue
	private int ioWaitTime = 0;// the total time the process needs to wait in the I/O queue


	//CONSTRUCTOR
	public ProcessControlBlock(String processName, int arrival, int priority, ArrayList<Integer> cpuBurstsList,
			ArrayList<Integer> ioBurstsList, int sumCPUbursts, int sumIObursts) {

		pid = pidNext++;
		this.processName = processName;
		this.arrivalTime = arrival;
		this.priority = priority;
		this.cpuBurstsList = cpuBurstsList;
		this.ioBurstsList = ioBurstsList;
		this.remainingTotalCPUbursts = sumCPUbursts;
		this.remainingTotalIObursts = sumIObursts;
		this.state = NEW;// when a process is created, its status is "NEW".

		initialCPUBurstsList = new ArrayList<Integer>();
		for(int i = 0; i < cpuBurstsList.size(); i++) {
			initialCPUBurstsList.add((int)cpuBurstsList.get(i));
		}
		initialIOBurstsList = new ArrayList<Integer>();
		for(int i = 0; i < ioBurstsList.size(); i++) {
			initialIOBurstsList.add((int)ioBurstsList.get(i));
		}
	}

	public void decrementCPUbursts() {
		// it decrements RemainingTotalCPUbursts and current cpuBurst
		if (remainingTotalCPUbursts > 0) {
			// decrement the current cupBursts and remove it from the list once it turns 0
			// before getting sent this process to io
			int currentCPUburst = cpuBurstsList.get(0);
			if (currentCPUburst > 0) {
				cpuBurstsList.set(0, --currentCPUburst);
				remainingTotalCPUbursts--;
			}

			if (currentCPUburst == 0) {// current cpuBurst == 0
				cpuBurstsList.remove(0);// remove this cpuBurst from the list if it is finished

				if (remainingTotalCPUbursts > 0) {// not the last cpuBurst, waiting for io
					setState(WAITING);
				} else {
					setState(TERMINATING); // last cpuBurst is finished, job terminated.

				}
			}
		} else {
			setState(TERMINATING);
			System.out.println(getProcessName() + " should not be here");
		}
	}

	// IO_Device Class will call this method to decrement every system time unit.
	public void decrementIObursts() {
		if (remainingTotalIObursts > 0) {
			// decrement the current ioBursts and remove it from the list once it turns 0
			// before getting sent this process to cpu
			int currentIOburst = ioBurstsList.get(0);// look into the 1st item in the ioList cuz an io is removed once
			// it turns 0

			if (currentIOburst > 0) {// if currentIO > 0, decrement
				ioBurstsList.set(0, --currentIOburst);
				remainingTotalIObursts--;
			}

			// if currentIO is zero, remove it from list
			if (currentIOburst == 0) {// current ioBurst == 0
				ioBurstsList.remove(0);// remove this ioBurst from the list once it is finished
				setState(READY);// io finished, add to ready queue, status is now "ready"
			}
		}
	}



	// getters
	public String getProcessName() {
		return processName;
	}

	public int getPid() {
		return pid;
	}

	public int getArrival() {
		return arrivalTime;
	}

	public int getPriority() {
		return priority;
	}

	public ArrayList<Integer> getCpuBurstsList() {
		return cpuBurstsList;
	}

	public ArrayList<Integer> getIoBurstsList() {
		return ioBurstsList;
	}

	public String getState() {
		return state;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getFinishTime() {
		return finishTime;
	}

	public int getRemainingTotalCPUbursts() {
		return remainingTotalCPUbursts;
	}

	public int getRemainingTotalIObursts() {
		return remainingTotalIObursts;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setFinishTime(int finishTime) {
		this.finishTime = finishTime;
	}


	public int getTurnaroundTime() {// determines turnaround time
		return finishTime - arrivalTime;
	}
	
	public int getResponseTime() {
		return startTime - arrivalTime;
	}



	public int getReadyQueueWaitTime() {
		return readyQueueWaitTime;
	}
	public int getIOWaitTime() {
		return ioWaitTime;
	}

	public void incrementReadyQueueWaitTime() {
		readyQueueWaitTime++;
	}
	public void incrementIOWaitTime() {
		ioWaitTime++;
	}

	@Override
	public String toString() {
		return (String.format("%-3d %-10s %-10d %-10d %-30s %-30s %-10s %-10s %-10s %-10d %-10d", 
				getPid(),
				getProcessName(), 
				getArrival(),
				getPriority(),
				initialCPUBurstsList.toString(),
				initialIOBurstsList.toString(),
				getStartTime()==-1?"n/a":String.valueOf(getStartTime()),
						getFinishTime()==-1?"n/a":String.valueOf(getFinishTime()),
								getState(),
								getReadyQueueWaitTime(), getIOWaitTime()));
	}

	public String[] pcbInfoToArray() {
		/*{ "PID", "Process Name", "Arrival","Priority",
		    "CPUBurst","IOBurst","Start","Finish",
		    "Status","RQWaitT","IOWaitTime" }; */

		return new String[] {
				Integer.toString(getPid()),
				getProcessName(),
				Integer.toString(getArrival()),
				Integer.toString(getPriority()),
				initialCPUBurstsList.toString(),
				initialIOBurstsList.toString(),
				getStartTime()==-1?"n/a":String.valueOf(getStartTime()),
						getFinishTime()==-1?"n/a":String.valueOf(getFinishTime()),
								getState(),
								Integer.toString(getReadyQueueWaitTime()), 
								Integer.toString(getIOWaitTime())
		};
	}

	public String[] burstsInfoToArray() {
		/*{"Process Name", "Arrival","Priority","CPUBurst","IOBurst"}*/
		return new String[] {
				getProcessName(),
				Integer.toString(getArrival()),
				Integer.toString(getPriority()),
				extractCPUBurstsList(),
				extractIOBurstsList(),
		};
	}


	public String extractCPUBurstsList() {
		StringBuilder list = new StringBuilder("");
		int sizeOfOriginal = initialCPUBurstsList.size();
		int sizeOfCurrent = cpuBurstsList.size();
		int difference = sizeOfOriginal-sizeOfCurrent;

		for(int i= 0; i < difference; i++) {
			list.append("0/"+initialCPUBurstsList.get(i) + "  ");
		}

		for (int i = difference, j=0; i<initialCPUBurstsList.size(); i++,j++) {
			list.append(this.cpuBurstsList.get(j)+"/"+initialCPUBurstsList.get(i) + "  ");
		}
		return list.toString();
	}

	public String extractIOBurstsList() {
		StringBuilder list = new StringBuilder("");
		int sizeOfOriginal = initialIOBurstsList.size();
		int sizeOfCurrent = ioBurstsList.size();
		int difference = sizeOfOriginal-sizeOfCurrent;

		for(int i= 0; i < difference; i++) {
			list.append("0/"+initialIOBurstsList.get(i) + "  ");
		}

		for (int i = difference, j=0; i<initialIOBurstsList.size(); i++,j++) {
			list.append(this.ioBurstsList.get(j)+"/"+initialIOBurstsList.get(i) + "  ");
		}
		return list.toString();
	}


	/* static methods */
	public static int getFinishedPCBCount() {
		return finishedPCBCount;
	}
	public static void resetFinishedPCBCount() {
		finishedPCBCount = 0;
	}

	public synchronized static void incrementFinishedPCBCount() {
		finishedPCBCount++;
	}
	
	public static void resetPIDCounter() {
		pidNext = 1;
	}

}
