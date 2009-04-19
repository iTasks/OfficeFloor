/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SectionObjectNode} implementation.
 * 
 * @author Daniel
 */
public class SectionObjectNodeImpl implements SectionObjectNode {

	/**
	 * Name of the {@link SectionObjectType}.
	 */
	private final String objectName;

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link SubSectionObject}.
	 */
	private final String sectionLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if this {@link SectionObjectType} is initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Object type.
	 */
	private String objectType;

	/**
	 * Flags whether within the {@link Office} context.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office} containing this
	 * {@link OfficeSectionObject}.
	 */
	private String officeLocation;

	/**
	 * Initiate not initialised.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObject}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SubSectionObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionObjectNodeImpl(String objectName, String sectionLocation,
			NodeContext context) {
		this.objectName = objectName;
		this.sectionLocation = sectionLocation;
		this.context = context;
	}

	/**
	 * Initiate initialised.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObjectType}.
	 * @param objectType
	 *            Object type.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SubSectionObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionObjectNodeImpl(String objectName, String objectType,
			String sectionLocation, NodeContext context) {
		this.objectName = objectName;
		this.sectionLocation = sectionLocation;
		this.context = context;
		this.initialise(objectType);
	}

	/*
	 * ================== SectionObjectNode ========================
	 */

	@Override
	public boolean isInitialised() {
		return this.isInitialised;
	}

	@Override
	public void initialise(String objectType) {
		this.objectType = objectType;
		this.isInitialised = true;
	}

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	/*
	 * =============== SectionObjectType ===========================
	 */

	@Override
	public String getSectionObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

	/*
	 * =============== SubSectionObject ============================
	 */

	@Override
	public String getSubSectionObjectName() {
		return this.objectName;
	}

	/*
	 * ==================== OfficeSectionObject =========================
	 */

	@Override
	public String getOfficeSectionObjectName() {
		return this.objectName;
	}

	/*
	 * =============== LinkObjectNode ==============================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {

		// Ensure not already linked
		if (this.linkedObjectNode != null) {
			if (this.isInOfficeContext) {
				// Office section object already linked
				this.context.getCompilerIssues().addIssue(
						LocationType.OFFICE,
						this.officeLocation,
						null,
						null,
						"Office section object " + this.objectName
								+ " linked more than once");
			} else {
				// Sub section object already linked
				this.context.getCompilerIssues().addIssue(
						LocationType.SECTION,
						this.sectionLocation,
						null,
						null,
						"Sub section object " + this.objectName
								+ " linked more than once");
			}
			return false; // already linked
		}

		// Link
		this.linkedObjectNode = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}