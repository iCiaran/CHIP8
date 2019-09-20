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
    int fontStart;

    enum Device {
        CPU,
        RAM,
        DISPLAY,
        INPUT
    }

    Chip8() {
        fontStart = 0x00;

        bus = new Bus();
        cpu = new Cpu(bus, fontStart);
        display = new Display(bus, 64, 32);
        ram = new Ram(bus);
        input = new Input(bus);
    }

    void loadProgram(String filename, boolean ETI660) {
        byte[] program;

        try {
            program = Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            program = new byte[0];
        }
        int[] programOut = new int[program.length];

        for (int i = 0; i < program.length; i++) {
            programOut[i] = Byte.toUnsignedInt(program[i]);
        }

        loadProgram(programOut, ETI660);
    }

    void loadProgram(int[] program, boolean ETI660) {
        if (program == null) program = new int[0];

        int start = ETI660 ? 0x600 : 0x200;

        for (int i = 0; i < program.length; i++) {
            bus.write(Device.RAM, start + i, program[i]);
        }

        cpu.setR_PC(start);
    }

    void loadProgram(String filename) {
        loadProgram(filename, false);
    }

    void loadFont(int[] font) {
        for (int i = 0; i < (16 * 5); i++) {
            bus.write(Device.RAM, fontStart + i, font[i]);
        }
    }

    void clock(int n) {
        cpu.clock(n);
    }
}
