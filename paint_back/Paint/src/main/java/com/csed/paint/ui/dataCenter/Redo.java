package com.csed.paint.ui.dataCenter;

import com.csed.paint.ui.model.Shape;

import java.util.Stack;

public class Redo {
    public static Stack<Integer> opStack = new Stack<>() ;
    public static Stack<Shape> objStack = new Stack<>() ;
}
