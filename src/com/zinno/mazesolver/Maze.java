package com.zinno.mazesolver;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Maze {
	
	private BufferedImage bimage;
	private int xBlockSize;
	private int yBlockSize;
	private int xAirSize;
	private int yAirSize;
	private boolean[][] maze;
	private boolean[][] solution;
	
	public Maze(File loc) throws IOException {
		this.bimage = ImageIO.read(loc);
		findYBlockSize();
		findXBlockSize();
		findYAirSize();
		findXAirSize();
		createMaze();
		generateSolution();
	}
	
	private void createMaze() {
		this.maze = new boolean[bimage.getHeight()/(xBlockSize + xAirSize)*2 + 1][bimage.getWidth()/(yBlockSize + yAirSize)*2 + 1];  // Problem
		int borderColor = bimage.getRGB(0,0);
		int y = 0;
		int yCount = 0;
		while(y < bimage.getHeight()) {
			int x = 0;
			int xCount = 0;
			while(x < bimage.getWidth()) {
				this.maze[yCount][xCount] = !(bimage.getRGB(x, y) == borderColor);
				xCount += 1;
				if(xCount % 2 == 0)
					x += this.xBlockSize;
				else
					x += this.xAirSize;
			}
			if(yCount % 2 == 0)
				y += this.yBlockSize;
			else
				y += this.yAirSize;
			yCount += 1;
		}
	}
	
	private void findXBlockSize() {
		int minValue = bimage.getWidth();
		for(int y = 0; y < bimage.getHeight(); y++) {
			boolean isFound = false;
			int currentHeight = 0;
			for(int x = 0; x < bimage.getWidth(); x++) {
				if(bimage.getRGB(x, y) == bimage.getRGB(0,0)) {
					isFound = true;
					currentHeight += 1;
				} else {
					if(isFound) {
						if(minValue > currentHeight)
							minValue = currentHeight;
						break;
					}
				}
			}
		}
		this.xBlockSize = minValue;
	}
	
	private void findYBlockSize() {
		int minValue = bimage.getHeight();
		for(int x = 0; x < bimage.getWidth(); x++) {
			boolean isFound = false;
			int currentHeight = 0;
			for(int y = 0; y < bimage.getHeight(); y++) {
				if(bimage.getRGB(x, y) == bimage.getRGB(0,0)) {
					isFound = true;
					currentHeight += 1;
				} else {
					if(isFound) {
						if(minValue > currentHeight)
							minValue = currentHeight;
						break;
					}
				}
			}
		}
		this.yBlockSize = minValue;
	}
	
	private void findXAirSize() {
		int minValue = bimage.getWidth();
		for(int y = 0; y < bimage.getHeight(); y++) {
			boolean isFound = false;
			int currentHeight = 0;
			for(int x = 0; x < bimage.getWidth(); x++) {
				if(bimage.getRGB(x, y) != bimage.getRGB(0,0)) {
					isFound = true;
					currentHeight += 1;
				} else {
					if(isFound) {
						if(minValue > currentHeight)
							minValue = currentHeight;
						break;
					}
				}
			}
		}
		this.xAirSize = minValue;
	}
	
	private void findYAirSize() {
		int minValue = bimage.getHeight();
		for(int x = 0; x < bimage.getWidth(); x++) {
			boolean isFound = false;
			int currentHeight = 0;
			for(int y = 0; y < bimage.getHeight(); y++) {
				if(bimage.getRGB(x, y) != bimage.getRGB(0,0)) {
					isFound = true;
					currentHeight += 1;
				} else {
					if(isFound) {
						if(minValue > currentHeight)
							minValue = currentHeight;
						break;
					}
				}
			}
		}
		this.yAirSize = minValue;
	}
	
	private void generateSolution() {
		boolean[][] copyOfMaze = this.getMaze();
		HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> locFrom = new HashMap<>();
		Pair<Integer, Integer> currentLoc = findPoints().getKey();
		Pair<Integer, Integer> endLoc = findPoints().getValue();
		do {
			Pair<Integer, Integer> oldLoc = currentLoc;
			copyOfMaze[oldLoc.getValue()][oldLoc.getKey()] = false;
			if(currentLoc.getValue() + 1 < copyOfMaze.length && copyOfMaze[currentLoc.getValue() + 1][currentLoc.getKey()]) { // NORTH;
				currentLoc = new Pair<>(currentLoc.getKey(), currentLoc.getValue() + 1);
				locFrom.put(currentLoc, oldLoc);
				continue;
			}
			if(currentLoc.getKey() + 1 < copyOfMaze[currentLoc.getValue()].length && copyOfMaze[currentLoc.getValue()][currentLoc.getKey() + 1]) { // EAST
				currentLoc = new Pair<>(currentLoc.getKey() + 1, currentLoc.getValue());
				locFrom.put(currentLoc, oldLoc);
				continue;
			}
			if(currentLoc.getValue() - 1 >= 0 && copyOfMaze[currentLoc.getValue() - 1][currentLoc.getKey()]) { // SOUTH
				currentLoc = new Pair<>(currentLoc.getKey(), currentLoc.getValue() - 1);
				locFrom.put(currentLoc, oldLoc);
				continue;
			}
			if(currentLoc.getKey() - 1 >= 0 && copyOfMaze[currentLoc.getValue()][currentLoc.getKey() - 1]) { // WEST
				currentLoc = new Pair<>(currentLoc.getKey() - 1, currentLoc.getValue());
				locFrom.put(currentLoc, oldLoc);
				continue;
			}
			currentLoc = locFrom.get(oldLoc);
		} while(!(currentLoc.equals(endLoc)));
		this.solution = new boolean[this.getMaze().length][this.getMaze()[0].length];
		for(boolean[] row : this.solution)
			Arrays.fill(row, false);
		Pair<Integer, Integer> current = endLoc;
		do {
			this.solution[current.getValue()][current.getKey()] = true;
			current = locFrom.get(current);
		} while(current != null);
	}
	
	@NotNull
	private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> findPoints() {
		Pair<Integer, Integer> start = null;
		Pair<Integer, Integer> end = null;
		for(int x = 0; x < maze[0].length; x++) {
			if(maze[0][x]) {
				if(start == null)
					start = new Pair<>(x, 0);
				else
					end = new Pair<>(x, 0);
				break;
			}
		}
		for(int x = 0; x < maze[maze.length-1].length; x++) {
			if(maze[maze.length-1][x]) {
				if(start == null)
					start = new Pair<>(x, maze.length-1);
				else
					end = new Pair<>(x, maze.length-1);
				break;
			}
		}
		for(int y = 0; y < maze.length; y++) {
			if(maze[y][0]) {
				if(start == null)
					start = new Pair<>(0, y);
				else
					end = new Pair<>(0, y);
				break;
			}
		}
		for(int y = 0; y < maze.length; y++) {
			if(maze[y][maze[y].length-1]) {
				if(start == null)
					start = new Pair<>(maze[y].length-1, y);
				else
					end = new Pair<>(maze[y].length-1, y);
				break;
			}
		}
		return new Pair<>(start, end);
	}
	
	public void printSolution(String name) throws IOException {
		if(this.getSolution() == null)
			this.generateSolution();
		File file = new File(name + ".png");
		if(!(file.exists())) {
			file.createNewFile();
		}
		BufferedImage solutionImage = this.bimage;
		for(int y = 1; y < this.getSolution().length; y+=2) {
			for(int x = 1; x < this.getSolution()[y].length; x+=2) {
				if(this.getSolution()[y][x])
					this.colorCord(x, y, solutionImage);
			}
		}
		ImageIO.write(solutionImage, "png", file);
		successMessage();
	}
	
	private void successMessage() {
		JFrame window = new JFrame("Maze Solver");
		JLabel label = new JLabel("<html><font family='times'><font size=10><font color='green'>Success</font><br>" +
				"<font size=5>The maze has been solved</font></html>", SwingConstants.CENTER);
		label.setBorder(new EmptyBorder(10,10,10,10));
		window.add(label);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	
	private void colorCord(int xCord, int yCord, BufferedImage image) {
		//calculate x pixels
		boolean xIsAir = true;
		int xCombinedAmount = Math.floorDiv(xCord, 2);
		int xCombinedSize = this.xBlockSize + this.xAirSize;
		int xBegin = xCombinedAmount * xCombinedSize;
		if(xCord%2 == 0) {
			xBegin += this.xBlockSize;
			xIsAir = false;
		}
		//calculate y pixels
		boolean yIsAir = true;
		int yCombinedAmount = Math.floorDiv(yCord, 2);
		int yCombinedSize = this.yBlockSize + this.yAirSize;
		int yBegin = yCombinedAmount * yCombinedSize;
		if(xCord%2 == 0) {
			yBegin += this.yBlockSize;
			yIsAir = false;
		}
		//prepares the loop to color pixels
		int xTotal;
		if(xIsAir)
			xTotal = this.xAirSize;
		else
			xTotal = this.xBlockSize;
		int yTotal;
		if(yIsAir)
			yTotal = this.yAirSize;
		else
			yTotal = this.yBlockSize;
		
		yTotal /= 1.5; // Makes the dots smaller
		xTotal /= 1.5;
		yBegin += this.yAirSize - yTotal; // Centers the dots
		xBegin += this.xAirSize - xTotal;
		
		// Colors the pixels
		for(int y = yBegin; y < yTotal + yBegin; y++) {
			for(int x = xBegin; x < xTotal + xBegin; x++) {
				image.setRGB(x, y, Color.RED.getRGB());
			}
		}
	}
	
	public boolean[][] getMaze() {
		return this.maze;
	}
	
	public boolean[][] getSolution() { return this.solution; }
	
	public static void main(String[] args) {
		File dir = new File(".").getAbsoluteFile();
		for(File file : dir.listFiles()) {
			if(!(file.getName().endsWith(".png")))
				continue;
			
			try {
				Maze maze = new Maze(file);
				maze.printSolution(file.getName() + " (with solution)");
				return;
			} catch(IOException ex) {}
			
		}
		errorMessage();
	}
	
	private static void errorMessage() {
		JFrame window = new JFrame("Maze Solver");
		JLabel label = new JLabel("<html><font family='times'><font size=10><font color='red'>Maze Not Found</font><br>" +
				"<font size=5>Try visiting mazegenerator.net and download a .png maze file</font></html>", SwingConstants.CENTER);
		label.setBorder(new EmptyBorder(10,10,10,10));
		window.add(label);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
}
