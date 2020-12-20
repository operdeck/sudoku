package ottop.sudoku.group;


import ottop.sudoku.StandardPuzzle;

public class SquareGroup extends AbstractGroup {

	public SquareGroup(int startX, int startY, StandardPuzzle myPuzzle, String id) {
		super(startX, startY, myPuzzle, id);
	}

//	protected int getIndex(int x, int y) { return y*3 + x; }
	@Override
	public int internalIndexToRelativeX(int idx) { return idx % 3; }
	@Override
	public int internalIndexToRelativeY(int idx) { return idx / 3; }
	
//	@Override
//	protected boolean isInGroup(int absX, int absY) {
//		int relX = absX - startX;
//		int relY = absY - startY;
//		boolean isInGroup = (relX >= 0 && relX <= 2 && relY >= 0 && relY <= 2);
//		return isInGroup;
//	}
//	
//	@Override
//	protected boolean isOccupied(int absX, int absY) {
//		int relX = absX - startX;
//		int relY = absY - startY;
//		int v = digits[getIndex(relX, relY)];
//		return v != -1;
//	}
}
