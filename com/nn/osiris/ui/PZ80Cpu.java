package com.nn.osiris.ui;

import com.codingrodent.microprocessor.Z80.*;
import com.codingrodent.microprocessor.Z80.CPUConstants.*;

public class PZ80Cpu {
    public Z80Core z80;
    public PZMemory z80Memory;
    public PZIO z80IO;
    public LevelOneParser parser
;	
    public PZ80Cpu(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZMemory();
    	z80IO  = new PZIO();
    	z80 = new Z80Core(z80Memory, z80IO);
    	z80.reset();
    }

    public void run(int address) { //
        // Ok, run the program
        z80.setProgramCounter(address);
        boolean test = z80.getHalt();
        while (!test) {
            try {
                System.out.println("----------------------------------------------------------Z80 Running... PC=0x"+Utilities.getWord(z80.getRegisterValue(RegisterNames.PC)));
                int pc = z80.getProgramCounter();	// Check if PC is calling resident
                if ( pc > (PortalConsts.R_MAIN -1) && pc < (PortalConsts.R_DUMMY3 +1))
                {
                	// need to process resident calls here.
                	int result = Resident(pc);
                	
                	break;
                	
                	//z80Memory.writeByte(pc, 0x76);
                }
                z80.executeOneInstruction();
                
                
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
    
    
    private int Resident(int val)
    {
    	System.out.print("----------------------------------------------------------Calling resident... ");

    	
    	switch(val)
    	{
    	
    	case PortalConsts.R_XMIT: 
        	{
            int k = z80.getRegisterValue(RegisterNames.HL);
        	System.out.println("R_XMIT 0x" + String.format("%x", k));

            //int temp_hold = mt_ksw;
            //if (k != 0x3a)
            //    mt_ksw = 0;
            parser.SendKeyDelay(k, 5);
            //mt_ksw = temp_hold;   
    		
    		return 1;
        	}
    	default: 
        	System.out.println("------------------------NOT handled 0x" + String.format("%x", val));
    	}
    	
    	return 0;
    }

}