/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.response.source;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseWriterWork;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.response.source.HttpFileWriterTaskFactory;
import net.officefloor.plugin.web.http.response.source.HttpResponseWriterWorkSource;
import net.officefloor.plugin.web.http.response.source.HttpFileWriterTaskFactory.HttpFileWriterTaskDependencies;

/**
 * Tests the {@link HttpResponseWriterWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil
				.validateSpecification(HttpResponseWriterWorkSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		WorkTypeBuilder<HttpResponseWriterWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new HttpResponseWriterWork(null));
		TaskTypeBuilder<HttpFileWriterTaskDependencies, None> file = work
				.addTaskType("WriteFileToResponse",
						new HttpFileWriterTaskFactory(),
						HttpFileWriterTaskDependencies.class, None.class);
		file.addObject(HttpFile.class).setKey(
				HttpFileWriterTaskDependencies.HTTP_FILE);
		file.addObject(ServerHttpConnection.class).setKey(
				HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION);
		file.addEscalation(IOException.class);
		WorkLoaderUtil.validateWorkType(work,
				HttpResponseWriterWorkSource.class);
	}

	/**
	 * Ensure can write a {@link HttpFile}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testWriteHttpFile() throws Throwable {

		TaskContext<HttpResponseWriterWork, HttpFileWriterTaskDependencies, None> taskContext = this
				.createMock(TaskContext.class);
		HttpFile httpFile = this.createMock(HttpFile.class);
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpResponse response = this.createMock(HttpResponse.class);
		ByteBuffer contents = ByteBuffer.allocate(0);
		OutputBufferStream body = this.createMock(OutputBufferStream.class);

		// Record
		this.recordReturn(
				taskContext,
				taskContext.getObject(HttpFileWriterTaskDependencies.HTTP_FILE),
				httpFile);
		this.recordReturn(
				taskContext,
				taskContext
						.getObject(HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(connection, connection.getHttpResponse(), response);
		this.recordReturn(httpFile, httpFile.getContentEncoding(), "");
		this.recordReturn(httpFile, httpFile.getContentType(), "");
		this.recordReturn(httpFile, httpFile.getCharset(), null);
		this.recordReturn(httpFile, httpFile.getContents(), contents);
		this.recordReturn(response, response.getBody(), body);
		body.append(contents);

		// Test
		this.replayMockObjects();

		// Create the task
		WorkType<HttpResponseWriterWork> work = WorkLoaderUtil
				.loadWorkType(HttpResponseWriterWorkSource.class);
		Task task = work.getTaskTypes()[0].getTaskFactory().createTask(
				work.getWorkFactory().createWork());

		// Execute the task
		assertNull(task.doTask(taskContext));

		this.verifyMockObjects();
	}

}