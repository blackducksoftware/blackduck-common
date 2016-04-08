/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.AntClassLoader;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.BuildInfo;
import com.blackducksoftware.integration.build.extractor.Recorder_3_0_Loader;
import com.blackducksoftware.integration.build.extractor.Recorder_3_1_Loader;
import com.blackducksoftware.integration.hub.exception.BDMavenRetrieverException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.google.gson.Gson;

public class BdMavenConfigurator {

	public static final String MAVEN_EXT_CLASS_PATH = "maven.ext.class.path";

	private final CILogger logger;

	public BdMavenConfigurator(final IntLogger intLogger) {
		final Logger slf4jLogger = LoggerFactory.getLogger(BdMavenConfigurator.class);
		logger = new CILogger(intLogger, slf4jLogger);
	}

	/**
	 * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git<br>
	 * <p>
	 * Gets the version of the specified Maven
	 * </p>
	 *
	 */
	public String getMavenVersion(final String mavenHome) throws BDMavenRetrieverException, IOException {
		if (StringUtils.isEmpty(mavenHome)) {
			throw new IllegalArgumentException("Can not retrieve Maven information from this Maven Home: '" + mavenHome +
					"'");
		}
		logger.trace(mavenHome);
		final File mavenHomeFile = new File(mavenHome);
		ClassRealm realm;
		try {
			realm = buildClassRealm(mavenHomeFile, null, null);
		} catch (final MalformedURLException e1) {
			throw new BDMavenRetrieverException(e1);
		}

		final ClassLoader original = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = null;
		try {
			Thread.currentThread().setContextClassLoader(realm);
			final URL resource = realm.findResource("META-INF/maven/org.apache.maven/maven-core/pom.properties");
			if (resource == null) {
				throw new BDMavenRetrieverException("Couldn't find maven version information in '" +
						mavenHomeFile.getCanonicalPath()
						+ "'. This may not be a valid Maven Home.");
			}
			logger.trace(resource.toString());
			inputStream = resource.openStream();
			final Properties properties = new Properties();
			properties.load(inputStream);
			final String version = properties.getProperty("version");
			return version;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (final IOException e) {
				logger.error(e.toString(), e);
			}
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	/**
	 * Checks if the version supplied is newer than Maven 3.1.x <br>
	 * We need this information because Maven was refactored between 3.0.x and 3.1.x
	 *
	 */
	public Boolean isMaven31OrLater(final String mavenVersion) throws IllegalArgumentException {
		if (StringUtils.isEmpty(mavenVersion)) {
			throw new IllegalArgumentException("No Maven version was supplied");
		}
		final String[] versionParts = mavenVersion.split("\\.");
		if (versionParts.length < 2) {
			throw new IllegalArgumentException(
					"Maven version supplied is too short. Need to at least supply the first 2 parts of the version. Ex: 2.1 , 3.0 , 3.2 , etc.");
		}
		Integer majorVersion = null;
		Integer minorVersion = null;
		try {
			majorVersion = Integer.valueOf(versionParts[0]);
			minorVersion = Integer.valueOf(versionParts[1]);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("The first 2 parts of the version should be integers. Ex : 2.1 , 3.0 , 3.3 , etc.", e);
		}

		// Return false if it is 3.0 or less
		if (majorVersion < 3 || (majorVersion == 3 && minorVersion == 0)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if the version supplied is newer than Maven 3.0.x <br>
	 * We need this information because Maven was refactored between 3.0.x and 3.1.x
	 *
	 */
	public Boolean isMaven30OrLater(final String mavenVersion) throws IllegalArgumentException {
		if (StringUtils.isEmpty(mavenVersion)) {
			throw new IllegalArgumentException("No Maven version was supplied");
		}
		final String[] versionParts = mavenVersion.split("\\.");
		if (versionParts.length < 2) {
			throw new IllegalArgumentException(
					"Maven version supplied is too short. Need to at least supply the first 2 parts of the version. Ex: 2.1 , 3.0 , 3.2 , etc.");
		}
		Integer majorVersion = null;
		try {
			majorVersion = Integer.valueOf(versionParts[0]);
			Integer.valueOf(versionParts[1]);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("The first 2 parts of the version should be integers. Ex : 2.1 , 3.0 , 3.3 , etc.", e);
		}
		// NOPMD com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanReturnCheck
		if (majorVersion < 3) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git<br>
	 * <p>
	 * Build a ClassRealm with all jars in mavenHome/lib/*.jar
	 * </p>
	 * <p>
	 * The ClassRealm is ChildFirst with the current classLoader as parent.
	 * </p>
	 *
	 */
	private ClassRealm buildClassRealm(final File mavenHome,
			final ClassWorld classWorld,
			final ClassLoader parentClassLoader) throws MalformedURLException {

		if (mavenHome == null) {
			throw new IllegalArgumentException("mavenHome cannot be null");
		}
		if (!mavenHome.exists()) {
			throw new IllegalArgumentException("mavenHome '" + mavenHome.getPath()
					+ "' doesn't seem to exist on this node (or you don't have sufficient rights to access it)");
		}
		ClassWorld world = new ClassWorld();
		if (classWorld != null) {
			world = classWorld;
		}
		// list all jar under mavenHome/lib
		final File libDirectory = new File(mavenHome, "lib");
		// try {
		// logger.trace(libDirectory.getCanonicalPath());
		// } catch (IOException e) {
		// logger.trace(libDirectory.getAbsolutePath());
		// }
		if (!libDirectory.exists()) {
			throw new IllegalArgumentException(mavenHome.getPath() +
					" doesn't have a 'lib' subdirectory - thus cannot be a valid maven installation!");
		}
		final File[] jarFiles = libDirectory.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".jar");
			}
		});
		final AntClassLoader antClassLoader = new AntClassLoader(Thread.currentThread().getContextClassLoader(), false);
		for (final File jarFile : jarFiles) {
			// try {
			// logger.trace(jarFile.getCanonicalPath());
			// } catch (IOException e) {
			// logger.trace(jarFile.getAbsolutePath());
			// }
			antClassLoader.addPathComponent(jarFile);
		}
		final ClassRealm classRealm = new
				ClassRealm(world, "plexus.core",
						parentClassLoader == null ? antClassLoader : parentClassLoader);
		for (final File jarFile : jarFiles) {
			classRealm.addURL(jarFile.toURI().toURL());
		}
		return classRealm;
	}

	/**
	 * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git
	 *
	 * Retrieves the jar file for the class provided.
	 *
	 *
	 */
	public File jarFile(final Class<?> clazz) throws IOException, BDMavenRetrieverException {
		ClassLoader cl = clazz.getClassLoader();
		if (cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}
		final URL res = cl.getResource(clazz.getName().replace('.', '/') + ".class");
		if (res == null) {
			throw new IllegalArgumentException("Unable to locate class file for " + clazz);
		}

		String resURL = res.toExternalForm();
		final String originalURL = resURL;
		if (resURL.startsWith("jar:file:") || resURL.startsWith("wsjar:file:")) {
			return fromJarUrlToFile(resURL);
		}

		if (resURL.startsWith("code-source:/")) {
			// OC4J apparently uses this. See http://www.nabble.com/Hudson-on-OC4J-tt16702113.html
			resURL = resURL.substring("code-source:/".length(), resURL.lastIndexOf('!')); // cut off jar: and the file
			// name portion
			return new File(decode(new URL("file:/" + resURL).getPath()));
		}

		if (resURL.startsWith("zip:")) {
			// weblogic uses this. See http://www.nabble.com/patch-to-get-Hudson-working-on-weblogic-td23997258.html
			// also see http://www.nabble.com/Re%3A-Hudson-on-Weblogic-10.3-td25038378.html#a25043415
			resURL = resURL.substring("zip:".length(), resURL.lastIndexOf('!')); // cut off zip: and the file name
			// portion
			return new File(decode(new URL("file:" + resURL).getPath()));
		}

		if (resURL.startsWith("file:")) {
			// unpackaged classes
			int n = clazz.getName().split("\\.").length; // how many slashes do wo need to cut?
			for (; n > 0; n--) {
				final int idx = Math.max(resURL.lastIndexOf('/'), resURL.lastIndexOf('\\'));
				if (idx < 0) {
					throw new IllegalArgumentException(originalURL + " - " + resURL);
				}
				resURL = resURL.substring(0, idx);
			}

			// won't work if res URL contains ' '
			// return new File(new URI(null,new URL(res).toExternalForm(),null));
			// won't work if res URL contains '%20'
			// return new File(new URL(res).toURI());

			return new File(decode(new URL(resURL).getPath()));
		}

		if (resURL.startsWith("vfszip:")) {
			// JBoss5
			final InputStream is = res.openStream();
			try {
				Object delegate = is;
				while (delegate.getClass().getEnclosingClass() != ZipFile.class) {
					final Field f = is.getClass().getDeclaredField("delegate");
					f.setAccessible(true);
					delegate = f.get(is);
				}
				final Field f = delegate.getClass().getDeclaredField("this$0");
				f.setAccessible(true);
				final ZipFile zipFile = (ZipFile) f.get(delegate);
				return new File(zipFile.getName());
			} catch (final NoSuchFieldException e) {
				// something must have changed in JBoss5. fall through
				throw new BDMavenRetrieverException("Failed to resolve vfszip into a jar location", e);
			} catch (final IllegalAccessException e) {
				// something must have changed in JBoss5. fall through
				throw new BDMavenRetrieverException("Failed to resolve vfszip into a jar location", e);
			} finally {
				is.close();
			}

		}

		final URLConnection con = res.openConnection();
		if (con instanceof JarURLConnection) {
			final JarURLConnection jcon = (JarURLConnection) con;
			final JarFile jarFile = jcon.getJarFile();
			if (jarFile != null) {
				final String n = jarFile.getName();
				if (n.length() > 0) {// JDK6u10 needs this
					return new File(n);
				} else {
					// JDK6u10 apparently starts hiding the real jar file name,
					// so this just keeps getting tricker and trickier...
					try {
						final Field f = ZipFile.class.getDeclaredField("name");
						f.setAccessible(true);
						return new File((String) f.get(jarFile));
					} catch (final NoSuchFieldException e) {
						throw new BDMavenRetrieverException("Failed to obtain the local cache file name of " + clazz, e);
					} catch (final IllegalAccessException e) {
						throw new BDMavenRetrieverException("Failed to obtain the local cache file name of " + clazz, e);
					}
				}
			}
		}

		throw new IllegalArgumentException(originalURL + " - " + resURL);
	}

	/**
	 * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git
	 *
	 *
	 */
	private File fromJarUrlToFile(final String resURL) throws MalformedURLException, UnsupportedEncodingException {
		final String usableUrl = resURL.substring(resURL.indexOf(':') + 1, resURL.lastIndexOf('!')); // cut off "scheme:" and
		// the file
		// name portion
		return new File(decode(new URL(usableUrl).getPath()));
	}

	/**
	 * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git
	 *
	 *
	 */
	private String decode(final String s) throws UnsupportedEncodingException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Character[] hexChars = new Character[2];
		boolean isHex = false;

		for (int i = 0; i < s.length(); i++) {
			final char ch = s.charAt(i);
			if (ch == '%') {
				// The next 2 characters should be the hex value
				isHex = true;
				continue;
			} else if (isHex) {
				if (hexChars[0] == null) {
					// First hex character
					hexChars[0] = ch;
					continue;
				}
				// Second hex character
				hexChars[1] = ch;
				baos.write(hexToInt(hexChars[0]) * 16 + hexToInt(hexChars[1]));
				// Reset the buffer and boolean after we write the converted hex
				hexChars = new Character[2];
				isHex = false;
				continue;

			}

			baos.write(ch);
		}
		return new String(baos.toByteArray(), "UTF-8");
	}

	/**
	 * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git
	 *
	 */
	private int hexToInt(final int ch) {
		return Character.getNumericValue(ch);
	}

	/**
	 * Locates the necessary jars, workspace, and build Id that will be needed to build the build-info.json files. Will
	 * still need to add the buildId to the buildParameters after this method is run.
	 *
	 */
	public String getMavenExtensionClasspath(final String mavenVersion,
			final Map<String, String> runParameters,
			final Map<String, String> buildParameters) throws BDMavenRetrieverException {

		final StringBuilder mavenExtClasspath = new StringBuilder();
		String mavenClasspath = "";
		try {
			File dependencyRecorderJar = null;
			File buildInfo = null;
			File slf4jJar = null;
			// File log4jJar = null;
			// File slf4jJDKBindingJar = null;
			// File commonApiJar = null;
			File gsonJar = null;
			String propertyBuildId = null;
			String propertyWorkingDirectory = null;
			if (isMaven30OrLater(mavenVersion)) {
				if (mavenVersion.contains("3.0")) {

					dependencyRecorderJar = jarFile(Recorder_3_0_Loader.class);

					propertyBuildId = Recorder_3_0_Loader.PROPERTY_BUILD_ID;
					propertyWorkingDirectory = Recorder_3_0_Loader.PROPERTY_WORKING_DIRECTORY;
					System.setProperty("propertyBuildId", propertyBuildId);
					System.setProperty("propertyWorkingDirectory", propertyWorkingDirectory);

					slf4jJar = jarFile(org.slf4j.helpers.FormattingTuple.class);
				} else if (isMaven31OrLater(mavenVersion)) {
					dependencyRecorderJar = jarFile(Recorder_3_1_Loader.class);
					propertyBuildId = Recorder_3_1_Loader.PROPERTY_BUILD_ID;
					propertyWorkingDirectory = Recorder_3_1_Loader.PROPERTY_WORKING_DIRECTORY;
					System.setProperty("propertyBuildId", propertyBuildId);
					System.setProperty("propertyWorkingDirectory", propertyWorkingDirectory);
					// dont need to supply the slf4j jar as Maven 3.1 already has the slf4j-api jar
				} else {
					throw new BDMavenRetrieverException("The BlackDuck CodeCenter Plugin does not support this version of Maven : " +
							mavenVersion);
				}
			} else {
				throw new BDMavenRetrieverException("The BlackDuck CodeCenter Plugin does not support this version of Maven : " +
						mavenVersion);
			}

			final String workDirectory = buildParameters.get("system.agent.work.dir");
			String checkoutDirectory = null;
			String customWorkspace = null;
			if (buildParameters.containsKey("teamcity.build.default.checkoutDir")) {
				checkoutDirectory = buildParameters.get("teamcity.build.default.checkoutDir");
			} else {
				checkoutDirectory = buildParameters.get("teamcity.build.checkoutDir");
			}
			if (runParameters.containsKey("teamcity.build.workingDir")) {
				customWorkspace = runParameters.get("teamcity.build.workingDir");
				if (!StringUtils.isEmpty(customWorkspace)) {
					System.setProperty(propertyWorkingDirectory, workDirectory + File.separator + checkoutDirectory + File.separator + customWorkspace);
				} else {
					System.setProperty(propertyWorkingDirectory, workDirectory + File.separator + checkoutDirectory);
				}
			} else {
				System.setProperty(propertyWorkingDirectory, workDirectory + File.separator + checkoutDirectory);
			}

			// log4jJar = jarFile(Logger.class);
			buildInfo = jarFile(BuildInfo.class);
			gsonJar = jarFile(Gson.class);

			if (dependencyRecorderJar != null) {
				appendClasspath(dependencyRecorderJar.getCanonicalPath(), mavenExtClasspath);
			}
			if (buildInfo != null) {
				appendClasspath(buildInfo.getCanonicalPath(), mavenExtClasspath);
			}
			if (slf4jJar != null) {
				appendClasspath(slf4jJar.getCanonicalPath(), mavenExtClasspath);
			}
			// if (log4jJar != null) {
			// appendClasspath(log4jJar.getCanonicalPath(), mavenExtClasspath);
			// }
			if (gsonJar != null) {
				appendClasspath(gsonJar.getCanonicalPath(), mavenExtClasspath);
			}
			mavenClasspath = mavenExtClasspath.toString();

			// check the last char in the string, if it is a ':' then remove it
			final String lastChar = mavenClasspath.substring(mavenClasspath.length() - 1);
			if (lastChar.equals(File.pathSeparator)) {
				mavenClasspath = mavenClasspath.substring(0, mavenClasspath.length() - 1);
			}
		} catch (final IllegalArgumentException e) {
			throw new BDMavenRetrieverException("Retrieving jar locations failed: "
					+ e.getMessage(), e);
		} catch (final NoClassDefFoundError e) {
			throw new BDMavenRetrieverException("Retrieving jar locations failed: "
					+ e.getMessage(), e);
		} catch (final IOException e) {
			throw new BDMavenRetrieverException("Retrieving jar locations failed: "
					+ e.getMessage(), e);
		}
		return mavenClasspath;
	}

	private void appendClasspath(final String filePath, final StringBuilder classpath) {
		if (!StringUtils.isEmpty(filePath)) {
			if (classpath.length() > 0) {
				classpath.append(File.pathSeparatorChar);
			}
			classpath.append(filePath);
		}
	}

	/**
	 * Parses the maven options to find if the maven.ext.class.path option has already been provided. If it has, this
	 * method will add the new additions to the already existing option. If it hasn't, it will add the option with the
	 * addition you want to use.
	 *
	 */
	public String addToMavenExtOption(final String mavenOptions, final String addition) {
		if (mavenOptions == null) {
			throw new IllegalArgumentException("Need to provide the string containing the maven options.");
		}
		if (addition == null || StringUtils.isEmpty(addition)) {
			throw new IllegalArgumentException("Need to provide the string containing the new maven class path extensions to be added.");
		}
		final String mavenExtOption = "-D" + MAVEN_EXT_CLASS_PATH + "=";
		String newMavenOptions = "";
		if (mavenOptions.contains(mavenExtOption)) {

			String mavenExtOptionValue = "";
			String newMavenExtOptionValue = "";
			final int startMavenExtOption = mavenOptions.indexOf(mavenExtOption) + mavenExtOption.length();
			mavenExtOptionValue = mavenOptions.substring(startMavenExtOption);

			int endMavenExtOption = 0;
			// check if there are other options by looking for -
			// if there are we can get the maven.ext.class.path by cutting the string before the next option
			if (mavenExtOptionValue.contains("-")) {
				// this will avoid get the first index of - which is the next option that is provided
				endMavenExtOption = mavenExtOptionValue.indexOf('-');
			} else {
				// if there are no other options then the end of the maven.ext.class.path option is the end of the
				// string;
				endMavenExtOption = mavenOptions.length();
			}
			mavenExtOptionValue = mavenExtOptionValue.substring(0, endMavenExtOption).trim();
			if (!StringUtils.isEmpty(mavenExtOptionValue.trim())) {
				newMavenExtOptionValue = mavenExtOptionValue.trim() + File.pathSeparatorChar + addition;
				newMavenOptions = mavenOptions.replace(mavenExtOptionValue, newMavenExtOptionValue);
			} else {
				newMavenExtOptionValue = addition;
				newMavenOptions = mavenOptions.replace(mavenExtOption, mavenExtOption + newMavenExtOptionValue);
			}
		} else {
			newMavenOptions = mavenOptions + " " + mavenExtOption + addition;

		}
		return newMavenOptions;

	}

}
