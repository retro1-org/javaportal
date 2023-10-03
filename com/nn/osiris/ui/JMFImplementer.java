package com.nn.osiris.ui;

import javax.media.*;
import java.io.*;

class JMFImplementer extends JMFInterface implements ControllerListener
{
	LevelOneParser	engine;
	private Player sound_player;

	public JMFImplementer()
	{
	}

	boolean hasJMF()
	{
		return true;
	}

	void setEngine(LevelOneParser engine)
	{
		this.engine = engine;
	}

	public synchronized void controllerUpdate(ControllerEvent event)
	{
		if (null == sound_player)
			return;

		if (event instanceof EndOfMediaEvent)
		{
			sound_player.stop();
			sound_player.deallocate();
			sound_player = null;
			engine.SendUsm(021);
		}
	}

	void soundStart(String sound_url)
	{
		if (null != sound_player)
		{
			sound_player.stop();
			sound_player.deallocate();
			sound_player = null;
		}

		try
		{
		java.net.URL	url = new java.net.URL(sound_url);

			if ((sound_player = Manager.createRealizedPlayer(url)) != null)
			{
				if	(PortalConsts.is_debugging)
					System.out.println("created soundplayer");
				sound_player.addControllerListener(this);
				sound_player.start();
				engine.SendUsm(020);
				return;
			}
		}
		catch (java.net.MalformedURLException e1)
		{
		}
		catch (IOException e2)
		{
		}
		catch (javax.media.NoPlayerException e3)
		{
		}
		catch (javax.media.CannotRealizeException e4)
		{
		}

		engine.SendUsm(027);
	}

	void soundStop()
	{
		if (null != sound_player)
		{
			sound_player.stop();
			sound_player.deallocate();
			sound_player = null;
			engine.SendUsm(025);
		}
	}

	void soundPause()
	{
		if (null != sound_player)
		{
			sound_player.stop();
			engine.SendUsm(022);
		}
	}

	void soundResume()
	{
		if (null != sound_player)
		{
			sound_player.start();
			engine.SendUsm(024);
		}
	}
}
