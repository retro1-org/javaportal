package com.nn.osiris.ui;

import java.awt.Color;
//import java.util.*;

import com.codingrodent.microprocessor.Z80.*;
import com.codingrodent.microprocessor.Z80.CPUConstants.*;

public class PZ80Cpu {
    public Z80Core z80;
    public PZMemory z80Memory;
    public PZIO z80IO;
    public LevelOneParser parser;
    
    public int m_mtPLevel;
    
    public boolean stopme;
    
    public boolean mtutor_waiting = false;
    
    private boolean giveupz80;
    
    private int m_mtincnt = 0;
    
    private int runs;
    
    public CircularBuffer keyBuffer;
    
    
    private static final long[] asciiToPlato = new long[]		// upper non-zero bytes to be sent first...
    {
    		60, 0022, 0030,   27,   17, 49, 0064, 0013,			// 0..7   	, ANS, BACK, COPY, DATA, EDIT, divide, HELP,
    	  0023,   53,   12,   21, 0035, 0026,   56,   16,		// 8..15	ERASE,,,  LAB, NEXT, BACK1, ,
    	    16,   58, 0031,   16, 0062,   -1,   59,   48,		// 16..23	SUP, STOP1, DATA, STOP, TERM, ,,,
    	  0012,   51, 0027,   -1,   -1,   57,   -1,   96,		// 24..31	times, SUB, EDIT ,,,,,,,
    	    64, 0176, 0177, 0x3c24, 0044, 0045, 10, 0047,		// 32..39	, !, ", #, $, %, &, ',
    	  0051, 0173, 0050, 0016,   95, 0017,   94, 0135,		// 40..47	(, ), *, plus, ",", minus, ., /,
    	  0000, 0001, 0002, 0003, 0004, 0005, 0006, 0007,		// 48..55	0, 1, 2, 3, 4, 5, 6, 7,
    	  0010, 0011, 0174, 0134, 0040, 0133, 0041, 0175,		// 56..63	8, 9, :, ;, <, =, >, ?,
    	  0x3c05, 0141, 0142, 0143, 0144, 0145, 0146, 0147,		// 64..71  @, A..G,
    	  0150, 0151, 0152, 0153, 0154, 0155, 0156, 0157,		// 72..79  H..O
    	  0160, 0162, 0162, 0163, 0164, 0165, 0166, 0167,		// 80..87  P..W
    	  0170, 0171, 0172, 0042, 0x3c5d, 0043, 13, 0046, 		// 88..95  X, Y, Z, [, \, ], _,
    	    11, 0101, 0102, 0103, 0104, 0105, 0106, 0107,		// 96..103  , a..g
    	  0110, 0111, 0112, 0113, 0114, 0115, 0116, 0117,		// 104..111  h..o
    	  0120, 0121, 0122, 0123, 0124, 0125, 0126, 0127,		// 112..119  p..w
    	  0130, 0131, 0132, 0x3c22, 39,   28, 0x4e3c40, 52	// 120..127  {, |, }, ~, ,,
    	  
    };
    
    
    public PZ80Cpu()
    {

    }

    public Z80Core Init(LevelOneParser x)
    {
    	parser = x;
    	z80Memory = new PZMemory();
    	z80IO  = new PZIO();
    	z80 = new Z80Core(z80Memory, z80IO);
    	
    	setM_KSW(0);	// keys to plato
    	
    	keyBuffer = new CircularBuffer(200);
    	
    	giveupz80 = false;
    	
    	return z80;
    }
    
    
    public void run() { //
        // Ok, run the program
    	
        saveParserState();
        
        if(mtutor_waiting)
        {
        	// restore local state
        	
        	restoreLocalState();
        }
        
        mtutor_waiting = true;
        
        int pc;
        long tstates = z80.getTStates();
        
        long loops = 0;
        
        pc = z80.getProgramCounter();
        z80.resetTStates();
        //System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 New time slice...Starting PC= "+String.format("%x", pc));

        boolean test = z80.getHalt();
        stopme = false;
        
	        while (true) {
	        	if (loops++ > parser.z80_loops || giveupz80)
	        	{
	        		giveupz80 = false;
	        		mtutor_waiting = true;
	        		break;
	        	}
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
	                	{
	                		mtutor_waiting = false;
	                		
	                		stopme = true;
	                	}
	                	
	                }
	                if (!test && !stopme)
	                	z80.executeOneInstruction();
	                else
	                	
	                	// for threading instead of a break we should be in a sleep loop here until stopme is false again
	                	
	                	if (PortalConsts.is_threaded)
	                	{
	                		if (stopme)
	                		{
	                	        tstates = z80.getTStates();
	                	        z80.resetTStates();
	                	        System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 thread waiting... waiting PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
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
	                		break;
	                
	                test = z80.getHalt();
	                
	                
	            } catch (Exception e) {
	                System.out.println("Z80 Hardware crash, oops! " + e.getMessage());
	            }
	        }
	        
	        if ( mtutor_waiting)
	        {
	        	// save local state for resume
	        	
	        	parser.do_repaint = true;
	        	
	        	saveLocalState();
	        }
	        
	        restoreParserState();
	        
	        tstates = z80.getTStates();
	        z80.resetTStates();
	        /*
	        if (!mtutor_waiting)
	        	System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 program stopped...End PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	        else
	        	System.out.println(">>>>>>>>>>>>>>>>>>>>>  Z80 program WAITING...End PC= "+String.format("%x", pc) + "    TStates= " + tstates + "  debug= " +runs);
	        	*/

    }
    
    
    //private boolean has_been_started = false;
    
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
    			parser.screen_mode = LevelOneParser.SCWRITE;
    			parser.clearScreen();
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
//    		giveupz80 = true;				// TODO ??
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
           
        	parser.SendRawKey(0x1b);
        	
        	x = Parity(0x40+(k & 0x3f));
        	parser.SendRawKey(x);
        	x = Parity(0x60+(k >> 6));
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
    			z80.setRegisterValue(RegisterNames.HL, (int)(-1));
    			return 1;
    		}
			System.out.println("------------------------R_INPUT pre-key: " + mkey);
    		
    		if ((mkey & 0xff) != 0xff)
    		{
    			mkey = asciiToPlato[(int)mkey];
    			System.out.println("------------------------R_INPUT post-key: " + mkey);

    			if (mkey != -1)
    			{
    				mkey = (int)mkey & 0xffff;
    	      		z80.setRegisterValue(RegisterNames.HL, (int)(mkey));
    			}
    			else
    				z80.setRegisterValue(RegisterNames.HL, (int)(-1));
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
    		
    		return 1;
    	
    	case PortalConsts.R_ALARM:
    		CharTest();		// temp
    		return 1;
    	
    	case PortalConsts.R_SSF:
    	{
    		int hl;
            int n = hl = z80.getRegisterValue(RegisterNames.HL);
            int device = (n >> 10) & 0x1f;
            int writ = (n >> 9) & 0x1;
            int inter = (n >> 8) & 0x1;
            int data = n & 0xff;
            z80Memory.writeByte(PortalConsts.M_ENAB, data);
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
        	   // set interrupt mask ??  TODO ??
        	   
               if (device == 1 && writ == 0)  // touch enable/disable
               {
//                   m_canvas->ptermTouchPanel((data & 0x20) != 0);  // TODO drs
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

    	default: 
        	System.out.println("------------------------NOT handled 0x" + String.format("%x", val));
        	mtutor_waiting = false;
        	sendKeysToPlato();
        	return 2;
    	}
    	
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
    }

}
