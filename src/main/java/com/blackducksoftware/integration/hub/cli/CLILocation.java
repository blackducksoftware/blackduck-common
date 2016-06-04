package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;

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

	/**
	 * Returns the executable file of the installation
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
					javaExec.setExecutable(true);
					return javaExec;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the executable exists
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
				file.setExecutable(true);

				return file;
			}
		} else {
			return null;
		}
	}

}
