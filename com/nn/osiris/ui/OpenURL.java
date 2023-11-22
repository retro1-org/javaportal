package com.nn.osiris.ui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Desktop;

public class OpenURL 
{
	public static void go(String url)
	{
	      if (Desktop.isDesktopSupported()) {
	          
	          //making a desktop object
	          Desktop desktop = Desktop.getDesktop();
	          try {
	             URI uri = new URI(url);
	             desktop.browse(uri);
	          } catch (IOException excp) {
	             excp.printStackTrace();
	          } catch (URISyntaxException excp) {
	             excp.printStackTrace();
	          }		
	      }
	}
}
	