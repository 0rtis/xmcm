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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Ortis
 */
public class MojoWrapper
{
	private static final Gson GSON = new Gson();

	private final Path mojoJarPath;


	public MojoWrapper(final Path mojoJarPath)
	{
		this.mojoJarPath = mojoJarPath;
	}


	public List<Account> generateAccounts(final int count) throws IOException, InterruptedException
	{
		final Path outputPath = Paths.get(UUID.randomUUID().toString());
		final Process process = new ProcessBuilder("java", "-jar", mojoJarPath.toString(), "wots", "--count", Integer.toString(count), outputPath.toString()).start();
		int read;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		while((read = process.getErrorStream().read(buffer)) > -1)
		{
			baos.write(buffer, 0, read);
		}
		process.waitFor();

		if(baos.size() > 0)
			throw new RuntimeException(new String(baos.toByteArray(), StandardCharsets.UTF_8));

		final List<Account> accounts = new ArrayList<>();

		final AccountsBean beans = GSON.fromJson(new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8), AccountsBean.class);

		for(final AccountBean bean : beans)
		{
			final Account account = new Account(bean.address, bean.secret);
			accounts.add(account);
		}
		Files.delete(outputPath);
		return accounts;
	}


	public byte[] sign(final String sourceWOTS, final String sourceSecret, final String destinationWOTS, final String changeWOTS, final BigInteger sourceBalance,
			final BigInteger payment) throws IOException, InterruptedException
	{
		final Path outputPath = Paths.get(UUID.randomUUID().toString());
		final Process process = new ProcessBuilder("java", "-jar", mojoJarPath.toString(), "sign", "--source-wots", sourceWOTS, "--source-secret", sourceSecret,
				"--destination-wots", destinationWOTS, "--change-wots", changeWOTS, "--source-balance", sourceBalance.toString(), "--offline", payment.toString(),
				outputPath.toString()).start();

		int read;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		while((read = process.getErrorStream().read(buffer)) > -1)
		{
			baos.write(buffer, 0, read);
		}
		process.waitFor();

		if(baos.size() > 0)
			throw new RuntimeException(new String(baos.toByteArray(), StandardCharsets.UTF_8));

		final byte[] tx = Files.readAllBytes(outputPath);
		Files.delete(outputPath);
		return tx;
	}

	private static class AccountsBean extends ArrayList<AccountBean>
	{

	}

	private static class AccountBean
	{
		public Integer id;
		public String address;
		public String secret;
	}
}
