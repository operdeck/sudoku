package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class RectangularGroup extends AbstractGroup {

    public RectangularGroup(int startX, int startY, ISudoku myPuzzle, String id) {
        super(startX, startY, myPuzzle, id);
    }

    @Override
    public int internalIndexToRelativeX(int idx) {
        return idx % 5;
    }

    @Override
    public int internalIndexToRelativeY(int idx) {
        return idx / 5;
    }
}
