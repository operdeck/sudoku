package ottop.sudoku;

import java.util.List;

public interface IPuzzle {
    boolean isSolved();

    //http://www.extremesudoku.info/sudoku.html
    //http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
    int solve();

    char toChar(int n);

    boolean isOccupied(int y, int x);

    char getOriginalCharacter(int y, int x);

    int getWidth();
    int getHeight();

    boolean isInconsistent();

    boolean eliminateByRadiationFromIntersections(PossibilitiesContainer cache);
    PossibilitiesContainer getPossibilities(); // TODO by hint level
    List<Group> getGroups();
}
