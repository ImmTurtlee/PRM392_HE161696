package com.he161696.kingbarber.model;

import java.util.Date;
import java.util.List;

public class Appointments {
    private int AppointmentId;
    private int ClientId;
    private int BarberId;
    private int ServiceId;
    private Date AppointmentDate;
    private String StartTime;
    private int Rating;
    private String Status;
    private List<Service> services;

    public List<Service> getServices() { return services; }
    public void setServices(List<Service> services) { this.services = services; }

    public Appointments() {
    }

    public Appointments(int appointmentId, String status, int rating, String startTime, Date appointmentDate, int serviceId, int clientId, int barberId) {
        AppointmentId = appointmentId;
        Status = status;
        Rating = rating;
        StartTime = startTime;
        AppointmentDate = appointmentDate;
        ServiceId = serviceId;
        ClientId = clientId;
        BarberId = barberId;
    }

    public int getRating() {
        return Rating;
    }

    public void setRating(int rating) {
        Rating = rating;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }

    public Date getAppointmentDate() {
        return AppointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        AppointmentDate = appointmentDate;
    }

    public int getBarberId() {
        return BarberId;
    }

    public void setBarberId(int barberId) {
        BarberId = barberId;
    }

    public int getClientId() {
        return ClientId;
    }

    public void setClientId(int clientId) {
        ClientId = clientId;
    }

    public int getAppointmentId() {
        return AppointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        AppointmentId = appointmentId;
    }
}
