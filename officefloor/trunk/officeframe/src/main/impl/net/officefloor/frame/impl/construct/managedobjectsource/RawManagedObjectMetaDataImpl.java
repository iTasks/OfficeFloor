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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Properties;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceUnknownPropertyError;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Raw {@link ManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public class RawManagedObjectMetaDataImpl<D extends Enum<D>, F extends Enum<F>>
		implements RawManagedObjectMetaDataFactory,
		RawManagedObjectMetaData<D, F> {

	/**
	 * Obtains the {@link RawManagedObjectMetaDataFactory}.
	 * 
	 * @return {@link RawManagedObjectMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
	public static RawManagedObjectMetaDataFactory getFactory() {
		return new RawManagedObjectMetaDataImpl(null, null, null, null, -1,
				null, null, false, false, null);
	}

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	private final ManagedObjectSourceConfiguration<F, ?> managedObjectSourceConfiguration;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<D, F> managedObjectSource;

	/**
	 * {@link ManagedObjectSourceMetaData} for the {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSourceMetaData<D, F> managedObjectSourceMetaData;

	/**
	 * Default timeout for sourcing the {@link ManagedObject} and asynchronous
	 * operations on the {@link ManagedObject}.
	 */
	private final long defaultTimeout;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * Type of the {@link Object} returned from the {@link ManagedObject}.
	 */
	private final Class<?> objectType;

	/**
	 * Flag indicating if {@link AsynchronousManagedObject}.
	 */
	private final boolean isAsynchronous;

	/**
	 * Flag indicating if {@link CoordinatingManagedObject}.
	 */
	private final boolean isCoordinating;

	/**
	 * {@link RawManagingOfficeMetaData}.
	 */
	private final RawManagingOfficeMetaDataImpl<F> rawManagingOfficeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectSourceConfiguration
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectSourceMetaData
	 *            {@link ManagedObjectSourceMetaData} for the
	 *            {@link ManagedObjectSource}.
	 * @param defaultTimeout
	 *            Default timeout for the {@link ManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 * @param objectType
	 *            Type of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param isAsynchronous
	 *            Flag indicating if {@link AsynchronousManagedObject}.
	 * @param isCoordinating
	 *            Flag indicating if {@link CoordinatingManagedObject}.
	 * @param rawManagingOfficeMetaData
	 *            {@link RawManagingOfficeMetaData}.
	 */
	private RawManagedObjectMetaDataImpl(
			String managedObjectName,
			ManagedObjectSourceConfiguration<F, ?> managedObjectSourceConfiguration,
			ManagedObjectSource<D, F> managedObjectSource,
			ManagedObjectSourceMetaData<D, F> managedObjectSourceMetaData,
			long defaultTimeout, ManagedObjectPool managedObjectPool,
			Class<?> objectType, boolean isAsynchronous,
			boolean isCoordinating,
			RawManagingOfficeMetaDataImpl<F> rawManagingOfficeMetaData) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceConfiguration = managedObjectSourceConfiguration;
		this.managedObjectSource = managedObjectSource;
		this.managedObjectSourceMetaData = managedObjectSourceMetaData;
		this.defaultTimeout = defaultTimeout;
		this.managedObjectPool = managedObjectPool;
		this.objectType = objectType;
		this.isAsynchronous = isAsynchronous;
		this.isCoordinating = isCoordinating;
		this.rawManagingOfficeMetaData = rawManagingOfficeMetaData;
	}

	/*
	 * ==================== RawManagedObjectMetaDataFactory ==================
	 */

	@Override
	public <d extends Enum<d>, h extends Enum<h>, MS extends ManagedObjectSource<d, h>> RawManagedObjectMetaData<d, h> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<h, MS> configuration,
			OfficeFloorIssues issues,
			OfficeFloorConfiguration officeFloorConfiguration) {

		// Obtain the managed object source name
		String managedObjectSourceName = configuration
				.getManagedObjectSourceName();
		if (ConstructUtil.isBlank(managedObjectSourceName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, OfficeFloor.class
					.getSimpleName(), "ManagedObject added without a name");
			return null; // can not carry on
		}

		// Obtain the managed object source
		Class<MS> managedObjectSourceClass = configuration
				.getManagedObjectSourceClass();
		if (managedObjectSourceClass == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No ManagedObjectSource class provided");
			return null; // can not carry on
		}

		// Instantiate the managed object source
		MS managedObjectSource = ConstructUtil.newInstance(
				managedObjectSourceClass, ManagedObjectSource.class,
				"Managed Object Source '" + managedObjectSourceName + "'",
				AssetType.MANAGED_OBJECT, managedObjectSourceName, issues);
		if (managedObjectSource == null) {
			return null; // can not carry on
		}

		// Create the resource locator
		ResourceLocator resourceLocator = new ClassLoaderResourceLocator();

		// Obtain the properties to initialise the managed object source
		Properties properties = configuration.getProperties();

		// Obtain the managing office for the managed object source
		ManagingOfficeConfiguration<h> managingOfficeConfiguration = configuration
				.getManagingOfficeConfiguration();
		if (managingOfficeConfiguration == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No managing office configuration");
			return null; // can not carry on
		}
		String officeName = managingOfficeConfiguration.getOfficeName();
		if (ConstructUtil.isBlank(officeName)) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No managing office specified");
			return null; // can not carry on
		}
		OfficeBuilder officeBuilder = null;
		for (OfficeConfiguration officeConfiguration : officeFloorConfiguration
				.getOfficeConfiguration()) {
			if (officeName.equals(officeConfiguration.getOfficeName())) {
				officeBuilder = officeConfiguration.getBuilder();
			}
		}
		if (officeBuilder == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Can not find managing office '" + officeName + "'");
			return null; // can not carry on
		}

		// Obtain the managing office builder
		ManagingOfficeBuilder<h> managingOfficeBuilder = managingOfficeConfiguration
				.getBuilder();

		// Create the context for the managed object source
		ManagedObjectSourceContextImpl<h> context = new ManagedObjectSourceContextImpl<h>(
				managedObjectSourceName, properties, resourceLocator,
				managingOfficeBuilder, officeBuilder);

		try {
			// Initialise the managed object source
			managedObjectSource.init(context);

		} catch (ManagedObjectSourceUnknownPropertyError ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Property '" + ex.getUnknownPropertyName()
							+ "' must be specified");
			return null; // can not carry on

		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Failed to initialise "
							+ managedObjectSourceClass.getName(), ex);
			return null; // can not carry on
		}

		// Flag initialising over
		context.flagInitOver();

		// Obtain the meta-data
		ManagedObjectSourceMetaData<d, h> metaData = managedObjectSource
				.getMetaData();
		if (metaData == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Must provide meta-data");
			return null; // can not carry on
		}

		// Obtain the object type
		Class<?> objectType = metaData.getObjectClass();
		if (objectType == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No object type provided");
			return null; // can not carry on
		}

		// Obtain managed object type to determine details
		Class<?> managedObjectClass = metaData.getManagedObjectClass();
		if (managedObjectClass == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No managed object class provided");
			return null; // can not carry on
		}

		// Determine if asynchronous and/or coordinating
		boolean isManagedObjectAsynchronous = AsynchronousManagedObject.class
				.isAssignableFrom(managedObjectClass);
		boolean isManagedObjectCoordinating = CoordinatingManagedObject.class
				.isAssignableFrom(managedObjectClass);

		// Obtain the default timeout
		long defaultTimeout = configuration.getDefaultTimeout();
		if (defaultTimeout < 0) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Must not have negative default timeout");
			return null; // can not carry on
		}
		if ((isManagedObjectAsynchronous) && (defaultTimeout <= 0)) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Non-zero timeout must be provided for "
							+ AsynchronousManagedObject.class.getSimpleName());
			return null; // can not carry on
		}

		// Obtain the flow meta-data
		ManagedObjectFlowMetaData<h>[] flowMetaDatas = metaData
				.getFlowMetaData();

		// Required process bound name if requires flows
		String processBoundManagedObjectName = null;
		if (RawManagingOfficeMetaDataImpl.isRequireFlows(flowMetaDatas)) {
			// Requires flows, so must be bound to process of office
			processBoundManagedObjectName = managingOfficeConfiguration
					.getProcessBoundManagedObjectName();
			if (ConstructUtil.isBlank(processBoundManagedObjectName)) {
				issues
						.addIssue(AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								"Must specify the process bound name as Managed Object Source requires flows");
				return null; // can not carry on
			}
		}

		// Obtain the managed object pool
		ManagedObjectPool managedObjectPool = configuration
				.getManagedObjectPool();

		// Obtain the recycle work name
		String recycleWorkName = context.getRecycleWorkName();

		// Create the raw managing office meta-data
		RawManagingOfficeMetaDataImpl<h> rawManagingOfficeMetaData = new RawManagingOfficeMetaDataImpl<h>(
				officeName, processBoundManagedObjectName, recycleWorkName,
				flowMetaDatas, managingOfficeConfiguration);

		// Created raw managed object meta-data
		RawManagedObjectMetaDataImpl<d, h> rawMoMetaData = new RawManagedObjectMetaDataImpl<d, h>(
				managedObjectSourceName, configuration, managedObjectSource,
				metaData, defaultTimeout, managedObjectPool, objectType,
				isManagedObjectAsynchronous, isManagedObjectCoordinating,
				rawManagingOfficeMetaData);

		// Make raw managed object available to the raw managing office
		rawManagingOfficeMetaData.setRawManagedObjectMetaData(rawMoMetaData);

		// Return the raw managed object meta-data
		return rawMoMetaData;
	}

	/*
	 * ==================== RawManagedObjectMetaData ===========================
	 */

	@Override
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public ManagedObjectSourceConfiguration<F, ?> getManagedObjectSourceConfiguration() {
		return this.managedObjectSourceConfiguration;
	}

	@Override
	public ManagedObjectSource<D, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectSourceMetaData<D, F> getManagedObjectSourceMetaData() {
		return this.managedObjectSourceMetaData;
	}

	@Override
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public boolean isAsynchronous() {
		return this.isAsynchronous;
	}

	@Override
	public boolean isCoordinating() {
		return this.isCoordinating;
	}

	@Override
	public RawManagingOfficeMetaData<F> getRawManagingOfficeMetaData() {
		return this.rawManagingOfficeMetaData;
	}

	@Override
	public ManagedObjectMetaData<D> createManagedObjectMetaData(
			RawBoundManagedObjectMetaData<D> boundMetaData,
			ManagedObjectIndex[] dependencyMappings,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Obtain the bound name and scope
		String boundName = boundMetaData.getBoundManagedObjectName();
		ManagedObjectScope scope = boundMetaData.getManagedObjectIndex()
				.getManagedObjectScope();

		// Create the source managed object asset manager
		AssetManager sourcingAssetManager = assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT, scope + ":"
						+ boundName, "source", issues);

		// Create operations asset manager only if asynchronous
		AssetManager operationsAssetManager = null;
		if (this.isAsynchronous) {
			// Asynchronous so provide operations manager
			operationsAssetManager = assetManagerFactory.createAssetManager(
					AssetType.MANAGED_OBJECT, scope + ":" + boundName,
					"operations", issues);
		}

		// Create the managed object meta-data
		ManagedObjectMetaDataImpl<D> moMetaData = new ManagedObjectMetaDataImpl<D>(
				boundName, this.objectType, this.managedObjectSource,
				this.managedObjectPool, sourcingAssetManager,
				this.isAsynchronous, operationsAssetManager,
				this.isCoordinating, dependencyMappings, this.defaultTimeout);

		// Have the managed object managed by its managing office
		this.rawManagingOfficeMetaData.manageManagedObject(moMetaData);

		// Return the managed object meta-data
		return moMetaData;
	}

}