package jade.util;

//#J2ME_EXCLUDE_FILE

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Set;

public class ThreadDumpManager {
	public static String dumpAllThreads() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		StringBuffer sb = new StringBuffer();
		java.util.Map<Thread, StackTraceElement[]> allStackTraces = Thread.currentThread().getAllStackTraces();
		Set<Thread> threads = allStackTraces.keySet();
		for (Thread thread : threads) {
			ThreadInfo threadInfo = threadMXBean.getThreadInfo(thread.getId());
			sb.append(dumpThread("", thread, threadInfo));
		}
		long[] threadIds = threadMXBean.findMonitorDeadlockedThreads();
		if(threadIds != null) {
			ThreadInfo[] threadInfoInDeadlock = threadMXBean.getThreadInfo(threadIds);
			sb.append("\n\n\n**************** WARNING ****************: Threads ");
			for (int i = 0; i < threadInfoInDeadlock.length; i++) {
				sb.append(" \"" + threadInfoInDeadlock[i].getThreadName() + "\"");
			}
			sb.append(" are in deadlock!");
		}
		return sb.toString();
	}	

	public static String dumpThread(String prefix, Thread t) {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo threadInfo = threadMXBean.getThreadInfo(t.getId());
		return dumpThread(prefix, t, threadInfo);
	}

	public static String dumpThread(String prefix, Thread t, ThreadInfo threadInfo) {
		StringBuffer sb = new StringBuffer();
		sb.append(prefix + "\"" + t.getName() + "\"");
		if(t.isDaemon()) {
			sb.append(" daemon");
		}
		String threadId = threadInfo != null ? String.valueOf(threadInfo.getThreadId()) : String.valueOf(t.getId());
		sb.append(" tid=" + threadId);
		sb.append(" " + t.getState().toString().toLowerCase());
		if(threadInfo != null) {
			String lockedOn = threadInfo.getLockName();
			if(lockedOn != null) {
				String lockedBy = threadInfo.getLockOwnerName();
				sb.append(" on " + lockedOn);
				if(lockedBy != null) {
					sb.append(" held by " + lockedBy);
				}
			}
		}
		sb.append("\n");
		StackTraceElement[] ste = t.getStackTrace();
		for(int i=0; i<ste.length; i++) {
			sb.append(prefix + "\t at " + ste[i] + "\n");
		}
		return sb.toString();
	}

}
