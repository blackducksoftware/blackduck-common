package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class CLIInstaller {
	public static final String VERSION_FILE_NAME = "hubVersion.txt";

	public static final String CLI_UNZIP_DIR = "Hub_Scan_Installation";

	private final File directoryToInstallTo;

	private String proxyHost;

	private Integer proxyPort;

	private String proxyUserName;

	private String proxyPassword;

	public CLIInstaller(final File directoryToInstallTo) {

		if (directoryToInstallTo == null) {
			throw new IllegalArgumentException("You must provided a directory to install the CLI to.");
		}
		this.directoryToInstallTo = directoryToInstallTo;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(final Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUserName() {
		return proxyUserName;
	}

	public void setProxyUserName(final String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(final String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public File getDirectoryToInstallTo() {
		return directoryToInstallTo;
	}

	public File getCLIInstallDir() {
		return new File(directoryToInstallTo, CLI_UNZIP_DIR);
	}

	public File getCLIHome() {

		final File cliHome = getCLIInstallDir();
		if (cliHome == null) {
			return null;
		}
		final File[] installDirFiles = cliHome.listFiles();
		if (installDirFiles == null) {
			return null;
		}
		if (installDirFiles.length > 1) {
			// The cli is currently packed with an extra directory "scan.cli-windows-X.X.X-SNAPSHOT"
			for (final File currentFile : installDirFiles) {
				if (!currentFile.getName().contains("windows")) {
					return currentFile;
				}
			}
			return null;
		} else if (installDirFiles.length == 1) {
			return installDirFiles[0];
		} else {
			return null;
		}

	}

	public void performInstallation(final IntLogger logger, final HubIntRestService restService, final String localHostName) throws IOException,
	InterruptedException, BDRestException, URISyntaxException, HubIntegrationException {
		if (StringUtils.isBlank(localHostName)) {
			throw new IllegalArgumentException("You must provided the hostName of the machine this is running on.");
		}

		final String cliDownloadUrl = getCLIDownloadUrl(logger, restService);
		if (StringUtils.isNotBlank(cliDownloadUrl)) {
			customInstall(new URL(cliDownloadUrl), restService.getHubVersion(), localHostName, logger);
		} else {
			logger.error("Could not find the correct Hub CLI download URL.");
		}

	}

	public String getCLIDownloadUrl(final IntLogger logger, final HubIntRestService restService) throws IOException, InterruptedException {

		try {
			final HubSupportHelper hubSupport = new HubSupportHelper();

			hubSupport.checkHubSupport(restService, logger);

			if (SystemUtils.IS_OS_MAC_OSX && hubSupport.isJreProvidedSupport()) {
				return HubSupportHelper.getOSXCLIWrapperLink(restService.getBaseUrl());
			} else if (SystemUtils.IS_OS_WINDOWS && hubSupport.isJreProvidedSupport()) {
				return HubSupportHelper.getWindowsCLIWrapperLink(restService.getBaseUrl());
			} else {
				return HubSupportHelper.getLinuxCLIWrapperLink(restService.getBaseUrl());
			}
		} catch (final URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		return null;

	}

	private boolean customInstall(final URL archive, String hubVersion, final String localHostName, final IntLogger logger) throws IOException, InterruptedException,
	HubIntegrationException {

		try {
			if (!directoryToInstallTo.exists() && !directoryToInstallTo.mkdirs()) {
				throw new HubIntegrationException("Could not create the directory : " + directoryToInstallTo.getCanonicalPath());
			}

			boolean cliMismatch = true;
			// For some reason the Hub returns the Version inside ""'s
			hubVersion = hubVersion.replace("\"", "");
			final File hubVersionFile = new File(directoryToInstallTo, VERSION_FILE_NAME);
			if (hubVersionFile.exists()) {
				final String storedHubVersion = IOUtils.toString(new FileInputStream(hubVersionFile));
				if (hubVersion.equals(storedHubVersion)) {
					cliMismatch = false;
				} else {
					hubVersionFile.delete();
				}
			}
			if (cliMismatch) {
				hubVersionFile.createNewFile();
				final FileWriter writer = new FileWriter(hubVersionFile);
				writer.write(hubVersion);
				writer.close();
			}
			URLConnection connection = null;
			try {
				Proxy proxy = null;

				if (StringUtils.isNotBlank(proxyHost)) {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				}
				if (proxy != null) {

					if (StringUtils.isNotBlank(proxyUserName) && StringUtils.isNotBlank(proxyPassword)) {
						Authenticator.setDefault(
								new Authenticator() {
									@Override
									public PasswordAuthentication getPasswordAuthentication() {
										return new PasswordAuthentication(
												proxyUserName,
												proxyPassword.toCharArray());
									}
								}
								);
					} else {
						Authenticator.setDefault(null);
					}
				}
				if (proxy != null) {
					connection = archive.openConnection(proxy);
				} else {
					connection = archive.openConnection();
				}
				connection.connect();
			} catch (final IOException ioe) {
				logger.error("Skipping installation of " + archive + " to " + directoryToInstallTo.getCanonicalPath() + ": " + ioe.toString());
				return false;
			}

			final File cliInstallDirectory = getCLIInstallDir();
			if (cliInstallDirectory.exists() && cliInstallDirectory.listFiles().length > 0) {
				if (!cliMismatch)
				{
					return false; // already up to date
				}

				// delete directory contents
				deleteFilesRecursive(cliInstallDirectory.listFiles());
			} else {
				cliInstallDirectory.mkdir();
			}

			logger.info("Unpacking " + archive.toString() + " to " + cliInstallDirectory.getCanonicalPath() + " on " + localHostName);

			final InputStream in = connection.getInputStream();
			final CountingInputStream cis = new CountingInputStream(in);

			try {
				unzip(cliInstallDirectory, cis, logger);
			} catch (final IOException e) {
				throw new IOException(String.format("Failed to unpack %s (%d bytes read of total %d)",
						archive, cis.getByteCount(), connection.getContentLength()), e);
			}
			return true;
		} catch (final IOException e) {
			throw new IOException("Failed to install " + archive + " to " + directoryToInstallTo.getCanonicalPath(), e);
		}

	}

	public void deleteFilesRecursive(final File[] files) {

		if (files != null && files.length > 0) {
			for (final File currentFile : files) {
				if (currentFile != null && currentFile.exists()) {
					if (currentFile.isDirectory()) {
						deleteFilesRecursive(currentFile.listFiles());
						currentFile.delete();
					} else {
						currentFile.delete();
					}
				}
			}
		}
	}

	private void unzip(final File dir, final InputStream in, final IntLogger logger) throws IOException {

		final File tmpFile = File.createTempFile("tmpzip", null); // uses java.io.tmpdir
		try {
			copyInputStreamToFile(in, tmpFile);
			unzip(dir, tmpFile, logger);
		} finally {
			tmpFile.delete();
		}

	}

	private void unzip(File dir, final File zipFile, final IntLogger logger) throws IOException {

		dir = dir.getAbsoluteFile(); // without absolutization, getParentFile below seems to fail
		final ZipFile zip = new ZipFile(zipFile);
		final
		Enumeration<ZipEntry> entries = zip.getEntries();
		try {
			while (entries.hasMoreElements()) {
				final ZipEntry e = entries.nextElement();
				final File f = new File(dir, e.getName());
				if (e.isDirectory()) {
					f.mkdirs();
				} else {
					final File p = f.getParentFile();
					if (p != null) {
						p.mkdirs();
					}
					final InputStream input = zip.getInputStream(e);
					try {
						copyInputStreamToFile(input, f);
					} finally {
						input.close();
					}
					f.setLastModified(e.getTime());
				}
			}
		} finally {
			zip.close();
		}
	}

	private void copyInputStreamToFile(final InputStream in, final File f) throws IOException {

		final FileOutputStream fos = new FileOutputStream(f);
		try {
			org.apache.commons.io.IOUtils.copy(in, fos);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(fos);
		}
	}

	/**
	 * Returns the executable file of the installation
	 *
	 *
	 */
	public File getProvidedJavaExec() throws IOException, InterruptedException {

		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}
		final File[] files = cliHomeFile.listFiles();
		if (files != null && files.length > 0) {
			File jreFolder = null;
			for (final File directory : files) {
				if ("jre".equalsIgnoreCase(directory.getName())) {
					jreFolder = directory;
					break;
				}
			}
			if (jreFolder != null) {
				File javaExec = null;
				if (SystemUtils.IS_OS_MAC_OSX) {
					javaExec = new File(jreFolder, "Contents");
					javaExec = new File(javaExec, "Home");
					javaExec = new File(javaExec, "bin");
				} else {
					javaExec = new File(jreFolder, "bin");
				}

				if (SystemUtils.IS_OS_WINDOWS) {
					javaExec = new File(javaExec, "java.exe");
				} else {
					javaExec = new File(javaExec, "java");
				}
				if (javaExec.exists()) {
					// when unpacking the bin directory files may not be executable
					if (!javaExec.canExecute()) {
						javaExec.setExecutable(true);
					}
					return javaExec;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the executable exists
	 *
	 */
	public boolean getCLIExists(final IntLogger logger) throws IOException, InterruptedException {

		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return false;
		}
		// find the lib folder in the iScan directory
		logger.debug("BlackDuck scan directory: " + cliHomeFile.getCanonicalPath());
		final File[] files = cliHomeFile.listFiles();
		if (files != null) {
			logger.debug("directories in the BlackDuck scan directory: " + files.length);
			if (files.length > 0) {
				File libFolder = null;
				for (final File directory : files) {
					if ("lib".equalsIgnoreCase(directory.getName())) {
						libFolder = directory;
						break;
					}
				}
				if (libFolder == null) {
					logger.error("Could not find the lib directory of the CLI.");
					return false;
				}
				logger.debug("BlackDuck scan lib directory: " + libFolder.getCanonicalPath());
				final FilenameFilter nameFilter = new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return name.matches("scan.cli.*.jar");
					}
				};
				final File[] cliFiles = libFolder.listFiles(nameFilter);

				File hubScanJar = null;
				if (cliFiles.length == 0) {
					return false;
				} else {
					hubScanJar = cliFiles[0];
				}

				return hubScanJar.exists();
			} else {
				logger.error("No files found in the BlackDuck scan directory.");
				return false;
			}
		} else {
			logger.error("No files found in the BlackDuck scan directory.");
			return false;
		}

	}

	/**
	 * Returns the executable file of the installation
	 *
	 */
	public File getCLI() throws IOException, InterruptedException {

		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}
		final File[] files = cliHomeFile.listFiles();
		if (files != null && files.length > 0) {
			File libFolder = null;
			for (final File directory : files) {
				if ("lib".equalsIgnoreCase(directory.getName())) {
					libFolder = directory;
					break;
				}
			}
			if (libFolder == null) {
				return null;
			}
			final FilenameFilter nameFilter = new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.matches("scan.cli.*.jar");
				}
			};
			final File[] cliFiles = libFolder.listFiles(nameFilter);
			if (cliFiles.length == 0) {
				return null;
			} else {
				final File file = cliFiles[0];

				if (!file.canExecute()) {
					file.setExecutable(true);
				}

				return file;
			}
		} else {
			return null;
		}
	}

	public File getOneJarFile() {

		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}
		File oneJarFile = new File(cliHomeFile, "lib");
		oneJarFile = new File(oneJarFile, "cache");
		oneJarFile = new File(oneJarFile, "scan.cli.impl-standalone.jar");

		if (!oneJarFile.canExecute()) {
			oneJarFile.setExecutable(true);
		}

		return oneJarFile;
	}
}
