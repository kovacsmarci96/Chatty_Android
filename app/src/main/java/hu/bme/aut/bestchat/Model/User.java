package hu.bme.aut.bestchat.Model;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private String id;
    private String username;
    private String imageURL;
    private String email;
    private String status;
    private String search;
    private String lasttimeonline;
    private String fullname;
    private String number;
    private String hometown;
    private String borntown;
    private String borntime;
    private String whocanseemyprofile;


    public User(String id, String username, String imageURL, String email, String status, String search, String lasttimeonline, String fullname, String phonenumber, String hometown, String birthtown, String birthdate, String whocanseemyprofile) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.email = email;
        this.status = status;
        this.search = search;
        this.lasttimeonline = lasttimeonline;
        this.fullname = fullname;
        this.number = phonenumber;
        this.hometown = hometown;
        this.borntown = birthtown;
        this.borntime = birthdate;
        this.whocanseemyprofile = whocanseemyprofile;
    }


    public String getWhocanseemyprofile() {
        return whocanseemyprofile;
    }

    public void setWhocanseemyprofile(String whocanseemyprofile) {
        this.whocanseemyprofile = whocanseemyprofile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastTimeOnline() {
        return lasttimeonline;
    }

    public void String(String lastTimeOnline) {
        this.lasttimeonline = lastTimeOnline;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public User(){}

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNumber() {
        return number;
    }

    public void setPhonenumber(String phonenumber) {
        this.number = phonenumber;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getBornTown() {
        return borntown;
    }

    public void setBirthtown(String birthtown) {
        this.borntown = birthtown;
    }

    public String getBornTime() {
        return borntime;
    }

    public void setBirthdate(String birthdate) {
        this.borntime = birthdate;
    }
}


