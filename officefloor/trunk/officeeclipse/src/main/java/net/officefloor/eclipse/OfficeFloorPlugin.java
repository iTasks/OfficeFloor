package net.officefloor.eclipse;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorSkin;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorSkin;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OfficeFloorPlugin extends AbstractUIPlugin {

	/**
	 * Plug-in Id for the {@link OfficeFloorPlugin}.
	 */
	public static final String PLUGIN_ID = "net.officefloor.eclipse";

	/**
	 * Shared instance.
	 */
	private static OfficeFloorPlugin plugin;

	/**
	 * {@link OfficeFloorSkin}.
	 */
	private static OfficeFloorSkin skin;

	/**
	 * The constructor.
	 */
	public OfficeFloorPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// TODO obtain skin via extension

		// No skin specified by extension so use standard
		skin = new StandardOfficeFloorSkin();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static OfficeFloorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
				"officefloor_eclipse", path);
	}

	/**
	 * Obtains the {@link OfficeFloorSkin}.
	 * 
	 * @return {@link OfficeFloorSkin}.
	 */
	public static OfficeFloorSkin getSkin() {
		return skin;
	}

	/**
	 * Creates a new {@link OfficeFloorCompiler} for the input
	 * {@link AbstractOfficeFloorEditPart}.
	 * 
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 * @return {@link OfficeFloorCompiler}.
	 */
	public OfficeFloorCompiler createCompiler(
			final AbstractOfficeFloorEditor<?, ?> editor) {

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();

		// Obtain the class loader for the project
		ClassLoader classLoader = ProjectClassLoader.create(editor);
		compiler.setClassLoader(classLoader);

		// Provide error reporting
		compiler.setCompilerIssues(new CompilerIssues() {

			@Override
			public void addIssue(LocationType locationType, String location,
					AssetType assetType, String assetName,
					String issueDescription) {
				editor.messageError(issueDescription);
			}

			@Override
			public void addIssue(LocationType locationType, String location,
					AssetType assetType, String assetName,
					String issueDescription, Throwable cause) {
				editor.messageError(issueDescription + "\n\n"
						+ cause.getClass().getSimpleName() + ": "
						+ cause.getMessage());
			}
		});

		// Return the compiler
		return compiler;
	}

}