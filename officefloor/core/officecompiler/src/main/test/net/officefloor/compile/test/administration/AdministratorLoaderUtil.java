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
package net.officefloor.compile.test.administration;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceSpecification;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Utility class for testing the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorLoaderUtil {

	/**
	 * Validates the {@link AdministrationSourceSpecification} for the
	 * {@link AdministrationSource}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            {@link Flow} key type.
	 * @param <G>
	 *            {@link Governance} key type.
	 * @param administrationSourceClass
	 *            {@link AdministrationSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> PropertyList validateSpecification(
			Class<S> administrationSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler().getAdministrationLoader()
				.loadSpecification(administrationSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link AdministrationTypeBuilder} to create the expected
	 * {@link AdministrationType}.
	 * 
	 * @return {@link AdministrationTypeBuilder}.
	 */
	public static <E, F extends Enum<F>, G extends Enum<G>> AdministrationTypeBuilder<F, G> createAdministrationTypeBuilder(
			Class<E> extensionInterface, Class<F> flowKeyClass, Class<G> governanceKeyClass) {
		return new AdministrationTypeBuilderImpl<E, F, G>(extensionInterface, flowKeyClass, governanceKeyClass);
	}

	/**
	 * Validates the {@link AdministrationType} contained in the
	 * {@link AdministrationTypeBuilder} against the {@link AdministrationType}
	 * loaded from the {@link AdministrationSource}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            {@link Flow} key type.
	 * @param <S>
	 *            {@link AdministrationSource} type
	 * @param expectedAdministratorType
	 *            Expected {@link AdministrationType}.
	 * @param administratorSourceClass
	 *            {@link AdministrationSource} class.
	 * @param propertyNameValues
	 *            Properties to configure the {@link AdministrationSource}..
	 * @return {@link AdministrationType} loaded from the
	 *         {@link AdministrationSource}.
	 */
	@SuppressWarnings("unchecked")
	public static <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationType<E, F, G> validateAdministratorType(
			AdministrationTypeBuilder<F, G> expectedAdministratorType, Class<S> administratorSourceClass,
			String... propertyNameValues) {

		// Cast to obtain expected administrator type
		if (!(expectedAdministratorType instanceof AdministrationType)) {
			Assert.fail("builder must be created from createAdministrationTypeBuilder");
		}
		AdministrationType<E, F, G> eType = (AdministrationType<E, F, G>) expectedAdministratorType;

		// Load the administration type
		AdministrationType<E, F, G> aType = loadAdministrationType(administratorSourceClass, propertyNameValues);

		// Ensure correct administrator type
		Assert.assertEquals("Incorrect extension interface type", eType.getExtensionInterface(),
				aType.getExtensionInterface());

		// Validate the type
		Assert.assertEquals("Incorrect extension interface", eType.getExtensionInterface(),
				aType.getExtensionInterface());
		Assert.assertEquals("Incorrect flow key class", eType.getFlowKeyClass(), aType.getFlowKeyClass());
		Assert.assertEquals("Incorrect governance key class", eType.getGovernanceKeyClass(),
				aType.getGovernanceKeyClass());
		Assert.assertNotNull("Must have administratration factory", aType.getAdministrationFactory());

		// Validate the flows
		AdministrationFlowType<?>[] eFlows = eType.getFlowTypes();
		AdministrationFlowType<?>[] aFlows = aType.getFlowTypes();
		for (int f = 0; f < eFlows.length; f++) {
			AdministrationFlowType<?> eFlow = eFlows[f];
			AdministrationFlowType<?> aFlow = aFlows[f];

			String flowLabel = "flow" + f;

			// Validate the flow
			Assert.assertEquals("Incorrect name for " + flowLabel, eFlow.getFlowName(), aFlow.getFlowName());
			Assert.assertEquals("Incorrect index for " + flowLabel, eFlow.getIndex(), aFlow.getIndex());
			Assert.assertEquals("Incorrect key for " + flowLabel, eFlow.getKey(), aFlow.getKey());
			Assert.assertEquals("Incorrect argument type for " + flowLabel, eFlow.getArgumentType(),
					aFlow.getArgumentType());
		}

		// Validate the escalations
		AdministrationEscalationType[] eEscalations = eType.getEscalationTypes();
		AdministrationEscalationType[] aEscalations = aType.getEscalationTypes();
		for (int e = 0; e < eEscalations.length; e++) {
			AdministrationEscalationType eEscalation = eEscalations[e];
			AdministrationEscalationType aEscalation = aEscalations[e];

			String escalationLabel = "escalation " + e;

			// Validate the escalation
			Assert.assertEquals("Incorrect escalation name for " + escalationLabel, eEscalation.getEscalationName(),
					aEscalation.getEscalationName());
			Assert.assertEquals("Incorrect escalation type for " + escalationLabel, eEscalation.getEscalationType(),
					aEscalation.getEscalationType());
		}

		// Validate the governances
		AdministrationGovernanceType<G>[] eGovernances = eType.getGovernanceTypes();
		AdministrationGovernanceType<G>[] aGovernances = aType.getGovernanceTypes();
		for (int g = 0; g < eGovernances.length; g++) {
			AdministrationGovernanceType<G> eGovernance = eGovernances[g];
			AdministrationGovernanceType<G> aGovernance = aGovernances[g];

			String governanceLabel = "governance " + g;

			// Validate the governance
			Assert.assertEquals("Incorrect governance name for " + governanceLabel, eGovernance.getGovernanceName(),
					aGovernance.getGovernanceName());
			Assert.assertEquals("Incorrect index for " + governanceLabel, eGovernance.getIndex(),
					aGovernance.getIndex());
			Assert.assertEquals("Incorrect key for " + governanceLabel, eGovernance.getKey(), aGovernance.getKey());
		}

		// Return the administrator type
		return aType;
	}

	/**
	 * Loads the {@link AdministrationType} from the
	 * {@link AdministrationSource}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            {@link Flow} key type.
	 * @param <G>
	 *            {@link Governance} key type.
	 * @param administrationSourceClass
	 *            {@link AdministrationSource} class.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link AdministrationType}.
	 */
	public static <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationType<E, F, G> loadAdministrationType(
			Class<S> administrationSourceClass, String... propertyNameValues) {

		// Load and return the administration type
		return getOfficeFloorCompiler().getAdministrationLoader().loadAdministrationType(administrationSourceClass,
				new PropertyListImpl(propertyNameValues));
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler() {
		// Create the office floor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private AdministratorLoaderUtil() {
	}

	/**
	 * {@link AdministrationTypeBuilder} implementation.
	 */
	private static class AdministrationTypeBuilderImpl<E, F extends Enum<F>, G extends Enum<G>>
			implements AdministrationTypeBuilder<F, G>, AdministrationType<E, F, G> {

		/**
		 * Extension interface.
		 */
		private Class<E> extensionInterface;

		/**
		 * {@link Flow} {@link Enum}.
		 */
		private final Class<F> flowKeyClass;

		/**
		 * Listing of {@link AdministrationFlowType} instances.
		 */
		private final List<AdministrationFlowType<F>> flows = new LinkedList<>();

		/**
		 * Listing of {@link AdministrationEscalationType} instances.
		 */
		private final List<AdministrationEscalationType> escalations = new LinkedList<>();

		/**
		 * {@link Governance} {@link Enum}.
		 */
		private final Class<G> governanceKeyClass;

		/**
		 * Listing of {@link AdministrationGovernanceType} instances.
		 */
		private final List<AdministrationGovernanceType<G>> governances = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 * @param flowKeyClass
		 *            {@link Flow} {@link Enum}.
		 * @param governanceKeyClass
		 *            {@link Governance} {@link Enum}.
		 */
		public AdministrationTypeBuilderImpl(Class<E> extensionInterface, Class<F> flowKeyClass,
				Class<G> governanceKeyClass) {
			this.extensionInterface = extensionInterface;
			this.flowKeyClass = flowKeyClass;
			this.governanceKeyClass = governanceKeyClass;
		}

		/*
		 * ================ AdministrationTypeBuilder ======================
		 */

		@Override
		public void addFlow(String flowName, Class<?> argumentType, int index, F flowKey) {
			this.flows.add(new AdministrationFlowTypeImpl<F>(flowName, argumentType, index, flowKey));
		}

		@Override
		public void addEscalation(String escalationName, Class<? extends Throwable> escalationType) {
			this.escalations.add(new AdministrationEscalationTypeImpl(escalationName, escalationType));
		}

		@Override
		public void addGovernance(String governanceName, int index, G governanceKey) {
			this.governances.add(new AdministrationGovernanceTypeImpl<G>(governanceName, index, governanceKey));
		}

		/*
		 * ================== AdministrationType ==========================
		 */

		@Override
		public Class<E> getExtensionInterface() {
			return this.extensionInterface;
		}

		@Override
		public AdministrationFactory<E, F, G> getAdministrationFactory() {
			throw new IllegalStateException("Should not require administration factory in validation");
		}

		@Override
		public Class<F> getFlowKeyClass() {
			return this.flowKeyClass;
		}

		@Override
		public AdministrationFlowType<F>[] getFlowTypes() {
			return CompileUtil.toArray(this.flows, new AdministrationFlowType[0]);
		}

		@Override
		public AdministrationEscalationType[] getEscalationTypes() {
			return CompileUtil.toArray(this.escalations, new AdministrationEscalationType[0]);
		}

		@Override
		public Class<G> getGovernanceKeyClass() {
			return this.governanceKeyClass;
		}

		@Override
		public AdministrationGovernanceType<G>[] getGovernanceTypes() {
			return CompileUtil.toArray(this.governances, new AdministrationGovernanceType[0]);
		}
	}

	/**
	 * {@link AdministrationFlowType} implementation.
	 */
	private static class AdministrationFlowTypeImpl<F extends Enum<F>> implements AdministrationFlowType<F> {

		/**
		 * Name of the {@link Flow}.
		 */
		private final String flowName;

		/**
		 * Argument type to the {@link Flow}.
		 */
		private final Class<?> argumentType;

		/**
		 * Index identifying the {@link Flow}.
		 */
		private final int index;

		/**
		 * Key identifying the {@link Flow}.
		 */
		private final F key;

		/**
		 * Initiate.
		 * 
		 * @param flowName
		 *            Name of the {@link Flow}.
		 * @param argumentType
		 *            Argument type to the {@link Flow}.
		 * @param index
		 *            Index identifying the {@link Flow}.
		 * @param key
		 *            Key identifying the {@link Flow}.
		 */
		public AdministrationFlowTypeImpl(String flowName, Class<?> argumentType, int index, F key) {
			this.flowName = flowName;
			this.argumentType = argumentType;
			this.index = index;
			this.key = key;
		}

		/*
		 * ==================== AdministrationFlowType =======================
		 */

		@Override
		public String getFlowName() {
			return this.flowName;
		}

		@Override
		public Class<?> getArgumentType() {
			return this.argumentType;
		}

		@Override
		public int getIndex() {
			return this.index;
		}

		@Override
		public F getKey() {
			return this.key;
		}
	}

	/**
	 * {@link AdministrationEscalationType} implementation.
	 */
	private static class AdministrationEscalationTypeImpl implements AdministrationEscalationType {

		/**
		 * {@link Escalation} name.
		 */
		private final String escalationName;

		/**
		 * {@link Escalation} type.
		 */
		private final Class<? extends Throwable> escalationType;

		/**
		 * Instantiate.
		 * 
		 * @param escalationName
		 *            {@link Escalation} name.
		 * @param escalationType
		 *            {@link Escalation} type.
		 */
		public AdministrationEscalationTypeImpl(String escalationName, Class<? extends Throwable> escalationType) {
			this.escalationName = escalationName;
			this.escalationType = escalationType;
		}

		/*
		 * ================= AdministrationEscalationType ===============
		 */

		@Override
		public String getEscalationName() {
			return this.escalationName;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> Class<E> getEscalationType() {
			return (Class<E>) this.escalationType;
		}
	}

	/**
	 * {@link AdministrationGovernanceType} implementation.
	 */
	private static class AdministrationGovernanceTypeImpl<G extends Enum<G>>
			implements AdministrationGovernanceType<G> {

		/**
		 * Name of {@link Governance}.
		 */
		private final String governanceName;

		/**
		 * Index identifying the {@link AdministrationGovernanceType}.
		 */
		private final int index;

		/**
		 * Key identifying the {@link AdministrationGovernanceType}.
		 */
		private final G key;

		/**
		 * Instantiate.
		 * 
		 * @param governanceName
		 *            Name of {@link Governance}.
		 * @param index
		 *            Index identifying the
		 *            {@link AdministrationGovernanceType}.
		 * @param key
		 *            Key identifying the {@link AdministrationGovernanceType}.
		 */
		public AdministrationGovernanceTypeImpl(String governanceName, int index, G key) {
			this.governanceName = governanceName;
			this.index = index;
			this.key = key;
		}

		/*
		 * ================= AdministrationGovernanceType ============
		 */

		@Override
		public String getGovernanceName() {
			return this.governanceName;
		}

		@Override
		public int getIndex() {
			return this.index;
		}

		@Override
		public G getKey() {
			return this.key;
		}
	}

}