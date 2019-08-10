/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.gef.ide;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.gef.mvc.fx.domain.IDomain;

import com.google.inject.Inject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.gef.bridge.ClassLoaderEnvironmentBridge;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.Configurer;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem.ConfigurableModelContext;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem.IdeConfiguration;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem.ItemConfigurer;
import net.officefloor.gef.ide.editor.AbstractItem.ConfigurableContext;
import net.officefloor.gef.ide.preferences.PreferencesEditor;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Abstract IDE Editor {@link Application}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractIdeTestApplication<R extends Model, RE extends Enum<RE>, O> extends Application {

	/**
	 * Decorators for the of {@link AbstractConfigurableItem} prototypes.
	 */
	private final Map<Class<?>, Consumer<?>> prototypeDecorators = new HashMap<>();

	@Inject
	private IDomain domain;

	/**
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param <R>       Root {@link Model}.
	 * @param <RE>      Root event {@link Enum}.
	 * @param <O>       Operations.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	protected abstract AbstractAdaptedIdeEditor<R, RE, O> createEditor(EnvironmentBridge envBridge);

	/**
	 * Obtains path to the {@link WritableConfigurationItem}.
	 * 
	 * @return Path to the {@link WritableConfigurationItem}.
	 */
	protected abstract String getConfigurationFileName();

	/**
	 * Registers a prototype decorator.
	 * 
	 * @param <P>           Prototype type.
	 * @param prototypeType Prototype {@link Class}.
	 * @param decorator     Prototype decorator.
	 */
	protected <P> void register(Class<P> prototypeType, Consumer<P> decorator) {
		this.prototypeDecorators.put(prototypeType, decorator);
	}

	/*
	 * =============== Application =============================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Setup environment
		EnvironmentBridge envBridge = new ClassLoaderEnvironmentBridge(this.getClass().getClassLoader());

		// Obtain the configuration
		String configurationPath = this.getClass().getPackageName().replace('.', '/') + "/"
				+ this.getConfigurationFileName();
		InputStream configurationContent = envBridge.getClassLoader().getResourceAsStream(configurationPath);
		if (configurationContent == null) {
			throw new FileNotFoundException("Can not find configuration on class path: " + configurationPath);
		}

		// Obtain the configuration item
		WritableConfigurationItem configurationItem = MemoryConfigurationContext
				.createWritableConfigurationItem(configurationPath);
		configurationItem.setConfiguration(configurationContent);

		// Create tabs for various items being tested
		TabPane folder = new TabPane();

		// Load the editor
		Tab editorTab = new Tab("Editor");
		folder.getTabs().add(editorTab);
		AbstractAdaptedIdeEditor<R, RE, O> editor = this.createEditor(envBridge);
		editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});
		editor.setConfigurationItem(configurationItem);

		// Display stage (required by editor)
		stage.setScene(new Scene(folder));
		stage.setWidth(1600);
		stage.setHeight(1200);
		stage.show();

		// Load editor view (with scene available)
		editor.loadView((view) -> editorTab.setContent(view));
		this.domain.activate();

		// Obtain the configurable context (now available from editor)
		ConfigurableContext<R, O> configurableContext = editor.getConfigurableContext();

		// Load the preferences
		PreferencesEditor<R> preferences = new PreferencesEditor<>(this.createEditor(envBridge), envBridge);
		Tab preferencesTab = new Tab("Preferences");
		folder.getTabs().add(preferencesTab);
		Pane preferencesPane = preferences.loadView((view) -> preferencesTab.setContent(view));
		BorderPane preferenceButtons = new BorderPane();
		preferenceButtons.setPadding(new Insets(10));
		preferencesPane.getChildren().add(preferenceButtons);

		// Reset button for preferences
		Button resetPreferences = new Button("Reset All");
		preferenceButtons.setLeft(resetPreferences);
		resetPreferences.setOnAction((event) -> preferences.resetToDefaults());

		// Apply button for preferences
		HBox preferenceRightButtons = new HBox();
		preferenceRightButtons.setSpacing(10);
		preferenceButtons.setRight(preferenceRightButtons);
		Button applyPreferences = new Button("Apply");
		applyPreferences.setOnAction((event) -> preferences.apply());
		Button cancelPreferences = new Button("Cancel");
		cancelPreferences.setOnAction((event) -> preferences.cancel());
		preferenceRightButtons.getChildren().addAll(applyPreferences, cancelPreferences);
		Runnable preferenceButtonEnabler = () -> {
			boolean isDisable = !preferences.dirtyProperty().getValue();
			applyPreferences.setDisable(isDisable);
			cancelPreferences.setDisable(isDisable);
		};
		preferences.dirtyProperty().addListener((event) -> preferenceButtonEnabler.run());
		preferenceButtonEnabler.run(); // initiate

		// Load the configuration items
		for (AbstractConfigurableItem<R, RE, O, ?, ?, ?> parent : editor.getParents()) {
			this.loadItem(parent, configurableContext, folder, envBridge);
		}
	}

	/**
	 * Loads the {@link AbstractConfigurableItem}.
	 * 
	 * @param <M>                 {@link Model} type for item.
	 * @param <I>                 Item type.
	 * @param item                Item.
	 * @param configurableContext {@link ConfigurableContext}.
	 * @param pane                {@link Pane} to configuration item.
	 * @param envBridge           {@link EnvironmentBridge}.
	 */
	private <M extends Model, I> void loadItem(AbstractConfigurableItem<R, RE, O, M, ?, I> item,
			ConfigurableContext<R, O> configurableContext, TabPane pane, EnvironmentBridge envBridge) {

		// Add the tab for item
		String itemName = item.getClass().getSimpleName();
		Tab tab = new Tab(itemName);
		pane.getTabs().add(tab);

		// Obtain the IDE configurer
		AbstractConfigurableItem<R, RE, O, M, ?, I>.IdeConfigurer ideConfigurer = item.configure();
		if (ideConfigurer == null) {
			tab.setContent(new Text("No configuration for " + this.getClass().getSimpleName()));
			return;
		}
		IdeConfiguration<O, M, I> configuration = AbstractConfigurableItem.extractIdeConfiguration(ideConfigurer);

		// Add tabs for each of the operations
		TabPane operationsPane = new TabPane();
		tab.setContent(operationsPane);

		// Create the prototype
		M prototype = item.prototype();

		// Possibly decorate the prototype
		@SuppressWarnings("unchecked")
		Consumer<M> prototypeDecorator = (Consumer<M>) this.prototypeDecorators.get(prototype.getClass());
		if (prototypeDecorator != null) {
			prototypeDecorator.accept(prototype);
		}

		// Specify the configurable context
		item.init(configurableContext);

		// Log changes
		Consumer<Change<?>> logChange = (change) -> {
			// Log running the change
			StringBuilder message = new StringBuilder();
			message.append("Executing change '" + change.getChangeDescription() + "' for target "
					+ change.getTarget().getClass().getName());
			if (!change.canApply()) {
				message.append(" (can not apply)");
				for (Conflict conflict : change.getConflicts()) {
					message.append(System.lineSeparator() + "\t" + conflict.getConflictDescription());
				}
			}
			System.out.println(message.toString());
		};

		// Load the add configuration
		if (configuration.add.length > 0) {
			Tab addTab = new Tab("Add");
			operationsPane.getTabs().add(addTab);
			Pane addParent = new Pane();
			addTab.setContent(addParent);
			I addItem = item.item(null);
			Configurer<I> addConfigurer = new Configurer<>(envBridge);
			ConfigurationBuilder<I> addBuilder = addConfigurer;
			ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return null;
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			for (ItemConfigurer<O, M, I> itemConfigurer : configuration.add) {
				itemConfigurer.configure(addBuilder, addContext);
			}
			addConfigurer.loadConfiguration(addItem, addParent);
		}

		// Load the add immediately
		if (configuration.addImmediately != null) {
			Tab addTab = new Tab("Add");
			operationsPane.getTabs().add(addTab);
			Pane addParent = new Pane();
			addTab.setContent(addParent);
			ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return null; // no modal on add
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			Button addButton = new Button("Add");
			addParent.getChildren().add(addButton);
			addButton.setOnAction((event) -> {
				try {
					configuration.addImmediately.action(addContext);
				} catch (Throwable ex) {
					System.out.println("Failed to add " + ex.getMessage());
				}
			});
		}

		// Load the refactor configuration
		if (configuration.refactor.length > 0) {
			Tab refactorTab = new Tab("Refactor");
			operationsPane.getTabs().add(refactorTab);
			Pane refactorParent = new Pane();
			refactorTab.setContent(refactorParent);
			I refactorItem = item.item(prototype);
			Configurer<I> refactorConfigurer = new Configurer<>(envBridge);
			ConfigurationBuilder<I> refactorBuilder = refactorConfigurer;
			ConfigurableModelContext<O, M> refactorContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return prototype;
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			for (ItemConfigurer<O, M, I> itemConfigurer : configuration.refactor) {
				itemConfigurer.configure(refactorBuilder, refactorContext);
			}
			refactorConfigurer.loadConfiguration(refactorItem, refactorParent);
		}

		// Load the delete configuration
		if (configuration.delete != null) {
			Tab deleteTab = new Tab("Delete");
			operationsPane.getTabs().add(deleteTab);
			Pane deleteParent = new Pane();
			deleteTab.setContent(deleteParent);
			ConfigurableModelContext<O, M> deleteContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return configurableContext.getOperations();
				}

				@Override
				public M getModel() {
					return prototype;
				}

				@Override
				public void execute(Change<M> change) {
					logChange.accept(change);
					configurableContext.getChangeExecutor().execute(change);
				}
			};
			Button deleteButton = new Button("Delete");
			deleteParent.getChildren().add(deleteButton);
			deleteButton.setOnAction((event) -> {
				try {
					configuration.delete.action(deleteContext);
				} catch (Throwable ex) {
					System.out.println("Failed to delete " + ex.getMessage());
				}
			});
		}
	}

}