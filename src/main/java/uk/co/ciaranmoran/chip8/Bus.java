package uk.co.ciaranmoran.chip8;

import java.util.HashMap;
import java.util.Map;

class Bus {
    private Map<Chip8.Device, BusItem> bus;

    Bus() {
        bus = new HashMap<>();
    }

    void addToBus(Chip8.Device device, BusItem item) {
        bus.put(device, item);
    }

    int read(Chip8.Device device, int address) {
        return bus.get(device).read(address);
    }

    void write(Chip8.Device device, int address, int data) {
        bus.get(device).write(address, data);
    }

}
