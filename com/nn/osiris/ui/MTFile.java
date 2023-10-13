package com.nn.osiris.ui;

import java.io.*;  

public class MTFile {
	private int _chkSum;
	private long position;
	private int rcnt;
	private int wcnt;
	private String filename;
	private File file;
	
	public String rwflag = "  ";
	
	
	public MTFile(String fn)
	{
		filename = fn;
		try
		{
			file = new File(filename);
			FileInputStream br=new FileInputStream(file); 
			br.close();
			return;
		}
		catch(Exception e)  
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
		catch(Exception e)  
		{  
			return false;
		}

	}
	
	public int ReadByte(long loc) 
	{
		int mybyte = -1;
		file = new File(filename);
		FileInputStream br=null; 
		try {
			br=new FileInputStream(file); 
			br.skip(loc);
			mybyte = br.read() & 0xff;
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return mybyte;
	}
	
	public int ReadByte() 
	{
		int mybyte = 0;

		return mybyte;
	}
	
	
	public boolean ReadSectors(int addr, long offset, int scount, PZ80Cpu cpu) 
	{
		int mybyte = -1;
		file = new File(filename);
		FileInputStream br=null; 
		
		try {
			br=new FileInputStream(file); 
			br.skip(offset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			for (int sectors = 0; sectors < scount; sectors++)
			{
				for (int bytes = 0; bytes < 128; bytes++)
				{
					mybyte = br.read() & 0xff;
					cpu.z80Memory.writeByte(addr++, mybyte);
				}
				// omit check bytes
				mybyte = br.read();
				mybyte = br.read();
			}
			br.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				br.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		} 
		
		
		return true;
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
	}
}
