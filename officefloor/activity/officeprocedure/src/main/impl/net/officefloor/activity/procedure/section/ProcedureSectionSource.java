/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.procedure.section;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;

/**
 * {@link SectionSource} for {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureSectionSource extends AbstractSectionSource {

	/**
	 * Indicates if next {@link SectionOutput} should be configured.
	 */
	public static final String IS_NEXT_PROPERTY_NAME = "next";

	/*
	 * ==================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, "Class");
		context.addProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME, "Service");
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain procedure details
		String className = context.getProperty(ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME);
		String serviceName = context.getProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME);
		String procedureName = context.getSectionLocation();
		boolean isNext = Boolean.parseBoolean(context.getProperty(IS_NEXT_PROPERTY_NAME, Boolean.FALSE.toString()));

		// Load the procedure
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(procedureName,
				ProcedureManagedFunctionSource.class.getName());
		namespace.addProperty(ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, className);
		namespace.addProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME, serviceName);
		namespace.addProperty(ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME, procedureName);
		SectionFunction procedure = namespace.addSectionFunction("procedure", procedureName);

		// Load procedure type
		ProcedureLoader procedureLoader = ProcedureEmployer.employProcedureLoader(designer, context);
		Class<?> clazz = context.loadClass(className);
		ManagedFunctionType<Indexed, Indexed> type = procedureLoader.loadProcedureType(clazz, procedureName,
				serviceName);

		// Link objects
		for (ManagedFunctionObjectType<Indexed> objectType : type.getObjectTypes()) {
			String objectName = objectType.getObjectName();
			SectionObject sectionObject = designer.addSectionObject(objectName, objectType.getObjectType().getName());
			sectionObject.setTypeQualifier(objectType.getTypeQualifier());
			designer.link(procedure.getFunctionObject(objectName), sectionObject);
		}

		// TODO determine parameter type
		String parameterType = null;

		// Provide input to invoke procedure
		SectionInput sectionInput = designer.addSectionInput(ProcedureArchitect.INPUT_NAME, parameterType);
		designer.link(sectionInput, procedure);

		// Determine if next output
		if (isNext) {
			Class<?> returnType = type.getReturnType();
			String returnTypeName = (returnType != null) ? returnType.getName() : null;
			SectionOutput next = designer.addSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME, returnTypeName, false);
			designer.link(procedure, next);
		}
	}

}