package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoveTest {

    private List<IPosition> mockShots;
    private List<IGame.ShotResult> mockResults;

    @BeforeEach
    void setUp() {
        // Inicializamos listas limpas antes de cada teste
        mockShots = new ArrayList<>();
        mockResults = new ArrayList<>();
    }

    @Test
    @DisplayName("Deve inicializar a classe e testar os Getters e toString")
    void testGettersAndToString() {
        Move move = new Move(1, mockShots, mockResults);

        assertEquals(1, move.getNumber(), "O número da jogada deve ser 1");
        assertEquals(mockShots, move.getShots(), "A lista de tiros deve corresponder");
        assertEquals(mockResults, move.getShotResults(), "A lista de resultados deve corresponder");

        String toStringResult = move.toString();
        assertTrue(toStringResult.contains("number=1"), "O toString deve conter o número da jogada");
        assertTrue(toStringResult.contains("shots=0"), "O toString deve conter a quantidade de tiros");
    }

    @Test
    @DisplayName("Deve processar tiros no modo Silencioso (verbose = false)")
    void testProcessEnemyFireSilent() {
        // Criar um tiro na água (Válido, Não Repetido, Sem Barco)
        IGame.ShotResult missedShot = mock(IGame.ShotResult.class);
        when(missedShot.valid()).thenReturn(true);
        when(missedShot.repeated()).thenReturn(false);
        when(missedShot.ship()).thenReturn(null);

        mockResults.add(missedShot);

        Move move = new Move(2, mockShots, mockResults);
        String jsonOutput = move.processEnemyFire(false);

        // Como verbose é false, não deve imprimir para a consola, mas tem de retornar o JSON
        assertNotNull(jsonOutput);
        assertTrue(jsonOutput.contains("\"missedShots\" : 1"), "O JSON deve registar 1 tiro falhado");
    }

    @Test
    @DisplayName("Deve processar todas as ramificações de tiros (Singular) no modo Verbose")
    void testProcessEnemyFireVerboseSingular() {
        // 1. Tiro Inválido (Fora do tabuleiro)
        IGame.ShotResult outsideShot = mock(IGame.ShotResult.class);
        when(outsideShot.valid()).thenReturn(false);

        // 2. Tiro Repetido
        IGame.ShotResult repeatedShot = mock(IGame.ShotResult.class);
        when(repeatedShot.valid()).thenReturn(true);
        when(repeatedShot.repeated()).thenReturn(true);

        // 3. Tiro na Água
        IGame.ShotResult missedShot = mock(IGame.ShotResult.class);
        when(missedShot.valid()).thenReturn(true);
        when(missedShot.repeated()).thenReturn(false);
        when(missedShot.ship()).thenReturn(null);

        // 4. Tiro que acerta num barco (mas não afunda)
        IShip frigate = mock(IShip.class);
        when(frigate.getCategory()).thenReturn("Frigate");
        IGame.ShotResult hitShot = mock(IGame.ShotResult.class);
        when(hitShot.valid()).thenReturn(true);
        when(hitShot.repeated()).thenReturn(false);
        when(hitShot.ship()).thenReturn(frigate);
        when(hitShot.sunk()).thenReturn(false);

        // 5. Tiro que afunda um barco
        IShip submarine = mock(IShip.class);
        when(submarine.getCategory()).thenReturn("Submarine");
        IGame.ShotResult sunkShot = mock(IGame.ShotResult.class);
        when(sunkShot.valid()).thenReturn(true);
        when(sunkShot.repeated()).thenReturn(false);
        when(sunkShot.ship()).thenReturn(submarine);
        when(sunkShot.sunk()).thenReturn(true);

        // Adicionar tudo à lista
        mockResults.addAll(List.of(outsideShot, repeatedShot, missedShot, hitShot, sunkShot));

        Move move = new Move(3, mockShots, mockResults);
        String jsonOutput = move.processEnemyFire(true);

        // Verificações no JSON
        assertTrue(jsonOutput.contains("\"outsideShots\" : 1"));
        assertTrue(jsonOutput.contains("\"repeatedShots\" : 1"));
        assertTrue(jsonOutput.contains("\"missedShots\" : 1"));
        assertTrue(jsonOutput.contains("\"validShots\" : 3")); // missed + hit + sunk
    }

    @Test
    @DisplayName("Deve testar a pluralidade (múltiplos tiros iguais) no modo Verbose")
    void testProcessEnemyFireVerbosePlural() {
        // Para cobrir as condições (x > 1 ? "s" : "") precisamos de adicionar 2 de cada tipo

        // 2 Tiros Inválidos
        IGame.ShotResult outside1 = mock(IGame.ShotResult.class);
        when(outside1.valid()).thenReturn(false);
        mockResults.add(outside1);
        mockResults.add(outside1); // Adiciona o mesmo mock duas vezes para simular plural

        // 2 Tiros na Água
        IGame.ShotResult missed1 = mock(IGame.ShotResult.class);
        when(missed1.valid()).thenReturn(true);
        when(missed1.repeated()).thenReturn(false);
        when(missed1.ship()).thenReturn(null);
        mockResults.add(missed1);
        mockResults.add(missed1);

        // 2 Acertos no mesmo tipo de barco
        IShip galleon = mock(IShip.class);
        when(galleon.getCategory()).thenReturn("Galleon");
        IGame.ShotResult hit1 = mock(IGame.ShotResult.class);
        when(hit1.valid()).thenReturn(true);
        when(hit1.repeated()).thenReturn(false);
        when(hit1.ship()).thenReturn(galleon);
        when(hit1.sunk()).thenReturn(false);
        mockResults.add(hit1);
        mockResults.add(hit1);

        Move move = new Move(4, mockShots, mockResults);
        String jsonOutput = move.processEnemyFire(true);

        assertTrue(jsonOutput.contains("\"outsideShots\" : 2"));
        assertTrue(jsonOutput.contains("\"missedShots\" : 2"));
        assertTrue(jsonOutput.contains("\"validShots\" : 4"));
    }
    @Test
    @DisplayName("Deve cobrir as ramificações amarelas: plurais de afundados e strings isoladas")
    void testProcessEnemyFireYellowBranches() {
        // PARTE 1: Testar plural de barcos afundados (2 submarinos)
        IShip submarine = mock(IShip.class);
        when(submarine.getCategory()).thenReturn("Submarine");

        IGame.ShotResult sunk1 = mock(IGame.ShotResult.class);
        when(sunk1.valid()).thenReturn(true);
        when(sunk1.repeated()).thenReturn(false);
        when(sunk1.ship()).thenReturn(submarine);
        when(sunk1.sunk()).thenReturn(true);

        // Adicionamos dois tiros que afundam submarinos
        mockResults.add(sunk1);
        mockResults.add(sunk1);

        Move moveSunk = new Move(5, mockShots, mockResults);
        moveSunk.processEnemyFire(true); // Isto deve limpar os amarelos do plural de barcos afundados

        // PARTE 2: Testar blocos isolados para que o "output.length() > 0" seja falso
        // Vamos criar uma nova jogada SÓ com tiros repetidos
        List<IGame.ShotResult> repeatedOnlyResults = new ArrayList<>();
        IGame.ShotResult repeated = mock(IGame.ShotResult.class);
        when(repeated.valid()).thenReturn(true);
        when(repeated.repeated()).thenReturn(true);
        repeatedOnlyResults.add(repeated);

        Move moveRepeated = new Move(6, mockShots, repeatedOnlyResults);
        moveRepeated.processEnemyFire(true); // Limpa o amarelo do " + " nos tiros repetidos

        // PARTE 3: Testar jogada SÓ com tiros fora
        List<IGame.ShotResult> outsideOnlyResults = new ArrayList<>();
        IGame.ShotResult outside = mock(IGame.ShotResult.class);
        when(outside.valid()).thenReturn(false);
        outsideOnlyResults.add(outside);

        Move moveOutside = new Move(7, mockShots, outsideOnlyResults);
        moveOutside.processEnemyFire(true); // Limpa o amarelo do " + " nos tiros fora
    }
    @Test
    @DisplayName("Deve testar uma jogada sem tiros nenhuns (lista vazia)")
    void testProcessEnemyFireEmpty() {
        // Criamos uma lista de resultados completamente vazia
        List<IGame.ShotResult> emptyResults = new ArrayList<>();

        Move emptyMove = new Move(8, mockShots, emptyResults);
        String jsonOutput = emptyMove.processEnemyFire(true);

        assertTrue(jsonOutput.contains("\"validShots\" : 0"));
    }
    @Test
    @DisplayName("Deve testar a ramificação de exatamente 1 tiro válido no modo Verbose")
    void testProcessEnemyFireExactlyOneValidShot() {
        // Para cobrir o cenário validShots == 1 e forçar o false no (validShots > 1 ? "s" : "")
        IGame.ShotResult missedShot = mock(IGame.ShotResult.class);
        when(missedShot.valid()).thenReturn(true);
        when(missedShot.repeated()).thenReturn(false);
        when(missedShot.ship()).thenReturn(null);

        List<IGame.ShotResult> oneResult = new ArrayList<>();
        oneResult.add(missedShot);

        Move move = new Move(9, mockShots, oneResult);
        move.processEnemyFire(true);
    }

    @Test
    @DisplayName("Deve testar a ramificação de tiros repetidos no plural (2+ tiros)")
    void testProcessEnemyFirePluralRepeated() {
        // Para cobrir o cenário repeatedShots > 1 (que escapou no teste plural anterior)
        IGame.ShotResult repeated = mock(IGame.ShotResult.class);
        when(repeated.valid()).thenReturn(true);
        when(repeated.repeated()).thenReturn(true);

        List<IGame.ShotResult> repResults = new ArrayList<>();
        repResults.add(repeated);
        repResults.add(repeated); // Adiciona 2 para ativar o plural

        Move move = new Move(10, mockShots, repResults);
        move.processEnemyFire(true);
    }
}