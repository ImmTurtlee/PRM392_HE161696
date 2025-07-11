package com.he161696.kingbarber.model;

public class Appointment_services {
    private int AppointmentId;
    private int ServiceId;

    private String serviceName;  // thông tin bổ sung để hiển thị
    private double price;
    private int serviceTime;

    public Appointment_services() {
    }

    public Appointment_services(int appointmentId, int serviceId, String serviceName, double price, int serviceTime) {
        AppointmentId = appointmentId;
        ServiceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.serviceTime = serviceTime;
    }

    public int getAppointmentId() {
        return AppointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        AppointmentId = appointmentId;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getServiceTime() {
        return serviceTime;
    }
    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }
}
