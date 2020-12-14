import numpy as np
import matplotlib.pyplot as plt


class SudokuSolver:
    """Solver"""

    def __init__(self, aSudoku):
        self.moves = dict()
        self.sudoku = aSudoku

    def moveToStr(self, mv):
        return self.sudoku.celltostr(mv[0]) + ": " + str(mv[1])

    def hasMoves(self):
        if len(self.moves) == 0:
            return False
        else:
            for cell in sorted(self.moves.keys()):
                print(self.moveToStr((cell, self.moves[cell]["move"])))
                for r in self.moves[cell]["reasons"]:
                    print("   " + r["method"])
                    # for detail in r["reason"]:
                    #     print("      * " + str(detail))
            return True

    def getBestCandidateMove(self):
        mv = sorted(self.moves.keys())[0]
        return mv, self.moves[mv]["move"]

    def solve(self):
        # These should move here
        self.sudoku.simpleElimination()
        self.sudoku.nakedSubgroupElimination()

        self.moves = self.sudoku.getMoves()
