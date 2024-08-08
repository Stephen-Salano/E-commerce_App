package com.example.e_commerce.Model;

public class Products {
    private String category, date, description, image, name,pid, price, time ;

    // constructors
    public  Products(){}

    public Products(String name, String description, String category, String image, String price, String pid, String date, String time) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.image = image;
        this.price = price;
        this.pid = pid;
        this.date = date;
        this.time = time;
    }

    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
