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
	private Tasks() {
		throw new UnsupportedOperationException("Tasks is a utility class");
	}

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

    private static void handleTiros(MenuState state) {
        if (state.game != null)
            state.game.printMyBoard(true, true);
    }

    private static void handleSimula(MenuState state) {
        if (state.game != null) {
            int contadorTiros = 0;
            while (state.game.getRemainingShips() > 0){
                state.game.randomEnemyFire();
                contadorTiros++;
                state.myFleet.printStatus();
                state.game.printMyBoard(true, false);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Best practice: restore interrupt status
                }
            }

            if (state.game.getRemainingShips() == 0) {
                state.game.over();
                DatabaseManager.saveGameResult("Computador (Simulação)", contadorTiros);
                System.exit(0);
            }
        }
    }

    private static void handleRajada(Scanner in, MenuState state) {
        if (state.game != null) {
            state.game.readEnemyFire(in);
            state.meusTiros +=3;
            state.myFleet.printStatus();
            state.game.printMyBoard(true, false);

            if (state.game.getRemainingShips() == 0) {
                state.game.over();
                DatabaseManager.saveGameResult("Jogador", state.meusTiros);
                System.exit(0);
            }
        }
    }

    private static void handleMapa(MenuState state) {
        if (state.myFleet != null)
            state.game.printMyBoard(false, true);
    }

    private static void handleStatus(MenuState state) {
        if (state.myFleet != null)
            state.myFleet.printStatus();
    }

    private static void handleLeFrota(Scanner in, MenuState state) {
        state.myFleet = buildFleet(in);
        state.game = new Game(state.myFleet);
        state.meusTiros= 0;
        state.game.printMyBoard(false, true);
    }

    private static void handleGeraFrota(MenuState state) {
        state.myFleet = Fleet.createRandom();
        state.game = new Game(state.myFleet);
        state.meusTiros= 0;
        state.game.printMyBoard(false, true);
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
		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException(MessageManager.get("error.noValidPosition"));
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponível
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiúsculas e minúsculas
		input = input.toUpperCase();

		// Verificar os dois formatos possíveis: compactos e com espaço
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(input.substring(1)); // Extrair a linha
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(part2); // Extrair a linha
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException(MessageManager.get("error.invalidFormat"));
		}
	}

    private static class MenuState {
        IFleet myFleet;
        IGame game;
        int meusTiros;
    }


}
