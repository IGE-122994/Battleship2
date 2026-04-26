package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Test class for Fleet.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - addShip: 3
 * - getShips: 1
 * - getShipsLike: 2
 * - getFloatingShips: 2
 * - shipAt: 2
 * - isInsideBoard: 3
 * - colisionRisk: 2
 */
	public class FleetTest {

		private Fleet fleet;

		@BeforeEach
		void setUp() {
			fleet = new Fleet();
		}

		@AfterEach
		void tearDown() {
			fleet = null;
		}

		/**
		 * Test for the Fleet constructor.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testConstructor() {
			assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
		}

		/**
		 * Test for the addShip method (all conditions true).
		 * Cyclomatic Complexity: 3
		 */
		@Test
		void testAddShip1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
		}

		/**
		 * Test for the addShip method (fleet size limit reached).
		 */
		@Test
		void testAddShip2() {
			for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
				fleet.addShip(new Barge(Compass.NORTH, new Position(i, 0)));
			}
			IShip anotherShip = new Barge(Compass.NORTH, new Position(10, 10));
			assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
		}

		/**
		 * Test for the addShip method (ship outside the board).
		 */
		@Test
		void testAddShip3() {
			IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
			assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
		}

		/**
		 * Test for the addShip method (collision risk).
		 */
		@Test
		void testAddShip4() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);
			assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
		}

		/**
		 * Test for the getShips method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testGetShips() {
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
			assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
		}

		/**
		 * Test for the getShipsLike method (ships of specific category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetShipsLike() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(2, 1));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> barges = fleet.getShipsLike("Barca");
			assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
			assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
		}

		/**
		 * Test for the getFloatingShips method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetFloatingShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> floatingShips = fleet.getFloatingShips();
			assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

			ship1.getPositions().get(0).shoot();  // Sink ship1
			floatingShips = fleet.getFloatingShips();
			assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
			assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
		}

		/**
		 * Test for the shipAt method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testShipAt() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
			assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
		}

		/**
		 * Test for private method isInsideBoard.
		 * Cyclomatic Complexity: 3
		 */
		@Test
		void testIsInsideBoard() throws Exception {
			// Use reflection to access private methods
			var method = Fleet.class.getDeclaredMethod("isInsideBoard", IShip.class);
			method.setAccessible(true);

			IShip insideShip = new Barge(Compass.NORTH, new Position(1, 1));
			IShip outsideShip = new Barge(Compass.NORTH, new Position(99, 99));

			assertTrue((Boolean) method.invoke(fleet, insideShip), "Error: Ship inside the board should return true.");
			assertFalse((Boolean) method.invoke(fleet, outsideShip), "Error: Ship outside the board should return false.");
		}

		/**
		 * Test for private method colisionRisk.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testColisionRisk() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);

			assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
			assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
					"Error: Ships at non-overlapping positions should not have a collision risk.");
		}

		/**
		 * Test for the printStatus method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testPrintStatus() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
		}
	@Test
	@DisplayName("Testar falha no limite ESQUERDO do tabuleiro")
	void testAddShipOutsideLeft() {
		IShip badShip = org.mockito.Mockito.mock(IShip.class);
		org.mockito.Mockito.when(badShip.getLeftMostPos()).thenReturn(-1);

		assertFalse(fleet.addShip(badShip), "Erro: Não deve adicionar se sair pela margem esquerda.");
	}

	@Test
	@DisplayName("Testar falha no limite SUPERIOR do tabuleiro")
	void testAddShipOutsideTop() {
		IShip badShip = org.mockito.Mockito.mock(IShip.class);
		org.mockito.Mockito.when(badShip.getLeftMostPos()).thenReturn(1);
		org.mockito.Mockito.when(badShip.getRightMostPos()).thenReturn(2);
		org.mockito.Mockito.when(badShip.getTopMostPos()).thenReturn(-1);

		assertFalse(fleet.addShip(badShip), "Erro: Não deve adicionar se sair pela margem superior.");
	}

	@Test
	@DisplayName("Testar o método getSunkShips")
	void testGetSunkShips() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship1);

		// Afundar o barco (a Barca só tem 1 posição)
		ship1.getPositions().get(0).shoot();

		List<IShip> sunk = fleet.getSunkShips();
		assertEquals(1, sunk.size(), "Erro: Deve retornar 1 barco afundado.");
		assertEquals(ship1, sunk.get(0), "Erro: O barco afundado deve coincidir.");
	}

	@Test
	@DisplayName("Testar restantes métodos de Impressão")
	void testOtherPrints() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertDoesNotThrow(() -> {
			fleet.printAllShips();
			fleet.printFloatingShips();
			fleet.printShipsByCategory("Barca");
		}, "Erro: Os métodos de print não devem lançar exceções.");
	}

	@Test
	@DisplayName("Testar a criação aleatória da frota")
	void testCreateRandom() {
		assertDoesNotThrow(() -> {
			IFleet randomFleet = Fleet.createRandom();
			assertNotNull(randomFleet, "Erro: A frota gerada aleatoriamente não deve ser nula.");
			// Pode ter menos de 10 dependendo das colisões geradas aleatoriamente,
			// mas a instância tem de ser criada com sucesso.
		});
	}
	@Test
	@DisplayName("Deve forçar os Asserts enviando valores nulos para todos os métodos")
	void testNullAssertions() {
		// Quando enviamos null, o Java lança um AssertionError (se os asserts estiverem ligados)
		// ou um NullPointerException (se estiverem desligados). O Throwable abrange ambos.
		assertThrows(Throwable.class, () -> fleet.addShip(null));
		assertThrows(Throwable.class, () -> fleet.getShipsLike(null));
		assertThrows(Throwable.class, () -> fleet.shipAt(null));
		assertThrows(Throwable.class, () -> fleet.printShips(null));
		assertThrows(Throwable.class, () -> fleet.printShipsByCategory(null));
	}

	@Test
	@DisplayName("Deve testar risco de colisão que só acontece a meio da lista (2º barco)")
	void testColisionRiskOnSecondShip() throws Exception {
		// Injetamos dois barcos válidos e seguros na frota
		IShip safeShip1 = org.mockito.Mockito.mock(IShip.class);
		// AQUI ESTÁ A CORREÇÃO: any(IShip.class) diz ao Java exatamente qual método usar
		org.mockito.Mockito.when(safeShip1.tooCloseTo(org.mockito.Mockito.any(IShip.class))).thenReturn(false);

		IShip safeShip2 = org.mockito.Mockito.mock(IShip.class);
		// AQUI TAMBÉM: any(IShip.class)
		org.mockito.Mockito.when(safeShip2.tooCloseTo(org.mockito.Mockito.any(IShip.class))).thenReturn(true);

		// Precisamos de aceder à lista privada para ignorar as validações do addShip neste teste específico
		fleet.getShips().add(safeShip1);
		fleet.getShips().add(safeShip2);

		// Criamos um barco novo para tentar adicionar
		IShip newShip = org.mockito.Mockito.mock(IShip.class);
		org.mockito.Mockito.when(newShip.getLeftMostPos()).thenReturn(0);
		org.mockito.Mockito.when(newShip.getRightMostPos()).thenReturn(2);
		org.mockito.Mockito.when(newShip.getTopMostPos()).thenReturn(0);
		org.mockito.Mockito.when(newShip.getBottomMostPos()).thenReturn(2);

		// Tentamos adicionar. Deve falhar porque bate no safeShip2
		assertFalse(fleet.addShip(newShip), "Erro: Deve detetar colisão com o segundo barco da lista.");
	}
	@Test
	@DisplayName("Forçar a cobertura total da Linha 119 (getShipsLike)")
	void testGetShipsLikeFullCoverage() {
		Fleet localFleet = new Fleet();
		// Adicionamos um de cada para forçar o IF a dar True e False na mesma procura
		localFleet.addShip(new Barge(Compass.NORTH, new Position(1, 1))); // Categoria "Barca"
		localFleet.addShip(new Caravel(Compass.NORTH, new Position(5, 5))); // Categoria "Caravela"

		List<IShip> result = localFleet.getShipsLike("Barca");

		// O IF avaliou True para a Barca e False para a Caravela!
		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Forçar a cobertura total da Linha 93 (O mega IF do addShip)")
	void testAddShipMegaIfCoverage() {
		Fleet localFleet = new Fleet();

		// 1. Tudo True (Adiciona com sucesso)
		IShip validShip = new Barge(Compass.NORTH, new Position(0, 0));
		assertTrue(localFleet.addShip(validShip), "Deveria adicionar o primeiro barco");

		// 2. Fora do tabuleiro (isInsideBoard = false) -> Testa o segundo ramo do IF
		IShip outsideShip = new Barge(Compass.NORTH, new Position(99, 99));
		assertFalse(localFleet.addShip(outsideShip), "Não deveria adicionar barco fora do tabuleiro");

		// 3. Risco de colisão (colisionRisk = true) -> Testa o terceiro ramo do IF
		IShip collisionShip = new Barge(Compass.NORTH, new Position(0, 0));
		assertFalse(localFleet.addShip(collisionShip), "Não deveria adicionar barco em colisão");

		// 4. Forçar Frota Cheia (ships.size() > FLEET_SIZE) -> Testa o primeiro ramo do IF
		// Vamos encher a frota dinamicamente usando o valor real do FLEET_SIZE
		// Adicionamos barcos em posições diferentes para evitar colisões
		for (int i = localFleet.getShips().size(); i <= Fleet.FLEET_SIZE + 1; i++) {
			int x = (i % 10);
			int y = (i / 10) % 10;
			// Usamos barcas de 1 posição para caberem bem
			localFleet.getShips().add(new Barge(Compass.NORTH, new Position(x, y)));
		}

		// Agora a frota está GARANTIDAMENTE cheia (ultrapassou o FLEET_SIZE)
		IShip extraShip = new Barge(Compass.NORTH, new Position(9, 9));
		boolean result = localFleet.addShip(extraShip);

		assertFalse(result, "Erro: A frota já tem " + localFleet.getShips().size() +
				" barcos, não devia aceitar mais (FLEET_SIZE=" + Fleet.FLEET_SIZE + ")");
	}
	}