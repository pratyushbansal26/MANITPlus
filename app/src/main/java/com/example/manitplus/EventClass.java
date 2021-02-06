package com.example.manitplus;


public class EventClass {
    private String purl;
    private String title,description,date,societyname,url;

    public EventClass()
    {
    }

    public EventClass(String purl, String title, String description, String date, String societyname, String URL){
        this.purl = purl;
        this.date=date;
        this.description=description;
        this.title = title;
        this.url=URL;
        this.societyname = societyname;
    }

    public String getPurl() {
        return purl;
    }

    public void setPurl(String purl) {
        this.purl = purl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSocietyname() {
        return societyname;
    }

    public void setSocietyname(String societyname) {
        this.societyname = societyname;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String URL) {
        this.url= URL;
    }
}