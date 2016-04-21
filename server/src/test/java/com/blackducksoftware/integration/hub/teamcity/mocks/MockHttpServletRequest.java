/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.mocks;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mockito;

import com.intellij.util.enumeration.ArrayEnumeration;
import com.intellij.util.enumeration.EmptyEnumeration;

public class MockHttpServletRequest {
	public static HttpServletRequest getMockedHttpServletRequest() {
		final HttpServletRequest mockedHttpServletRequest = Mockito.mock(HttpServletRequest.class);

		Mockito.when(mockedHttpServletRequest.getParameter(Mockito.anyString())).thenReturn("");

		return mockedHttpServletRequest;
	}

	public static void addGetParameter(final HttpServletRequest mockedRequest, final String parameterName,
			final String parameterValue) {
		Mockito.when(mockedRequest.getParameter(parameterName)).thenReturn(parameterValue);
	}

	public static void requestHasParamters(final HttpServletRequest mockedRequest, final boolean hasParameters) {
		if (hasParameters) {
			final Enumeration<?> enumeration = new ArrayEnumeration(new String[] { "" });
			Mockito.when(mockedRequest.getParameterNames()).thenReturn(enumeration);
		} else {
			final Enumeration<?> enumeration = EmptyEnumeration.INSTANCE;

			Mockito.when(mockedRequest.getParameterNames()).thenReturn(enumeration);
		}

	}
}
