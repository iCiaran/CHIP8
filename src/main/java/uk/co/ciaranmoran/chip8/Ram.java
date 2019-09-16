package uk.co.ciaranmoran.chip8;

public class Ram implements Connectable{

    Bus bus;
    int[] data;

    public Ram(Bus bus) {
        bus.addToBus(Chip8.Device.RAM, this);
        this.bus = bus;
        data = new int[0xFFF];
    }

    @Override
    public void write(int address, int value) {
        data[address] = value;
    }

    @Override
    public int read(int address) {
        return data[address];
    }
}
