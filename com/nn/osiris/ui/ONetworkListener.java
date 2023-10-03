/*
 * ONetworkListener.java
 *
 * Started 1999
 *
 * Copyright Pearson Digital Learning
 */

package com.nn.osiris.ui;

import java.util.EventListener;

/**
 * Interface for a listener for novaNET network connection status.
 */
public interface ONetworkListener extends EventListener
{
	/**
	 * Method to be called when the network is connected.
	 */
	public void networkConnected(final LevelOnePanel lop);

	/**
	 * Method to be called when the network is disconnected.
	 */
	public void networkDisconnected(final LevelOnePanel lop);

	/**
	 * Method to be called when the network connection attempt fails.
	 */
	public void networkConnectFailed(final LevelOnePanel lop,final String s);
}
