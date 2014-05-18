package com.buggers.rover.util;

import org.apache.log4j.Logger;

public class Utility {
	private static Logger logger = Logger.getLogger(Utility.class);
	
	/**
	 * Sleeping with the current thread for seconds.
	 * 
	 * @param seconds
	 */
	public static void sleep(int seconds) {
		for (int i = seconds; i > 0; i--) {
			logger.info(i + " sec until end of program");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("thread sleep fail", e);
			}
		}
	}
}
