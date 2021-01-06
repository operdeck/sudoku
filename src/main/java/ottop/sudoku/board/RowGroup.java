package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class RowGroup extends AbstractGroup {

    public RowGroup(int startX, int startY, ISudoku myPuzzle, String id) {
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
