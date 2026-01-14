package com.example.transactions.enums;

public enum Currency {
    RON((byte) 1),
    EUR((byte) 5),
    USD((byte) 4);

    private final byte value;

    Currency(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
