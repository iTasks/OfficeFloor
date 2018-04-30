/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor.internal.style;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Abstract {@link StyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractStyleRegistry implements StyleRegistry {

	/**
	 * Creates the {@link URLStreamHandler}.
	 * 
	 * @param protocol
	 *            Protocol.
	 * @return {@link URLStreamHandler}.
	 */
	public static URLConnection openConnection(URL url) throws IOException {
		OfficeFloorUrlConnection connection = new OfficeFloorUrlConnection(url);
		connection.connect();
		return connection;
	}

	/**
	 * {@link URLConnection} implementation to retrieve style sheet.
	 */
	private static class OfficeFloorUrlConnection extends URLConnection {

		/**
		 * Stylesheet content.
		 */
		private ReadOnlyProperty<String> stylesheetContent;

		/**
		 * Instantiate.
		 * 
		 * @param url
		 *            {@link URL}.
		 */
		protected OfficeFloorUrlConnection(URL url) {
			super(url);
			this.setUseCaches(false);
		}

		/*
		 * ============= URLConnection ======================
		 */

		@Override
		public void connect() throws IOException {
			String urlPath = this.getURL().getPath();
			this.stylesheetContent = urlPathToStyleContent.get(urlPath);
			if (this.stylesheetContent == null) {
				throw new IOException("URL " + url + " has no style sheet registered");
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {

			// Obtain the content
			byte[] data = new byte[0];
			String content = this.stylesheetContent.getValue();
			if (content != null) {
				data = content.getBytes();
			}

			// Return input stream to content
			return new ByteArrayInputStream(data);
		}
	}

	/**
	 * <p>
	 * Mapping of {@link URL} to style sheet content {@link Property}.
	 * <p>
	 * Allow {@link Property} instances to be GC'ed once no longer used by editor.
	 */
	private static Map<String, ReadOnlyProperty<String>> urlPathToStyleContent = new WeakHashMap<>();

	/**
	 * Next instance index.
	 */
	private static AtomicInteger nextInstanceIndex = new AtomicInteger(1);

	/**
	 * Index for current instance.
	 */
	private final int instanceIndex;

	/**
	 * Instantiate.
	 */
	public AbstractStyleRegistry() {
		this.instanceIndex = nextInstanceIndex.getAndIncrement();
	}

	/**
	 * Obtains the style sheet content.
	 * 
	 * @param url
	 *            {@link URL}.
	 * @return {@link ReadOnlyProperty} to the content of the style sheet for the
	 *         {@link URL}. May be <code>null</code> if editor no longer active.
	 */
	public ReadOnlyProperty<String> getStylesheetContent(String url) {
		return null;
	}

	/**
	 * Obtains the {@link URL} for the configuration path.
	 * 
	 * @param configurationPath
	 *            Configuration path.
	 * @param version
	 *            Version of the content for the {@link URL}.
	 * @return {@link URL} for the configuration path.
	 */
	protected URL getUrl(String configurationPath, int version) {
		String url = "officefloorstyle://" + String.valueOf(this.instanceIndex) + "/" + configurationPath + "?version="
				+ version;
		try {
			return new URL(url);
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Failed to create URL for " + url, ex);
		}
	}

	/**
	 * =============== StyleRegistry =====================
	 */

	@Override
	public ReadOnlyProperty<URL> registerStyle(String configurationPath, ReadOnlyProperty<String> stylesheetContent) {

		// Determine the URL
		int[] version = new int[] { 0 };
		URL url = this.getUrl(configurationPath, version[0]++);

		// Load the mapping
		ReadOnlyProperty<String> stylesheetContentProperty = urlPathToStyleContent.get(url.getPath());
		if (stylesheetContentProperty != null) {
			throw new IllegalStateException("Stylesheet already registered for URL " + url);
		}

		// Register the style property
		urlPathToStyleContent.put(url.getPath(), stylesheetContent);

		// Create property for URL
		Property<URL> urlProperty = new SimpleObjectProperty<>(url);

		// Trigger reload of URL on change to style content
		stylesheetContent.addListener((change) -> {
			urlProperty.setValue(this.getUrl(configurationPath, version[0]++));
		});

		// Return the URL property
		return urlProperty;
	}

}