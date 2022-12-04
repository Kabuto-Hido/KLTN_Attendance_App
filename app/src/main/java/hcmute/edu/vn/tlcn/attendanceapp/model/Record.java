package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class Record implements Serializable {
    public String username;
    public String day;
    public String time;
    public String month;
    public String year;
    public String status; //on time - late - none
    public String type; //checkIn - checkOut

    public Record(String username, String day, String time, String month, String year, String status, String type) {
        this.username = username;
        this.day = day;
        this.time = time;
        this.month = month;
        this.year = year;
        this.status = status;
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Record() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
