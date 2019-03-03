package chav1961.fsyscommander;

import chav1961.fsyscommander.interfaces.FileContainer;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.ui.swing.terminal.PseudoConsole;
import chav1961.purelib.ui.swing.terminal.TermUtils;

public class ViewerAsTable extends PseudoConsole implements FileContainer {
	private static final long serialVersionUID = 4914841285660983985L;

	public ViewerAsTable() {
		super(40,25);
		TermUtils.box(this,1,1,getConsoleWidth(),getConsoleHeight(),TermUtils.LineStyle.Single);
		repaint();
	}

	@Override
	public boolean hasSelections() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<Content> totalContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Content> selectedContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderingMode getOrderingMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOrderingMode(OrderingMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OrderingDirection getOrderingDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOrderingDirection(OrderingDirection direction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getVisibility() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVisibility(boolean visibility) {
		// TODO Auto-generated method stub
		
	}
	
	
}
