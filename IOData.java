
public class IOData extends ThreadData {
	static final int STAGE_ZERO = 0;
	static final int STAGE_ONE = 1;
	static final int STAGE_TWO = 2;
	static final int STAGE_THREE = 3;
	volatile static int stage = STAGE_ZERO;
	
	private int ioName;
	private ProcessControlBlock pcb;
	
	private int idleTime = 0;
	private int busyTime = 0;//io is busy
	

	public IOData (int ioName) {//constructor
		this.ioName = ioName;
	}
	
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
	
	public int getIOname() {
		return ioName;
	}
	public static int getStage() {
		return stage;
	}
	public static void setStage(int _stage) {
		stage = _stage;
	}
}
