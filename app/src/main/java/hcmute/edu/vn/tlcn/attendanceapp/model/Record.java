package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class Record implements Serializable {
    public String username;
    public String day;
    public String time;
    public String status;

    public Record(String username, String day, String time, String status) {
        this.username = username;
        this.day = day;
        this.time = time;
        this.status = status;
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
