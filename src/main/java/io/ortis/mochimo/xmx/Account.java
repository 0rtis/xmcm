/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package io.ortis.mochimo.xmx;

/**
 * @author Ortis
 */
public class Account
{
	private final String wots;
	private final String secret;


	public Account(final String wots, final String secret)
	{
		this.wots = wots;
		this.secret = secret;
	}

	public String getTaggedWots(final String tag)
	{
		if(tag.length() != Utils.TAG_STRING_LENGTH)
			throw new IllegalArgumentException("Invalid tag");

		final StringBuilder sb = new StringBuilder(this.wots.substring(0, 2208 * 2 - 24));

		sb.append(tag);

		return sb.toString();

	}

	public String getWots()
	{
		return wots;
	}

	public String getSecret()
	{
		return secret;
	}

	@Override
	public String toString()
	{
		return "wots="+this.wots.substring(0, 64)+", secret="+secret;
	}
}
