package com.nn.osiris.ui;

import java.awt.Color;
//import java.util.*;

import com.codingrodent.microprocessor.Z80.*;
import com.codingrodent.microprocessor.Z80.CPUConstants.*;

public class PZ80Cpu extends Thread {
    public Z80Core z80;
    public PZMemory z80Memory;
    public PZIO z80IO;
    public LevelOneParser parser;
    
    public int m_mtPLevel;
    
    public boolean stopme;
    
    public	int threadsCopied;
    
    private int runs;
    
    public PZ80Cpu(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZMemory();
    	z80IO  = new PZIO();
    	z80 = new Z80Core(z80Memory, z80IO);
    	z80.reset();
    	threadsCopied = 0;
    	runs = 0;
    }
 
    public PZ80Cpu()
    {

    }
    
    public PZ80Cpu( PZ80Cpu base)
    {
    	parser = base.parser;
    	z80Memory = base.z80Memory;
    	z80IO = base.z80IO;
    	z80 = base.z80;
    	stopme = false;
    	m_mtPLevel = base.m_mtPLevel;
    	runs = base.runs;
    	
    	threadsCopied = base.threadsCopied + 1;
    	
    }
    
    
    public Z80Core Init(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZMemory();
    	z80IO  = new PZIO();
    	z80 = new Z80Core(z80Memory, z80IO);
    			
    	return z80;
    }
    
    
    public void run() { //
        // Ok, run the program

        int pc;
        long tstates = z80.getTStates();
        pc = z80.getProgramCounter();
        z80.resetTStates();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 program running...Starting PC= "+String.format("%x", pc));

        boolean test = z80.getHalt();
        stopme = false;
        
	        while (true) {
	            try {
	                //System.out.println("------------------------ Z80 Running... PC=0x"+Utilities.getWord(z80.getRegisterValue(RegisterNames.PC)));
	                pc = z80.getProgramCounter();	// Check if PC is calling resident
	                if ( pc > (PortalConsts.R_MAIN -1) && pc < (PortalConsts.R_DUMMY3 +1) && !stopme)
	                {
	                	// need to process resident calls here.
	                	int result = Resident(pc);
	                	
	                	if (result == 1)
	                		z80.ret();
	                	else if (result == 0)
	                		continue;
	                	else if (result == 2)
	                		stopme = true;
	                	
	                }
	                if (!test && !stopme)
	                	z80.executeOneInstruction();
	                else
	                	
	                	// for threading in stead of a break we should be in a sleep loop here until stopme is false again
	                	
	                	if (PortalConsts.is_threaded)
	                	{
	                		if (stopme)
	                		{
	                	        tstates = z80.getTStates();
	                	        z80.resetTStates();
	                	        System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 thread waiting... waiting PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	                	    	//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>===========Threads copied: "+threadsCopied);

	                		}
	                		while (stopme)
	                		{
	                			sleep(50);
	                		}
	            	        //long tstates = z80.getTStates();
	            	        z80.resetTStates();
	            	        System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 thread continuing...PC= "+String.format("%x", pc));
	            	    	//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>===========Threads copied: "+threadsCopied);
	                		continue;
	                	}
	                	else	
	                		break;
	                
	                test = z80.getHalt();
	                
	                
	            } catch (Exception e) {
	                System.out.println("Z80 Hardware crash, oops! " + e.getMessage());
	            }
	        }
	        tstates = z80.getTStates();
	        z80.resetTStates();
	        System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 program stopped...End PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	    	//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>===========Threads copied: "+threadsCopied);

    }
    
    
    private boolean has_been_started = false;
    
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
        
        runs++;
        
        stopme = true;
        
        switch (m_mtPLevel)
        {
        case 2: break; // PatchL2 (); break;
        case 3: break; // PatchL3 (); break;
        case 4: PatchL4 (); break;
        case 5: break; // PatchL5 (); break;
        case 6: break; // PatchL6 (); break;
        default: break;
        }
        
        z80.setResetAddress(address);
        z80.setProgramCounter(address);
    	 
		if (PortalConsts.is_threaded && !has_been_started)
		{
			has_been_started = true;
			start();
		}
		else if (PortalConsts.is_threaded && has_been_started)
			stopme = false;
		else
	        run();
    }
    
    void PatchL4 ()
    {
    	
    	//System.out.println("----------------PatchL4");
    	
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

        
        int cx = parser.center_x;
        
        int sMode = z80Memory.readByte(PortalConsts.M_MODE) & 3;
        
    	//System.out.print("----------------------------------------------------------Calling resident... ");
        //System.out.println("  ---------------------------=============================== >>>>>>>>>>> WAIT CALL " + String.format("%x", z80Memory.readWord(PortalConsts.Level4Pause)));
        
/*    	
        if (R_CHARS_Inprogress && val != PortalConsts.R_CHARS)
        {
        	parser.FlushText();
        	R_CHARS_Inprogress = false;
        }
*/       
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
        	
        	parser.PlotLine(x+cx, y, x+cx, y, sMode, 0, 0, 1, 0);
           	
            parser.current_x = x;
            parser.current_y = y;        	
        	
    		return 1;    		
    	
    	case PortalConsts.R_LINE:
        	//System.out.print("R_LINE");
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);
        	//System.out.println(": (" + x+ ","+ y + ")" );
        	
        	parser.PlotLine(parser.current_x+cx, parser.current_y, x+cx, y,  sMode, 0, 0, 1, 0);
        	
            parser.current_x = x;
            parser.current_y = y;
            
    		return 1;
    		
    	case PortalConsts.R_CHARS:
        	int cpointer = z80.getRegisterValue(RegisterNames.HL);
        	byte[] cbuf =  new byte[500];	// never seen more than one char at a time from mtutor
        	
        	int chr = z80Memory.readByte(cpointer++);
        	int lth = 0;
        	int charM;
        	
            for (;;)
            {
            	if (chr == 0x3f && 0 == z80Memory.readByte(cpointer))
            	{
            		break;
            	}
            	
                int save = z80Memory.readByte(PortalConsts.M_CCR);
                int pv;
                charM = (save & 0x0e) >> 1; // Current M slot

                if (chr > 0x3F )
                {
                    // advance M slot by one
                	z80Memory.writeByte(PortalConsts.M_CCR,(z80Memory.readByte(PortalConsts.M_CCR) & ~0x0e) | (charM + 1) << 1);
                    charM = (z80Memory.readByte(PortalConsts.M_CCR) & 0x0e) >> 1; // Current M slot
                }

                cbuf[lth++] = (byte)(chr & 0x3f);      	//((byte)Sixbit.sixBitCharToAscii(chr & 0x3f, charM > 0));
                pv = cbuf[0];							// for testing value

                int chr0 = chr;
                chr = z80Memory.readByte(cpointer++);
                
                
            	boolean p1 = (chr == 0x3f);
            	boolean p2 = (z80Memory.readByte(cpointer) == 0);
                if (p1 && p2)
                {
     // Crude char code converter
                	
                	parser.text_charset = 1;	// assume lower case alpha
                	
                	if (charM > 0  && pv < 27)				// upper case
                	{
                		parser.text_charset = 0;
                		cbuf[0] += 64;			// upper case alpha
                	}
                	else if (charM  == 0 )
                	{
                		byte cv = convert0[pv];
                		
                		cbuf[0] = cv;
                		if (pv != cv)
                			parser.text_charset = 0;
                	}
                	
                    switch(cbuf[0])
                    {
                    case 0x2d:				// space
                    	cbuf[0] = 0x20;
                    	break;
                    	
                    	default: break;
                    }

     // end char converter
                    
                	parser.AlphaDataM(cbuf);
                	parser.FlushText();
                	
                	//parser.drawString(cbuf, lth, fgcolor, bgcolor, parser.current_x, 512-parser.current_y, wrMode, 1, 0, 0);
                
                	if (chr0 > 0x3F )
                    {
                        // restore M slot
                    	z80Memory.writeByte(PortalConsts.M_CCR, save);
                    }
                                  	
                }

            }
                        
    		return 1;

    	case PortalConsts.R_MODE:
    		
    		int mode = z80.getRegisterValue(RegisterNames.L) & 0xff;
    		
    		z80Memory.writeByte(PortalConsts.M_MODE, (mode & 0x1f ) >> 1);
    		
    		switch ((mode >> 1) & 3)
    		{
    		case 0: parser.screen_mode = LevelOneParser.SCINVERSE; break;
    		case 1: parser.screen_mode = LevelOneParser.SCREWRITE; break;
    		case 2: parser.screen_mode = LevelOneParser.SCERASE; break;
    		case 3: parser.screen_mode = LevelOneParser.SCWRITE; break;
    		}
    		
    		if ((mode & 1) == 1)
    		{
    			parser.clearScreen();
    		}
    		
    		return 1;

    	case PortalConsts.R_DIR:
    		int zhl = z80.getRegisterValue(RegisterNames.HL);
    		z80Memory.writeByte(PortalConsts.M_DIR, zhl & 3);
    		return 1;
    		
    	case PortalConsts.R_STEPX:
    		int sdir = z80Memory.readByte(PortalConsts.M_DIR) & 3;
    		if ((sdir & 2)==0)
    			parser.current_x++;
    		else
    			parser.current_x--;
    		
    		return 1;

    	case PortalConsts.R_STEPY:
    		int sdir2 = z80Memory.readByte(PortalConsts.M_DIR) & 3;
    		if ((sdir2 & 1)==0)
    			parser.current_y++;
    		else
    			parser.current_y--;
    		
    		return 1;
    		
    	case PortalConsts.R_WE:
    		parser.PlotLine(parser.current_x+cx, parser.current_y, parser.current_x+cx, parser.current_y, 1, 0, 0, 1, 0);
    		
    		return 1;
    		
    		
    	case PortalConsts.R_EXEC:
    		
    		// TODO
    		
    		return 1;
    		
    	case PortalConsts.R_INPX:
        	//System.out.println("R_INPX");
        	z80.setRegisterValue(RegisterNames.HL, parser.current_x);
        	
        	return 1;
    		
    	case PortalConsts.R_INPY:
        	//System.out.println("R_INPY");
        	z80.setRegisterValue(RegisterNames.HL, parser.current_y);
        	
        	return 1;
    		
    	case PortalConsts.R_OUTX:
        	//System.out.println("R_OUTX");
        	parser.current_x = z80.getRegisterValue(RegisterNames.HL);
        	
        	return 1;
        	
    	case PortalConsts.R_OUTY:
        	//System.out.println("R_OUTY");
        	parser.current_y = z80.getRegisterValue(RegisterNames.HL);
        	
        	return 1;

    	case PortalConsts.R_XMIT: 
        	{
            int k = z80.getRegisterValue(RegisterNames.HL);

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
        	int ccr_val = z80.getRegisterValue(RegisterNames.L) & 0xff;
        	z80Memory.writeByte(PortalConsts.M_CCR, ccr_val);
        	
        	parser.text_size = (byte)((ccr_val >> 5) & 1);

        	return 1;
        	
    	case PortalConsts.R_INPUT:
    		//System.out.println("----------------------- R_INPUT");
    		z80.setRegisterValue(RegisterNames.HL, -1 & 0xffff); // TMP TODO
    		
    		return 1;
        	
    	case PortalConsts.R_FCOLOR:
    		//System.out.println("R_FCOLOR"); // TODO
    		
    		{
    			int red = z80.getRegisterValue(RegisterNames.H);
    			int green = z80.getRegisterValue(RegisterNames.L);
    			int blue = z80.getRegisterValue(RegisterNames.D);
    			parser.fg_color = new Color(red, green, blue);
    		}
    		return 1;
    		
    	case PortalConsts.R_FCOLOR+1:
    		{
    			int DE = z80.getRegisterValue(RegisterNames.DE);
    			int red  = z80Memory.readByte(DE++);
    			int green  = z80Memory.readByte(DE++);
    			int blue  = z80Memory.readByte(DE);
    			parser.fg_color = new Color(red, green, blue);
    		}
    		
    		return 1;
    	
    	case PortalConsts.R_FCOLOR+2:
    		//System.out.println("R_FCOLOR+2"); // TODO
    		return 1;
    	
    	case PortalConsts.R_BCOLOR:
		{
			int red = z80.getRegisterValue(RegisterNames.H);
			int green = z80.getRegisterValue(RegisterNames.L);
			int blue = z80.getRegisterValue(RegisterNames.D);
			parser.bg_color = new Color(red, green, blue);
		}
    		return 1;
    		
    	case PortalConsts.R_BCOLOR+1:
    		{
    			int DE = z80.getRegisterValue(RegisterNames.DE);
    			int red  = z80Memory.readByte(DE++);
    			int green  = z80Memory.readByte(DE++);
    			int blue  = z80Memory.readByte(DE);
    			parser.bg_color = new Color(red, green, blue);
    		}
    		
    		return 1;    	
    	
    	case PortalConsts.R_WAIT16:
    		
    		// TODO
    		
    		try {
				sleep(15);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//  e.printStackTrace();
			}
    		
    		return 1;
    	
    	case PortalConsts.R_ALARM:
    		
    		CharTest();
    		
    		return 1;
    	
    	case PortalConsts.R_SSF:
    	
            int hl = z80.getRegisterValue(RegisterNames.HL);

            
            System.out.println("------------------------R_SSF HL: " + String.format("%x", hl));
    		// TODO
    		System.out.println("------------------------NOT handled R_SSF");
    		return 2;

    	default: 
        	System.out.println("------------------------NOT handled 0x" + String.format("%x", val));
        	return 2;
        	
    	}
    	
    }
    
    private void CharTest()
    {

    	byte[] cbuf =  new byte[5];	// never seen more than one char at a time from mtutor
    	
    	int chr;
    	int lth = 0;
    	
        for (int i = 0; i < 64 ; i++) //  0..63 or 64..127
        {
        	chr = i;
        
            cbuf[lth++] = (byte)(chr);
            
           
            if (true)
            {
            	parser.text_charset = 0;

            	parser.AlphaDataM(cbuf);
            	parser.FlushText();
            	
                lth = 0;              	
            }

        }

    }
    
    
    private static final byte[] convert0 =
    		{
    			 0,  1,  2,  3,  4,  5,  6,  7,
    			 8,  9, 10, 11, 12, 13, 14, 15,
    			16, 17, 18, 19, 20, 21, 22, 23,
    			24, 25, 26, 48, 49, 50, 51, 52,
    			53, 54, 55, 56, 57, 37, 38, 42,
    			47, 41, 42, 43, 44, 45, 46, 46,
    			48, 49, 50, 51, 52, 53, 54, 55,
    			56, 57, 58, 59, 60, 63, 62, 63,
    			0
    		};


}