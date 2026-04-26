package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GameReportPDFTest {

    private Game game;
    private Fleet fleet;

    @BeforeEach
    void setUp() {
        fleet = new Fleet();
        // Adicionar navios manualmente
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 1)));
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 3)));
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 5)));
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 7)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position('C', 1)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position('C', 4)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position('C', 7)));
        fleet.addShip(new Frigate(Compass.NORTH, new Position('E', 1)));
        fleet.addShip(new Carrack(Compass.NORTH, new Position('E', 4)));
        fleet.addShip(new Carrack(Compass.NORTH, new Position('E', 7)));
        fleet.addShip(new Galleon(Compass.NORTH, new Position('G', 1)));
        game = new Game(fleet);
        System.out.println("Navios na frota: " + game.getMyFleet().getShips().size());
    }

    // ===================== generate =====================

    @Test
    @DisplayName("generate retorna caminho do ficheiro PDF")
    void testGenerateReturnsPdfPath() {
        String path = GameReportPDF.generate(game, "1m 30s 500ms");
        assertNotNull(path);
        assertTrue(path.endsWith(".pdf"));
    }

    @Test
    @DisplayName("generate cria o ficheiro PDF em disco")
    void testGenerateCreatesFile() {
        String path = GameReportPDF.generate(game, "1m 30s 500ms");
        File file = new File(path);
        assertTrue(file.exists());
        file.delete();
    }

    @Test
    @DisplayName("generate cria pasta data/ se não existir")
    void testGenerateCreatesDataDirectory() {
        GameReportPDF.generate(game, "0m 10s 0ms");
        File dir = new File("data/");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    @Test
    @DisplayName("generate com duração vazia não lança exceção")
    void testGenerateWithEmptyDuration() {
        assertDoesNotThrow(() -> GameReportPDF.generate(game, ""));
    }

    @Test
    @DisplayName("generate retorna caminho com prefixo 'data/relatorio_'")
    void testGeneratePathPrefix() {
        String path = GameReportPDF.generate(game, "2m 36s 469ms");
        assertTrue(path.startsWith("data/relatorio_"));
    }

    // ===================== summariseMove - todos os ramos =====================

    @Test
    @DisplayName("generate com tiro fora do tabuleiro cobre ramo 'exterior'")
    void testGenerateWithOutsideShot() {
        List<IPosition> shots = List.of(
                new Position(-1, 0),
                new Position(-1, 1),
                new Position(-1, 2)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"));
    }

    @Test
    @DisplayName("generate com tiro repetido cobre ramo 'repetido'")
    void testGenerateWithRepeatedShot() {
        List<IPosition> shots1 = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots1);

        List<IPosition> shots2 = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots2);

        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 10s 0ms"));
    }

    @Test
    @DisplayName("generate com tiro na água cobre ramo 'água'")
    void testGenerateWithWaterShot() {
        // B1, B2, B3 não têm navios
        List<IPosition> shots = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"));
    }

    @Test
    @DisplayName("generate com acerto num navio cobre ramo 'acerto'")
    void testGenerateWithHitOnShip() {
        List<IShip> ships = game.getMyFleet().getShips();
        assumeTrue(!ships.isEmpty(), "Frota sem navios — ignorar teste");

        IPosition shipPos = ships.get(0).getPositions().get(0);

        List<IPosition> shots = List.of(
                shipPos,
                new Position('B', 8),
                new Position('B', 9)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"));
    }

    @Test
    @DisplayName("generate com navio afundado cobre ramo 'afundado'")
    void testGenerateWithSunkShip() {
        List<IShip> ships = game.getMyFleet().getShips();
        assumeTrue(!ships.isEmpty(), "Frota sem navios — ignorar teste");

        // Barge em A1 — tamanho 1, afunda com 1 tiro
        IPosition pos = new Position('A', 1);

        List<IPosition> shots = List.of(
                pos,
                new Position('B', 8),
                new Position('B', 9)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"));
    }

    @Test
    @DisplayName("generate com lista de moves vazia cobre ramo 'sem jogadas'")
    void testGenerateWithNoMoves() {
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 0s 0ms"));
    }

    @Test
    @DisplayName("generate com múltiplos moves cobre addMovesSummary completo")
    void testGenerateWithMultipleMoves() {
        List<IPosition> shots1 = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots1);

        List<IPosition> shots2 = List.of(
                new Position('B', 4),
                new Position('B', 5),
                new Position('B', 6)
        );
        game.fireShots(shots2);

        assertDoesNotThrow(() -> GameReportPDF.generate(game, "1m 0s 0ms"));
    }

    @Test
    @DisplayName("generate com todos os navios afundados cobre addFinalBoard completo")
    void testGenerateWithAllShipsSunk() {
        IFleet myFleet = game.getMyFleet();
        assumeTrue(!myFleet.getShips().isEmpty(), "Frota sem navios — ignorar teste");

        for (IShip ship : myFleet.getShips()) {
            for (IPosition pos : ship.getPositions()) {
                try {
                    List<IPosition> shots = List.of(
                            pos,
                            new Position('J', 8),
                            new Position('J', 9)
                    );
                    game.fireShots(shots);
                } catch (Exception e) {
                    // ignorar duplicados
                }
            }
        }
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "5m 0s 0ms"));
    }

    @Test
    @DisplayName("summariseMove sem resultados retorna '-'")
    void testGenerateWithEmptyShotResults() {
        // Move com lista de resultados vazia — summariseMove retorna "-"
        List<IPosition> shots = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"));
    }

    @Test
    @DisplayName("addFinalBoard cobre tiro em posição adjacente a navio afundado")
    void testGenerateWithShotOnAdjacentPosition() {
        // Afundar a Barge em A1
        List<IPosition> shots1 = List.of(
                new Position('A', 1),
                new Position('B', 8),
                new Position('B', 9)
        );
        game.fireShots(shots1);

        // Disparar na posição adjacente A2 (halo da Barge afundada)
        List<IPosition> shots2 = List.of(
                new Position('A', 2),
                new Position('B', 6),
                new Position('B', 7)
        );
        game.fireShots(shots2);

        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 10s 0ms"));
    }
    @Test
    @DisplayName("summariseMove retorna '-' quando não há resultados")
    void testSummariseMoveReturnsHyphenWhenNoResults() {
        // Move com lista de resultados vazia
        Move move = new Move(1, List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        ), List.of());
        game.getAlienMoves().add(move);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"));
    }

    @Test
    @DisplayName("addFinalBoard cobre tiro em posição adjacente marcada com '-'")
    void testFinalBoardShotOnAdjacentMinus() {
        // Afundar Barge em A1 — posição A2 fica com '-'
        List<IPosition> shots1 = List.of(
                new Position('A', 1),
                new Position('J', 8),
                new Position('J', 9)
        );
        game.fireShots(shots1);

        // Disparar em A2 que está marcada como '-' (adjacente à Barge afundada)
        List<IPosition> shots2 = List.of(
                new Position('A', 2),
                new Position('J', 6),
                new Position('J', 7)
        );
        game.fireShots(shots2);

        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 10s 0ms"));
    }

    @Test
    @DisplayName("generate lida com erro de IO quando pasta não pode ser criada")
    void testGenerateHandlesIOException() throws Exception {
        // Apagar pasta data/ se existir
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            for (File f : dataDir.listFiles()) f.delete();
            dataDir.delete();
        }

        // Criar um ficheiro chamado "data" para bloquear a criação da pasta
        File dataFile = new File("data");
        dataFile.createNewFile();

        try {
            // Agora o generate vai falhar porque "data" é um ficheiro, não uma pasta
            String path = GameReportPDF.generate(game, "0m 5s 0ms");
            assertNotNull(path);
        } finally {
            // Limpar — apagar o ficheiro "data"
            dataFile.delete();
        }
    }
}