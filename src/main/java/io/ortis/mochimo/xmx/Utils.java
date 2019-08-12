/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package io.ortis.mochimo.xmx;

import java.math.BigInteger;
import java.util.Locale;

/**
 * @author Ortis
 */
public class Utils
{
	public static final int WOTS_STRING_LENGTH = 2208 * 2;
	public static final int TAG_STRING_LENGTH = 24;
	public static final int TAG_STRING_OFFSET = WOTS_STRING_LENGTH - TAG_STRING_LENGTH;
	public static String NO_TAG = "420000000e00000001000000";


	private final static char[] hexArray = "0123456789abcdef".toCharArray();

	public static byte[] hexToBytes(String hex)
	{
		StringBuilder sb = new StringBuilder(hex);
		if(hex.toUpperCase(Locale.ENGLISH).startsWith("0X"))
			sb = sb.delete(0, 2);

		if(hex.length() % 2 != 0)
			sb.insert(0, '0');

		hex = sb.toString();
		return fit(new BigInteger(hex, 16).toByteArray(), hex.length() >> 1);
	}

	public static String bytesToHex(final byte[] bytes)
	{
		return bytesToHex(bytes, 0, bytes.length);
	}

	public static String bytesToHex(final byte[] bytes, final int offset, final int length)
	{
		char[] hexChars = new char[length << 1];
		for(int j = 0; j < length; j++)
		{
			int v = bytes[j + offset] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] fit(final byte[] bytes, final int length)
	{
		final int diff = bytes.length - length;
		final byte[] fit = new byte[length];

		if(diff > 0)
		{// bytes > fit

			// check that all byte to be trimed are 0
			final byte[] trim = new byte[diff];
			System.arraycopy(bytes, 0, trim, 0, diff);

			final BigInteger bi = new BigInteger(trim);
			if(bi.compareTo(BigInteger.ZERO) != 0)
				throw new IllegalArgumentException("Bytes array truncated (buffer overflow)");

			System.arraycopy(bytes, diff, fit, 0, length);
		}
		else
			System.arraycopy(bytes, 0, fit, -diff, bytes.length); // bytes < fit

		return fit;
	}


}
