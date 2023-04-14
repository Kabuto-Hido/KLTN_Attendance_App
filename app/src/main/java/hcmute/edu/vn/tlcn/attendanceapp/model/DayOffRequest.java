package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class DayOffRequest implements Serializable {
    public String userUUID;
    public String reason;
    public String status;
    public String dateOff;

    public DayOffRequest(String userUUID, String reason, String status, String dateOff) {
        this.userUUID = userUUID;
        this.reason = reason;
        this.status = status;
        this.dateOff = dateOff;
    }

    public DayOffRequest() {
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
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
