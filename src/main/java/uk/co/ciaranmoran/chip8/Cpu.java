package uk.co.ciaranmoran.chip8;

public class Cpu implements BusItem {
    /*
     * Internal
     */
    private int[] stack;
    private int[] registers;
    private int r_I;
    private int r_PC;
    private int r_SP;
    private int r_DT;
    private int r_ST;

    private long l_DT;
    private long l_ST;
    private final long PERIOD;

    private Bus bus;

    Cpu(Bus bus) {
        bus.addToBus(Chip8.Device.CPU, this);
        this.bus = bus;

        registers = new int[0xF];
        stack = new int[0xF];
        PERIOD = 1000 / 60;
    }

    void clock(int n) {
        for(int i = 0; i < n; i++) {
            clock();
        }
    }

    void clock() {
        tickTimers();
    }

    private void tickTimers() {
        long now = System.currentTimeMillis();
        if (r_DT > 0 && (now - l_DT) > PERIOD) {
            r_DT--;
            l_DT = now;
        }
        if (r_ST > 0 && (now - l_ST) > PERIOD) {
            r_ST--;
            l_ST = now;
        }
    }

    void setR_PC(int address) {
        r_PC = address;
    }

    int getR_PC() {
        return r_PC;
    }


    @Override
    public void write(int address, int data) {

    }

    @Override
    public int read(int address) {
        return 0;
    }
}
