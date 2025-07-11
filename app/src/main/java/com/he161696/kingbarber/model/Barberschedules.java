package com.he161696.kingbarber.model;

import java.util.Date;
import java.util.Timer;

public class Barberschedules {
    private int ScheduleId;
    private int BarberId;
    private Date WorkDate;
    private String StartTime;
    private String EndTime;

    public Barberschedules() {
    }

    public Barberschedules(int scheduleId, int barberId, Date workDate, String startTime, String endTime) {
        ScheduleId = scheduleId;
        BarberId = barberId;
        WorkDate = workDate;
        StartTime = startTime;
        EndTime = endTime;
    }

    public int getScheduleId() {
        return ScheduleId;
    }

    public void setScheduleId(int scheduleId) {
        ScheduleId = scheduleId;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public Date getWorkDate() {
        return WorkDate;
    }

    public void setWorkDate(Date workDate) {
        WorkDate = workDate;
    }

    public int getBarberId() {
        return BarberId;
    }

    public void setBarberId(int barberId) {
        BarberId = barberId;
    }

}
