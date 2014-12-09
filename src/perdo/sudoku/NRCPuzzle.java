package perdo.sudoku;

public class NRCPuzzle extends Puzzle {

	public NRCPuzzle(String[] mySudoku) {
		this(null, mySudoku);
	}
	
	public NRCPuzzle(String name, String[] mySudoku) {
		super(name);
		addStandardGroups(mySudoku);
		addNRCGroups(mySudoku);
		setSubAreas();
	}

	@Override
	public void reInit(String[] mySudoku) {
		addStandardGroups(mySudoku);
		addNRCGroups(mySudoku);
		setSubAreas();
	}
	
	protected void addNRCGroups(String[] mySudoku) {
		int cnt=0;
		for (int y=0; y<2; y++) {
			for (int x=0; x<2; x++) {
				Group newGrp = new SquareGroup(x*4+1, y*4+1, mySudoku, "NRC Group " + (++cnt));
				groups.add(newGrp);
//				System.out.println(newGrp);
			}
		}
	}
}
