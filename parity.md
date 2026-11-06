# Parity with the human-reasoning-puzzles engine

How the puzzles in this repository check out against the newer constraint
engine at [operdeck/human-reasoning-puzzles](https://github.com/operdeck/human-reasoning-puzzles)
(issue [#78](https://github.com/operdeck/human-reasoning-puzzles/issues/78)).
The question it answers: can anything the Java solver here solve stump the
new engine?

## Corpora

The Java solver draws on these sets:

| Corpus | Where | Puzzles | Role |
|---|---|---|---|
| `top95.txt` (MagicTour / Norvig) | `src/main/resources/` | 95 | the `main` default; **byte-identical** to the new repo's `norvig` corpus |
| Named boards | `PuzzleDB.java` | 15 | hard 9×9 and NRC boards the tests assert it solves |
| Python boards | `python/sudokupy/SudokuPuzzles.py` | 3 | two NRC, one standard |
| `HardestDatabase110626.txt` | `src/main/resources/` | 376 | community set this solver **cannot** crack (`HardestSudokuDatabaseReader`) |

Out of scope: the `LetterSudoku`, `SDoku` (snake regions) and `Sudoku10x10`
variants — the new engine models none of them.

## Findings

### The named and NRC boards: full parity

Reading the boards out of `PuzzleDB.java` and `SudokuPuzzles.py` gives 18
puzzles. One is a half-solved snapshot of another, so 17 are distinct. All 17
solve on the new engine, **every one by deduction** — none needs its `what_if`
fallback. Hardest step per board:

| Grade | Boards |
|---|---:|
| `single` | 7 |
| `subsumption` | 1 |
| `cover2` | 5 |
| `cover3` | 3 |
| `chains` | 1 |

This is the parity result that matters: apart from `top95`, these are the boards
the Java solver is exercised on, and the new engine keeps up.

The 17 were then imported into the new repository's Sudoku gallery as
`sudoku_9_9x9` … `sudoku_34_9x9` (five of them the NRC four-box variant), in
[operdeck/human-reasoning-puzzles#196](https://github.com/operdeck/human-reasoning-puzzles/pull/196).

### `top95`: the new engine is weaker on the hardest boards

On the shared `top95` the new engine still leaves about 35 of 95 boards to its
`what_if` (assume-and-refute) fallback, and two — Magic tour #9 and #75 — take
~23 s. The forcing chains here close those by deduction. That gap is tracked in
the new repository as issues
[#171](https://github.com/operdeck/human-reasoning-puzzles/issues/171) and
[#172](https://github.com/operdeck/human-reasoning-puzzles/issues/172).

### `HardestDatabase110626.txt`: out of scope

The Java solver does not crack any of these (per
`HardestSudokuDatabaseReader`), so there is nothing for the new engine to match.
It is not part of the comparison.

## Method

Each board was read straight from the Java/Python source, the NRC four-box
regions reconstructed from `NRCSudoku.createGroups`, and the resulting puzzle
run through the new engine's deduction ladder with an independent brute-force
solution as the oracle — a solve is credited only when it matches an answer the
ladder did not produce.
