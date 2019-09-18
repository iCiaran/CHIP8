package uk.co.ciaranmoran.chip8;

import java.util.StringJoiner;
import java.util.stream.IntStream;

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
        execute();
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

    private void execute() {
        int upper = bus.read(Chip8.Device.RAM, r_PC++);
        int lower = bus.read(Chip8.Device.RAM, r_PC++);

        int[] nibbles = {(upper & 0xF0) >> 4, upper & 0xF, (lower & 0xF0) >> 4, lower & 0xF};

        switch (nibbles[0]) {
            case 0x0:
                //0_XX
                switch (lower) {
                    case 0xE0:
                        //00E0 - CLS -> Clear the display
                        break;
                    case 0xEE:
                        //00EE - RET -> Return from a subroutine
                        break;
                    default:
                        notImplemented(nibbles, false);
                        break;
                }
                break;
            case 0x1:
                //1nnn - JP addr -> Jump to location nnn
                JP(nibbles[1] << 8 | lower);
                break;
            case 0x2:
                //2nnn - CALL addr -> Call subroutine at nnn
                break;
            case 0x3:
                //3xkk - SE Vx, byte -> Skip next instruction if Vx = kk
                break;
            case 0x4:
                //4xkk - SNE Vx, byte -> Skip next instruction if Vx != kk
                break;
            case 0x5:
                //5__X
                switch (nibbles[3]) {
                    case 0x0:
                        //5xy0 - SE Vx, Vy -> Skip next instruction if Vx = Vy
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            case 0x6:
                //6xkk - LD Vx, byte -> Set Vx = kk
                break;
            case 0x7:
                //7xkk - ADD Vx, byte -> Set Vx = Vx + kk
                break;
            case 0x8:
                //8__X
                switch (nibbles[3]) {
                    case 0x0:
                        //8xy0 - LD Vx, Vy -> Set Vx = Vy
                        break;
                    case 0x1:
                        //8xy1 - OR Vx, Vy -> Set Vx = Vx OR Vy
                        break;
                    case 0x2:
                        //8xy2 - AND Vx, Vy -> Set Vx = Vx AND Vy
                        break;
                    case 0x3:
                        //8xy3 - XOR Vx, Vy -> Set Vx = Vx XOR Vy
                        break;
                    case 0x4:
                        //8xy4 - ADD Vx, Vy -> Set Vx = Vx + Vy, set VF = carry
                        break;
                    case 0x5:
                        //8xy5 - SUB Vx, Vy -> Set Vx = Vx - Vy, set VF = NOT borrow
                        break;
                    case 0x6:
                        //8xy6 - SHR Vx {, Vy} -> Set Vx = Vx SHR 1
                        break;
                    case 0x7:
                        //8xy7 - SUBN Vx, Vy -> Set Vx = Vy - Vx, set VF = NOT borrow
                        break;
                    case 0xE:
                        //8xyE - SHL Vx {, Vy} -> Set Vx = Vx SHL 1.
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            case 0x9:
                //9__X
                switch (nibbles[3]) {
                    case 0x0:
                        //9xy0 - SNE Vx, Vy -> Skip next instruction if Vx != Vy
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            case 0xA:
                //Annn - LD I, addr -> Set I = nnn
                break;
            case 0xB:
                //Bnnn - JP V0, addr -> Jump to location nnn + V0
                break;
            case 0xC:
                //Cxkk - RND Vx, byte -> Set Vx = random byte AND kk
                break;
            case 0xD:
                //Dxyn - DRW Vx, Vy, nibble ->Display n-byte sprite starting
                //                            at memory location I at (Vx, Vy), set VF = collision
                break;
            case 0xE:
                //E_XX
                switch (lower) {
                    case 0x9E:
                        //Ex9E - SKP Vx -> Skip next instruction if key with the value of Vx is pressed
                        break;
                    case 0xA1:
                        //ExA1 - SKNP Vx -> Skip next instruction if key with the value of Vx is not pressed
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            case 0xF:
                //F_XX
                switch (lower) {
                    case 0x07:
                        //Fx07 - LD Vx, DT -> Set Vx = delay timer value
                        break;
                    case 0x0A:
                        //Fx0A - LD Vx, K -> Wait for a key press, store the value of the key in Vx
                        break;
                    case 0x15:
                        //Fx15 - LD DT, Vx -> Set delay timer = Vx
                        break;
                    case 0x18:
                        //Fx18 - LD ST, Vx -> Set sound timer = Vx
                        break;
                    case 0x1E:
                        //Fx1E - ADD I, Vx -> Set I = I + Vx
                        break;
                    case 0x29:
                        //Fx29 - LD F, Vx -> Set I = location of sprite for digit Vx.
                        break;
                    case 0x33:
                        //Fx33 - LD B, Vx ->Store BCD representation of Vx in memory locations I, I+1, and I+2
                        break;
                    case 0x55:
                        //Fx55 - LD [I], Vx -> Store registers V0 through Vx in memory starting at location I
                        break;
                    case 0x65:
                        //Fx65 - LD Vx, [I] -> Read registers V0 through Vx from memory starting at location I
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            default:
                notImplemented(nibbles, true);
                break;
        }
    }

    @Override
    public void write(int address, int data) {

    }

    @Override
    public int read(int address) {
        return 0;
    }

    private void notImplemented(int[] instruction, boolean stop) {
        StringJoiner sj = new StringJoiner("");
        IntStream.of(instruction).forEach(x -> sj.add(Integer.toHexString(x)));
        System.err.println("Instruction not implemented -> " + sj.toString());

        if (stop) System.exit(0);
    }

    private void JP(int addr) {
        r_PC = addr;
    }
}
