
public class CPUData extends ThreadData {
	static final int STAGE_ZERO = 0;
	static final int STAGE_ONE = 1;
	static final int STAGE_TWO = 2;
	static final int STAGE_THREE = 3;
	volatile static int stage = STAGE_ZERO;
	
	private static int quantum = 0;
	
	private static boolean isRoundRobin = false;
	private static boolean isPriorityAlgorithm = false;
	private static int priorityAlgorithm = 1;
	
	private int pcbRoundRobinTime = 0;//reset to zero every time cpu runs a new pcb
	
	private int cpuName;
	private ProcessControlBlock pcb;
	
	//use to cal cpu utilization
	private int idleTime = 0;
	private int busyTime = 0;

	
	public CPUData (int cpuName) {//constructor
		this.cpuName = cpuName;
	}
	
	/* Dynamic methods */
	public ProcessControlBlock getPCB() {
		return pcb;	
	}
	public void setPCB(ProcessControlBlock pcb) {
		this.pcb = pcb;	
	}
	public int getIdleTime() {
		return idleTime;
	}
	public int getBusyTime() {
		return busyTime;
	}
	public void incrementIdleTime() {
		idleTime++;
	}
	public void incrementBusyTime() {
		busyTime++;
	}
	public int getCPUname() {
		return cpuName;
	}

	public void incrementPcbRoundRobinTime () {
		pcbRoundRobinTime++;
	}
	
	public void resetPcbRoundRobinTime () {
		pcbRoundRobinTime = 0;
	}
	
	public int getPcbRoundRobinTime () {
		return pcbRoundRobinTime;
	}
	
	public int getCPUutilization() {
		return (int)(((float)busyTime/((float)busyTime+(float)idleTime))*100.00);
	}
	
	
	/* Static methods */
	public static boolean executeRoundRobin() {
		return isRoundRobin;
	}
	public static void setRoundRobin(boolean _isRoundRobin) {
		isRoundRobin = _isRoundRobin;
	}
	public static boolean executePriorityAlgorithm() {
		return isPriorityAlgorithm;
	}
	public static void setPriorityAlgorithm(boolean _isPriorityAlgorithm) {
		isPriorityAlgorithm = _isPriorityAlgorithm;
	}
	public static int getQuantum() {
		return quantum;	
	}
	public static void setQuantum(int _quantum) {
		quantum = _quantum;	
	}
	public static void setPriorityAlgorithm(int _priorityAlgorithm) {
		/* Verify it is setting to one of the two Priority Algorithms */
		if (_priorityAlgorithm > 2 || _priorityAlgorithm < 1) _priorityAlgorithm = 1;
		priorityAlgorithm = _priorityAlgorithm;
	}
	public static int getPriorityAlgorithm() {
		return priorityAlgorithm;
	}
	public static int getStage() {
		return stage;
	}
	public static void setStage(int _stage) {
		stage = _stage;
	}
	
	
}
