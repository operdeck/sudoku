package ottop.sudoku.board;

import ottop.sudoku.puzzle.IPuzzle;

public class RectangularGroup extends AbstractGroup {

    public RectangularGroup(int startX, int startY, IPuzzle myPuzzle, String id) {
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
