
public class ThreadData {

	static final int STAGE_WAIT_SLEEP_TIME = 10;

	volatile static boolean terminate = false;
	volatile boolean processing = false;

	public static boolean shouldTerminate() {
		return terminate;
	}
	public static void terminateThreads() {
		terminate = true;
	}
	public static void setTerminateThreads(boolean _terminate) {
		terminate = _terminate;
	}
	public boolean isProcessing() {
		return processing;	
	}

	public void setProcessing(boolean processing) {
		this.processing = processing;
	}
}
