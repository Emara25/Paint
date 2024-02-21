package com.csed.paint.ui.model;

public  class Shape {

    private String type ;
    private String color ;
    private long id ;
    private double[] properties ;

    public Shape(String type , long id ,String color , double[] properties) {
        this.type = type ;
        this.id = id;
        this.color = color;
        this.properties = properties;
    }
    public Shape(){}


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double[] getProperties() {
        return properties;
    }

    public void setProperties(double[] properties) {
        this.properties = properties;
    }
}

