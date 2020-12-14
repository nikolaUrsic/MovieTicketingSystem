package Models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MovieDisplay {

    private String id;
    private String idMovie;
    private String movieName;
    private String idCinema;
    private String cinemaName;
    private Date dateTimeOfBeginning;
    private boolean is3D;
    private List<String> listOfTicketsIds= new ArrayList<>();
    private boolean deleted;

    public MovieDisplay(){
    }

    public MovieDisplay(String id, String idMovie, String movieName, String idCinema, String cinemaName, Date dateTimeOfBeginning, boolean is3D, List<String> listOfTicketsIds, boolean deleted) {
        this.id = id;
        this.idMovie = idMovie;
        this.movieName = movieName;
        this.idCinema = idCinema;
        this.cinemaName = cinemaName;
        this.dateTimeOfBeginning = dateTimeOfBeginning;
        this.is3D = is3D;
        this.listOfTicketsIds = listOfTicketsIds;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdMovie() {
        return idMovie;
    }

    public void setIdMovie(String idMovie) {
        this.idMovie = idMovie;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getIdCinema() {
        return idCinema;
    }

    public void setIdCinema(String idCinema) {
        this.idCinema = idCinema;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public Date getDateTimeOfBeginning() {
        return dateTimeOfBeginning;
    }

    public void setDateTimeOfBeginning(Date dateTimeOfBeginning) {
        this.dateTimeOfBeginning = dateTimeOfBeginning;
    }

    public boolean isIs3D() {
        return is3D;
    }

    public void setIs3D(boolean is3D) {
        this.is3D = is3D;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
