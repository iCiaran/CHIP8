package uk.co.ciaranmoran.chip8;

public class Display implements Connectable {

    Bus bus;
    boolean[] data;

    public Display(Bus bus, int width, int height) {
        bus.addToBus(Chip8.Device.DISPLAY, this);
        this.bus = bus;

        data = new boolean[width * height];
    }

    @Override
    public void write(int address, int value) {
        data[address] = value > 0;
    }

    @Override
    public int read(int address) {
        return data[address] ? 1 : 0;
    }
}