package gpio.beaglebone;

import gpio.BeagleboneGpioDevice;
import gpio.GpioDevice;
import gpio.PinDefinition;
import gpio.PwmOutputPin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;

import org.apache.log4j.Logger;

/**
 * Output PWM pin.
 * @author Koert Zeilstra
 */
public class BeaglebonePwmOutputPin implements PwmOutputPin {
	private  Logger logger=Logger.getLogger(this.getClass().toString());
    private PinDefinition pinDefinition;
    private BeagleboneGpioDevice device;
    private OutputStreamWriter period;
    private OutputStreamWriter duty;
    private OutputStreamWriter polarity;
    private long periodNs;
    private long dutyCycle;

    /**
     * Constructor.
     * @param pinDefinition Pin.
     * @param device Device abstraction.
     * @throws java.io.IOException Failed to read/write device.
     */
    public BeaglebonePwmOutputPin(PinDefinition pinDefinition, BeagleboneGpioDevice device) throws IOException {
        //To change body of created methods use File | Settings | File Templates.
        this.pinDefinition = pinDefinition;
        this.device = device;
        device.setup(pinDefinition, GpioDevice.PinUse.OUTPUT_PWM);
        File pwmTest = device.findFile(device.getOcpDir(), "pwm_test_" + pinDefinition.getKey(), true);
        logger.debug("pwmTest path "+pwmTest.getAbsolutePath()+ " by syso");
        period = new OutputStreamWriter(new FileOutputStream(new File(pwmTest, "period")));
        
        duty = new OutputStreamWriter(new FileOutputStream(new File(pwmTest, "duty")));
        polarity = new OutputStreamWriter(new FileOutputStream(new File(pwmTest, "polarity")));
        frequency((float) 8000.0).dutyCycle((float) 0.0).polarity(false);
    }

    /**
     * @param frequency Frequency.
     * @throws java.io.IOException Failed to read/write device.
     */
    public BeaglebonePwmOutputPin frequency(float frequency) throws IOException {
        if (frequency <= 0.0) {
            throw new IllegalArgumentException("frequency must be greater than 0");
        }
        this.periodNs = BigDecimal.valueOf(1e9).divide(new BigDecimal(frequency)).longValue();
        logger.debug("BigDecimal.valueOf(1e9)"+ BigDecimal.valueOf(1e9).toString()+ " "+BigDecimal.valueOf(1e9).longValue() );
        logger.debug("periodNs "+this.periodNs+ " by syso ");
        
        //this.period.write(Long.toString(this.periodNs));
        this.period.write(""+this.periodNs);
        this.period.flush();
        
        // logger.debug("periodNs "+this.periodNs+ " by syso ");
        return this;
    }

    /**
     * @param polarity Polarity.
     * @throws java.io.IOException Failed to read/write device.
     */
    public BeaglebonePwmOutputPin polarity(boolean polarity) throws IOException {
        if (polarity) {
            this.polarity.write("1");
        } else {
            this.polarity.write("0");
        }
        this.polarity.flush();
        return this;
    }

    /**
     * @param dutyCycle Duty cycle, minimum value is 0, maxiumum value is 1.
     * @throws java.io.IOException Failed to read/write device.
     */
    public BeaglebonePwmOutputPin dutyCycle(float dutyCycle) throws IOException {
        if (dutyCycle < 0.0 || dutyCycle > 1.0) {
            new IllegalArgumentException("dutyCycle must have a value from 0.0 to 1.0");
        }
        this.dutyCycle = (long) (this.periodNs * dutyCycle);
        this.duty.write(Long.toString(this.dutyCycle));
        logger.debug("dutyCycle(float dutyCycle) method: dutyCycle " + periodNs + " " + dutyCycle + " " + this.dutyCycle);
        this.duty.flush();
        return this;
    }

    /**
     * @param dutyCycle Duty cycle, minimum value is 0, maxiumum value is Short.MAX_VALUE.
     * @throws java.io.IOException Failed to read/write device.
     */
    public BeaglebonePwmOutputPin dutyCycle(short dutyCycle) throws IOException {
        if (dutyCycle < 0) {
            new IllegalArgumentException("dutyCycle must have a value from 0 to (including) Short.MAX_VALUE");
        }
        try {
        this.dutyCycle = (long) (this.periodNs * dutyCycle / Short.MAX_VALUE);
        this.duty.write(Long.toString(this.dutyCycle));
        logger.debug("dutyCycle(short dutyCycle)  method: dutyCycle " + periodNs + " " + dutyCycle + " " + this.dutyCycle);
        this.duty.flush();
        } catch (IOException e) {
            logger.debug("IOException dutyCycle=" + dutyCycle);
            throw e;
        }
        return this;
    }

    /**
     * Stop using this pin.
     */
    public void close() throws IOException {
        try {
        	//period.flush();
            period.close();
        } catch (IOException e) {
        	logger.error("Closing period was not succeed!", e);
        }
        logger.debug("Closing period was succeed!");
        try {
        	//duty.flush();
            duty.close();
        } catch (IOException e) {
        	logger.error("Closing duty was not succeed!", e);
        }
        logger.debug("Closing duty was succeed!");
        try {
        	//polarity.flush();
            polarity.close();
        } catch (IOException e) {
        	logger.error("Closing duty was not succeed!", e);
        }
        logger.debug("Closing polarity was succeed!");
        device.close(pinDefinition);
    }
}
