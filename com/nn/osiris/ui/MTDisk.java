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
	
	public String rwflag = "  ";
	
	
	public MTDisk(String fn)
	{
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
	
	public boolean ReadSectors(int addr, long offset, int scount, PZ80Cpu cpu) 
	{
		int mybyte = -1;
		
		try {
			raf.seek(offset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			for (int sectors = 0; sectors < scount; sectors++)
			{
				for (int bytes = 0; bytes < 128; bytes++)
				{
					mybyte = raf.read();
					cpu.z80Memory.writeByte(addr++, mybyte);
				}
				// omit check bytes
				mybyte = raf.read();
				mybyte = raf.read();
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
		try {
			return raf.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public void WriteByte(int w)
	{
		try {
			raf.write(w);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
	
}
