package battleship;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Game class.
 * Author: Diogo Almeida
 * Date: 2026-05-02 21:46
 * Cyclomatic Complexity (tested methods only):
 * - Game(IFleet): 1
 * - getMyFleet(): 1
 * - getMyMoves(): 1
 * - getAlienFleet(): 1
 * - getAlienMoves(): 1
 * - getRepeatedShots(): 1
 * - getInvalidShots(): 1
 * - getHits(): 1
 * - getSunkShips(): 1
 * - getRemainingShips(): 1
 * - fireSingleShot(): 5
 * - fireShots(): 2
 * - repeatedShot(): 2
 * - readEnemyFire(): 4

 * Excluded by design:
 * - printBoard(), printMyBoard(), printAlienBoard(), over()  (GUI)
 * - jsonShots() (serialization)
 * - randomEnemyFire() (non-deterministic)
 */
public class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(new Fleet());
    }

    @AfterEach
    void tearDown() {
        game = null;
    }

    // ------------------------------------------------------------
    // Constructor & basic getters (CC = 1 each)
    // ------------------------------------------------------------

    @DisplayName("Construtor inicializado corretamente")
    @Test
    void constructor() {
        assertNotNull(game, "Error: Game instance should not be null.");
        assertNotNull(game.getMyFleet(), "Error: myFleet should not be null.");
        assertNotNull(game.getAlienMoves(), "Error: alienMoves should not be null.");
        assertNotNull(game.getMyMoves(), "Error: myMoves should not be null.");
        assertEquals(0, game.getInvalidShots(), "Error: invalidShots should start at 0.");
        assertEquals(0, game.getRepeatedShots(), "Error: repeatedShots should start at 0.");
        assertEquals(0, game.getHits(), "Error: hits should start at 0.");
        assertEquals(0, game.getSunkShips(), "Error: sunkShips should start at 0.");
    }

    @DisplayName("getMyFleet funciona corretamente")
    @Test
    void getMyFleet() {
        Fleet fleet = new Fleet();
        Game g = new Game(fleet);
        assertSame(fleet, g.getMyFleet(), "Error: getMyFleet should return the same instance passed to constructor.");
    }

    @DisplayName("getMyMoves inicialmente vazio")
    @Test
    void getMyMoves() {
        assertTrue(game.getMyMoves().isEmpty(), "Error: myMoves should be empty initially.");
    }

    @DisplayName("getAlienFleet funciona corretamente")
    @Test
    void getAlienFleet() {
        Fleet fleet = new Fleet();
        Game g = new Game(fleet);
        assertSame(fleet, g.getAlienFleet(), "Error: getAlienFleet should return the same instance passed to constructor.");
    }

    @DisplayName("getAlienMoves inicialmente vazio")
    @Test
    void getAlienMoves() {
        assertTrue(game.getAlienMoves().isEmpty(), "Error: alienMoves should be empty initially.");
    }

    // ------------------------------------------------------------
    // fireSingleShot (CC = 5)
    // ------------------------------------------------------------

    @DisplayName("Tiro único - posição inválida (incrementa tiros inválidos)")
    @Test
    void fireSingleShot1() {
        Position invalid = new Position(-1, 5);
        IGame.ShotResult result = game.fireSingleShot(invalid, false);

        assertAll(
                () -> assertEquals(1, game.getInvalidShots(), "Error: invalidShots should increment."),
                () -> assertFalse(result.valid(), "Error: invalid shot should be marked invalid."),
                () -> assertFalse(result.repeated(), "Error: invalid shot should not be repeated."),
                () -> assertNull(result.ship(), "Error: invalid shot should not reference a ship."),
                () -> assertFalse(result.sunk(), "Error: invalid shot cannot sink a ship.")
        );
    }

    @DisplayName("Tiro único - tiro repetido (incrementa tiros repetidos)")
    @Test
    void fireSingleShot2() {
        Position pos = new Position(2, 3);
        IGame.ShotResult result = game.fireSingleShot(pos, true);

        assertAll(
                () -> assertEquals(1, game.getRepeatedShots(), "Error: repeatedShots should increment."),
                () -> assertTrue(result.valid(), "Error: repeated shot should still be valid."),
                () -> assertTrue(result.repeated(), "Error: repeated flag should mark shot as repeated."),
                () -> assertNull(result.ship(), "Error: repeated shot should not reference a ship."),
                () -> assertFalse(result.sunk(), "Error: repeated shot cannot sink a ship.")
        );
    }

    @DisplayName("Tiro único - tiro válido falhado (não afeta contadores)")
    @Test
    void fireSingleShot3() {
        Position pos = new Position(3, 3);
        IGame.ShotResult result = game.fireSingleShot(pos, false);

        assertAll(
                () -> assertEquals(0, game.getInvalidShots(), "Error: invalidShots should remain 0."),
                () -> assertEquals(0, game.getRepeatedShots(), "Error: repeatedShots should remain 0."),
                () -> assertEquals(0, game.getHits(), "Error: hits should remain 0."),
                () -> assertEquals(0, game.getSunkShips(), "Error: sunkShips should remain 0."),
                () -> assertTrue(result.valid(), "Error: miss should be valid."),
                () -> assertFalse(result.repeated(), "Error: miss should not be repeated."),
                () -> assertNull(result.ship(), "Error: miss should not reference a ship."),
                () -> assertFalse(result.sunk(), "Error: miss cannot sink a ship.")
        );
    }

    @DisplayName("Tiro único - tiro certeiro que não afunda (aumenta tiros acertados)")
    @Test
    void fireSingleShot4() {
        Fleet fleet = new Fleet();
        Ship caravel = new Caravel(Compass.NORTH, new Position(1, 1));
        fleet.addShip(caravel);
        Game g = new Game(fleet);

        IPosition target = caravel.getPositions().get(0);
        IGame.ShotResult result = g.fireSingleShot(target, false);

        assertAll(
                () -> assertEquals(1, g.getHits(), "Error: hits should increment."),
                () -> assertEquals(0, g.getSunkShips(), "Error: ship should not be sunk yet."),
                () -> assertTrue(result.valid(), "Error: hit should be valid."),
                () -> assertSame(caravel, result.ship(), "Error: result should reference the hit ship."),
                () -> assertFalse(result.sunk(), "Error: ship should still be floating.")
        );
    }

    @DisplayName("Tiro único - tiro certeiro que afunda (aumenta tiros acertados e barcos afundados")
    @Test
    void fireSingleShot5() {
        Fleet fleet = new Fleet();
        Ship caravel = new Caravel(Compass.NORTH, new Position(1, 1));
        fleet.addShip(caravel);
        Game g = new Game(fleet);

        IPosition p1 = caravel.getPositions().get(0);
        IPosition p2 = caravel.getPositions().get(1);

        g.fireSingleShot(p1, false);
        IGame.ShotResult result = g.fireSingleShot(p2, false);

        assertAll(
                () -> assertEquals(2, g.getHits(), "Error: hits should be 2."),
                () -> assertEquals(1, g.getSunkShips(), "Error: sunkShips should be 1."),
                () -> assertTrue(result.sunk(), "Error: ship should be sunk."),
                () -> assertSame(caravel, result.ship(), "Error: result should reference sunk ship.")
        );
    }

    // ------------------------------------------------------------
    // fireShots (CC = 2)
    // ------------------------------------------------------------

    @DisplayName("Disparos - número correto de tiros (cria um movimento)")
    @Test
    void fireShots1() {
        List<IPosition> shots = List.of(
                new Position(2, 3),
                new Position(2, 4),
                new Position(2, 5)
        );

        game.fireShots(shots);

        assertEquals(1, game.getAlienMoves().size(),
                "Error: alienMoves should contain exactly one move.");
    }

    @DisplayName("Disparos - número incorreto de tiros (lança exceção)")
    @Test
    void fireShots2() {
        List<IPosition> shots = List.of(
                new Position(2, 3),
                new Position(2, 4)
        );

        assertThrows(IllegalArgumentException.class,
                () -> game.fireShots(shots),
                "Error: fireShots should throw when number of shots != 3.");
    }

    // ------------------------------------------------------------
    // repeatedShot (CC = 2)
    // ------------------------------------------------------------

    @DisplayName("Tiro repetido - devolve True em repeatedShot (posição repetida)")
    @Test
    void repeatedShot1() {
        List<IPosition> shots = List.of(
                new Position(2, 3),
                new Position(2, 4),
                new Position(2, 5)
        );
        game.fireShots(shots);

        assertTrue(game.repeatedShot(new Position(2, 3)),
                "Error: position should be marked repeated after being shot.");
    }

    @DisplayName("Tiro repetido - devolve False em repeatedShot (posição não repetida)")
    @Test
    void repeatedShot2() {
        assertFalse(game.repeatedShot(new Position(2, 3)),
                "Error: position should not be repeated before any shots.");
    }

    // ------------------------------------------------------------
    // readEnemyFire (CC = 4)
    // ------------------------------------------------------------

    @DisplayName("Posição válida com tokens separados - regista o movimento")
    @Test
    void readEnemyFire1() {
        Scanner scanner = new Scanner("A 1 B 2 C 3");
        String json = game.readEnemyFire(scanner);

        assertNotNull(json, "Error: JSON should not be null.");
        assertEquals(1, game.getAlienMoves().size(),
                "Error: one move should be registered.");
    }

    @DisplayName("Posição válida com tokens juntos - regista o movimento")
    @Test
    void readEnemyFire2() {
        Scanner scanner = new Scanner("A3 B4 C5");
        String json = game.readEnemyFire(scanner);

        assertNotNull(json, "JSON should not be null for compact tokens.");
        assertEquals(1, game.getAlienMoves().size(),
                "One move should be registered when using compact tokens.");
    }

    @DisplayName("Posição incompleta - lança exceção")
    @Test
    void readEnemyFire3() {
        Scanner scanner = new Scanner("A 1 B");
        assertThrows(IllegalArgumentException.class,
                () -> game.readEnemyFire(scanner),
                "Error: incomplete position should throw.");
    }

    @DisplayName("Número errado de posições - lança exceção")
    @Test
    void readEnemyFire4() {
        Scanner scanner = new Scanner("A 1 B 2");
        assertThrows(IllegalArgumentException.class,
                () -> game.readEnemyFire(scanner),
                "Error: wrong number of positions should throw.");
    }

    // ------------------------------------------------------------
    // getRemainingShips (CC = 1)
    // ------------------------------------------------------------

    @DisplayName("getRemainingShips funciona corretamente")
    @Test
    void getRemainingShips() {
        IFleet fleet = game.getMyFleet();
        Ship s1 = new Barge(Compass.NORTH, new Position(1, 1));
        Ship s2 = new Frigate(Compass.EAST, new Position(5, 5));

        fleet.addShip(s1);
        assertEquals(1, game.getRemainingShips(), "Error: should be 1 ship.");

        fleet.addShip(s2);
        assertEquals(2, game.getRemainingShips(), "Error: should be 2 ships.");

        s2.sink();
        assertEquals(1, game.getRemainingShips(), "Error: should be 1 floating ship.");
    }

    // ------------------------------------------------------------
    // Counter consistency test
    // ------------------------------------------------------------

    @DisplayName("Teste conjunto aos contadores - atualizações corretas")
    @Test
    void counters() {
        Fleet fleet = new Fleet();
        Ship caravel = new Caravel(Compass.NORTH, new Position(1, 1));
        fleet.addShip(caravel);
        Game g = new Game(fleet);

        IPosition invalid = new Position(-1, 0);
        IPosition hit = caravel.getPositions().get(0);

        g.fireSingleShot(invalid, false);
        g.fireSingleShot(hit, false);
        g.fireSingleShot(hit, true);

        assertAll(
                () -> assertEquals(1, g.getInvalidShots(), "Error: invalidShots should be 1."),
                () -> assertEquals(1, g.getHits(), "Error: hits should be 1."),
                () -> assertEquals(1, g.getRepeatedShots(), "Error: repeatedShots should be 1."),
                () -> assertEquals(0, g.getSunkShips(), "Error: no ship should be sunk.")
        );
    }
}
