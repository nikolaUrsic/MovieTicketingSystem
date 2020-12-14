package Models;

public interface IUser {
    void setPasswordInHashFormat(String passwordInHashFormat);
    String getId();
    String getPasswordInHashFormat();
}
