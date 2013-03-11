package bricks;

public enum Sizes {
	plate(5,2), brick(5,6), block(10,10);
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
	
	private int width, height;
	private Sizes(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
