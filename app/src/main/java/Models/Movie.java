package Models;

import java.util.ArrayList;
import java.util.List;

public class Movie {

    private String id;
    private String name;
    private int duration;
    private List<String> genres= new ArrayList<>();
    private String description;

    public Movie(){
    }

    public Movie(String id, String name, int duration, List<String> genres, String description) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.genres = genres;
        this.description = description;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
