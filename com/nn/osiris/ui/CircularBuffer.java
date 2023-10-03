/**
 * PROJECT	Portal panel
 * FILE		CircularBuffer.java
 *
 *			(c) copyright 1999
 *			NCS NovaNET Learning
 *			
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

/**
 * This class represents a circular buffer.
 */
public final class CircularBuffer
{
	/** The buffer for the curcular buffer. */
	public byte[] buffer;

	/**
	 * Constructor for circular buffer object.
	 *
	 * @param	size	The size for the circular buffer.
	 */
	public CircularBuffer(int size)
	{
		buffer = new byte[size];
		inpoint = 0;
		outpoint = 0;
	}

	/**
	 * Returns number of bytes queued in circular buffer.
	 *
	 * @return		Number of bytes queued in circular buffer.
	 */
	public int BytesQueued()
	{
		int	queued = inpoint-outpoint;
		
		if (queued < 0)
			queued += buffer.length;
		
		return queued;
	}
	
	/**
	 * Returns number of bytes that are not used.
	 *
	 * @return		Number of bytes that are not used.
	 */
	public int BytesFree()
	{
		return buffer.length-BytesQueued()-1;
	}

	/**
	 * Returns number of bytes that could be enqueued via block copy.
	 *
	 * @return		Number of bytes that could be enqueued via block copy.
	 */
	public int EnqueueBufferLimit()
	{
		if (inpoint >= outpoint)
		{
			int	limit = buffer.length-inpoint;
			
			if (0 == outpoint)
				limit--;
			return limit;
		}
		else
			return outpoint-inpoint-1;
	}

	/**
	 * Returns the number of bytes that could be dequeued via block copy.
	 *
	 * @return		Number of bytes that could be dequeued via block copy.
	 */
	public int DequeueBufferLimit()
	{
		int	limit = 0;
		int queued = 0;

		if (outpoint >= inpoint)
			limit = buffer.length-outpoint;
		else
			limit = inpoint-outpoint;
		
		queued = BytesQueued();
		return (limit < queued ? limit : queued);
	}

	/**
	 * Enqueues bytes from passed buffer.
	 *
	 * @param	data	The data we want to enqueue.
	 * @param	offset	The offset.
	 * @param	length	The length of the data.
	 * @return			Number of bytes actually enqueued.
	 */
	public int Enqueue(
		byte[] data,
		int offset,
		int length)
	{
		int nlength = (length < BytesFree() ? length : BytesFree());

		for (int i = 0;
			i < nlength;
			i++, inpoint = (inpoint+1)%buffer.length)
		{
			buffer[inpoint] = data[i+offset];
		}
		
		return nlength;
	}

	/**
	 * Enqueues one byte of data.
	 *
	 * @param	data	The one-byte data we want to enqueue.
	 * @return			Number of bytes actually enqueued.
	 */
	public int Enqueue(int data)
	{
		if (0 != BytesFree())
		{
			buffer[inpoint] = (byte)data;
			inpoint = (inpoint+1)%buffer.length;
			return 1;
		}
		else
			return 0;
	}

	/**
	 * Enqueues the specified number of zeroes.
	 *
	 * @param	length	Number of zeroes to enqueue.
	 * @return			Number of zeroes actually enqueued.
	 */
	public int EnqueueZeroes(int length)
	{
		int	nlength = (length < BytesFree() ? length : BytesFree());
		
		for (int i = 0;
			i < nlength;
			i++, inpoint = (inpoint+1)%buffer.length)
		{
			buffer[inpoint] = 0;
		}
		
		return nlength;
	}

	/**
	 * Dequeues bytes to the passed buffer.
	 *
	 * @param	data	Place to stored dequeued data.
	 * @param	offset	The offset.
	 * @param	length	Number of bytes we want to dequeue.
	 * @return			Number of bytes actually dequeued.
	 */
	public int Dequeue(byte[] data,int offset,int length)
	{
		int nlength = (length < BytesQueued() ? length : BytesQueued());
		
		for (int i = 0;
			i<nlength;
			i++, outpoint = (outpoint+1)%buffer.length)
		{
			data[i+offset] = buffer[outpoint];
		}
		
		return nlength;
	}

	/**
	 * Dequeues one byte and returns it.
	 *
	 * @return	-1	Nothing was dequeued.
	 *				One byte of dequeued data.
	 */
	public int Dequeue()
	{
		if (inpoint != outpoint)
		{
			byte temp = buffer[outpoint];
			
			outpoint = (outpoint+1)%buffer.length;
			if (temp < 0)
				return 256+temp;
			else
				return temp;
		}
		else
			return -1;
	}

	/**
	 * Re-queues just unqueued data.
	 *
	 * @param	length	Number of bytes to undequeue.
	 */
	public void UnDequeue(int length)
	{
		if (outpoint < length)
			outpoint = outpoint-length+buffer.length;
		else
			outpoint = outpoint-length;
	}

	/**
	 * Returns a pointer to a buffer that can be copied to to enqueue data.
	 *
	 * @return		An index to a buffer that can be copied to to enqueue data.
	 */
	public int GetEnqueueBuffer()
	{
		return inpoint;
	}

	/**
	 * Returns a pointer to a buffer that can be copied from to dequeue data.
	 *
	 * @return		An index to a buffer than can be copied from to dequeue
	 *				data.
	 */
	public int GetDequeueBuffer()
	{
		return outpoint;
	}

	/**
	 * Companion for GetEnqueueBuffer.
	 *
	 * @param	number	Number of bytes.
	 */
	public void EnqueuedBytes(int number)
	{
		inpoint = (inpoint+number)%buffer.length;
	}

	/**
	 * Companion for GetDequeueBuffer.
	 */
	public void DequeuedBytes(int number)
	{
		outpoint = (outpoint+number)% buffer.length;
	}
	
	/**
	 * Drops any current queue contents.
	 */	
	public void EmptyQueue()
	{
		outpoint = inpoint;
	}

	/**
	 * Destructor for the class.
	 */
	protected void finalize() throws java.lang.Throwable
	{
		buffer = null;
		super.finalize();
	}

	/** The in point for the circular buffer. */
	private int inpoint;
	/** The out point for the circular buffer. */
	private int	outpoint;
}
