package perdo.sudoku;

// As computer security expert Ben Laurie has stated, Sudoku is "a denial of service attack on human intellect"

public class Sudoku {

	public static void main(String[] args) {
		Puzzle p = new NRCPuzzle("NRC_5dec14", new String[] {
				"....65...",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." });
		
		int iterations = p.solve();
		
		if (p.solved()) {
			System.out.println("Puzzle " + p + " solved in " + iterations + " iterations.");
		} else {
			System.out.println("Puzzle " + p + " not solved. " + iterations + " iterations done.");
		}
	}
}
