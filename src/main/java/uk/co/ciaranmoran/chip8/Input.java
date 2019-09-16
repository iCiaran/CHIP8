package uk.co.ciaranmoran.chip8;

public class Input implements Connectable{

    public Input(Bus bus) {
        bus.addToBus(Chip8.Device.INPUT, this);
    }

    @Override
    public void write(int address, int data) {

    }

    @Override
    public int read(int address) {
        return 0;
    }
}
