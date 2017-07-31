package colors;

public class LEGOColorGrid {
	private LEGOColor[][] grid;
	
	public LEGOColorGrid(LEGOColor[][] grid) {
		this.grid = grid;
	}
	
	public LEGOColor[] getRow(int y) {
		return grid[y];
	}
	
	public int getHeight() {
		return grid.length;
	}
	
	public int getWidth() {
		return grid[0].length;
	}
}
