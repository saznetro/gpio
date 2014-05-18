package gpio;

import gpio.epoll.EpollDescriptor;
import gpio.epoll.FileMonitor;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Device abstraction.
 * @author Koert Zeilstra
 */
public class BeagleboneGpioDevice extends GpioDevice {

    private boolean pwmInitialized = false;
    private Map<PinDefinition, PinUse> exportedPins = new HashMap<PinDefinition, PinUse>();
    private File ocpDir;

    /**
     * Setup pin before use.
     * @param pinDefinition Pin to be setup.
     * @param pinUse How to use this pin.
     * 				 Eg.: INPUT_DIGITAL, OUTPUT_DIGITAL, OUTPUT_PWM 
     * @throws java.io.IOException Failed to write to device.
     * @throws gpio.PinConfigurationException Failed to configure pin.
     */
    @Override
    public void setup(PinDefinition pinDefinition, PinUse pinUse) throws IOException, PinConfigurationException {
        if (exportedPins.containsKey(pinDefinition)) {
            throw new PinConfigurationException("Attempted to reconfigure pin: " + pinDefinition.getName());
        } else {
            String deviceName = MessageFormat.format("/sys/class/gpio/gpio{0}/direction", pinDefinition.getGpio());
            switch(pinUse) {
                case INPUT_DIGITAL:
                    writeToDevice(DEVICE_EXPORT, Integer.toString(pinDefinition.getGpio()));
                    writeToDevice(deviceName, "in");
                    break;
                case OUTPUT_DIGITAL:
                    writeToDevice(DEVICE_EXPORT, Integer.toString(pinDefinition.getGpio()));
                    writeToDevice(deviceName, "out");
                    break;
                case OUTPUT_PWM:
                    initializePwm();
                    loadDeviceTree(MessageFormat.format("bone_pwm_{0}", pinDefinition.getKey()));
                    break;
            }
            exportedPins.put(pinDefinition, pinUse);
        }
    }

    /**
     * Setup pin before use.
     * @param pinDefinition Pin to be setup.
     * @throws java.io.IOException Failed to write to device.
     * @throws gpio.PinConfigurationException Failed to configure pin.
     */
    @Override
    public void setupPwm(PinDefinition pinDefinition) throws IOException, PinConfigurationException {
        if (exportedPins.containsKey(pinDefinition)) {
            throw new PinConfigurationException("Attempted to reconfigure pin: " + pinDefinition.getName());
        } else {
            initializePwm();
            loadDeviceTree(MessageFormat.format("bone_pwm_{0}", pinDefinition.getKey()));
            exportedPins.put(pinDefinition, PinUse.OUTPUT_PWM);
        }
    }

    /**
     * Setup pin before use.
     * @param pinDefinition Pin to be setup.
     * @throws java.io.IOException Failed to write to device.
     * @throws gpio.PinConfigurationException Failed to configure pin.
     */
    @Override
    public void close(PinDefinition pinDefinition) throws IOException, PinConfigurationException {
        if (!exportedPins.containsKey(pinDefinition)) {
            throw new PinConfigurationException("Attempted to close unconfigured pin: " + pinDefinition.getName());
        } else {
            PinUse pinUse = exportedPins.get(pinDefinition);
            switch(pinUse) {
                case OUTPUT_PWM:
                    unloadDeviceTree(MessageFormat.format("bone_pwm_{0}", pinDefinition.getKey()));
                    break;
                default:
                    break;
            }
            writeToDevice(DEVICE_UNEXPORT, Integer.toString(pinDefinition.getGpio()));
            exportedPins.remove(pinDefinition);
        }
    }

    /**
     * Set value of output pin.
     * @param pinDefinition Pin.
     * @param value True: high, false: low.
     * @throws java.io.IOException Failed to write to device.
     */
    @Override
    public void setValue(PinDefinition pinDefinition, boolean value) throws IOException {
        if (exportedPins.containsKey(pinDefinition)) {
            if (exportedPins.get(pinDefinition) == PinUse.OUTPUT_DIGITAL) {
                String text = null;
                if (value) {
                    text = "1";
                } else {
                    text = "0";
                }
                String deviceName = MessageFormat.format("/sys/class/gpio/gpio{0}/value", pinDefinition.getGpio());
                writeToDevice(deviceName, text);
            } else {
                throw new PinConfigurationException("Pin is not configured for output: " + pinDefinition.getName());
            }
        } else {
            throw new PinConfigurationException("Pin not configured: " + pinDefinition.getName());
        }
    }

    /**
     * Read state of pin.
     * @param pinDefinition Pin.
     * @return True if input is high, otherwise false.
     */
    @Override
    public boolean getBooleanValue(PinDefinition pinDefinition) throws IOException {
        String deviceName = MessageFormat.format("/sys/class/gpio/gpio{0}/value", pinDefinition.getGpio());
        InputStreamReader reader = new InputStreamReader(new FileInputStream(deviceName));

        char[] buffer = new char[1];
        int length = reader.read(buffer);
        if (length == 0) {
            throw new IOException("Failed to read value from device '" + deviceName  + "'");
        }
        boolean value = false;
        if (buffer[0] != '0') {
            value = true;
        }
        return value;
    }

//    /**
//     * Read state of pin.
//     * @param pinDefinition Pin.
//     * @return True if input is high, otherwise false.
//     */
//    public FileChannel getChannel(PinDefinition pinDefinition) throws IOException {
//        String deviceName = MessageFormat.format("/sys/class/gpio/gpio{0}/value", pinDefinition.getGpio());
//        FileInputStream fis = new FileInputStream(deviceName);
//        return fis.getChannel();
//    }

    /**
     * Read state of pin.
     * @param pinDefinition Pin.
     * @return True if input is high, otherwise false.
     */
    public void setEdge(PinDefinition pinDefinition, Edge edge) throws IOException {
        String deviceName = MessageFormat.format("/sys/class/gpio/gpio{0}/edge", pinDefinition.getGpio());
        writeToDevice(deviceName, edge.getCode());
    }

    /**
     * Create monitor for interrupts on file/device.
     * @return File/device monitor.
     */
    @Override
    public FileMonitor createFileMonitor() {
        return new EpollDescriptor();
    }

    /**
     * @return OCP directory.
     */
    public File getOcpDir() {
        return ocpDir;
    }

    private void loadDeviceTree(final String name) throws IOException {
        File capeMgrDir = findFile(new File("/sys/devices"), "bone_capemgr", true);
        File slotsFile = new File(capeMgrDir, "slots");
        boolean deviceFound = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(slotsFile));
            String line = reader.readLine();
            while(line != null) {
               logger.debug("line: " + line);
                if (line != null && line.contains(name)) {
                    deviceFound = true;
                }
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        if (!deviceFound) {
            writeToDevice(slotsFile.getAbsolutePath(), name);
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
    }

    private void unloadDeviceTree(final String name) throws IOException {
        File capeMgrDir = findFile(new File("/sys/devices"), "bone_capemgr", true);
        File slotsFile = new File(capeMgrDir, "slots");
        boolean deviceFound = false;
        BufferedReader reader = null;
        try {
            String lineNumber = null;
            reader = new BufferedReader(new FileReader(slotsFile));
            String line = reader.readLine();
            while(line != null) {
            	logger.debug("line: " + line);
                int index = line.indexOf(':');
                if (index >= 0) {
                    lineNumber = line.substring(0, index);
                }
                line = reader.readLine();
            }
            try {
                reader.close();
            } catch (IOException e) {
            }
            if (lineNumber != null) {
                writeToDevice(slotsFile.getAbsolutePath(), "-" + lineNumber);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        if (!deviceFound) {
            writeToDevice(slotsFile.getAbsolutePath(), name);
        }
    }

    public File findFile(File directory, String name, boolean required) {
        File found = null;
        for(File file : directory.listFiles()) {
            if (file.getName().contains(name)) {
                found = file;
            }
        }
        if (required && found == null) {
            throw new PinConfigurationException(name + " not found " + directory.getAbsolutePath());
        }
        return found;
    }

    private void initializePwm() throws IOException {
        if (!pwmInitialized) {
            pwmInitialized = true;
            loadDeviceTree("am33xx_pwm");
            ocpDir = findFile(new File("/sys/devices"), "ocp", true);
        }
    }

}
