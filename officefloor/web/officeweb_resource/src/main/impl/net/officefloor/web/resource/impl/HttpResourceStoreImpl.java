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
package net.officefloor.web.resource.impl;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerContext;
import net.officefloor.web.route.WebRouter;

/**
 * {@link HttpResourceStore} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceStoreImpl implements HttpResourceStore, ResourceSystemContext, Closeable {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpResourceStoreImpl.class.getName());

	/**
	 * All working files, so delete on close and only allow read access to
	 * contents.
	 */
	private static final OpenOption[] OPEN_OPTIONS = new OpenOption[] { StandardOpenOption.READ,
			StandardOpenOption.DELETE_ON_CLOSE };

	/**
	 * Default <code>Content-Type</code> should it not be determined for the
	 * resource.
	 */
	private static final HttpHeaderValue DEFAULT_CONTENT_TYPE = new HttpHeaderValue("application/octet");

	/**
	 * {@link Map} of {@link HttpResource} path to the current
	 * {@link CreateSingleton} for the {@link HttpResource}.
	 */
	private final Map<String, CreateSingleton> resolvingSingletons = new HashMap<>();

	/**
	 * {@link Map} of {@link HttpResource} path to the {@link HttpResource}.
	 */
	private final Map<String, AbstractHttpResource> cache = new ConcurrentHashMap<>();

	/**
	 * {@link HttpResourceCache}.
	 */
	private final HttpResourceCacheImpl httpResourceCache = new HttpResourceCacheImpl();

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * {@link ResourceSystem}.
	 */
	private final ResourceSystem resourceSystem;

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * {@link ResourceTransformer} instances.
	 */
	private final ResourceTransformer[] transformers;

	/**
	 * Cache of the <code>Content-Type</code> {@link HttpHeaderValue} instances
	 * to reduce GC.
	 */
	private final Map<String, HttpHeaderValue> contentTypes = new ConcurrentHashMap<>();

	/**
	 * {@link Charset} for {@link ResourceSystem}.
	 */
	private volatile Charset charset = Charset.defaultCharset();

	/**
	 * {@link FileCache}.
	 */
	private final FileCache fileCache;

	/**
	 * Directory default resource names.
	 */
	private final String[] directoryDefaultResourceNames;

	/**
	 * Instantiate.
	 * 
	 * @param location
	 *            Location for the {@link ResourceSystemContext}.
	 * @param resourceSystemFactory
	 *            {@link ResourceSystemFactory}.
	 * @param contextPath
	 *            Context path for {@link HttpResource} instances from this
	 *            {@link HttpResourceStore}.
	 * @param fileCacheFactory
	 *            {@link FileCacheFactory}.
	 * @param transformers
	 *            {@link ResourceTransformer} instances.
	 * @param directoryDefaultResourceNames
	 *            Directory default resource names.
	 * @throws IOException
	 *             If fails to instantiate the {@link HttpResourceStore}.
	 */
	public HttpResourceStoreImpl(String location, ResourceSystemFactory resourceSystemFactory, String contextPath,
			FileCacheFactory fileCacheFactory, ResourceTransformer[] transformers,
			String[] directoryDefaultResourceNames) throws IOException {
		this.location = location;

		// Create the resource system
		this.resourceSystem = resourceSystemFactory.createResourceSystem(this);

		// Obtain the context path
		if (contextPath == null) {
			contextPath = "/";
		}
		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		while (contextPath.endsWith("/")) {
			contextPath = contextPath.substring(0, contextPath.length() - "/".length());
		}
		this.contextPath = contextPath;

		// Specify the transformers
		this.transformers = transformers != null ? transformers : new ResourceTransformer[0];

		// Create the file cache
		String cacheName = location.replace('/', '_');
		this.fileCache = fileCacheFactory.createFileCache(cacheName);

		// Create the directory default resource names
		this.directoryDefaultResourceNames = new String[directoryDefaultResourceNames.length];
		for (int i = 0; i < this.directoryDefaultResourceNames.length; i++) {
			String defaultName = directoryDefaultResourceNames[i];
			if (!defaultName.startsWith("/")) {
				defaultName = "/" + defaultName;
			}
			this.directoryDefaultResourceNames[i] = WebRouter.transformToCanonicalPath(defaultName);
		}
	}

	/**
	 * Obtains the {@link HttpResourceCache}.
	 * 
	 * @return {@link HttpResourceCache}.
	 */
	public HttpResourceCache getCache() {
		return this.httpResourceCache;
	}

	/**
	 * Obtains the default {@link HttpFile} for the {@link HttpDirectory}.
	 * 
	 * @param directory
	 *            {@link HttpDirectory}.
	 * @return {@link HttpFile} for the {@link HttpDirectory} or
	 *         <code>null</code> if no default {@link HttpFile}.
	 * @throws IOException
	 *             If failure in obtaining default {@link HttpFile}.
	 */
	HttpFile getDefaultHttpFile(HttpDirectory directory) throws IOException {

		// Iterate over default file names to find file
		for (int i = 0; i < this.directoryDefaultResourceNames.length; i++) {
			String filePath = directory.getPath() + this.directoryDefaultResourceNames[i];
			HttpResource resource = this.getHttpResource(filePath);
			if ((resource != null) && (resource instanceof HttpFile)) {

				// Found the default HTTP file
				return (HttpFile) resource;
			}
		}

		// As here, no default HTTP file
		return null;
	}

	/*
	 * ==================== HttpResourceStore ======================
	 */

	@Override
	public HttpResource getHttpResource(String path) throws IOException {

		// Ensure path is canonical
		path = WebRouter.transformToCanonicalPath(path);

		// Determine if matching context path
		if (!path.startsWith(this.contextPath)) {
			// Not match context path (so not found)
			return new NotExistHttpResource(path);
		}

		// Determine if have cached
		HttpResource cachedResource = this.httpResourceCache.getSafeHttpResource(path);
		if (cachedResource != null) {
			return cachedResource;
		}

		// Obtain the resource path (minus context path)
		String resourcePath = path.substring(this.contextPath.length());

		// Obtain the create singleton
		CreateSingleton singleton;
		synchronized (this.resolvingSingletons) {

			// Determine if cached
			cachedResource = this.httpResourceCache.getSafeHttpResource(path);
			if (cachedResource != null) {
				return cachedResource;
			}

			// Not cached, so resolve singleton
			singleton = this.resolvingSingletons.get(path);
			if (singleton == null) {
				singleton = new CreateSingleton(path, resourcePath);
				this.resolvingSingletons.put(path, singleton);
			}
		}

		// Obtain the singleton instance
		AbstractHttpResource resource = singleton.createHttpResource();

		// Remove this singleton from currently resolving
		synchronized (this.resolvingSingletons) {

			// Cache the HTTP resource
			if (resource.isExist()) {
				this.cache.put(path, resource);
			}

			// As cached, now remove from resolving
			this.resolvingSingletons.remove(path);
		}

		// Return the resource
		return resource;
	}

	/*
	 * ================== ResourceSystemContext ====================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public Path createFile(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path createDirectory(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharset(Charset charset) {
		this.charset = charset != null ? charset : Charset.defaultCharset();
	}

	@Override
	public void notifyResourceChanged(String resourcePath) {

		// Determine if have path
		String httpPath;
		if (resourcePath == null) {
			httpPath = null;

		} else {
			// Ensure path is canonical
			resourcePath = WebRouter.transformToCanonicalPath(resourcePath);

			// Add the context path
			if (resourcePath.startsWith("/")) {
				httpPath = this.contextPath + resourcePath;
			} else {
				httpPath = this.contextPath + "/" + resourcePath;
			}
		}

		// Clear HTTP resources
		List<AbstractHttpResource> cleanupResources = new ArrayList<>(httpPath == null ? this.cache.size() : 1);
		synchronized (this.resolvingSingletons) {
			if (httpPath != null) {

				// Remove the specific entry from cache
				AbstractHttpResource resource = this.cache.remove(httpPath);
				if (resource != null) {
					cleanupResources.add(resource);
				}

			} else {

				// Remove all entries from cache
				this.cache.forEach((path, resource) -> {
					cleanupResources.add(resource);
					this.cache.remove(path);
				});
			}
		}
		for (AbstractHttpResource cleanupResource : cleanupResources) {
			try {
				cleanupResource.close();
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING,
						"Failed to clean up " + HttpResource.class.getSimpleName() + " " + cleanupResource.getPath(),
						ex);
			}
		}
	}

	/*
	 * ==================== Closeable ======================
	 */

	@Override
	public void close() throws IOException {

		// Close the file cache
		this.fileCache.close();
	}

	/**
	 * {@link HttpResourceCache} implementation.
	 */
	private class HttpResourceCacheImpl implements HttpResourceCache {

		/**
		 * Obtains the {@link HttpResource} assuming input is canonical path.
		 * 
		 * @param canonicalPath
		 *            Canonical path to the {@link HttpResource}.
		 * @return {@link HttpResource} or <code>null</code> if no resource at
		 *         path.
		 */
		private HttpResource getSafeHttpResource(String canonicalPath) {
			return HttpResourceStoreImpl.this.cache.get(canonicalPath);
		}

		/*
		 * ================ HttpResourceCache ====================
		 */

		@Override
		public HttpResource getHttpResource(String path) throws IOException {

			// Obtain the canonical path
			String canonicalPath = WebRouter.transformToCanonicalPath(path);

			// Obtain and return the resource
			return this.getSafeHttpResource(canonicalPath);
		}
	}

	/**
	 * <p>
	 * Promise to create the {@link HttpResource}.
	 * <p>
	 * This avoids the {@link HttpResource} being created multiple times.
	 */
	private class CreateSingleton {

		/**
		 * Path for the {@link HttpResource}.
		 */
		private final String httpPath;

		/**
		 * Path for the resource within the {@link ResourceSystem}.
		 */
		private final String resourcePath;

		/**
		 * Singleton {@link AbstractHttpResource}.
		 */
		private AbstractHttpResource singletonHttpResource = null;

		/**
		 * Potential creation {@link IOException}.
		 */
		private IOException creationFailure = null;

		/**
		 * Instantiate.
		 * 
		 * @param httpPath
		 *            Path for the {@link HttpResource}.
		 * @param resourcePath
		 *            Path for the resource within the {@link ResourceSystem}.
		 */
		private CreateSingleton(String httpPath, String resourcePath) {
			this.httpPath = httpPath;
			this.resourcePath = resourcePath;
		}

		/**
		 * Creates the {@link AbstractHttpResource}.
		 * 
		 * @return {@link AbstractHttpResource}.
		 * @throws IOException
		 *             If fails to create the {@link AbstractHttpResource}.
		 */
		@SuppressWarnings("resource")
		private synchronized AbstractHttpResource createHttpResource() throws IOException {

			// Determine if have resolved resource (by another thread)
			if (this.singletonHttpResource != null) {
				return this.singletonHttpResource;
			}

			// Determine if failure in resolving
			if (this.creationFailure != null) {
				throw this.creationFailure;
			}

			// Easy access to HTTP resource store
			HttpResourceStoreImpl store = HttpResourceStoreImpl.this;

			// Not yet resolved, so resolve in critical section
			try {

				// Obtain the path to the resource
				final Path resource = store.resourceSystem.getResource(this.resourcePath);

				// Determine if have resource
				if ((resource == null) || (Files.notExists(resource))) {
					return new NotExistHttpResource(this.httpPath);
				}

				// Determine if directory
				if (Files.isDirectory(resource)) {
					this.singletonHttpResource = new HttpDirectoryImpl(this.httpPath, store);
					return this.singletonHttpResource;
				}

				// Determine the content type of resource
				String contentType = Files.probeContentType(resource);
				HttpHeaderValue contentTypeHeaderValue;
				if (contentType == null) {
					// Could not determine, so use default
					contentTypeHeaderValue = DEFAULT_CONTENT_TYPE;
				} else {
					// Obtain the content type
					contentTypeHeaderValue = store.contentTypes.get(contentType);
					if (contentTypeHeaderValue == null) {
						// Not cached, so create and cache
						contentTypeHeaderValue = new HttpHeaderValue(contentType);
						store.contentTypes.put(contentType, contentTypeHeaderValue);
					}
				}

				// Undertake transformation of resource
				ResourceTransformerContextImpl context = new ResourceTransformerContextImpl(resource,
						contentTypeHeaderValue);
				for (int i = 0; i < store.transformers.length; i++) {
					ResourceTransformer transformer = store.transformers[i];
					transformer.transform(context);
				}

				// Determine if resulted in the original resource
				if (Files.isSameFile(resource, context.resource)) {

					/*
					 * Must make copy of file. This is so the file channel is
					 * always backed by file. Specifically, if file is deleted
					 * from the resource system outside the control of the HTTP
					 * resource store, then this needs to be managed rather than
					 * breaking file channel.
					 */
					Path copy = context.createFile();
					Files.copy(context.resource, copy, StandardCopyOption.REPLACE_EXISTING);
					context.resource = copy;
				}

				// Create the HTTP file
				FileChannel fileChannel = FileChannel.open(context.resource, OPEN_OPTIONS);
				HttpFileImpl httpFile = new HttpFileImpl(this.httpPath, fileChannel, context.contentEncoding,
						context.contentType, context.getCharset());

				// Flag HTTP resource as resolved
				this.singletonHttpResource = httpFile;

				// Return the resolve HTTP resource
				return this.singletonHttpResource;

			} catch (IOException ex) {
				// Capture failure in resolving singleton
				this.creationFailure = ex;

				// Propagate the failure
				throw this.creationFailure;
			}
		}
	}

	/**
	 * {@link ResourceTransformerContext} implementation.
	 */
	private class ResourceTransformerContextImpl implements ResourceTransformerContext {

		/**
		 * {@link Path} to the resource.
		 */
		private Path resource;

		/**
		 * Path of the {@link HttpResource} to use as name for cached files.
		 * This aids identifying the file for debugging.
		 */
		private String path;

		/**
		 * Index of the next file. This aids identifying the file for debugging.
		 */
		private int fileIndex = 0;

		/**
		 * <code>Content-Type</code> {@link HttpHeaderValue}.
		 */
		private HttpHeaderValue contentType;

		/**
		 * {@link Charset}.
		 */
		private Charset charset = null;

		/**
		 * <code>Content-Encoding</code> {@link HttpHeaderValue}.
		 */
		private HttpHeaderValue contentEncoding = null;

		/**
		 * {@link Path} instances to the files that require clean up.
		 */
		private final List<Path> cleanupFiles = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param resource
		 *            {@link Path} to the {@link ResourceSystem} resource.
		 * @param contentType
		 *            <code>Content-Type</code> {@link HttpHeaderValue}.
		 */
		private ResourceTransformerContextImpl(Path resource, HttpHeaderValue contentType) {
			this.resource = resource;
			this.contentType = contentType;
		}

		/*
		 * =============== ResourceTransformerContext ===================
		 */

		@Override
		public Path getResource() {
			return this.resource;
		}

		@Override
		public Path createFile() throws IOException {

			// Create the name for the file
			String name = this.path + this.fileIndex;
			this.fileIndex++;
			name = name.replace('/', '_');

			// Create and register the new file
			Path newFile = HttpResourceStoreImpl.this.fileCache.createFile(name);
			this.cleanupFiles.add(newFile);

			// Return the new file
			return newFile;
		}

		@Override
		public String getContentType() {
			return this.contentType.getValue();
		}

		@Override
		public Charset getCharset() {
			return this.charset != null ? this.charset : HttpResourceStoreImpl.this.charset;
		}

		@Override
		public void setContentType(HttpHeaderValue contentType, Charset charset) {
			if (contentType != null) {
				this.contentType = contentType;
			}
			this.charset = charset;
		}

		@Override
		public String getContentEncoding() {
			return this.contentEncoding != null ? this.contentEncoding.getValue() : null;
		}

		@Override
		public void setContentEncoding(HttpHeaderValue contentEncoding) throws IOException {
			if (contentEncoding != null) {
				this.contentEncoding = contentEncoding;
			}
		}

		@Override
		public void setTransformedResource(Path resource) {
			if (resource != null) {
				this.resource = resource;
			}
		}
	}

}