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

package net.officefloor.plugin.section.clazz.flow.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.PropertyValue;
import net.officefloor.plugin.section.clazz.SectionInterface;
import net.officefloor.plugin.section.clazz.SectionNameAnnotation;
import net.officefloor.plugin.section.clazz.SectionOutputLink;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.flow.ClassSectionSubSectionOutputLink;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;

/**
 * {@link ClassSectionFlowManufacturer} for {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionClassSectionFlowManufacturer
		implements ClassSectionFlowManufacturer, ClassSectionFlowManufacturerServiceFactory {

	/*
	 * ================ ClassSectionFlowManufacturerServiceFactory ================
	 */

	@Override
	public ClassSectionFlowManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ClassSectionFlowManufacturer ========================
	 */

	@Override
	public ClassSectionFlow createFlow(ClassSectionFlowManufacturerContext context) throws Exception {

		// Determine if section interface
		AnnotatedType annotatedType = context.getAnnotatedType();
		SectionInterface sectionInterface = annotatedType.getAnnotation(SectionInterface.class);
		if (sectionInterface == null) {
			return null; // not section interface
		}

		// Obtain the section name
		SectionNameAnnotation nameAnnotation = annotatedType.getAnnotation(SectionNameAnnotation.class);
		String sectionName = nameAnnotation != null ? nameAnnotation.getName() : "SECTION";

		// Build the section
		String location = getValue(sectionInterface.locationClass(), sectionInterface.location());
		PropertyList properties = context.getSourceContext().createPropertyList();
		for (PropertyValue property : sectionInterface.properties()) {
			String value = getValue(property.valueClass(), property.value());
			properties.addProperty(property.name()).setValue(value);
		}
		List<ClassSectionSubSectionOutputLink> links = new LinkedList<>();
		for (SectionOutputLink outputLink : sectionInterface.outputs()) {
			ClassSectionSubSectionOutputLink link = context.createSubSectionOutputLink(outputLink.name(),
					outputLink.link());
			links.add(link);
		}
		SubSection subSection = context.getOrCreateSubSection(sectionName, sectionInterface.source().getName(),
				location, properties, links.toArray(new ClassSectionSubSectionOutputLink[links.size()]))
				.getSubSection();

		// Should always be function flow
		ManagedFunctionFlowType<?> flowType = (ManagedFunctionFlowType<?>) annotatedType;

		// Return the input to the section
		Class<?> parameterType = flowType.getArgumentType();
		SubSectionInput input = subSection.getSubSectionInput(flowType.getFlowName());
		return new ClassSectionFlow(input, parameterType);
	}

	/**
	 * Obtains the value taking {@link Class} as priority.
	 * 
	 * @param valueClass Value {@link Class}.
	 * @param value      Fallback value if not {@link Class}.
	 * @return Value.
	 */
	private static String getValue(Class<?> valueClass, String value) {
		return (valueClass != null) && (!Void.class.equals(valueClass)) && (!Void.TYPE.equals(valueClass))
				? valueClass.getName()
				: value;
	}

}
