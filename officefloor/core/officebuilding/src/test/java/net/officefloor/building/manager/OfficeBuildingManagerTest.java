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
package net.officefloor.building.manager;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.UnmarshalException;
import java.rmi.server.RMIClientSocketFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.building.process.ProcessShellMBean;
import net.officefloor.building.process.officefloor.MockWork;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * Tests the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagerTest extends OfficeFrameTestCase {

	/**
	 * Next port to run next current test.
	 */
	private static int nextPort = 13778;

	/**
	 * Port to run the current test on.
	 */
	private int port;

	/**
	 * Trust store file.
	 */
	private File trustStore;

	/**
	 * Password to trust store.
	 */
	private String trustStorePassword;

	/**
	 * Username.
	 */
	private String username;

	/**
	 * Password.
	 */
	private String password;

	/**
	 * {@link MBeanServer}.
	 */
	private MBeanServer mbeanServer;

	@Override
	protected void setUp() throws Exception {

		// Obtain the next port for this test
		this.port = nextPort;

		// Ensure stop office building
		OfficeBuildingTestUtil.ensureOfficeBuildingStopped(this.port);

		// Obtain the trust store for SSL to work
		this.trustStore = OfficeBuildingTestUtil.getTrustStore();
		this.trustStorePassword = OfficeBuildingTestUtil.getTrustStorePassword();

		// Obtain the credentials
		this.username = OfficeBuildingTestUtil.getLoginUsername();
		this.password = OfficeBuildingTestUtil.getLoginPassword();

		// Obtain the MBean Server
		this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure stop office building
		OfficeBuildingTestUtil.ensureOfficeBuildingStopped(this.port);
	}

	/**
	 * Starts the Office Building for testing.
	 * 
	 * @return {@link OfficeBuildingManager}.
	 */
	private OfficeBuildingManagerMBean startOfficeBuilding() throws Exception {

		// Obtain key store for office building
		File keyStore = OfficeBuildingTestUtil.getKeyStore();
		String keyStorePassword = OfficeBuildingTestUtil.getKeyStorePassword();

		// Start the office building
		OfficeBuildingManagerMBean manager = OfficeBuildingManager.startOfficeBuilding(null, this.port, keyStore,
				keyStorePassword, this.username, this.password, null, false, new Properties(), this.mbeanServer,
				new String[0], false);

		// Ensure not a proxy implementation on starting
		assertTrue("Should be the manager (not proxy MBean)", manager.getClass() == OfficeBuildingManager.class);
		assertFalse("Should not be proxy MBean", Proxy.isProxyClass(manager.getClass()));

		// Ensure office building is available
		boolean isOfficeBuildingAvailable = OfficeBuildingManager.isOfficeBuildingAvailable(null, nextPort, keyStore,
				keyStorePassword, this.username, this.password);
		assertTrue("OfficeBuilding should be available", isOfficeBuildingAvailable);

		// Return the manager
		return manager;
	}

	/**
	 * Ensure able to start the Office Building.
	 */
	public void testRunningOfficeBuilding() throws Exception {

		// Start the Office Building (recording times before/after)
		long beforeTime = System.currentTimeMillis();
		OfficeBuildingManagerMBean manager = this.startOfficeBuilding();
		long afterTime = System.currentTimeMillis();

		// Ensure OfficeBuilding is available
		assertTrue("OfficeBuilding should be available", OfficeBuildingManager.isOfficeBuildingAvailable(null,
				this.port, this.trustStore, this.trustStorePassword, this.username, this.password));

		// Ensure correct JMX Service URL
		String actualServiceUrl = manager.getOfficeBuildingJmxServiceUrl();
		String hostName = InetAddress.getLocalHost().getHostName();
		String expectedServiceUrl = "service:jmx:rmi://" + hostName + ":" + this.port + "/jndi/rmi://" + hostName + ":"
				+ this.port + "/OfficeBuilding";
		assertEquals("Incorrect service url", expectedServiceUrl, actualServiceUrl);

		// Obtain the Office Building Manager MBean
		OfficeBuildingManagerMBean managerMBean = OfficeBuildingManager.getOfficeBuildingManager(hostName, this.port,
				this.trustStore, this.trustStorePassword, this.username, this.password);

		// Ensure start time is accurate
		long startTime = managerMBean.getStartTime().getTime();
		assertTrue("Start time recorded incorrectly", ((beforeTime <= startTime) && (startTime <= afterTime)));

		// Ensure MBean reports correct service URL
		String mbeanReportedServiceUrl = managerMBean.getOfficeBuildingJmxServiceUrl();
		assertEquals("Incorrect MBean service URL", expectedServiceUrl, mbeanReportedServiceUrl);

		// Ensure correct host and port
		String mbeanReportedHostName = managerMBean.getOfficeBuildingHostName();
		assertEquals("Incorrect MBean host name", hostName, mbeanReportedHostName);
		int mbeanReportedPort = managerMBean.getOfficeBuildingPort();
		assertEquals("Incorrect MBean port", this.port, mbeanReportedPort);

		// Ensure no processes running
		String[] processNamespaces = managerMBean.listProcessNamespaces();
		assertEquals("Should be no processes running", 0, processNamespaces.length);

		// OfficeBuilding should still be available
		assertTrue("OfficeBuilding should still be available", OfficeBuildingManager.isOfficeBuildingAvailable(null,
				this.port, this.trustStore, this.trustStorePassword, this.username, this.password));

		// Stop the Office Building
		String stopDetails = managerMBean.stopOfficeBuilding(10000);
		assertEquals("Incorrect stop details", "OfficeBuilding stopped", stopDetails);

		// OfficeBuilding now not be available
		assertFalse("OfficeBuilding should be stopped and therefore unavailable",
				OfficeBuildingManager.isOfficeBuildingAvailable(null, this.port, this.trustStore,
						this.trustStorePassword, this.username, this.password));
	}

	/**
	 * Ensure able to open the configured {@link OfficeFloor}.
	 */
	public void testEnsureOfficeFloorOpens() throws Exception {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.addSourceAliases();
		compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
		compiler.setOfficeFloorLocation(this.getOfficeFloorLocation());
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");
		officeFloor.openOfficeFloor();
		assertNotNull("Should have function",
				officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.writeMessage"));
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} with a Jar.
	 */
	public void testOfficeFloorJarManagement() throws Exception {
		this.doOfficeFloorManagementTest(new OfficeFloorOpener() {
			@Override
			public String openOfficeFloor(String processName, String officeFloorLocation,
					OfficeBuildingManagerMBean buildingManager) throws Exception {
				OpenOfficeFloorConfiguration config = new OpenOfficeFloorConfiguration();
				config.setOfficeFloorName(processName);
				config.setOfficeFloorSourceClassName(OfficeFloorModelOfficeFloorSource.class.getName());
				config.setOfficeFloorLocation(officeFloorLocation);
				config.addUploadArtifact(
						new UploadArtifact(OfficeBuildingManagerTest.this.findFile("lib/MockCore.jar")));
				return buildingManager.openOfficeFloor(config);
			}
		});
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} with JMX string command.
	 */
	public void testOfficeFloorJmxManagement() throws Exception {
		this.doOfficeFloorManagementTest(new OfficeFloorOpener() {
			@Override
			public String openOfficeFloor(String processName, String officeFloorLocation,
					OfficeBuildingManagerMBean buildingManager) throws Exception {
				return buildingManager.openOfficeFloor("--officefloorsource "
						+ OfficeFloorModelOfficeFloorSource.class.getName() + " --location " + officeFloorLocation);
			}
		});
	}

	/**
	 * Opens the {@link OfficeFloor}.
	 */
	private static interface OfficeFloorOpener {

		/**
		 * Opens the {@link OfficeFloor} via the
		 * {@link OfficeBuildingManagerMBean}.
		 * 
		 * @param processName
		 *            Process name.
		 * @param officeFloorLocation
		 *            {@link OfficeFloor} location.
		 * @param buildingManager
		 *            {@link OfficeBuildingManagerMBean}.
		 * @return Process namespace.
		 * @throws Exception
		 *             If fails to open.
		 */
		String openOfficeFloor(String processName, String officeFloorLocation,
				OfficeBuildingManagerMBean buildingManager) throws Exception;
	}

	/**
	 * Ensure able to open the {@link OfficeFloor}.
	 * 
	 * @param opener
	 *            {@link OfficeFloorOpener}.
	 */
	private void doOfficeFloorManagementTest(OfficeFloorOpener opener) throws Exception {

		// Start the OfficeBuilding
		this.startOfficeBuilding();

		// Obtain the manager MBean
		OfficeBuildingManagerMBean buildingManager = OfficeBuildingManager.getOfficeBuildingManager(null, this.port,
				this.trustStore, this.trustStorePassword, this.username, this.password);

		// Open the OfficeFloor
		String officeFloorLocation = this.getOfficeFloorLocation();
		String processNamespace = opener.openOfficeFloor(this.getName(), officeFloorLocation, buildingManager);

		// Ensure process running
		String[] processNamespaces = buildingManager.listProcessNamespaces();
		assertEquals("Incorrect number of processes running", 1, processNamespaces.length);
		assertEquals("Incorrect process running", processNamespace, processNamespaces[0]);

		// Ensure OfficeFloor opened (obtaining local floor manager)
		OfficeFloorManagerMBean localFloorManager = OfficeBuildingManager.getOfficeFloorManager(null, this.port,
				processNamespace, this.trustStore, this.trustStorePassword, this.username, this.password);

		// Obtain the local Process Manager MBean
		ProcessManagerMBean processManager = OfficeBuildingManager.getProcessManager(null, this.port, processNamespace,
				this.trustStore, this.trustStorePassword, this.username, this.password);

		// Obtain the local Process Shell MBean
		ProcessShellMBean localProcessShell = OfficeBuildingManager.getProcessShell(null, this.port, processNamespace,
				this.trustStore, this.trustStorePassword, this.username, this.password);

		// Wait for OfficeFloor to open to obtain OfficeFloor
		OfficeBuildingTestUtil.waitUntilOfficeFloorOpens(localFloorManager, processManager,
				ManagementFactory.getPlatformMBeanServer());
		OfficeFloorMBean localOfficeFloor = OfficeBuildingManager.getOfficeFloor(null, this.port, processNamespace,
				this.trustStore, this.trustStorePassword, this.username, this.password);

		// Validate the process host and port
		String remoteHostName = processManager.getProcessHostName();
		int remotePort = processManager.getProcessPort();
		String serviceUrlValue = localProcessShell.getJmxConnectorServiceUrl();
		JMXServiceURL remoteServiceUrl = new JMXServiceURL(serviceUrlValue);
		assertEquals("Incorrect process host", remoteServiceUrl.getHost(), remoteHostName);
		assertEquals("Incorrect process port", remoteServiceUrl.getPort(), remotePort);

		// Ensure OfficeFloor running
		this.validateRemoteProcessRunning(remoteServiceUrl);

		// Ensure the functions are available
		String[] officeNames = localOfficeFloor.getOfficeNames();
		assertEquals("Incorrect number of offices", 1, officeNames.length);
		assertEquals("Incorrect office name", "OFFICE", officeNames[0]);
		String[] functionNames = localOfficeFloor.getManagedFunctionNames("OFFICE");
		assertEquals("Incorrect number of functions", 1, functionNames.length);
		assertEquals("Incorrect function name", "SECTION.writeMessage", functionNames[0]);

		// Invoke the function
		File file = OfficeBuildingTestUtil.createTempFile(this);
		localOfficeFloor.invokeFunction("OFFICE", "SECTION.writeMessage", file.getAbsolutePath());

		// Ensure work invoked (content in file)
		OfficeBuildingTestUtil.validateFileContent("Work should be invoked", MockWork.MESSAGE, file);

		// Obtain expected details of stopping the OfficeBuilding
		String expectedStopDetails = "Stopping processes:\n\t" + processManager.getProcessName() + " ["
				+ processManager.getProcessNamespace() + "]\n\nOfficeBuilding stopped";

		// Stop the OfficeBuilding
		String stopDetails = buildingManager.stopOfficeBuilding(10000);
		assertEquals("Ensure correct stop details", expectedStopDetails, stopDetails);

		// Ensure the OfficeFloor process is also stopped
		this.validateRemoteProcessStopped(remoteServiceUrl);
	}

	/**
	 * Ensure can close the {@link OfficeFloor}.
	 */
	public void testCloseOfficeFloor() throws Exception {

		// Start the OfficeBuilding
		this.startOfficeBuilding();

		// Obtain the manager MBean
		OfficeBuildingManagerMBean buildingManager = OfficeBuildingManager.getOfficeBuildingManager(null, this.port,
				this.trustStore, this.trustStorePassword, this.username, this.password);

		// Open the OfficeFloor
		String officeFloorLocation = this.getOfficeFloorLocation();
		OpenOfficeFloorConfiguration openConfiguration = new OpenOfficeFloorConfiguration();
		openConfiguration.setOfficeFloorSourceClassName(OfficeFloorModelOfficeFloorSource.class.getName());
		openConfiguration.setOfficeFloorLocation(officeFloorLocation);
		String processNamespace = buildingManager.openOfficeFloor(openConfiguration);

		// Ensure OfficeFloor opened (obtaining local floor manager)
		OfficeFloorManagerMBean localFloorManager = OfficeBuildingManager.getOfficeFloorManager(null, this.port,
				processNamespace, this.trustStore, this.trustStorePassword, this.username, this.password);
		assertNotNull("Must have OfficeFloor manager", localFloorManager);

		// Obtain the local process shell
		JMXConnector localConnector = connectToJmxAgent(
				new JMXServiceURL(buildingManager.getOfficeBuildingJmxServiceUrl()), true);
		MBeanServerConnection localMBeanServer = localConnector.getMBeanServerConnection();
		ProcessShellMBean localProcessShell = JMX.newMBeanProxy(localMBeanServer, ProcessManager.getLocalObjectName(
				processNamespace, ProcessShell.getProcessShellObjectName(processNamespace)), ProcessShellMBean.class);

		// Obtain the remote process JMX service URL
		JMXServiceURL remoteServiceUrl = new JMXServiceURL(localProcessShell.getJmxConnectorServiceUrl());

		// Ensure the OfficeFloor process is running
		this.validateRemoteProcessRunning(remoteServiceUrl);

		// Close the OfficeFloor
		buildingManager.closeOfficeFloor(processNamespace, 3000);

		// Ensure the OfficeFloor process is closed
		this.validateRemoteProcessStopped(remoteServiceUrl);

		// Stop the building manager
		buildingManager.stopOfficeBuilding(10000);
	}

	/**
	 * Ensure able to spawn the {@link OfficeBuilding}.
	 */
	public void testSpawnOfficeBuilding() throws Exception {

		// Spawn the OfficeBuilding
		ProcessManager process = OfficeBuildingManager.spawnOfficeBuilding(null, this.port, this.trustStore,
				this.trustStorePassword, this.username, this.password, null, false, null, null, false, null);
		try {

			// Ensure the OfficeBuilding is available
			assertTrue("OfficeBuilding should be available", OfficeBuildingManager.isOfficeBuildingAvailable(null,
					this.port, this.trustStore, this.trustStorePassword, this.username, this.password));

			// Stop the spawned OfficeBuilding
			OfficeBuildingManagerMBean manager = OfficeBuildingManager.getOfficeBuildingManager(null, this.port,
					this.trustStore, this.trustStorePassword, this.username, this.password);
			manager.stopOfficeBuilding(1000);

			// Ensure the OfficeBuilding stopped
			assertFalse("OfficeBuilding should be stopped", OfficeBuildingManager.isOfficeBuildingAvailable(null,
					this.port, this.trustStore, this.trustStorePassword, this.username, this.password));

		} finally {
			// Ensure process stopped
			process.destroyProcess();
		}
	}

	/**
	 * Obtains the {@link OfficeFloor} location.
	 * 
	 * @return {@link OfficeFloor} location.
	 */
	private String getOfficeFloorLocation() {
		return this.getClass().getPackage().getName().replace('.', '/') + "/TestOfficeFloor.officefloor";
	}

	/**
	 * Validates {@link ProcessShellMBean} is running.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL} to determine if running.
	 */
	private void validateRemoteProcessRunning(JMXServiceURL serviceUrl) throws IOException {
		try {
			this.connectToJmxAgent(serviceUrl, false);
			fail("Security should prevent connection to running remote process");
		} catch (SecurityException ex) {
			assertEquals("Incorrect cause", "Bad credentials", ex.getMessage());
		}
	}

	/**
	 * Validate {@link ProcessShellMBean} is stopped.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL} to determine if stopped.
	 */
	private void validateRemoteProcessStopped(JMXServiceURL serviceUrl) throws InterruptedException {

		// Allow time for process to stop (10 seconds)
		long endTime = System.currentTimeMillis() + 10000;
		while (System.currentTimeMillis() < endTime) {

			try {
				this.connectToJmxAgent(serviceUrl, true);
				fail("Should not connect to stopped remote process");

			} catch (NoSuchObjectException ex) {
				// In process of shutting down (keep waiting)

			} catch (ConnectException ex) {
				assertEquals("Incorrect cause", "Connection refused", ex.getCause().getMessage());
				return; // successfully identified as closed

			} catch (UnmarshalException ex) {
				assertTrue("Incorrect cause " + ex.getCause().getClass().getName(),
						(ex.getCause() instanceof EOFException));
				return; // successfully identified as closed

			} catch (IOException ex) {
				fail("Should only be ConnectionException but was " + ex.getMessage() + " [" + ex.getClass().getName()
						+ "]");
			}

			// Allow some time for process to complete
			Thread.sleep(100);
		}

		// As here process failed to stop in time
		fail("Process took too long to stop");
	}

	/**
	 * Connects to the JMX agent.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL}.
	 * @param isSecure
	 *            Indicates if to provide security details to connect.
	 * @return {@link JMXConnector}.
	 */
	private JMXConnector connectToJmxAgent(JMXServiceURL serviceUrl, boolean isSecure) throws IOException {
		Map<String, Object> environment = new HashMap<String, Object>();
		if (isSecure) {
			byte[] keyStoreContent = OfficeBuildingRmiServerSocketFactory.getKeyStoreContent(this.trustStore);
			RMIClientSocketFactory socketFactory = new OfficeBuildingRmiClientSocketFactory(
					OfficeBuildingManager.getSslProtocol(), OfficeBuildingManager.getSslAlgorithm(), keyStoreContent,
					this.trustStorePassword);
			environment.put("com.sun.jndi.rmi.factory.socket", socketFactory);
			environment.put(JMXConnector.CREDENTIALS, new String[] { this.username, this.password });
		}
		return JMXConnectorFactory.connect(serviceUrl, environment);
	}

}