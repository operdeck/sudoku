package perdo.sudoku;
import java.util.Set;
import java.util.TreeSet;

public class GroupIntersection {
	Set<Coord> intersection;
	Group[] grps = new Group[2];
	
	public GroupIntersection(Group a, Group b) {
		intersection = new TreeSet<Coord>(a.getCoords());
		intersection.retainAll(b.getCoords());
		grps[0] = a;
		grps[1] = b;
	}

	public boolean isEmpty() {
		return intersection.size() <= 1; // consider an area of only one cell empty as well
	}
	
	@Override
	public String toString() {
		return intersection.toString();
	}

	@Override
	public int hashCode() {
		return intersection.hashCode();
	}

	@Override
	// Consider equal if the intersection is equal but don't care about the order of the two group references.
	public boolean equals(Object obj) {
		if (obj instanceof GroupIntersection) {
			GroupIntersection other = (GroupIntersection)obj;
			if ((grps[0] == other.grps[0] && grps[1] == other.grps[1]) || (grps[0] == other.grps[1] && grps[1] == other.grps[0])) {
				if (intersection == null && other.intersection == null) {
					return true;
				} else if (intersection == null || other.intersection == null) {
					return false;
				} else {
					return intersection.equals(other.intersection);
				}
			} else {
				return false;
			}
		}
		return super.equals(obj);
	}
}
