import matplotlib.pyplot as plt
import numpy as np


class Sudoku:
    """Base Sudoku class"""

    def __init__(self, sudoku):
        self.puzzle = self.fromTextTuple(sudoku)
        self.originalPuzzle = self.puzzle.copy()
        self.initGroups()
        self.initPossibilities()

    def fromTextTuple(self, sudoku):
        puzzle = dict()
        if (len(sudoku) != 9):
            raise Exception('Need 9 rows')
        for r in range(9):
            arow = sudoku[r]
            if (len(arow) != 9):
                raise Exception('Need 9 cells in row ' + str(r + 1) + ': \"' + arow + '"')
            for c in range(9):
                if arow[c] == ' ' or arow[c] == '.':
                    pass
                elif arow[c] >= '1' and arow[c] <= '9':
                    puzzle[(r, c)] = int(arow[c])
                else:
                    raise Exception('Unrecognized character ' + arow[c] + ' at row ' + str(r + 1) + " col " + str(
                        c + 1) + ': \"' + arow + '"')
        return puzzle

    def initGroups(self):
        allrows = list()
        allcols = list()
        allsquares = list()
        self.emptycells = set()
        for x in range(9):
            allrows.append({"name": "row " + str(1 + x), "cells": list()})
            allcols.append({"name": "col " + str(1 + x), "cells": list()})
            allsquares.append({"name": "sqr " + str(1 + x), "cells": list()})
        for i in range(9):
            for j in range(9):
                cell = (i, j)
                allrows[i]["cells"].append(cell)
                allcols[j]["cells"].append(cell)
                allsquares[3 * (i // 3) + (j // 3)]["cells"].append(cell)
                if not cell in self.puzzle:
                    self.emptycells.add(cell)
        self.groups = allrows + allcols + allsquares

    def initPossibilities(self):
        self.possibilities = dict()
        self.explanations = dict()
        for cell in self.emptycells:
            self.possibilities[cell] = set(range(1, 10))
            self.explanations[cell] = []

    def isInconsistent(self):
        # TODO
        return False

    def isComplete(self):
        return 0 == len(self.emptycells)

    def placeFromStringRepresentation(self, digit, cellAsString):
        if len(cellAsString) != 4:
            raise Exception('Cell format is 4 characters R<row>C<col>')
        row = int(cellAsString[1]) - 1
        col = int(cellAsString[3]) - 1
        cell = (row, col)
        self.placeAtCell(digit, cell)

    def placeAtCell(self, digit, cell):
        print("Place " + str(digit) + " at " + self.celltostr(cell))
        self.puzzle[cell] = digit
        self.emptycells.remove(cell)
        self.initPossibilities()

    def print(self):
        print(self.groups)
        print(str(len(self.groups)) + " groups")

    def getShowRaster(self):
        mx = np.zeros((9, 9))
        # color some squares to highlight the structure
        for x in range(0, 3):
            for y in range(0, 3):
                mx[x, y] = 1
                mx[x + 6, y] = 1
                mx[x, y + 6] = 1
                mx[x + 6, y + 6] = 1
                mx[x + 3, y + 3] = 1
        return mx

    def show(self, highlightCell = None):
        fig, ax = plt.subplots()
        im = ax.imshow(self.getShowRaster(), cmap="Purples",
                       alpha=0.6)  # strange way to plot a grid - need something simpler...
        # https://matplotlib.org/3.1.0/tutorials/colors/colormaps.html
        # All ticks and label them with the respective list entries
        ax.set_xticks(np.arange(9))
        ax.set_yticks(np.arange(9))
        ax.set_xticklabels(range(1, 10))
        ax.set_yticklabels(range(1, 10))
        for cell in self.puzzle:
            ax.text(cell[1], cell[0], self.puzzle[cell], ha="center", va="center",
                    color=("black" if (cell in self.originalPuzzle) else "blue"), size=15)
        for cell in self.emptycells:
            p = self.possibilities[cell]
            txt = "\n".join(["".join((str(i) if (i in p) else ".") for i in range(1, 4)),
                             "".join((str(i) if (i in p) else ".") for i in range(4, 7)),
                             "".join((str(i) if (i in p) else ".") for i in range(7, 10))])
            ax.text(cell[1], cell[0], txt, ha="center", va="center", color="black", size=9, alpha=0.6,
                    family='monospace')
        for x in range(8):
            if x == 2 or x == 5:
                pass
            else:
                plt.axhline(0.5 + x, color="grey")
                plt.axvline(0.5 + x, color="grey")
        for x in range(8):
            if x == 2 or x == 5:
                plt.axhline(0.5 + x, color="black")
                plt.axvline(0.5 + x, color="black")
        ax.set_title("Sudoku")

        if highlightCell is not None:
            plt.plot([highlightCell[1]-0.5, highlightCell[1]-0.5], [highlightCell[0]-0.5, highlightCell[0]+0.5], lw=2, c="red", zorder=10)
            plt.plot([highlightCell[1]+0.5, highlightCell[1]+0.5], [highlightCell[0]-0.5, highlightCell[0]+0.5], lw=2, c="red", zorder=10)
            plt.plot([highlightCell[1]-0.5, highlightCell[1]+0.5], [highlightCell[0]-0.5, highlightCell[0]-0.5], lw=2, c="red", zorder=10)
            plt.plot([highlightCell[1]-0.5, highlightCell[1]+0.5], [highlightCell[0]+0.5, highlightCell[0]+0.5], lw=2, c="red", zorder=10)
        fig.tight_layout()
        plt.show()

    # Make sure all groups are correct
    def check(self):
        pass

    def celltostr(self, pair):
        return "R" + str(1 + pair[0]) + "C" + str(1 + pair[1])

    def getMoves(self):
        possibleMoves = dict()
        for cell in self.emptycells:
            p = self.possibilities[cell]
            # Only one possible value in this cell
            if (len(p) == 1):
                d = list(p)[0]
                if cell in possibleMoves:
                    if possibleMoves[cell]["move"] == d:
                        # just add another reason for the same move
                        possibleMoves[cell]["reasons"].append({"method": "single cell value",
                                                               "reason": self.explanations[cell]})
                    else:
                        # inconsistent results
                        raise Exception("ERROR! Found different move than " + str(d) + " at " + self.celltostr(cell))
                else:
                    possibleMoves[cell] = {"move": d,
                                           "reasons": [{"method": "single cell value",
                                                        "reason": self.explanations[cell]}]}
        for group in self.groups:
            for d in range(1, 10):
                onlyPossibileCellInGroup = None
                nPossibilitiesInGroup = 0
                emptyCellsInGroup = set(group["cells"]).intersection(self.emptycells)
                for cell in emptyCellsInGroup:
                    if d in self.possibilities[cell]:
                        onlyPossibileCellInGroup = cell
                        nPossibilitiesInGroup = nPossibilitiesInGroup + 1
                if nPossibilitiesInGroup == 1:
                    # reasons why d is excluded in all other cells in the group
                    # for all other cells get explanations x
                    # if x[1] contains d then add x[0] to reason why d is excluded
                    detailedReasons = {}
                    emptyCellsInGroup.remove(onlyPossibileCellInGroup)
                    for cell in emptyCellsInGroup:
                        for x in self.explanations[cell]:
                            if d in x["eliminations"]:
                                detailedReasons[x["method"] + str(x["detail"])] = {"method": x["method"],
                                                                                   "detail": x["detail"]}
                    if not onlyPossibileCellInGroup in possibleMoves:
                        possibleMoves[onlyPossibileCellInGroup] = {"move": d, "reasons": []}
                    if possibleMoves[onlyPossibileCellInGroup]["move"] == d:
                        # just add another reason for the same move
                        possibleMoves[onlyPossibileCellInGroup]["reasons"].append(
                            {"method": "only place in " + group["name"],
                             "reason": detailedReasons.values()})
                    else:
                        raise Exception("ERROR! Found different move than " + str(d) + " at " + self.celltostr(
                            onlyPossibileCellInGroup))
        return possibleMoves

    # Filters the existing possibilities per cell by applying elimination
    # from all groups that this cell is part of. Basis is simple but we also
    # want to process the group that provides the largest nr of eliminations
    # first, so this becomes an iterative process.
    def simpleElimination(self):
        print('Simple elimination per cell')
        prepossibilitiescount = sum([len(c) for c in self.possibilities.values()])
        for cell in self.emptycells:
            candidates = self.possibilities[cell]
            while True:
                highestNrOfIntersections = 0
                groupWithHighestNrOfIntersections = None
                for agroup in self.groups:
                    if cell in agroup["cells"]:
                        digitsingroup = set([self.puzzle[c] for c in agroup["cells"] if c in self.puzzle])
                        commonelements = candidates.intersection(digitsingroup)
                        if len(commonelements) > highestNrOfIntersections:
                            highestNrOfIntersections = len(commonelements)
                            groupWithHighestNrOfIntersections = agroup
                if highestNrOfIntersections == 0:
                    break  # Done - no more intersections to be found
                digitsingroupWithHighestNrOfIntersections = set(
                    [self.puzzle[c] for c in groupWithHighestNrOfIntersections["cells"] if c in self.puzzle])
                commonelements = candidates.intersection(digitsingroupWithHighestNrOfIntersections)
                candidates = candidates - digitsingroupWithHighestNrOfIntersections
                self.explanations[cell].append({"method": "simple elimination",
                                                "detail": groupWithHighestNrOfIntersections["name"],
                                                "eliminations": sorted(commonelements)})
                self.possibilities[cell] = candidates
        postpossibilitiescount = sum([len(c) for c in self.possibilities.values()])
        print(postpossibilitiescount)

    # Filters the existing possibilities by checking if there are subgroups
    # in each group that all have the same possibilities (complete subgroups). So
    # if there are three cells in a group with all {1,7,8} then we are sure these
    # values can only exist there and 1, 7 and 8 can be removed from all other
    # possibilities in the cells of this group.
    def nakedSubgroupElimination(self):
        for agroup in self.groups:
            subgroups = dict()
            subgroupPossibilities = dict()
            for cell in agroup["cells"]:
                if cell in self.emptycells:
                    possibilitiesKey = str(sorted(self.possibilities[cell]))
                    if not possibilitiesKey in subgroups:
                        subgroups[possibilitiesKey] = []
                        subgroupPossibilities[possibilitiesKey] = sorted(self.possibilities[cell])
                    subgroups[possibilitiesKey].append(cell)
            for k in subgroups.keys():
                if len(subgroupPossibilities[k]) == len(subgroups[k]):
                    print("NAKED SUBGROUP FOUND IN " + agroup["name"])
                    print(subgroupPossibilities[k])
                    print(subgroups[k])
                    # this means these can be eliminated from all other cells
                    # in that same group
                    # reason: "naked subgroup [1,6,7] in nrc4" or so
                    # TODO: apply the naked subgroup elimination

    # Filters by looking for subgroups of values inside a group, where a subgroup
    # of size N exists in N cells (with N > 1 and N < 9) - which means that the
    # values of this subgroup can be eliminated from the other cells in the group.
    # This is similar to the above method however does not require the subgroups are
    # complete, i.e. there could be two cells with possibilities {1,6,7} and {1,3,7,8}
    # and {1,7} not occurring anywhere else in the possibilities of that group. This
    # means the two cells can be limited to just {1,7} and both 1 and 7 can be
    # eliminated from the rest of the group.
    def subgroupElimination(self):
        pass

    # Filters by looking at intersections of pairs of groups. If a value can only occur
    # in that intersection from the perspective of one of the two groups, that value can
    # be eliminated from the rest of the other group as well.
    def radiationElimination(self):
        pass


class NRCSudoku(Sudoku):
    """ Adds rectangular areas """

    def initGroups(self):
        super().initGroups()
        sqr1 = list()
        sqr2 = list()
        sqr3 = list()
        sqr4 = list()
        for i in range(9):
            for j in range(9):
                if 1 <= i <= 3 and 1 <= j <= 3:
                    sqr1.append((i, j))
                if 1 <= i <= 3 and 5 <= j <= 7:
                    sqr2.append((i, j))
                if 5 <= i <= 7 and 1 <= j <= 3:
                    sqr3.append((i, j))
                if 5 <= i <= 7 and 5 <= j <= 7:
                    sqr4.append((i, j))
        self.groups.append({"name": "nrc 1", "cells": sqr1})
        self.groups.append({"name": "nrc 2", "cells": sqr2})
        self.groups.append({"name": "nrc 3", "cells": sqr3})
        self.groups.append({"name": "nrc 4", "cells": sqr4})

    def getShowRaster(self):
        mx = super().getShowRaster()
        for x in range(0, 3):
            for y in range(0, 3):
                mx[x + 1, y + 1] = 2
                mx[x + 5, y + 1] = 2
                mx[x + 1, y + 5] = 2
                mx[x + 5, y + 5] = 2
        return mx
