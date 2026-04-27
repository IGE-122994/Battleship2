package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Compass.
 * Author: Francisco Silva
 * Date: 2026-04-27
 * Cyclomatic Complexity:
 * - Constructor: 1
 * - getDirection(): 1
 * - toString(): 1
 * - charToCompass(): 4
 * - randomBearing(): 1
 * - values(): 1
 * - valueOf(): 1
 */
public class CompassTest {

	private Compass compass;

	@BeforeEach
	void setUp() {
		compass = Compass.NORTH;
	}

	@AfterEach
	void tearDown() {
		compass = null;
	}

	// ===================== constructor - CC=1 =====================

	@Test
	void constructor() {
		assertNotNull(compass, "Error: Compass instance should not be null.");
	}

	// ===================== getDirection - CC=1 =====================

	@Test
	void getDirection() {
		assertAll(
				() -> assertEquals('n', Compass.NORTH.getDirection(), "Error: Direction for NORTH should be 'n'."),
				() -> assertEquals('s', Compass.SOUTH.getDirection(), "Error: Direction for SOUTH should be 's'."),
				() -> assertEquals('e', Compass.EAST.getDirection(), "Error: Direction for EAST should be 'e'."),
				() -> assertEquals('o', Compass.WEST.getDirection(), "Error: Direction for WEST should be 'o'.")
		);
	}

	// ===================== toString - CC=1 =====================

	@Test
	void toStringTest() {
		assertAll(
				() -> assertEquals("n", Compass.NORTH.toString(), "Error: String representation for NORTH should be 'n'."),
				() -> assertEquals("s", Compass.SOUTH.toString(), "Error: String representation for SOUTH should be 's'."),
				() -> assertEquals("e", Compass.EAST.toString(), "Error: String representation for EAST should be 'e'."),
				() -> assertEquals("o", Compass.WEST.toString(), "Error: String representation for WEST should be 'o'.")
		);
	}

	// ===================== charToCompass - CC=4 =====================

	@Test
	void charToCompass1() {
		// Path: 'n' → NORTH
		assertEquals(Compass.NORTH, Compass.charToCompass('n'), "Error: 'n' should map to Compass.NORTH.");
	}

	@Test
	void charToCompass2() {
		// Path: 's' → SOUTH
		assertEquals(Compass.SOUTH, Compass.charToCompass('s'), "Error: 's' should map to Compass.SOUTH.");
	}

	@Test
	void charToCompass3() {
		// Path: 'e' → EAST
		assertEquals(Compass.EAST, Compass.charToCompass('e'), "Error: 'e' should map to Compass.EAST.");
	}

	@Test
	void charToCompass4() {
		// Path: 'o' → WEST
		assertEquals(Compass.WEST, Compass.charToCompass('o'), "Error: 'o' should map to Compass.WEST.");
	}

	@Test
	void charToCompass5() {
		// Path: inválido → null
		assertNull(Compass.charToCompass('x'), "Error: 'x' should map to null.");
	}

	@Test
	void charToCompass6() {
		// Path: caracter nulo → null
		assertNull(Compass.charToCompass('\0'), "Error: Null character should map to null.");
	}

	// ===================== randomBearing - CC=1 =====================

	@Test
	void randomBearing() {
		// Path: retorna valor não nulo e válido do enum
		Compass result = Compass.randomBearing();
		assertAll(
				() -> assertNotNull(result, "Error: randomBearing should not return null."),
				() -> assertTrue(
						result == Compass.NORTH ||
								result == Compass.SOUTH ||
								result == Compass.EAST  ||
								result == Compass.WEST,
						"Error: randomBearing should return a valid Compass value."
				)
		);
	}

	// ===================== values - CC=1 =====================

	@Test
	void values() {
		assertEquals(4, Compass.values().length, "Error: Compass should have 4 values.");
	}

	// ===================== valueOf - CC=1 =====================

	@Test
	void valueOf() {
		assertAll(
				() -> assertEquals(Compass.NORTH, Compass.valueOf("NORTH"), "Error: valueOf('NORTH') should return NORTH."),
				() -> assertEquals(Compass.SOUTH, Compass.valueOf("SOUTH"), "Error: valueOf('SOUTH') should return SOUTH."),
				() -> assertEquals(Compass.EAST, Compass.valueOf("EAST"), "Error: valueOf('EAST') should return EAST."),
				() -> assertEquals(Compass.WEST, Compass.valueOf("WEST"), "Error: valueOf('WEST') should return WEST.")
		);
	}
}