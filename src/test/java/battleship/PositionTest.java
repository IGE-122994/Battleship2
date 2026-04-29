package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Test class for Position.
 * Author: Pedro Vicêncio
 * Date: 2026-04-23 12:00
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getRow: 1
 * - getColumn: 1
 * - isInside: 4
 * - isAdjacentTo: 4
 * - isOccupied: 1
 * - isHit: 1
 * - occupy: 1
 * - shoot: 1
 * - equals: 3
 * - hashCode: 1
 * - toString: 1
 * - getClassicRow: 1
 * - getClassicColumn: 1
 * - randomPosition: 1
 * - adjacentPositions: 1
 */
public class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@DisplayName("Test Position constructor")
	@Test
	void constructor() {
		Position pos = new Position(1, 1);
		assertAll(
			() -> assertNotNull(pos, "Failed to create Position: object is null"),
			() -> assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow()),
			() -> assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn()),
			() -> assertFalse(pos.isOccupied(), "New position should not be occupied"),
			() -> assertFalse(pos.isHit(), "New position should not be hit")
		);
	}

	@DisplayName("Test getRow method")
	@Test
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@DisplayName("Test getColumn method")
	@Test
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@DisplayName("Test isInside with valid position (0,0)")
	@Test
	void isInside1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be inside");
	}

	@DisplayName("Test isInside with negative row")
	@Test
	void isInside2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should not be inside");
	}

	@DisplayName("Test isInside with negative column")
	@Test
	void isInside3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should not be inside");
	}

	@DisplayName("Test isInside with row >= BOARD_SIZE")
	@Test
	void isInside4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should not be inside");
	}

	@DisplayName("Test isAdjacentTo with horizontal adjacency")
	@Test
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@DisplayName("Test isAdjacentTo with vertical adjacency")
	@Test
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@DisplayName("Test isAdjacentTo with diagonal adjacency")
	@Test
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@DisplayName("Test isAdjacentTo with non-adjacent positions")
	@Test
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@DisplayName("Test isAdjacentTo with null input throws exception")
	@Test
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"isAdjacentTo should throw NullPointerException for null input");
	}

	@DisplayName("Test isOccupied method")
	@Test
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@DisplayName("Test isHit method")
	@Test
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@DisplayName("Test occupy method")
	@Test
	void occupy() {
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@DisplayName("Test shoot method")
	@Test
	void shoot() {
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@DisplayName("Test equals with same object")
	@Test
	void equals1() {
		assertTrue(position.equals(position), "A position should be equal to itself");
	}

	@DisplayName("Test equals with equal positions")
	@Test
	void equals2() {
		Position same = new Position(2, 3);
		assertTrue(position.equals(same), "Equal positions not identified as equal");
	}

	@DisplayName("Test equals with different positions")
	@Test
	void equals3() {
		Position other = new Position(2, 4);
		assertFalse(position.equals(other), "Positions with the same row but different column should not be equal");
	}

	@DisplayName("Test hashCode consistency")
	@Test
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

	@DisplayName("Test toString format")
	@Test
	void toStringFormat() {
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
	}

	@DisplayName("Test getClassicRow method")
	@Test
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get classic row: expected C but got " + position.getClassicRow());
	}

	@DisplayName("Test getClassicColumn method")
	@Test
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "Failed to get classic column: expected 4 but got " + position.getClassicColumn());
	}

	@DisplayName("Test randomPosition method")
	@Test
	void randomPosition() {
		Position pos = Position.randomPosition();
		assertNotNull(pos, "Random position should not be null");
		assertTrue(pos.isInside(), "Random position should be inside the board");
	}

	@DisplayName("Test adjacentPositions method")
	@Test
	void adjacentPositions() {
		List<IPosition> adj = position.adjacentPositions();
		assertNotNull(adj, "Adjacent positions should not be null");
		// Assuming position (2,3) has 8 adjacent, but check if all are inside
		for (IPosition p : adj) {
			assertTrue(p.isInside(), "Adjacent position should be inside the board");
		}
	}
}