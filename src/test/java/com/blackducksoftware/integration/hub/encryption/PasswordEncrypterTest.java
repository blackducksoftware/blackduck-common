package com.blackducksoftware.integration.hub.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public final class PasswordEncrypterTest {
	private static PrintStream orgStream = null;
	private static PrintStream orgErrStream = null;
	private static ByteArrayOutputStream byteOutput = null;

	@BeforeClass
	public static void init() throws IOException {
		orgStream = System.out;
		orgErrStream = System.err;
		byteOutput = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(byteOutput);
		System.setOut(ps);
		System.setErr(ps);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		System.setOut(orgStream);
		System.setErr(orgErrStream);

		if (byteOutput != null) {
			byteOutput.close();
		}
		byteOutput = null;
	}

	@Before
	public void testSetup() throws IOException {
		// reset the output stream for each test
		byteOutput.reset();
		assertTrue(byteOutput.size() == 0);
	}

	@Test
	public void testMainNullArgs() throws Exception {
		PasswordEncrypter.main(null);

		final String output = byteOutput.toString("UTF-8");
		String split[] = null;
		split = output.split("\\n");

		assertTrue("Expected #lines > 0 - Output: " + output, split.length > 1);
		assertEquals(output, "Please provide a UserName and Password, UserName should be provided first.", split[1]);
	}

	@Test
	public void testMainNoArgs() throws Exception {
		PasswordEncrypter.main(new String[0]);

		final String output = byteOutput.toString("UTF-8");
		String split[] = null;
		split = output.split("\\n");

		assertTrue("Expected #lines > 0 - Output: " + output, split.length > 2);
		assertEquals(output, "# of arguments = 0", split[0]);
		assertEquals(output, "Please provide a UserName and Password, UserName should be provided first.", split[2]);
	}

	@Test
	public void testMainUserNameOnly() throws Exception {
		final String[] args = new String[1];
		args[0] = "UserName";
		PasswordEncrypter.main(args);

		final String output = byteOutput.toString("UTF-8");
		String split[] = null;
		split = output.split("\\n");

		assertTrue("Expected #lines > 0 - Output: " + output, split.length > 2);
		assertEquals(output, "# of arguments = 1", split[0]);
		assertEquals(output, "Example input: UserName Password", split[1]);
		assertEquals(output, "Please provide both a UserName and Password, UserName should be provided first.",
				split[2]);
	}

	@Test
	public void testMainPasswordOnly() throws Exception {
		final String[] args = new String[1];
		args[0] = "Password";
		PasswordEncrypter.main(args);

		final String output = byteOutput.toString("UTF-8");
		String split[] = null;
		split = output.split("\\n");

		assertTrue("Expected #lines > 0 - Output: " + output, split.length > 2);
		assertEquals(output, "# of arguments = 1", split[0]);
		assertEquals(output, "Example input: UserName Password", split[1]);
		assertEquals(output, "Please provide both a UserName and Password, UserName should be provided first.",
				split[2]);
	}

	@Test
	public void testMainExtraArgs() throws Exception {
		final String[] args = new String[4];
		args[0] = "UserName";
		args[1] = "Password";
		args[2] = "SecondUserName";
		args[3] = "SecondPassword";
		PasswordEncrypter.main(args);

		final String output = byteOutput.toString("UTF-8");
		String split[] = null;
		split = output.split("\\n");

		assertTrue("Expected #lines > 0 - Output: " + output, split.length > 2);
		assertEquals(output, "# of arguments = 4", split[0]);
		assertEquals(output, "Example input: UserName Password", split[1]);
		assertEquals(output, "Please ONLY provide a UserName and Password, UserName should be provided first.",
				split[2]);
	}

	@Test
	public void testMainEncryptPassword() throws Exception {
		final String[] args = new String[2];
		args[0] = "UserName";
		args[1] = "Password";
		PasswordEncrypter.main(args);

		final String output = byteOutput.toString("UTF-8");
		String split[] = null;
		split = output.split("\\n");

		assertTrue("Expected #lines > 0 - Output: " + output, 1 == split.length);
		assertEquals(output,
				"UserName = SaTaqurAqc7q0nf0n6IL4erSd/Sfogvh6tJ39J+iC+Hq0nf0n6IL4erSd/Sfogvh6tJ39J+iC+Hq0nf0n6IL4Q==",
				split[0]);
	}

}
