package com.nn.osiris.ui;

import com.codingrodent.microprocessor.Z80.*;
import com.codingrodent.microprocessor.Z80.CPUConstants.*;

public class PZ80Cpu {
    public Z80Core z80;
    public PZMemory z80Memory;
    public PZIO z80IO;
    public LevelOneParser parser;
    
    public int m_mtPLevel;
    
    int currentX;
    int currentY;
    
    public PZ80Cpu(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZMemory();
    	z80IO  = new PZIO();
    	z80 = new Z80Core(z80Memory, z80IO);
    	//z80.reset();
    }

    public void run(int address) { //
        // Ok, run the program
        z80.setProgramCounter(address);
        boolean test = z80.getHalt();
        while (!test) {
            try {
                //System.out.println("----------------------------------------------------------Z80 Running... PC=0x"+Utilities.getWord(z80.getRegisterValue(RegisterNames.PC)));
                int pc = z80.getProgramCounter();	// Check if PC is calling resident
                if ( pc > (PortalConsts.R_MAIN -1) && pc < (PortalConsts.R_DUMMY3 +1))
                {
                	// need to process resident calls here.
                	int result = Resident(pc);
                	
                	if (result == 1)
                		z80.ret();
                	else if (result == 0)
                		continue;
                	else if (result == 2)
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
    	
        // find mtutor release level
        // unfortunately cdc put it in different places in different
        // releases - but only off by 1 byte.
        int release = z80Memory.readByte(0x530b);  // release or year *y1*
        if (release == 8)           // year *y1* 82 - 8?
            release = z80Memory.readByte(0x530a);  // release
        m_mtPLevel = release;

        switch (m_mtPLevel)
        {
        case 2: break; // PatchL2 (); break;
        case 3: break; // PatchL3 (); break;
        case 4: PatchL4 (); break;
        case 5: break; // PatchL5 (); break;
        case 6: break; // PatchL6 (); break;
        default: break;
        }
    	
    	run(address);
    }
    
    void PatchL4 ()
    {
    	
    	System.out.println("----------------PatchL4");
    	
        // Call resident and wxWidgets for brief pause
    	z80Memory.writeByte(PortalConsts.Level4Pause, PortalConsts.CALL8080);
    	z80Memory.writeByte(PortalConsts.Level4Pause + 1, PortalConsts.R_WAIT16);
    	z80Memory.writeByte(PortalConsts.Level4Pause + 2, 0);
        

        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
        //RAM[Level4Xplato] = 0;
        //RAM[Level4Xplato + 1] = 0;

        //RAM[0x5f5c] = RET8080;  // ret to disable ist-3 screen print gunk

//        PatchColor (0xe5);
    }
    
    public int Parity(int x)
    {
    	
    	boolean p = CPUConstants.PARITY_TABLE[x];
    	
    	if (p)
    		return x;
    	else
    		return x + 0x80;
    }
    
    
    
    
 // This emulates the "ROM resident".  Return values:
 // 0: PC is not special (not in resident), proceed normally.
 // 1: PC is ROM function entry point, it has been emulated,
//     do a RET now.
 // 2: PC is either the z80 emulation exit magic value, or R_INIT,
//     or an invalid resident value.  Exit z80 emulation.

    private int Resident(int val)
    {
    	
        int x, y, cp, c, x2, y2;

        
    	//System.out.print("----------------------------------------------------------Calling resident... ");

    	
    	switch(val)
    	{
    	
    	case PortalConsts.R_MAIN:
        	//System.out.println("R_MAIN");
    		return 2;
    	
    	case PortalConsts.R_INIT:
        	//System.out.println("R_INIT");
    		return 2;
    		
    	case PortalConsts.R_DOT:
        	//System.out.println("R_DOT");
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);
        	
        	parser.PlotLine(x, y, x, y, 1, 0, 0, 1, 0);
           	
            currentX = x;
            currentY = y;        	
        	
    		return 1;    		
    	
    	case PortalConsts.R_LINE:
        	//System.out.print("R_LINE");
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);
        	//System.out.println(": (" + x+ ","+ y + ")" );
        	
        	parser.PlotLine(currentX, currentY, x, y, 1, 0, 0, 1, 0);
        	
            currentX = x;
            currentY = y;
            
    		return 1;
    		
    	case PortalConsts.R_CHARS:
        	//System.out.println("R_CHARS");
        	int cpointer = z80.getRegisterValue(RegisterNames.HL);
        	byte[] cbuf =  new byte[500];
        	
        	int chr = z80Memory.readByte(cpointer++);
        	int lth = 0;
        	
            for (;;)
            {

                int save = z80Memory.readByte(PortalConsts.M_CCR);
                int charM = (z80Memory.readByte(PortalConsts.M_CCR) & 0x0e) >> 1; // Current M slot

                if (chr > 0x3F )
                {
                    // advance M slot by one
                	z80Memory.writeByte(PortalConsts.M_CCR,(z80Memory.readByte(PortalConsts.M_CCR) & ~0x0e) | (charM + 1) << 1);
                }

                cbuf[lth++] = ((byte)(chr & 0x3f));

                if (chr > 0x3F )
                {
                    // restore M slot
                	z80Memory.writeByte(PortalConsts.M_CCR, save);
                }

                chr = z80Memory.readByte(cpointer++);

            	boolean p1 = (chr == 0x3f);
            	boolean p2 = (z80Memory.readByte(cpointer) == 0);
                if (p1 && p2)
                {
                	int wrMode = (z80Memory.readByte(PortalConsts.M_MODE)) & 0x3;
                	
                	int fgcolor = 0xffffff | (255 << 24);
                	int bgcolor = 0 | (255 << 24);
                	
                	//parser.AlphaDataM(cbuf);
                
                	parser.drawString(cbuf, lth, fgcolor, bgcolor, currentX, 512-currentY, wrMode, 1, 0, 0);
                	currentX += 8;
                	
                	//parser.FlushText();
                    break;
                }

            }
                        
    		return 1;

    	case PortalConsts.R_MODE:
    		
    		int mode = z80.getRegisterValue(RegisterNames.HL) & 0xff;
    		
    		z80Memory.writeByte(PortalConsts.M_MODE, mode >> 1);
    		
    		if ( (mode & 1) == 1)
    			mode = mode;		// TODO erase screen
    		
    		return 1;

    	case PortalConsts.R_DIR:
    		z80Memory.writeByte(PortalConsts.M_DIR, z80.getRegisterValue(RegisterNames.HL) & 3);
    		return 1;
    		
    	case PortalConsts.R_STEPX:
    		int sdir = z80Memory.readByte(PortalConsts.M_DIR) & 3;
    		if ((sdir & 2)==0)
    			currentX++;
    		else
    			currentX--;
    		
    		return 1;

    	case PortalConsts.R_STEPY:
    		int sdir2 = z80Memory.readByte(PortalConsts.M_DIR) & 3;
    		if ((sdir2 & 1)==0)
    			currentY++;
    		else
    			currentY--;
    		
    		return 1;
    		
    	case PortalConsts.R_WE:
    		parser.PlotLine(currentX, currentY, currentX, currentY, 1, 0, 0, 1, 0);
    		
    		return 1;
    		
    		
    	case PortalConsts.R_EXEC:
    		
    		// TODO
    		
    		return 1;

    		
    	case PortalConsts.R_INPX:
        	//System.out.println("R_INPX");
        	z80.setRegisterValue(RegisterNames.HL, currentX);
        	
        	return 1;
    		
    	case PortalConsts.R_INPY:
        	//System.out.println("R_INPY");
        	z80.setRegisterValue(RegisterNames.HL, currentY);
        	
        	return 1;
    		
    	case PortalConsts.R_OUTX:
        	//System.out.println("R_OUTX");
        	currentX = z80.getRegisterValue(RegisterNames.HL);
        	
        	return 1;
        	
    	case PortalConsts.R_OUTY:
        	//System.out.println("R_OUTY");
        	currentY = z80.getRegisterValue(RegisterNames.HL);
        	
        	return 1;

    	case PortalConsts.R_XMIT: 
        	{
            int k = z80.getRegisterValue(RegisterNames.HL);
        	//System.out.println("R_XMIT 0x" + String.format("%x", k));

            //int temp_hold = mt_ksw;
            //if (k != 0x3a)
            //    mt_ksw = 0;
           
            
        	parser.SendRawKey(0x1b);
        	
        	x = Parity(0x40+(k & 0x3f));
        	parser.SendRawKey(x);
        	x = Parity(0x60+(k >> 6));
        	parser.SendRawKey(x);
        	
        	//mt_ksw = temp_hold;   
    		
    		return 1;
        	}
        	
    	case PortalConsts.R_CCR:
        	//System.out.println("R_CCR");
        	int ccr_val = z80.getRegisterValue(RegisterNames.HL);
        	z80Memory.writeWord(PortalConsts.M_CCR, ccr_val);
        	
        	// TODO set flags
        	
        	return 1;
        	
    	case PortalConsts.R_INPUT:
    		
    		z80.setRegisterValue(RegisterNames.HL, -1 & 0xffff); // TMP TODO
    		
    		return 1;
        	
    	case PortalConsts.R_FCOLOR:
    		//System.out.println("R_FCOLOR"); // TODO
    		return 1;
    		
    	case PortalConsts.R_FCOLOR+1:
    		//System.out.println("R_FCOLOR+1"); // TODO
    		return 1;
    	
    	case PortalConsts.R_FCOLOR+2:
    		//System.out.println("R_FCOLOR+2"); // TODO
    		return 1;
    	
    	
    	case PortalConsts.R_WAIT16:
    		
    		// TODO
    		
    	return 1;
    	
    	case PortalConsts.R_SSF:
    		
    		// TODO
    		System.out.println("------------------------NOT handled R_SSF");
    		return 2;

    	default: 
        	System.out.println("------------------------NOT handled 0x" + String.format("%x", val));
        	return 2;
        	
    	}
    	
    	//return 2;
    }

}