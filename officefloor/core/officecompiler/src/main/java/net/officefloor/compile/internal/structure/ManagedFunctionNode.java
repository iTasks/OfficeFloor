/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.ResponsibleTeam;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link SectionFunction} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionNode extends LinkFlowNode, SectionFunction, OfficeSectionFunction {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Function";

	/**
	 * Obtains the fully qualified name of this {@link ManagedFunction}.
	 * 
	 * @return Fully qualified name of this {@link ManagedFunction}.
	 */
	String getFullyQualifiedFunctionName();

	/**
	 * Initialises this {@link ManagedFunctionNode}.
	 * 
	 * @param managedFunctionTypeName
	 *            {@link ManagedFunctionType} name.
	 * @param functionNamespace
	 *            {@link FunctionNamespaceNode} for the
	 *            {@link ManagedFunctionNode}.
	 */
	void initialise(String managedFunctionTypeName, FunctionNamespaceNode functionNamespace);

	/**
	 * Sources the {@link ManagedFunction}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if successfully sourced the
	 *         {@link ManagedFunction}. <code>false</code> if failed to source
	 *         with issues reported to the {@link CompilerIssues}.
	 */
	boolean souceManagedFunction(TypeContext typeContext);

	/**
	 * Auto wires the {@link ResponsibleTeam} for this {@link ManagedFunction}.
	 * 
	 * @param autoWirer
	 *            {@link AutoWirer}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	void autoWireManagedFunctionResponsibility(AutoWirer<LinkTeamNode> autoWirer, TypeContext typeContext);

	/**
	 * Loads the {@link OfficeFunctionType}.
	 * 
	 * @param parentSubSectionType
	 *            Containing {@link OfficeSubSectionType} to this
	 *            {@link OfficeSectionFunction}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeFunctionType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeFunctionType loadOfficeFunctionType(OfficeSubSectionType parentSubSectionType, TypeContext typeContext);

	/**
	 * Obtains the {@link FunctionNamespaceNode} containing this
	 * {@link ManagedFunctionNode}.
	 * 
	 * @return {@link FunctionNamespaceNode} containing this
	 *         {@link ManagedFunctionNode}.
	 */
	FunctionNamespaceNode getFunctionNamespaceNode();

	/**
	 * Loads the {@link ManagedFunctionType} for this
	 * {@link ManagedFunctionNode}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link ManagedFunctionType} for this {@link ManagedFunctionNode}.
	 *         May be <code>null</code> if can not determine
	 *         {@link ManagedFunctionType}.
	 */
	ManagedFunctionType<?, ?> loadManagedFunctionType(TypeContext typeContext);

	/**
	 * Builds the {@link ManagedFunction} for this {@link ManagedFunctionNode}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	void buildManagedFunction(OfficeBuilder officeBuilder, TypeContext typeContext);

}