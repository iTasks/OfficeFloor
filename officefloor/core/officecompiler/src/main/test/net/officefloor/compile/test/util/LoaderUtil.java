/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.test.util;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Assert;

/**
 * Utility methods for loader test utilities.
 * 
 * @author Daniel Sagenschneider
 */
public class LoaderUtil {

	/**
	 * Asserts the arrays are of the same length, providing useful debug
	 * information if not.
	 * 
	 * @param message
	 *            Message.
	 * @param expected
	 *            Expected items of array.
	 * @param actual
	 *            Actual items of array.
	 * @param toString
	 *            {@link Function} to obtain {@link String} description of item.
	 */
	public static <T> void assertLength(String message, T[] expected, T[] actual, Function<T, String> toString) {

		// Determine if lengths match
		if (expected.length == actual.length) {
			return; // same length
		}

		// Not same length, so assert with debug information
		String eText = String.join(", ", Arrays.stream(expected).map(toString).toArray(String[]::new));
		String aText = String.join(", ", Arrays.stream(actual).map(toString).toArray(String[]::new));

		// Assert the length
		Assert.assertEquals(message + "\n\tE: " + eText + "\n\tA: " + aText, expected.length, actual.length);
	}

	/**
	 * All access via static methods.
	 */
	private LoaderUtil() {
	}

}