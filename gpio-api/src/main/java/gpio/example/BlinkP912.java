package gpio.example;

import gpio.BeagleboneGPio;
import gpio.BeagleboneGpioFactory;
import gpio.BinaryOutputPin;
import gpio.Gpio;

/**
 * Test program that blinks the P9_12 output ten times.
 * @author Koert Zeilstra
 */
public class BlinkP912 {

    public static void main(String[] args) {
        Gpio gpio = new Gpio(new BeagleboneGpioFactory());
        try {
            BinaryOutputPin pin = gpio.binaryOutputPin(BeagleboneGPio.P9_12);
            for (int i=0; i<10; i++) {
                pin.low();
                Thread.sleep(1000);
                pin.high();
                Thread.sleep(1000);
            }
            pin.low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
