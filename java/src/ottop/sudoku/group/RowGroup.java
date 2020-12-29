package ottop.sudoku.group;

import ottop.sudoku.puzzle.IPuzzle;

public class RowGroup extends AbstractGroup {

    public RowGroup(int startX, int startY, IPuzzle myPuzzle, String id) {
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
