/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package io.ortis.mochimo.xmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Ortis
 */
public class Fountain
{
	private final String host;


	public Fountain(final String host)
	{
		this.host = host;
	}

	public void activateTag(final String taggedWots) throws IOException, Exception
	{
		if(taggedWots.length() != Utils.WOTS_STRING_LENGTH)
			throw new IllegalArgumentException("Invalid wots");

		if(taggedWots.endsWith(Utils.NO_TAG))
			throw new IllegalArgumentException("WOTS is not tagged");


		System.out.println("Activating tag " + taggedWots.substring(Utils.TAG_STRING_OFFSET));
		final HttpURLConnection conn = (HttpURLConnection) new URL(this.host + "/fund/" + taggedWots).openConnection();
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


	}

}
