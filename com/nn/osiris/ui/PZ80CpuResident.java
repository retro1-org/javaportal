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

/**
 * 
 * Contains the Z80/Resident object, the memory object, and the IO object,
 * Provides Terminal Resident emulation and mtutor interp. patching function.
 * Also "clocks" the Z80 through each instruction and intercepts calls to the
 * Resident.  Provides part of the time slicing function and control of Z80.
 * 
 */
public class PZ80CpuResident {
    /** z80 cpu */
	private Z80Core z80;
	/** ram for z80 */
    private PZ80Memory z80Memory;
    /** IO for the z80 */
    private PZ80IO z80IO;
    /** LevelOneParser that owns us */
    private LevelOneParser parser;
    /** mtutor level of the interpreter */
    private int m_mtPLevel = 0;
    /** internal stop processing flag for z80 - */
    private boolean giveupz80;
    /** internal counter */
    /** is mtutor/z80_program running/waiting flag */
    private boolean mtutor_waiting = false;

    private int m_mtincnt = 0;
    
    private int r_execs = 0;
    
    /** circular buffer for accumulating keys */
    private CircularBuffer keyBuffer;
    
    /** flag indicating z80 should stop */ 
    public boolean stopme;
    
    public boolean in_r_exec = false;

    /** The following are read only outside of this class.  Provide access... */
    
    public boolean mtutor_waiting()
    {
    	return mtutor_waiting;
    }

    public CircularBuffer keyBuffer()
    {
    	return keyBuffer;
    }
    
    public PZ80IO z80IO()
    {
    	return z80IO;
    }

    public Z80Core z80()
    {
    	return z80;
    }
    
    public PZ80Memory z80Memory()
    {
    	return z80Memory;
    }
    
    /** translates portal (non flow control) keys to mtutor keys */
    private static final long[] portalToMTutor = new long[]		
    {
    		60,   18,   24,   27,   17,   49,   52,   18,		// 0..7     ACCESS, ANS, BACK, COPY, SUB, SUB1, FONT, ANS,
    	    19,   53,   12,   21,   29,   22,   56,   16,		// 8..15	ERASE, HELP1, TAB, HELP, LAB, NEXT, BACK1, SUP,
    	    -1,   58,   25,   16,   50,   -1,   59,   48,		// 16..23	??, STOP1, DATA, SUP, TERM, ??, COPY1, SUP1,
    	    10,   51,   23,   -1,   44,   57,   54,   96,		// 24..31	times, ERASE1, EDIT , ??, CR, DATA1, EDIT1, BACK-SPACE,
    	    64,  126,  127,   46,   36,   37,   10,   42,		// 32..39	SPACE, !, ", SIGMA, $, %, times, union,
    	    41,  123,   40,   14,   95,   15,   94,   93,		// 40..47	(, ), *, plus, ",", minus, ., /,
    	     0,    1,    2,    3,    4,    5,    6,    7,		// 48..55	0, 1, 2, 3, 4, 5, 6, 7,
    	     8,    9,  124,   92,   32,   91,   33,  125,		// 56..63	8, 9, :, ;, <, =, >, ?,
    	    43,   97,   98,   99,  100,  101,  102,  103,		// 64..71  intersection, A..G,
    	   104,  105,  106,  107,  108,  109,  110,  111,		// 72..79  H..O
    	   112,  113,  114,  115,  116,  117,  118, 119,		// 80..87  P..W
    	   120,  121,  122,   34,   45,   35,   13,   38, 		// 88..95  X, Y, Z, [, \, ], _,
    	    11,   65,   66,   67,   68,   69,   70,   71,		// 96..103  divide, a..g
    	    72,   73,   74,   75,   76,   77,   78,   79,		// 104..111  h..o
    	    80,   81,   82,   83,   84,   85,   86,   87,		// 112..119  p..w
    	    88,   89,   90,   20,   39,   28,   -1,   52		// 120..127  x, y, z, {, |, }, ~, ??,FONT,     // -1 was 0x4e3c40
    };
    
    /** 
     * Default constructor
     */
    public PZ80CpuResident()
    {
    }

    /** Initializes the Processor/Resident object */
    public Z80Core Init(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZ80Memory();
    	z80IO  = new PZ80IO();
    	z80 = new Z80Core(z80Memory, z80IO);
    	z80IO.Init(this);
    	
    	setM_KSW(0);	// init send keys to plato/host
    	
    	keyBuffer = new CircularBuffer(200);
    	giveupz80 = false;
    	return z80;
    }
    
    /**
     * 
     *  main z80 instruction loop with test for call to Resident
     *  and return from resident mode 6 processing.
     * 
     * Also saves and restores parser state vars we mess with on
     * entry and exit
     * 
     */
    private void runZ80() {
        int pc;					// Program counter
        long loops = 0;			// instructions this time slice
        /**
    	 * We save and restore parser vars we mess with at the beginning
    	 * and end of each time slice
    	 */
        saveParserState();
    	/** restore local (mtutor) state except for first time through loop */
        if(mtutor_waiting || in_r_exec)
        	restoreLocalState();
        else
        	keyBuffer.EmptyQueue();	// clear key buffer
        
        /** mark mtutor/z80 running */
        mtutor_waiting = true;	
		in_r_exec = false;
        z80.resetTStates();
        stopme = false;
       	/** z80 main instruction loop */
        for (;;) {
        	/** end the loop ?*/
        	if (loops++ > parser.z80_loops || giveupz80)
        	{
        		if (giveupz80)  // set only in R_EXEC
        			in_r_exec = true;
	        		
        		giveupz80 = false;
        		mtutor_waiting = true;		// tell owner we need more time slices
        		break;
        	}
            try {
                pc =  z80.reg_PC; // z80.getProgramCounter();	
                
                /** Check if PC (running program/MTutor) is calling Resident */
                if ( pc > (PlatoConsts.R_MAIN -1) && pc < (PlatoConsts.R_DUMMY3 + 3) && !stopme)
                {
                	/** need to process resident calls here. */
                	int result = Resident(pc);
	                	
                	if (result == 1)
                		/** execute a z80 return following resident work */
                		z80.ret();
	               	else if (result == 0)
	               	{
	               		z80.executeOneInstruction();	// we get to execute one z80 instruction
	               		continue;						// and continue
	               	}
	               	else if (result == 2)
	               	{
	               		mtutor_waiting = false;
	               		stopme = true;
	               	}
	               }
                /** continue processing ? */
                if (!z80.getHalt() && !stopme)
                {
                	/** Special checks for return from mode six data handler */
                	if ( in_mode6 && z80.reg_PC == PlatoConsts.Level4Mode6Ret && m_mtPLevel > 3 )  // return from mode 6 handler address
                	{
                		z80.RestoreState();
                		in_mode6 = false;
                		if (z80.reg_PC == PlatoConsts.R_MAIN) 			// Mtutor no longer running!
                			z80.reg_PC = PlatoConsts.Level4MainLoop;	// put it back in main loop!
                	}
                	/** finally we get to execute one z80 instruction */
                	z80.executeOneInstruction();
                }
                else	/** end the loop */
                	break;
	                
            } catch (Exception e) {
            	System.out.println("Z80 Hardware crash, oops! " + e.getMessage());
            }
        }
        if ( mtutor_waiting)
        {
        	/** save local state for resume */
        	parser.do_repaint = true;	// the caller to repaint screen
        	saveLocalState();			// save the local state
        }
        restoreParserState();			// restore the parsers state
    }
    
    /** 
     * Do checks and setup before running the z80
     *
	 * do checks here for patching the levels of MTUTOR -  then run
	 *
	 * find mtutor release level
	 * unfortunately cdc put it in different places in different
	 * releases - but only off by 1 byte.
	 * 
	 * WARNING *** WARNING *** WARNING!!!
	 * 
	 * WARNING - NON-MTutor user z80 programs MUST NOT use addresses
	 * PortalConsts.MTUTLVL and PortalConsts.MTUTLVL+1 = 0x530a .. 0x530b.
	 * Those locations should be 0 = noops.  Otherwise this code might think it
	 * needs to patch MTutor!!
	 * 
	 * At least those locations MUST NOT contain: 2..6
    */
    public void runWithMtutorCheck(int address)
    {
    	int release = z80Memory.readByte(PlatoConsts.MTUTLVL + 1);  // release or year *y1*
        if (release == 8)           // year *y1* 82 - 8?
            release = z80Memory.readByte(PlatoConsts.MTUTLVL); 	 // release
        
        if (!parser.booted)
        	m_mtPLevel = release;
        
        /**
         * There are some things in the Mtutor interpreter we need to patch for our
         * emulated environment.  Do that BEFORE allowing the Z80 to run it's 
         * time slice.
         */
        switch (m_mtPLevel)
        {
        case 2: PatchL2(); break;
        case 3: PatchL3(); break;
        case 4: PatchL4(); break;
        case 5: PatchL5(); break;
        case 6: PatchL6(); break;
        default: break;
        }
        
        z80.setResetAddress(address);
        z80.setProgramCounter(address);	// where to start
        /** And we are off! Call the z80 execute loop controller */
	    runZ80();
    }
    
    /**
     * These patches were determined by examining the disassembled mtutor interpreter.
     * 
     * Mtutor pause WAS a loop tuned to the speed of the processors in each terminal.
     * That has been replaced with a new resident call that uses native waiting tech.
     * >> LevelNPause
     * 
     * Calls to the resident r.exec entry point are skipped altogether >> NOOPs.
     * 
     * Calls to the  SCREEN PRINT function are aborted.
     * 
     */
    
    /**
     * Patch Level 2 mtutor
     */
    void PatchL2 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PlatoConsts.Level2Pause, PlatoConsts.CALL8080);
    	z80Memory.writeWord(PlatoConsts.Level2Pause + 1, PlatoConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..
    	z80Memory.writeWord(PlatoConsts.Level2Xplato,  0);
    	z80Memory.writeByte(PlatoConsts.Level2Xplato + 2,  0);
    	
        // patch xerror tight getkey loop problem in mtutor
        // top 32K of ram was for memory mapped video on ist 2/3
        // so that's safe for us to use
    	z80Memory.writeWord(0x5d26,  0x8010);	// jmp just above interp
    	
    	// here is the patch
    	z80Memory.writeByte(0x8010, PlatoConsts.CALL8080);
    	z80Memory.writeWord(0x8011, 0x602f);	// xplato

    	z80Memory.writeByte(0x8013, PlatoConsts.JUMP8080);  // jmp
    	z80Memory.writeWord(0x8014, 0x5d22);// back to loop - getkey
    }

    /**
     * Patch Level 3 mtutor
     */
    void PatchL3 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PlatoConsts.Level3Pause, PlatoConsts.CALL8080);
    	z80Memory.writeWord(PlatoConsts.Level3Pause + 1, PlatoConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
    	z80Memory.writeWord(PlatoConsts.Level3Xplato,  0);
    	
        // ret to disable ist-3 screen print gunk
    	z80Memory.writeByte(0x600d, PlatoConsts.RET8080);  // z80 ret
    }

    
    /**
     * Patch Level 4 mtutor
     */
    void PatchL4 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PlatoConsts.Level4Pause, PlatoConsts.CALL8080);
    	z80Memory.writeWord(PlatoConsts.Level4Pause + 1, PlatoConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
    	z80Memory.writeWord(PlatoConsts.Level4Xplato,  0);
    	
        // to redirect ist-3 screen print gunk
    	z80Memory.writeByte(0x5f5c , PlatoConsts.RET8080);  // z80 ret

        PatchColor (PlatoConsts.Level4GetVar);
    }
    
    /**
     * Patch Level 5 mtutor
     */
    void PatchL5 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PlatoConsts.Level56Pause, PlatoConsts.CALL8080);
    	z80Memory.writeWord(PlatoConsts.Level56Pause + 1, PlatoConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
    	z80Memory.writeWord(PlatoConsts.Level56Xplato,  0);
    	
        // ret to disable ist-3 screen print gunk
    	z80Memory.writeByte(0x5f5c, PlatoConsts.RET8080);  // z80 ret

        PatchColor (PlatoConsts.Level56GetVar);
    }

    /**
     * Patch Level 6 mtutor
     */
    void PatchL6 ()
    {
        // Call resident for brief pause
    	z80Memory.writeByte(PlatoConsts.Level56Pause, PlatoConsts.CALL8080);
    	z80Memory.writeWord(PlatoConsts.Level56Pause + 1, PlatoConsts.R_WAIT16);
    	
        // remove off-line check for calling r.exec - 
        // only safe place to give up control..  
        // was a z80 jr - 2 bytes only
    	z80Memory.writeWord(PlatoConsts.Level56Xplato,  0);
    	
        // ret to disable ist-3 screen print gunk
    	z80Memory.writeByte(0x5f5c, PlatoConsts.RET8080);  // z80 ret

        PatchColor (PlatoConsts.Level56GetVar);
    }

    
    /**
     * 
     * This does wild and crazy stuff patching up Mtutor -color	display- to
     * make life easier for the resident.  Info to do this was gathered from
     * disassembling the mtutor interpreter and long inspection.  Also
     * stuffed some extra code in high memory  to do some pre-processing
     * in z80 code
     * 
     */
    void PatchColor(int getvar)
    {
        // color display
    	z80Memory.writeByte(0x66aa, PlatoConsts.JUMP8080); // jump to hi mem patch
    	z80Memory.writeWord(0x66ab, 0x8000);				// color patch jump

    	// The patch...
    	z80Memory.writeByte(0x8000, PlatoConsts.CALL8080);
    	z80Memory.writeWord(0x8001, 0x66b0);		// fcolor

    	z80Memory.writeByte(0x8003, 0x21);			// ld hl,0x7d25  - Floating Accumulator Address
    	z80Memory.writeWord(0x8004, PlatoConsts.FLOATACC);

    	z80Memory.writeByte(0x8006, PlatoConsts.CALL8080);
    	z80Memory.writeWord(0x8007, PlatoConsts.R_FCOLOR + 2);

    	z80Memory.writeByte(0x8009, PlatoConsts.CALL8080);
        z80Memory.writeWord(0x800a, getvar);

        z80Memory.writeByte(0x800c, PlatoConsts.CALL8080);
        z80Memory.writeWord(0x800d, 0x66bd);		// bcolor

        z80Memory.writeByte(0x800f, 0x21);			// ld hl,0x7d25  - Floating Accumulator Address
        z80Memory.writeWord(0x8010, PlatoConsts.FLOATACC);

        z80Memory.writeByte(0x8012, PlatoConsts.CALL8080);
        z80Memory.writeWord(0x8013, PlatoConsts.R_BCOLOR + 2);

        z80Memory.writeByte(0x8015, PlatoConsts.JUMP8080);
        z80Memory.writeWord(0x8016, PlatoConsts.Level4MainLoop);

        ////////////////////////////////////////////////////////
        
        // paint - flood fill patch
        z80Memory.writeWord(0x66c3, 0x8020);		// jump to hi mem patch

        // paint patch
        z80Memory.writeByte(0x8020, 0x21);			// ld hl,00
        z80Memory.writeWord(0x8021, 0);

        z80Memory.writeByte(0x8023, PlatoConsts.CALL8080);
        z80Memory.writeWord(0x8024, PlatoConsts.R_PAINT);

        z80Memory.writeByte(0x8026, PlatoConsts.JUMP8080);
        z80Memory.writeWord(0x8027, PlatoConsts.Level4MainLoopPlus);
    }
    
    
    /** add parity bit if needed */
    public static int Parity(int x)		
    {
    	
    	boolean p = CPUConstants.PARITY_TABLE[x];
    	
    	if (p)
    		return x;
    	else
    		return x + 0x80;
    }
    
    public boolean in_mode6 = false;
    
 /** This emulates the "ROM resident".  Return values:
 // 0: PC is not special (not in resident), proceed normally.
 // 1: PC is ROM function entry point, it has been emulated,
//     do a RET now.
 // 2: PC is either the z80 emulation exit magic value, or R_INIT,
//     or an invalid resident value.  Exit z80 emulation.
*/
    private int Resident(int val)
    {
    	
        int x, y;
        int cx = parser.center_x;
        int sMode = z80Memory.readByte(PlatoConsts.M_MODE) & 3;

    	switch(val)
    	{
    	
    	case PlatoConsts.R_MAIN:			// mtutor terminates with this
        	//System.out.println("R_MAIN");
    		mtutor_waiting = false;
        	
        	sendKeysToPlato();
    		return 2;
    	
    	case PlatoConsts.R_INIT:
        	System.out.println("R_INIT");
    		return 2;
    		
    	case PlatoConsts.R_DOT:		// mode 0
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);
        	parser.PlotLine(x + cx, y, x + cx, y, sMode, 0, 0, 1, 0);
            parser.current_x = x;
            parser.current_y = y;        	
    		return 1;    		
    	
    	case PlatoConsts.R_LINE:		// mode 1
        	x = z80.getRegisterValue(RegisterNames.HL);
        	y = z80.getRegisterValue(RegisterNames.DE);

        	if (parser.OkDraw())
    		{
    			parser.PlotLine ( parser.current_x+parser.center_x, parser.current_y,
    					x+parser.center_x, y,
    					screen_mode, parser.style_pattern, 
    					parser.style_fill, parser.style_thickness,
    					parser.style_dash);
    		}

    		//parser.first_line = false;
            parser.current_x = x;
            parser.current_y = y;      
            parser.do_repaint = true;
    		return 1;
    		
    	case PlatoConsts.R_CHARS:		// mode 3
    		R_CHARS();
    		return 1;
    		
    	case PlatoConsts.R_BLOCK:		// mode 4
	    	{
	    		int hl = z80.getRegisterValue(RegisterNames.HL);
	    		int x1 = z80Memory.readWord(hl);
	    		int y1 = z80Memory.readWord(hl+2);
	    		int x2 = z80Memory.readWord(hl+4);
	    		int y2 = z80Memory.readWord(hl+6);
	    		parser.BlockData(x1, y1, x2, y2);
	    	}
    		return 1;
    		
    	case PlatoConsts.R_INPX:
        	z80.setRegisterValue(RegisterNames.HL, parser.current_x);
        	return 1;
    		
    	case PlatoConsts.R_INPY:
        	z80.setRegisterValue(RegisterNames.HL, parser.current_y);
        	return 1;
    		
    	case PlatoConsts.R_OUTX:
        	parser.current_x = z80.getRegisterValue(RegisterNames.HL);
        	return 1;
        	
    	case PlatoConsts.R_OUTY:
        	parser.current_y = z80.getRegisterValue(RegisterNames.HL);
        	return 1;

    	case PlatoConsts.R_XMIT: 
	    	{
		        int k = z80.getRegisterValue(RegisterNames.HL);
		        byte temp_hold = getM_KSW();
		        if (k != 0x3a)
		            setM_KSW(0);
		        
		    	parser.SendRawKey(0x1b);
		    	
		    	x = Parity(0x40+(k & 0x3f));
		    	parser.SendRawKey(x);
		
		    	x = Parity(0x60+(k >> 6));
		    	parser.SendRawKey(x);
		    	
		    	setM_KSW(temp_hold);   
				
				return 1;
	    	}
    	
    	case PlatoConsts.R_MODE:
	    	{
	    		int mode = z80.getRegisterValue(RegisterNames.L) & 0xff;
	    		z80Memory.writeByte(PlatoConsts.M_MODE, (mode & 0x1f ) >> 1);
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
	            	parser.do_repaint = true;		// tell the caller to repaint screen
	            	parser.screen_mode = save;
	    		}
	    	}
    		return 1;

    	case PlatoConsts.R_STEPX:
    		int sdir = z80Memory.readByte(PlatoConsts.M_DIR) & 3;
    		if ((sdir & 2)==0)
    			parser.current_x++;
    		else
    			parser.current_x--;

    		return 1;

    	case PlatoConsts.R_STEPY:
    		int sdir2 = z80Memory.readByte(PlatoConsts.M_DIR) & 3;
    		if ((sdir2 & 1)==0)
    			parser.current_y++;
    		else
    			parser.current_y--;
    		
    		return 1;
    		
    	case PlatoConsts.R_WE:
    		parser.PlotLine(parser.current_x + cx, parser.current_y, parser.current_x + cx, parser.current_y, 1, 0, 0, 1, 0);
    		
    		return 1;
    		
    	case PlatoConsts.R_DIR:
    		int zhl = z80.getRegisterValue(RegisterNames.HL);
    		z80Memory.writeByte(PlatoConsts.M_DIR, zhl & 3);
    		return 1;
    		
    	case PlatoConsts.R_INPUT:
    		R_INPUT();
    		return 1;
        	
    	case PlatoConsts.R_SSF:
    		R_SSF();            
    		return 1;
    		
    	case PlatoConsts.R_CCR:
        	int ccr_val = z80.getRegisterValue(RegisterNames.L) & 0xff;
        	z80Memory.writeByte(PlatoConsts.M_CCR, ccr_val);
        	parser.text_size = (byte)((ccr_val >> 5) & 1);
        	return 1;
        	
    	case PlatoConsts.R_EXTOUT:
    		
    		return 1;
        	
    	case PlatoConsts.R_EXEC:
        	parser.do_repaint = true;		// tell the caller to repaint screen
        	
        	// r.exec is called very frequently when needed.  If we give up the processor too much -pause  time- does not work near right
        	if ((++r_execs % PlatoConsts.r_exec_mod) == 0)
        	{
        		r_execs = 0;
        		giveupz80 = true;
        	}
        	return 1;
    		
        case PlatoConsts.R_GJOB:
            return 1;				// noop
            
        case PlatoConsts.R_XJOB:
        	return 1;				// noop
        	
        case PlatoConsts.R_RETURN:	// obsolete noop
        	
        	return 1;
        	
       	case PlatoConsts.R_CHRCV: 
            int numchars = z80.getRegisterValue(RegisterNames.HL);
       		int fwa = z80.getRegisterValue(RegisterNames.DE);
       		int[] chardata = new int[8];
       		int slot = (fwa - PlatoConsts.M2ADDR) >> 4;
       		
       		for (int i = 0 ; i < numchars ; i++)
       		{
       			for (int j = 0 ; j < 8 ; j++)
       				chardata[j] = (z80Memory.readWord(fwa + j*2 + (i << 4 )) & 0xffff);
     			
       			parser.BuildMChar(chardata);
       			parser.LoadAddrCharFlip(slot++);
       		}
       		
    		return 1;
       		
    	case PlatoConsts.R_ALARM:
    		parser.Beep();
    		
 //   		CharTest();		// temp
    		
    		return 1;
    		
    	case PlatoConsts.R_PRINT:
    		return 1;
    	
    	case PlatoConsts.R_FCOLOR:			// standard
    		{
    			int red = z80.getRegisterValue(RegisterNames.H);
    			int green = z80.getRegisterValue(RegisterNames.L);
    			int blue = z80.getRegisterValue(RegisterNames.D);
    			parser.fg_color = new Color(red, green, blue);
    		}
    		return 1;
    		
    	case PlatoConsts.R_FCOLOR+1:		// using ccode commands
    		{
    			int DE = z80.getRegisterValue(RegisterNames.DE);
    			int red  = z80Memory.readByte(DE++);
    			int green  = z80Memory.readByte(DE++);
    			int blue  = z80Memory.readByte(DE);
    			parser.fg_color = new Color(red, green, blue);
    		}
    		return 1;
    	
    	case PlatoConsts.R_FCOLOR+2:		// redirected by patch
    		parser.fg_color = GetColor(z80.getRegisterValue(RegisterNames.HL));
    		return 1;
    	
    	case PlatoConsts.R_BCOLOR:			// standard
		{
			int red = z80.getRegisterValue(RegisterNames.H);
			int green = z80.getRegisterValue(RegisterNames.L);
			int blue = z80.getRegisterValue(RegisterNames.D);
			parser.bg_color = new Color(red, green, blue);
		}
    		return 1;
    		
    	case PlatoConsts.R_BCOLOR+1:		// using ccode commands
    		{
    			int DE = z80.getRegisterValue(RegisterNames.DE);
    			int red  = z80Memory.readByte(DE++);
    			int green  = z80Memory.readByte(DE++);
    			int blue  = z80Memory.readByte(DE);
    			parser.bg_color = new Color(red, green, blue);
    		}
    		return 1;    	
    	
    	case PlatoConsts.R_BCOLOR+2:		// redirected by patch
    		parser.bg_color = GetColor(z80.getRegisterValue(RegisterNames.HL));
    		return 1;

    	case PlatoConsts.R_PAINT:
    		
    		return 1;

    	case PlatoConsts.R_WAIT16:
 
        	parser.do_repaint = true;		// tell the caller to repaint screen
    		
    		try {
				Thread.sleep(8);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		return 1;
   
       	case PlatoConsts.R_WAIT16 + 1:
    		return 1;
    		
       	case PlatoConsts.R_WAIT16 + 2:
    		return 1;
       	
    	case PlatoConsts.R_EXTEND:
    		return	1;
    		
    		/**
    		 * A hook for an "Extensions to Micro Tutor" notion.
    		 * MTutor -ccode- can be used to call this and
    		 * pass parameters in consecutive memory locations
    		 * pointed at on entry by the Z80 DE register.
    		 */
    	case PlatoConsts.R_EXTEND + 1:
			ExtendMTutor();
    		return	1;

    	case PlatoConsts.R_EXTEND + 2:
    		return	1;

    	case PlatoConsts.R_DUMMY3:
    		return	1;

    	case PlatoConsts.R_DUMMY3 + 1:
    		return	1;
    	
    	case PlatoConsts.R_DUMMY3 + 2:
    		return	1;
    	
    	default: 
        	System.out.println("------------------------Resident R. routine NOT handled 0x" + String.format("%x", val));
        	mtutor_waiting = false;
        	sendKeysToPlato();
        	return 2;
    	}
    }

    /** Extend MTUTOR here.  Lesson will provide ccode interface at address r. 155. */
    private void ExtendMTutor()
    {
		int DE = z80.getRegisterValue(RegisterNames.DE);
		int cmd  = z80Memory.readByte(DE++);	// first byte is command

		switch(cmd)
		{
			case 1:			// Local font select
				FontStyle(DE);
				break;
						
			case 2:			// Text style
				TextStyle(DE);
				break;
				
			case 3:			//Draw Circle/Ellipse
				Ellipse(DE);
				break;
				
			case 4:			//Draw Circle/Ellipse Arc
				TheArc(DE);
				break;
				
			case 5:
				Thickness(DE);	// set line thickness
				break;

			case 6:
				Joint(DE);		// set thick line joint style
				break;
			
			case 7:
				FillFlag(DE);	// polyfill -draw-s flag
				break;
			
			case 8:
				Pattern(DE);	// set fill pattern slot
				break;
			
			case 9:
				Dash(DE);		// set line dash style slot
				break;
			
			case 10:
				Cap(DE);		// set line cap style
				break;
			
			case 11:
				Clear();		// clear graphics styles
				break;
			
			default: break;
		}
    }
    
    /**
     * Set Local Font
     * @param DE
     */
    private void FontStyle(int DE)		// ccode  155,cmd, family where local vars are ==   i,8: cmd, family, size, bold, italic -- cmd = 1
    {
    	int family = z80Memory.readByte(DE++);
    	int size = z80Memory.readByte(DE++);
    	boolean bold = z80Memory.readByte(DE++) > 0;
    	boolean italic = z80Memory.readByte(DE) > 0;
    	
    	int select = (family & 0x3f) << 12;
    	select |= (size & 127);
    	select |= bold ? 04000 : 0;
    	select |= italic ? 02000 : 0;

    	parser.FontSelect2(select);
    }
    	
    /**
     * Set text style
     * @param DE
     */
    private void TextStyle(int DE)		// ccode  155,cmd, styleb where local vars are ==   i,8: cmd, styleb -- cmd = 2
    {
    	parser.text_style = z80Memory.readByte(DE);
    }
    
    /**
     * Draw an Ellipse/Circle
     * @param DE
     */
    private void Ellipse(int DE)
    {
    	int radius = z80Memory.readWord(DE);
    	int r1 = bendint(radius);

    	int radius2 = z80Memory.readWord(DE+2);
    	int r2 = bendint(radius2);

		boolean fill = (r1 < 0);
    	if (fill)
    		r1 = -r1 & 0xfff ;
    	if (r2 < 0)
    		r2 = -r2  & 0x0fff;
    	
		parser.PlotEllipse ( r1, r2,
				parser.current_x+center_x,parser.current_y,parser.screen_mode,
				1,5,fill ? 1 : 0);

		parser.levelone_container.repaint();
    }
    
    /**
     * Draw a Elliptical or Circular Arc
     * @param DE
     */
    private void TheArc(int DE)
    {
    	int radius = z80Memory.readWord(DE);
    	int r1 = bendint(radius);

    	int radius2 = z80Memory.readWord(DE+2);
    	int r2 = bendint(radius2);

    	int ang1 = z80Memory.readWord(DE+4);
    	int a1 = bendint(ang1);

    	int ang2 = z80Memory.readWord(DE+6);
    	int a2 = bendint(ang2);

		boolean fill = (r1 < 0);
    	if (fill)
    		r1 = -r1 & 0xfff ;
    	if (r2 < 0)
    		r2 = -r2  & 0x0fff;
    	
		parser.PlotArc ( r1, r2, a1, a2,
				parser.current_x+center_x,parser.current_y,parser.screen_mode,
				1,5,fill ? 1 : 0);
    	
		parser.levelone_container.repaint();
    }
    
    /**
     * Set line thickness
     * @param DE
     */
    private void Thickness(int DE)
    {
        parser.checkPoly();
    	int pat = z80Memory.readByte(DE);
    	parser.style_thickness = pat & 0x7f;
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }

    /**
     * Set line joint style
     * @param DE
     */
    private void Joint(int DE)
    {
        parser.checkPoly();
    	int pat = z80Memory.readByte(DE);
    	parser.style_join = pat & 0x03;
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }
  
    /**
     * Set/Clear polyline fill flag
     * @param DE
     */
    private void FillFlag(int DE)
    {
        parser.checkPoly();
    	int pat = z80Memory.readByte(DE);
    	parser.style_fill = pat & 0x01;
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }

    /**
     * Set pattern for fills
     * @param DE
     */
    private void Pattern(int DE)
    {
        parser.checkPoly();
    	int pat = z80Memory.readByte(DE);
    	parser.style_pattern = pat & 0x3f;
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }

    /**
     * Set dash style for lines
     * @param DE
     */
    private void Dash(int DE)
    {
        parser.checkPoly();
    	int dash = z80Memory.readByte(DE);
    	parser.style_dash = dash & 0x1f;
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }
    
    /**
     * Set line cap style
     * @param DE
     */
    private void Cap(int DE)
    {
        parser.checkPoly();
    	int cap = z80Memory.readByte(DE);
    	parser.style_cap = cap & 0x03;
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }
    
    /**
     * Clear graphics styles
     * 
     */
    private void Clear()
    {
        parser.checkPoly();
        parser.ClearStyles();
    	parser.first_line = true;
		parser.levelone_container.repaint();
    }
    
    
    /**
     * Swap byte order and sign extend 
     * @param x
     * @return
     */
    private int bendint(int x)
    {
    	int hi = ((x & 0xff) << 8);
    	int lo = ((x >> 8) & 0xff);
    	int r1 = hi | lo;
		if ((r1 & 0100000) != 0)
			r1 |= 0xffff0000;
    	return r1;
    }

    
    /**
     * Plot chars for mode 3
     */
    private void R_CHARS()
	{
    	int cpointer = z80.getRegisterValue(RegisterNames.HL);
		byte[] cbuf =  new byte[100];	// I've never seen more than one char at a time from mtutor
		int chr = z80Memory.readByte(cpointer++);
		int lth = 0;
		int charM;
		
		parser.text_margin =  z80Memory.readWord(PlatoConsts.M_MARGIN);
	
	    for (;;)
	    {
	    	if (chr == 0x3f && 0 == z80Memory.readByte(cpointer))
	    		break;
	    	
	        int save = z80Memory.readByte(PlatoConsts.M_CCR);
	        int pv;
	        charM = (save & 0x0e) >> 1; // Current M slot
	
	        if (chr > 0x3F )
	        {
	            // advance M slot by one
	        	z80Memory.writeByte(PlatoConsts.M_CCR,(z80Memory.readByte(PlatoConsts.M_CCR) & ~0x0e) | (charM + 1) << 1);
	            charM = (z80Memory.readByte(PlatoConsts.M_CCR) & 0x0e) >> 1; // Current M slot
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
	        		return;
	        	}
	// Char code converter
	        	parser.text_charset = 1;	// assume lower case alpha -> M1 (ASCII)
	        	
	        	switch (charM)
	        	{
	        	case 0: 		// unSHIFTed char code
	            	{
	            		byte cv = convert0[pv];
	            		if (cv > 0  && cv < 27)
	            		{
	            			cv += 96;
		            		cbuf[0] = cv;
		            		parser.text_charset = 0;
		            		break;
	            		}
	            		cbuf[0] = cv;
	            		parser.text_charset =newchrset0[pv];
	            	}
	        		break;
	        		
	        	case 1:			// SHIFTed char code
	            	{
	            		byte cv = convert1[pv];
	            		cbuf[0] = cv;
	            		parser.text_charset =newchrset1[pv];
	            		
	            	}
	            	break;
	            	
	        	case 2:			// font char code
	            	{
	            		cbuf[0] = (byte)(pv + 32);
	            		parser.text_charset = 2;
	            	}
	        		break;
	        		
	        	case 3:			// SHIFTed font char code
	            	{
	            		cbuf[0] = (byte)(pv + 32);
	            		parser.text_charset = 3;
	            	}
	        		break;
	            	
	        	}
	// end char converter
	        	parser.AlphaDataM(cbuf);
	        
	        	if (chr0 > 0x3F )
	            {
	                // restore M slot
	            	z80Memory.writeByte(PlatoConsts.M_CCR, save);
	            }
	        }
	    }
		parser.FlushText();

	}

    /**
     * Get a key code
     */
    private void R_INPUT()
    {
		long mkey = keyBuffer.Dequeue();
		if (mkey == -1)
		{
			z80.setRegisterValue(RegisterNames.HL, -1);
			return;
		}
		
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
    	      		
    	      		return;
				}
			}
			
			mkey = portalToMTutor[(int)mkey];

			if (mkey != -1)
			{
				mkey = (int)mkey & 0xffff;
	      		z80.setRegisterValue(RegisterNames.HL, (int)(mkey));
			}
			else
				z80.setRegisterValue(RegisterNames.HL, -1);
		}
    }
    
    /**
     * SSF function
     */
    private void R_SSF()
    {
        int n = z80.getRegisterValue(RegisterNames.HL);
        int device = (n >> 10) & 0x1f;
        int writ = (n >> 9) & 0x1;
        int inter = (n >> 8) & 0x1;
        int data = n & 0xff;
        z80Memory.writeByte(PlatoConsts.M_ENAB, data | 0xd0);
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
        	   int enab = z80Memory.readByte(PlatoConsts.M_ENAB);
        	   if (parser.is_touch_enabled)
        	   {
        		   z80Memory.writeByte(PlatoConsts.M_ENAB, enab | (0x20));
        	   }
        	   else
        	   {
        		   z80Memory.writeByte(PlatoConsts.M_ENAB, enab & (0xdf));
        	   }
        	   enab = z80Memory.readByte(PlatoConsts.M_ENAB);
        	   //System.out.println("touch: " + parser.is_touch_enabled + "  m.enab: " + (enab & 0x20) + "  data: " + data);
           }
       }
	}

    
    /**
     * 
     * Get color from 48 bit mtutor floating point var in ram starting at *loc*
     * 
     */
    Color GetColor (int loc)
    {
    	// grab a floating point color value
        int exp = z80Memory.readByte(loc + 1);	// exponent
        // 24 bit mantissa
        long cb = z80Memory.readByte(loc + 2) << 16;
        cb |= z80Memory.readByte(loc + 3) << 8;
        cb |= z80Memory.readByte(loc + 4);

        cb = cb >> (0x18 - exp);	// adjust to be sure color bits in lower 24 bits
        // get component 8 bit values  r,g,b
        int red = (int)(cb>>16) & 0xff;
        int green = (int)(cb >> 8) & 0xff;
        int blue = (int)(cb) & 0xff;

        return new Color(red, green, blue);
    }
    
    /** get resident status byte
     * 
     * @return
     */
    public byte getM_KSW()
    {
    	byte val = (byte)z80Memory.readByte(PlatoConsts.M_KSW);
    	return val;
    }

    /*
     *  set resident status byte
     */
    public void setM_KSW(int val)
    {
    	val &= 0xff;
    	z80Memory.writeByte(PlatoConsts.M_KSW, val);
    }
    
    /**
     *  check if we are sending keys to mtutor
     * @return
     */
    public boolean key2mtutor()
    {
    	return (getM_KSW() & 1) == 1;
    }
    
    /**
     *  set to send keys to host
     */
    public void sendKeysToPlato()
    {
    	byte val = (byte)z80Memory.readByte(PlatoConsts.M_KSW);
    	val &= 0xfe;
    	z80Memory.writeByte(PlatoConsts.M_KSW, val);
    }

    /**
     * set to send keys to mtutor
     */
    public void sendKeysToMicro()
    {
    	byte val = (byte)z80Memory.readByte(PlatoConsts.M_KSW);
    	val |= 1;
    	z80Memory.writeByte(PlatoConsts.M_KSW, val);
    }

    
    
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
    
    
   /**
    *   index position is char code, value is index into M table
    */
    private static final byte[] convert0 =
    		{
    			58, 97, 98, 99,100,101,102,103,		// 0..7	
    		   104,105,106,107,108,109,110,111,		// 8..15
    		   112,113,114,115,116,117,118,119,		// 16..23
    		   120,121,122, 48, 49, 50, 51, 52,		// 24..31
    			53, 54, 55, 56, 57, 43, 45, 42,		// 32..39
    			47, 40, 41, 36, 61, 32, 44, 46,		// 40..47
    			47, 91, 93, 37, 42, 36, 39, 34,		// 48..55
    			33, 59, 60, 62, 95, 63, 63, 63,		// 56..63
    			0
    		};

/**
  *  entries of 0 for M0 1 for M1 (ASCII) M0 has upper alpha M1 lower alpha
  */
    private static final byte[] newchrset0 =				
    	{
    			0, 0, 0, 0, 0, 0, 0, 0,			// 0..7
    			0, 0, 0, 0, 0, 0, 0, 0,			// 8..15
    			0, 0, 0, 0, 0, 0, 0, 0,			// 16..23
    			0, 0, 0, 0, 0, 0, 0, 0,			// 24..31
    			0, 0, 0, 0, 0, 0, 0, 0,			// 32..39
    			0, 0, 0, 0, 0, 1, 0, 0,			// 40..47
    			1, 0, 0, 0, 1, 1, 0, 0,			// 48..55
    			0, 0, 0, 0, 0, 0, 1, 1,			// 56..63
    			1
    	};

    /**
     *   index position is char code, value is index into M table
     */
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

    /**
     *  entries of 0 for M0 1 for M1 (ASCII) M0 has upper alpha M1 lower alpha
     */
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
    
    
	
	/**
	 *  Parser state to save/restore 
	 *  Two sets - one local and one resident
	 */
	
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
    private int text_style;
    private boolean first_line;
    private int style_thickness;
    

    
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
    private int Rtext_style;
    private boolean Rfirst_line;
    private int Rstyle_thickness;
	
    /**
     * Save local state
     */
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
    	Rtext_style = parser.text_style;
    	Rfirst_line = parser.first_line;
    	Rstyle_thickness = parser.style_thickness;

    }

    /**
     * Restore local state
     */
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
    	parser.text_style = Rtext_style;
    	parser.first_line = Rfirst_line;
    	parser.style_thickness = Rstyle_thickness;
    }

    /**
     * Save parser state
     */
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
    	text_style = parser.text_style;
    	first_line = parser.first_line;
    	style_thickness = parser.style_thickness;

    }
    
    /**
     * Restore parser state
     */
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
    	parser.text_style = text_style;
    	parser.first_line = first_line;
    	parser.style_thickness = style_thickness;
    }
    
    
    /**
     * Boot to Micro-Tutor
     * 
     * 
     * @param fn - filename/path of virtual disk file
     * @return
     */
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
			
			if (this.z80IO.m_MTDisk()[0] != null)
			{
				this.z80IO.m_MTDisk()[0].Close();
			}
			
			this.z80IO.m_MTDisk()[0] = myFile;
    	}

    	
		int it = this.z80IO.m_MTDisk()[0].ReadByte(25);
		
		if (it == 0)
			return false;   // no router set
		m_mtPLevel = this.z80IO.m_MTDisk()[0].ReadByte(36);
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
		
		if (!this.z80IO.m_MTDisk()[0].ReadSectorsForBoot(PlatoConsts.MTutorLoad, PlatoConsts.MTutorOffset, readnum, this))	// read interp to ram
			return false;

		parser.needToBoot = false; 
		parser.booted = true;
		
		((PlatoFrame)parser.parent_frame).setTitle((LevelOnePanel)(parser.levelone_container), "Micro-Tutor");
		
		this.z80.reset();
		runWithMtutorCheck(PlatoConsts.MTutorBoot);  	// f.inix - boot entry point

		return true;
    }

}
