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

/**
 * @author Ortis
 */
public class BlockchainEntry
{

	private final String wots;
	private final String tag;
	private final BigInteger balance;
	private final boolean valid;

	public BlockchainEntry(final String wots, final String tag, final BigInteger balance, final boolean valid)
	{
		this.wots = wots;
		this.tag = tag;
		this.balance = balance;
		this.valid = valid;
	}

	public boolean isValid()
	{
		return valid;
	}

	public String getWots()
	{
		return wots;
	}

	public String getTag()
	{
		return tag;
	}

	public BigInteger getBalance()
	{
		return balance;
	}

	@Override
	public String toString()
	{
		return "wots=" + (this.wots == null ? null : this.wots.substring(0, 64)) + ", tag=" + this.tag + ", balance=" + this.balance;
	}
}
