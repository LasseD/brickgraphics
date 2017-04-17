package building;

import colors.LEGOColor;

public class Part implements Comparable<Part> {
	public int x, y, z; // Position in [0,...] for studXstudXplate position
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
}
