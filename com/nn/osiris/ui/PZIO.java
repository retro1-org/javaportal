package com.nn.osiris.ui;

import com.codingrodent.microprocessor.IBaseDevice;

// do nothing Z80 IO handler TODO

public class PZIO implements IBaseDevice {
    private int value;
    private int m_mtdrivefunc = 0;
    private int m_mtdrivetemp = 0xcb;
    private PZ80Cpu m_cpu;
    private PZMemory m_mem;
    private long init_milli;
    private int m_mtDiskUnit = 0;
    private int m_mtDiskTrack;
    private int m_mtDiskSector;
    private int m_mtDisk1;
    private int m_mtDisk2;
    private int m_mtDiskCheck1;
    private int m_mtDiskCheck2;
    private long m_mtSeekPos;
    private boolean m_clockPhase = true;
    private int m_mtDataPhase;
    private int m_mtsingledata;
    private int m_mtcanresp;
    
    private long readcnt = 0;
    
    public MTDisk[] m_MTDisk = new MTDisk[2];
//    private boolean m_floppy0;
//    private boolean m_floppy1;
    
    public void Init(PZ80Cpu cpu)
    {
    	init_milli = java.lang.System.currentTimeMillis();
    	m_cpu = cpu;
    	m_mem = cpu.z80Memory;
    	
/*
    	if (this.m_MTDisk[0] == null)
    	{
	    	String fn = PortalConsts.InitDisk0;  //  kludge TODO
	    	
			boolean fexists = MTDisk.Exists(fn);
			if (!fexists)
				return;
			
			MTDisk myFile = new MTDisk(fn);
			
			this.m_MTDisk[0] = myFile;
    	}
 */
    	
		/*
    	fn = PortalConsts.InitDisk1;  //  kludge TODO
    	
		if (!fexists)
			return;
		
		myFile = new MTDisk(fn);
		
		this.m_MTDisk[1] = myFile;
*/
    
    
    }
    
    /**
     * Just return the latest value output
     */
    @Override
    public int IORead(int address) {
    	int retval = 0;
    	int port = address >> 8 & 0xff;
//    	if (port == 0)
    		port = address & 0xff;
    	int data = port;

        switch (data)
        {

        //***********************************************************************
        //*   If the value of the z80's memory, RAM[PC], is equal to 1 or 2,
        //* then first or second player input is requested.
        //***********************************************************************
            case 1:
            case 2:
                retval = 0;
                break;
            // below used by mtutor
            case 0x2a:
                retval = 0x37;
                break;
            case 0x2b:
                retval = 1;
                break;
            case 0xaa:  // new in level 4
                retval = 0x37 + 1;
                break;

            case 0xab:  // new in level 4
                retval = 0x01;
                break;

            case 0xae:          // cdc disk data port
                retval = 0;     // default
                switch (m_mtdrivefunc)
                {
                case 0:
                    // read next byte of data from disk
                    if ( m_MTDisk[m_mtDiskUnit&1] == null)
                    {
                    	/*
                    	if (m_MTDisk[2] == null)
                    	{
                    		break;
                    	}
                    	*/
                    	readcnt = 0;
                    	/*
                    	retval = m_MTDisk[2].ReadByte();
                        System.out.print(readcnt + "; " + retval + " : ");
                        */
                    	break;
                    }
                    else
                    {
                    	readcnt++;
                    	retval = m_MTDisk[m_mtDiskUnit&1].ReadByte();
                    }
                   // System.out.println(readcnt + ", " + retval);
                    break;
                case 2: // write data to disk - noop
                    break;
                case 11:
                    // read millisec clock - called twice - for lower and upper
                    {
                        int temp = (int)((java.lang.System.currentTimeMillis() - init_milli) / 1000);
                        retval = m_clockPhase ? (temp & 0xff) : ((temp << 8) & 0xff);
                        m_clockPhase = !m_clockPhase;
                    }
                    break;
                    
                case 4:
                    retval = m_mtsingledata;  // d.rcid
                    break;
                case 8:     // d.clear
                    break;
                default:
                    break;
                }
                break;
 
                // printf("CDC drive DATA responding to: %02x  with:  %02x\n", m_mtdrivefunc, retval);
                
            case 0xaf:          // cdc disk control port
                retval = m_mtcanresp;
                // printf("CDC drive control responding to: %02x  with:  %02x\n", m_mtdrivefunc, retval);
                switch (m_mtdrivefunc)
                {
                case 0:
                case 2:
                case 11:
                    m_mtcanresp = 0x50;
                    break;

                default:
                    break;

                }

                break;
            
        ///***********************************************************************
        //*   If the value of the z80's memory, RAM[PC], is not equal to any of
        //* the above cases, then the program requested bad input data. Debug
        //* information containing the value of the data is printed to STDOUT.
        //***********************************************************************
            default:
            	System.out.println("INp BAD -> Data = " + data);

                retval = 0;
                break;
        }
    	
        return retval;
    }


    
    @Override
    public void IOWrite(int address, int acc) {

    	   int comp = ~acc & 0xff;
    	   boolean ok = comp == m_mtdrivetemp;
    	
    	int port = address;  //   >> 8 & 0xff;
    	//if (port == 0)
    		port = address & 0xff;

    	   switch (port)
    	   {

    	   /***********************************************************************
    	   *   If the value of the z80's memory, RAM[PC], is equal to 2, the
    	   * content of the A register is moved into the left shift amount.
    	   ***********************************************************************/
    	       case 2:
    	       // below used by mtutor 
    	       case 0x2b:
    	           break;

    	       case 0xab:  // new in level 4

    	           break;

    	       case 0xae:      // CDC drive data port
    	           switch (m_mtdrivefunc)
    	           {
    	           case 0: // read disk
    	           case 2: // write data to disk - and recieve setup data
    	               // if (m_mtdrivefunc == 10)
    	               //    printf("CDC drive DATA WRITE");
    	               switch (m_mtDataPhase++)
    	               {
    	                   case 1:
    	                       m_mtDiskUnit = acc & 1;
    	                       break;
    	                   case 2:
    	                       m_mtDiskTrack = acc;
    	                       break;
    	                   case 3:
    	                       m_mtDiskSector = acc;
    	                       break;
    	                   case 4:
    	                       m_mtDisk1 = acc;
    	                       break;
    	                   case 5:
    	                       m_mtDisk2 = acc;
    	                       break;
    	                   case 6:
    	                       m_mtDiskCheck1 = acc;
    	                       break;
    	                   case 7: // 128 bytes/sector plus two check bytes
    	                       m_mtDiskCheck2 = acc;

    	                       m_mtSeekPos = (128 * 64 * m_mtDiskTrack) + (128 * (m_mtDiskSector-1));
    	                       if (m_mtSeekPos < 0)
    	                           break;
    	                       if ( m_MTDisk[m_mtDiskUnit&1] == null)
    	                       {
    	                    	//   System.out.println(">>>>>>> DISK NOT LOADED");
        	                       //m_MTDisk[2].Seek(m_mtSeekPos);
    	                       }
    	                       else
    	                    	   m_MTDisk[m_mtDiskUnit&1].Seek(m_mtSeekPos);
    	                       break;

    	                   default:   // write data
    	                       if ( m_MTDisk[m_mtDiskUnit&1] == null)
    	                    	   break;
    	                       m_MTDisk[m_mtDiskUnit&1].WriteByte(acc);  // TODO
    	                       m_mtcanresp = 0x50;
    	                       break;
    	               }
    	               break;

    	           case 10: // format
    	               switch (m_mtDataPhase++)
    	               {
    	               case 1:
    	                   m_mtDiskUnit = acc;
    	                   break;
    	               case 2:
    	                   m_mtDiskTrack = acc;
    	                   break;
    	               case 3:
    	                   m_mtDiskSector = acc;
    	                   break;
    	               case 4:
    	                   m_mtDisk1 = acc;
    	                   break;
    	               case 5:
    	                   m_mtDisk2 = acc;
    	                   break;
    	               default:
    	                   m_MTDisk[m_mtDiskUnit & 1].Format();
    	                   break;
    	               }
    	               break;

    	           // case 11:
    	           //    // read millisec clock - noop
    	           // break;

    	           // case 4:  // noop
    	           // case 8:  // d.clear - noop
    	           //    break;

    	           default:
    	               break;

    	           }
    	           // printf("CDC drive DATA recieving for: %02x  data:  %02x\n", m_mtdrivefunc, acc);
    	           break;

    	       case 0xaf:      // CDC drive control port
    	           if (ok)     // accept function
    	           {
    	               m_mtdrivefunc = m_mtdrivetemp;
    	               m_mtdrivetemp = 0xcb;
    	               m_mtDataPhase = 1;

    	               switch(m_mtdrivefunc)
    	               {
    	               case 0:
    	               case 2:
    	               case 10:
    	               case 11:
    	                   m_mtcanresp = 0x4a;
    	                   m_clockPhase = true;
    	                   break;

    	               case 4:
    	                   m_mtcanresp = 0x4a;
    	                   m_mtsingledata = 0x02;
    	                   if (this.m_MTDisk[1] != null)
    	                   {
    	                       m_mtsingledata |= 0x80;
    	                   }
    	                   break;

    	               case 8:
    	                   m_mtcanresp = 0x50;
    	                   break;

    	               default:
    	                   m_mtsingledata = 2;  // remove me
    	                   break;
    	               }
    	               // printf("CDC drive control accept: %02x\n", m_mtdrivefunc);
    	           }
    	           else
    	           {
    	               m_mtdrivetemp = acc; // set function
    	               // printf("CDC drive control command: %02x\n", m_mtdrivetemp);
    	               m_mtcanresp = 0x48;
    	           }
    	           
    	           break;


    	   /**
    	    *   If the value of the z80's memory, RAM[PC], is not equal to any of
    	    * the above cases, then the program requested bad output data.  Debug
    	    * information containing the value of the data and the accumulator are
    	    * printed to STDOUT.
    	    */
    	       default:
    	          // printf ("OUTp BAD -> Data = %d   A = %d\n", data, acc);
    	    	   
    	    	   System.out.println("OUTp BAD -> Data = " + (address & 0xff) + ", " + port + ", " + acc);

    	    	   
    	           break;
    	   }


    }
}
 