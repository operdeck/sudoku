package ottop.sudoku;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coord implements Comparable<Coord>{

	// must be of form r4c8
	public Coord(String s) {
		Pattern pattern = Pattern.compile("^r(\\d+)c(\\d+)$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(s);
		boolean matchFound = matcher.find();
		if(matchFound) {
			int y = Integer.parseInt(matcher.group(1))-1;
			int x = Integer.parseInt(matcher.group(2))-1;
			coord = getKey(x,y);
		} else {
			throw new IllegalArgumentException("Not a valid coordinate: " + s);
		}

	}

	public Coord(int x, int y) {
		if (x<0 || y<0) throw new IllegalArgumentException("Coordinates should be >= 0");
		coord = getKey(x, y);
	}

	// internal representation
	private final int coord;

	@Override
	public int hashCode() {
		return coord;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof Coord) {
			return ((Coord)obj).coord == coord;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		int y = getY();
		int x = getX();
		return "r"+(y+1)+"c"+(x+1);
	}

	public int compareTo(Coord o) {
		return coord - o.coord;
	}

	private final int MULTIPLIER = 1000;

	private int getKey(int x, int y) {
		return x+y*MULTIPLIER;
	}

	public Integer getY() {
		return coord / MULTIPLIER;
	}

	public Integer getX() {
		return coord % MULTIPLIER;
	}
}
