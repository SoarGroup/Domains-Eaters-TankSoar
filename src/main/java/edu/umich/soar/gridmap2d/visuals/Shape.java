package edu.umich.soar.gridmap2d.visuals;

import java.util.HashMap;
import java.util.Map;

public class Shape {
	public static final String kRound = "round";
	public static final String kSquare = "square";
	public static final String kTriangle = "triangle";
	
	public static final Shape ROUND = new Shape(kRound);
	public static final Shape SQUARE = new Shape(kSquare);
	public static final Shape TRIANGLE = new Shape(kTriangle);
	
	private static Map<String, Shape> shapeHash = new HashMap<String, Shape>();
	
	static {
		shapeHash.put(kRound, ROUND);
		shapeHash.put(kSquare, SQUARE);
		shapeHash.put(kTriangle, TRIANGLE);
	}
	
	private static int nextID = 0;
	private final String name;
	private final int id;
	
	public static Shape getShape(String name) {
		return shapeHash.get(name);
	}
	
	private Shape(String name) {
		this.name = name;
		this.id = nextID;
		++nextID;
	}
	
	public int id() {
		return id;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean equals(Shape shape) {
		return this.id == shape.id;
	}
}
