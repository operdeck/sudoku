package ottop.sudoku;

public interface IPuzzle {
    boolean solved();

    //http://www.extremesudoku.info/sudoku.html
    //http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
    int solve();

    char toChar(int n);

    boolean isOccupied(int y, int x);

    char getOriginalCharacter(int y, int x);
}
