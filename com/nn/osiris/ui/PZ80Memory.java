package com.nn.osiris.ui;

import com.codingrodent.microprocessor.IMemory;

// Represents the full 64K of Z80 memory

public class PZ80Memory implements IMemory {
	
    public final int[] memory = new int[65536];
	

    public PZ80Memory() {
    	
    }
    
    @Override
    // Read a byte from memory
    public int readByte(int address) {
        //     System.out.println("read " + Utilities.getByte(memory[address]) + " @ " + Utilities.getWord(address));
        return memory[address];
    }

    @Override
    // Read a word from memory
    public int readWord(int address) {
        return readByte(address) + readByte(address + 1) * 256;
    }

    @Override
    public void writeByte(int address, int data) {

        //     System.out.println("write " + Utilities.getByte(data) + " @ " + Utilities.getWord(address));
        memory[address] = data;
    }

    @Override
    public void writeWord(int address, int data) {
        writeByte(address, (data & 0x00FF));
        address = (address + 1) & 65535;
        data = (data >>> 8);
        writeByte(address, data);
    }
    
    public void ClearRam()
    {
    	for (int i = 0 ; i < 65536 ; i++)
    	{
    		memory[i] = 0;
    	}
    }
    
}
