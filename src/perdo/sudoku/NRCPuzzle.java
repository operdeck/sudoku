package perdo.sudoku;

public class NRCPuzzle extends Puzzle {

	public NRCPuzzle(String[] mySudoku) {
		this(null, mySudoku);
	}
	
	public NRCPuzzle(String name, String[] mySudoku) {
		super(name, mySudoku);
	}
	
	@Override
	protected void addGroups() {
		super.addGroups();
		
		// add special 'NRC' groups
		int cnt=0;
		for (int y=0; y<2; y++) {
			for (int x=0; x<2; x++) {
				Group newGrp = new SquareGroup(x*4+1, y*4+1, this, "NRC Group " + (++cnt));
				groups.add(newGrp);
//				System.out.println(newGrp);
			}
		}
	}
}
