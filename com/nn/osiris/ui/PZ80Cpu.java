/*
 * PZ80Cpu.java
 *
 * Encapsulates Z80 microprocessor emulator and a PLATO Terminal Resident emulator.
 * 
 * Author: Dale Sinder
 * 
 */

package com.nn.osiris.ui;

import java.awt.Color;
//import java.util.*;

import com.codingrodent.microprocessor.Z80.*;
import com.codingrodent.microprocessor.Z80.CPUConstants.*;

public class PZ80Cpu {
    /* z80 cpu */
	public Z80Core z80;
	/* ram for z80 */
    public PZMemory z80Memory;
    /* IO for the z80 */
    public PZIO z80IO;
    /* LevelOneParser that owns us */
    public LevelOneParser parser;
    /* mtutor level of the interpreter */
    public int m_mtPLevel;
    /* flag indicating z80 should stop */ 
    public boolean stopme;
    /* is mtutor running flag */
    public boolean mtutor_waiting = false;
    
    private long xmits = 0;
    
    /* internal stop processing flag for z80 - */
    private boolean giveupz80;
    /* internal counter */
    private int m_mtincnt = 0;
    
    private int r_execs = 0;
    
    //private boolean timer_off = false;
    
    // circular buffer for accumulating keys */
    public CircularBuffer keyBuffer;
    
    /* translates portal (non flow control) keys to mtutor keys */
    private static final long[] portalToMTutor = new long[]		
    {
    		60,   18,   24,   27,   17,   49,   52,   18,		// 0..7     ACCESS, ??, BACK, COPY, SUB, SUB1, ??, ANS,
    	    19,   53,   12,   21,   29,   22,   56,   16,		// 8..15	ERASE, HELP1,,  LAB, NEXT, BACK1, ,
    	    -1,   58,   25,   16,   50,   -1,   59,   48,		// 16..23	??, STOP1, DATA, SUP, TERM, ,,,
    	    10,   51,   23,   -1,   -1,   57,   -1,   96,		// 24..31	times, SUB, EDIT ,,,,,,,
    	    64,  126,  127,   -1,   36,   37,   10,   42,		// 32..39	, !, ", ??, $, %, &, ',
    	    41,  123,   40,   14,   95,   15,   94,   93,		// 40..47	(, ), *, plus, ",", minus, ., /,
    	     0,    1,    2,    3,    4,    5,    6,    7,		// 48..55	0, 1, 2, 3, 4, 5, 6, 7,
    	     8,    9,  124,   92,   32,   91,   33,  125,		// 56..63	8, 9, :, ;, <, =, >, ?,
    	    43,   97,   98,   99,  100,  101,  102,  103,		// 64..71  @, A..G,
    	   104,  105,  106,  107,  108,  109,  110,  111,		// 72..79  H..O
    	   112,  113,  114,  115,  116,  117,  118, 119,		// 80..87  P..W
    	   120,  121,  122,   34,   45,   35,   13,   38, 		// 88..95  X, Y, Z, [, \, ], _,
    	    11,   65,   66,   67,   68,   69,   70,   71,		// 96..103  , a..g
    	    72,   73,   74,   75,   76,   77,   78,   79,		// 104..111  h..o
    	    80,   81,   82,   83,   84,   85,   86,   87,		// 112..119  p..w
    	    88,   89,   90,   20,   39,   28,   -1,   52		// 120..127  x, y, z, {, |, }, ~, ,FONT,     // -1 was 0x4e3c40
    };
    
    public PZ80Cpu()
    {

    }

    /* Initializes the object */
    public Z80Core Init(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZMemory();
    	z80IO  = new PZIO();
    	z80 = new Z80Core(z80Memory, z80IO);
    	z80IO.Init(this);
    	
    	setM_KSW(0);	// init send keys to plato/host
    	
    	keyBuffer = new CircularBuffer(200);
    	
    	giveupz80 = false;
    	
    	return z80;
    }
    
    /* main z80 instruction loop */
    public void runZ80() { //
        // Ok, run the program
    	
        saveParserState();
        
        if(mtutor_waiting)
        {
        	// restore local state except for first time through loop
        	restoreLocalState();
        }
        else {
        	keyBuffer.EmptyQueue();
        }
        
        mtutor_waiting = true;	// mark mtutor/z80 running
        
        int pc;					// Program counter
        long tstates = z80.getTStates();
        
        long loops = 0;
        
        pc = z80.getProgramCounter();
        z80.resetTStates();
        //System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 New time slice...Starting PC= "+String.format("%x", pc));

        boolean test;  // = z80.getHalt();
        stopme = false;
        
	        while (true) {
                test = z80.getHalt();

	        	if (loops++ > parser.z80_loops || giveupz80)	// limit loops per time slice
	        	{
	        		giveupz80 = false;
	        		mtutor_waiting = true;		// tell owner we need more time slices
	        		break;
	        	}
	            try {
	                //System.out.println("------------------------ Z80 Running... PC=0x"+Utilities.getWord(z80.getRegisterValue(RegisterNames.PC)));
	                pc =  z80.reg_PC; // z80.getProgramCounter();	// Check if PC is calling Resident
	                if ( pc > (PortalConsts.R_MAIN -1) && pc < (PortalConsts.R_DUMMY3 + 3) && !stopme)
	                {
	                	// need to process resident calls here.
	                	int result = Resident(pc);
	                	
	                	if (result == 1)
	                		z80.ret();
	                	else if (result == 0)
	                		continue;
	                	else if (result == 2)
	                	{
	                		mtutor_waiting = false;
	                		
	                		stopme = true;
	                	}
	                	
	                }
	                if (!test && !stopme && !giveupz80 )
	                {
	                	z80.executeOneInstruction();
	                }
	                else
	                	
	                	/*
	                	
	                	// for threading instead of a break we should be in a sleep loop here until stopme is false again
	                	
	                	if (PortalConsts.is_threaded)
	                	{
	                		if (stopme)
	                		{
	                	        tstates = z80.getTStates();
	                	        z80.resetTStates();
	                	       // System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 thread waiting... waiting PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	                	    	//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>===========Threads copied: "+threadsCopied);

	                		}
	            	        restoreParserState();

	                		while (stopme)
	                		{
	                			//sleep(50);
	                		}
	            	        //long tstates = z80.getTStates();
	            	        z80.resetTStates();
	            	        System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 thread continuing...PC= "+String.format("%x", pc));
	                		
	            	        saveParserState();	            	        
	            	        continue;
	                	}
	                	else
	                	*/
	                	
	                		break;
	                
	                //test = z80.getHalt();		// for test at top of loop 
	                
	                
	            } catch (Exception e) {
	                System.out.println("Z80 Hardware crash, oops! " + e.getMessage());
	            }
	        }
	        
	        if ( mtutor_waiting)
	        {
	        	// save local state for resume
	        	
	        	parser.do_repaint = true;	// the caller to repaint screen
	        	
	        	saveLocalState();			// save the local state
	        }
	        
	        restoreParserState();			// restore the parsers state
	        
	        /*
	        tstates = z80.getTStates();
	        z80.resetTStates();
	        if (!mtutor_waiting)
	        	System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 program stopped...End PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	        else
	        	System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 program WAITING...End PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	        	*/

    }
    
    
    /* 
     * 
     * Do checks and setup before running the z80
     * 
     */
    public void runWithMtutorCheck(int address)
    {
    	// do checks here for patching the levels of MTUTOR -  then run
    	
        // find mtutor release level
        // unfortunately cdc put it in different places in different
        // releases - but only off by 1 byte.
        int release = z80Memory.readByte(0x530b);  // release or year *y1*
        if (release == 8)           // year *y1* 82 - 8?
            release = z80Memory.readByte(0x530a);  // release
        
        if (!parser.booted)
        	m_mtPLevel = release;
        
       // runs++;
        
        stopme = true;
        
        switch (m_mtPLevel)
        {
        case 2: PatchL2 (); break;
        case 3: PatchL3 (); break;
        case 4: PatchL4 (); break;
        case 5: break; // PatchL5 (); break;
        case 6: break; // PatchL6 (); break;
        default: break;
        }
        
        z80.setResetAddress(address);
        z80.setProgramCounter(address);
  
        /*
		if (PortalConsts.is_threaded && !has_been_started)
		{
			has_been_started = true;
			start();
		}
		else if (PortalConsts.is_threaded && has_been_started)
			stopme = false;
		else
		
		*/
	    runZ80();
    }
    
    
    void PatchL2 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PortalConsts.Level2Pause, PortalConsts.CALL8080);
    	z80Memory.writeWord(PortalConsts.Level2Pause + 1, PortalConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..
    	z80Memory.writeWord(PortalConsts.Level2Xplato,  0);
    	z80Memory.writeByte(PortalConsts.Level2Xplato + 2,  0);
    	
        // patch xerror tight getkey loop problem in mtutor
        // top 32K of ram was for memory mapped video on ist 2/3
        // so that's safe for us to use
    	z80Memory.writeByte(0x5d26,  0x10);
    	z80Memory.writeByte(0x5d27,  0x80);		// jmp just above interp
    	

    	z80Memory.writeByte(0x8010, PortalConsts.CALL8080);
    	z80Memory.writeByte(0x8011, 0x2f);
    	z80Memory.writeByte(0x8012, 0x60);	// xplato

    	z80Memory.writeByte(0x8013, PortalConsts.JUMP8080);  // jmp
    	z80Memory.writeByte(0x8014, 0x22);
    	z80Memory.writeByte(0x8015, 0x5d);	// back to loop - getkey
    }

    void PatchL3 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PortalConsts.Level3Pause, PortalConsts.CALL8080);
    	z80Memory.writeWord(PortalConsts.Level3Pause + 1, PortalConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
    	z80Memory.writeWord(PortalConsts.Level3Xplato,  0);
    	
        //RAM[0x5f5c] = RET8080;  // ret to disable ist-3 screen print gunk
    	z80Memory.writeByte(0x600d, PortalConsts.RET8080);  // z80 ret
    }

    
    void PatchL4 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PortalConsts.Level4Pause, PortalConsts.CALL8080);
    	z80Memory.writeWord(PortalConsts.Level4Pause + 1, PortalConsts.R_WAIT16);
    	
    	//z80Memory.writeWord(0x6958, 2);
    	//z80Memory.writeWord(0x6962, 3);
        
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
    	z80Memory.writeWord(PortalConsts.Level4Xplato,  0);
    	
        //RAM[0x5f5c] = RET8080;  // ret to disable ist-3 screen print gunk
    	z80Memory.writeByte(0x5f5c, PortalConsts.RET8080);  // z80 ret

        PatchColor (0xe5);
    }
    
    
    
    /*
     * 
     * This does wild and crazy stuff patching up Mtutor -color	display- to
     * make life easier for the resident
     * 
     */
    void PatchColor(int low_getvar)
    {
        // color display
    	z80Memory.writeByte(0x66aa, PortalConsts.JUMP8080);
    	z80Memory.writeByte(0x66ab, 0x00);
    	z80Memory.writeByte(0x66ac, 0x80);         // color patch jump

    	z80Memory.writeByte(0x8000, PortalConsts.CALL8080);
    	z80Memory.writeByte(0x8001, 0xb0);
    	z80Memory.writeByte(0x8002, 0x66);         // fcolor
    	z80Memory.writeByte(0x8003, 0x21);
    	z80Memory.writeByte(0x8004, 0x25);
    	z80Memory.writeByte(0x8005, 0x7d);         // floating acc
    	z80Memory.writeByte(0x8006, PortalConsts.CALL8080);
    	z80Memory.writeByte(0x8007, 0x90);
    	z80Memory.writeByte(0x8008, 0x00);         // r.fcolor + 2

    	z80Memory.writeByte(0x8009, PortalConsts.CALL8080);
        z80Memory.writeByte(0x800a, low_getvar);
        z80Memory.writeByte(0x800b, 0x71);         // getvar

        z80Memory.writeByte(0x800c, PortalConsts.CALL8080);
        z80Memory.writeByte(0x800d, 0xbd);
        z80Memory.writeByte(0x800e, 0x66);         // bcolor

        z80Memory.writeByte(0x800f, 0x21);
        z80Memory.writeByte(0x8010, 0x25);
        z80Memory.writeByte(0x8011, 0x7d);         // floating acc

        z80Memory.writeByte(0x8012, PortalConsts.CALL8080);
        z80Memory.writeByte(0x8013, 0x93);
        z80Memory.writeByte(0x8014, 0x00);         // r.bcolor + 2

        z80Memory.writeByte(0x8015, PortalConsts.JUMP8080);
        z80Memory.writeByte(0x8016, 0x52);
        z80Memory.writeByte(0x8017, 0x61);         // pincg

                                    // paint - flood fill
        z80Memory.writeByte(0x66c3, 0x20);
        z80Memory.writeByte(0x66c4, 0x80);

        z80Memory.writeByte(0x8020, 0x21);
        z80Memory.writeByte(0x8021, 0);
        z80Memory.writeByte(0x8022, 0);
        z80Memory.writeByte(0x8023, PortalConsts.CALL8080);
        z80Memory.writeByte(0x8024, 0x94);
        z80Memory.writeByte(0x8025, 0x00);         // r.paint
        z80Memory.writeByte(0x8026, PortalConsts.JUMP8080);
        z80Memory.writeByte(0x8027, 0x5d);
        z80Memory.writeByte(0x8028, 0x61);         // pinc1
    }
    
    
    
    public static int Parity(int x)		// add parity bit if needed
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
    	
        int x, y;
        int cx = parser.center_x;
        int sMode = z80Memory.readByte(PortalConsts.M_MODE) & 3;

    	switch(val)
    	{
    	
    	case PortalConsts.R_MAIN:
    		sendKeysToPlato();
    		return 2;
    	
    	case PortalConsts.R_INIT:
        	System.out.println("R_INIT");
    		return 2;
    		
    	case PortalConsts.R_DOT:
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);
        	parser.PlotLine(x + cx, y, x + cx, y, sMode, 0, 0, 1, 0);
            parser.current_x = x;
            parser.current_y = y;        	
    		return 1;    		
    	
    	case PortalConsts.R_LINE:
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);
        	parser.PlotLine(parser.current_x + cx, parser.current_y, x + cx, y,  sMode, 0, 0, 1, 0);
            parser.current_x = x;
            parser.current_y = y;
    		return 1;
    		
    	case PortalConsts.R_CHARS:
        	int cpointer = z80.getRegisterValue(RegisterNames.HL);
        	byte[] cbuf =  new byte[5];	// never seen more than one char at a time from mtutor
        	int chr = z80Memory.readByte(cpointer++);
        	int lth = 0;
        	int charM;
            for (;;)
            {
            	if (chr == 0x3f && 0 == z80Memory.readByte(cpointer))
            		break;
            	
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
                	if (pv == 63) // new line??
                	{
                		parser.CReturn2();
                		return 1;
                	}
     // Char code converter
                	parser.text_charset = 1;	// assume lower case alpha -> M1 (ASCII)

                	if (charM  == 0 )			// unSHIFTed char code
                	{
                		byte cv = convert0[pv];
                		cbuf[0] = cv;
                		parser.text_charset =newchrset0[pv];
                	}

                	else if (charM  == 1 )		// SHIFTed char code
                	{
                		byte cv = convert1[pv];
                		cbuf[0] = cv;
                		parser.text_charset =newchrset1[pv];
                	}
                	else if (charM  == 2 )	// font char code
                	{
                		byte cv = convert0[pv];
                		cbuf[0] = cv;
                		parser.text_charset =(byte) (newchrset0[pv]+ 2);
                	}
                	else if (charM  == 3 )	// SHIFTed font char code
                	{
                		byte cv = convert1[pv];
                		cbuf[0] = cv;
                		parser.text_charset = (byte)(newchrset1[pv] +2);
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
    		
    		
    	case PortalConsts.R_BLOCK:
    	{
    		int hl = z80.getRegisterValue(RegisterNames.HL);
    		int x1 = z80Memory.readWord(hl);
    		int y1 = z80Memory.readWord(hl+2);
    		int x2 = z80Memory.readWord(hl+4);
    		int y2 = z80Memory.readWord(hl+6);
    		parser.BlockData(x1, y1, x2, y2);
    	}
    		return 1;

    	case PortalConsts.R_MODE:
    	{
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
    			int save = parser.screen_mode;
    			parser.screen_mode = LevelOneParser.SCERASE;
    			parser.clearScreen();
    			
    			parser.BlockData(0,0,511,511);
    			
            	parser.do_repaint = true;		// tell the caller to repaint screen
            	parser.screen_mode = save;
    		}
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
    		parser.PlotLine(parser.current_x + cx, parser.current_y, parser.current_x + cx, parser.current_y, 1, 0, 0, 1, 0);
    		
    		return 1;
    		
    	case PortalConsts.R_EXEC:
        	parser.do_repaint = true;		// tell the caller to repaint screen
    		
        	// r.exec is called very frequently when needed.  If we give up the processor too much -pause  time- does not work near right
        	if ((++r_execs % PortalConsts.r_exec_mod) == 0)
        	{
        		r_execs = 0;
        		giveupz80 = true;
        	}
    		return 1;
    		
        case PortalConsts.R_GJOB:
            return 1;
            
        case PortalConsts.R_XJOB:
            return 1;
    		
    	case PortalConsts.R_INPX:
        	z80.setRegisterValue(RegisterNames.HL, parser.current_x);
        	return 1;
    		
    	case PortalConsts.R_INPY:
        	z80.setRegisterValue(RegisterNames.HL, parser.current_y);
        	return 1;
    		
    	case PortalConsts.R_OUTX:
        	parser.current_x = z80.getRegisterValue(RegisterNames.HL);
        	parser.text_margin = parser.current_x;
        	return 1;
        	
    	case PortalConsts.R_OUTY:
        	parser.current_y = z80.getRegisterValue(RegisterNames.HL);
        	return 1;

    	case PortalConsts.R_XMIT: 
        	{
            int k = z80.getRegisterValue(RegisterNames.HL);

            byte temp_hold = getM_KSW();
            if (k != 0x3a)
                setM_KSW(0);
 
            //System.out.print("XMIT: " + String.format("%h", k)+"  count: " + ++xmits);
            
            
        	parser.SendRawKey(0x1b);
        	
        	x = Parity(0x40+(k & 0x3f));
            //System.out.print("  >>   1: " + String.format("%h", (k & 0x3f)));
        	parser.SendRawKey(x);
        	x = Parity(0x60+(k >> 6));
            //System.out.println("  >>   2: " + String.format("%h", (k >> 6)));
        	parser.SendRawKey(x);
        	
        	setM_KSW(temp_hold);   
    		
    		return 1;
        	}
        	
    	case PortalConsts.R_CCR:
        	int ccr_val = z80.getRegisterValue(RegisterNames.L) & 0xff;
        	z80Memory.writeByte(PortalConsts.M_CCR, ccr_val);
        	parser.text_size = (byte)((ccr_val >> 5) & 1);
        	return 1;
        	
    	case PortalConsts.R_INPUT:
    		long mkey = keyBuffer.Dequeue();
    		if (mkey == -1)
    		{
    			z80.setRegisterValue(RegisterNames.HL, -1);
    			return 1;
    		}
//			System.out.println("------------------------R_INPUT pre-key: " + mkey);
    		
    		if ((mkey & 0xff) < 128)
    		{
    			
    			if (mkey == 0x1b)	// ESC - use next two byte
    			{
    				long mkey2 = (keyBuffer.Dequeue() & 0x3f);
    				long temp = keyBuffer.Dequeue() & 0xf;
    				long mkey3 = (temp)  << 6;
    				temp = temp & 0x1c;
    				if (temp  == 0x4)	// just touch keys
    				{
	    				long sendit = mkey2 | mkey3;
	    	      		z80.setRegisterValue(RegisterNames.HL, (int)(sendit));
	    	      		
	    	      		return 1;
    				}
    			}
    			
    			
    			mkey = portalToMTutor[(int)mkey];
//    			System.out.println("------------------------R_INPUT post-key: " + mkey);

    			if (mkey != -1)
    			{
    				mkey = (int)mkey & 0xffff;
    	      		z80.setRegisterValue(RegisterNames.HL, (int)(mkey));
    			}
    			else
    				z80.setRegisterValue(RegisterNames.HL, -1);
    		}
    		
    		return 1;
        	
    	case PortalConsts.R_FCOLOR:
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
    		parser.fg_color = GetColor(z80.getRegisterValue(RegisterNames.HL));
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
    	
    	case PortalConsts.R_BCOLOR+2:
    		parser.bg_color = GetColor(z80.getRegisterValue(RegisterNames.HL));
    		return 1;

    	
    	case PortalConsts.R_WAIT16:
 
        	parser.do_repaint = true;		// tell the caller to repaint screen
    		
//        	int HL = z80.getRegisterValue(RegisterNames.HL);
 
    		int ram = z80Memory.readWord(0x6962);
    		
    		
    		try {
				Thread.sleep(8);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		   		
  /*  		
    		long start_time = System.nanoTime();
    		System.out.println(++runs);
    		while ((System.nanoTime() - start_time) < 150000)
    		{
    			;
    		}

*/
    		
    		
    		return 1;
   
       	case PortalConsts.R_WAIT16 + 1:
       		/*
       		       		try {
       						Thread.sleep(15);
       					} catch (InterruptedException e) {
       						// TODO Auto-generated catch block
       						e.printStackTrace();
       					}
       		   */ 		
       		    		return 1;
    		
       	case PortalConsts.R_WAIT16 + 2:
       		/*
       		       		try {
       						Thread.sleep(15);
       					} catch (InterruptedException e) {
       						// TODO Auto-generated catch block
       						e.printStackTrace();
       					}
       		   */ 		
       		    		return 1;
       	
    	case PortalConsts.R_ALARM:
    		parser.Beep();
    		
 //   		CharTest();		// temp
    		
    		return 1;
    	
    	case PortalConsts.R_SSF:
    	{
    		int ssftype;
            int n = z80.getRegisterValue(RegisterNames.HL);
            int device = ssftype = (n >> 10) & 0x1f;
            int writ = (n >> 9) & 0x1;
            int inter = (n >> 8) & 0x1;
            int data = n & 0xff;
            z80Memory.writeByte(PortalConsts.M_ENAB, data | 0xd0);
           // remember devices
           if(writ == 1)
           {
               parser.m_indev = (byte)device;
           }
           else
           {
        	   parser.m_outdev = (byte)device;
           }
           if (device == 15 && writ == 1 && inter == 0)   
           {  
               if ((m_mtincnt & 3) == 0)
            	   z80.setRegisterValue(RegisterNames.L, 0xcc); //  state->registers.byte[Z80_L] = 0xcc;
               else if ((m_mtincnt & 3) == 1)
            	   z80.setRegisterValue(RegisterNames.L, 0x63);  //state->registers.byte[Z80_L] = 0x63;
               else if ((m_mtincnt & 3) == 2)
            	   z80.setRegisterValue(RegisterNames.L, 0x33);    //state->registers.byte[Z80_L] = 0x33;
               else
            	   z80.setRegisterValue(RegisterNames.L, 0x40);   //state->registers.byte[Z80_L] = 0x40;        // cdc disk resident loaded/running
               m_mtincnt++;            // rotating selection of 3 possible responses
                                       // mtutor tries many times
           }
/*           
	        switch(ssftype)
	   		{
	   			// Slide projector functions (ignored).
	   			case 0:
	   				break;
	   			// Set interrupt mask (touch enable/disable).
	   			case 1:
	   				if ((n & 0x20) != 0)
	   				{
	   					if (!parser.is_touch_enabled)
	   						parser.is_touch_enabled = true;
	   				}
	   				else if (parser.is_touch_enabled)
	   				{
	   					parser.is_touch_enabled = false;
	   				}
	   				break;
	   			// Select input/output.
	   			default:
	   				if ((n & 0x0200) == 0)
	   				{
	   					//ext_device = ssftype;
	   	//				if ( 0 == ( word & 0x0100))
	   	//					ExtData ( word & 0xff);
	   				}
	   				break;
	   		}
 */          
           // I think this switch is PTerm specific   will leave for now
           
           switch (n)
           {
           case 0x1f00:    // xin 7; means start CWS functions
               parser.m_cwsmode = 1;
               break;
           case 0x1d00:    // xout 7; means stop CWS functions
        	   parser.m_cwsmode = 2;
               break;
           case -1:
               break;
           default:
        	   // set interrupt mask
        	   
               if (device == 1 && writ == 0)  // touch enable/disable
               {
            	   parser.is_touch_enabled = ((data & 0x20) != 0);
            	   int enab = z80Memory.readByte(PortalConsts.M_ENAB);
            	   if (parser.is_touch_enabled)
            	   {
            		   z80Memory.writeByte(PortalConsts.M_ENAB, enab | (0x20));
            	   }
            	   else
            	   {
            		   z80Memory.writeByte(PortalConsts.M_ENAB, enab & (0xdf));
            	   }
            	   enab = z80Memory.readByte(PortalConsts.M_ENAB);
            	   //System.out.println("touch: " + parser.is_touch_enabled + "  m.enab: " + (enab & 0x20) + "  data: " + data);
               }
           }
    	}
            
            //System.out.println("------------------------R_SSF HL: 0x" + String.format("%x", hl));
    		// 
            //System.out.println("------------------------NOT handled R_SSF");
        	//mtutor_waiting = false;
        	//sendKeysToPlato();
    		return 1;
    		
    	case PortalConsts.R_PAINT:
    		
    		return 1;

    	case PortalConsts.R_DUMMY2:
    		
    		return	1;
    		
    	case PortalConsts.R_DUMMY2 + 1:
    		
    		return	1;

    	case PortalConsts.R_DUMMY2 + 2:
    		
    		return	1;

    	case PortalConsts.R_DUMMY3:
    		
    		return	1;

    	case PortalConsts.R_DUMMY3 + 1:
    		
    		return	1;
    	
    	case PortalConsts.R_DUMMY3 + 2:
    		// Boot Pterm HELP disk
    		
    		//  parser.needToBoot = true;
    		
    		return	1;

    	
    	
    	default: 
        	System.out.println("------------------------NOT handled 0x" + String.format("%x", val));
        	mtutor_waiting = false;
        	sendKeysToPlato();
        	return 2;
    	}
    	
    }
 
    Color GetColor (int loc)
    {
        int exp = z80Memory.readByte(loc + 1);

        int cb = z80Memory.readByte(loc + 2) << 16;
        cb |= z80Memory.readByte(loc + 3) << 8;
        cb |= z80Memory.readByte(loc + 4);

        cb = cb >> (0x18 - exp);

        return new Color((cb>>16) & 0xff, (cb >> 8) & 0xff, (cb) & 0xff);
    }

    
    // get resident status byte
    public byte getM_KSW()
    {
    	byte val = (byte)z80Memory.readByte(PortalConsts.M_KSW);
    	return val;
    }

    // set resident status byte
    public void setM_KSW(int val)
    {
    	val &= 0xff;
    	z80Memory.writeByte(PortalConsts.M_KSW, val);
    }
    
    // check if we are sending keys to mtutor
    public boolean key2mtutor()
    {
    	return (getM_KSW() & 1) == 1;
    }
    
    // set to send keys to host
    public void sendKeysToPlato()
    {
    	byte val = (byte)z80Memory.readByte(PortalConsts.M_KSW);
    	val &= 0xfe;
    	z80Memory.writeByte(PortalConsts.M_KSW, val);
    }

    //set to send keys to mtutor
    public void sendKeysToMicro()
    {
    	byte val = (byte)z80Memory.readByte(PortalConsts.M_KSW);
    	val |= 1;
    	z80Memory.writeByte(PortalConsts.M_KSW, val);
    }
/*    
    private void CharTest()
    {
    	parser.current_x = 0;
    	parser.current_y = 496;
    	CharTestx((byte)0,0);

    	parser.current_x = 0;
    	parser.current_y = 496-16;
    	CharTestx((byte)0,64);

    	parser.current_x = 0;
    	parser.current_y = 496-32;
    	CharTestx((byte)1, 0);

    	parser.current_x = 0;
    	parser.current_y = 496-48;
    	CharTestx((byte)1, 64);
    }
*/    
    private void CharTestx(byte set, int start )
    {
    	byte[] cbuf =  new byte[5];	// never seen more than one char at a time from mtutor
    	int chr;
    	int lth = 0;
        for (int i = start; i < start+64 ; i++) //  0..63 or 64..127
        {
        	chr = i;
            cbuf[lth++] = (byte)(chr);
           	parser.text_charset = set;
           	parser.AlphaDataM(cbuf);
           	parser.FlushText();
            lth = 0;              	
        }
    }
    
   //  index position is char code, value is index into M table
    
    private static final byte[] convert0 =
    		{
    			58,  1,  2,  3,  4,  5,  6,  7,		// 0..7	
    			 8,  9, 10, 11, 12, 13, 14, 15,		// 8..15
    			16, 17, 18, 19, 20, 21, 22, 23,		// 16..23
    			24, 25, 26, 48, 49, 50, 51, 52,		// 24..31
    			53, 54, 55, 56, 57, 43, 45, 42,		// 32..39
    			47, 40, 41, 36, 61, 32, 44, 46,		// 40..47
    			47, 91, 93, 37, 42, 36, 39, 34,		// 48..55
    			33, 59, 60, 62, 95, 63, 63, 63,		// 56..63
    			0
    		};

 // entries of 0 for M0 1 for M1 (ASCII) M0 has upper alpha M1 lower alpha
    
    private static final byte[] newchrset0 =				
    	{
    			0, 1, 1, 1, 1, 1, 1, 1,			// 0..7
    			1, 1, 1, 1, 1, 1, 1, 1,			// 8..15
    			1, 1, 1, 1, 1, 1, 1, 1,			// 16..23
    			1, 1, 1, 0, 0, 0, 0, 0,			// 24..31
    			0, 0, 0, 0, 0, 0, 0, 0,			// 32..39
    			0, 0, 0, 0, 0, 1, 0, 0,			// 40..47
    			1, 0, 0, 0, 1, 1, 0, 0,			// 48..55
    			0, 0, 0, 0, 0, 0, 1, 1,			// 56..63
    			1
    	};

    //  index position is char code, value is index into M table

    private static final byte[] convert1 =
		{
			35, 65, 66, 67, 68, 69, 70, 71,		// 0..7	
			72, 73, 74, 75, 76, 77, 78, 79,		// 8..15
			80, 81, 82, 83, 84, 85, 86, 87,		// 16..23
			88, 89, 90, 27, 28, 29, 30, 31,		// 24..31
			32, 33, 34, 35, 30, 43, 44, 39,		// 32..39
			40, 27, 29, 38, 44, 45, 28, 47,		// 40..47
			48, 48, 49, 50, 51, 52, 53, 54,		// 48..55
			55, 56, 57, 58, 59, 64, 92, 62,		// 56..63
			0
		};

    // entries of 0 for M0 1 for M1 (ASCII) M0 has upper alpha M1 lower alpha

    private static final byte[] newchrset1 =
    	{
    			0, 0, 0, 0, 0, 0, 0, 0,			// 0..7
    			0, 0, 0, 0, 0, 0, 0, 0,			// 8..15
    			0, 0, 0, 0, 0, 0, 0, 0,			// 16..23
    			0, 0, 0, 1, 1, 1, 1, 1,			// 24..31
    			1, 1, 1, 1, 1, 1, 1, 1,			// 32..39
    			1, 1, 1, 0, 1, 1, 1, 1,			// 40..47
    			1, 1, 1, 1, 1, 1, 1, 1,			// 48..55
    			1, 1, 1, 1, 1, 0, 0, 1,			// 56..63
    			1
    	};
    
    
	
	////// Parser state to save/restore
	
	/** Current screen mode of terminal. */
    private int screen_mode;
	/** Current x location. */
    private int current_x;
	/** Current y location. */
    private int current_y;
	/** Current charset working in. */
    private byte text_charset;
	/** Text size. */
    private byte text_size;
	/** X coordinate for centering. */
    private int center_x;
	/** Foreground color. */
    private Color fg_color;
	/** Background color. */
    private Color bg_color;
    
    private int text_margin;

    
	/** Current screen mode of terminal. */
    private int Rscreen_mode;
	/** Current x location. */
    private int Rcurrent_x;
	/** Current y location. */
    private int Rcurrent_y;
	/** Current charset working in. */
    private byte Rtext_charset;
	/** Text size. */
    private byte Rtext_size;
	/** X coordinate for centering. */
    private int Rcenter_x;
	/** Foreground color. */
    private Color Rfg_color;
	/** Background color. */
    private Color Rbg_color;
	
    private int Rtext_margin;
	
	/////

    public void saveLocalState()
    {
    	Rscreen_mode = parser.screen_mode;
    	Rcurrent_x = parser.current_x;
    	Rcurrent_y = parser.current_y;
    	Rtext_charset = parser.text_charset;
    	Rtext_size = parser.text_size;
    	Rcenter_x = parser.center_x;
    	Rfg_color = parser.fg_color;
    	Rbg_color = parser.bg_color;
    	Rtext_margin = parser.text_margin;
    }

    
    public void restoreLocalState()
    {
    	parser.screen_mode = Rscreen_mode;
    	parser.current_x = Rcurrent_x;
    	parser.current_y = Rcurrent_y;
    	parser.text_charset = Rtext_charset;
    	parser.text_size = Rtext_size;
    	parser.center_x = Rcenter_x;
    	parser.fg_color = Rfg_color;
    	parser.bg_color = Rbg_color;
    	parser.text_margin = Rtext_margin;
    }

    
    public void saveParserState()
    {
    	screen_mode = parser.screen_mode;
    	current_x = parser.current_x;
    	current_y = parser.current_y;
    	text_charset = parser.text_charset;
    	text_size = parser.text_size;
    	center_x = parser.center_x;
    	fg_color = parser.fg_color;
    	bg_color = parser.bg_color;
    	text_margin = parser.text_margin;
    }
    
    public void restoreParserState()
    {
    	parser.screen_mode = screen_mode;
    	parser.current_x = current_x;
    	parser.current_y = current_y;
    	parser.text_charset = text_charset;
    	parser.text_size = text_size;
    	parser.center_x = center_x;
    	parser.fg_color = fg_color;
    	parser.bg_color = bg_color;
    	parser.text_margin = text_margin;
    }
    
    public boolean BootMtutor(String fn)
    {
    	if (fn != null)
    	{
			boolean fexists = MTDisk.Exists(fn);
			if (!fexists)
				return false;
			
			stopme = true;
			z80.halt = true;
		
			MTDisk myFile = new MTDisk(fn);
			
			if (this.z80IO.m_MTDisk[0] != null)
			{
				this.z80IO.m_MTDisk[0].Close();
			}
			
			this.z80IO.m_MTDisk[0] = myFile;
    	}

    	
		int it = this.z80IO.m_MTDisk[0].ReadByte(25);
		
		if (it == 0)
			return false;   // no router set
		m_mtPLevel = this.z80IO.m_MTDisk[0].ReadByte(36);
		int readnum = 0;  	// lth of interp in sectors
		

		if (m_mtPLevel == 2)
	    {
	        readnum = 80;	// lth of interp in sectors
	    }
	    else if (m_mtPLevel == 3)
	    {
	        readnum = 81;	// lth of interp in sectors
	    }
	    else if (m_mtPLevel == 4)
	    {
	        readnum = 82;	// lth of interp in sectors
	    }
	    else if (m_mtPLevel == 5)
	    {
	        readnum = 82;	// lth of interp in sectors
	    }
	    else if (m_mtPLevel == 6)
	    {
	        readnum = 82;	// lth of interp in sectors
	    }
		
		if (!this.z80IO.m_MTDisk[0].ReadSectorsForBoot(0x5300, 21504, readnum, this))	// read interp to ram
			return false;

		parser.needToBoot = false; 
		parser.booted = true;
		
		((PortalFrame)parser.parent_frame).setTitle((LevelOnePanel)(parser.levelone_container), "Micro-Tutor");
		
		this.z80.reset();
		runWithMtutorCheck(0x5306);  					// f.inix - boot entry point

		return true;
    }

}
