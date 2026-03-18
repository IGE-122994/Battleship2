/**
 * 
 */

package battleship;
import java.util.Scanner;
/**
 * The type Main.
 *
 * @author britoeabreu
 * @author adrianolopes
 * @author miguelgoulao
 */
public class Main {

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Escolha o idioma / Choose language (pt/en): ");
        String language = scanner.nextLine().trim();

        MessageManager.setLanguage(language);

        System.out.println(MessageManager.get("game.title"));
        Tasks.menu();
    }
}
