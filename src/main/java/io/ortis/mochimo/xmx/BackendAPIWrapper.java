/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package io.ortis.mochimo.xmx;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Ortis
 */
public class BackendAPIWrapper
{
	private static final Gson GSON = new Gson();


	private final String host;


	public BackendAPIWrapper(final String host)
	{
		this.host = host;
	}

	public BigInteger getBlock() throws IOException, Exception
	{

		final HttpURLConnection conn = (HttpURLConnection) new URL(this.host + "/bc/chain").openConnection();
		conn.setRequestProperty("Content-Type", "application/json");

		final int responseCode = conn.getResponseCode();

		final InputStream is;
		if(responseCode != 200)
			is = conn.getErrorStream();
		else
			is = conn.getInputStream();

		BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();

		while((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();

		conn.disconnect();

		if(responseCode != 200)
			throw new Exception("Error while pushing transaction - " + response);


		final Chain chain = GSON.fromJson(response.toString(), Chain.class);

		return chain.height;

	}


	public void pushTransaction(final byte[] transaction) throws IOException, Exception
	{
		final byte[] tx = Base64.getEncoder().encode(transaction);
		final Push push = new Push();
		push.transaction = new String(tx, StandardCharsets.UTF_8);

		final byte[] payload = GSON.toJson(push).getBytes(StandardCharsets.UTF_8);

		final HttpURLConnection conn = (HttpURLConnection) new URL(this.host + "/push").openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.getOutputStream().write(payload);
		conn.getOutputStream().flush();

		final int responseCode = conn.getResponseCode();

		final InputStream is;
		if(responseCode != 200)
			is = conn.getInputStream();
		else
			is = conn.getInputStream();

		BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();

		while((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();

		conn.disconnect();

		if(responseCode != 200)
			throw new Exception("Error while pushing transaction - " + response);

	}

	public BlockchainEntry getBlockchainEntry(final String address) throws Exception
	{

		final String url;
		if(address.length() == Utils.WOTS_STRING_LENGTH)
			url = "/bc/balance/" + address;
		else if(address.length() == Utils.TAG_STRING_LENGTH)
			url = "/bc/resolve/" + address;
		else
			throw new IllegalArgumentException("Invalid address");


		final HttpURLConnection conn = (HttpURLConnection) new URL(this.host + url).openConnection();
		conn.setRequestProperty("Content-Type", "application/json");
		final int responseCode = conn.getResponseCode();

		final InputStream is;
		if(responseCode != 200)
			is = conn.getErrorStream();
		else
			is = conn.getInputStream();

		BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();

		while((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();

		conn.disconnect();

		if(responseCode != 200)
			throw new Exception("Error requesting access token - " + response);

		final APIBalance balance = GSON.fromJson(response.toString(), APIBalance.class);

		final BlockchainEntry bce;
		if(address.length() == Utils.WOTS_STRING_LENGTH)
		{
			final String tag = address.endsWith(Utils.NO_TAG) ? null : address.substring(Utils.WOTS_STRING_LENGTH - Utils.TAG_STRING_LENGTH);

			bce = new BlockchainEntry(address, tag, balance.balance, balance.block.height.signum() > 0);
		}
		else
		{
			bce = new BlockchainEntry(balance.address, address, balance.balance, balance.block.height.signum() > 0);
		}

		return bce;
	}


	private static class APIBalance
	{

		public BigInteger balance;
		public String address;
		public Chain block;
	}


	private static class Push
	{
		public String transaction;
	}

	private static class Chain
	{
		public BigInteger height;
	}

}
