/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.test.properties;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Utility class for testing a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListUtil {

	/**
	 * Validates the {@link Property} instances in the {@link PropertyList}.
	 * 
	 * @param propertyList       {@link PropertyList} to validate.
	 * @param propertyNameLabels Name/Label pair listing of expected
	 *                           {@link Property} instances in the
	 *                           {@link PropertyList}.
	 */
	public static void validatePropertyNameLabels(PropertyList propertyList, String... propertyNameLabels) {

		// Create the listing of properties
		List<Property> properties = new ArrayList<Property>();
		for (Property property : propertyList) {
			properties.add(property);
		}

		// Verify the properties
		TestCase.assertEquals("Incorrect number of properties", propertyNameLabels.length / 2, properties.size());
		for (int i = 0; i < propertyNameLabels.length; i += 2) {
			Property property = properties.get(i / 2);
			String name = propertyNameLabels[i];
			String label = propertyNameLabels[i + 1];
			TestCase.assertEquals("Incorrect name for property " + i, name, property.getName());
			TestCase.assertEquals("Incorrect label for property " + i, label, property.getLabel());
			TestCase.assertNull("Should be blank value for property " + (i / 2), property.getValue());
		}
	}

	/**
	 * Validates the {@link Property} values in the {@link PropertyList}.
	 * 
	 * @param propertyList       {@link PropertyList} to validate.
	 * @param propertyNameValues Name/value pair listing of expected
	 *                           {@link Property} instances in the
	 *                           {@link PropertyList}.
	 */
	public static void assertPropertyValues(PropertyList propertyList, String... propertyNameValues) {

		// Validate the properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			assertEquals("Incorrect property " + name, value, propertyList.getPropertyValue(name, null));
		}

		// Ensure no extra properties
		assertEquals("Incorrect number of properties", propertyNameValues.length / 2,
				propertyList.getProperties().size());
	}

	/**
	 * All access via static methods.
	 */
	private PropertyListUtil() {
	}
}
