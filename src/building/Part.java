package building;

import java.io.*;

import colors.LEGOColor;

public class Part implements Comparable<Part> {
	public static final String STUDS_UP_TURN_0 = " 0 0 -1 0 1 0  1 0  0";
	public static final String STUDS_UP_TURN_1 = " 1 0  0 0 1 0  0 0  1";
	public static final String STUDS_UP_TURN_2 = " 0 0  1 0 1 0 -1 0  0";
	public static final String STUDS_UP_TURN_3 = "-1 0  0 0 1 0  0 0 -1";
	public static final String[] STUDS_UP_TURNS = {STUDS_UP_TURN_0, STUDS_UP_TURN_1, STUDS_UP_TURN_2, STUDS_UP_TURN_3};
	
	public int x, y, z, step; // Position in [0,...] for studXstudXplate position
	public LEGOColor color;
	public PartType type;
	
	public Part(int x, int y, int z, LEGOColor color, PartType type) {
		this.color = color;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}

	@Override
	public int compareTo(Part other) {
		if(step != other.step)
			return step - other.step;
		if(z != other.z)
			return z - other.z;
		if(type.isBrick() != other.type.isBrick()) {
			return type.isBrick() ? 1 : -1;
		}
		if(y != other.y)
			return y - other.y;
		if(x != other.x)
			return x - other.x;
		if(other.type.getTimesTurned90Degrees() != other.type.getTimesTurned90Degrees())
			return type.getTimesTurned90Degrees() - other.type.getTimesTurned90Degrees();
		if(color.getIDRebrickable() != other.color.getIDRebrickable())
			return color.getIDRebrickable() - other.color.getIDRebrickable();
		return type.getID() - other.type.getID();
	}
	
	public void printLDR(PrintWriter out, int xMult, int yMult, int zMult) {
		int overwriteY = (int)Math.round(yMult*(y + 0.5*type.getDepth()));
		int overwriteX = (int)Math.round(xMult*(x + 0.5*type.getWidth()));
		int overwriteZ = z*zMult;
		if(type.isBrick())
			overwriteZ += 2*zMult;

		int overwriteColor = color.getFirstIDLDraw();
		out.printf("1 %d %d %d %d %s %s.dat", overwriteColor, overwriteY, overwriteZ, overwriteX, 
				STUDS_UP_TURNS[type.getTimesTurned90Degrees()], type.getID());
		out.println(); // Because \n apparently is a different character...
	}
}
