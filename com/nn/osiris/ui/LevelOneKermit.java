/**
 * PROJECT	Portal panel
 * FILE		LevelOneKermit.java
 *
 *			(c) copyright 1999
 *			NCS NovaNET Learning
 *			
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

import java.io.*;

public final class LevelOneKermit
{
	/**
	 * Construct a level one kermit.
	 *
	 * @param	levelone_network	The level one network to use.
	 * @param	transmit_rate		Transmit rate for this transmission.
	 */
	public LevelOneKermit(
		LevelOneNetwork levelone_network,
		String current_dir,
		String temp_dir,
		int transmit_rate)
	{
		this.levelone_network = levelone_network;
		this.current_dir = current_dir;
		this.temp_dir = temp_dir;
		this.transmit_rate = transmit_rate;

		is_binary_mode = false;
		
		servefunc();
	}

	/**
	 * Destructor for level one kermit.
	 */
	public void destructor()
	{
		if (null != infile)
		{
			zclosi();
		}
		if (null != outfile)
		{
			zcloso(true);
		}
	}

	/**
	 * FSA that implements the kermit server. Return 1 to close server.
	 *
	 * @param
	 * @param
	 * @return
	 */
	public  boolean processPacket(
		byte[] packet,
		int packet_length)
	{
		int type,x;
	 
	 	rcvpkt = packet;
	 
		type = input (packet,packet_length);
		if (type == PACKET_TYPE_TIMEOUT)
			return false;

		switch (state)
		{
			// Awaiting a command.
			case SSERV:
				switch(type)
				{
					case PACKET_TYPE_INITIALIZE:
						spar(packet,rcv_data_offset,rcv_data_length);
						ack1(rpar());
						seq = 0;
						break;
					// Client wants to receive one of our files.
					case PACKET_TYPE_RECEIVE_INITIATION:
					{
						ByteArrayOutputStream baos = 
							new ByteArrayOutputStream();

						decstr(baos);	// Decode filename.
						filename = baos.toString();
						if (sinit('S') < 0)
							error("Parameter error.");
						else
						{
							filcnt = 0;
							state = SSFIL;
						}
						break;
					}

					// Client wants to send one of his files.
					case PACKET_TYPE_SEND_INITIATION:
						state = SRFIL;
						// Read client parameters.
						spar(packet,rcv_data_offset,rcv_data_length);
						ack1(rpar());	// Respond with our parameters.
						// Switch block type check.
						bctu = 3;
						break;
					case PACKET_TYPE_GENERIC:
					{
						ByteArrayOutputStream baos =
							new ByteArrayOutputStream();
						byte[] strbuf;

						decstr(baos);
						strbuf = baos.toByteArray();
						xpkt = true;		// Use X packets.
						
						switch(strbuf[0])
						{
							// Finish command.
							case 'F':
							// Logout command.
							case 'L':
								ack();
								return true;

							// Set or query variable.
							case 'V':
							{
								String arg1,arg2,arg3;

								arg1 = GetGenericArgument(
									strbuf,
									1,
									rcv_data_length,
									1);
								
								if (arg1.equals("S"))
								{
									arg2 = GetGenericArgument(
										strbuf,
										1,
										rcv_data_length,
										2);
									if (arg2.equals("FILE TYPE"))
									{
										arg3 = GetGenericArgument(
											strbuf,
											1,
											rcv_data_length,
											3);
										if (arg3.equals("BINARY"))
											is_binary_mode = true;
										else
											is_binary_mode = false;
									}
								}
								ack();	// Indicate sucess.
								servefunc();
								break;
							}
							default:
								error("Bad generic cmnd.");
								break;
						}
						break;
					}
					case PACKET_TYPE_ERROR:
						epmess();
						break;
					default:
						error("Unknown command");
						break;
				}
				break;

			// Send file state. Expecting to send file.
			case SSFIL:
				switch(type)
				{
					case PACKET_TYPE_ACK:
						if (filcnt++ == 0)
							spar(packet,rcv_data_offset,rcv_data_length);
						bctu = bctr;
						if (filcnt == 1)
						{
							if (gnfile() > 0)
							{
								if (sfile() < 0)
									error("File open error.");
								else
									state = SSDAT;
							}
							else
							{
								if (seot() < 0)
									error("Com failure.");
								else
									state = SSEOT;
							}
						}
						else
						{
							if (seot() < 0)
								error("Com failure.");
							else
								state = SSEOT;
						}
						break;
					case PACKET_TYPE_ERROR:
						epmess();
						break;
					default:
						error("Bad packet.");
				}
				break;

			// Send Data state. Sending file data.
			case SSDAT:
				switch(type)
				{
					case PACKET_TYPE_ACK:
						// Check for X or Z in ACK packet. If present, shut up.
						switch (packet[rcv_data_offset])
						{
							case 'X':
							case 'Z':
								abort = true;
						}
						if ((x = sdata()) == 0)
						{
							int result;

							if (abort)
							{
								byte[] abortdata = {(byte)'D'};

								result = seof(abortdata);
							}
							else
								result = seof(null);
							if (result < 0)
								error("File close error.");
							else
								state = SSFIL;
						}
						else if (x<0)
							error("Transmit error.");
						break;
					case PACKET_TYPE_ERROR:
						epmess();
						break;
					default:
						error("Bad packet.");
				}
				break;

			// Check for ACK after EOT sent.
			case SSEOT:
				switch(type)
				{
					case PACKET_TYPE_ACK:
						servefunc();
						break;
					case PACKET_TYPE_ERROR:
						epmess();
						break;
					default:
						error("Bad packet.");
				}
				break;

			// Receive file mode. Expecting to receive file.
			case SRFIL:
				switch(type)
				{
					case PACKET_TYPE_BREAK_TRANSMISSION:
						ack();
						servefunc();
						break;
					case PACKET_TYPE_FILE_HEADER:
						if (rcvfil() < 0)
						{
							error("File open error.");
						}
						else
						{
							ack();
							state = SRATT;
						}
						break;
					case PACKET_TYPE_ERROR:
						epmess();
						break;
					default:
						error("Bad packet.");
				}
				break;

			// Receiving attribute packets.
			case SRATT:
				if (type == PACKET_TYPE_FILE_ATTRIBUTES)
				{
					ack();
					break;
				}
				
			// Receiving data packets.
			case SRDAT:
				switch(type)
				{
					case PACKET_TYPE_DATA:
						state = SRDAT;
						if (decode() < 0)
							error("File write error.");
						else
						{
							if (abort)
							{
							byte[]	abortdata = {(byte) 'Z'};
								ack1 (abortdata);
							}
							else
								ack();
						}
						break;
					case PACKET_TYPE_END_OF_FILE:
						if (closof(packet[rcv_data_offset]) < 0)
							error("File close error.");
						else
						{
							ack();
							state = SRFIL;
						}
						break;
					case PACKET_TYPE_ERROR:
						epmess();
						break;
					default:
						error("Bad packet.");
						break;
				}
				break;
		}

		return false;
	}

	/** Level one network to be used. */
	private LevelOneNetwork levelone_network;
	/** Original directory to open files in */
	private String original_dir;
	/** Current directory to open files in */
	private String current_dir;
	private String temp_dir;
	/** Keys per second for kermit traffic. */
	private int transmit_rate;

	/** State of the kermit server. */
	private int state;
	/** Abort transfer. */
	private boolean abort;

	/** Used for eol conversions. */
	private boolean eatlf;
	/** Length of data in received packet. */
	private int rcv_data_length;
	/** Offset to data in received packet. */
	private int rcv_data_offset;
	/** Offset to end of data in recieved packet. */
	private int rcv_data_end_offset;

	/** Current packet number. */
 	private int seq;
	/** Current size of output packet data. */
	private int size;
	/** Previous output packet data size. */
	private int osize;
	/** Received packet sequence number. */
	private int rsn;
	/** Length of packet being sent. */
	private int sndpkl;
	/** Entire packet being sent. */
	private byte[] sndpkt = new byte[1500];
	/** Packet being received. */
	private byte[] rcvpkt;
	/** Packet data buffer. */
	private byte[] data = new byte [1500];

	/** Where to get characters being encoded (replacing isp). */
	private InputStream encode_source;

	/** Where to put characters being decoded (replacing osp). */
	private OutputStream decode_destination;

	/** Current filename in use by kermit. */
	private String filename;
	/** Flag to send X packets. */
	private boolean xpkt;
	/** File count. */
	private int filcnt;
	/** Flag for first input from file. */
	private int first;
	/** Is binary mode transfer. */
	private boolean is_binary_mode;
	/** Next packet X packet? */
	private boolean xflag;				/* next packet x packet? */

	/** Input file. */
	private FileInputStream infile;
	/** Output file. */
	private FileOutputStream outfile;

	/*
	 * SEND-INIT Parameters.
	 */
	/** Max data to send in a packet. */
	private int spsiz;
	/** Max data we will receive in packet. */
	private int rpsiz;
	/** Our timeout interval. */
	private int stimo;
	/** Remote timeout interval. */
	private int rtimo;
	/** How much padding to ask for. */
	private int spadn;
	/** How much padding to send. */
	private int rpadn;
	/** Padding character to send. */
	private int spadc;
	/** Padding character to ask for. */
	private int rpadc;
	/** End of line to send. */
	private int seol;
	/** End of line to ask for. */
	private int reol;
	/** Control prefix to send. */
	private int sctlq;
	/** Control prefix to ask for. */
	private int rctlq;
	
	/*
	 * Kermit options flags.
	 */
	/** Is 8-bit quoting. */
	private boolean ebqflg;
	/** Flag for negotiating option. */
	private int rqf;
	/** 8th bit quoting prefix. */
	private	int ebq;
	/** Received 8th bit quoting bid. */
	private	int rq;
	/** Send 8th bit quoting bid. */
	private int sq;

	/*
	 * Block check type:
	 *	1 -> 1 byte checksum
	 *	2 -> 2 byte checksum
	 *	3 -> 3 byte crc
	 */
	/** Block check type requested by client. */
	private int bctr;
	/** Block check type currently in use. */
	private int bctu;

	/** Is run-length encoding. */
	private boolean rptflg;
	/** Repeat prefix. */
	private int rptq;
	/** Current repeat count. */
	private int rpt;

	/** Capabilities byte. */
 	private int capas;
	/** Are attribute packets. */
	private boolean atcapu;
	/** Are long packets. */
	private boolean lpcapu;

	/*
	 * Variables used while encoding packets.
	 */
	/** Leftover bytes. */
	private byte[] remain = new byte[6];
	/** Static for gnchar. */
	private int getpktc;
	/** Used to do local/kermit text eol translate. */
	private int nextchar, lastchar;
	
	/**
	 * Packet types.
	 */
	private static final int PACKET_TYPE_ACK = 89;
	private static final int PACKET_TYPE_NACK = 78;
	private static final int PACKET_TYPE_SEND_INITIATION=83;
	private static final int PACKET_TYPE_INITIALIZE = 73;
	private static final int PACKET_TYPE_FILE_HEADER = 70;
	private static final int PACKET_TYPE_TEXT_HEADER = 88;
	private static final int PACKET_TYPE_FILE_ATTRIBUTES = 65;
	private static final int PACKET_TYPE_DATA = 68;
	private static final int PACKET_TYPE_END_OF_FILE = 90;
	private static final int PACKET_TYPE_BREAK_TRANSMISSION = 66;
	private static final int PACKET_TYPE_ERROR = 69;
	private static final int PACKET_TYPE_RECEIVE_INITIATION = 82;
	private static final int PACKET_TYPE_HOST_COMMAND = 67;
	private static final int PACKET_TYPE_KERMIT_COMMAND = 75;
	private static final int PACKET_TYPE_TIMEOUT = 84;
	private static final int PACKET_TYPE_CORRUPT = 81;
	private static final int PACKET_TYPE_GENERIC = 71;
	/** Pound sign. */
	private static final int DEFAULT_CONTROL_PREFIX = 35;
	/** Ampersnad. */
	private static final int DEFAULT_EIGHTH_BIT_PREFIX = 38;
	/** Tilde. */
	private static final int DEFAULT_RUN_LENGTH_PREFIX = 126;

	private static final int MAXPKT = 1450;

	/** Definitions for the capabilities stuff. */
	private static final int atcapb = 8;
	private static final int lpcapb = 2;

	/** FSA state definitions. */
	private static final int SSERV=1;
	private static final int SSGEN=2;
	private static final int SSFIL=3;
	private static final int SSDAT=4;
	private static final int SSEOT=5;
	private static final int SRFIL=6;
	private static final int SRDAT=7;
	private static final int XSTATE=8;
	private static final int SRATT=9;

	/**
	 *
	 * @param
	 */
	private byte tochar(int value)
	{
		return (byte) (value + 32);
	}

	/**
	 *
	 * @param
	 */
	private int unchar(byte value)
	{
		int unsigned_value;

		if (value < 0)
			unsigned_value = 127 - value;
		else
			unsigned_value = value;

		return unsigned_value - 32;
	}

	/**
	 *
	 * @param
	 */
	private byte ctl(int value)
	{
		return (byte) (value ^ 64);
	}

	/**
	 * Initialize for kermit server mode.
	 */
	private void servefunc()
	{
		state = SSERV;

		spsiz = 94;
		rpsiz = 94;
		stimo = 5;
		rtimo = 7;
		spadn = 0;
		rpadn = 0;
		spadc = 0;
		rpadc = 0;
		seol = 13;
		reol = 13;
		sctlq = DEFAULT_CONTROL_PREFIX;
		rctlq = DEFAULT_CONTROL_PREFIX;
		ebq = DEFAULT_EIGHTH_BIT_PREFIX;
		ebqflg = false;
		rqf = -1;
		rq = 0;
		sq = PACKET_TYPE_ACK;
		bctr = 1;
		bctu = 1;
		rptflg = false;
		rptq = DEFAULT_RUN_LENGTH_PREFIX;
		rpt = 0;
		capas = 0;

		tinit();
	}

	/**
	 * Decode received error packet, re-enter server mode.
	 */
	private void epmess()
	{
		ByteArrayOutputStream error_string = new ByteArrayOutputStream();

		zclosi();
		zcloso(true);
		decstr(error_string);
		if	(PortalConsts.is_debugging)
			System.out.println(error_string.toString());
		servefunc();
	}

	/**
	 * Send error packet, re-enter server mode.
	 *
	 * @param
	 */
	private void error(String s)
	{
		byte[] bytestring = s.getBytes();

		zclosi();
		zcloso (true);
		spack(PACKET_TYPE_ERROR,seq,bytestring.length,bytestring);
		servefunc();
	}

	/**
	 * Decode packet data to passed memory pointer.
	 *
	 * @param
	 */
	private void decstr(ByteArrayOutputStream baos)
	{
		OutputStream old_decode_destination = decode_destination;

		try
		{
			decode_destination = baos;
			decode();
		}
		finally
		{
			decode_destination = old_decode_destination;
		}
	}

	/**
	 * Send packet to host.
	 *
	 * @param
	 * @param
	 * @param
	 * @return	-1	Error.
	 *			0	Success.
	 */
	private int ttol(
		byte[] packet,
		int offset,
		int length)
	{
		int delay;		  	// Delay between blobs.
		int blob_size = 3;	// Size of blobs we send.

		// If we can send at a real baudrate, also use larger blobs.
		if (transmit_rate >= 1000)
			blob_size = 32;
		else if (transmit_rate < 1)
			transmit_rate = 20;

		delay = blob_size*1000/transmit_rate;

		for (int i = 0; i < length; i += blob_size)
		{
			int write_size;

			if (length-i >= blob_size)
				write_size = blob_size;
			else
				write_size = length-i;

			levelone_network.write(packet,offset+i,write_size,delay);
		}

		return 0;
	}

	/**
	 * Open input file.
	 *
	 * @param
	 * @return	-1	Error.
	 *			0	Success.
	 */
	private int zopeni(String filename)
	{
	String	dir = current_dir;

		if (filename.equals(".LOCALOS"))
		{
			dir = temp_dir;
			filename = "localos.tmp";
		}

		try
		{
			infile = new FileInputStream(new File(dir,filename));
		}
		catch (IOException e)
		{
			return -1;
		}

		if	(PortalConsts.is_debugging)
			System.out.println("kermit upload: "+filename);
		return 0;
	}

	/**
	 * Opens file for writing.
	 *
	 * @param
	 * @return	-1	Error.
	 *			0	Success.
	 */
	private int zopeno(String filename)
	{
		try
		{
			outfile = new FileOutputStream(new File(current_dir,filename));
		}
		catch (IOException e)
		{
			return -1;
		}
		if	(PortalConsts.is_debugging)
			System.out.println("kermit download: "+current_dir+"@"+filename);

		return 0;
	}

	/**
	 * Close input file.
	 *
	 * @return	-1	Error.
	 *			0	Success.
	 */
	private int zclosi()
	{
		if (null != infile)
		{
			try
			{
				infile.close();
			}
			catch (IOException e)
			{
				return -1;
			}
			finally
			{
				infile = null;
			}

			return 0;
		}
		else
			return -1;
	}

	/**
	 * Close output file. Delete if discard parameter non-zero.
	 *
	 * @param
	 * @return	-1	Error.
	 *			0	Success.
	 */
	private int zcloso(boolean discard) 
	{
		if (null != outfile)
		{
			try
			{
				outfile.close();
			}
			catch (IOException e)
			{
				return -1;
			}
			finally
			{
				outfile = null;
			}
			return 0;
		}
		else
			return -1;
	}

	/**
	 * Returns next character from input file.
	 *
	 * @param
	 * @return	-1		Error.
	 *			else	Next character.
	 */
	private int zgetc()
	{
		int c;

		if (nextchar != 0)
		{
			c = nextchar;
			nextchar = 0;
			return c;
		}

		try
		{
			c = infile.read();
		}
		catch (IOException e)
		{
			c = -1;
		}

		if (-1 == c)
			return -1;

		// kermit uses CRLF as end of line -- create the illusion
		if (!is_binary_mode)
		{
			// the following will make LF as eol emit cr-lf
			if (10 == c && 13 != lastchar)
			{
				c = 13;
				nextchar = 10;
			}
			// the following will make CRLF as eol not emit cr-lf-lf
			else if (10 == c)
			{
				lastchar = 10;
				return zgetc();
			}
			// the following will make CR or CRLF as eol emit cr-lf
			else if (13 == c)
				nextchar = 10;

			lastchar = c;
		}

		return c;
	}

	/**
	 * Writes character to output file. Returns -1 on error.
	 *
	 * @param
	 * @return	-1	Error.
	 *			0	Success.
	 */
	private int zputc(int c)
	{
	// unix+mac text files don't contain crs
		if	(!PortalConsts.is_windows)
		{
			if (!is_binary_mode)
			{
				if (13 == c)
				{
					c = 10;
					eatlf = true;
				}
				else if (10 == c && eatlf)
				{
					eatlf = false;
					return 0;
				}
			}
			eatlf = false;
		}

		try
		{
			outfile.write(c);
		}
		catch (IOException e)
		{
			return -1;
		}
		
		return 0;
	}

	/**
	 * Increment sequence number, update progress.
	 */
	private void nxtpkt()
	{
		seq = (seq + 1) & 63;
	}

	/**
	 * Initializes for new transaction.
	 */
	private void tinit()
	{
		// transfer not being aborted
		abort = false;
		// not eating linefeeds yet
		eatlf = false;

		// clear sequence number
		seq = 0;
		// clear buffers
		sndpkl = 0;
		// set check type to 1
		bctu = 1;
		// clear X-packet flag
		xpkt = false;
		xflag = false;
		// clear memory encode/decode ptrs
		decode_destination = null;
		encode_source = null;
		// clear next/last char
		nextchar = lastchar = 0;
		
		// eighth-bit prefix
		ebqflg = false;
		sq = 'Y';
		rqf = -1;
		
		// run-length encoding
		rptflg = false;
	}

	/**
	 * Put send init parameters into data array.
	 *
	 * @return
	 */
	private byte[] rpar()
	{
		byte[] packet = new byte[13];

		// max packet size
		packet[0] = tochar(rpsiz);
		// timeout
		packet[1] = tochar(rtimo);
		// padding request
		packet[2] = tochar(rpadn);
		// padding character
		packet[3] = ctl(rpadc);
		// end of line character
		packet[4] = tochar(reol);
		// control quote character
		packet[5] = (byte) DEFAULT_CONTROL_PREFIX;
		// 8th bit quoting flag
		packet[6] = (byte) sq;
		// block check type (CRC)
		packet[7] = (byte) (3 + '0');
		// run length encoding prefix
		if (rptflg)
			packet[8] = (byte) rptq;
		else
			packet[8] = (byte) DEFAULT_RUN_LENGTH_PREFIX;
		// set capabilites mask
		packet[9] = tochar(atcapb | lpcapb);
		// window size
		packet[10] = tochar(1);
		// max packet size
		packet[11] = tochar(MAXPKT / 95);
		packet[12] = tochar(MAXPKT % 95);

		return packet;
	}

	/**
	 * Decode other sides parameters packet.
	 *
	 * @param
	 * @param
	 * @param
	 */
	private void spar(
		byte[] packet,
		int offset,
		int length)
	{
		int x;
		
		// get limit on size of outbound packets
		spsiz = unchar(packet[offset]);
		if (spsiz > 94)
			spsiz = 94;
		
		// Timeout on inbound packets = unchar(packet[offset+1]
		// Outbound padding = unchar(packet[offset+2]]
		spadn = spadc = 0;
		if (length >= 3)
		{
			spadn = unchar(packet[offset+2]);
			// Outbound padding character = unchar(packet[offset+3]
			if (length >= 4)
				spadc = ctl(packet[offset+3]);
		}
		// Outbound packet terminator = unchar(packet[offset+4]
		if (length >= 5)
			seol = unchar(packet[offset+4]);
		if (seol < 2 || seol > 31)
			seol = 13;
		// Control prefix = packet[offset+5]
		if (length >= 6)
			x = packet[offset+5];
		else
			x = DEFAULT_CONTROL_PREFIX;
		if ((x > 32 && x < 63) || (x > 95 && x < 127))
			rctlq = x;
		else
			rctlq = DEFAULT_CONTROL_PREFIX;
		// 8th bit prefix = packet[offset+6]
		if (length >= 7)
			rq = packet[offset+6];
		else
			rq = 0;
		if (rq == PACKET_TYPE_ACK)
			rqf = 1;
		else if ((rq > 32 && rq < 63) || (rq > 95 && rq < 127))
			rqf = 2;
		else
			rqf = 0;
		
		switch (rqf)
		{
			case 0:
				ebqflg = false;
				break;
			case 1:
				break;
			case 2:
				if (ebqflg = (ebq == sq || sq == PACKET_TYPE_ACK))
					ebq = rq;
		}
		// Block check type = packet[offset+7]
		if (length >= 8)
			bctr = packet[offset+7]-'0';
		else
			bctr = 1;
		// Repeat count prefix = packet[offset+8]
		if (length >= 9)
		{
			rptq = packet[offset+8];
			rptflg = ((rptq > 32 && rptq < 63) || (rptq > 95 && rptq < 127));
		}
		else
			rptflg = false;
		// check extensions 
		atcapu = false;
		lpcapu = false;
		if (length >= 10)
		{
			x = unchar(packet[9]);
			atcapu = (x & atcapb) != 0;
			lpcapu = (x & lpcapb) != 0;

			for (x=9; (length > x) && (0 != (unchar(packet[x]) & 1)); x++);
			if (length > x+3)
				spsiz = unchar(packet[x+1])*95 + unchar(packet[x+2]);
		}
	}

	/**
	 * Gets next packet, returns type.
	 *
	 * @param
	 * @param
	 * @return
	 */
	private int input(
		byte[] packet,
		int packet_length)
	{
		int type,resends=0;

		for (;;)
		{
			type = rpack(packet,packet_length);

			if (type == PACKET_TYPE_TIMEOUT || 
				type == PACKET_TYPE_CORRUPT ||
				type == PACKET_TYPE_NACK ||
				rsn != seq)
			{
				// Ignore sequence number on G or E packets.
				if ((rsn != seq) &&
					((type==PACKET_TYPE_ERROR)||(type==PACKET_TYPE_GENERIC)))
				{
					seq = rsn;
					return type;
				}

				if (type == PACKET_TYPE_NACK && (rsn == ((seq+1) & 63)))
					// NAK for next packet is ACK for current packet. accept
					// it.
					return PACKET_TYPE_ACK;
				else
				{
					if (resends > 5)
						return PACKET_TYPE_TIMEOUT;
					resends++;
					resend();
				}
			}
			// Otherwise accept it and return.
			else
				return type;
		}
	}

	/**
	 * Send ack packet with no data.
	 *
	 * @return
	 */
	private int ack()
	{
		int x;
		
		x = spack(PACKET_TYPE_ACK,seq,0,null);
		nxtpkt();
		return x;
	}

	/**
	 * Send ack packet with data.
	 *
	 * @param
	 * @return
	 */
	private int ack1(byte[] s)
	{
		int  x;

		x = spack(PACKET_TYPE_ACK,seq,s.length,s);
		nxtpkt();
		return x;
	}

	/**
	 * Send nak packet.
	 *
	 * @return
	 */
	private int nak()
	{
		int x;

		x = spack(PACKET_TYPE_ACK,seq,0,null);
		return x;
	}

	/**
	 * Initiate a send.
	 *
	 * @param
	 * @return
	 */
	private int sinit(int c)
	{
		byte[] s = rpar();

		return (spack(c,seq,s.length,s));
	}

	/**
	 * Encode a string in memory.
	 *
	 * @param
	 * @return
	 */
	private int encstr(ByteArrayInputStream bais)
	{
		InputStream	old_encode_source = encode_source;

		try
		{
			first = 1;
			encode_source = bais;
			getpkt(94);
		}
		finally
		{
			encode_source = old_encode_source;
		}

		return size;
	}

	/**
	 * Opens a file and sends the header packet.
	 *
	 * @return
	 */
	private int sfile()
	{
		int x;

		if (zopeni(filename) < 0)
			return -1;

		// Setup to encode the local filename in a kermit packet.
		ByteArrayInputStream bais =
			new ByteArrayInputStream(filename.getBytes());

		x = encstr(bais);

		first = 1;
		nxtpkt();
		return spack(
			(xpkt ? PACKET_TYPE_TEXT_HEADER : PACKET_TYPE_FILE_HEADER),
			seq,
			x,
			data);
	}

	/**
	 * Get packet of data and send it.
	 *
	 * @return
	 */
	private int sdata()
	{
		int x,maxpacket;

		if (lpcapu)
			maxpacket = spsiz - bctu;
		else
			maxpacket = spsiz - bctu - 2;

		x = getpkt(maxpacket);
		if (0 == x)
			return 0;

		nxtpkt();
		if (spack(PACKET_TYPE_DATA,seq,x,data) < 0)
			return 0;
		else
			return x;
	}

	/**
	 * Closes input file and sends Z packet to client.
	 *
	 * @param
	 * @return
	 */
	private int seof(byte[] s)
	{
		if (zclosi() < 0)
			return -1;

		nxtpkt();
		if (null == s)
			return spack(PACKET_TYPE_END_OF_FILE,seq,0,s);
		else
			return spack(PACKET_TYPE_END_OF_FILE,seq,s.length,s);
	}
	
	/**
	 * Sends end of transaction packet to client.
	 *
	 * @return
	 */
	private int seot()
	{
		nxtpkt();
		return spack(PACKET_TYPE_BREAK_TRANSMISSION,seq,0,null);
	}

	/**
	 * Gets next file to upload.
	 *
	 * @return
	 */
	private int gnfile()
	{
		if (filcnt == 1)
			return 1;
		else
			return 0;
	}

	/**
	 * Prepare to receive file.
	 *
	 * @return
	 */
	private int rcvfil()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		decstr(baos);
		filename = baos.toString();

		return zopeno(filename);
	}

	/**
	 * Closes the output file, and discards it if parameter is D.
	 *
	 * @param
	 * @return
	 */
	private int closof(int discard)
	{
		if (xflag)
			return 0;

		if (zcloso(discard == 'D') < 0)
			return -1;
		else
			return 0;
	}

	/**
	 * Resend the last packet transmitted.
	 *
	 * @return
	 */
	private int resend()
	{
		int  x;
	   
		if (sndpkl > 0)
			x = ttol(sndpkt,0,sndpkl);
		else
			x = nak();

		return x;
	}

	/**
	 * Compute packet check type 1 (folded checksum).
	 *
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	private int computeCheckOne(
		byte[] packet,
		int offset,
		int length)
	{
		int s,t;

		s = computeCheckTwo(packet,offset,length);
		t = (((s & 192) >> 6) + s) & 63;
		return t;
	}

	/**
	 * Compute packet checksum.
	 *
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	private int computeCheckTwo(
		byte[] packet,
		int offset,
		int length)
	{
		int i,s;

		s = 0;
		for (i=0; i<length; i++)
			s += packet[offset+i] & 0x7f;

		return (s & 0xfff);
	}


	/**
	 * Compute CRC for packet.
	 *
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	private int compute_check_three(
		byte[] packet,
		int offset,
		int length)
	{
		int i,c,q,crc=0;

		for (i=0; i<length; i++)
		{
			c = packet[offset+i] & 0x7f;
			q = (crc ^ c) & 017;
			crc = (crc >> 4) ^ (q*010201);
			q = (crc ^ (c>>4)) & 017;
			crc = (crc >> 4) ^ (q*010201);
		}

		return (crc & 0xffff);
	}

	/**
	 * Reads a packet and returns the type.
	 *
	 * @param
	 * @param
	 * @return
	 */
	private int rpack(
		byte[] packet,
		int packet_length)
	{
		int type,len;
		int check_offset;

		// In case of failure.
		rsn = rcv_data_length = rcv_data_end_offset = -1;

		// Null terminate the packet.
		packet[packet_length] = 0;

		// Figure out packet format.
		len = unchar(packet[1]);
		if (len < 0)
			return 'Q';

		// If the normal length field is zero, then the packet is encoded
		// using the extended packout layout.
		if (0 == len)
			rcv_data_length = unchar(packet[4])*95 + unchar(packet[5]) - bctu;
		else
			rcv_data_length = len - bctu - 2;

		// If the packet length is negative, then the packet is invalid.
		if (rcv_data_length < 0)
			return 'Q';

		// rsn = sequence number.
		rsn = unchar(packet[2]);
		if (rsn < 0)
			return 'Q';

		// type = packet type. 
		type = packet[3];
		if (type < 32)
			return 'Q';

		// rcv_data_offset = pointer to data in packet.
		if (0 == len)
			rcv_data_offset = 7;
		else
			rcv_data_offset = 4;

		rcv_data_end_offset = rcv_data_offset+rcv_data_length-1;

		// Position of start of packet check bytes.
		check_offset = rcv_data_length + rcv_data_offset;

		// Perform checksum test on header (extended packets only).
		if (0 == len)
		{
			int received_header_checksum;
			int calculated_header_checksum;

			received_header_checksum = unchar(packet[6]);
			calculated_header_checksum = computeCheckOne(packet,1,5);
			if (received_header_checksum != calculated_header_checksum)
			{
				return 'Q';
			}
		}

		// Perform checksum test on entire packet.
		int received_packet_checksum = -1;
		int calculated_packet_checksum = -2;

		switch(bctu)
		{
			case 1:
				calculated_packet_checksum = computeCheckOne(
					packet,
					1,
					check_offset-1);
				received_packet_checksum = unchar(packet[check_offset]);
				break;
			case 2:
				calculated_packet_checksum = computeCheckTwo(
					packet,
					1,
					check_offset-1);
				received_packet_checksum = (unchar(packet[check_offset])<<6) |
					unchar(packet[check_offset+1]);
				break;
			case 3:
				calculated_packet_checksum = compute_check_three(
					packet,
					1,
					check_offset-1);
				received_packet_checksum = (unchar(packet[check_offset])<<12) |
					(unchar(packet[check_offset+1])<<6) |
					unchar(packet[check_offset+2]);
				break;
		}

		if (received_packet_checksum != calculated_packet_checksum)
			return 'Q';
		else
			return type;
	}


	/**
	 * Encapsulates data into kermit packet and sends it.
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	private int spack(
		int type,
		int n,
		int len,
		byte[] data)
	{
		int j,i=0;

		// Fill in packet header: mark, length, sequence number, type.
		sndpkt[i++] = 1;
		if (len+bctu+2 > 94)
			sndpkt[i++] = tochar(0);
		else
			sndpkt[i++] = tochar(len+bctu+2);

		sndpkt[i++] = tochar(n);
		sndpkt[i++] = (byte) type;
		if (len+bctu+2 > 94)
		{
			sndpkt[i++] = tochar((len+bctu) / 95);
			sndpkt[i++] = tochar((len+bctu) % 95);
			sndpkt[i] = 0;
			sndpkt[i++] = tochar(computeCheckOne(sndpkt,1,5));
		}

		// Copy data into packet.
		for (j=0; j<len; j++)
			sndpkt[i++] = data[j];

		// Add the appropriate block check bytes to the end.
		switch (bctu)
		{
			case 1:
				sndpkt[i++] = tochar(computeCheckOne(sndpkt,1,i-1));
				break;
			case 2:
				j = computeCheckTwo(sndpkt,1,i-1);
				sndpkt[i++] = tochar((j>>6) & 077);
				sndpkt[i++] = tochar(j & 077);
				break;
			case 3:
				j = compute_check_three(sndpkt,1,i-1);
				sndpkt[i++] = tochar((j>>12) & 017);
				sndpkt[i++] = tochar((j>>6) & 077);
				sndpkt[i++] = tochar(j & 077);
				break;
		}

		// Append CR and null terminate the string.
		sndpkt[i++] = (byte) seol;
		sndpkt[i] = 0;
		sndpkl = i;

		// Send the packet.
		i = ttol(sndpkt,0,sndpkl);
		return(i);
	}


	/**
	 * Encodes character into the global data array.
	 *
	 * @param
	 * @param
	 */
	private void encode(
		int a,
		int next)
	{
		int a7,b8;

		// Check for run for run length encoding.
		if (rptflg)
		{
			if (a == next)
			{
				if (++rpt < 94)
				{
					// Yes, count em.
					return;
				}
				else if (rpt == 94)
				{ 
					// If at maximum.
					data[size++] = (byte) rptq;
					data[size++] = tochar(rpt);
					rpt = 0;
				}
			}
			else if (rpt == 1)
			{
				rpt = 0;
				encode(a,-1);
				if (size <= 88)
					// Watch for boundary. 
					osize = size;
				rpt = 0;
				encode(a,-1);
				return;
			}
			else if (rpt > 1)
			{
				// Run broken, more than two?
				data[size++] = (byte) rptq;
				data[size++] = tochar(++rpt);
				rpt = 0;
			}
		}

		a7 = a & 127;
		b8 = a & 128;

		// If 8th bit set, need to add 8 bit prefix char.
		if (ebqflg && b8 != 0)
		{
			data[size++] = (byte) ebq;
			a = a7;
		}
		// Check for control character.
		if (a7 < 32 || a7 == 127)
		{
			data[size++] = (byte) sctlq;
			a = ctl(a);
		}
		// Check if sending control prefix, 8th bit prefix, RLL prefix.
		else if (a7 == sctlq)
			data[size++] = (byte) sctlq;
		else if (ebqflg && a7 == ebq)
			data[size++] = (byte) sctlq;
		else if (rptflg && a7 == rptq)
			data[size++] = (byte) sctlq;

		// Add in the actual character now.
		data[size++] = (byte) a;
		data[size] = 0;
	}

	/**
	 * Decode kermit encoded data.
	 *
	 * @return
	 */
	private int decode()
	{
		int a,a7,b8;

		while (rcv_data_offset <= rcv_data_end_offset)
		{
			a = rcvpkt[rcv_data_offset++];
			rpt = 1;	// repeat count

			// Check for repeat prefix.
			if (rptflg && a == rptq)
			{
				rpt = unchar(rcvpkt[rcv_data_offset++]);
				a = rcvpkt[rcv_data_offset++];
			}
			// Check for 8 bit prefix.
			b8 = 0;
			if (ebqflg && a == ebq)
			{
				b8 = 128;
				a = rcvpkt[rcv_data_offset++];
			}
			// Check for control prefix.
			if (a == rctlq)
			{
				a = rcvpkt[rcv_data_offset++];
				a7 = a & 127;
				if (a7 > 62 && a7 < 96)
					a = ctl(a);
			}
			a |= b8;
			for (; rpt > 0; rpt--)
				if (pnchar(a) < 0)
					return -1;
		}

		return 0;
	}

	/**
	 * Create a packet with up to the passed length.
	 *
	 * @param
	 * @return
	 */
	private int getpkt(int maxlen)
	{
		int i,next;

		// Do first time initialization.
		if (1 == first)
		{
			first = 0;
			remain[0] = 0;
			getpktc = gnchar();
			// Check for empty file.
			if (getpktc < 0)
			{
				first = -1;
				return(size = 0);
			}
		}
		else if (first == -1)
		{		/* EOF from last time ? */
			for (size = 0;(data[size] = remain[size]) != 0;size++);
			remain[0] = 0;
			return (size);
		}
		for (size = 0;(data[size] = remain[size]) != 0;size++);
		remain[0] = 0;

		// Initialize repeat counter.
		rpt = 0;
		while (first > -1)
		{
			next = gnchar();	// Look ahead one character.
			if  (next < 0)
				first = -1;		// If none, we are at EOF.
			osize = size;
			encode(getpktc,next);
			getpktc = next;

			if (size == maxlen)
				return(size);
			// If overfilled, must save some.
			if (size > maxlen)
			{
				for (i=0;(remain[i] = data[osize+i]) != 0; i++);
				size = osize;
			 	data[size] = 0;
				return(size);
			}
		}
		return(size);
	}

	/**
	 * Get next character to encode.
	 *
	 * @return
	 */
	private int gnchar()
	{
		int woof;

		if (null != encode_source)
		{
			try
			{
				woof = encode_source.read();
			}
			catch (IOException e)
			{
				woof = -1;
			}
		}
		else
		{
			woof = zgetc();
		}

		return woof;
	}

	/**
	 * Output decoded character.
	 *
	 * @param
	 * @return
	 */
	private int pnchar(int c)
	{
		if (xflag)
		{
			return 1;
		}
		else if (null != decode_destination)
		{
			try
			{
				decode_destination.write(c);
			}
			catch (IOException e)
			{
			}

			return 1;
		}
		else
			return(zputc(c));
	}

	/**
	 * Gets arguments for generic commands.
	 *
	 * Arguments are sent like:
	 *	byte 0 = length1 (encoded with tochar())
	 *	byte 1-length = arg1
	 *	byte length+1 = length2
	 *	byte length+2-.. = arg2
	 *
	 * @param
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	private String GetGenericArgument(
		byte[] packet,
		int offset,
		int length,
		int target_arg)
	{
		int current_arg = 1;
		int arg_length;

		for (;;)
		{
			// Exit if beyond packet length.
			if (offset >= length)
				return null;
			arg_length = unchar(packet[offset]);
			if (target_arg == current_arg)
				return new String(packet,offset+1,arg_length);
			
			// Increment argument number.
			current_arg++;
			// Point offset at next argument.
			offset += 1 + arg_length;
		}
	}
}
