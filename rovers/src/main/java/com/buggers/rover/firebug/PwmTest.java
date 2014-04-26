package com.buggers.rover.firebug;

import java.io.IOException;

import org.apache.log4j.Logger;

import gpio.BeagleboneGPio;
import gpio.BeagleboneGpioFactory;
import gpio.BinaryOutputPin;
import gpio.Gpio;
import gpio.PwmOutputPin;

public class PwmTest {
	private  Logger logger=Logger.getLogger(this.getClass().toString());
	public static void main(String[] args) {
		new PwmTest().pwmtest();
		// TODO Auto-generated method stub

	}

	
	public void pwmtest(){
		Gpio gpio = new Gpio(new BeagleboneGpioFactory());
		PwmOutputPin pin=null;
		BinaryOutputPin pin2=null;
		System.out.println("Program started by syso");
		logger.info("Program started by logger");
		try {
			pin2 = gpio.binaryOutputPin(BeagleboneGPio.P9_15);
			pin = gpio.pwmOutputPin(BeagleboneGPio.P9_16);
			logger.info("Pins setting was succes");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Pins setting up was fail",e);
			return;
		}
		
		try {
			pin.frequency((float) 8000.0);
			pin.dutyCycle((float) 0.15);
			pin.polarity(false);
			pin2.low();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			logger.error("Pins setting value was fail",e2);
		}
		for (int i = 3; i > 0; i--) {
			logger.info(i+ " sec until end of program");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("thread sleep fail",e);
			}
		}
		try {
			
			pin.close();
			pin2.close();
			logger.info("Pin closing succes");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Pins close was fail",e);
		}
		logger.info("everything was succes");
	}
	
	
}
