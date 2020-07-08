package chav1961.fsyscommander;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.KeyStroke;

import chav1961.fsyscommander.interfaces.FileContainer;
import chav1961.fsyscommander.interfaces.Navigable;
import chav1961.fsyscommander.settings.HighlightSettings;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.terminal.PseudoConsole;
import chav1961.purelib.ui.swing.terminal.TermUtils;

public class ViewerAsTable extends PseudoConsole implements FileContainer {
	private static final long serialVersionUID = 4914841285660983985L;

	private static final String						PARENT_DIR = "..";
	private static final Comparator<Content>[][]	CC;
	private static final Content					RET_CONTENT = new FileContent(PARENT_DIR,true,0,0) {
																	public void setSelection(final boolean selection) {}
																}; 
	
	static {
		CC = new Comparator[OrderingMode.values().length][OrderingDirection.values().length];

		CC[OrderingMode.ByName.ordinal()][OrderingDirection.NoMatter.ordinal()] =
		CC[OrderingMode.BySize.ordinal()][OrderingDirection.NoMatter.ordinal()] =
		CC[OrderingMode.ByModificationDate.ordinal()][OrderingDirection.NoMatter.ordinal()] =
		CC[OrderingMode.NoOrdering.ordinal()][OrderingDirection.Ascending.ordinal()] =
		CC[OrderingMode.NoOrdering.ordinal()][OrderingDirection.Descending.ordinal()] =
		CC[OrderingMode.NoOrdering.ordinal()][OrderingDirection.NoMatter.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
		
		CC[OrderingMode.ByName.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)-> {
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				if (PARENT_DIR.equals(o1.getName())) {
					return -1;
				}
				else if (PARENT_DIR.equals(o2.getName())) {
					return 1;
				}
				else {
					return o1.getName().compareTo(o2.getName());
				}
			}
			else {
				return dir;
			}
		};
		CC[OrderingMode.ByName.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				if (PARENT_DIR.equals(o1.getName())) {
					return -1;
				}
				else if (PARENT_DIR.equals(o2.getName())) {
					return 1;
				}
				else {
					return -o1.getName().compareTo(o2.getName());
				}
			}
			else {
				return dir;
			}
		};

		CC[OrderingMode.ByExtension.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				if (PARENT_DIR.equals(o1.getName())) {
					return -1;
				}
				else if (PARENT_DIR.equals(o2.getName())) {
					return 1;
				}
				else {
					return o1.getName().compareTo(o2.getName());
				}
			}
			else {
				return dir;
			}
		};
		CC[OrderingMode.ByExtension.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				if (PARENT_DIR.equals(o1.getName())) {
					return -1;
				}
				else if (PARENT_DIR.equals(o2.getName())) {
					return 1;
				}
				else {
					return -o1.getName().compareTo(o2.getName());
				}
			}
			else {
				return dir;
			}
		};
		
		CC[OrderingMode.BySize.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				final long	delta = o1.getSize() - o2.getSize();
				
				return delta < 0 ? -1 : delta > 0 ? 1 : 0;
			}
			else {
				return dir;
			}
		};
		CC[OrderingMode.BySize.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				final long	delta = o1.getSize() - o2.getSize();
				
				return delta < 0 ? 1 : delta > 0 ? -1 : 0;
			}
			else {
				return dir;
			}
		};
		  
		CC[OrderingMode.ByModificationDate.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				final long	delta = o1.getTimestamp() - o2.getTimestamp();
				
				return delta < 0 ? -1 : delta > 0 ? 1 : 0;
			}
			else {
				return dir;
			}
		};
		CC[OrderingMode.ByModificationDate.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->{
			final int	dir = (o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
			
			if (dir == 0) {
				final long	delta = o1.getTimestamp() - o2.getTimestamp();
				
				return delta < 0 ? 1 : delta > 0 ? -1 : 0;
			}
			else {
				return dir;
			}
		};
	}

	@FunctionalInterface
	public interface ViewerCallback {
		void processItem(Content item) throws Exception;
	}
	
	private final List<Content>		content = new ArrayList<>();
	private final HighlightSettings	settings;
	private OrderingMode			orderingMode = OrderingMode.NoOrdering;
	private OrderingDirection		orderingDir = OrderingDirection.NoMatter;
	private int						startIndex = 0, focusedIndex = 0;
	
	public ViewerAsTable(final HighlightSettings settings, final ViewerCallback callback) {
		super(40,25);
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
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,KeyEvent.CTRL_DOWN_MASK), (e)->toBottom(),"end");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,KeyEvent.CTRL_DOWN_MASK), (e)->toTop(),"home");
			
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), (e)->{
				try {
					callback.processItem(currentContent());
				} catch (Exception e1) {
				}
			},"enter");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0), (e)->{
				currentContent().setSelection(!currentContent().isSelected());
				lineDown();
				redrawContent();
			},"select");
			
			addFocusListener(new FocusListener() {
				@Override public void focusLost(FocusEvent e) {redrawContent();}
				@Override public void focusGained(FocusEvent e) {redrawContent();}
			});
		}
	}

	@Override
	public boolean hasSelections() {
		for (Content item : totalContent()) {
			if (item.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterable<Content> totalContent() {
		return content;
	}

	@Override
	public Iterable<Content> selectedContent() {
		return new Iterable<Content>() {
			@Override
			public Iterator<Content> iterator() {
				return new Iterator<Content>() {
					int		index = 0;
					
					@Override
					public boolean hasNext() {
						while (index < content.size()) {
							if (content.get(index).isSelected()) {
								return true;
							}
							else {
								index++;
							}
						}
						return false;
					}

					@Override
					public Content next() {
						return content.get(index++);
					}
				};
			}
		};
	}

	@Override
	public Content currentContent() {
		return content.get(focusedIndex);
	}
	
	@Override
	public OrderingMode getOrderingMode() {
		return orderingMode;
	}

	@Override
	public void setOrderingMode(final OrderingMode mode) {
		this.orderingMode = mode;
		resortContent();
	}

	@Override
	public OrderingDirection getOrderingDirection() {
		return orderingDir;
	}

	@Override
	public void setOrderingDirection(final OrderingDirection direction) {
		this.orderingDir = direction;
		resortContent();
	}

	@Override
	public boolean getVisibility() {
		return isVisible();
	}

	@Override
	public void setVisibility(boolean visibility) {
		setVisible(visibility);
	}

	@Override
	public int getContainerSize() {
		return content.size();
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
	
	public void clearContent() {
		content.clear();
		content.add(RET_CONTENT);
	}
	
	public void addContent(final String name, final boolean isDirectory, final long timestamp, final long size) {
		content.add(new FileContent(name,isDirectory,size,timestamp));
	}
	
	public void resortContent() {
		startIndex = 0;
		focusedIndex = 0;
		content.sort(CC[orderingMode.ordinal()][orderingDir.ordinal()]);
		repaint();
	}

	public void fillContent(final FileSystemInterface fsi) throws IOException {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else {
			clearContent();
			try(final FileSystemInterface			fs = fsi.clone()) {
				for (String item : fs.list()) {
					try(final FileSystemInterface	child = fs.clone().open(item)) {
						addContent(item,child.isDirectory(),child.lastModified(),child.size());
					}
				}
			}
			resortContent();
			redrawContent();
		}
	}

	private void fillVisualContent() {
		TermUtils.clear(this,settings.scheme.getForeGroundColor(),settings.scheme.getBackGroundColor());
		TermUtils.box(this,1,1,getConsoleWidth(),getConsoleHeight(),TermUtils.LineStyle.Single);
		
		for (int y = 0, maxY = Math.min(content.size()-startIndex,getConsoleHeight()-1); y < maxY; y++) {
			final Content	item = content.get(startIndex+y);
			final char[]	name = item.getName().toCharArray();
			final Color		color = item.isDirectory() ? (item.isSelected() ? settings.selectedDirectory.toColor() : settings.directory.toColor()) : (item.isSelected() ? settings.selectedFile.toColor() : settings.file.toColor());
			final Color		bgColor = isFocusOwner() && startIndex+y == focusedIndex ? settings.scheme.getSelectedBackGroundColor() : settings.scheme.getBackGroundColor();
			
			for (int x = 1, maxX = Math.min(getConsoleWidth()-2,name.length); x <= maxX; x++) {
				writeAttribute(x+1,y+2,color,bgColor);
				writeContent(x+1,y+2,name[x-1]);
			}
		}
	}

	private void redrawContent() {
		fillVisualContent();
		repaint();
	}

	private void setCurrentLocation(final int newFocusedIndex) {
		final int	effectiveFocusedIndex;
		
		if (newFocusedIndex < 0) {
			effectiveFocusedIndex = 0;
		}
		else if (newFocusedIndex > getContainerSize()-1) {
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
			else if (focusedIndex > startIndex+getPageSize()) {
				startIndex = Math.max(0,focusedIndex-getPageSize());
			}
			redrawContent();
		}
	}
	
	
	private static class FileContent implements Content {
		final String	name;
		final boolean	isDirectory;
		final long		size;
		final long		timestamp;

		boolean			mark = false;
		
		public FileContent(String name, boolean isDirectory, long size, long timestamp) {
			this.name = name;
			this.isDirectory = isDirectory;
			this.size = size;
			this.timestamp = timestamp;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isDirectory() {
			return isDirectory;
		}

		@Override
		public boolean isSelected() {
			return mark;
		}

		@Override
		public void setSelection(final boolean selection) {
			this.mark = selection;
		}
		
		@Override
		public long getSize() {
			return size;
		}

		@Override
		public long getTimestamp() {
			return timestamp;
		}

		@Override
		public String toString() {
			return "FileContent [name=" + name + ", isDirectory=" + isDirectory + ", size=" + size + ", timestamp=" + timestamp + "]";
		}
	}
}
