/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.value.retriever;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.value.loader.NameTranslator;

/**
 * {@link ValueRetriever} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RootValueRetrieverImpl<T> implements ValueRetriever<T> {

	/**
	 * Mapping of the property name to the {@link ValueRetriever}.
	 */
	private final Map<String, ValueRetriever<Object>> propertyToRetriever = new HashMap<String, ValueRetriever<Object>>();

	/**
	 * {@link NameTranslator}.
	 */
	private final NameTranslator translator;

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link PropertyMetaData} instances.
	 * @param translator
	 *            {@link NameTranslator}.
	 */
	public RootValueRetrieverImpl(PropertyMetaData[] properties,
			NameTranslator translator) {
		this.translator = translator;

		// Load the property retriever
		for (PropertyMetaData property : properties) {
			String propertyName = property.getPropertyName();
			ValueRetriever<Object> propertyRetriever = new PropertyValueRetrieverImpl<Object>(
					property, translator);
			this.propertyToRetriever.put(propertyName, propertyRetriever);
		}
	}

	/*
	 * ===================== ValueRetreiver ======================
	 */

	@Override
	public String retrieveValue(T object, String name) throws Exception {

		// Obtain the property name
		String propertyName;
		String remainingName;
		int splitIndex = name.indexOf('.');
		if (splitIndex < 0) {
			propertyName = name;
			remainingName = "";
		} else {
			propertyName = name.substring(0, splitIndex);
			remainingName = name.substring(splitIndex + 1); // +1 ignore '.'
		}

		// Translate the property name
		propertyName = this.translator.translate(propertyName);

		// Obtain the property value retriever
		ValueRetriever<Object> propertyRetriever = this.propertyToRetriever
				.get(propertyName);
		if (propertyRetriever == null) {
			// Unknown value
			return null;
		}

		// Return the retrieved value
		return propertyRetriever.retrieveValue(object, remainingName);
	}

}