package Authentication.Model;

/**
 * @author Robin Duda
 *
 * Holds account data for an user used to communicate with the view.
 */
public class Account {
    private String username;
    private String password;
    private String email;

    public Account() {
    }

    public Account(AccountMapping account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
    }

    public String getUsername() {
        return username;
    }

    public Account setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }
}
