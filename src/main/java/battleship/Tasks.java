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
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		IFleet myFleet = null;
		IGame game = null;
		int meusTiros = 0;
		menuHelp();


		System.out.print(MessageManager.get("menu.prompt"));
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {

			switch (command) {
				case GERAFROTA:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					meusTiros= 0;
					game.printMyBoard(false, true);
					break;
				case LEFROTA:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					meusTiros= 0;
					game.printMyBoard(false, true);
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					if (game != null) {
						game.readEnemyFire(in);
						meusTiros +=3;
						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							game.over();
							DatabaseManager.saveGameResult("Jogador", meusTiros);
							System.exit(0);
						}
					}
					break;
				case SIMULA:
					if (game != null) {
						int contadorTiros = 0;
						while (game.getRemainingShips() > 0){
							game.randomEnemyFire();
							contadorTiros++;
							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt(); // Best practice: restore interrupt status
							}
						}

						if (game.getRemainingShips() == 0) {
							game.over();
							DatabaseManager.saveGameResult("Computador (Simulação)", contadorTiros);
							System.exit(0);
						}
					}
					break;
				case TIROS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case LLM:
					if (game != null) {
						HuggingFaceClient llmClient = new HuggingFaceClient();
						try {
							llmClient.initialize();
							System.out.println("A jogar contra o LLM...");
							int failCount = 0;
							while (game.getRemainingShips() > 0 && failCount < 5) {
								String history = HuggingFaceClient.buildGameHistory(game.getAlienMoves());
								try {
									List<IPosition> shots = llmClient.getNextMove(history);
									game.fireShots(shots);
									myFleet.printStatus();
									game.printMyBoard(true, false);
									failCount = 0;
									if (game.getRemainingShips() == 0) {
										game.over();
										System.exit(0);
									}
								} catch (Exception e) {
									failCount++;
									System.err.println("Erro na jogada (" + failCount + "/5): " + e.getMessage());
								}
							}
							if (game.getRemainingShips() > 0) {
								System.out.println("LLM desistiu após erros consecutivos.");
							}
						} catch (Exception e) {
							System.err.println("Erro na comunicação com o LLM: " + e.getMessage());
						}
					}
					break;
				case AJUDA:
					menuHelp();
					break;
				default:
					System.out.println(MessageManager.get("menu.invalidCommand"));
			}
			System.out.print(MessageManager.get("menu.prompt"));
			command = in.next();
		}
		System.out.println(MessageManager.get("menu.goodbye"));
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
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
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
	 * This operation reads data about a ship, build it and returns it
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
	 * This operation allows reading a position in the map
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
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		validateScannerHasInput(in);
		
		String part1 = in.next(); // Primeiro token
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
			return in.next(); // Segundo token, se disponível
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
		char column = input.charAt(0); // Extrair a coluna
		int row = Integer.parseInt(input.substring(1)); // Extrair a linha
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
		char column = part1.charAt(0); // Extrair a coluna
		int row = Integer.parseInt(part2); // Extrair a linha
		return new Position(column, row);
	}
}
