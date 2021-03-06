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

package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.FunctionModel;

/**
 * Tests renaming the {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameFunctionTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures handles {@link FunctionModel} not being on the
	 * {@link SectionModel}.
	 */
	public void testRenameFunctionNotOnSection() {
		FunctionModel function = new FunctionModel("NOT_IN_SECTION", false, "NAMESPACE", "MANAGED_FUNCTION", null);
		Change<FunctionModel> change = this.operations.renameFunction(function, "NEW_NAME");
		this.assertChange(change, function, "Rename function NOT_IN_SECTION to NEW_NAME", false,
				"Function NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can rename the {@link FunctionModel}.
	 */
	public void testRenameFunction() {
		FunctionModel function = this.model.getFunctions().get(0);
		assertNotNull("Ensure have function", function);
		Change<FunctionModel> change = this.operations.renameFunction(function, "NEW_NAME");
		this.assertChange(change, function, "Rename function OLD_NAME to NEW_NAME", true);
	}

	/**
	 * Ensures on renaming the {@link FunctionModel} that order is maintained.
	 */
	public void testRenameFunctionCausingFunctionOrderChange() {
		this.useTestSetupModel();
		FunctionModel function = this.model.getFunctions().get(0);
		Change<FunctionModel> change = this.operations.renameFunction(function, "FUNCTION_C");
		this.assertChange(change, function, "Rename function FUNCTION_A to FUNCTION_C", true);
	}
}
