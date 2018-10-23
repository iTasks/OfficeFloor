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
package net.officefloor.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can integrate Spring via boot.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringBootTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext context;

	@Override
	protected void setUp() throws Exception {
		this.context = SpringApplication.run(MockSpringBootConfiguration.class);

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			System.out.println("  " + name);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		this.context.close();
	}

	/**
	 * Ensure can configure Spring bean.
	 */
	public void testSpringConfiguredBeans() {

		// Ensure can obtain simple bean
		SimpleBean simple = this.context.getBean(SimpleBean.class);
		assertNotNull("Should obtain simple bean", simple);
		assertEquals("Incorrect simple bean", "SIMPLE", simple.getValue());

		// Ensure can obtain complex bean
		ComplexBean complex = this.context.getBean(ComplexBean.class);
		assertNotNull("Should obtain complex bean", complex);
		assertSame("Should have simple bean injected", simple, complex.getSimpleBean());
	}

}