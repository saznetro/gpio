package gpio;

import gpio.beaglebone.BeagleboneBinaryInputPin;
import gpio.beaglebone.BeagleboneBinaryOutputPin;
import gpio.beaglebone.BeaglebonePwmOutputPin;

import java.io.IOException;

/**
 * @author koert
 */
public class BeagleboneGpioFactory implements GpioFactory {

    private BeagleboneGpioDevice device = new BeagleboneGpioDevice();

    public BeagleboneGpioFactory() {
        this.device = device;
    }

    public BinaryInputPin createBinaryInputPin(PinDefinition pinDefinition) throws IOException {
        return new BeagleboneBinaryInputPin(pinDefinition, device);
    }

    public BinaryOutputPin createBinaryOutputPin(PinDefinition pinDefinition) throws IOException {
        return new BeagleboneBinaryOutputPin(pinDefinition, device);
    }

    public PwmOutputPin createPwmOutputPin(PinDefinition pinDefinition) throws IOException {
        return new BeaglebonePwmOutputPin(pinDefinition, device);
    }

}
