package chav1961.fsyscommander;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.KeyStroke;

import chav1961.fsyscommander.interfaces.Navigable;
import chav1961.fsyscommander.interfaces.OKCallback;
import chav1961.fsyscommander.interfaces.FileContainer.Content;
import chav1961.fsyscommander.settings.HighlightSettings;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.terminal.PseudoConsole;
import chav1961.purelib.ui.swing.terminal.TermUtils;

public class FileViewer extends PseudoConsole implements Navigable {
	private static final long serialVersionUID = -1376971596509593925L;
	
	private final HighlightSettings	settings;
	private char[][]				content = null;
	private int						startIndex = 0, focusedIndex = 0, startDispl = 0, maxDispl = 0;
	
	public FileViewer(final HighlightSettings settings, final OKCallback callback) {
		super(80,25);
		if (settings == null) {
			throw new NullPointerException("Highlight settings can't be null");
		}
		else {
			this.settings = settings;
			setFocusable(true);
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0), (e)->lineDown(),"down");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_UP,0), (e)->lineUp(),"up");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0), (e)->pageDown(),"pgDown");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0), (e)->pageUp(),"pgUp");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,KeyEvent.CTRL_DOWN_MASK), (e)->toBottom(),"last");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,KeyEvent.CTRL_DOWN_MASK), (e)->toTop(),"first");

			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0), (e)->colLeft(),"left");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0), (e)->colRight(),"right");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,KeyEvent.CTRL_DOWN_MASK), (e)->colTabLeft(),"tabLeft");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,KeyEvent.CTRL_DOWN_MASK), (e)->colTabRight(),"tabRight");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_HOME,0), (e)->colHome(),"home");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_END,0), (e)->colEnd(),"end");
			
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), (e)->callback.processOK(),"exit");
			addFocusListener(new FocusListener() {
				@Override public void focusLost(FocusEvent e) {}
				@Override public void focusGained(FocusEvent e) {redrawContent();}
			});
		}
	}

	@Override
	public int getContainerSize() {
		return content.length;
	}

	@Override
	public int getPageSize() {
		return getConsoleHeight()-2;
	}
	
	@Override
	public int getCurrentLocation() {
		return focusedIndex;
	}

	
	@Override
	public Navigable lineDown() {
		setCurrentLocation(getCurrentLocation()+1);
		return this;
	}

	@Override
	public Navigable lineUp() {
		setCurrentLocation(getCurrentLocation()-1);
		return this;
	}

	@Override
	public Navigable pageDown() {
		setCurrentLocation(getCurrentLocation()+getPageSize());
		return this;
	}

	@Override
	public Navigable pageUp() {
		setCurrentLocation(getCurrentLocation()-getPageSize());
		return this;
	}

	@Override
	public Navigable toTop() {
		setCurrentLocation(0);
		return this;
	}

	@Override
	public Navigable toBottom() {
		setCurrentLocation(getContainerSize()-1);
		return this;
	}

	private void setCurrentLocation(final int newFocusedIndex) {
		final int	effectiveFocusedIndex;
		
		if (newFocusedIndex < 0) {
			effectiveFocusedIndex = 0;
		}
		else if (newFocusedIndex >= getContainerSize()-1) {
			effectiveFocusedIndex = getContainerSize()-1;
		}
		else {
			effectiveFocusedIndex = newFocusedIndex;
		}
		if (effectiveFocusedIndex != focusedIndex) {
			focusedIndex = effectiveFocusedIndex;
			if (focusedIndex < startIndex) {
				startIndex = focusedIndex;
			}
			else if (focusedIndex >= startIndex+getPageSize()) {
				startIndex = Math.max(0,focusedIndex-getPageSize()+1);
			}
			redrawContent();
		}
	}

	private void colLeft() {
		setCurrentDispl(getCurrentDispl()-1);
	}

	private void colRight() {
		setCurrentDispl(getCurrentDispl()+1);
	}

	private void colTabLeft() {
		setCurrentDispl(getCurrentDispl()-getTabSize());
	}

	private void colTabRight() {
		setCurrentDispl(getCurrentDispl()+getTabSize());
	}

	private void colHome() {
		setCurrentDispl(0);
	}

	private void colEnd() {
		setCurrentDispl(getColSize());
	}
	
	private int getCurrentDispl() {
		return startDispl;
	}
	
	private int getTabSize() {
		return 8;
	}
	
	private int getColSize() {
		return maxDispl;
	}
	
	private void setCurrentDispl(final int newDispl) {
		final int	effectiveDispl;
		
		if (newDispl < 0) {
			effectiveDispl = 0;
		}
		else if (newDispl > getColSize()-1) {
			effectiveDispl = getColSize()-1;
		}
		else {
			effectiveDispl = newDispl;
		}
		if (startDispl != effectiveDispl) {
			startDispl = effectiveDispl;	
			redrawContent();
		}
	}
	
	public void loadContent(final InputStream is) throws IOException {
		final List<char[]>			content = new ArrayList<>();
		final Reader				rdr = new InputStreamReader(is);
		
		try(final LineByLineProcessor lblp = new LineByLineProcessor((displacement,lineNo,data,from,length)-> {
										content.add(Arrays.copyOfRange(data,from,from+length));
										maxDispl = Math.max(maxDispl,length);
									})) {
			try{lblp.write(rdr);
			} catch (SyntaxException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
		this.content = content.toArray(new char[content.size()][]);
		content.clear();
		startIndex = 0;
		focusedIndex = 0;
		startDispl = 0;
		maxDispl = Math.max(0,maxDispl-getConsoleWidth()+1);
		redrawContent();
	}

	private void redrawContent() {
		fillVisualContent();
		repaint();
	}
	
	private void fillVisualContent() {
		TermUtils.clear(this,settings.scheme.getForeGroundColor(),settings.scheme.getBackGroundColor());
		TermUtils.box(this,1,1,getConsoleWidth(),getConsoleHeight(),TermUtils.LineStyle.Single);
		
		for (int y = 0, maxY = Math.min(content.length-startIndex,getConsoleHeight()-2); y < maxY; y++) {
			final char[]	name = content[startIndex+y];
			final Color		color = settings.scheme.getForeGroundColor();
			final Color		bgColor = isFocusOwner() && startIndex+y == focusedIndex ? settings.scheme.getSelectedBackGroundColor() : settings.scheme.getBackGroundColor();
			
			for (int x = 1, maxX = Math.min(getConsoleWidth()-2,Math.max(0,name.length-getCurrentDispl())); x <= maxX; x++) {
				writeAttribute(x+1,y+2,color,bgColor);
				writeContent(x+1,y+2,name[x-1+getCurrentDispl()]);
			}
		}
	}
	
}
