from sudokupy.SudokuPuzzles import *
from sudokupy.SudokuSolver import SudokuSolver

if __name__ == "__main__":

    # TODO: add more advanced solving code, see naked subgroups and see Java in same repo

    puzzle: Sudoku = nrc3feb2018

    puzzle.print()

    while True:
        nextMove = None
        if puzzle.isInconsistent():
            print("Inconsistency detected")
        elif puzzle.isComplete():
            print("Sudoku is complete.")
        else:
            solver = SudokuSolver(puzzle)
            solver.solve()
            if solver.hasMoves():
                nextMove = solver.getBestCandidateMove()
                print("Next move " + solver.moveToStr(nextMove))
            else:
                print("No moves available")
        if nextMove is None:
            print("Dismiss dialog to end application")
        else:
            print("Dismiss dialog to do next move")
        puzzle.show(nextMove[0])
        if nextMove is None:
            break
        # will be done after dialog returns
        puzzle.placeAtCell(nextMove[1], nextMove[0])
