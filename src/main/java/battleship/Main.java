/**
 * 
 */

package battleship;

/**
 * The type Main.
 *
 * @author britoeabreu
 * @author adrianolopes
 * @author miguelgoulao
 */
public class Main
{
	/**
	 * Main.
	 *
	 * @param args the args
	 */

	public static void main(String[] args)
    {
		DatabaseManager.initializeDatabase();
		System.out.println("***  Battleship  ***");

		Tasks.menu();
    }
}
