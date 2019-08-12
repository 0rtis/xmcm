/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package io.ortis.mochimo.xmx;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ortis
 */
public class Exchange implements Runnable
{

	private final SecureRandom RANDOM = new SecureRandom();
	private final BigInteger MIN_BALANCE = BigInteger.valueOf(501);
	private final BigInteger TX_FEE = BigInteger.valueOf(500);

	private final TagAccount exchangeWallet;
	private final MojoWrapper mojo;
	private final BackendAPIWrapper api;
	private final Fountain fountain;

	private final LinkedList<Account> reserve = new LinkedList<>();//Should be an offline secure database
	private final Map<String, TagAccount> userAccounts = new HashMap<>();
	private BigInteger block;


	public Exchange(final TagAccount exchangeWallet, final MojoWrapper mojo, final BackendAPIWrapper api, final Fountain fountain) throws IOException, Exception
	{
		this.exchangeWallet = exchangeWallet;
		this.mojo = mojo;
		this.api = api;
		this.fountain = fountain;

		this.block = this.api.getBlock();
	}

	@Override
	public void run()
	{
		try
		{
			while(!Thread.interrupted())
			{
				synchronized(this)
				{
					//check reserve
					if(this.reserve.size() < 100)
					{
						System.out.println("Expanding account reserve");
						//expand reserve of wots account
						final List<Account> newAccounts = mojo.generateAccounts(100);
						this.reserve.addAll(newAccounts);
						System.out.println("Reserve size: " + this.reserve.size());
					}

					this.block = this.api.getBlock();

					//handle deposit
					for(final TagAccount userAccount : this.userAccounts.values())
					{
						final BlockchainEntry bce = this.api.getBlockchainEntry(userAccount.getTag());

						if(!bce.isValid())
							break;

						if(bce.getWots() == null)
						{
							System.out.println("User account [" + userAccount + "] not activated yet");
							continue;
						}

						if(userAccount.getTransactedBlock() != null)
						{
							if(userAccount.getNextAccount() == null)
							{
								System.out.println("User account [" + userAccount + "] has been activated");
								userAccount.transacted(null, null);
							}
							else if(bce.getBalance().compareTo(MIN_BALANCE) == 0)
							{
								System.out.println("Balance of user account [" + userAccount + "] has been transferred to exchange wallet");
								userAccount.transactionCompleted();
							}
						}
						else
						{
							if(bce.getBalance().compareTo(MIN_BALANCE.add(TX_FEE)) > 0)
							{
								final BigInteger payment = bce.getBalance().subtract(MIN_BALANCE).subtract(TX_FEE);
								if(payment.compareTo(TX_FEE) > 0)
								{
									System.out.println("Spending user account [" + userAccount + "] to exchange wallet");
									final Account change = this.reserve.poll();
									if(change == null)
										throw new IllegalStateException("Reserve is empty");

									final byte[] tx = this.mojo.sign(userAccount.getTaggedWOTS(), userAccount.getAccount().getSecret(),
											this.exchangeWallet.getTaggedWOTS(), change.getTaggedWots(userAccount.getTag()), bce.getBalance(), payment);

									api.pushTransaction(tx);
									userAccount.transacted(block, change);
								}
							}
						}
					}
				}
				Thread.sleep(30000);
			}

		} catch(final Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized TagAccount createUserAccount() throws Exception
	{
		final Account account = this.reserve.poll();

		if(account == null)
			throw new IllegalStateException("Reserve is empty");

		final byte[] tag = new byte[12];
		RANDOM.nextBytes(tag);
		tag[0] = 1;
		tag[1] = 1;

		final TagAccount userAccount = new TagAccount(Utils.bytesToHex(tag), account);

		this.fountain.activateTag(userAccount.getTaggedWOTS());
		userAccount.transacted(block, null);
		this.userAccounts.put(userAccount.getTag(), userAccount);
		return userAccount;
	}

	public static void main(String[] args) throws Exception
	{
		final MojoWrapper mojo = new MojoWrapper(Paths.get("mojo-app-2.4-0-20190715.jar"));
		final BackendAPIWrapper api = new BackendAPIWrapper("http://104.196.5.151:8889");
		final Fountain fountain = new Fountain("http://35.211.198.192");

		final String exchangeTag = EXCHANGE TAG;
		final String exchangeWots = EXCHANGE WOTS;

		final Account exchangeAccount = new Account(exchangeWots, null);
		final TagAccount exchangeWallet = new TagAccount(exchangeTag, exchangeAccount);

		final Exchange exchange = new Exchange(exchangeWallet, mojo, api, fountain);
		new Thread(exchange).start();

		Thread.sleep(10000);


		//create a user account
		exchange.createUserAccount();

		/**
		 * send MCM to the user's tag and the fund will be transferred to the exchange wallet automatically
		 */
	}

}
