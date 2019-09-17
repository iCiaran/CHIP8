package uk.co.ciaranmoran.chip8;

public class Main {
    public static void main(String[] args) {
        final int CLOCK = 500;
        final long PERIOD = 1000 / CLOCK;

        Chip8 chip8 = new Chip8();
        byte[] program = {0x12, 0x00};
        chip8.loadProgram(program, false);

        long start = System.currentTimeMillis();
        long dt;
        while (true) {
            dt = (System.currentTimeMillis() - start);
            if (dt >= PERIOD) {
                start = System.currentTimeMillis();
                chip8.clock((int) (dt / PERIOD));
            }
        }
    }
}