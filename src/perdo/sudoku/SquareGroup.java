package perdo.sudoku;


public class SquareGroup extends Group {

	public SquareGroup(int startX, int startY, Puzzle myPuzzle, String id) {
		super(startX, startY, myPuzzle, id);
	}

//	protected int getIndex(int x, int y) { return y*3 + x; }
	@Override
	protected int internalIndexToRelativeX(int idx) { return idx % 3; }
	@Override
	protected int internalIndexToRelativeY(int idx) { return idx / 3; }
	
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
