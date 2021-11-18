import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class Driver extends JFrame implements ActionListener {

	/* Static variables */
	static Driver gui;
	static boolean simulationFlag = false;
	static boolean simulateNextStep = false;
	static CPUData[] cpus;
	static IOData[] ios;
	static Thread[] threads;
	static int systemTime = 0;

	/* GUI variables */
	private JFrame frame;
	private JLabel algorithm, saveFile, cPULabel, readyQLabel, iODeviceLabel, iOQLabel, cPUCountLabel, timeQuantumLabel,
			systemTimeLabel, throughputLabel, avgTurnLabel, avgWaitLabel, iOCountLabel, speedLabel;
	private JTextField algorithmTF, saveFileTF, readyQTA, iOQTA, timeQuantumTF;
	private static JTextField fileNameTF;
	public JTextArea output;
	public JTextArea cPUTF;
	public JTextArea iODeviceTF;
	public JTextArea cPURemainingBursts;
	public JComboBox algorithmBox;
	public JProgressBar currentStageProgessBar;
	public JComboBox<Integer> cPUCountBox, iOCountBox;
	public JComboBox<String> frameRateCountBox;
	private final JFileChooser fileChooser = new JFileChooser();
	private JButton saveFileButton, startButton, loadFileButton, manualButton, killSwitchButton;
	private JScrollPane scrollPane, scrollPane1, scrollPane2;
	private JTable pcbInfoTable, burstsTable;

	/* Simulation variables */
	static Scanner keyboard = new Scanner(System.in);
	public static int systemTimeCounter = -1;
	public static final String[] algorithms = { "Priority Scheduling", "Shortest Job First", "First Come First Serve",
			"Round Robin" };

	public static final Integer[] cPUAndIOCountMenu = { 1, 2, 4, 6, 8 };
	public static final Integer[] frameRateValues = { 500, 250, 100 }; // , 20, 0};
	public static final String[] frameRateMenu = { "Slow", "Medium", "Fast" }; // , "Rocket*", "Natural*" };
	/*
	 * **WARNING!!** (*) Means these rates cause exceptions to occur in the GUI as
	 * it is updating too fast. Program still run normally but some GUI updates are
	 * missed as a result. Data is not affected.
	 */
	private static final int WIDTH = 1240; // width of GUI
	private static final int HEIGHT = 1000; // height of GUI
	boolean manualFlag = true;

	public static LinkedList<ProcessControlBlock> pcbList;
	public static StringBuilder outputBuilder = new StringBuilder();
	private static boolean killSwitch = false;

	public void loadFile() {
		File openDirectory = new File(System.getProperty("user.dir"));
		fileChooser.setCurrentDirectory(openDirectory);
		int value = fileChooser.showOpenDialog(Driver.this);
		if (value == JFileChooser.APPROVE_OPTION) {
			File fileToTest = fileChooser.getSelectedFile();
			fileNameTF.setText(fileToTest.getName());

			startButton.setEnabled(true);
			manualButton.setEnabled(true);
		}
	}

	public void saveFile() {
		File guiOutputFile = new File(saveFileTF.getText());
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(guiOutputFile));

			writer.append(outputBuilder.toString());
			writer.close();
			log("><    Output saved to \"" + guiOutputFile.getAbsolutePath() + "\"");
		} catch (IOException e) {
			log("><" + "unable to save file with that directory.");
		}

	}

	public void enableKillSwitch(boolean enable) {
		gui.killSwitchButton.setEnabled(enable);
	}

	public void EndPause() {
		simulationFlag = false;
		startButton.setText("Start");
	}

	public void StartPause() {
		simulationFlag = !simulationFlag;

		if (simulationFlag) {
			startButton.setText("Pause");

		} else {
			startButton.setText("Resume");
		}

	}

	public void manual() {
		/* Use this method as next to advance to the next step */
		simulateNextStep = true;

		startButton.setText("Resume");
		simulationFlag = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		/* Check if a button called */
		if (e.getSource() instanceof JButton) {
			JButton clickedButton = (JButton) e.getSource();

			/* Load a file */
			if (clickedButton == loadFileButton) {
				loadFile();
			}

			if (clickedButton == manualButton) {
				manual();
			}

			if (clickedButton == saveFileButton) {
				saveFile();
			}

			if (clickedButton == startButton) {
				StartPause();
			}

			if (clickedButton == killSwitchButton) {
				killSwitch = true;
			}

		}

	}

	public static void main(String[] args) throws InterruptedException, IOException {

		/* Create GUI */
		gui = new Driver();

		while (true) {
			/*
			 * Wait for settings to be done by the user before createSimulation() is called.
			 */
			try {
				waitForNextStep();
			} catch (Exception e) {
				/* Kill switch will be disabled, will never get here */
			}

			/* set GUI to default inputs */
			resetGUI();
			// System.out.println("Creating simulation");
			try {
				createSimulation();
			} catch (FileNotFoundException e) {
				handleInvalidFileException(e.getMessage());
				continue;
			}

			/* This should be the program here */
			/* Probably need to loop it if they want to load a new file */
			// System.out.println("Starting simulation");
			simulation();

			endSimulation();

			// System.out.println("Simulation has ended");
			log("Simulation has ended");

		}
		// System.out.println("Program ended");

	}// end main

	private static void simulation() {

		gui.enableKillSwitch(true);
		try {
			while (continueSimulation()) {

				updateGUI();
				waitForNextStep();

				// stage 1: set cpu and io data
				while (ProcessQueueOnArrivalTime.queueHasJobOnArrivalTime(systemTime)) {// check if there is a job
																						// arrives at the current system
																						// time

					ProcessControlBlock pcb = ProcessQueueOnArrivalTime.popQueue();
					log("+ " + pcb.getProcessName() + " was created");
					ReadyQueue.pushReadyQ(pcb);// add PCBs to ready queue

					/* Update log with new job */

				} // end while
				setProcessingTrue(cpus);
				setProcessingTrue(ios);
				IOData.setStage(IOData.STAGE_ONE);
				waitForProcessing(ios);
				CPUData.setStage(CPUData.STAGE_ONE);
				waitForProcessing(cpus);

				updateGUI();
				waitForNextStep();

				// stage 2: set cpu and io data
				setProcessingTrue(cpus);
				setProcessingTrue(ios);
				CPUData.setStage(CPUData.STAGE_TWO);
				IOData.setStage(IOData.STAGE_TWO);
				waitForProcessing(cpus);
				waitForProcessing(ios);

				updateGUI();
				waitForNextStep();

				// stage 3: set cpu and io data
				setProcessingTrue(cpus);
				setProcessingTrue(ios);
				CPUData.setStage(CPUData.STAGE_THREE);
				IOData.setStage(IOData.STAGE_THREE);
				waitForProcessing(cpus);
				waitForProcessing(ios);

				/* Increment queue PCBs waitTimes */
				ReadyQueue.incrementWaitTime();
				WaitQueue.incrementWaitTime();

				/*
				 * Show progress bar for stages complete before moving on to the next System
				 * Time
				 */
				fillProgressBarShortWait();

				/* Increment the system time */
				systemTime++;

			}

		} catch (Exception e) {
			/* Exception is thrown if kill switch is set */
			/* Just end simulation and possibly start a new one */
		}
		gui.enableKillSwitch(false);
		killSwitch = false;

	}

	public synchronized static void log(String message) {
		/* Add to front of stringbuilder */
		outputBuilder.insert(0, "(" + systemTime + ")  " + message + "\n");

		/* Update GUI */
		gui.output.setText(outputBuilder.toString());

	}

	private static void updateGUI() {
		Queue<ProcessControlBlock> clonequeue;
		StringBuilder sb = new StringBuilder();
		String temp = "";

		/* Update System Timer */
		gui.systemTimeLabel.setText("System Time: " + systemTime);

		/* Update Ready Queue */
		sb.setLength(0);
		clonequeue = ReadyQueue.cloneReadyQueue();
		if (clonequeue != null && !clonequeue.isEmpty()) {
			while (!clonequeue.isEmpty()) {
				ProcessControlBlock pcb = clonequeue.remove();
				sb.append(pcb.getProcessName() + ", ");
			}

			gui.readyQTA.setText(sb.toString());
		} else {
			gui.readyQTA.setText("Queue is empty");
		}

		/* Update Wait Queue */
		sb.setLength(0);
		clonequeue = WaitQueue.cloneWaitQueue();
		if (clonequeue != null && !clonequeue.isEmpty()) {
			while (!clonequeue.isEmpty()) {
				ProcessControlBlock pcb = clonequeue.remove();
				sb.append(pcb.getProcessName() + ", ");
			}

			gui.iOQTA.setText(sb.toString());
		} else {
			gui.iOQTA.setText("Queue is empty");
		}

		/* Update CPU */
		sb.setLength(0);
		sb.append(String.format("%-5s %-15s %-15s %n", "CPU#", "Process", "Utilization"));
		for (int i = 0; i < cpus.length; i++) {
			CPUData cpuData = cpus[i];
			ProcessControlBlock pcb = cpuData.getPCB();

			if (pcb == null)
				temp = "idle";
			else
				temp = pcb.getProcessName();

			sb.append(String.format("CPU%-2d %-15s %-15s %n", cpuData.getCPUname(), temp,
					cpuData.getCPUutilization() + "%"));

		}
		gui.cPUTF.setText(sb.toString());

		/* Update I/O */
		sb.setLength(0);
		sb.append(String.format("%-6s %-15s %n", "I/O#", "Process"));
		for (int i = 0; i < ios.length; i++) {
			IOData ioData = ios[i];
			ProcessControlBlock pcb = ioData.getPCB();

			if (pcb == null)
				temp = "idle";
			else
				temp = pcb.getProcessName();

			sb.append(String.format("IO%-2d %-15s%n", ioData.getIOname(), temp));

		}
		gui.iODeviceTF.setText(sb.toString());

		/* Update Process Info */
		DefaultTableModel pcbInfoTableModel = new DefaultTableModel(0, 0);
		pcbInfoTableModel.setColumnIdentifiers(ProcessControlBlock.PCB_INFO_ARRAY);
		gui.pcbInfoTable.setModel(pcbInfoTableModel);

		for (int i = 0; i < pcbList.size(); i++) {
			pcbInfoTableModel.addRow(pcbList.get(i).pcbInfoToArray());
		}

		/* Update burstsTable */
		DefaultTableModel burstsTableModel = new DefaultTableModel(0, 0);
		burstsTableModel.setColumnIdentifiers(ProcessControlBlock.BURSTS_INFO_COLUMN_NAME);
		gui.burstsTable.setModel(burstsTableModel);

		for (int i = 0; i < pcbList.size(); i++) {
			burstsTableModel.addRow(pcbList.get(i).burstsInfoToArray());
		}

		/* Update progress bar */
		if (CPUData.getStage() == CPUData.STAGE_THREE)
			gui.currentStageProgessBar.setValue(0);
		else if (CPUData.getStage() == CPUData.STAGE_ONE)
			gui.currentStageProgessBar.setValue(33);
		else if (CPUData.getStage() == CPUData.STAGE_TWO)
			gui.currentStageProgessBar.setValue(66);
		else
			gui.currentStageProgessBar.setValue(0);

		/* Update Throughput, average turnaround time, average wait time */
		// Throughput
		if (systemTime != 0)
			gui.throughputLabel.setText(String.format("Throughput: %.2f",
					(float) ProcessControlBlock.getFinishedPCBCount() / (float) systemTime));
		// average turnaround time
		int sumTurnaroundTime = 0;
		int pcbCount = 0;
		for (int i = 0; i < pcbList.size(); i++) {
			ProcessControlBlock pcb = pcbList.get(i);
			if (pcb.getState().equals(ProcessControlBlock.TERMINATED)) {
				pcbCount++;
				sumTurnaroundTime += pcb.getTurnaroundTime();
			}
		}
		if (pcbCount != 0)
			gui.avgTurnLabel.setText(String.format("AVG Turn: %.2f", (float) sumTurnaroundTime / (float) pcbCount));
		else
			gui.avgTurnLabel.setText(String.format("AVG Turn: %.2f", 0.0));
		// average wait time
		int sumWaitTime = 0;
		pcbCount = 0;
		for (int i = 0; i < pcbList.size(); i++) {
			ProcessControlBlock pcb = pcbList.get(i);
			if (!pcb.getState().equals(ProcessControlBlock.NEW)) {
				pcbCount++;
				sumWaitTime += pcb.getReadyQueueWaitTime();
			}
		}
		if (pcbCount != 0)
			gui.avgWaitLabel.setText(String.format("AVG Wait: %.2f", (float) sumWaitTime / (float) pcbCount));
		else
			gui.avgWaitLabel.setText(String.format("AVG Wait: %.2f", 0.0));

	}

	private static void fillProgressBarShortWait() {
		gui.currentStageProgessBar.setValue(100);

		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void setProcessingTrue(ThreadData[] tds) {
		for (int i = 0; i < tds.length; i++) {
			tds[i].setProcessing(true);
		}
	}

	private static void waitForProcessing(ThreadData[] tds) {
		boolean done = false;
		while (true) {
			done = true;
			for (int i = 0; i < tds.length; i++) {
				if (tds[i].isProcessing()) {
					done = false;
					break;
				}
			}

			if (done)
				break;

			try {
				Thread.sleep(ThreadData.STAGE_WAIT_SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getSystemTime() {
		return systemTime;
	}

	private static void waitForNextStep() throws Exception {

		if (simulationFlag) {
			try {
				Thread.sleep((int) frameRateValues[gui.frameRateCountBox.getSelectedIndex()]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/* Need outside loop incase in automatic mode */
		if (killSwitch)
			throw new Exception("Kill Simulation");

		while (simulationFlag == false && simulateNextStep == false) {
			/* If in manual mode, might set the kill switch */
			if (killSwitch)
				throw new Exception("Kill Simulation");
			try {
				Thread.sleep(ThreadData.STAGE_WAIT_SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		simulateNextStep = false;
	}

	private static void createSimulation() throws FileNotFoundException {

		CPUData.setStage(CPUData.STAGE_ZERO);
		IOData.setStage(IOData.STAGE_ZERO);
		ThreadData.setTerminateThreads(false);
		systemTime = 0;
		ProcessControlBlock.resetPIDCounter();
		CPUData.setRoundRobin(false);
		CPUData.setPriorityAlgorithm(false);
		ProcessControlBlock.resetFinishedPCBCount();

		/* Process the file for PCBs */
		pcbList = ProcessQueueOnArrivalTime.extractProcesses(gui.fileChooser.getSelectedFile());

		int num_of_cpu = (int) gui.cPUCountBox.getSelectedItem();
		int num_of_io = (int) gui.iOCountBox.getSelectedItem();

		cpus = new CPUData[num_of_cpu];
		ios = new IOData[num_of_io];
		threads = new Thread[num_of_cpu + num_of_io];

		for (int i = 0; i < num_of_cpu; i++) {
			cpus[i] = new CPUData(i);
			threads[i] = new Thread(new CPUThread(cpus[i]));
		}

		for (int i = 0; i < num_of_io; i++) {
			ios[i] = new IOData(i);
			threads[i + num_of_cpu] = new Thread(new IOThread(ios[i]));
		}

		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}

		int algorithmType = gui.algorithmBox.getSelectedIndex() + 1;
		/* Create the Queues */
		if (algorithmType == 1) {// priority queue on priority value
			// create a ready queue and a wait queue, pass these 2 pointers to static
			// methods initializeReadyQueue() and initializeWaitQueue() respectively
			ReadyQueue.initializeReadyQueue(new PriorityQueue<ProcessControlBlock>(new PriorityComparator()));
			WaitQueue.initializeWaitQueue(new LinkedList<ProcessControlBlock>());
			CPUData.setPriorityAlgorithm(true);
			CPUData.setPriorityAlgorithm(1);
		} else if (algorithmType == 2) {// priority queue on ShortestJobFirst
			ReadyQueue.initializeReadyQueue(new PriorityQueue<ProcessControlBlock>(new ShortestJobFirstComparator()));
			WaitQueue.initializeWaitQueue(new LinkedList<ProcessControlBlock>());
			CPUData.setPriorityAlgorithm(true);
			CPUData.setPriorityAlgorithm(2);
		} else if (algorithmType == 3 || algorithmType == 4) {// regular queue, first-in-first-out, round robin
			ReadyQueue.initializeReadyQueue(new LinkedList<ProcessControlBlock>());
			WaitQueue.initializeWaitQueue(new LinkedList<ProcessControlBlock>());
			if (algorithmType == 4) {// RR
				CPUData.setQuantum(Integer.parseInt(gui.timeQuantumTF.getText()));
				CPUData.setRoundRobin(true);
			}
		}

	}

	private static void endSimulation() {
		gui.startButton.setEnabled(false);
		gui.manualButton.setEnabled(false);
		gui.EndPause();
		ThreadData.terminateThreads();

		/* Kill all CPU and IO threads */
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.out.println("Failed to join thread " + i);
			}
		}

	}

	private static void handleInvalidFileException(String message) {
		gui.startButton.setEnabled(false);
		gui.manualButton.setEnabled(false);
		gui.EndPause();
		gui.log(message);
	}

	private static boolean continueSimulation() {

		/* end the simulation once all processes are terminated */
		for (int i = 0; i < pcbList.size(); i++) {
			if (!pcbList.get(i).getState().equalsIgnoreCase("TERMINATED")) {
				return true;
			}
		}
		return false;
	}

	private static void resetGUI() {
		gui.output.setText("");
		outputBuilder.setLength(0);

		/* Update System Timer */
		gui.systemTimeLabel.setText("System Time: ");

		/* Update Queues */
		gui.readyQTA.setText("Empty");
		gui.iOQTA.setText("Empty");

		/* Update CPU I/O */
		gui.cPUTF.setText("N/A");
		gui.iODeviceTF.setText("N/A");

		/* Update Process Info */
		DefaultTableModel pcbInfoTableModel = new DefaultTableModel(0, 0);
		pcbInfoTableModel.setColumnIdentifiers(ProcessControlBlock.PCB_INFO_ARRAY);
		gui.pcbInfoTable.setModel(pcbInfoTableModel);

		/* Update burstsTable */
		DefaultTableModel burstsTableModel = new DefaultTableModel(0, 0);
		burstsTableModel.setColumnIdentifiers(ProcessControlBlock.BURSTS_INFO_COLUMN_NAME);
		gui.burstsTable.setModel(burstsTableModel);

		/* Update progress bar */
		gui.currentStageProgessBar.setValue(0);

		/* Update Throughput, average turnaround time, average wait time */
		gui.throughputLabel.setText("Throughput: ");
		gui.avgTurnLabel.setText("AVG Turn: ");
		gui.avgWaitLabel.setText("AVG Wait: ");
	}

	public Driver() {

		frame = new JFrame("CPU Scheduling Simulator");
		// Create the labels
		frame.setLayout(null);
		algorithm = new JLabel("Select Algorithm:");
		algorithm.setBounds(0, 0, 150, 20);
		cPUCountLabel = new JLabel("Select CPU Count:");
		cPUCountLabel.setBounds(540, 0, 150, 20);
		timeQuantumLabel = new JLabel("Enter Quantum:");
		timeQuantumLabel.setBounds(1010, 0, 150, 20);
		saveFile = new JLabel("Enter file name to save output data:");
		saveFile.setBounds(750, 850, 225, 20);
		cPULabel = new JLabel("CPU");
		cPULabel.setBounds(560, 110, 100, 20);
		readyQLabel = new JLabel("Ready Queue");
		readyQLabel.setBounds(560, 80, 225, 20);
		iODeviceLabel = new JLabel("I/O");
		iODeviceLabel.setBounds(560, 345, 100, 20);
		iOQLabel = new JLabel("I/O Queue");
		iOQLabel.setBounds(560, 315, 100, 20);
		systemTimeLabel = new JLabel("System Time:");
		systemTimeLabel.setBounds(10, 50, 120, 20);
		throughputLabel = new JLabel("Throughput:");
		throughputLabel.setBounds(150, 50, 105, 20);
		avgTurnLabel = new JLabel("AVG Turn:");
		avgTurnLabel.setBounds(300, 50, 105, 20);
		avgWaitLabel = new JLabel("AVG Wait:");
		avgWaitLabel.setBounds(460, 50, 120, 20);
		iOCountLabel = new JLabel("Select IO Count:");
		iOCountLabel.setBounds(780, 0, 150, 20);
		speedLabel = new JLabel("Speed");
		speedLabel.setBounds(350, 870, 50, 20);

		// Create the text fields
		algorithmTF = new JTextField(6);
		algorithmTF.setBounds(320, 0, 50, 60);
		fileNameTF = new JTextField(6);
		fileNameTF.setBounds(430, 0, 80, 30);
		fileNameTF.setEnabled(false);
		saveFileTF = new JTextField(6);
		saveFileTF.setBounds(750, 870, 225, 30);
		saveFileTF.setText("log.txt");
		readyQTA = new JTextField();
		readyQTA.setBounds(675, 80, 535, 20);
		readyQTA.setEnabled(false);
		readyQTA.setDisabledTextColor(Color.BLACK);
		readyQTA.setText("readyQTA");
		iOQTA = new JTextField();
		iOQTA.setBounds(675, 315, 535, 20);
		iOQTA.setEnabled(false);
		iOQTA.setText("iOQTA");
		iOQTA.setDisabledTextColor(Color.BLACK);
		timeQuantumTF = new JTextField();
		timeQuantumTF.setBounds(1110, 0, 50, 20);
		timeQuantumTF.setText("2");

		// Create text area with scroll
		output = new JTextArea();
		output.setEnabled(false);
		output.setText("output");
		output.setDisabledTextColor(Color.BLACK);
		scrollPane = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 80, 535, 470);

		// Create text areas
		cPUTF = new JTextArea();
		cPUTF.setBounds(675, 110, 535, 180);
		cPUTF.setEnabled(false);
		cPUTF.setDisabledTextColor(Color.BLACK);
		iODeviceTF = new JTextArea();
		iODeviceTF.setBounds(675, 345, 535, 180);
		iODeviceTF.setEnabled(false);
		iODeviceTF.setDisabledTextColor(Color.BLACK);
		iODeviceTF.setText("iODeviceTF");

		// Create Combo Box
		algorithmBox = new JComboBox(algorithms);
		algorithmBox.addActionListener(this);
		algorithmBox.setBounds(130, 0, 150, 20);
		cPUCountBox = new JComboBox<Integer>(cPUAndIOCountMenu);
		cPUCountBox.addActionListener(this);
		cPUCountBox.setBounds(650, 0, 50, 20);
		iOCountBox = new JComboBox<Integer>(cPUAndIOCountMenu);
		iOCountBox.addActionListener(this);
		iOCountBox.setBounds(880, 0, 50, 20);
		frameRateCountBox = new JComboBox<String>(frameRateMenu);
		frameRateCountBox.addActionListener(this);
		frameRateCountBox.setBounds(400, 870, 100, 20);

		// Create Buttons
		startButton = new JButton("Start");
		startButton.addActionListener(this);
		startButton.setBounds(10, 870, 75, 30);
		startButton.setEnabled(false);

		manualButton = new JButton("Manual");
		manualButton.addActionListener(this);
		manualButton.setBounds(95, 870, 75, 30);
		manualButton.setEnabled(false);

		loadFileButton = new JButton("Load File");
		loadFileButton.addActionListener(this);
		loadFileButton.setBounds(300, 0, 115, 30);

		saveFileButton = new JButton("Save Output");
		saveFileButton.addActionListener(this);
		saveFileButton.setBounds(600, 870, 115, 30);

		killSwitchButton = new JButton("Kill Simulation");
		killSwitchButton.addActionListener(this);
		killSwitchButton.setBounds(180, 870, 150, 30);
		killSwitchButton.setEnabled(false);

		currentStageProgessBar = new JProgressBar(); // create a progressbar
		currentStageProgessBar.setValue(0); // set initial value
		currentStageProgessBar.setStringPainted(true);
		currentStageProgessBar.setBounds(10, 35, 70, 15);

		/* JTable */
		pcbInfoTable = new JTable();
		scrollPane1 = new JScrollPane(pcbInfoTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane1.setBounds(10, 570, 1200, 150);

		burstsTable = new JTable();
		scrollPane2 = new JScrollPane(burstsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane2.setBounds(10, 740, 1200, 100);// (10, 740, 700, 100)

		frame.getContentPane().add(scrollPane);
		frame.getContentPane().add(scrollPane1);
		frame.getContentPane().add(scrollPane2);
		frame.add(algorithm);
		frame.add(fileNameTF);
		frame.add(startButton);
		frame.add(saveFile);
		frame.add(saveFileTF);
		frame.add(saveFileButton);
		frame.add(cPULabel);
		frame.add(readyQLabel);
		frame.add(iODeviceLabel);
		frame.add(iOQLabel);
		frame.add(cPUTF);
		frame.add(readyQTA);
		frame.add(iODeviceTF);
		frame.add(iOQTA);
		frame.add(algorithmBox);
		frame.add(loadFileButton);
		frame.add(cPUCountLabel);
		frame.add(cPUCountBox);
		frame.add(timeQuantumLabel);
		frame.add(timeQuantumTF);
		frame.add(systemTimeLabel);
		frame.add(throughputLabel);
		frame.add(avgTurnLabel);
		frame.add(avgWaitLabel);
		frame.add(manualButton);
		frame.add(iOCountLabel);
		frame.add(iOCountBox);
		frame.add(frameRateCountBox);
		frame.add(speedLabel);
		frame.add(currentStageProgessBar);
		frame.add(killSwitchButton);

		// Set the title of the window
		setTitle("CPU Scheduling Simulator");

		// Set the size of the window and display it
		frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
