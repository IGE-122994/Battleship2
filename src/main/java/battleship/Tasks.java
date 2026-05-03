package battleship;

import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The type Tasks.
 */
public class Tasks {

	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private Tasks() {
		throw new UnsupportedOperationException("Tasks is a utility class");
	}

	/**
	 * Strings to be used by the user
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String LLM = "llm";

	/**
	 * This task also tests the fighting element of a round of three shots.
	 * Displays the menu and processes user commands until the user quits.
	 */
	public static void menu() {
		MenuState state = new MenuState();
		state.myFleet = null;
		state.game = null;
		state.meusTiros = 0;

		menuHelp();

		System.out.print(MessageManager.get("menu.prompt"));
		Scanner in = new Scanner(System.in);
		String command = in.next();

		while (!command.equals(DESISTIR)) {
			handleCommand(command, in, state);
			System.out.print(MessageManager.get("menu.prompt"));
			command = in.next();
		}

		System.out.println(MessageManager.get("menu.goodbye"));
	}

	/**
	 * Handles a single menu command by dispatching to the appropriate handler.
	 *
	 * @param command the command string entered by the user
	 * @param in      the scanner to read additional input from
	 * @param state   the current menu state
	 */
	private static void handleCommand(String command, Scanner in, MenuState state) {
		switch (command) {
			case GERAFROTA:
				handleGeraFrota(state);
				break;
			case LEFROTA:
				handleLeFrota(in, state);
				break;
			case STATUS:
				handleStatus(state);
				break;
			case MAPA:
				handleMapa(state);
				break;
			case RAJADA:
				handleRajada(in, state);
				break;
			case SIMULA:
				handleSimula(state);
				break;
			case TIROS:
				handleTiros(state);
				break;
			case LLM:
				handleLlm(state);
				break;
			case AJUDA:
				menuHelp();
				break;
			default:
				System.out.println(MessageManager.get("menu.invalidCommand"));
		}
	}

	/**
	 * Handles the gerafrota command by generating a random fleet.
	 *
	 * @param state the current menu state
	 */
	private static void handleGeraFrota(MenuState state) {
		state.myFleet = Fleet.createRandom();
		state.game = new Game(state.myFleet);
		state.meusTiros = 0;
		state.game.printMyBoard(false, true);
	}

	/**
	 * Handles the lefrota command by reading a fleet from user input.
	 *
	 * @param in    the scanner to read from
	 * @param state the current menu state
	 */
	private static void handleLeFrota(Scanner in, MenuState state) {
		state.myFleet = buildFleet(in);
		state.game = new Game(state.myFleet);
		state.meusTiros = 0;
		state.game.printMyBoard(false, true);
	}

	/**
	 * Handles the estado command by printing the fleet status.
	 *
	 * @param state the current menu state
	 */
	private static void handleStatus(MenuState state) {
		if (state.myFleet != null)
			state.myFleet.printStatus();
	}

	/**
	 * Handles the mapa command by printing the game board.
	 *
	 * @param state the current menu state
	 */
	private static void handleMapa(MenuState state) {
		if (state.myFleet != null)
			state.game.printMyBoard(false, true);
	}

	/**
	 * Handles the rajada command by reading and processing enemy fire.
	 *
	 * @param in    the scanner to read from
	 * @param state the current menu state
	 */
	private static void handleRajada(Scanner in, MenuState state) {
		if (state.game != null) {
			state.game.readEnemyFire(in);
			state.meusTiros += 3;
			state.myFleet.printStatus();
			state.game.printMyBoard(true, false);

			if (state.game.getRemainingShips() == 0) {
				state.game.over();
				DatabaseManager.saveGameResult("Jogador", state.meusTiros);
				System.exit(0);
			}
		}
	}

	/**
	 * Handles the simula command by simulating a full game with random enemy fire.
	 *
	 * @param state the current menu state
	 */
	private static void handleSimula(MenuState state) {
		if (state.game != null) {
			int contadorTiros = 0;
			while (state.game.getRemainingShips() > 0) {
				state.game.randomEnemyFire();
				contadorTiros++;
				state.myFleet.printStatus();
				state.game.printMyBoard(true, false);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			if (state.game.getRemainingShips() == 0) {
				state.game.over();
				DatabaseManager.saveGameResult("Computador (Simulação)", contadorTiros);
				System.exit(0);
			}
		}
	}

	/**
	 * Handles the tiros command by printing the board with shots.
	 *
	 * @param state the current menu state
	 */
	private static void handleTiros(MenuState state) {
		if (state.game != null)
			state.game.printMyBoard(true, true);
	}

	/**
	 * Handles the llm command by letting the LLM play automatically.
	 *
	 * @param state the current menu state
	 */
	private static void handleLlm(MenuState state) {
		if (state.game != null) {
			HuggingFaceClient llmClient = new HuggingFaceClient();
			try {
				llmClient.initialize();
				System.out.println("A jogar contra o LLM...");
				int failCount = 0;
				while (state.game.getRemainingShips() > 0 && failCount < 5) {
					String history = HuggingFaceClient.buildGameHistory(state.game.getAlienMoves());
					try {
						List<IPosition> shots = llmClient.getNextMove(history);
						state.game.fireShots(shots);
						state.myFleet.printStatus();
						state.game.printMyBoard(true, false);
						failCount = 0;
						if (state.game.getRemainingShips() == 0) {
							state.game.over();
							System.exit(0);
						}
					} catch (Exception e) {
						failCount++;
						System.err.println("Erro na jogada (" + failCount + "/5): " + e.getMessage());
					}
				}
				if (state.game.getRemainingShips() > 0) {
					System.out.println("LLM desistiu após erros consecutivos.");
				}
			} catch (Exception e) {
				System.err.println("Erro na comunicação com o LLM: " + e.getMessage());
			}
		}
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println(MessageManager.get("menu.help.title"));
		System.out.println(MessageManager.get("menu.help.intro"));
		System.out.println(MessageManager.get("menu.help.gerafrota"));
		System.out.println(MessageManager.get("menu.help.lefrota"));
		System.out.println(MessageManager.get("menu.help.estado"));
		System.out.println(MessageManager.get("menu.help.mapa"));
		System.out.println(MessageManager.get("menu.help.rajada"));
		System.out.println(MessageManager.get("menu.help.simula"));
		System.out.println(MessageManager.get("menu.help.tiros"));
		System.out.println(MessageManager.get("menu.help.desisto"));
		System.out.println(MessageManager.get("menu.help.llm"));
		System.out.println(MessageManager.get("menu.help.footer"));
	}

	/**
	 * This operation allows the build up of a fleet, given user data.
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {
		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0;
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it.
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {
		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map.
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {
		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map in classic notation.
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		validateScannerHasInput(in);

		String part1 = in.next();
		String part2 = readOptionalSecondToken(in);

		String input = combineInputTokens(part1, part2);
		input = normalizeInput(input);

		return parsePositionFromInput(input, part1, part2);
	}

	/**
	 * Validates that the scanner has input available.
	 *
	 * @param in the scanner to validate
	 * @throws IllegalArgumentException if no input is available
	 */
	private static void validateScannerHasInput(@NotNull Scanner in) {
		if (!in.hasNext()) {
			throw new IllegalArgumentException(MessageManager.get("error.noValidPosition"));
		}
	}

	/**
	 * Reads the optional second token if it's an integer.
	 *
	 * @param in the scanner to read from
	 * @return the second token as a string, or null if not available
	 */
	private static String readOptionalSecondToken(@NotNull Scanner in) {
		if (in.hasNextInt()) {
			return in.next();
		}
		return null;
	}

	/**
	 * Combines the input tokens into a single string.
	 *
	 * @param part1 the first token
	 * @param part2 the second token (may be null)
	 * @return the combined input string
	 */
	private static String combineInputTokens(String part1, String part2) {
		return (part2 != null) ? part1 + part2 : part1;
	}

	/**
	 * Normalizes the input to uppercase for consistent processing.
	 *
	 * @param input the input string to normalize
	 * @return the normalized input string
	 */
	private static String normalizeInput(String input) {
		return input.toUpperCase();
	}

	/**
	 * Parses the position from the input tokens, trying different formats.
	 *
	 * @param input the combined and normalized input
	 * @param part1 the first token
	 * @param part2 the second token (may be null)
	 * @return the parsed position
	 * @throws IllegalArgumentException if the input format is invalid
	 */
	private static IPosition parsePositionFromInput(String input, String part1, String part2) {
		if (isCompactFormat(input)) {
			return parseCompactFormat(input);
		}
		if (isSeparatedFormat(part1, part2)) {
			return parseSeparatedFormat(part1, part2);
		}
		throw new IllegalArgumentException(MessageManager.get("error.invalidFormat"));
	}

	/**
	 * Checks if the input is in compact format (e.g., "A1").
	 *
	 * @param input the input string to check
	 * @return true if the input matches compact format
	 */
	private static boolean isCompactFormat(String input) {
		return input.matches("[A-Z]\\d+");
	}

	/**
	 * Checks if the input is in separated format (e.g., "A" "1").
	 *
	 * @param part1 the first token
	 * @param part2 the second token
	 * @return true if the tokens match separated format
	 */
	private static boolean isSeparatedFormat(String part1, String part2) {
		return part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+");
	}

	/**
	 * Parses a position from compact format (e.g., "A1").
	 *
	 * @param input the compact format input
	 * @return the parsed position
	 */
	private static IPosition parseCompactFormat(String input) {
		char column = input.charAt(0);
		int row = Integer.parseInt(input.substring(1));
		return new Position(column, row);
	}

	/**
	 * Parses a position from separated format (e.g., "A" "1").
	 *
	 * @param part1 the column token
	 * @param part2 the row token
	 * @return the parsed position
	 */
	private static IPosition parseSeparatedFormat(String part1, String part2) {
		char column = part1.charAt(0);
		int row = Integer.parseInt(part2);
		return new Position(column, row);
	}

	/**
	 * Inner class to hold the current state of the menu session.
	 */
	private static class MenuState {
		/** The player's fleet. */
		IFleet myFleet;
		/** The current game instance. */
		IGame game;
		/** The total number of shots fired by the player. */
		int meusTiros;
	}
}