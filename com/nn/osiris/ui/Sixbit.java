/**
 * PROJECT	Portal panel
 * FILE		Sixbit.java
 *
 *			(c) copyright 2000
 *			NCS NovaNET Learning
 *
 * @author	J Hegarty
 */

package com.nn.osiris.ui;

/**
 * Class for manipulating legacy string data. All data is manipulated using
 * the Java long datatype, which represent 60 bits ones complement data
 * containing 6 bit codes for the legacy CDC mainframes.
 */
public final class Sixbit
{
	/**
	 * Converts a single TUTOR word to an ASCII string.
	 *
	 * @param 	input	Word to be converted.
	 * @return			ASCII representation.
	 */
	public static final String displayToSixBitAscii(long input)
	{
		long sixbit_code;
		StringBuffer result = new StringBuffer();

		for (int shift = 54; shift > -1; shift -= 6)
		{
			sixbit_code = (input >> shift) & 077L;
			if (0 == sixbit_code)
				break;
			result.append(pa6[(int)sixbit_code]);
		}
		
		return result.toString();
	}

	/**
	 * Converts multiple TUTOR words to an ASCII string.
	 *
	 * @param	input	Words to be converted.
	 * @param 	start	Offset into input to start conversion.
	 * @param 	length	Maximum characters to convert.
	 * @return			ASCII representation.
	 */
	public static final String displayToSixBitAscii(
		long[] input,
		int start,
		int length)
	{
		long sixbit_code;
		StringBuffer result = new StringBuffer();
		
		for (int shift = 54, offset = 0;
			length > 0;
			length--, shift -= 6)
		{
			if (shift < 0)
			{
				offset++;
				shift = 54;
			}

			sixbit_code = (input[start+offset] >> shift) & 077L;
			if (0 == sixbit_code)
				break;
			result.append(pa6[(int)sixbit_code]);
		}
		
		return result.toString();
	}
	
	/**
	 * Converts multiple TUTOR words to an ASCII string.
	 *
	 * @param 	input	Words to be converted.
	 * @param 	length	Maximum characters to convert.
	 * @return 			ASCII representation.
	 */
	public static final String displayToSixBitAscii(
		long[] input,
		int length)
	{
		return displayToSixBitAscii(input, 0, length);
	}
	
	/**
	 * Converts ASCII string to TUTOR words.
	 *
	 * @param 	input	ASCII string to be converted.
	 * @param 	output	Destination for TUTOR data.
	 * @param 	start	Offset into destination to start conversion.
	 * @param 	length	Maximum characters to produce.
	 */
	public static final void sixBitAsciiToDisplay(
		String input,
		long[] output,
		int start,
		int length)
	{
		int max = input.length();
		int	shift = 54;
		
		for (int i = 0;
			i < max && length> 0;
			i++, length--)
		{
			// Initialize each new output word.
			if (54 == shift)
				output[start] = 0;
			
			// Or in the translation of the next character.
			long	temp = ap6[input.charAt(i)];

			output[start] |= temp << shift;
			
			// Advance to next word if this one is full.
			if (0 == shift)
			{
				shift = 54;
				start++;
			}
			// Decrement the shift.
			else
				shift -= 6;
		}
	}
	
	/**
	 * Converts ASCII string to TUTOR word.
	 *
	 * @param	input	ASCII string to be converted.
	 * @param 	length	Maximum characters to produce.
	 */
	public static final long sixBitAsciiToDisplay(
		String input,
		int length)
	{
		int max = input.length();
		int	shift = 54;
		long output = 0;

		for (int i = 0;
			i < max && length> 0;
			i++, length--)
		{
			// Or in the translation of the next character.
			long	temp = ap6[input.charAt(i)];
			
			output |= temp << shift;
			
			// Decrement the shift.
			shift -= 6;
		}
		
		return output;
	}

	// Conversion table from CDC 6 bit display codes to CDC 6 bit ascii.
	public static final char pa6[] =
	{
		':','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
		'q','r','s','t','u','v','w','x','y','z',
		'0','1','2','3','4','5','6','7','8','9',
		'+','-','*','/','(',')','$','=',' ',',','.','#','[',']','%',
		'"','_','!','&','\'','?','<','>','@','\\','^',';'
	};

	// Conversion table from CDC 6 bit ascii to CDC 6 bit display codes.
	private static final byte ap6[] =
	{
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		055, 066, 064, 060, 053, 063, 067, 070,
		051, 052, 047, 045, 056, 046, 057, 050,
		27, 28, 29, 30, 31, 32, 33, 34,
		35, 36, 00, 077, 072, 054, 073, 071,
		074, 001, 002, 003, 004, 005, 006, 007,
		010, 011, 012, 013, 014, 015, 016, 017,
		020, 021, 022, 023, 024, 025, 026, 027,
		030, 031, 032, 061, 075, 062, 076, 065,
		0, 1, 2, 3, 4, 5, 6, 7,
		8, 9, 10, 11, 12, 13, 14, 15,
		16, 17, 18, 19, 20, 21, 22, 23,
		24, 25, 26, 0, 0, 0, 0, 0
	};
}
