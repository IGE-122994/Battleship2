package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Ship.
 * Author: Pedro Vicêncio
 * Date: 2026-04-23
 * Time: 13:00
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getCategory: 1
 * - getSize: 1
 * - getBearing: 1
 * - getPositions: 1
 * - stillFloating: 2
 * - shoot: 2
 * - occupies: 2
 * - tooCloseTo (IShip): 2
 * - tooCloseTo (IPosition): 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 */
public class ShipTest {

    private Ship ship;

    @BeforeEach
    void setUp() {
        // Since Ship is abstract, instantiate it with a concrete subclass (e.g., Barge)
        ship = new Barge(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        ship = null;
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @DisplayName("Test Ship constructor")
    @Test
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
    }

    /**
     * Test for the getCategory method.
     * Cyclomatic Complexity: 1
     */
    @DisplayName("Test getCategory method")
    @Test
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    /**
     * Test for the getSize method.
     * Cyclomatic Complexity: 1
     */
    @DisplayName("Test getSize method")
    @Test
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    /**
     * Test for the getBearing method.
     * Cyclomatic Complexity: 1
     */
    @DisplayName("Test getBearing method")
    @Test
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    /**
     * Test for the getPositions method.
     * Cyclomatic Complexity: 1
     */
    @DisplayName("Test getPositions method")
    @Test
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test stillFloating with all positions intact")
    @Test
    void testStillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @DisplayName("Test stillFloating with all positions hit")
    @Test
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    /**
     * Test for the shoot method (valid position).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test shoot with valid position")
    @Test
    void testShoot1() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    /**
     * Test for the shoot method (invalid position).
     */
    @DisplayName("Test shoot with invalid position throws exception")
    @Test
    void testShoot2() {
        Position invalidPos = new Position(-1, 5);
        assertThrows(AssertionError.class, () -> ship.shoot(invalidPos), "Shoot should throw AssertionError for position not inside.");
    }

    /**
     * Test for the occupies method (position occupied).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test occupies with position occupied")
    @Test
    void testOccupies1() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    /**
     * Test for the occupies method (position not occupied).
     */
    @DisplayName("Test occupies with position not occupied")
    @Test
    void testOccupies2() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships too close).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test tooCloseTo with ships too close")
    @Test
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships not close).
     */
    @DisplayName("Test tooCloseTo with ships not close")
    @Test
    void testTooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions adjacent).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test tooCloseTo with positions adjacent")
    @Test
    void testTooCloseToPosition1() {
        Position pos = new Position(5, 6); // Adjacent position
        assertTrue(ship.tooCloseTo(pos), "Error: Ship should be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions not adjacent).
     */
    @DisplayName("Test tooCloseTo with positions not adjacent")
    @Test
    void testTooCloseToPosition2() {
        Position pos = new Position(7, 7); // Non-adjacent position
        assertFalse(ship.tooCloseTo(pos), "Error: Ship should not be too close to the given position.");
    }

    /**
     * Test for the getTopMostPos method (size 1).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test getTopMostPos with size 1")
    @Test
    void testGetTopMostPos1() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
    }

    /**
     * Test for the getTopMostPos method (size > 1).
     */
    @DisplayName("Test getTopMostPos with size > 1")
    @Test
    void testGetTopMostPos2() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(5, 5));
        assertEquals(5, frigate.getTopMostPos(), "Error: The topmost position should be 5 for Frigate.");
    }

    /**
     * Test for the getBottomMostPos method (size 1).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test getBottomMostPos with size 1")
    @Test
    void testGetBottomMostPos1() {
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
    }

    /**
     * Test for the getBottomMostPos method (size > 1).
     */
    @DisplayName("Test getBottomMostPos with size > 1")
    @Test
    void testGetBottomMostPos2() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(5, 5));
        assertEquals(8, frigate.getBottomMostPos(), "Error: The bottommost position should be 8 for Frigate.");
    }

    /**
     * Test for the getLeftMostPos method (size 1).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test getLeftMostPos with size 1")
    @Test
    void testGetLeftMostPos1() {
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
    }

    /**
     * Test for the getLeftMostPos method (size > 1).
     */
    @DisplayName("Test getLeftMostPos with size > 1")
    @Test
    void testGetLeftMostPos2() {
        Ship frigate = new Frigate(Compass.EAST, new Position(5, 5));
        assertEquals(5, frigate.getLeftMostPos(), "Error: The leftmost position should be 5 for Frigate EAST.");
    }

    /**
     * Test for the getRightMostPos method (size 1).
     * Cyclomatic Complexity: 2
     */
    @DisplayName("Test getRightMostPos with size 1")
    @Test
    void testGetRightMostPos1() {
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method (size > 1).
     */
    @DisplayName("Test getRightMostPos with size > 1")
    @Test
    void testGetRightMostPos2() {
        Ship frigate = new Frigate(Compass.EAST, new Position(5, 5));
        assertEquals(8, frigate.getRightMostPos(), "Error: The rightmost position should be 8 for Frigate EAST.");
    }
}