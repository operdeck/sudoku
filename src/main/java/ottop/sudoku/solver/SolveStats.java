package ottop.sudoku.solver;

public class SolveStats {
    private int numberOfEliminationIterations = 0;
    private int maxNumberOfEliminationIterations = 0;

    public void addIteration() {
        numberOfEliminationIterations++;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rounds: ").append(maxNumberOfEliminationIterations);
        return sb.toString();
    }

    public int getIterations() {
        return maxNumberOfEliminationIterations;
    }

    public void startFindMove() {
        numberOfEliminationIterations = 0;
    }

    public void endFindMove() {
        maxNumberOfEliminationIterations = Math.max(numberOfEliminationIterations, maxNumberOfEliminationIterations);
    }

}
