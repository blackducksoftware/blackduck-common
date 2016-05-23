package com.blackducksoftware.integration.hub.util;

import java.lang.reflect.Constructor;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.commons.lang3.StringUtils;

public class AuthenticatorUtil {

	public static void resetAuthenticator() {
		Authenticator.setDefault(null);

		attemptResetProxyCache();
	}

	public static void setAuthenticator(final String proxyUser, final String proxyPassword) {
		if (!StringUtils.isBlank(proxyUser) && !StringUtils.isBlank(proxyPassword)) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					if (getRequestorType() == RequestorType.PROXY) {
						return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
					}
					return null;
				}
			});
		}
	}

	private static void attemptResetProxyCache() {
		try {
			Class<?> sunAuthCacheValue;
			Class<?> sunAuthCache;
			Class<?> sunAuthCacheImpl;
			try {
				sunAuthCacheValue = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
				sunAuthCache = Class.forName("sun.net.www.protocol.http.AuthCache");
				sunAuthCacheImpl = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
			} catch (final Exception e) {
				// Must not be using a JDK with sun classes so we abandon this
				// reset since it is sun specific
				return;
			}

			final java.lang.reflect.Method m = sunAuthCacheValue.getDeclaredMethod("setAuthCache", sunAuthCache);

			final Constructor<?> authCacheImplConstr = sunAuthCacheImpl.getConstructor();
			final Object authCachImp = authCacheImplConstr.newInstance();

			m.invoke(null, authCachImp);
		} catch (final Exception e) {
			System.err.println(e.toString());
		}
	}
}
