package Models;

public class Ticket {

    private String id;
    private String idMovieDisplay;
    private int posotion;
    private float price;
    private String status;

    public Ticket() {
    }

    public Ticket(String id, String idMovieDisplay, int posotion, float price, String status) {
        this.id = id;
        this.idMovieDisplay = idMovieDisplay;
        this.posotion = posotion;
        this.price = price;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdMovieDisplay() {
        return idMovieDisplay;
    }

    public void setIdMovieDisplay(String idMovieDisplay) {
        this.idMovieDisplay = idMovieDisplay;
    }

    public int getPosotion() {
        return posotion;
    }

    public void setPosotion(int posotion) {
        this.posotion = posotion;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
