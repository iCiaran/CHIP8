package uk.co.ciaranmoran.chip8;

import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class Cpu implements BusItem {
    /*
     * Internal
     */
    private int[] r_S;
    private int[] r_V;
    private int r_I;
    private int r_PC;
    private int r_SP;
    private int r_DT;
    private int r_ST;

    private int fontStart;
    private long l_DT;
    private long l_ST;
    private final long PERIOD;

    private Random rand;

    private Bus bus;

    Cpu(Bus bus, int fontStart) {
        bus.addToBus(Chip8.Device.CPU, this);
        this.bus = bus;

        this.fontStart = fontStart;

        r_V = new int[0x10];
        r_S = new int[0x10];
        PERIOD = 1000 / 60;
        r_SP = -1;

        rand = new Random();
    }

    void clock(int n) {
        for (int i = 0; i < n; i++) {
            clock();
        }
    }

    private void clock() {
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
                        CLS();
                        break;
                    case 0xEE:
                        //00EE - RET -> Return from a subroutine
                        RET();
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
                CALL(nibbles[1] << 8 | lower);
                break;
            case 0x3:
                //3xkk - SE Vx, byte -> Skip next instruction if Vx = kk
                SEVB(nibbles[1], lower);
                break;
            case 0x4:
                //4xkk - SNE Vx, byte -> Skip next instruction if Vx != kk
                SNEVB(nibbles[1], lower);
                break;
            case 0x5:
                //5__X
                switch (nibbles[3]) {
                    case 0x0:
                        //5xy0 - SE Vx, Vy -> Skip next instruction if Vx = Vy
                        SEVV(nibbles[1], nibbles[2]);
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            case 0x6:
                //6xkk - LD Vx, byte -> Set Vx = kk
                LDVB(nibbles[1], lower);
                break;
            case 0x7:
                //7xkk - ADD Vx, byte -> Set Vx = Vx + kk
                ADDVB(nibbles[1], lower);
                break;
            case 0x8:
                //8__X
                switch (nibbles[3]) {
                    case 0x0:
                        //8xy0 - LD Vx, Vy -> Set Vx = Vy
                        LDVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x1:
                        //8xy1 - OR Vx, Vy -> Set Vx = Vx OR Vy
                        ORVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x2:
                        //8xy2 - AND Vx, Vy -> Set Vx = Vx AND Vy
                        ANDVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x3:
                        //8xy3 - XOR Vx, Vy -> Set Vx = Vx XOR Vy
                        XORVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x4:
                        //8xy4 - ADD Vx, Vy -> Set Vx = Vx + Vy, set VF = carry
                        ADDVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x5:
                        //8xy5 - SUB Vx, Vy -> Set Vx = Vx - Vy, set VF = NOT borrow
                        SUBVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x6:
                        //8xy6 - SHR Vx {, Vy} -> Set Vx = Vx SHR 1
                        SHRVV(nibbles[1], nibbles[2]);
                        break;
                    case 0x7:
                        //8xy7 - SUBN Vx, Vy -> Set Vx = Vy - Vx, set VF = NOT borrow
                        SUBNVV(nibbles[1], nibbles[2]);
                        break;
                    case 0xE:
                        //8xyE - SHL Vx {, Vy} -> Set Vx = Vx SHL 1.
                        SHLVV(nibbles[1], nibbles[2]);
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
                        SNEVV(nibbles[1], nibbles[2]);
                        break;
                    default:
                        notImplemented(nibbles, true);
                        break;
                }
                break;
            case 0xA:
                //Annn - LD I, addr -> Set I = nnn
                LDIN(nibbles[1] << 8 | lower);
                break;
            case 0xB:
                //Bnnn - JP V0, addr -> Jump to location nnn + V0
                JPN(nibbles[1] << 8 | lower);
                break;
            case 0xC:
                //Cxkk - RND Vx, byte -> Set Vx = random byte AND kk
                RND(nibbles[1], lower);
                break;
            case 0xD:
                //Dxyn - DRW Vx, Vy, nibble ->Display n-byte sprite starting
                //                            at memory location I at (Vx, Vy), set VF = collision
                DRW(nibbles[1], nibbles[2], nibbles[3]);
                break;
            case 0xE:
                //E_XX
                switch (lower) {
                    case 0x9E:
                        //Ex9E - SKP Vx -> Skip next instruction if key with the value of Vx is pressed
                        SKPV(nibbles[1]);
                        break;
                    case 0xA1:
                        //ExA1 - SKNP Vx -> Skip next instruction if key with the value of Vx is not pressed
                        SKNPV(nibbles[1]);
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
                        LDVD(nibbles[1]);
                        break;
                    case 0x0A:
                        //Fx0A - LD Vx, K -> Wait for a key press, store the value of the key in Vx
                        LDVK(nibbles[1]);
                        break;
                    case 0x15:
                        //Fx15 - LD DT, Vx -> Set delay timer = Vx
                        LDDV(nibbles[1]);
                        break;
                    case 0x18:
                        //Fx18 - LD ST, Vx -> Set sound timer = Vx
                        LDSV(nibbles[1]);
                        break;
                    case 0x1E:
                        //Fx1E - ADD I, Vx -> Set I = I + Vx
                        ADDIV(nibbles[1]);
                        break;
                    case 0x29:
                        //Fx29 - LD F, Vx -> Set I = location of sprite for digit Vx.
                        LDFV(nibbles[1]);
                        break;
                    case 0x33:
                        //Fx33 - LD B, Vx -> Store BCD representation of Vx in memory locations I, I+1, and I+2
                        LDBV(nibbles[1]);
                        break;
                    case 0x55:
                        //Fx55 - LD [I], Vx -> Store registers V0 through Vx in memory starting at location I
                        LDIV(nibbles[1]);
                        break;
                    case 0x65:
                        //Fx65 - LD Vx, [I] -> Read registers V0 through Vx from memory starting at location I
                        LDVI(nibbles[1]);
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


    private void CLS() {
        for (int i = 0; i < 64 * 32; i++) {
            bus.write(Chip8.Device.DISPLAY, i, 0x0);
        }
    }

    private void RET() {
        r_PC = r_S[r_SP];
        r_SP--;
    }

    /**
     * 1nnn - JP addr
     * Jump to location nnn.
     * <p>
     * The interpreter sets the program counter to nnn.
     *
     * @param nnn A 12-bit value, the lowest 12 bits of the instruction
     */
    private void JP(int nnn) {
        r_PC = nnn;
    }

    /**
     * 2nnn - CALL addr
     * Call subroutine at nnn.
     * <p>
     * The interpreter increments the stack pointer, then puts the current PC on the top of the stack.
     * The PC is then set to nnn.
     *
     * @param nnn A 12-bit value, the lowest 12 bits of the instruction
     */
    private void CALL(int nnn) {
        r_SP++;
        r_S[r_SP] = r_PC;
        r_PC = nnn;
    }

    /**
     * 3xkk - SE Vx, byte
     * Skip next instruction if Vx = kk.
     * <p>
     * The interpreter compares register Vx to kk, and if they are equal,
     * increments the program counter by 2.
     *
     * @param x  A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param kk An 8-bit value, the lowest 8 bits of the instruction
     */
    private void SEVB(int x, int kk) {
        if (r_V[x] == kk) {
            r_PC += 2;
        }
    }

    /**
     * 4xkk - SNE Vx, byte
     * Skip next instruction if Vx != kk.
     * <p>
     * The interpreter compares register Vx to kk, and if they are not equal,
     * increments the program counter by 2.
     *
     * @param x  A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param kk An 8-bit value, the lowest 8 bits of the instruction
     */
    private void SNEVB(int x, int kk) {
        if (r_V[x] != kk) {
            r_PC += 2;
        }
    }

    /**
     * 5xy0 - SE Vx, Vy
     * Skip next instruction if Vx = Vy.
     * <p>
     * The interpreter compares register Vx to register Vy, and if they are equal, increments the program counter by 2.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void SEVV(int x, int y) {
        if (r_V[x] == r_V[y]) {
            r_PC += 2;
        }
    }

    /**
     * 6xkk - LD Vx, byte
     * Set Vx = kk.
     * <p>
     * The interpreter puts the value kk into register Vx.
     *
     * @param x  A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param kk An 8-bit value, the lowest 8 bits of the instruction
     */
    private void LDVB(int x, int kk) {
        r_V[x] = kk;
    }

    /**
     * 7xkk - ADD Vx, byte
     * Set Vx = Vx + kk.
     * <p>
     * Adds the value kk to the value of register Vx, then stores the result in Vx.
     *
     * @param x  A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param kk An 8-bit value, the lowest 8 bits of the instruction
     */
    private void ADDVB(int x, int kk) {
        r_V[x] = (r_V[x] + kk) % 256;
    }

    /**
     * 8xy0 - LD Vx, Vy
     * Set Vx = Vy.
     * <p>
     * Stores the value of register Vy in register Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void LDVV(int x, int y) {
        r_V[x] = r_V[y];
    }

    /**
     * 8xy1 - OR Vx, Vy
     * Set Vx = Vx OR Vy.
     * <p>
     * Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx.
     * A bitwise OR compares the corrseponding bits from two values, and if either bit is 1,
     * then the same bit in the result is also 1. Otherwise, it is 0.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void ORVV(int x, int y) {
        r_V[x] = r_V[x] | r_V[y];
    }

    /**
     * 8xy2 - AND Vx, Vy
     * Set Vx = Vx AND Vy.
     * <p>
     * Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
     * A bitwise AND compares the corrseponding bits from two values, and if both bits are 1,
     * then the same bit in the result is also 1. Otherwise, it is 0.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void ANDVV(int x, int y) {
        r_V[x] = r_V[x] & r_V[y];
    }

    /**
     * 8xy3 - XOR Vx, Vy
     * Set Vx = Vx XOR Vy.
     * <p>
     * Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result in Vx.
     * An exclusive OR compares the corrseponding bits from two values, and if the bits are not both the same,
     * then the corresponding bit in the result is set to 1. Otherwise, it is 0.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void XORVV(int x, int y) {
        r_V[x] = r_V[x] ^ r_V[y];
    }

    /**
     * 8xy4 - ADD Vx, Vy
     * Set Vx = Vx + Vy, set VF = carry.
     * <p>
     * The values of Vx and Vy are added together.
     * If the result is greater than 8 bits (i.e., > 255,) VF is set to 1, otherwise 0.
     * Only the lowest 8 bits of the result are kept, and stored in Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void ADDVV(int x, int y) {
        int temp = r_V[x] + r_V[y];
        r_V[0xF] = temp > 0xFF ? 1 : 0;
        r_V[x] = temp % 256;
    }

    /**
     * 8xy5 - SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     * <p>
     * If Vx > Vy, then VF is set to 1, otherwise 0.
     * Then Vy is subtracted from Vx, and the results stored in Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void SUBVV(int x, int y) {
        r_V[0xF] = r_V[x] > r_V[y] ? 1 : 0;
        r_V[x] = (r_V[x] - r_V[y]) & 0xFF;
    }

    /**
     * 8xy6 - SHR Vx {, Vy}
     * Set Vx = Vy SHR 1, set VF = LSB of Vy
     * <p>
     * Store the value of register Vy shifted right one bit in register Vx
     * Set register VF to the least significant bit prior to the shift
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void SHRVV(int x, int y) {
        r_V[0xF] = r_V[y] & 0x1;
        r_V[x] = r_V[y] >> 1;
    }

    /**
     * 8xy7 - SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     * <p>
     * If Vx > Vy, then VF is set to 1, otherwise 0.
     * Then Vy is subtracted from Vx, and the results stored in Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void SUBNVV(int x, int y) {
        r_V[0xF] = r_V[y] > r_V[x] ? 1 : 0;
        r_V[x] = (r_V[y] - r_V[x]) & 0xFF;
    }

    /**
     * 8xyE - SHL Vx {, Vy}
     * Set Vx = Vy SHL 1, set VF = MSB of Vy
     * <p>
     * Store the value of register Vy shifted left one bit in register Vx
     * Set register VF to the most significant bit prior to the shift
     */
    private void SHLVV(int x, int y) {
        r_V[0xF] = (r_V[y] & 0x80) >> 7;
        r_V[x] = (r_V[y] << 1) & 0xFF;
    }

    /**
     * 9xy0 - SNE Vx, Vy
     * Skip next instruction if Vx != Vy.
     * <p>
     * The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     */
    private void SNEVV(int x, int y) {
        if (r_V[x] != r_V[y]) {
            r_PC += 2;
        }
    }

    /**
     * Annn - LD I, addr
     * Set I = nnn.
     * <p>
     * The value of register I is set to nnn.
     *
     * @param nnn A 12-bit value, the lowest 12 bits of the instruction
     */
    private void LDIN(int nnn) {
        r_I = nnn;
    }

    /**
     * Bnnn - JP V0, addr
     * Jump to location nnn + V0.
     * <p>
     * The program counter is set to nnn plus the value of V0.
     *
     * @param nnn A 12-bit value, the lowest 12 bits of the instruction
     */
    private void JPN(int nnn) {
        r_PC = r_V[0] + nnn;
    }

    /**
     * Cxkk - RND Vx, byte
     * Set Vx = random byte AND kk.
     * <p>
     * The interpreter generates a random number from 0 to 255, which is then ANDed with the value kk.
     * The results are stored in Vx.
     *
     * @param x  A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param kk An 8-bit value, the lowest 8 bits of the instruction
     */
    private void RND(int x, int kk) {
        r_V[x] = rand.nextInt(256) & kk;
    }

    /**
     * Dxyn - DRW Vx, Vy, nibble
     * Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
     * <p>
     * The interpreter reads n bytes from memory, starting at the address stored in I.
     * These bytes are then displayed as sprites on screen at coordinates (Vx, Vy).
     * Sprites are XORed onto the existing screen.
     * If this causes any pixels to be erased, VF is set to 1, otherwise it is set to 0.
     * If the sprite is positioned so part of it is outside the coordinates of the display,
     * it wraps around to the opposite side of the screen.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     * @param y A 4-bit value, the upper 4 bits of the low byte of the instruction
     * @param n A 4-bit value, the lowest 4 bits of the instruction
     */
    private void DRW(int x, int y, int n) {
        int sX = r_V[x];
        int sY = r_V[y];
        int erased = 0x0;

        if (sX < 64 && sY < 32) {
            for (int dY = 0; dY < n; dY++) {                                     // For line in sprite
                int line = bus.read(Chip8.Device.RAM, r_I + dY);        // Read line from RAM
                for (int dX = 7; dX >= 0; dX--) {                                 // For pixel in line
                    int pixel = line & 0x1;                                   // (right to left)
                    line = line >> 1;                                           //
                    int address = ((sY + dY) % 32) * 64 + ((sX + dX) % 64);     // Address after wrapping
                    int current = bus.read(Chip8.Device.DISPLAY, address);      // Read current value
                    int xor = pixel ^ current;                              // XOR sprite with screen
                    if (current == 0x1 && xor == 0x0) {                         // If a pixel is erased
                        erased = 0x1;                                           // set the erased flag
                    }                                                           // to 0x1.
                    bus.write(Chip8.Device.DISPLAY, address, xor);              // Write new value to screen
                }
            }
        }
        r_V[0xF] = erased;

        //System.out.println(bus.get(Chip8.Device.DISPLAY));
    }

    /**
     * Ex9E - SKP Vx
     * Skip next instruction if key with the value of Vx is pressed.
     * <p>
     * Checks the keyboard, and if the key corresponding to the value of Vx is currently in the down position,
     * PC is increased by 2.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void SKPV(int x) {
        if (r_V[x] < 16 && bus.read(Chip8.Device.INPUT, r_V[x]) == 0x1) {
            r_PC += 2;
        }
    }

    /**
     * ExA1 - SKNP Vx
     * Skip next instruction if key with the value of Vx is not pressed.
     * <p>
     * Checks the keyboard, and if the key corresponding to the value of Vx is currently in the up position,
     * PC is increased by 2.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void SKNPV(int x) {
        if (r_V[x] < 16 && bus.read(Chip8.Device.INPUT, r_V[x]) == 0x0) {
            r_PC += 2;
        }
    }

    /**
     * Fx07 - LD Vx, DT
     * Set Vx = delay timer value.
     * <p>
     * The value of DT is placed into Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDVD(int x) {
        r_V[x] = r_DT;
    }

    /**
     * Fx0A - LD Vx, K
     * Wait for a key press, store the value of the key in Vx.
     * <p>
     * All execution stops until a key is pressed, then the value of that key is stored in Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDVK(int x) {
        for (int i = 0; i < 16; i++) {
            if (bus.read(Chip8.Device.INPUT, i) == 0x1) {
                r_V[x] = i;
                return;
            }
        }
        r_PC -= 2;
    }

    /**
     * Fx15 - LD DT, Vx
     * Set delay timer = Vx.
     * <p>
     * DT is set equal to the value of Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDDV(int x) {
        r_DT = r_V[x];
    }

    /**
     * Fx18 - LD ST, Vx
     * Set sound timer = Vx.
     * <p>
     * ST is set equal to the value of Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDSV(int x) {
        r_ST = r_V[x];
    }

    /**
     * Fx1E - ADD I, Vx
     * Set I = I + Vx.
     * <p>
     * The values of I and Vx are added, and the results are stored in I.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void ADDIV(int x) {
        int temp = r_I + r_V[x];
        r_V[0xF] = temp > 0xFFF ? 1 : 0;
        r_I = temp % 4096;
    }

    /**
     * Fx29 - LD F, Vx
     * Set I = location of sprite for digit Vx.
     * <p>
     * The value of I is set to the location for the hexadecimal sprite corresponding to the value of Vx.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDFV(int x) {
        r_I = fontStart + (r_V[x] & 0xF) * 5;
    }

    /**
     * Fx33 - LD B, Vx
     * Store BCD representation of Vx in memory locations I, I+1, and I+2.
     * The interpreter takes the decimal value of Vx, and places the hundreds digit in memory at location in I,
     * the tens digit at location I+1, and the ones digit at location I+2.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDBV(int x) {
        bus.write(Chip8.Device.RAM, r_I, r_V[x] / 100);
        bus.write(Chip8.Device.RAM, r_I + 1, (r_V[x] / 10) % 10);
        bus.write(Chip8.Device.RAM, r_I + 2, r_V[x] % 10);
    }

    /**
     * Fx55 - LD [I], Vx
     * Store registers V0 through Vx in memory starting at location I.
     * <p>
     * The interpreter copies the values of registers V0 through Vx into memory, starting at the address in I.
     *
     * @param x A 4-bit value, the lower 4 bits of the high byte of the instruction
     */
    private void LDIV(int x) {
        for (int i = 0; i <= x; i++) {
            bus.write(Chip8.Device.RAM, r_I + i, r_V[i]);
        }
        r_I = r_I + x + 1;
    }

    /**
     * Fx65 - LD Vx, [I]
     * Read registers V0 through Vx from memory starting at location I.
     * <p>
     * The interpreter reads values from memory starting at location I into registers V0 through Vx.
     */
    private void LDVI(int x) {
        for (int i = 0; i <= x; i++) {
            r_V[i] = bus.read(Chip8.Device.RAM, r_I + i);
        }
        r_I = r_I + x + 1;
    }

}
