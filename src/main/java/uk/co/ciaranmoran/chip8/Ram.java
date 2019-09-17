package uk.co.ciaranmoran.chip8;

public class Ram implements BusItem {

    private int[] data;

    Ram(Bus bus) {
        bus.addToBus(Chip8.Device.RAM, this);
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
