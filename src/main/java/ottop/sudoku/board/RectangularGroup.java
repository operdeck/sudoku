package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

import java.util.ArrayList;
import java.util.List;

public class RectangularGroup extends AbstractGroup {

    private static Coord[] createCoords(int startX, int startY, int endX, int endY) {
        List<Coord> coords = new ArrayList<>();
        for (int x=startX; x<endX; x++) {
            for (int y=startY; y<endY; y++) {
                Coord c = new Coord(x, y);
                coords.add(c);
            }
        }
        return coords.toArray(new Coord[0]);
    }

    public RectangularGroup(int startX, int startY, int endX, int endY, String id) {
        super(createCoords(startX, startY, endX, endY), id);
    }

//    @Override
//    public int internalIndexToRelativeX(int idx) {
//        return idx % 5;
//    }
//
//    @Override
//    public int internalIndexToRelativeY(int idx) {
//        return idx / 5;
//    }
}
