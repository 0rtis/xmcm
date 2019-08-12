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
public class TagAccount
{
	private final String tag;
	private Account account;
	private BigInteger transactedBlock = null;
	private Account nextAccount;


	public TagAccount(final String tag, final Account account)
	{
		this.tag = tag;
		this.account = account;
	}

	public void transacted(final BigInteger height, final Account nextAccount)
	{
		this.transactedBlock = height;
		this.nextAccount = nextAccount;
	}

	public void transactionCompleted()
	{
		this.transactedBlock = null;
		this.account = this.nextAccount;
		this.nextAccount = null;
	}

	public BigInteger getTransactedBlock()
	{
		return transactedBlock;
	}


	public Account getAccount()
	{
		return account;
	}


	public Account getNextAccount()
	{
		return nextAccount;
	}

	public String getTag()
	{
		return tag;
	}

	public String getTaggedWOTS()
	{
		if(this.tag == null)
			throw new IllegalStateException("Tag not set");

		return this.account.getTaggedWots(this.tag);

	}


	@Override
	public String toString()
	{
		return this.tag;
	}
}
