package net.officefloor.compile.run;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.compile.ModelCompilerTestSupport;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.test.TestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * {@link TestSupport} to open {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenOfficeFloorTestSupport implements TestSupport, AfterEachCallback {

	/**
	 * {@link ModelCompilerTestSupport}.
	 */
	private ModelCompilerTestSupport modelTestSupport;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * Instantiate as {@link TestSupport}.
	 */
	public OpenOfficeFloorTestSupport() {
	}

	/**
	 * Compatibility for non-JUnit5 tests.
	 * 
	 * @param modelTestSupport {@link ModelCompilerTestSupport}.
	 */
	public OpenOfficeFloorTestSupport(ModelCompilerTestSupport modelTestSupport) {
		this.modelTestSupport = modelTestSupport;
	}

	/*
	 * =================== TestSupport ====================
	 */

	@Override
	public void init(ExtensionContext context) throws Exception {
		this.modelTestSupport = TestSupportExtension.getTestSupport(ModelCompilerTestSupport.class, context);
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	public OfficeFloor open() {

		// Ensure open
		assertNull(this.officeFloor, "OfficeFloor already compiled");

		// Obtain the resource source
		ResourceSource resourceSource = this.modelTestSupport.getResourceSource();

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
		compiler.setOfficeFloorLocation("office-floor");
		compiler.addResources(resourceSource);

		// Compile the OfficeFloor
		this.officeFloor = compiler.compile("OfficeFloor");
		assertNotNull(this.officeFloor, "Should compile the OfficeFloor");

		// Open the OfficeFloor
		try {
			this.officeFloor.openOfficeFloor();
		} catch (Exception ex) {
			return fail(ex);
		}

		// Return the open OfficeFloor
		return this.officeFloor;
	}

	/*
	 * =================== Extensions ====================
	 */

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Ensure close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

}