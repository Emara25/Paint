package com.csed.paint.ui.model;

public class Factory{

    public static Shape createShape(String type, Shape shape) {
        double[] properties = shape.getProperties();
        return switch (type) {
            case "square" ->
                    new Square(shape.getId(), shape.getColor(), shape.getProperties());
            case "line" ->
                    new Line(shape.getId(), shape.getColor(), shape.getProperties());
            case "triangle" ->
                    new Triangle(shape.getId(), shape.getColor(), shape.getProperties());
            case "circle" ->
                    new Circle(shape.getId(), shape.getColor(), shape.getProperties());
            case "rectangle" ->
                    new Rectangle(shape.getId(), shape.getColor(), shape.getProperties());
            case "ellipse" ->
                    new Ellipse(shape.getId(), shape.getColor(), shape.getProperties());

            default -> null;
        };
    }
}
