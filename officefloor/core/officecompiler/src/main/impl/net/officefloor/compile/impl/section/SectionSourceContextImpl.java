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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link SectionSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourceContextImpl extends SourceContextImpl implements SectionSourceContext {

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private final String sectionLocation;

	/**
	 * {@link Node} requiring the {@link OfficeSection}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param sectionLocation
	 *            Location of the {@link SectionNode}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param node
	 *            {@link Node} requiring the {@link OfficeSection}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionSourceContextImpl(boolean isLoadingType, String sectionLocation, PropertyList propertyList, Node node,
			NodeContext context) {
		super(isLoadingType, context.getRootSourceContext(), new PropertyListSourceProperties(propertyList));
		this.sectionLocation = sectionLocation;
		this.node = node;
		this.context = context;
	}

	/*
	 * ================= SectionSourceContext ================================
	 */

	@Override
	public String getSectionLocation() {
		return this.sectionLocation;
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FunctionNamespaceType loadManagedFunctionType(String workSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(FunctionNamespaceType.class, workSourceClassName, this.context.getCompilerIssues(),
				() -> {

					// Obtain the managed function source class
					Class managedFunctionSourceClass = this.context.getManagedFunctionSourceClass(workSourceClassName,
							this.node);
					if (managedFunctionSourceClass == null) {
						return null;
					}

					// Load and return the function namespace type
					ManagedFunctionLoader managedFunctionLoader = this.context.getManagedFunctionLoader(this.node);
					return managedFunctionLoader.loadFunctionNamespaceType(managedFunctionSourceClass, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed object source class
					Class managedObjectSourceClass = this.context
							.getManagedObjectSourceClass(managedObjectSourceClassName, this.node);
					if (managedObjectSourceClass == null) {
						return null;
					}

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(this.node);
					return managedObjectLoader.loadManagedObjectType(managedObjectSourceClass, properties);
				});

	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SectionType loadSectionType(String sectionSourceClassName, String location, PropertyList properties) {
		return CompileUtil.loadType(SectionType.class, sectionSourceClassName, this.context.getCompilerIssues(), () -> {

			// Obtain the section source class
			Class sectionSourceClass = this.context.getSectionSourceClass(sectionSourceClassName, this.node);
			if (sectionSourceClass == null) {
				return null;
			}

			// Load and return the section type
			SectionLoader sectionLoader = this.context.getSectionLoader(this.node);
			return sectionLoader.loadSectionType(sectionSourceClass, location, properties);
		});
	}

}