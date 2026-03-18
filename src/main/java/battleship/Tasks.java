package battleship;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Tasks {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String AJUDA = "ajuda";
    private static final String GERAFROTA = "gerafrota";
    private static final String LEFROTA = "lefrota";
    private static final String DESISTIR = "desisto";
    private static final String RAJADA = "rajada";
    private static final String TIROS = "tiros";
    private static final String MAPA = "mapa";
    private static final String STATUS = "estado";
    private static final String SIMULA = "simula";

    public static void menu() {
        IFleet myFleet = null;
        IGame game = null;
        menuHelp();

        System.out.print(MessageManager.get("menu.prompt"));
        Scanner in = new Scanner(System.in);
        String command = in.next();

        while (!command.equals(DESISTIR)) {
            switch (command) {
                case GERAFROTA:
                    myFleet = Fleet.createRandom();
                    game = new Game(myFleet);
                    game.printMyBoard(false, true);
                    break;
                case LEFROTA:
                    myFleet = buildFleet(in);
                    game = new Game(myFleet);
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
                        myFleet.printStatus();
                        game.printMyBoard(true, false);

                        if (game.getRemainingShips() == 0) {
                            game.over();
                            System.exit(0);
                        }
                    }
                    break;
                case SIMULA:
                    if (game != null) {
                        int contadorTiros = 0;
                        while (game.getRemainingShips() > 0) {
                            game.randomEnemyFire();
                            contadorTiros++;
                            myFleet.printStatus();
                            game.printMyBoard(true, false);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
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
        System.out.println(MessageManager.get("menu.help.footer"));
    }

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

    public static Ship readShip(Scanner in) {
        assert in != null;

        String shipKind = in.next();
        Position pos = readPosition(in);
        char c = in.next().charAt(0);
        Compass bearing = Compass.charToCompass(c);
        return Ship.buildShip(shipKind, bearing, pos);
    }

    public static Position readPosition(Scanner in) {
        assert in != null;

        int row = in.nextInt();
        int column = in.nextInt();
        return new Position(row, column);
    }

    public static IPosition readClassicPosition(@NotNull Scanner in) {
        if (!in.hasNext()) {
            throw new IllegalArgumentException(MessageManager.get("error.noValidPosition"));
        }

        String part1 = in.next();
        String part2 = null;

        if (in.hasNextInt()) {
            part2 = in.next();
        }

        String input = (part2 != null) ? part1 + part2 : part1;
        input = input.toUpperCase();

        if (input.matches("[A-Z]\\d+")) {
            char column = input.charAt(0);
            int row = Integer.parseInt(input.substring(1));
            return new Position(column, row);
        } else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
            char column = part1.charAt(0);
            int row = Integer.parseInt(part2);
            return new Position(column, row);
        } else {
            throw new IllegalArgumentException(MessageManager.get("error.invalidFormat"));
        }
    }
}