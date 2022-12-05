package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class DayOffRequest implements Serializable {
    public String userPhone;
    public String reason;
    public String status;
    public String dateOff;

    public DayOffRequest(String userPhone, String reason, String status, String dateOff) {
        this.userPhone = userPhone;
        this.reason = reason;
        this.status = status;
        this.dateOff = dateOff;
    }

    public DayOffRequest() {
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateOff() {
        return dateOff;
    }

    public void setDateOff(String dateOff) {
        this.dateOff = dateOff;
    }
}
