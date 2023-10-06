package com.nn.osiris.ui;

import com.codingrodent.microprocessor.IBaseDevice;

// do nothing Z80 IO handler TODO

public class PZIO implements IBaseDevice {
    private int value;

    /**
     * Just return the latest value output
     */
    @Override
    public int IORead(int address) {
        return value;
    }

    /**
     * Print a character. Flush with any non printable
     */
    @Override
    public void IOWrite(int address, int data) {
        if (data < 32)
            System.out.println();
        else
            System.out.print((char) data);
        value = data;
    }
}
