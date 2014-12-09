package perdo.sudoku;

public class RowGroup extends Group {

	public RowGroup(int startX, int startY, String[] mySudoku, String id) {
		super(startX, startY, mySudoku, id);
	}

	@Override
	protected int internalIndexToRelativeX(int idx) {
		return idx;
	}

	@Override
	protected int internalIndexToRelativeY(int idx) {
		return 0;
	}

	public int getRow() {
		return startY;
	}
}
