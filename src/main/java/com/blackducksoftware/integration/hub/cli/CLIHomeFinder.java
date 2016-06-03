package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

public class CLIHomeFinder {
	private static final List<String> REQUIRED_DIRECTORIES = Arrays.asList("bin", "jre", "lib");

	/**
	 * The CLI Home is defined as the first directory found at or beneath the
	 * cliInstallDir that contains the directories: -bin -jre -lib and we only
	 * go down at most three levels to look.
	 */
	public File findCliHome(final File cliInstallDir) {
		return checkForRequiredDirectories(0, cliInstallDir);
	}

	private File checkForRequiredDirectories(int level, File cliHome) {
		if (null == cliHome || !cliHome.exists() || !cliHome.isDirectory() || level >= 3) {
			return null;
		}

		if (containsRequiredDirectories(cliHome)) {
			return cliHome;
		}

		final File[] directories = cliHome.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		for (final File directory : directories) {
			cliHome = checkForRequiredDirectories(level++, directory);
			if (null != cliHome) {
				return cliHome;
			}
		}

		return null;
	}

	private boolean containsRequiredDirectories(final File directoryToCheck) {
		final File[] files = directoryToCheck.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.exists() && file.isDirectory() && REQUIRED_DIRECTORIES.contains(file.getName());
			}
		});

		if (null == files || files.length < REQUIRED_DIRECTORIES.size()) {
			return false;
		}

		final Set<String> uniqueFilenames = new HashSet<String>();
		for (final File file : files) {
			uniqueFilenames.add(file.getName());
		}
		return uniqueFilenames.size() == REQUIRED_DIRECTORIES.size();
	}

}
