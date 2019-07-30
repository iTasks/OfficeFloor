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
package net.officefloor.eclipse.woof;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel.WoofHttpContinuationEvent;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofResourceModel.WoofResourceEvent;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionModel.WoofSectionEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel.WoofSectionOutputEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;

/**
 * Configuration for the {@link WoofSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofSectionModel, WoofSectionEvent, WoofSectionOutputModel, WoofSectionOutputEvent> {

	@Override
	public WoofSectionOutputModel prototype() {
		return new WoofSectionOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofSectionEvent.ADD_OUTPUT,
				WoofSectionEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofSectionModel parentModel, WoofSectionOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofSectionOutputModel model, AdaptedChildVisualFactoryContext<WoofSectionOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofSectionOutputToWoofSectionInputModel.class,
						WoofSectionOutputToWoofTemplateModel.class, WoofSectionOutputToWoofResourceModel.class,
						WoofSectionOutputToWoofSecurityModel.class, WoofSectionOutputToWoofHttpContinuationModel.class)
						.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSectionOutputName(),
				WoofSectionOutputEvent.CHANGE_WOOF_SECTION_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_SECTION_OUTPUT, WoofSectionInputEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeSectionOuputToSectionInput(ctx.getModel()));
				}));

		// Template
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_SECTION_OUTPUT, WoofTemplateEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOuputToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_SECTION_OUTPUT, WoofResourceEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOuputToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_SECTION_OUTPUT, WoofSecurityEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOuputToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections
				.add(new IdeConnection<>(WoofSectionOutputToWoofHttpContinuationModel.class)
						.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofSectionOutput(),
								WoofSectionOutputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
						.to(WoofHttpContinuationModel.class)
						.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofHttpContinuation(),
								WoofHttpContinuationEvent.ADD_WOOF_SECTION_OUTPUT,
								WoofHttpContinuationEvent.REMOVE_WOOF_SECTION_OUTPUT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().linkSectionOutputToHttpContinuation(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().removeSectionOuputToHttpContinuation(ctx.getModel()));
						}));
	}

}