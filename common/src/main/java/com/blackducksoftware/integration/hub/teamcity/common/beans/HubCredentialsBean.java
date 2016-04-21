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
package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("globalHubCredentials")
public class HubCredentialsBean implements Serializable {
	private static final long serialVersionUID = -7836018713177994162L;

	private String hubUser;
	private String hubPass;
	private Integer actualPasswordLength;

	public HubCredentialsBean(final String hubUser) {
		this.hubUser = hubUser;
	}

	public HubCredentialsBean(final String hubUser, final String password) {
		this.hubUser = hubUser;
		hubPass = password;
	}

	public HubCredentialsBean(final HubCredentialsBean credentials) {
		if ((credentials != null) && !credentials.isEmpty()) {
			hubUser = credentials.getHubUser();
			hubPass = credentials.getEncryptedPassword();
		}
	}

	public String getHubUser() {
		return hubUser;
	}

	public void setHubUser(final String hubUser) {
		this.hubUser = hubUser;
	}

	public String getEncryptedPassword() {
		return hubPass;
	}

	public void setEncryptedPassword(final String encryptedPassword) {
		hubPass = encryptedPassword;
	}

	public Integer getActualPasswordLength() {
		return actualPasswordLength;
	}

	public void setActualPasswordLength(final Integer actualPasswordLength) {
		this.actualPasswordLength = actualPasswordLength;
	}

	public String getMaskedPassword() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, EncryptionException {
		if (StringUtils.isBlank(hubPass)) {
			return null;
		}

		if (actualPasswordLength == null) {
			final String password = getDecryptedPassword();
			if (StringUtils.isNotBlank(password)) {
				actualPasswordLength = password.length();
			}
		}
		if (actualPasswordLength != null) {
			final char[] array = new char[actualPasswordLength];
			Arrays.fill(array, '*');
			return new String(array);
		} else {
			return null;
		}
	}

	public String getDecryptedPassword() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, EncryptionException {
		final String encryptedPassword = hubPass;
		if (StringUtils.isBlank(encryptedPassword)) {
			return null;
		}
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		boolean changed = false;
		try {
			if (PasswordDecrypter.class.getClassLoader() != originalClassLoader) {
				changed = true;
				Thread.currentThread().setContextClassLoader(PasswordDecrypter.class.getClassLoader());
			}

			return PasswordDecrypter.decrypt(encryptedPassword);
		} finally {
			if (changed) {
				Thread.currentThread().setContextClassLoader(originalClassLoader);
			}
		}
	}

	public boolean isEmpty() {
		return StringUtils.isBlank(hubUser) && StringUtils.isBlank(hubPass);
	}

	@Override
	public String toString() {
		return "CCCredentialsBean [hubUser=" + hubUser + ", hubPass=" + hubPass + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubPass == null) ? 0 : hubPass.hashCode());
		result = prime * result + ((hubUser == null) ? 0 : hubUser.hashCode());
		result = prime * result + ((actualPasswordLength == null) ? 0 : actualPasswordLength);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HubCredentialsBean other = (HubCredentialsBean) obj;
		if (hubPass == null) {
			if (other.hubPass != null) {
				return false;
			}
		} else if (!hubPass.equals(other.hubPass)) {
			return false;
		}
		if (hubUser == null) {
			if (other.hubUser != null) {
				return false;
			}
		} else if (!hubUser.equals(other.hubUser)) {
			return false;
		}
		if (actualPasswordLength == null) {
			if (other.actualPasswordLength != null) {
				return false;
			}
		} else if (actualPasswordLength != other.actualPasswordLength) {
			return false;
		}
		return true;
	}

}
