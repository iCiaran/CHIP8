package uk.co.ciaranmoran.chip8;

public interface BusItem {

    void write(int address, int data);

    int read(int address);

}
