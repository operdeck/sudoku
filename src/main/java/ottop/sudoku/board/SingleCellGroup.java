package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class SingleCellGroup extends AbstractGroup {
    public SingleCellGroup(Coord cell) {
        super(cell.getX(), cell.getY(), 1, "Cell: " + cell.toString());
    }

    @Override
    public int internalIndexToRelativeX(int idx) {
        return 0;
    }

    @Override
    public int internalIndexToRelativeY(int idx) {
        return 0;
    }
}
