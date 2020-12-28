package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class GroupIntersection {
	private final Set<Coord> intersection;
	private AbstractGroup[] grps = new AbstractGroup[2];
	
	public GroupIntersection(AbstractGroup a, AbstractGroup b) {
		intersection = new TreeSet<>(a.getCoords());
		intersection.retainAll(b.getCoords());
		grps[0] = a;
		grps[1] = b;
	}

	public static Set<GroupIntersection> createGroupIntersections(AbstractGroup[] groups) {
		Set<GroupIntersection> intersections = new LinkedHashSet<>();
		for (AbstractGroup a:groups) {
			for (AbstractGroup b:groups) {
				if (a != b) {
					GroupIntersection overlap = new GroupIntersection(a, b);
					// Intersections of 1 don't count. These would be seen as "lone values" anyway.
					// TODO: if all elements of the intersection are occupied skip it also
					if (overlap.intersection.size() > 1) {
						intersections.add(overlap);
					}
				}
			}
		}
		return intersections;
	}

	public AbstractGroup getIntersectionGroup(int i) {
		return grps[i];
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

	public Set<Coord> getIntersection() {
		return intersection;
	}
}
