package com.he161696.kingbarber.model;

public class Service {
    private int ServiceId;
    private String Name;
    private int Price;
    private int serviceTime;

    public Service() {
    }

    public Service(String name, int price, int serviceId) {
        Name = name;
        Price = price;
        ServiceId = serviceId;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        Price = price;
    }

    public int getServiceTime() {
        return serviceTime;
    }
    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }
}
