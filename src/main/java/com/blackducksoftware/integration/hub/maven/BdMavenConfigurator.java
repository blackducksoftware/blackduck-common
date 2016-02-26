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
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.google.gson.Gson;

public class BdMavenConfigurator {

    public static final String MAVEN_EXT_CLASS_PATH = "maven.ext.class.path";

    private final CILogger logger;

    public BdMavenConfigurator(IntLogger intLogger) {
        Logger slf4jLogger = LoggerFactory.getLogger(BdMavenConfigurator.class);
        logger = new CILogger(intLogger, slf4jLogger);
    }

    /**
     * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git<br>
     * <p>
     * Gets the version of the specified Maven
     * </p>
     *
     * @param mavenHome
     *            String
     * @param log
     *            BuildLog
     * @return String
     * @throws BDMavenRetrieverException
     * @throws IOException
     */
    public String getMavenVersion(String mavenHome) throws BDMavenRetrieverException, IOException {
        if (StringUtils.isEmpty(mavenHome)) {
            throw new IllegalArgumentException("Can not retrieve Maven information from this Maven Home: '" + mavenHome +
                    "'");
        }
        logger.trace(mavenHome);
        File mavenHomeFile = new File(mavenHome);
        ClassRealm realm;
        try {
            realm = buildClassRealm(mavenHomeFile, null, null);
        } catch (MalformedURLException e1) {
            throw new BDMavenRetrieverException(e1);
        }

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = null;
        try {
            Thread.currentThread().setContextClassLoader(realm);
            URL resource = realm.findResource("META-INF/maven/org.apache.maven/maven-core/pom.properties");
            if (resource == null) {
                throw new BDMavenRetrieverException("Couldn't find maven version information in '" +
                        mavenHomeFile.getCanonicalPath()
                        + "'. This may not be a valid Maven Home.");
            }
            logger.trace(resource.toString());
            inputStream = resource.openStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty("version");
            return version;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.toString(), e);
            }
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    /**
     * Checks if the version supplied is newer than Maven 3.1.x <br>
     * We need this information because Maven was refactored between 3.0.x and 3.1.x
     *
     * @param mavenVersion
     *            String
     * @return Boolean
     * @throws IllegalArgumentException
     */
    public Boolean isMaven31OrLater(String mavenVersion) throws IllegalArgumentException {
        if (StringUtils.isEmpty(mavenVersion)) {
            throw new IllegalArgumentException("No Maven version was supplied");
        }
        String[] versionParts = mavenVersion.split("\\.");
        if (versionParts.length < 2) {
            throw new IllegalArgumentException(
                    "Maven version supplied is too short. Need to at least supply the first 2 parts of the version. Ex: 2.1 , 3.0 , 3.2 , etc.");
        }
        Integer majorVersion = null;
        Integer minorVersion = null;
        try {
            majorVersion = Integer.valueOf(versionParts[0]);
            minorVersion = Integer.valueOf(versionParts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The first 2 parts of the version should be integers. Ex : 2.1 , 3.0 , 3.3 , etc.", e);
        }

        if (majorVersion < 3) {
            return false;
        } else if (majorVersion == 3) {
            if (minorVersion > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Checks if the version supplied is newer than Maven 3.0.x <br>
     * We need this information because Maven was refactored between 3.0.x and 3.1.x
     *
     * @param mavenVersion
     *            String
     * @return Boolean
     * @throws IllegalArgumentException
     */
    public Boolean isMaven30OrLater(String mavenVersion) throws IllegalArgumentException {
        if (StringUtils.isEmpty(mavenVersion)) {
            throw new IllegalArgumentException("No Maven version was supplied");
        }
        String[] versionParts = mavenVersion.split("\\.");
        if (versionParts.length < 2) {
            throw new IllegalArgumentException(
                    "Maven version supplied is too short. Need to at least supply the first 2 parts of the version. Ex: 2.1 , 3.0 , 3.2 , etc.");
        }
        Integer majorVersion = null;
        Integer minorVersion = null;
        try {
            majorVersion = Integer.valueOf(versionParts[0]);
            minorVersion = Integer.valueOf(versionParts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The first 2 parts of the version should be integers. Ex : 2.1 , 3.0 , 3.3 , etc.", e);
        }

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
     * @param mavenHome
     *            cannot be null
     * @param world
     *            can be null
     * @param parentClassLoader
     *            can be null
     * @return ClassRealm
     */
    private ClassRealm buildClassRealm(File mavenHome,
            ClassWorld classWorld,
            ClassLoader parentClassLoader) throws MalformedURLException {

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
        File libDirectory = new File(mavenHome, "lib");
        // try {
        // logger.trace(libDirectory.getCanonicalPath());
        // } catch (IOException e) {
        // logger.trace(libDirectory.getAbsolutePath());
        // }
        if (!libDirectory.exists()) {
            throw new IllegalArgumentException(mavenHome.getPath() +
                    " doesn't have a 'lib' subdirectory - thus cannot be a valid maven installation!");
        }
        File[] jarFiles = libDirectory.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        AntClassLoader antClassLoader = new AntClassLoader(Thread.currentThread().getContextClassLoader(), false);
        for (File jarFile : jarFiles) {
            // try {
            // logger.trace(jarFile.getCanonicalPath());
            // } catch (IOException e) {
            // logger.trace(jarFile.getAbsolutePath());
            // }
            antClassLoader.addPathComponent(jarFile);
        }
        ClassRealm classRealm = new
                ClassRealm(world, "plexus.core",
                        parentClassLoader == null ? antClassLoader : parentClassLoader);
        for (File jarFile : jarFiles) {
            classRealm.addURL(jarFile.toURI().toURL());
        }
        return classRealm;
    }

    /**
     * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git
     *
     * Retrieves the jar file for the class provided.
     *
     * @throws BDMavenRetrieverException
     *
     */
    public File jarFile(Class clazz) throws IOException, BDMavenRetrieverException {
        ClassLoader cl = clazz.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        URL res = cl.getResource(clazz.getName().replace('.', '/') + ".class");
        if (res == null) {
            throw new IllegalArgumentException("Unable to locate class file for " + clazz);
        }

        String resURL = res.toExternalForm();
        String originalURL = resURL;
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
                int idx = Math.max(resURL.lastIndexOf('/'), resURL.lastIndexOf('\\'));
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
            InputStream is = res.openStream();
            try {
                Object delegate = is;
                while (delegate.getClass().getEnclosingClass() != ZipFile.class) {
                    Field f = is.getClass().getDeclaredField("delegate");
                    f.setAccessible(true);
                    delegate = f.get(is);
                }
                Field f = delegate.getClass().getDeclaredField("this$0");
                f.setAccessible(true);
                ZipFile zipFile = (ZipFile) f.get(delegate);
                return new File(zipFile.getName());
            } catch (NoSuchFieldException e) {
                // something must have changed in JBoss5. fall through
                throw new BDMavenRetrieverException("Failed to resolve vfszip into a jar location", e);
            } catch (IllegalAccessException e) {
                // something must have changed in JBoss5. fall through
                throw new BDMavenRetrieverException("Failed to resolve vfszip into a jar location", e);
            } finally {
                is.close();
            }

        }

        URLConnection con = res.openConnection();
        if (con instanceof JarURLConnection) {
            JarURLConnection jcon = (JarURLConnection) con;
            JarFile jarFile = jcon.getJarFile();
            if (jarFile != null) {
                String n = jarFile.getName();
                if (n.length() > 0) {// JDK6u10 needs this
                    return new File(n);
                } else {
                    // JDK6u10 apparently starts hiding the real jar file name,
                    // so this just keeps getting tricker and trickier...
                    try {
                        Field f = ZipFile.class.getDeclaredField("name");
                        f.setAccessible(true);
                        return new File((String) f.get(jarFile));
                    } catch (NoSuchFieldException e) {
                        throw new BDMavenRetrieverException("Failed to obtain the local cache file name of " + clazz, e);
                    } catch (IllegalAccessException e) {
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
     * @throws UnsupportedEncodingException
     *
     */
    private File fromJarUrlToFile(String resURL) throws MalformedURLException, UnsupportedEncodingException {
        String usableUrl = resURL.substring(resURL.indexOf(':') + 1, resURL.lastIndexOf('!')); // cut off "scheme:" and
                                                                                               // the file
        // name portion
        return new File(decode(new URL(usableUrl).getPath()));
    }

    /**
     * Code taken from : https://github.com/jenkinsci/lib-jenkins-maven-embedder.git
     *
     * @throws UnsupportedEncodingException
     *
     */
    private String decode(String s) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Character[] hexChars = new Character[2];
        boolean isHex = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
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
    private int hexToInt(int ch) {
        return Character.getNumericValue(ch);
    }

    /**
     * Locates the necessary jars, workspace, and build Id that will be needed to build the build-info.json files. Will
     * still need to add the buildId to the buildParameters after this method is run.
     *
     * @param mavenVersion
     *            String
     * @param runParameters
     *            Map
     * @param buildParameters
     *            Map
     * @return the necessary jar locations to add to the Maven classpath
     * @throws BDMavenRetrieverException
     */
    public String getMavenExtensionClasspath(final String mavenVersion,
            Map<String, String> runParameters,
            Map<String, String> buildParameters) throws BDMavenRetrieverException {

        StringBuilder mavenExtClasspath = new StringBuilder();
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

            String workDirectory = buildParameters.get("system.agent.work.dir");
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
            String lastChar = mavenClasspath.substring(mavenClasspath.length() - 1);
            if (lastChar.equals(File.pathSeparator)) {
                mavenClasspath = mavenClasspath.substring(0, mavenClasspath.length() - 1);
            }
        } catch (IllegalArgumentException e) {
            throw new BDMavenRetrieverException("Retrieving jar locations failed: "
                    + e.getMessage(), e);
        } catch (NoClassDefFoundError e) {
            throw new BDMavenRetrieverException("Retrieving jar locations failed: "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new BDMavenRetrieverException("Retrieving jar locations failed: "
                    + e.getMessage(), e);
        }
        return mavenClasspath;
    }

    private void appendClasspath(String filePath, StringBuilder classpath) {
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
     * @param mavenOptions
     *            String
     * @param addition
     *            String
     *
     * @return the adjusted maven options
     */
    public String addToMavenExtOption(String mavenOptions, String addition) {
        if (mavenOptions == null) {
            throw new IllegalArgumentException("Need to provide the string containing the maven options.");
        }
        if (addition == null || StringUtils.isEmpty(addition)) {
            throw new IllegalArgumentException("Need to provide the string containing the new maven class path extensions to be added.");
        }
        String mavenExtOption = "-D" + MAVEN_EXT_CLASS_PATH + "=";
        String newMavenOptions = "";
        if (mavenOptions.contains(mavenExtOption)) {

            String mavenExtOptionValue = "";
            String newMavenExtOptionValue = "";
            int startMavenExtOption = mavenOptions.indexOf(mavenExtOption) + mavenExtOption.length();
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
