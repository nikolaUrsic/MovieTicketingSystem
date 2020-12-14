package Models;

public class Admin implements IUser {

    private String id;
    private String username;
    private String passwordInHashFormat;
    private String email;

    public Admin() {

    }

    public Admin(String id, String username, String passwordInHashFormat, String email) {
        this.id = id;
        this.username = username;
        this.passwordInHashFormat = passwordInHashFormat;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordInHashFormat() {
        return passwordInHashFormat;
    }

    public void setPasswordInHashFormat(String passwordInHashFormat) {
        this.passwordInHashFormat = passwordInHashFormat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
