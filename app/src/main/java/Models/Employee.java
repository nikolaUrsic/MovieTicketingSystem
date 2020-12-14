package Models;

import java.util.ArrayList;
import java.util.List;

public class Employee implements IUser{

    private String id;
    private String username;
    private String name;
    private String surname;
    private String passwordInHashFormat;
    private List<String> listOfCinemasIds = new ArrayList<>();
    private String email;
    private int numberOfTicketsCharged;
    private int numberOfTicketsStamped;

    public Employee(){

    }

    public Employee(String id, String username, String name, String surname, String passwordInHashFormat, List<String> listOfCinemasIds, String email, int numberOfTicketsCharged, int numberOfTicketsStamped) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.passwordInHashFormat = passwordInHashFormat;
        this.listOfCinemasIds = listOfCinemasIds;
        this.email = email;
        this.numberOfTicketsCharged = numberOfTicketsCharged;
        this.numberOfTicketsStamped = numberOfTicketsStamped;
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

    public List<String> getListOfCinemasIds() {
        return listOfCinemasIds;
    }

    public void setListOfCinemasIds(List<String> listOfCinemasIds) {
        this.listOfCinemasIds = listOfCinemasIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void addCinemaId(String cinemaId){
        listOfCinemasIds.add(cinemaId);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumberOfTicketsCharged() {
        return numberOfTicketsCharged;
    }

    public void setNumberOfTicketsCharged(int numberOfTicketsCharged) {
        this.numberOfTicketsCharged = numberOfTicketsCharged;
    }

    public int getNumberOfTicketsStamped() {
        return numberOfTicketsStamped;
    }

    public void setNumberOfTicketsStamped(int numberOfTicketsStamped) {
        this.numberOfTicketsStamped = numberOfTicketsStamped;
    }

    public void ticketCharged(){
        this.numberOfTicketsCharged++;
    }

    public void ticketStamped(){
        this.numberOfTicketsStamped++;
    }
}
