package hu.bme.aut.bestchat.Model;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private Boolean seen;
    private String imageURL;
    private String latitude;
    private String longitude;
    private String username;

    public Chat(String sender, String receiver, String message, Boolean seen, String imageURL, String latitude, String longitude, String username) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
        this.imageURL = imageURL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Chat(){}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
