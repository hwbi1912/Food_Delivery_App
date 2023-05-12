package com.example.food_delivery_app.Model;

import java.util.List;

public class Order {
    private String id;
    private String phone;
    private String name;
    private String address;
    private String total;
    private List<OrderDetail> foods;
    private String status;
    private String distance;

    public Order() {
    }

    public Order(String phone, String name, String address, String total, List<OrderDetail> foods, String distance) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.foods = foods;
        this.status = "0";
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<OrderDetail> getFoods() {
        return foods;
    }

    public void setFoods(List<OrderDetail> foods) {
        this.foods = foods;
    }
}
