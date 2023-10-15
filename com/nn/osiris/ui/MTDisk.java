package com.nn.osiris.ui;

import java.io.*;  

public class MTDisk {
	private int _chkSum;
	private long position;
	private int rcnt;
	private int wcnt;
	private String filename;
	private File file;
	private RandomAccessFile raf;
	private String rwflag;
	
	
	
	public MTDisk(String fn)
	{
		rwflag = "  ";
	    _chkSum = 0;
		position = 0;
	    rcnt = 0;
	    wcnt = 0;
	    file = null;
		filename = fn;
		try
		{
			file = new File(filename);
			raf = new RandomAccessFile(file, "rw"); 

			return;
		}
		catch(IOException e)  
		{  
			e.printStackTrace();
			filename = null;
			file = null;
		}  
		file = null;
	}
	
	public static boolean Exists(String fn)
	{
		try
		{
			File filex = new File(fn);
			FileInputStream br=new FileInputStream(filex); 
			br.close();
			return filex != null;
		}
		catch(IOException e)  
		{  
			return false;
		}

	}
	
	public void Close()
	{
		try {
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean ReadSectorsForBoot(int addr, long offset, int scount, PZ80Cpu cpu) 
	{
		int mybyte = -1;
		
		Seek(offset);
		
		try {
			for (int sectors = 0; sectors < scount; sectors++)
			{
				for (int bytes = 0; bytes < 128; bytes++)
				{
					mybyte = ReadByte();
					cpu.z80Memory.writeByte(addr++, mybyte);
				}
				// omit check bytes
				mybyte = ReadByte();
				mybyte = ReadByte();
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		
		return true;
	}
	
	public int ReadByte()
	{
		int myByte;
		
		// return checksum
        if (rcnt == 129)                              
        {                                           
            rcnt++;                                 
            return (_chkSum & 0xff);            
        }                                           
      else if (rcnt == 130)                         
        {                                           
            int ret = ((_chkSum >> 8) & 0xff);   
            ReadReset ();                           
            return ret;                             
        }

		try {
			myByte  = raf.read();
	        position++;
	        rcnt++;
	        CalcCheck (myByte);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		return myByte;
	}

	public void WriteByte(int w)
	{
	    if (wcnt > 129)
	    {
	        WriteReset ();
	        return;
	    }
	    if (wcnt > 128)
	    {
	        wcnt++;
	        return;
	    }
	    
		try {
			raf.write(w);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        wcnt++;
        if (wcnt > 130)
            WriteReset ();

        position++;

	}

	
	public int ReadByte(long offset)
	{
		int mybyte = -1;

		try {
			raf.seek(offset);
			return raf.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return mybyte;
		}
	}
	
	public void ReadReset()
	{
		rcnt = 1;
	    _chkSum = 0;
	    rwflag = "R ";
	}
	
	public void WriteReset()
	{
		wcnt = 1;
	    rwflag = "W ";
	}
	
	public void Seek(long loc)
	{
		rcnt = wcnt = 1;
		position = loc;
		
		try {
			raf.seek(loc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
		
	
	void CalcCheck (int b)
	{
	    int cupper = ((_chkSum >> 8) & 0xff);
	    int clower = (_chkSum & 0xff);
	    cupper ^= b;
	    int x = cupper << 1;
	    if ((x & 0x100) > 0)
	        x = (x | 1) & 0xff;
	    cupper = x;
	    clower ^= b;
	    int y = 0;
	    if ((clower & 1) == 1)
	        y = 0x80;
	    x = clower >> 1;
	    x = (x | y) & 0xff;
	    clower = x;
	    _chkSum = (((cupper << 8) & 0xff00) | (clower & 0xff));
	}

	public void Format()
	{
		rwflag = "W ";	
		Seek(0);
		
	    for (long i = 0; i < (128L * 64L * 154L); i++)
		{
	    	try {
				raf.write(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
