package ottop.sudoku;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This is specific to 9x9 boards
public class Coord implements Comparable<Coord>{
	private Pattern pattern = Pattern.compile("^r(\\d+)c(\\d+)$", Pattern.CASE_INSENSITIVE);

	// must be of form r4c8
	public Coord(String s) {
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
	private int coord;

	static final List<Coord> all = new ArrayList<>();
	
	static {
		for (int x=0; x<9; x++) {
			for (int y=0; y<9; y++) {
				all.add(new Coord(x, y));
			}
		}
	}
	
	private int getKey(int x, int y) {
		return x+y*9;
	}

	@Override
	public int hashCode() {
		return coord;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coord) {
			return ((Coord)obj).coord == coord;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		int y = coord / 9;
		int x = coord % 9;
		return "r"+(y+1)+"c"+(x+1);
	}

	public int compareTo(Coord o) {
		return coord - o.coord;
	}

	public Integer getY() {
		return coord / 9;
	}

	public Integer getX() {
		return coord % 9;
	}
	
}
