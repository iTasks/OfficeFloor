package net.officefloor.frame.impl.execute.managedobject.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.frame.test.ThreadedTestSupport.MultiThreadedExecution;

/**
 * Ensure block completion of opening {@link OfficeFloor} until startup
 * complete.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ManagedObjectSourceStartupTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Start up immediately.
	 */
	@Test
	public void startupImmediately() throws Throwable {

		// Complete start up immediately
		MockStartupManagedObjectSource mos = new MockStartupManagedObjectSource(true);

		// Should open immediately (without blocking)
		this.construct.constructManagedObject("MO", mos, this.construct.getOfficeName());
		try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();
		}
	}

	/**
	 * Wait on start up.
	 */
	@Test
	public void waitOnStartupCompletion() throws Throwable {

		// Delay the start up
		MockStartupManagedObjectSource mos = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("MO", mos, this.construct.getOfficeName());
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// Wait for start up completion creation
		this.threading.waitForTrue(() -> mos.startup != null);

		// Ensure service is not yet started
		assertFalse(mos.isServicingStarted, "Should not start service");

		// Complete startup and ensure service is now started
		mos.startup.complete();
		this.threading.waitForTrue(() -> mos.isServicingStarted);

		// Should now allow opening OfficeFloor
		execution.waitForCompletion();
		assertTrue(mos.isServicingStopped, "Should have stopped servicing");
	}

	/**
	 * Fail start up.
	 */
	@Test
	public void failStartup() throws Throwable {

		// Fail start up immediately
		Exception failure = new Exception("TEST");
		MockStartupManagedObjectSource mos = new MockStartupManagedObjectSource(failure);

		// Should open immediately (without blocking)
		this.construct.constructManagedObject("MO", mos, this.construct.getOfficeName());
		try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
			try {
				officeFloor.openOfficeFloor();
				fail("Should not successfully open");
			} catch (Exception ex) {
				assertSame(failure, ex, "Incorrect failure");
			}
		}
	}

	/**
	 * Delayed failed start up.
	 */
	@Test
	public void delayedFailedStartup() throws Throwable {

		// Delay failure on start up
		Exception failure = new Exception("TEST");
		MockStartupManagedObjectSource mos = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("MO", mos, this.construct.getOfficeName());
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// Wait for start up completion creation
		this.threading.waitForTrue(() -> mos.startup != null);

		// Specify failure on startup
		mos.startup.failOpen(failure);

		// Should fail to open
		try {
			execution.waitForCompletion();
			fail("Should not successfully open");
		} catch (Exception ex) {
			assertSame(failure, ex, "Incorrect failure");
		}
	}

	@Test
	public void timeoutStartup() throws Exception {

		// Time out waiting on start completion
		MockStartupManagedObjectSource mos = new MockStartupManagedObjectSource(false);
		this.construct.getOfficeFloorBuilder().setMaxStartupWaitTime(1);

		// Construct OfficeFloor
		this.construct.constructManagedObject("MO", mos, this.construct.getOfficeName());
		try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
			try {
				officeFloor.openOfficeFloor();
				fail("Should time out on attempting to open");
			} catch (TimeoutException ex) {
				assertEquals("OfficeFloor took longer than 1 milliseconds to start", ex.getMessage(),
						"Incorrect cause");
			}
		}
	}

	/**
	 * Mock {@link ManagedObjectSource} to test start up logic.
	 */
	@TestSource
	private static class MockStartupManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		private final boolean isCompleteImmeidately;

		private final Exception immediateFailure;

		private volatile ManagedObjectStartupCompletion startup;

		private volatile boolean isServicingStarted = false;

		private volatile boolean isServicingStopped = false;

		private MockStartupManagedObjectSource(boolean isCompleteImmediately) {
			this.isCompleteImmeidately = isCompleteImmediately;
			this.immediateFailure = null;
		}

		private MockStartupManagedObjectSource(Exception immediateFailure) {
			this.isCompleteImmeidately = false;
			this.immediateFailure = immediateFailure;
		}

		/*
		 * ==================== ManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {

			// Create the startup completion
			this.startup = context.createStartupCompletion();

			// Determine if complete/fail immediately
			if (this.isCompleteImmeidately) {
				this.startup.complete();
			}
			if (this.immediateFailure != null) {
				this.startup.failOpen(this.immediateFailure);
			}

			// Add service
			context.addService(new ManagedObjectService<None>() {

				@Override
				public void startServicing(ManagedObjectServiceContext<None> serviceContext) throws Exception {
					MockStartupManagedObjectSource.this.isServicingStarted = true;
				}

				@Override
				public void stopServicing() {
					MockStartupManagedObjectSource.this.isServicingStopped = true;
				}
			});
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ======================== ManagedObject ========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}