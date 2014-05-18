package com.buggers.rover.firebug;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.buggers.rover.util.Utility;

import gpio.BeagleboneGPio;
import gpio.BeagleboneGpioFactory;
import gpio.BinaryOutputPin;
import gpio.Gpio;
import gpio.PwmOutputPin;

/**
 * 
 * @author saznetro
 * 
 *         FireBug technical details: Two motorcontroller.
 */
public class FirebugMotorControl {
	private Logger logger = Logger.getLogger(this.getClass().toString());

	public static final float FREQUENCY = (float) 8000.0;
	public static final boolean POLARITY = false;
	//Maximum power in case of 4s(14.4V) accumlator 
	public static final float MAXPOWERWITH4S=(float) 0.75;
	//Minimum power to start moving in case of 4s(14.4V) accumlator 
	public static final float MINPOWERWITH4S=(float) 0.10;
	public static final float VALIDMINPOWER=(float) 0.0;
	// Left side
	PwmOutputPin leftMotorsPWMPin = null;
	BinaryOutputPin leftMotorsGPIOPin = null;
	// Rigth side
	PwmOutputPin rightMotorsPWMPin = null;
	BinaryOutputPin rightMotorsGPIOPin = null;

	public FirebugMotorControl() {
		logger.info("Initializing pins.");
		Gpio gpio = new Gpio(new BeagleboneGpioFactory());
		try {
			rightMotorsPWMPin = gpio.pwmOutputPin(BeagleboneGPio.P8_13);
			rightMotorsGPIOPin = gpio.binaryOutputPin(BeagleboneGPio.P9_15);
			leftMotorsPWMPin = gpio.pwmOutputPin(BeagleboneGPio.P9_16);
			leftMotorsGPIOPin = gpio.binaryOutputPin(BeagleboneGPio.P9_12);
		} catch (IOException e) {
			logger.error("Pin init failed", e);
		}

		logger.info("Initializing pins done.");
		Utility.sleep(3);
	}

	public void setStartValue() {

		new Thread() {
			public void run() {
				try {
					rightMotorsPWMPin.frequency((float) FREQUENCY);
					rightMotorsPWMPin.dutyCycle((float) 0);
					rightMotorsPWMPin.polarity(POLARITY);
					rightMotorsGPIOPin.high();
					logger.debug("rigth motor start success");
				} catch (IOException e) {
					logger.error("Pins setting start value of the right was fail", e);
				}
			}
		}.start();
		logger.debug("Pins setting start value of the right was succes");
		new Thread() {
			public void run() {
				try {
					leftMotorsPWMPin.frequency((float) FREQUENCY);
					leftMotorsPWMPin.dutyCycle((float) 0);
					leftMotorsPWMPin.polarity(POLARITY);
					leftMotorsGPIOPin.low();
					logger.debug("left motor start success");
				} catch (IOException e) {
					logger.error("Pins setting start value of the right was fail", e);
				}
			}
		}.start();
		logger.debug("Pins setting start value of the left was succes");

		logger.info("Start value setting succeed.");
		Utility.sleep(3);
	}

	public void stop() {
		try {
			rightMotorsPWMPin.dutyCycle((float) 0);
			leftMotorsPWMPin.dutyCycle((float) 0);
		} catch (IOException e) {
			logger.error("Pins setting start value was fail", e);
		}
		logger.debug("Stop succeed.");
	}

	public void detachPins() {
		try {
			rightMotorsPWMPin.close();
			rightMotorsGPIOPin.close();
			// leftMotorsPWMPin.close(); //TODO BUG causing full speed if its uncommented
			leftMotorsGPIOPin.close();
		} catch (IOException e) {
			logger.error("Pins closing was fail", e);
		}
		logger.debug("Closing succeed.");
	}

	public void moveForward() {
		try {
			rightMotorsGPIOPin.high();
			logger.debug("Left motor GPIO set to HIGH");
			rightMotorsPWMPin.dutyCycle((float) 0.10);
			logger.debug("rigth motor duty set to 10");
			leftMotorsGPIOPin.low();
			logger.debug("Left motor GPIO set to LOW");
			leftMotorsPWMPin.dutyCycle((float) 0.10);
			logger.debug("left motor duty set to 10");
		} catch (IOException e) {
			logger.error("Pins setting value when moving forward was fail", e);
		}
		logger.debug("Move forward set succeed.");
	}
	
	/**
	 * 
	 * @param time
	 * 
	 * @param power valid value betweeen 0.0 and 1.0
	 * 		  eg.: 0.10
	 */
	
	public void moveForward(float time, float power) {
		setForwardDirection();
		setMotorsPowerWithSameValue(power);
		Utility.sleep((int) Math.round(time));
	}
	
	
	public void moveBackward(float time, float power) {
		setBackwardDirection();
		setMotorsPowerWithSameValue(power);
		Utility.sleep((int) Math.round(time));
	}
	
	public void turnLeft(float time, float power) {
		setToTurnLeft();
		setMotorsPowerWithSameValue(power);
		Utility.sleep((int) Math.round(time));
	}
	
	public void turnRight(float time, float power) {
		setToTurnLeft();
		setMotorsPowerWithSameValue(power);
		Utility.sleep((int) Math.round(time));
	}
	
	private void setMotorsPowerWithSameValue(float power){
		power = powerValidator(power);
		try {
			float powerPercent=(power*100);
			rightMotorsPWMPin.dutyCycle((float) power);
			logger.debug("Rigth motor duty set to"+powerPercent+ "%");
			leftMotorsPWMPin.dutyCycle((float) power);
			logger.debug("Left motor duty set to"+powerPercent+ "%");
		} catch (IOException e) {
			logger.error("Pins setting value when moving forward was fail", e);
		}
		logger.debug("setMotorsPowerWithSameValue was succeed.");
	}

	private void setForwardDirection(){
		try {
			rightMotorsGPIOPin.high();
			logger.debug("Left motor GPIO set to HIGH");
			leftMotorsGPIOPin.low();
			logger.debug("Left motor GPIO set to LOW");
		} catch (IOException e) {
			logger.error("Pins setting value when moving forward was fail", e);
		}
		logger.debug("Forward direction set was succeed.");
	}
	
	
	private void setBackwardDirection(){
		try {
			rightMotorsGPIOPin.low();
			logger.debug("Left motor GPIO set to HIGH");
			leftMotorsGPIOPin.high();
			logger.debug("Left motor GPIO set to LOW");
		} catch (IOException e) {
			logger.error("Pins setting value when moving forward was fail", e);
		}
		logger.debug("Forward direction set was succeed.");
	}

	private void setToTurnLeft(){
		try {
			rightMotorsGPIOPin.low();
			logger.debug("Left motor GPIO set to HIGH");
			leftMotorsGPIOPin.low();
			logger.debug("Left motor GPIO set to LOW");
		} catch (IOException e) {
			logger.error("Pins setting value when moving forward was fail", e);
		}
		logger.debug("Forward direction set was succeed.");
	}
	
	private void setToTurnRight(){
		try {
			rightMotorsGPIOPin.high();
			logger.debug("Left motor GPIO set to HIGH");
			leftMotorsGPIOPin.high();
			logger.debug("Left motor GPIO set to HIGH");
		} catch (IOException e) {
			logger.error("Pins setting value when moving forward was fail", e);
		}
		logger.debug("Forward direction set was succeed.");
	}
	
	void getSoftStatusOfPins() {

	}

	void getHardStatusOfPins() {

	}

	private float powerValidator(float power){
		if (power<VALIDMINPOWER){
			logger.error("The power is less than \""+VALIDMINPOWER+"\"! Power value will be set to this "+VALIDMINPOWER);
			return VALIDMINPOWER;
		}
		if (power>MAXPOWERWITH4S){
			logger.error("The power is more than \""+MAXPOWERWITH4S+"\"! Power value will be set to this "+MAXPOWERWITH4S);
			return MAXPOWERWITH4S;
		}		
		return power;
	}
}
