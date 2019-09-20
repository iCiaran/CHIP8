import sys
import struct

program = [line.split("-")[0].strip() for line in open(sys.argv[1]) if line[0] != '#']
program = [[int("0x" + instruction[:2], 16), int("0x" + instruction[2:], 16)] for instruction in program]
program = [j for i in program for j in i]

with open(sys.argv[1].split('.')[0] + ".rom", 'wb') as f:
    for value in program:
        f.write(struct.pack('B', value))
