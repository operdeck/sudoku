package ottop.sudoku;

public class RowGroup extends Group {

	public RowGroup(int startX, int startY, Puzzle myPuzzle, String id) {
		super(startX, startY, myPuzzle, id);
	}

	@Override
	public int internalIndexToRelativeX(int idx) {
		return idx;
	}

	@Override
	public int internalIndexToRelativeY(int idx) {
		return 0;
	}

	public int getRow() {
		return super.startY;
	}
}
