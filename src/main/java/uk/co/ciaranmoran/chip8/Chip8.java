package uk.co.ciaranmoran.chip8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Chip8 {

    private Bus bus;
    Cpu cpu;
    private Display display;
    private Ram ram;
    private Input input;

    enum Device {
        CPU,
        RAM,
        DISPLAY,
        INPUT
    }

    Chip8() {
        bus = new Bus();
        cpu = new Cpu(bus);
        display = new Display(bus, 62, 32);
        ram = new Ram(bus);
        input = new Input(bus);
    }

    void loadProgram(String filename, boolean ETI660) {
        int start = ETI660 ? 0x600 : 0x200;
        byte[] program;

        try {
            program = Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            program = new byte[0];
        }

        for (int i = 0; i < program.length; i++) {
            bus.write(Device.RAM, start + i, program[i]);
        }

        cpu.setR_PC(start);
    }

    void loadProgram(byte[] program, boolean ETI660) {
        if (program == null) program = new byte[0];

        int start = ETI660 ? 0x600 : 0x200;

        for (int i = 0; i < program.length; i++) {
            bus.write(Device.RAM, start + i, program[i]);
        }

        cpu.setR_PC(start);
    }

    public void loadProgram(String filename) {
        loadProgram(filename, false);
    }

    void clock(int n) {
        cpu.clock(n);
    }
}
