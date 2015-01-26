package icon;

public class BrickIconMeasure {
	public final int mid, brickHeight, plateHeight, studHeight, brickWidth, studWidth;

	public BrickIconMeasure(int size) {
		mid = size/2;

		brickHeight = (size*6)/10;
		plateHeight = (size*2)/10;
		studHeight = size/10;
		brickWidth = size/2;
		studWidth = brickWidth*2/3;		
	}
}
