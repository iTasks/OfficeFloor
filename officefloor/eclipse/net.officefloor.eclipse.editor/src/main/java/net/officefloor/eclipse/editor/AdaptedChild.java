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
package net.officefloor.eclipse.editor;

import java.net.URL;
import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnectorImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChild<M extends Model> extends AdaptedModel<M> {

	/**
	 * Obtains the {@link ReadOnlyStringProperty} for the label.
	 * 
	 * @return {@link StringProperty} for the label. May be <code>null</code> if no
	 *         label.
	 */
	ReadOnlyProperty<String> getLabel();

	/**
	 * Obtains the {@link StringProperty} to edit the label.
	 * 
	 * @return {@link StringProperty} to edit the label. May be <code>null</code> if
	 *         label not editable.
	 */
	Property<String> getEditLabel();

	/**
	 * Obtains the {@link ChildrenGroup} instances.
	 * 
	 * @return {@link ChildrenGroup} instances.
	 */
	List<ChildrenGroup<M, ?>> getChildrenGroups();

	/**
	 * Obtains the {@link AdaptedConnectorImpl} instances.
	 * 
	 * @return {@link AdaptedConnectorImpl} instances.
	 */
	List<AdaptedConnector<M>> getAdaptedConnectors();

	/**
	 * Obtains the {@link AdaptedConnectorImpl}.
	 * 
	 * @param connectionClass
	 *            {@link ConnectionModel} {@link Class}.
	 * @param type
	 *            {@link AdaptedConnectorRole}.
	 * @return {@link AdaptedConnectorImpl}.
	 */
	AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
			AdaptedConnectorRole type);

	/**
	 * Obtains the {@link AdaptedConnection} instances of this {@link AdaptedChild}
	 * and all its {@link AdaptedChild} instances.
	 * 
	 * @return {@link AdaptedConnection} instances.
	 */
	List<AdaptedConnection<?>> getConnections();

	/**
	 * Obtains the {@link AdaptedPotentialConnection} to the target.
	 * 
	 * @param <T>
	 *            Target {@link Model} type.
	 * @param target
	 *            Target {@link AdaptedChild},
	 * @return {@link AdaptedPotentialConnection} to the target or <code>null</code>
	 *         if no means to connect to target.
	 */
	<T extends Model> AdaptedPotentialConnection getPotentialConnection(AdaptedChild<T> target);

	/**
	 * Creates the {@link ConnectionModel} within the {@link Model} structure.
	 * 
	 * @param <T>
	 *            Target {@link Model} type.
	 * @param target
	 *            Target {@link AdaptedChild}.
	 * @param sourceRole
	 *            {@link AdaptedConnectorRole} of the this source
	 *            {@link AdaptedChild}.
	 */
	<T extends Model> void createConnection(AdaptedChild<T> target, AdaptedConnectorRole sourceRole);

	/**
	 * Creates the visual {@link Pane}.
	 * 
	 * @param context
	 *            {@link AdaptedModelVisualFactoryContext}.
	 * @return Visual {@link Node}.
	 */
	Node createVisual(AdaptedModelVisualFactoryContext<M> context);

	/**
	 * Obtains the {@link Property} to the style sheet for this
	 * {@link AdaptedChild}.
	 * 
	 * @return {@link Property} to the style sheet for this {@link AdaptedChild}.
	 */
	Property<String> getStylesheet();

	/**
	 * <p>
	 * Obtains the {@link Property} to the style sheet URL for this visual of this
	 * {@link AdaptedChild}.
	 * <p>
	 * May be <code>null</code> to indicate no specific styling.
	 * 
	 * @return {@link ReadOnlyProperty} to the style sheet {@link URL}. May be
	 *         <code>null</code>.
	 */
	ReadOnlyProperty<URL> getStylesheetUrl();

	/**
	 * Undertakes the {@link ModelAction}.
	 *
	 * @param <R>
	 *            Root {@link Model} type.
	 * @param <O>
	 *            Operations type.
	 * @param action
	 *            {@link ModelAction}.
	 */
	<R extends Model, O> void action(ModelAction<R, O, M> action);

	/**
	 * Obtains the {@link AdaptedErrorHandler}.
	 * 
	 * @return {@link AdaptedErrorHandler}.
	 */
	AdaptedErrorHandler getErrorHandler();

	/**
	 * Obtains whether {@link SelectOnly}.
	 * 
	 * @return {@link SelectOnly} or <code>null</code> to allow functionality.
	 */
	SelectOnly getSelectOnly();

}