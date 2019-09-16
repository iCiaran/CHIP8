package uk.co.ciaranmoran.chip8;

import java.util.ArrayList;

public class Cpu {
    /*
     * Internal
     */
    int[] stack;
    int[] registers;
    int r_I;
    int r_PC;
    int r_DT;
    int r_ST;
    int r_SP;

    Bus bus;

    Cpu(Bus bus) {
        this.bus = bus;

        registers = new int[0xF];
        stack = new int[0xF];
    }

    public void clock(int n) {
        for(int i = 0; i < n; i++) {
            clock();
        }
    }

    public void clock(){

    }

    public void setR_PC(int address) {
        r_PC = address;
    }


}
