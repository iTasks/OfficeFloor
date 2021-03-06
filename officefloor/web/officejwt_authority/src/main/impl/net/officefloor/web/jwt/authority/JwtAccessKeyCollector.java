/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * Collects {@link JwtAccessKey} instances for JWT validation.
 * <p>
 * It is expected that the {@link JwtAccessKey} instances (and their
 * corresponding {@link JwtValidateKey} instances) are rotated. This minimises
 * the impact of "leaked" keys (for whatever reason) from creating security
 * problems.
 * <p>
 * Furthermore, in a clustered environment, co-ordinating the creation of
 * {@link JwtAccessKey} instances can become complicated. It is, therefore,
 * possible to have multiple {@link JwtAccessKey} instances in play, with the
 * example following algorithm:
 * <ol>
 * <li>A collect of keys is triggered for a particular instance in the
 * cluster</li>
 * <li>The instance retrieves all {@link JwtAccessKey} instances from a central
 * store, and identifies a new {@link JwtAccessKey} is required.</li>
 * <li>The instance creates the {@link JwtAccessKey} and stores it in the
 * central store.</li>
 * <ul>
 * <li>Note: the active window for the {@link JwtAccessKey} should be in the
 * future. It should only be active after a time that all instances in the
 * cluster will have collected the new {@link JwtAccessKey} (and corresponding
 * {@link JwtValidateKey} instances).</li>
 * </ul>
 * </li>
 * <li>The instance then includes the {@link JwtAccessKey} in its encoding</li>
 * <li>Other instances in the cluster trigger a collect, and pull in the created
 * {@link JwtAccessKey} from the central store.</li>
 * <li>Should two instances in the cluster create a {@link JwtAccessKey}
 * simultaneously, then both {@link JwtAccessKey} instances can be arbitrarily
 * used. This is ok as all instances should load both corresponding
 * {@link JwtValidateKey} instances.
 * <ul>
 * <li>Note: this does come with the cost of extra computation on the consumers
 * to validate the JWT instances. However, this algorithm also works if the
 * cluster is co-ordinated to only create the one {@link JwtAccessKey} per time
 * period (reducing this computation).</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAccessKeyCollector {

	/**
	 * Specifies the {@link JwtAccessKey} instances.
	 * 
	 * @param keys {@link JwtAccessKey} instances.
	 */
	void setKeys(JwtAccessKey[] keys);

}
