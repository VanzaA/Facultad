package jade.core;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.imtp.leap.JICP.PDPContextManager;
import jade.imtp.leap.JICP.JICPProtocol;

import java.io.*;
import java.util.Random;

// All JADE configuration options must be specified prefixing them with "jade."
// Command line example:
// java -cp .... -jade.port 2099 -jade.proto ssl -i 100 -s 10000 -t 5000 -n 10 -measure rtt
// Launch the Round Trip Time measure using 10 sender-receiver couples performing 100
// send-receive iterations, using messages with a content of 10K and waiting 5 secs after each
// iteration. 
// FrontEnds connect to the local host (default) on port 2099 and using the ssl protocol.  
public class ScalabilityTest {
	// Size of the content of each message exchanged during the test
	private static final String CONTENT_SIZE = "s";
	private static final int DEFAULT_CONTENT_SIZE = 1000;

	// Time between two successive iterations
	private static final String TIME_INTERVAL = "t";
	private static final long DEFAULT_TIME_INTERVAL = 1000;
	private static final long STEPBYSTEP_TIME_INTERVAL = -1;

	// Number of iterations
	private static final String N_ITERATIONS = "i";
	private static final int DEFAULT_N_ITERATIONS = -1;

	// Number of sender-receiver couples
	private static final String N_COUPLES = "n";
	private static final int DEFAULT_N_COUPLES = 10;

	// Base cnt for assigning names to agents. This allows launching more than one
	// ScalabilityTest in parallel without conflicts in agent names 
	private static final String BASE = "base";
	private static final int DEFAULT_BASE = 0;

	// Test initialization mode 
	// - Ready-go mode: All agents starts and, when they are all ready, the user is prompted 
	// to start the test by pressing Enter (default and suggested mode)
	// - Fast mode: All agents begin the test as soon as they are started
	// - Slow mode: Wait a bit after each sender-receiver couple initialization 
	// - Step-by-step mode: Prompt the user after each sender-receiver couple initialization
	private static final String MODE = "mode";
	private static final String FAST_MODE_S = "fast";
	private static final String SLOW_MODE_S = "slow";
	private static final String STEP_BY_STEP_MODE_S = "stepbystep";
	private static final String READY_GO_MODE_S = "readygo";
	private static final int FAST_MODE = 0;
	private static final int SLOW_MODE = 1;
	private static final int STEP_BY_STEP_MODE = 2;
	private static final int READY_GO_MODE = 3;
	
	// If specified makes agents begin the test scattered randomly within a time interval.
	// Has no effect if time-interval is <= 0 (e.g. step-by-step mode)
	private static final String RANDOM_START_S = "randomstart";

	// Target measure: bitrate or round trip time
	private static final String MEASURE = "measure";
	private static final String BITRATE_MEASURE_S = "bitrate";
	private static final String RTT_MEASURE_S = "rtt";
	private static final int BITRATE_MEASURE = 0;
	private static final int RTT_MEASURE = 1;

	private static Object terminatedLock = new Object();
	private static Object readyLock = new Object();	
	private static Object semaphore = new Object();

	private static byte[] content;
	private static long timeInterval;
	private static int nIterations;
	private static int nCouples;
	private static int base;
	private static int mode; 
	private static boolean randomStart;
	private static int measure;
	private static int readyCnt = 0;
	private static int terminatedCnt = 0;

	private static long totalTime = 0;
	private static long totalTime2 = 0;

	private static BufferedReader inputReader;
	
	private static Random random = new Random();

	public static void main(String[] args) {
		ExtendedProperties pp = parseArguments(args);
		Properties jadeProps = pp.extractSubset("jade.");
		System.out.println("JADE PROPERTIES: "+jadeProps);

		int contentSize = DEFAULT_CONTENT_SIZE;
		try {
			contentSize = Integer.parseInt(pp.getProperty(CONTENT_SIZE));
		}
		catch (Exception e) {
			// Keep default
		}
		content = new byte[contentSize];

		timeInterval = DEFAULT_TIME_INTERVAL;
		try {
			timeInterval = Long.parseLong(pp.getProperty(TIME_INTERVAL));
		}
		catch (Exception e) {
			// Keep default
		}

		nIterations = DEFAULT_N_ITERATIONS;
		try {
			nIterations = Integer.parseInt(pp.getProperty(N_ITERATIONS));
		}
		catch (Exception e) {
			// Keep default
		}

		nCouples = DEFAULT_N_COUPLES;
		try {
			nCouples = Integer.parseInt(pp.getProperty(N_COUPLES));
		}
		catch (Exception e) {
			// Keep default
		}

		base = DEFAULT_BASE;
		try {
			base = Integer.parseInt(pp.getProperty(BASE));
		}
		catch (Exception e) {
			// Keep default
		}

		mode = READY_GO_MODE;
		try {
			String modeStr = pp.getProperty(MODE);
			if (SLOW_MODE_S.equals(modeStr)) {
				mode = SLOW_MODE;
			}
			else if (FAST_MODE_S.equals(modeStr)) {
				mode = FAST_MODE;
			}
			else if (STEP_BY_STEP_MODE_S.equals(modeStr)) {
				mode = STEP_BY_STEP_MODE;
			}
		}
		catch (Exception e) {
			// Keep default
		}
		// Prepare the inputReader to get user inputs if necessary
		if (mode == READY_GO_MODE || mode == STEP_BY_STEP_MODE) {
			inputReader = new BufferedReader(new InputStreamReader(System.in));
		}
		
		randomStart = "true".equals(pp.getProperty(RANDOM_START_S, "false"));
		

		measure = BITRATE_MEASURE;
		try {
			String measureStr = pp.getProperty(MEASURE);
			if (RTT_MEASURE_S.equals(measureStr)) {
				measure = RTT_MEASURE;
			}
		}
		catch (Exception e) {
			// Keep default
		}

		String prefix = Profile.getDefaultNetworkName();
		for (int i = base; i < base+nCouples; i++) {
			initCouple(jadeProps, prefix, i);
			switch (mode) {
			case SLOW_MODE:
				waitABit();
				break;
			case STEP_BY_STEP_MODE:
				prompt("Couple #"+i+" started. Press enter to continue");
				break;
			default:
				Thread.currentThread().yield();
			}
		}

		waitUntilReady();
		if (mode == READY_GO_MODE) {
			prompt("All "+nCouples+" couples ready. Press enter to go");
		}	
		start();
		if (nIterations > 0) {
			System.out.println("Measurement started....");
		}

		if (timeInterval == STEPBYSTEP_TIME_INTERVAL) {
			int i = 0;
			while (true) {
				waitUntilReady();
				prompt("Iteration # "+i+" terminated by all couples. Press enter to go");
				++i;
				start();
			}
		}
	}

	private static void notifyReady() {
		synchronized (semaphore) {
			synchronized (readyLock) {
				readyCnt++;
				readyLock.notifyAll();
			}
			try {
				semaphore.wait();
			}
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	private static void waitUntilReady() {
		synchronized (readyLock) {
			while (readyCnt < nCouples) {
				try {
					readyLock.wait();
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}

	private static void start() {
		synchronized (semaphore) {
			semaphore.notifyAll();			
			readyCnt = 0;
		}
	}


	private static void waitABit(long timeout) {
		try {
			Thread.sleep(timeout);
		}
		catch (Exception e) {}
	}
	
	private static void waitABit() {
		waitABit(1000);
	}

	private static void prompt(String msg) {
		System.out.println(msg);
		try {
			inputReader.readLine();
		}
		catch (IOException ioe) {
		}
	}

	private static ExtendedProperties parseArguments(String[] args) {
		ExtendedProperties props = new ExtendedProperties();
		int i = 0;
		while (i < args.length) {
			if (args[i].startsWith("-")) {
				// Parse next option
				String name = args[i].substring(1);
				if (++i < args.length) {
					props.setProperty(name, args[i]);
				}
				else {
					throw new IllegalArgumentException("No value specified for property \""+name+"\"");
				}
				++i;
			}
			else {
				throw new IllegalArgumentException("Invalid property \""+args[i]+"\". It does not start with '-'");
			}
		}

		return props;
	}

	private static void initCouple(Properties basePP, String prefix, int index) {
		String senderClass = "jade.core.ScalabilityTest$BitrateSenderAgent";
		String receiverClass = "jade.core.ScalabilityTest$BitrateReceiverAgent";
		if (measure == RTT_MEASURE) {
			senderClass = "jade.core.ScalabilityTest$RTTSenderAgent";
			receiverClass = "jade.core.ScalabilityTest$RTTReceiverAgent";
		}

		Properties pp = (Properties) basePP.clone();
		/*Properties pp = new Properties();
		if (host != null) {
			pp.setProperty(MicroRuntime.HOST_KEY, host);
		}
		if (port != null) {
			pp.setProperty(MicroRuntime.PORT_KEY, port);
		}
		if (proto != null) {
			pp.setProperty(MicroRuntime.PROTO_KEY, proto);
		}
		if (maxDiscTime != null) {
			pp.setProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY, maxDiscTime);
		}*/
		String sName = "S-"+prefix+"-"+index;
		pp.setProperty(PDPContextManager.MSISDN, sName);
		String rName = "R-"+prefix+"-"+index;
		String prop = sName+":"+senderClass+"("+rName+")";
		pp.setProperty(MicroRuntime.AGENTS_KEY, prop);
		//pp.setProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY, "-1");
		FrontEndContainer fes = new FrontEndContainer();
		fes.start(pp);

		pp = (Properties) basePP.clone();
		/*pp = new Properties();
		if (host != null) {
			pp.setProperty(MicroRuntime.HOST_KEY, host);
		}
		if (port != null) {
			pp.setProperty(MicroRuntime.PORT_KEY, port);
		}
		if (proto != null) {
			pp.setProperty(MicroRuntime.PROTO_KEY, proto);
		}
		if (maxDiscTime != null) {
			pp.setProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY, maxDiscTime);
		}*/
		pp.setProperty(PDPContextManager.MSISDN, rName);
		prop = rName+":"+receiverClass;
		pp.setProperty(MicroRuntime.AGENTS_KEY, prop);
		//pp.setProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY, "-1");
		FrontEndContainer fer = new FrontEndContainer();
		fer.start(pp);
	}

	private static void notifyTerminated(long time, long time2) {
		synchronized (terminatedLock) {
			totalTime += time;
			totalTime2 += time2;
			terminatedCnt++;
			if (terminatedCnt == nCouples) {
				// All couples have terminated. Compute the average round-trip time
				long n = nCouples * ((long) nIterations);
				if (measure == BITRATE_MEASURE) {
					double totBytes = n * ((double) content.length);
					double averageBitrate = totBytes / totalTime;
					System.out.println("----------------------------------\nTest completed successufully.\nAverage bitrate (Kbyte/s) = "+averageBitrate+"\n----------------------------------");
				}
				else if (measure == RTT_MEASURE) {
					long averageRoundTripTime = totalTime / n;
					double avg = (double) averageRoundTripTime;
					double x = totalTime2 + n*avg*avg - 2*avg*totalTime;
					double standardDeviation = Math.sqrt(x / n);
					System.out.println("----------------------------------\nTest completed successufully.\nAverage round trip time = "+averageRoundTripTime+" ms\nStandard deviation = "+standardDeviation+"\n----------------------------------");
				}
				System.exit(0);
			}
		}
	}
	
	private static long getRandomTime(long max) {
		float f = random.nextFloat();
		// f is a random value between 0.0 and 1.0
		if (f == 0) {
			// Avoid returning 0
			return 10;
		}
		return (long) f * max;
	}


	/**
	   Inner class BitrateSenderAgent
	   Send messages to the corresponding receiver.
	   When the receiver gets message 0 takes the START time. 
	   When the receiver gets message N (number of iterations) takes the END time  
	   and completes
	 */
	public static class BitrateSenderAgent extends Agent {
		private ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		private boolean firstRound = true;
		private AID myReceiver;

		protected void setup() {
			Object[] args = getArguments();
			if (args != null && args.length == 1) {
				myReceiver = new AID((String) args[0], AID.ISLOCALNAME);
			}
			else {
				System.out.println("Missing receiver name !!!!!");
				doDelete();
				return;
			}
			msg.addReceiver(myReceiver);
			msg.setByteSequenceContent(content);

			System.out.println("BitRate-Sender "+getName()+" ready: my receiver is "+myReceiver.getName());
			notifyReady();

			if (randomStart && timeInterval > 0) {
				waitABit(getRandomTime(timeInterval));
			}
			
			if (timeInterval > 0) {
				addBehaviour(new TickerBehaviour(this, timeInterval) {
					public void onTick() {
						job();
					}
				} );
			}
			else {
				addBehaviour(new CyclicBehaviour(this) {
					public void action() {
						job();
					}
				} );
			}
		}

		private void job() {
			if (firstRound) {
				long time = System.currentTimeMillis();
				// Insert the start-time ion the protocol field
				msg.setProtocol(String.valueOf(time));
				firstRound = false;
			}
			send(msg);
			if (timeInterval == STEPBYSTEP_TIME_INTERVAL) {
				notifyReady();
			}
		}
	} // END of inner class BitrateSenderAgent


	/**
	   Inner class BitrateReceiverAgent
	 */
	public static class BitrateReceiverAgent extends Agent {
		private boolean firstReceived = false;
		private boolean terminated = false;
		private long startTime;
		private int cnt = 0;

		protected void setup() {
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					ACLMessage msg = myAgent.receive();
					if (msg != null) {
						cnt++;
						if (!firstReceived) {
							firstReceived = true;
							// First message: get startTime from the protocol field 
							startTime = Long.parseLong(msg.getProtocol());
						}
						if (!terminated) {
							if (nIterations > 0 && cnt >= nIterations) {
								long endTime = System.currentTimeMillis();
								long totalCoupleTime = endTime - startTime;
								// Unless we are in step-by-step mode, take time interval between iterations into account
								if (timeInterval != STEPBYSTEP_TIME_INTERVAL) {
									totalCoupleTime -= (nIterations - 1) * timeInterval;
								}
								long totalCoupleTime2 = totalCoupleTime*totalCoupleTime;
								notifyTerminated(totalCoupleTime, totalCoupleTime2);
								terminated = true;
							}
						}
					}
					else {
						block();
					}
				}
			} );
		}
	} // END of inner class BitrateReceiverAgent


	/**
	   Inner class RTTSenderAgent
	   Send messages to the corresponding receiver. For each message waits for the
	   response and compute the round trip time.
	 */
	public static class RTTSenderAgent extends Agent {
		private ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		private AID myReceiver;
		private MessageTemplate myTemplate;

		private long totalCoupleTime = 0;
		private long totalCoupleTime2 = 0;
		private boolean terminated = false;

		private int cnt = 0;

		protected void setup() {
			Object[] args = getArguments();
			if (args != null && args.length == 1) {
				myReceiver = new AID((String) args[0], AID.ISLOCALNAME);
			}
			else {
				System.out.println("Missing receiver name !!!!!");
				doDelete();
				return;
			}
			msg.addReceiver(myReceiver);
			msg.setByteSequenceContent(content);
			myTemplate = MessageTemplate.MatchSender(myReceiver);

			System.out.println("RTT-Sender "+getName()+" ready: my receiver is "+myReceiver.getName());
			notifyReady();

			if (timeInterval > 0) {
				addBehaviour(new TickerBehaviour(this, timeInterval) {
					public void onTick() {
						job();
					}
				} );
			}
			else {
				addBehaviour(new CyclicBehaviour(this) {
					public void action() {
						job();
					}
				} );
			}				
		}

		private void job() {
			long start = System.currentTimeMillis();
			send(msg);
			blockingReceive(myTemplate);
			long time = System.currentTimeMillis() - start;

			if (!terminated) {
				System.out.println("Agent "+getLocalName()+" "+cnt+" OK");
				totalCoupleTime += time;
				totalCoupleTime2 += (time*time);
				if (nIterations > 0 && (++cnt) >= nIterations) {
					notifyTerminated(totalCoupleTime, totalCoupleTime2);
					terminated = true;
				}
			}
			if (timeInterval == STEPBYSTEP_TIME_INTERVAL) {
				notifyReady();
			}
		}
	} // END of inner class RTTSenderAgent


	/**
	   Inner class RTTReceiverAgent
	 */
	public static class RTTReceiverAgent extends Agent {
		private int cnt = 0;
		protected void setup() {
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					ACLMessage msg = myAgent.receive();
					if (msg != null) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						reply.setByteSequenceContent(msg.getByteSequenceContent());
						myAgent.send(reply);
						System.out.println("Agent "+getLocalName()+" message "+cnt+" received");
						cnt++;
					}
					else {
						block();
					}
				}
			} );
		}
	} // END of inner class RTTReceiverAgent

}