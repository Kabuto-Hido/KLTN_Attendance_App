package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class Statistic implements Serializable {
    public int onTime;
    public int late;
    public int absentWithPer;
    public int absentWithoutPer;
    public String statisticMonth;
    public String statisticYear;
    public String userUUID;
    public String hourWorked;

    public Statistic(int onTime, int late, int absentWithPer, int absentWithoutPer,
                     String statisticMonth, String statisticYear, String userUUID, String hourWorked) {
        this.onTime = onTime;
        this.late = late;
        this.absentWithPer = absentWithPer;
        this.absentWithoutPer = absentWithoutPer;
        this.statisticMonth = statisticMonth;
        this.statisticYear = statisticYear;
        this.userUUID = userUUID;
        this.hourWorked = hourWorked;
    }

    public Statistic() {
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getStatisticMonth() {
        return statisticMonth;
    }

    public void setStatisticMonth(String statisticMonth) {
        this.statisticMonth = statisticMonth;
    }

    public String getStatisticYear() {
        return statisticYear;
    }

    public void setStatisticYear(String statisticYear) {
        this.statisticYear = statisticYear;
    }

    public int getOnTime() {
        return onTime;
    }

    public void setOnTime(int onTime) {
        this.onTime = onTime;
    }

    public int getLate() {
        return late;
    }

    public void setLate(int late) {
        this.late = late;
    }

    public int getAbsentWithPer() {
        return absentWithPer;
    }

    public void setAbsentWithPer(int absentWithPer) {
        this.absentWithPer = absentWithPer;
    }

    public int getAbsentWithoutPer() {
        return absentWithoutPer;
    }

    public void setAbsentWithoutPer(int absentWithoutPer) {
        this.absentWithoutPer = absentWithoutPer;
    }

    public String getHourWorked() {
        return hourWorked;
    }

    public void setHourWorked(String hourWorked) {
        this.hourWorked = hourWorked;
    }
}
