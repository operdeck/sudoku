package ottop.sudoku.group;

import ottop.sudoku.Coord;
import ottop.sudoku.puzzle.IPuzzle;

public class SingleCellGroup extends AbstractGroup {
    public SingleCellGroup(Coord cell, IPuzzle p) {
        super(cell.getX(), cell.getY(), p, "Cell: " + cell.toString());
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
