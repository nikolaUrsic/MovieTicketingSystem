package Models;

import java.util.ArrayList;
import java.util.List;

public class Cinema {

    private String id;
    private String name;
    private String seatArrangement;
    private int numberOfSeats;
    private boolean posibilityOf3D;


    public Cinema(){
    }

    public Cinema(String id, String name, String seatArrangement, int numberOfSeats,  boolean posibilityOf3D) {
        this.id = id;
        this.name = name;
        this.seatArrangement = seatArrangement;
        this.numberOfSeats = numberOfSeats;
        this.posibilityOf3D = posibilityOf3D;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeatArrangement() {
        return seatArrangement;
    }

    public void setSeatArrangement(String seatArrangement) {
        this.seatArrangement = seatArrangement;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public boolean isPosibilityOf3D() {
        return posibilityOf3D;
    }

    public void setPosibilityOf3D(boolean posibilityOf3D) {
        this.posibilityOf3D = posibilityOf3D;
    }
}
