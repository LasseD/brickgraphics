package building;

import java.io.*;

import building.PartType.Category;

import colors.LEGOColor;

public class Part implements Comparable<Part> {
	public static final String STUDS_UP_TURN_LDRAW_0 = " 1 0  0 0 1 0  0 0  1"; 
	public static final String STUDS_UP_TURN_LDRAW_1 = " 0 0  1 0 1 0 -1 0  0";
	public static final String STUDS_UP_TURN_LDRAW_2 = "-1 0  0 0 1 0  0 0 -1";
	public static final String STUDS_UP_TURN_LDRAW_3 = " 0 0 -1 0 1 0  1 0  0";
	public static final String STUDS_UP_TURN_LDD_0 = "1,0,0,0,1,0,0,0,1"; 
	public static final String STUDS_UP_TURN_LDD_1 = "0,0,1,0,1,0,-1,0,0";
	public static final String STUDS_UP_TURN_LDD_2 = "-1,0,0,0,1,0,0,0,-1";
	public static final String STUDS_UP_TURN_LDD_3 = "0,0,-1,0,1,0,1,0,0";
	public static final String[] LDraw_STUDS_UP_TURNS = {STUDS_UP_TURN_LDRAW_0, STUDS_UP_TURN_LDRAW_1, STUDS_UP_TURN_LDRAW_2, STUDS_UP_TURN_LDRAW_3};
	public static final String[] LDD_STUDS_UP_TURNS = {STUDS_UP_TURN_LDD_3, STUDS_UP_TURN_LDD_2, STUDS_UP_TURN_LDD_1, STUDS_UP_TURN_LDD_0};
	
	public int x, y, z, step; // Position in [0,...] for studXstudXplate position
	public LEGOColor color;
	public PartType type;
	
	public Part(int x, int y, int z, LEGOColor color, PartType type) {
		if(type == null)
			throw new NullPointerException("Type is null!");
		
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
		if(type.getCategory() != other.type.getCategory()) {
			return type.getCategory().compareTo(other.type.getCategory());
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
		if(!color.isLDraw())
			throw new IllegalStateException("Color is not available in LDraw: " + color);
		int overwriteY = yMult*y+type.getLDrawCenterY();
		int overwriteX = xMult*x+type.getLDrawCenterX();
		int overwriteZ = z*zMult;
		if(type.getCategory() == Category.Brick)
			overwriteZ += 2*zMult;
		
		int overwriteColor = color.getLDraw()[0].getID();
		out.printf("1 %d %d %d %d %s %s.dat", overwriteColor, overwriteX, overwriteZ, overwriteY, 
				LDraw_STUDS_UP_TURNS[type.getTimesTurned90Degrees()], type.getID());
		out.println(); // Because \n apparently is a different character...
	}
}
