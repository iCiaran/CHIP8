package uk.co.ciaranmoran.chip8;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Chip8 {

    Bus bus;
    Cpu cpu;
    Display display;
    Ram ram;
    Input input;

    enum Device {
        CPU,
        RAM,
        DISPLAY,
        INPUT
    }

    public Chip8() {
        bus = new Bus();
        cpu = new Cpu(bus);
        display = new Display(bus, 62, 32);
        ram = new Ram(bus);
        input = new Input(bus);
    }

    public void loadProgram(String filename, boolean ETI660) {
        int start = ETI660 ? 0x600 : 0x200;
        byte[] program;

        try {
            program = Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            program = new byte[0];
        }

        for(int i = start; i < start + program.length; i++) {
            bus.write(Device.RAM, start + i, program[i]);
        }

    }

    public void loadProgram(String filename) {
        loadProgram(filename, false);
    }
}
