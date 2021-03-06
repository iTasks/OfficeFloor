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

package net.officefloor.compile.impl.managedfunction;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;

/**
 * {@link ManagedFunctionObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionObjectTypeImpl<M extends Enum<M>>
		implements ManagedFunctionObjectType<M>, ManagedFunctionObjectTypeBuilder<M> {

	/**
	 * Type of the dependency {@link Object}.
	 */
	private final Class<?> objectType;

	/**
	 * Type qualifier.
	 */
	private String typeQualifier;

	/**
	 * Label describing this {@link ManagedFunctionObjectType}.
	 */
	private String label = null;

	/**
	 * Index identifying this {@link ManagedFunctionObjectType}.
	 */
	private int index;

	/**
	 * {@link Enum} key identifying this {@link ManagedFunctionObjectType}.
	 */
	private M key = null;

	/**
	 * Annotations.
	 */
	private final List<Object> annotations = new LinkedList<>();

	/**
	 * Initiate with the index of the {@link ManagedFunctionObjectType}.
	 * 
	 * @param objectType Type of the dependency {@link Object}.
	 * @param index      Index identifying this {@link ManagedFunctionObjectType}.
	 */
	public ManagedFunctionObjectTypeImpl(Class<?> objectType, int index) {
		this.objectType = objectType;
		this.index = index;
	}

	/*
	 * ================= ManagedFunctionObjectTypeBuilder =================
	 */

	@Override
	public ManagedFunctionObjectTypeBuilder<M> setKey(M key) {
		this.key = key;
		this.index = key.ordinal();
		return this;
	}

	@Override
	public ManagedFunctionObjectTypeBuilder<M> setTypeQualifier(String qualifier) {
		this.typeQualifier = qualifier;
		return this;
	}

	@Override
	public ManagedFunctionObjectTypeBuilder<M> setLabel(String label) {
		this.label = label;
		return this;
	}

	@Override
	public ManagedFunctionObjectTypeBuilder<M> addAnnotation(Object annotation) {
		this.annotations.add(annotation);
		return this;
	}

	/*
	 * ================== ManagedFunctionObjectType ==================
	 */

	@Override
	public String getObjectName() {
		// Follow priorities to obtain the object name
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.key != null) {
			return this.key.toString();
		} else {
			return String.valueOf(this.index);
		}
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public M getKey() {
		return this.key;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations.toArray(new Object[this.annotations.size()]);
	}

}
