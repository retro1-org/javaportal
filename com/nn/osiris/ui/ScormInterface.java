package com.nn.osiris.ui;

import java.io.*;

class ScormInterface
{
	long rstartu,rstartl,lstatus;
	long [] studvars;
	static final long cybermagic = 0100000000000000000000L;

    public ScormInterface()
	{
		if	(PortalConsts.is_debugging)System.out.println("Using ScormInterface");
	}
	public void init(javax.swing.JApplet papplet)
	{
//		ScormApiImp.InitInstance(papplet);
//		ScormApiImp.GetInstance().initialize();
	}
	public String getLesson()
	{
//	String lesson = ScormApiImp.GetInstance().getLaunchData("lesson");
//		return "eng2";
//		return "eng2";
		return null;
	}
	public String getArg1()
	{
//	String arg1 = ScormApiImp.GetInstance().getLaunchData("arg1");
//		return "0100000000000000000666";
		return null;
	}
	public String getArg2()
	{
//	String arg2 = ((ScormApiImpl)ScormApiImpl.GetInstance()).getLaunchData("arg2");
		return null;
	}
	public String getSuspendData()
	{
//	String suspendData = ScormApiImpl.GetInstance().getSuspendData();
		try
		{
		FileInputStream	fis = new FileInputStream("suspend_data");
		byte[]	contents = new byte[8192];
		int		len;

			len = fis.read(contents);
			fis.close();
			System.out.println("read suspend data, len="+len);
			return new String(contents,0,len,"UTF-8");
		}
		catch (java.lang.Exception e1)
		{
		}
		return null;
	}
	public void setStudentVariables(long[] studvars)
	{
		this.studvars = studvars;
	}
	public void signout()
	{
	StringBuffer	sb = new StringBuffer();

		sb.append(Long.toOctalString(lstatus|cybermagic));
		sb.append(Long.toOctalString(rstartl|cybermagic));
		sb.append(Long.toOctalString(rstartu|cybermagic));
		for (int i=0;i<150;i++)
			sb.append(Long.toOctalString(studvars[i]|cybermagic));
		try
		{
       	FileOutputStream    trap_file = new FileOutputStream("suspend_data");

			trap_file.write(sb.toString().getBytes());
			trap_file.close();
			System.out.println("wrote suspend data");
		}
		catch (java.lang.Exception e1)
		{
		}
	}
	public void setLdone(int value)
	{ // -1=complete,0=incomplete,1=no*end
	}
	public void setStatus(long value)
	{ // random 60 bits of data for suspending
		lstatus = value;
	}
	public void setScore(int value)
	{ // this gets sent to scorm as a fraction
	//ScormApiImpl.GetInstance().setObjectiveMeasure(value/100.0);
		System.out.println("Scorm.setScore("+value+")");
	}
	public void setRestart(long lesson,long unit)
	{ // random 120 bits of data for suspending
		rstartl = lesson;
		rstartu = unit;
	}
}
