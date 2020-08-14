package net.officefloor.compile.test.system;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * {@link Extension} for specifying system properties (
 * {@link System#getProperty(String)} ) for tests.
 * 
 * * @author Daniel Sagenschneider
 */
public class SystemPropertiesExtension extends AbstractSystemPropertiesOverride<SystemPropertiesExtension>
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * {@link Namespace}.
	 */
	private final static Namespace NAMESPACE = Namespace.create(SystemPropertiesExtension.class);

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial {@link System} property name/value pairs.
	 */
	public SystemPropertiesExtension(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/**
	 * Obtains the {@link Store}.
	 * 
	 * @param context {@link ExtensionContext}.
	 * @return {@link Store}.
	 */
	private Store getStore(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}

	/*
	 * ======================== Extension ===========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Undertake override
		OverrideReset overrideReset = this.override();

		// Register state to reset
		this.getStore(context).put(context.getRequiredTestClass(),
				new SystemPropertiesOverrideState(true, overrideReset));
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Determine if override all
		SystemPropertiesOverrideState override = this.getStore(context).get(context.getRequiredTestClass(),
				SystemPropertiesOverrideState.class);
		if (override != null) {
			// Overriding before all tests
			return;
		}

		// Override on each test
		OverrideReset overrideReset = this.override();

		// Register state to reset
		this.getStore(context).put(context.getRequiredTestClass(),
				new SystemPropertiesOverrideState(false, overrideReset));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Obtain the override reset
		SystemPropertiesOverrideState override = this.getStore(context).get(context.getRequiredTestClass(),
				SystemPropertiesOverrideState.class);
		if (override.isAfterAll) {
			return; // reset after all tests
		}

		// Reset after each test
		override.overrideReset.resetOverrides();

		// Remove state
		this.getStore(context).remove(context.getRequiredTestClass());
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Obtain the override reset
		SystemPropertiesOverrideState override = this.getStore(context).get(context.getRequiredTestClass(),
				SystemPropertiesOverrideState.class);
		if (override == null) {
			return; // reset after each
		}

		// Reset if after all
		if (override.isAfterAll) {
			override.overrideReset.resetOverrides();
		}

		// Remove state
		this.getStore(context).remove(context.getRequiredTestClass());
	}

	/**
	 * Override state for the {@link System} properties.
	 */
	private static class SystemPropertiesOverrideState {

		/**
		 * Indicates if override after all tests complete.
		 */
		private final boolean isAfterAll;

		/**
		 * {@link OverrideReset}.
		 */
		private final OverrideReset overrideReset;

		/**
		 * Instantiate.
		 * 
		 * @param isAfterAll    Indicates if override after all tests complete.
		 * @param overrideReset {@link OverrideReset}.
		 */
		private SystemPropertiesOverrideState(boolean isAfterAll, OverrideReset overrideReset) {
			this.isAfterAll = isAfterAll;
			this.overrideReset = overrideReset;
		}
	}

}