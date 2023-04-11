package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class Config implements Serializable {
    private String startCheckIn;
    private String endCheckIn;
    private String period ;

    public Config(String startCheckIn, String endCheckIn, String period) {
        this.startCheckIn = startCheckIn;
        this.endCheckIn = endCheckIn;
        this.period = period;
    }

    public Config() {
    }

    public String getStartCheckIn() {
        return startCheckIn;
    }

    public void setStartCheckIn(String startCheckIn) {
        this.startCheckIn = startCheckIn;
    }

    public String getEndCheckIn() {
        return endCheckIn;
    }

    public void setEndCheckIn(String endCheckIn) {
        this.endCheckIn = endCheckIn;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
