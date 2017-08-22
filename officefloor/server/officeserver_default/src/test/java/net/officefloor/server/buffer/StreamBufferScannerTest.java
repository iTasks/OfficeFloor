/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.buffer;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.buffer.StreamBufferScanner.ScanTarget;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link StreamBufferScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferScannerTest extends OfficeFrameTestCase {

	/**
	 * {@link Supplier} of an {@link Error} that should not occur.
	 */
	private static final Supplier<Error> shouldNotOccur = () -> new Error("Should not occur");

	/**
	 * Ensure creates correct scan mask.
	 */
	public void testScanMask() {
		assertEquals(0xffffffffffffffffL, new ScanTarget((byte) 0xff).mask);
		assertEquals(0, new ScanTarget((byte) 0x00).mask);
		assertEquals(0x0101010101010101L, new ScanTarget((byte) 0x01).mask);
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFirstByte() {
		assertEquals(0, indexOfFirst(1));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSecondByte() {
		assertEquals(1, indexOfFirst(2));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testThirdByte() {
		assertEquals(2, indexOfFirst(3));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFourthByte() {
		assertEquals(3, indexOfFirst(4));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFifthByte() {
		assertEquals(4, indexOfFirst(5));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSixthByte() {
		assertEquals(5, indexOfFirst(6));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSeventhByte() {
		assertEquals(6, indexOfFirst(7));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testEighthByte() {
		assertEquals(7, indexOfFirst(8));
	}

	/**
	 * Ensure ignore top bit false positives.
	 */
	public void testAllFalsePositives() {
		assertEquals(8, scan(createBuffer(0, 0, 0, 0, 0, 0, 0, 0, 1), 1).length());
	}

	/**
	 * Ensure can handle less than long length.
	 */
	public void testLessThanLongLength() {
		assertEquals(6, scan(createBuffer(1, 2, 3, 4, 5, 6, 7), 7).length());
	}

	/**
	 * Run through each possible combination to ensure skips all false positives
	 * to find the byte.
	 */
	public void testExhaustiveFindByte() {

		// Exhaustively handle each byte value
		// (Don't need to check negatives, as don't search for negative HTTP
		// characters)
		for (int value = 0; value <= Byte.MAX_VALUE; value++) {

			// Create buffer with all other values before value (+1 for zero)
			int extraBytes = 16; // ensure uses long comparison
			byte[] data = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE + extraBytes + 1];
			int writeIndex = 0;
			for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
				if (b != value) {
					data[writeIndex++] = (byte) b;
				}
			}

			// Write the value last
			int valueIndex = data.length - extraBytes - 1;
			data[valueIndex] = (byte) value;

			// Create the buffer
			StreamBuffer<ByteBuffer> buffer = createBuffer(data);

			// Scan for the value
			int scanIndex = scan(buffer, value).length();

			// Ensure correct index
			assertEquals("Incorrect index for byte " + Integer.toHexString(value) + " (" + value + ")", valueIndex,
					scanIndex);
		}
	}

	/**
	 * Run through each possible combination to ensure skips all false positives
	 * to not find the byte.
	 */
	public void testExhaustiveNotFindByte() {

		// Exhaustively handle each byte value
		// (again don't need to check for negative values)
		for (int value = 0; value <= Byte.MAX_VALUE; value++) {

			// Create the buffer with all other values (+1 for zero)
			byte[] data = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE + 1];
			int writeIndex = 0;
			for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
				if (b != value) {
					data[writeIndex++] = (byte) b;
				}
			}

			// Ensure last byte not zero (to match 0)
			data[writeIndex] = -1;

			// Create the buffer
			StreamBuffer<ByteBuffer> buffer = createBuffer(data);

			// Scan for the value
			StreamBufferByteSequence sequence = scan(buffer, value);

			// Ensure not find the value
			assertNull("Should not find byte " + Integer.toHexString(value) + " (" + value + ")", sequence);
		}
	}

	/**
	 * Ensure can scan along the data.
	 */
	public void testMultipleScans() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(0, 1, 1, 2, 3, 4, 5, 6, 7, 8, 1, 0);

		// Create the target
		ScanTarget target = new ScanTarget((byte) 1);

		// Ensure find each one in data (note scans are exclusive to target)
		assertEquals(1, scanner.scanToTarget(target, 1000, shouldNotOccur).length());
		scanner.skipBytes(1); // skip past for next match
		assertEquals(0, scanner.scanToTarget(target, 1000, shouldNotOccur).length());
		scanner.skipBytes(1); // skip past for next match
		assertEquals(7, scanner.scanToTarget(target, 1000, shouldNotOccur).length());
		scanner.skipBytes(1); // skip past for next match
		assertNull("Should not find further bytes", scanner.scanToTarget(target, 1000, shouldNotOccur));
	}

	/**
	 * Ensure can build long.
	 */
	public void testBuildLong() {

		// Create scanner with data just for first long
		StreamBufferScanner scanner = createScanner(1, 1, 1, 1, 1, 1, 1, 1);

		// Ensure can build first long
		assertEquals("Incorrect immediate long", 0x0101010101010101L, scanner.buildLong(shouldNotOccur));
		assertEquals("Should be able build same long", 0x0101010101010101L, scanner.buildLong(shouldNotOccur));

		// Skip the long bytes
		scanner.skipBytes(8);

		// Ensure require further data for second long
		assertEquals("Should require further bytes for another long", -1, scanner.buildLong(shouldNotOccur));

		// Incrementally add further data (ensuring requires all data)
		for (int i = 1; i <= 7; i++) {
			scanner.appendStreamBuffer(createBuffer(2));
			assertEquals("Only " + i + " bytes for long", -1, scanner.buildLong(shouldNotOccur));
		}

		// Add remaining byte to now build the long
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Incorrect built long", 0x0202020202020202L, scanner.buildLong(shouldNotOccur));

		// Ensure can scan in 7 bytes then 1 byte (test the bulk data reads)
		scanner.appendStreamBuffer(createBuffer(3));
		assertEquals("Only first byte", -1, scanner.buildLong(shouldNotOccur));
		scanner.appendStreamBuffer(createBuffer(3, 3, 3, 3, 3, 3, 3));
		assertEquals("Should bulk read in bytes", 0x0303030303030303L, scanner.buildLong(shouldNotOccur));
	}

	/**
	 * Ensure handle negative values in building long.
	 */
	public void testBuildLongWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xfe);
		assertEquals("Should provide long value", 0xfffffffffffffffeL, scanner.buildLong(shouldNotOccur));
	}

	/**
	 * Series of 0xff bytes should not occur when building a long.
	 */
	public void testBuildIllegalLong() {

		// Create the scanner with 0xff long value in bytes
		StreamBufferScanner scanner = createScanner(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
		assertEquals("Not enough bytes", -1, scanner.buildLong(shouldNotOccur));

		// Add the final byte for -1 long value
		scanner.appendStreamBuffer(createBuffer(0xff));
		final Exception exception = new Exception("TEST");
		try {
			long value = scanner.buildLong(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Should be same exception", exception, ex);
		}
	}

	/**
	 * Ensure can build short.
	 */
	public void testBuildShort() {

		// Create scanner with data just for first long
		StreamBufferScanner scanner = createScanner(1, 1);

		// Ensure can build first short
		assertEquals("Incorrect immediate short", 0x0101L, scanner.buildShort(shouldNotOccur));
		assertEquals("Should be able build same long", 0x0101L, scanner.buildShort(shouldNotOccur));

		// Skip the short bytes
		scanner.skipBytes(2);

		// Ensure require further data for second short
		assertEquals("Should require further bytes for another short", -1, scanner.buildShort(shouldNotOccur));

		// Incrementally add further data (ensuring requires all data)
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Only 1 byte for short", -1, scanner.buildShort(shouldNotOccur));

		// Add remaining byte to now build the short
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Incorrect built short", 0x0202L, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Ensure handle negative values in building short.
	 */
	public void testBuildShortWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xff, 0xfe);
		assertEquals("Should provide short value", -2, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Series of -1 bytes should not occur when building a short.
	 */
	public void testBuildIllegalShort() {

		// Create the scanner with -1 short value in bytes
		StreamBufferScanner scanner = createScanner(0xff);
		assertEquals("Not enough bytes", -1, scanner.buildShort(shouldNotOccur));

		// Add the final byte for -1 short value
		scanner.appendStreamBuffer(createBuffer(0xff));
		final Exception exception = new Exception("TEST");
		try {
			long value = scanner.buildShort(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Should be same exception", exception, ex);
		}
	}

	/**
	 * Ensure can skip bytes.
	 */
	public void testSkipBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2);

		// Ensure can skip bytes
		scanner.skipBytes(3);
		assertEquals("Incorrect long", 0x0202020202020202L, scanner.buildLong(shouldNotOccur));
		assertEquals("Incorrect short", 0x0202, scanner.buildShort(shouldNotOccur));

		// Ensure find target after skipping byte
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals("Incorrect target after skipping", 8, sequence.length());
		for (int i = 0; i < 8; i++) {
			assertEquals("Incorrect sequnce byte " + i, 2, sequence.byteAt(i));
		}

		// Ensure scan is exclusive
		assertEquals("Should now obtain target and next byte", 0x0102, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Ensure can peek bytes.
	 */
	public void testPeekBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Ensure not find byte
		assertEquals("Should not find byte", -1, scanner.peekToTarget(new ScanTarget((byte) 4)));
		assertEquals("Should find byte", 2, scanner.peekToTarget(new ScanTarget((byte) 3)));

		// Should not have progressed through buffer data
		assertEquals("Should still be at start", 0x0102, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Ensure can peek bytes by larger buffer.
	 */
	public void testPeekBytesOnLargerBuffer() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1);

		// Skip first 2 bytes
		scanner.skipBytes(2);

		// Ensure find byte
		assertEquals("Incorrect offset position from skip", 12, scanner.peekToTarget(new ScanTarget((byte) 1)));
	}

	/**
	 * Ensure can scan in fixed set of bytes.
	 */
	public void testScanBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3, 4, 5, 6);

		// Scan in bytes
		StreamBufferByteSequence sequence = scanner.scanBytes(3);
		assertEquals("Incorrect number of bytes", 3, sequence.length());
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect byte", i + 1, sequence.byteAt(i));
		}

		// Scan in remaining bytes
		sequence = scanner.scanBytes(3);
		assertEquals("Should have remaining bytes", 3, sequence.length());
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect byte", i + 4, sequence.byteAt(i));
		}
	}

	/**
	 * Ensure can scan in fixed set of bytes.
	 */
	public void testScanBytesAcrossMultipleBuffers() {

		final int BYTES_LENGTH = 10;

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertNull("Not enough bytes", scanner.scanBytes(BYTES_LENGTH));

		// Scan in bytes (incrementally in worst case scenario)
		for (int i = 1; i < BYTES_LENGTH; i++) {
			assertNull("Should not obtain sequence, as not enough bytes - " + i, scanner.scanBytes(BYTES_LENGTH));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Should now obtain sequence
		StreamBufferByteSequence sequence = scanner.scanBytes(BYTES_LENGTH);
		assertNotNull("Should have all bytes", sequence);

		// Ensure correct bytes
		assertEquals("Incorrect number of bytes", BYTES_LENGTH, sequence.length());
		for (int i = 0; i < BYTES_LENGTH; i++) {
			assertEquals("Incorrect byte", i + 1, sequence.byteAt(i));
		}
	}

	/**
	 * Creates a number buffer with value being repeated for rest of long bytes.
	 * Then scans for that value to ensure returns first.
	 * 
	 * @param value
	 *            Value.
	 * @return {@link ByteBuffer} for long read.
	 */
	private static int indexOfFirst(int value) {

		// Create the buffer
		long data = 0;
		for (int i = 0; i < value; i++) {
			data <<= 8; // move up a byte
			data += (byte) (i + 1);
		}
		for (int i = value; i < 8; i++) {
			data <<= 8; // move up a byte
			data += (byte) value;
		}

		// Scan the buffer for the value
		int scanIndex = StreamBufferScanner.indexOf(data, new ScanTarget((byte) value));

		// Return the scan index
		return scanIndex;
	}

	/**
	 * Convenience method to scan.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 * @param startPosition
	 *            Start position within the {@link ByteBuffer}.
	 * @param value
	 *            Byte value to scan for.
	 * @return {@link StreamBufferByteSequence}. Or <code>null</code> if not
	 *         found.
	 */
	private static StreamBufferByteSequence scan(StreamBuffer<ByteBuffer> buffer, int value) {
		StreamBufferScanner scanner = new StreamBufferScanner();
		scanner.appendStreamBuffer(buffer);
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) value), 1000, shouldNotOccur);
		return sequence;
	}

	/**
	 * Creates a {@link StreamBufferScanner} with the data.
	 * 
	 * @param values
	 *            Values for the data.
	 * @return {@link StreamBufferScanner}.
	 */
	private static StreamBufferScanner createScanner(int... values) {
		StreamBuffer<ByteBuffer> buffer = createBuffer(values);
		StreamBufferScanner scanner = new StreamBufferScanner();
		scanner.appendStreamBuffer(buffer);
		return scanner;
	}

	/**
	 * Creates a test {@link StreamBuffer}.
	 * 
	 * @param values
	 *            Byte values for the {@link StreamBuffer}.
	 * @return {@link StreamBuffer}.
	 */
	private static StreamBuffer<ByteBuffer> createBuffer(int... values) {
		byte[] data = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			data[i] = (byte) values[i];
		}
		return createBuffer(data);
	}

	/**
	 * Creates a test {@link StreamBuffer}.
	 * 
	 * @param data
	 *            Data for the {@link StreamBuffer}.
	 * @return {@link StreamBuffer}.
	 */
	private static StreamBuffer<ByteBuffer> createBuffer(byte[] data) {
		MockBufferPool pool = new MockBufferPool(() -> ByteBuffer.allocateDirect(data.length));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
		buffer.write(data);
		return buffer;
	}

}