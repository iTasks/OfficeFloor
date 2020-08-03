package net.officefloor.maven.migrate.helloworld;

import lombok.Value;

/**
 * @author Daniel Sagenschneider
 */
@Value
public class HelloWorld {
	private long id;
	private String content;
}