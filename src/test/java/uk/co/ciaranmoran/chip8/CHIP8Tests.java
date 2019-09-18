package uk.co.ciaranmoran.chip8;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CHIP8Tests {

    @ParameterizedTest
    @CsvSource({
            "true,         0x600",
            "false,        0x200"
    })
    void InitialPCValue(boolean ETI660, int start) {
        Chip8 chip8 = new Chip8();
        chip8.loadProgram("", ETI660);
        assertEquals(chip8.cpu.getR_PC(), start);
    }

}
