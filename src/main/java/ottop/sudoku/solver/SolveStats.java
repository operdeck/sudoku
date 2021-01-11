package ottop.sudoku.solver;

public class SolveStats {
    // TODO: this should become a proper solveStats class instead
    private int numberOfEliminationIterations;


    public void addIteration() {
        numberOfEliminationIterations++;
    }
}
