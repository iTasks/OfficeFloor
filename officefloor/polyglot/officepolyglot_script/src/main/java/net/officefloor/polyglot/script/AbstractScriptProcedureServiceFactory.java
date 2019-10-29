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
package net.officefloor.polyglot.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.AsynchronousFlowParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.InParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ObjectParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.OutParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ValueParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.VariableParameterFactory;
import net.officefloor.plugin.section.clazz.FlowAnnotation;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;
import net.officefloor.web.HttpCookieParameterAnnotation;
import net.officefloor.web.HttpHeaderParameterAnnotation;
import net.officefloor.web.HttpObjectAnnotation;
import net.officefloor.web.HttpParametersAnnotation;
import net.officefloor.web.HttpPathParameterAnnotation;
import net.officefloor.web.HttpQueryParameterAnnotation;

/**
 * {@link ProcedureServiceFactory} providing abstract support for Scripts.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractScriptProcedureServiceFactory implements ProcedureServiceFactory {

	/**
	 * {@link ScriptEngineManager}.
	 */
	private static final ScriptEngineManager engineManager = new ScriptEngineManager();

	/**
	 * Obtains the service name for this {@link ProcedureService}.
	 * 
	 * @return Service name for this {@link ProcedureService}.
	 */
	protected abstract String getServiceName();

	/**
	 * Obtains the name of the {@link ScriptEngine}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Name of the {@link ScriptEngine}.
	 * @throws Exception If fails to obtain the name of the {@link ScriptEngine}.
	 */
	protected abstract String getScriptEngineName(SourceContext context) throws Exception;

	/**
	 * Obtains the path to the setup script.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Path to the setup script or <code>null</code> if no setup.
	 * @throws Exception If fails to obtain the setup script path.
	 */
	protected String getSetupScriptPath(SourceContext context) throws Exception {
		return null;
	}

	/**
	 * Obtains the path to the script that extracts the function meta-data.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Path to the script that extracts the function meta-data.
	 * @throws Exception If fails to obtain the path to the function meta-data
	 *                   script.
	 */
	protected abstract String getMetaDataScriptPath(SourceContext context) throws Exception;

	/**
	 * Enables overriding to decorate the {@link ScriptEngine}.
	 * 
	 * @param engine  {@link ScriptEngine}.
	 * @param context {@link SourceContext}.
	 * @throws Exception If fails to decorate the {@link ScriptEngine}.
	 */
	protected void decorateScriptEngine(ScriptEngine engine, SourceContext context) throws Exception {
		// No decoration by default
	}

	/**
	 * Obtains the {@link ScriptExceptionTranslator}.
	 * 
	 * @return {@link ScriptExceptionTranslator}.
	 */
	protected ScriptExceptionTranslator getScriptExceptionTranslator() {
		return null; // will be defaulted
	}

	/**
	 * Lists the {@link Procedure} instances.
	 * 
	 * @param context {@link ProcedureListContext}.
	 * @throws Exception If fails to list the {@link Procedure} instances.
	 */
	protected void listProcedures(ProcedureListContext context) throws Exception {
		// TODO implement ProcedureService.listProcedures
		throw new UnsupportedOperationException("TODO implement ProcedureService.listProcedures");
	}

	/**
	 * Loads the {@link ManagedFunction} for the {@link Procedure}.
	 * 
	 * @param context {@link ProcedureManagedFunctionContext}.
	 * @throws Exception If fails to load the {@link ManagedFunction}.
	 */
	protected void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {

		// Obtain the script engine
		SourceContext sourceContext = context.getSourceContext();
		String engineName = this.getScriptEngineName(sourceContext);
		ScriptEngine engine = engineManager.getEngineByName(engineName);
		this.decorateScriptEngine(engine, sourceContext);

		// Ensure invocable
		if (!(engine instanceof Invocable)) {
			throw new IllegalStateException(
					"Script engine " + engineName + " must be " + Invocable.class.getSimpleName());
		}
		Invocable invocable = (Invocable) engine;

		// Load the setup script (if provided)
		String setupScriptPath = this.getSetupScriptPath(sourceContext);
		String setupScript = null;
		if (setupScriptPath != null) {
			setupScript = readContent(sourceContext.getResource(setupScriptPath));
			engine.eval(setupScript);
		}

		// Load the Script contents
		String scriptPath = context.getResource();
		String script = readContent(sourceContext.getResource(scriptPath));
		engine.eval(script);

		// Load the meta-data for the function
		String procedureName = context.getProcedureName();
		String metaDataScriptPath = this.getMetaDataScriptPath(sourceContext);
		String metaDataScript = readContent(sourceContext.getResource(metaDataScriptPath));
		metaDataScript = metaDataScript.replace("_FUNCTION_NAME_", procedureName);
		engine.eval(metaDataScript);
		Object metaData = invocable.invokeFunction("OFFICEFLOOR_METADATA_" + procedureName);

		// Parse out the meta-data
		if (metaData == null) {
			throw new Exception("No meta-data provided for " + procedureName);
		} else if (!(metaData instanceof String)) {
			throw new Exception("Meta-data provided for " + procedureName + " must be JSON string ("
					+ metaData.getClass().getName() + ")");
		}
		String metaDataJsonString = (String) metaData;

		// Obtain the script meta-data
		ScriptFunctionMetaData functionMetaData = new ObjectMapper().readValue(metaDataJsonString,
				ScriptFunctionMetaData.class);

		// Ensure no error
		String error = functionMetaData.getError();
		if (!CompileUtil.isBlank(error)) {
			throw new Exception(error);
		}

		// Translates the class
		Function<String, String> translateClass = (className) -> {
			switch (className) {
			case "boolean":
				return Boolean.class.getName();
			case "byte":
				return Byte.class.getName();
			case "short":
				return Short.class.getName();
			case "char":
				return Character.class.getName();
			case "int":
				return Integer.class.getName();
			case "long":
				return Long.class.getName();
			case "float":
				return Float.class.getName();
			case "double":
				return Double.class.getName();
			default:
				return className;
			}
		};

		// Loads the class
		Function<String, Class<?>> loadClass = (className) -> sourceContext.loadClass(translateClass.apply(className));

		// Obtain the parameters (ensuring have list)
		List<ScriptParameterMetaData> parameterMetaDatas = functionMetaData.getParameters();
		if (parameterMetaDatas == null) {
			parameterMetaDatas = Collections.emptyList();
		}

		// Obtain the exception translator (defaulting to no translation)
		ScriptExceptionTranslator scriptExceptionTranslator = this.getScriptExceptionTranslator();
		if (scriptExceptionTranslator == null) {
			scriptExceptionTranslator = (ex) -> ex;
		}

		// Load the function
		MethodParameterFactory[] parameterFactories = new MethodParameterFactory[parameterMetaDatas.size()];
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = context
				.setManagedFunction(new ScriptManagedFunction(engineManager, engineName, setupScript, script,
						procedureName, parameterFactories, scriptExceptionTranslator), Indexed.class, Indexed.class);

		// Capture the flows
		List<FlowAnnotation> flowAnnotations = new LinkedList<>();

		// Load the parameters
		int objectIndex = 0;
		int flowIndex = 0;
		for (int i = 0; i < parameterMetaDatas.size(); i++) {
			ScriptParameterMetaData parameterMetaData = parameterMetaDatas.get(i);
			// Obtain the object details
			String parameterName = parameterMetaData.getName();
			String qualifier = parameterMetaData.getQualifier();
			String typeName = parameterMetaData.getType();

			// Obtain type (reflection will not provide primitives)
			Class<?> type;
			final String arraySuffix = "[]";
			if (CompileUtil.isBlank(typeName)) {
				// No type provided
				type = null;

			} else if (typeName.endsWith(arraySuffix)) {
				// Load array
				String componentTypeName = typeName.substring(0, typeName.length() - arraySuffix.length());
				Class<?> componentType = sourceContext.loadClass(componentTypeName);
				type = Array.newInstance(componentType, 0).getClass();

			} else {
				// No array, so load class
				type = loadClass.apply(typeName);
			}

			// Ease checking for type and name
			int parameterIndex = i;
			Runnable ensureHaveType = () -> {
				if (type == null) {
					throw new IllegalStateException("Must provide type for parameter " + parameterIndex
							+ " (with nature " + parameterMetaData.getNature() + ")");
				}
			};
			Runnable ensureHaveName = () -> {
				if (CompileUtil.isBlank(parameterName)) {
					throw new IllegalStateException("Must provide name for parameter " + parameterIndex
							+ " (with nature " + parameterMetaData.getNature() + ")");
				}
			};

			// Obtain the nature
			String nature = parameterMetaData.getNature();
			if (nature == null) {
				nature = "object";
			}
			ManagedFunctionObjectTypeBuilder<Indexed> parameter;
			boolean isVariable;
			switch (nature) {

			case "parameter":
				// Add the parameter
				ensureHaveType.run();
				function.addAnnotation(new ParameterAnnotation(type, i));
				// Carry on to load object for parameter
			case "object":
				// Add the object
				ensureHaveType.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(type);
				if (qualifier != null) {
					// Use qualified type for name
					parameter.setLabel(qualifier + "-" + type.getName());
					parameter.setTypeQualifier(qualifier);
				} else {
					// Use type for name
					parameter.setLabel(type.getName());
				}
				isVariable = false;
				break;

			case "val":
				ensureHaveType.run();
				parameterFactories[i] = new ValueParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "in":
				ensureHaveType.run();
				parameterFactories[i] = new InParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "out":
				ensureHaveType.run();
				parameterFactories[i] = new OutParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "var":
				ensureHaveType.run();
				parameterFactories[i] = new VariableParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "httpPathParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpPathParameterAnnotation httpPathParameter = new HttpPathParameterAnnotation(parameterName);
				parameter.addAnnotation(httpPathParameter);
				parameter.setTypeQualifier(httpPathParameter.getQualifier());
				isVariable = false;
				break;

			case "httpQueryParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpQueryParameterAnnotation httpQueryParameter = new HttpQueryParameterAnnotation(parameterName);
				parameter.addAnnotation(httpQueryParameter);
				parameter.setTypeQualifier(httpQueryParameter.getQualifier());
				isVariable = false;
				break;

			case "httpHeaderParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpHeaderParameterAnnotation httpHeaderParameter = new HttpHeaderParameterAnnotation(parameterName);
				parameter.addAnnotation(httpHeaderParameter);
				parameter.setTypeQualifier(httpHeaderParameter.getQualifier());
				isVariable = false;
				break;

			case "httpCookieParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpCookieParameterAnnotation httpCookieParameter = new HttpCookieParameterAnnotation(parameterName);
				parameter.addAnnotation(httpCookieParameter);
				parameter.setTypeQualifier(httpCookieParameter.getQualifier());
				isVariable = false;
				break;

			case "httpParameters":
				ensureHaveType.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(type);
				parameter.addAnnotation(new HttpParametersAnnotation());
				if (qualifier != null) {
					parameter.setTypeQualifier(qualifier);
				}
				isVariable = false;
				break;

			case "httpObject":
				ensureHaveType.run();
				parameterFactories[i] = new ObjectParameterFactory(objectIndex++);
				parameter = function.addObject(type);
				parameter.addAnnotation(new HttpObjectAnnotation());
				if (qualifier != null) {
					parameter.setTypeQualifier(qualifier);
				}
				isVariable = false;
				break;

			case "flow":
				ensureHaveName.run();
				ManagedFunctionFlowTypeBuilder<?> flow = function.addFlow();
				flow.setLabel(parameterName);
				if (type != null) {
					flow.setArgumentType(type);
				}
				flowAnnotations.add(new FlowAnnotation(parameterName, flowIndex, false, type, true));
				parameterFactories[i] = new ScriptFlowParameterFactory(flowIndex++);
				isVariable = false;
				break;

			case "asynchronousFlow":
				parameterFactories[i] = new AsynchronousFlowParameterFactory();
				isVariable = false;
				break;

			default:
				// Unknown nature
				throw new IllegalStateException("Unknown nature " + nature + " for parameter " + i + " ("
						+ (qualifier == null ? "" : "qualifier=" + qualifier + ", ") + "type=" + typeName + ")");
			}

			// Configure variable
			if (isVariable) {
				String validTypeName = VariableManagedObjectSource.type(typeName);
				String variableName = VariableManagedObjectSource.name(qualifier, validTypeName);
				parameter = function.addObject(Var.class);
				parameter.setTypeQualifier(variableName);
				parameter.addAnnotation(new VariableAnnotation(variableName, validTypeName));
			}
		}

		// Load possible flows
		if (flowAnnotations.size() > 0) {
			function.addAnnotation(flowAnnotations.toArray(new FlowAnnotation[flowAnnotations.size()]));
		}

		// Load the section annotations for the function
		String nextArgumentType = functionMetaData.getNextArgumentType();
		if (nextArgumentType != null) {
			Class<?> argumentType = (nextArgumentType == null) ? null : sourceContext.loadClass(nextArgumentType);
			function.setReturnType(argumentType);
		}
	}

	/**
	 * Reads in the content.
	 * 
	 * @param content Content.
	 * @return Content as string.
	 * @throws IOException If fails to read content.
	 */
	private static String readContent(InputStream content) throws IOException {
		StringWriter buffer = new StringWriter();
		try (Reader reader = new InputStreamReader(content)) {
			for (int character = reader.read(); character != -1; character = reader.read()) {
				buffer.write(character);
			}
		}
		return buffer.toString();
	}

	/*
	 * ==================== ProcedureServiceFactory ===================
	 */

	@Override
	public ProcedureService createService(ServiceContext context) throws Throwable {
		return new ScriptProcedureService();
	}

	/**
	 * {@link ProcedureService} providing abstract support for Scripts.
	 */
	private class ScriptProcedureService implements ManagedFunctionProcedureService {

		/*
		 * ==================== ManagedFunctionProcedureService ====================
		 */

		@Override
		public String getServiceName() {
			return AbstractScriptProcedureServiceFactory.this.getServiceName();
		}

		@Override
		public void listProcedures(ProcedureListContext context) throws Exception {
			AbstractScriptProcedureServiceFactory.this.listProcedures(context);
		}

		@Override
		public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
			AbstractScriptProcedureServiceFactory.this.loadManagedFunction(context);
		}
	}

}