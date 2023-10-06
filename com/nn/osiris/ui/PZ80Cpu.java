package com.nn.osiris.ui;

import com.codingrodent.microprocessor.Z80.*;
import com.codingrodent.microprocessor.Z80.CPUConstants.*;

public class PZ80Cpu {
    public Z80Core z80;
    public PZMemory z80Memory;
	
    public PZ80Cpu()
    {
    	z80Memory = new PZMemory();
    	z80 = new Z80Core(z80Memory, new PZIO());
    	z80.reset();
    }

    public void run(int address) { //
        // Ok, run the program
        z80.setProgramCounter(address);
        while (!z80.getHalt()) {
            try {
                System.out.println(Utilities.getWord(z80.getRegisterValue(RegisterNames.PC)));
                z80.executeOneInstruction();
                
                int pc = z80.getProgramCounter();	// Check if PC is calling resident
                if ( pc > (PortalConsts.R_MAIN -1) && pc < (PortalConsts.R_DUMMY3 +1))
                {
                	// need to process resident calls here.
                }
                
            } catch (Exception e) {
                System.out.println("Z80 Hardware crash, oops! " + e.getMessage());
            }
        }
    }
    
    
    public void runWithMtutorCheck(int address)
    {
    	// do checks here for patching the levels of MTUTOR -  then run
    	
    	
    	run(address);
    }

}
