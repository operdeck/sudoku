package ottop.sudoku.board;


import ottop.sudoku.puzzle.ISudoku;

public class SquareGroup extends AbstractGroup {

    public SquareGroup(int startX, int startY, ISudoku myPuzzle, String id) {
        super(startX, startY, myPuzzle, id);
    }

    @Override
    public int internalIndexToRelativeX(int idx) {
        return idx % 3;
    }

    @Override
    public int internalIndexToRelativeY(int idx) {
        return idx / 3;
    }
}
