package uk.co.ciaranmoran.chip8;

public interface Connectable {

    void write(int address, int data);

    int read(int address);

}
