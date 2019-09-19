package uk.co.ciaranmoran.chip8;

public class Display implements BusItem {

    private boolean[] data;
    private int width;
    private int height;

    Display(Bus bus, int width, int height) {
        this.width = width;
        this.height = height;

        bus.addToBus(Chip8.Device.DISPLAY, this);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(data[y * width + x] ? '#' : '.');
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
