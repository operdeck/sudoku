package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.SquareGroup;

public class NRCPuzzle extends StandardPuzzle {

	public NRCPuzzle(String[] mySudoku) {
		super(mySudoku);
	}
	
	public NRCPuzzle(String name, String[] mySudoku) {
		super(name, mySudoku);
	}

	public NRCPuzzle(String row1, String row2, String row3,
				  String row4, String row5, String row6,
				  String row7, String row8, String row9) {
		super(row1,row2,row3,row4,row5,row6,row7,row8,row9);
	}

	public NRCPuzzle(String name,
				  String row1, String row2, String row3,
				  String row4, String row5, String row6,
				  String row7, String row8, String row9) {
		super(name, row1,row2,row3,row4,row5,row6,row7,row8,row9);
	}


	@Override
	public void initGroups() {
		super.initGroups();
		
		// add special 'NRC' groups
		int cnt=0;
		for (int y=0; y<2; y++) {
			for (int x=0; x<2; x++) {
				AbstractGroup newGrp = new SquareGroup(x*4+1, y*4+1, this, "NRC Group " + (++cnt));
				addGroup(newGrp);
//				System.out.println(newGrp);
			}
		}
	}

	protected IPuzzle newInstance(String name, String[] brd) {
		NRCPuzzle p = new NRCPuzzle(name, brd);
		p.previousPuzzle = this;
		return p;

	}
}
