

public class IOThread implements Runnable {
	private IOData io;


	public IOThread (IOData io) {//constructor
		this.io = io;
	}

	@Override
	public void run() {

		while (true) {

			if(!waitForStage(IOData.STAGE_ONE)) break;
			/* ***************** stage 1 **********************************/
			
			if (io.getPCB() != null && io.getPCB().getState() == ProcessControlBlock.READY) {
				// process status has turned ready, push it to ready queue
				ReadyQueue.pushReadyQ(io.getPCB());
				Driver.log("# "+io.getPCB().getProcessName() + " completes I/O and return to ready queue");
				io.setPCB(null);// clear pcb pointer for next system time
			}
			
			// once work is done, turn processingFlag to false
			io.setProcessing(false);
			/* ***************** end stage 1 ******************************/
			
			
			if(!waitForStage(IOData.STAGE_TWO)) break;
			/* ***************** stage 2 **********************************/
			// attempt to get a job from wait queue
			if (io.getPCB() == null) {// idling io, get a job from wait queue
				io.setPCB(WaitQueue.popWaitQ());
				if(io.getPCB() != null) {
					Driver.log("* "+io.getPCB().getProcessName() + " dispatched from the wait queue to use the I/O");
				}
			}
		
			// once work is done, turn processingFlag to false
			io.setProcessing(false);
			/* ***************** end stage 2 ******************************/

			
			if(!waitForStage(IOData.STAGE_THREE)) break;
			/* ***************** stage 3 **********************************/

			if(io.getPCB() != null)
				io.getPCB().decrementIObursts();// call this method to decrement current ioBurst & remaining total ioBurst

			// once work is done, turn processingFlag to false
			io.setProcessing(false);
			/* ***************** end stage 3 ******************************/
		}
	}

	/** waitForStage
	  * Loops forever until one of two flags are set: ThreadData.terminate or ThreadData.stage= input parm stage
	  * 
	  * returns boolean
	  * True -> Start the stage
	  * False -> Terminate the thread
	  * 
	  */
	private boolean waitForStage(int stage) {
		// Checking if the stage matches. 
		// Yes--> return. No--> sleep and repeat.
		
		while (IOData.getStage() != stage) {
			if(ThreadData.shouldTerminate()) {
				return false;
			}
			
			try {
				Thread.sleep(ThreadData.STAGE_WAIT_SLEEP_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return true;
	}

}
