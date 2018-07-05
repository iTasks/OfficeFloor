/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Raw meta-data for a bound {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaData {

	/**
	 * Recursively loads all the {@link ManagedObjectIndex} instances for the
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundMo
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param requiredManagedObjects
	 *            Mapping of the required {@link ManagedObjectIndex} instances
	 *            by the {@link ManagedFunction} to their respective
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @return {@link ManagedObjectIndex} of the input
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	public static ManagedObjectIndex loadRequiredManagedObjects(RawBoundManagedObjectMetaData boundMo,
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects) {

		// Obtain the bound managed object index
		ManagedObjectIndex boundMoIndex = boundMo.getManagedObjectIndex();
		if (!requiredManagedObjects.containsKey(boundMoIndex)) {

			// Not yet required, so add and include all its dependencies
			requiredManagedObjects.put(boundMoIndex, boundMo);
			for (RawBoundManagedObjectInstanceMetaData<?> boundMoInstance : boundMo
					.getRawBoundManagedObjectInstanceMetaData()) {
				RawBoundManagedObjectMetaData[] dependencies = boundMoInstance.getDependencies();
				if (dependencies != null) {
					for (RawBoundManagedObjectMetaData dependency : dependencies) {
						loadRequiredManagedObjects(dependency, requiredManagedObjects);
					}
				}
			}
		}

		// Return the managed object index for the bound managed object
		return boundMoIndex;
	}

	/**
	 * <p>
	 * Sorts the required {@link ManagedObjectIndex} instances for the
	 * {@link ManagedFunction} so that dependency {@link ManagedObject}
	 * instances are before the {@link ManagedObject} instances using them. In
	 * essence this is a topological sort so that dependencies are first.
	 * <p>
	 * This is necessary for coordinating so that dependencies are coordinated
	 * before the {@link ManagedObject} instances using them are coordinated.
	 * 
	 * @param requiredManagedObjects
	 *            Mapping of the {@link ManagedObjectIndex} to its
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of asset to issues.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return Listing of required {@link ManagedObject} instances to be sorted,
	 *         or <code>null</code> indicating unable to sort, possible because
	 *         of cyclic dependencies.
	 */
	public static ManagedObjectIndex[] createSortedRequiredManagedObjects(
			final Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects, AssetType assetType,
			String assetName, OfficeFloorIssues issues) {

		// Create the required managed object indexes
		ManagedObjectIndex[] requiredManagedObjectIndexes = new ManagedObjectIndex[requiredManagedObjects.size()];
		int requiredIndex = 0;
		for (ManagedObjectIndex requiredManagedObjectIndex : requiredManagedObjects.keySet()) {
			requiredManagedObjectIndexes[requiredIndex++] = requiredManagedObjectIndex;
		}

		// Initially sort by scope and index
		Arrays.sort(requiredManagedObjectIndexes, new Comparator<ManagedObjectIndex>() {
			@Override
			public int compare(ManagedObjectIndex a, ManagedObjectIndex b) {
				int value = a.getManagedObjectScope().ordinal() - b.getManagedObjectScope().ordinal();
				if (value == 0) {
					value = a.getIndexOfManagedObjectWithinScope() - b.getIndexOfManagedObjectWithinScope();
				}
				return value;
			}
		});

		// Create the set of dependencies for each required managed object
		final Map<ManagedObjectIndex, Set<ManagedObjectIndex>> dependencies = new HashMap<ManagedObjectIndex, Set<ManagedObjectIndex>>();
		for (ManagedObjectIndex index : requiredManagedObjectIndexes) {

			// Obtain the managed object for index
			RawBoundManagedObjectMetaData managedObject = requiredManagedObjects.get(index);

			// Load the dependencies
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> moDependencies = new HashMap<ManagedObjectIndex, RawBoundManagedObjectMetaData>();
			loadRequiredManagedObjects(managedObject, moDependencies);

			// Register the dependencies for the index
			dependencies.put(index, new HashSet<ManagedObjectIndex>(moDependencies.keySet()));
		}

		try {
			// Sort so dependencies are first (detecting cyclic dependencies)
			Arrays.sort(requiredManagedObjectIndexes, new Comparator<ManagedObjectIndex>() {
				@Override
				public int compare(ManagedObjectIndex a, ManagedObjectIndex b) {

					// Obtain the dependencies
					Set<ManagedObjectIndex> aDep = dependencies.get(a);
					Set<ManagedObjectIndex> bDep = dependencies.get(b);

					// Determine dependency relationship
					boolean isAdepB = bDep.contains(a);
					boolean isBdepA = aDep.contains(b);

					// Compare based on relationship
					if (isAdepB && isBdepA) {
						// Cyclic dependency
						String[] names = new String[2];
						names[0] = requiredManagedObjects.get(a).getBoundManagedObjectName();
						names[1] = requiredManagedObjects.get(b).getBoundManagedObjectName();
						Arrays.sort(names);
						throw new CyclicDependencyException(
								"Can not have cyclic dependencies (" + names[0] + ", " + names[1] + ")");
					} else if (isAdepB) {
						// A dependent on B, so B must come first
						return -1;
					} else if (isBdepA) {
						// B dependent on A, so A must come first
						return 1;
					} else {
						/*
						 * No dependency relationship. As the sorting only
						 * changes on differences (non 0 value) then need means
						 * to differentiate when no dependency relationship.
						 * This is especially the case with the merge sort used
						 * by default by Java.
						 */

						// Least number of dependencies first.
						// Note: this pushes no dependencies to start.
						int value = aDep.size() - bDep.size();
						if (value == 0) {
							// Same dependencies, so base on scope
							value = a.getManagedObjectScope().ordinal() - b.getManagedObjectScope().ordinal();
							if (value == 0) {
								// Same scope, so arbitrary order
								value = a.getIndexOfManagedObjectWithinScope() - b.getIndexOfManagedObjectWithinScope();
							}
						}
						return value;
					}
				}
			});

		} catch (CyclicDependencyException ex) {
			// Register issue that cyclic dependency
			issues.addIssue(assetType, assetName, ex.getMessage());

			// Not sorted as cyclic dependency
			return null;
		}

		// Return the sorted required managed object indexes
		return requiredManagedObjectIndexes;
	}

	/**
	 * Thrown to indicate a cyclic dependency.
	 */
	private static class CyclicDependencyException extends RuntimeException {

		/**
		 * Initiate.
		 * 
		 * @param message
		 *            Initiate with description for {@link OfficeFloorIssues}.
		 */
		public CyclicDependencyException(String message) {
			super(message);
		}
	}

	/**
	 * Name that the {@link ManagedObject} is bound under.
	 */
	private final String boundManagedObjectName;

	/**
	 * Indicates if an Input {@link ManagedObject}.
	 */
	private final boolean isInput;

	/**
	 * Bound {@link ManagedObjectIndex} for the {@link ManagedObject}.
	 */
	private ManagedObjectIndex index = null;

	/**
	 * Listing of {@link RawBoundManagedObjectInstanceMetaData} instances for
	 * this {@link RawBoundManagedObjectMetaData}.
	 */
	final List<RawBoundManagedObjectInstanceMetaData<?>> instancesMetaData = new LinkedList<RawBoundManagedObjectInstanceMetaData<?>>();

	/**
	 * Default instance index.
	 */
	int defaultInstanceIndex = -1;

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param isInput
	 *            Indicates if an Input {@link ManagedObject}.
	 */
	public RawBoundManagedObjectMetaData(String boundManagedObjectName, boolean isInput) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.isInput = isInput;
	}

	/**
	 * Indicates if input.
	 * 
	 * @return <code>true</code> if input.
	 */
	public boolean isInput() {
		return this.isInput;
	}

	/**
	 * Specifies the {@link ManagedObjectIndex}.
	 * 
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param indexOfManagedObjectWithinScope
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	public void setManagedObjectIndex(ManagedObjectScope managedObjectScope, int indexOfManagedObjectWithinScope) {
		this.index = new ManagedObjectIndexImpl(managedObjectScope, indexOfManagedObjectWithinScope);
	}

	/**
	 * Adds a {@link RawBoundManagedObjectInstanceMetaData} to this
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param dependenciesConfiguration
	 *            Listing of the {@link ManagedObjectDependencyConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param governanceConfiguration
	 *            Listing of the {@link ManagedObjectGovernanceConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param preloadAdministration
	 *            Listing of the pre-load {@link AdministrationConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @return {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	public RawBoundManagedObjectInstanceMetaData<?> addInstance(String boundManagedObjectName,
			RawManagedObjectMetaData<?, ?> rawMoMetaData,
			ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration,
			ManagedObjectGovernanceConfiguration[] governanceConfiguration,
			AdministrationConfiguration<?, ?, ?>[] preloadAdministration) {

		// Obtain the index for the instance
		int instanceIndex = this.instancesMetaData.size();

		// Add the instance meta-data
		RawBoundManagedObjectInstanceMetaData<?> instance = new RawBoundManagedObjectInstanceMetaData<>(
				boundManagedObjectName, this, instanceIndex, rawMoMetaData, dependenciesConfiguration,
				governanceConfiguration, preloadAdministration);
		this.instancesMetaData.add(instance);
		return instance;
	}

	/**
	 * Obtains the name the {@link ManagedObject} is bound under.
	 *
	 * @return Name the {@link ManagedObject} is bound under.
	 */
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	/**
	 * Obtains the {@link ManagedObjectIndex}.
	 *
	 * @return {@link ManagedObjectIndex}.
	 */
	public ManagedObjectIndex getManagedObjectIndex() {
		return this.index;
	}

	/**
	 * Obtains the index of the default
	 * {@link RawBoundManagedObjectInstanceMetaData} for this
	 * {@link RawBoundManagedObjectMetaData}.
	 *
	 * @return Index of the default
	 *         {@link RawBoundManagedObjectInstanceMetaData} for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	public int getDefaultInstanceIndex() {
		return this.defaultInstanceIndex;
	}

	/**
	 * Obtains the {@link RawBoundManagedObjectInstanceMetaData} instances for
	 * the {@link ManagedObjectSource} instances that may provide a
	 * {@link ManagedObject} for this {@link RawBoundManagedObjectMetaData}.
	 *
	 * @return {@link RawBoundManagedObjectMetaData} instances for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	public RawBoundManagedObjectInstanceMetaData<?>[] getRawBoundManagedObjectInstanceMetaData() {
		// Provide the instances in order of their indexes
		return this.instancesMetaData.toArray(new RawBoundManagedObjectInstanceMetaData[0]);
	}

}