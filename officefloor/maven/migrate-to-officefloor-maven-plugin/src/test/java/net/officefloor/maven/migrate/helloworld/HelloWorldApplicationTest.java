package net.officefloor.maven.migrate.helloworld;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Tests the {@link HelloWorldApplication}.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootTest
@AutoConfigureMockMvc
public class HelloWorldApplicationTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void testHelloWorld() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/hello")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.equalTo("HelloWorld")));
	}

}