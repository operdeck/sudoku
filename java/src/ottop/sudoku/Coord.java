package ottop.sudoku;
import java.util.ArrayList;
import java.util.List;

// This is specific to 9x9 boards
public class Coord implements Comparable<Coord>{
	public Coord(int x, int y) {
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

	// why not return int?
	public Integer getRow() {
		return coord / 9;
	}

	public Integer getCol() {
		return coord % 9;
	}
	
}
