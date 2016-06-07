package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class CLILocation {
	public static final String CLI_UNZIP_DIR = "Hub_Scan_Installation";
	public static final String VERSION_FILE_NAME = "hubVersion.txt";

	private final File directoryToInstallTo;

	public CLILocation(final File directoryToInstallTo) {
		if (directoryToInstallTo == null) {
			throw new IllegalArgumentException("You must provided a directory to install the CLI to.");
		}
		this.directoryToInstallTo = directoryToInstallTo;
	}

	public File getJreSecurityDirectory() {
		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}

		final File[] files = cliHomeFile.listFiles();
		final File jreFolder = findFileByName(files, "jre");
		if (null == jreFolder) {
			return null;
		}

		File jreContents = getJreContentsDirectory(jreFolder);
		jreContents = new File(jreContents, "lib");
		jreContents = new File(jreContents, "security");

		return jreContents;
	}

	public String getCLIDownloadUrl(final IntLogger logger, final HubIntRestService restService)
			throws IOException, InterruptedException {
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
			if (logger != null) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	public File getOneJarFile() {
		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}
		File oneJarFile = new File(cliHomeFile, "lib");
		oneJarFile = new File(oneJarFile, "cache");
		oneJarFile = new File(oneJarFile, "scan.cli.impl-standalone.jar");

		oneJarFile.setExecutable(true);

		return oneJarFile;
	}

	public File createHubVersionFile() throws HubIntegrationException, IOException {
		if (!directoryToInstallTo.exists() && !directoryToInstallTo.mkdirs()) {
			throw new HubIntegrationException(
					"Could not create the directory : " + directoryToInstallTo.getCanonicalPath());
		}

		return new File(directoryToInstallTo, VERSION_FILE_NAME);
	}

	public File getCLIInstallDir() {
		return new File(directoryToInstallTo, CLI_UNZIP_DIR);
	}

	public String getCanonicalPath() throws IOException {
		return directoryToInstallTo.getCanonicalPath();
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

	public File getProvidedJavaExec() throws IOException, InterruptedException {
		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}

		final File[] files = cliHomeFile.listFiles();
		final File jreFolder = findFileByName(files, "jre");
		if (null == jreFolder) {
			return null;
		}

		final File jreContents = getJreContentsDirectory(jreFolder);
		File javaExec = new File(jreContents, "bin");
		if (SystemUtils.IS_OS_WINDOWS) {
			javaExec = new File(javaExec, "java.exe");
		} else {
			javaExec = new File(javaExec, "java");
		}
		if (!javaExec.exists()) {
			return null;
		}

		javaExec.setExecutable(true);
		return javaExec;
	}

	public boolean getCLIExists(final IntLogger logger) throws IOException, InterruptedException {
		final File cli = getCLI(logger);
		return null != cli && cli.exists();
	}

	public File getCLI(final IntLogger logger) throws IOException, InterruptedException {
		final File cliHomeFile = getCLIHome();
		if (cliHomeFile == null) {
			return null;
		}

		// find the lib folder in the iScan directory
		logger.debug("BlackDuck scan directory: " + cliHomeFile.getCanonicalPath());
		final File[] files = cliHomeFile.listFiles();
		if (null == files || files.length <= 0) {
			logger.error("No files found in the BlackDuck scan directory.");
			return null;
		}

		logger.debug("directories in the BlackDuck scan directory: " + files.length);
		final File libFolder = findFileByName(files, "lib");
		if (libFolder == null) {
			logger.error("Could not find the lib directory of the CLI.");
			return null;
		}

		logger.debug("BlackDuck scan lib directory: " + libFolder.getCanonicalPath());
		File hubScanJar = null;
		for (final File file : libFolder.listFiles()) {
			if (file.getName().startsWith("scan.cli") && file.getName().endsWith(".jar")) {
				hubScanJar = file;
				hubScanJar.setExecutable(true);
				break;
			}
		}

		return hubScanJar;
	}

	private File getJreContentsDirectory(final File jreFolder) {
		File jreContents = jreFolder;

		final List<String> filenames = Arrays.asList(jreContents.list());
		if (filenames.contains("Contents")) {
			jreContents = new File(jreContents, "Contents");
			jreContents = new File(jreContents, "Home");
		}

		return jreContents;
	}

	private File findFileByName(final File[] files, final String name) {
		if (files != null && files.length > 0) {
			for (final File file : files) {
				if (name.equalsIgnoreCase(file.getName())) {
					return file;
				}
			}
		}
		return null;
	}

}
