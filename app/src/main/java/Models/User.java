package Models;

import java.util.ArrayList;
import java.util.List;

public class User implements IUser{

    private String id;
    private String username;
    private String passwordInHashFormat;
    private String email;

    private List<String> listOfTicketsIds = new ArrayList<>();

    public User() {
    }

    public User(String id, String username, String passwordInHashFormat,String email) {
        this.id = id;
        this.username = username;
        this.passwordInHashFormat = passwordInHashFormat;
        this.email = email;
    }

    public User(String id, String username, String passwordInHashFormat, String email, List<String> listOfTicketsIds, List<String> listOfEventsIds) {
        this.id = id;
        this.username = username;
        this.passwordInHashFormat = passwordInHashFormat;
        this.email = email;
        this.listOfTicketsIds = listOfTicketsIds;
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

    public List<String> getListOfTicketsIds() {
        return listOfTicketsIds;
    }

    public void setListOfTicketsIds(List<String> listOfTicketsIds) {
        this.listOfTicketsIds = listOfTicketsIds;
    }

    public void addTicket(String ticket){
        this.listOfTicketsIds.add(ticket);
    }

    public void removeTicket(String ticket){
        if(this.listOfTicketsIds.contains(ticket)){
            this.listOfTicketsIds.remove(ticket);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
