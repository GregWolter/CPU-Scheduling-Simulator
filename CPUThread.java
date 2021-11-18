

public class CPUThread implements Runnable {
	private CPUData cpu;


	public CPUThread (CPUData cpu) {//constructor
		this.cpu = cpu;
	}

	@Override
	public void run() {

		while (true) {

			if(!waitForStage(CPUData.STAGE_ONE)) break;
			/* ***************** stage 1 **********************************/
			//System.out.println("cpu: "+cpu.getCPUname()+" did stage 1.");
			
			/* Did the PCB finish? */
			if(cpu.getPCB() != null) {
				if (cpu.getPCB().getState().equals(ProcessControlBlock.WAITING)) {//this process has changed its state to waiting, push it to wait queue
					
					WaitQueue.pushWaitQ(cpu.getPCB());
					Driver.log("$ "+cpu.getPCB().getProcessName() + " interrupted or put into an I/O queue");

					cpu.setPCB(null);//clear pcb pointer
				} else if (cpu.getPCB().getState().equals(ProcessControlBlock.TERMINATING)) {
					/* Do we end at the end of the systemTimer or the beginning of the next? */
					cpu.getPCB().setState(ProcessControlBlock.TERMINATED);
					cpu.getPCB().setFinishTime(Driver.getSystemTime());
					//cpu.getPCB().setFinishTime(Driver.getSystemTime() - 1);
					Driver.log("- "+cpu.getPCB().getProcessName() + "  was terminated. \nTurnaround time: " + cpu.getPCB().getTurnaroundTime() +
							", CPU WaitTime: " + cpu.getPCB().getReadyQueueWaitTime() +
							", I/O WaitTime: " + cpu.getPCB().getIOWaitTime()+", Response time: "+cpu.getPCB().getResponseTime());
					ProcessControlBlock.incrementFinishedPCBCount();//increment finished job count
					cpu.setPCB(null);//clear pcb pointer
				}
			}
			
			
			if(cpu.getPCB() != null) {
				/* Check if job done before RR*/
				if (CPUData.executeRoundRobin()) {//RR
					if (cpu.getPcbRoundRobinTime() >= CPUData.getQuantum()) {//time is up.
						//System.out.println("time is up!");
						
						ReadyQueue.pushReadyQ(cpu.getPCB());
						Driver.log("! "+cpu.getPCB().getProcessName() + " preempted(RR) and put back to ready queue");
						
						cpu.setPCB(null);
					} 
				}
				/* Do we need to preempt for priority? */
				else if(CPUData.executePriorityAlgorithm()) {
					ProcessControlBlock PCBAfterPreempt = ReadyQueue.preemption(cpu.getPCB());
					
					if(cpu.getPCB() != PCBAfterPreempt) {
						Driver.log("! "+cpu.getPCB().getProcessName() + " preempted(PS/SJF) and put back to ready queue");
						cpu.setPCB(PCBAfterPreempt);//if preempted, return higher-priority pcb to run
						Driver.log("^ "+cpu.getPCB().getProcessName() + " dispatched from the ready queue to use the CPU");
					}
				}
			}
			
			// once work is done, turn processingFlag to false
			cpu.setProcessing(false);
			/* ***************** end stage 1 ******************************/
			

			if(!waitForStage(CPUData.STAGE_TWO)) break;
			/* ***************** stage 2 **********************************/
			//System.out.println("cpu: "+cpu.getCPUname()+" did stage 2.");
			
			if(cpu.getPCB() == null) {
				cpu.setPCB(ReadyQueue.popReadyQ());
				if(cpu.getPCB() != null) {
					cpu.resetPcbRoundRobinTime();
					Driver.log("^ "+cpu.getPCB().getProcessName() + " dispatched from the ready queue to use the CPU");
				}
			}
			
			// once work is done, turn processingFlag to false
			cpu.setProcessing(false);
			/* ***************** end stage 2 ******************************/
			
			

			if(!waitForStage(CPUData.STAGE_THREE)) break;
			/* ***************** stage 3 **********************************/
			//System.out.println("cpu: "+cpu.getCPUname()+" did stage 3.");
			
			/* If we have PCB, do work. Set StartTime if PCB hasn't */
			if(cpu.getPCB() != null) {
				if (cpu.getPCB().getStartTime() == -1) {
					cpu.getPCB().setStartTime(Driver.getSystemTime());//set it when it first start running
				}
				//call this method to decrement current cpuBurst & remaining total cpuBurst, change status if needed
				cpu.getPCB().decrementCPUbursts();
				cpu.incrementBusyTime();
				cpu.incrementPcbRoundRobinTime();
			}else {
				cpu.incrementIdleTime();
			}
			
			// once work is done, turn processingFlag to false
			cpu.setProcessing(false);
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
		
		while (CPUData.getStage() != stage) {
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
