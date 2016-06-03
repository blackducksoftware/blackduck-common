package com.blackducksoftware.integration.hub.cli;

import java.io.File;

public class CLIHomeFinder {
	public File findCliHome(final File cliInstallDir) {
		final File cliHome = cliInstallDir;
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

}
