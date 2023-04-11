package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class Record implements Serializable {
    public String userPhone;
    public String day;
    public String time;
    public String status; //on time - late - none
    public String type; //checkIn - checkOut
    private LocationRecord location;

    public Record(String userPhone, String day, String time, String status, String type, LocationRecord location) {
        this.userPhone = userPhone;
        this.day = day;
        this.time = time;
        this.status = status;
        this.type = type;
        this.location = location;
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

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
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

    public LocationRecord getLocation() {
        return location;
    }

    public void setLocation(LocationRecord location) {
        this.location = location;
    }

}
