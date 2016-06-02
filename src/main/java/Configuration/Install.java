package Configuration;

/**
 * @author Robin Duda
 */
public class Install {
    public static void main(String[] args) {
        System.out.print("Setting up configuration.. ");
        TokenRefresher.refresh();
        System.out.println("Completed.");
    }
}
