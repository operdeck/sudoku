
// As computer security expert Ben Laurie has stated, Sudoku is "a denial of service attack on human intellect"

public class Sudoku {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Puzzle NRC_5dec14 = new NRCPuzzle("NRC_5dec14", new String[] {
				"....65...",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." });

		Puzzle NRC_28dec = new NRCPuzzle("NRC_28dec", new String[] {
				".....2...",
				"..85..1.9",
				".......6.",
				"..39.....",
				".........",
				".....3...",
				".24..5...",
				".8.7.....",
				"...1....7" });
		
		Puzzle NRC_17nov = new NRCPuzzle("NRC_17nov", new String[] {
				".86...3..",
				"..95.....",
				"......1.8",
				"1.7.4.5..",
				"2........",
				"........9",
				"..41.....",
				".....5...",
				"........." });

		Puzzle Parool_18nov = new Puzzle("Parool_18nov", new String[] {
				"........8",
				"..9..2.7.",
				".64.38...",
				"1.7.6....",
				"..3...8..",
				"....2.7.3",
				"...48.36.",
				".5.9..2..",
				"9........" });
		
		Puzzle Trouw_535 = new Puzzle("Trouw_535", new String[] {
				"9 1357   ",
				"3        ",
				" 8   6  1",
				" 26 3 49 ",
				"  96 81  ",
				" 18 2 63 ",
				"1  5   8 ",
				"        3",
				"   1637 5" });
		
		Puzzle www_extremesudoku_info_evil = new Puzzle("www_extremesudoku_info_evil", new String[] {
				" 4  8 6  ",
				"  84    3",
				"2   1  8 ",
				"       5 ",
				"1 3 2 9 6",
				" 7       ",
				" 6  9   2",
				"9    15  ",
				"  5 3  1 "});
		
		Puzzle www_extremesudoku_info_evil_271113 = new Puzzle("www_extremesudoku_info_evil_271113", new String[] {
				"..1.9.5..",
				".5.4.3.1.",
				"9...8...6",
				".8.....3.",
				"5.2...9.4",
				".1.....7.",
				"3...2...1",
				".2.7.9.5.",
				"..4.1.6.."});
		
		Puzzle extreme_sudoku_extreme_28nov = new Puzzle("http://www.extremesudoku.info/sudoku.html Thursday, 28th November 2013", new String[] {
				" 1   9   ",
				"  4 7   1",
				"  2   98 ",
				"6  9 3   ",
				" 5  1  7 ",
				"   7 6  5",
				" 71   3  ",
				"5   2 8  ",
				"   3   6 "
		});
		
		Puzzle extreme_sudoku_extreme_10nov = new Puzzle("http://www.extremesudoku.info/sudoku.html Sunday, 10th November 2013", new String[] {
				"  89    2",
				" 2  7  8 ",
				"3    41  ",
				"6    92  ",
				" 5  4  9 ",
				"  25    7",
				"  56    3",
				" 1  3  6 ",
				"8    74  "
		});
		
		Puzzle p = 
//				null; // null means it will solve all
				NRC_5dec14
//				NRC_28dec
//				NRC_17nov
//				Parool_18nov
//				Trouw_535
//				www_extremesudoku_info_evil
//				www_extremesudoku_info_evil_271113
//				extreme_sudoku_extreme_28nov
//				extreme_sudoku_extreme_10nov
				;
		
		if (p == null) {
			int[] iterations = new int[Puzzle.all.size()];
			for (int idx=0; idx<Puzzle.all.size(); idx++) {
				p = Puzzle.all.get(idx);
				iterations[idx] = p.solve();
				if (!p.solved()) iterations[idx] = -iterations[idx];
			}
			for (int idx=0; idx<Puzzle.all.size(); idx++) {
				p = Puzzle.all.get(idx);
				if (iterations[idx] >= 0) {
					System.out.println("Puzzle " + p + " solved in " + iterations[idx]);
				} else {
					System.out.println("Puzzle " + p + " not solved. " + iterations[idx] + " iterations done.");
				}
			}
		} else {
			int iterations = p.solve();
			
			if (p.solved()) {
				System.out.println("Puzzle " + p + " solved in " + iterations + " iterations.");
			} else {
				System.out.println("Puzzle " + p + " not solved. " + iterations + " iterations done.");
			}
		}
	}
}
