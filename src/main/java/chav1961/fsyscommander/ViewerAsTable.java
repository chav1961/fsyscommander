package chav1961.fsyscommander;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import chav1961.fsyscommander.interfaces.FileContainer;
import chav1961.purelib.ui.swing.terminal.PseudoConsole;
import chav1961.purelib.ui.swing.terminal.TermUtils;

public class ViewerAsTable extends PseudoConsole implements FileContainer {
	private static final long serialVersionUID = 4914841285660983985L;
	
	private static final Comparator<Content>[][]	CC;
	private static final Content					RET_CONTENT = new FileContent("..",true,0,0) {
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
		
		CC[OrderingMode.ByName.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
		CC[OrderingMode.ByName.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
		
		CC[OrderingMode.BySize.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
		CC[OrderingMode.BySize.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
		  
		CC[OrderingMode.ByModificationDate.ordinal()][OrderingDirection.Ascending.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
		CC[OrderingMode.ByModificationDate.ordinal()][OrderingDirection.Descending.ordinal()] = (o1,o2)->(o1.isDirectory() ? 0 : 1) - (o2.isDirectory() ? 0 : 1);
	}

	private final List<Content>		content = new ArrayList<>();
	private OrderingMode			orderingMode = OrderingMode.NoOrdering;
	private OrderingDirection		orderingDir = OrderingDirection.NoMatter;
	
	public ViewerAsTable() {
		super(40,25);
		TermUtils.box(this,1,1,getConsoleWidth(),getConsoleHeight(),TermUtils.LineStyle.Single);
		repaint();
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
		return getVisibility();
	}

	@Override
	public void setVisibility(boolean visibility) {
		setVisible(visibility);
	}

	public void clearContent() {
		content.clear();
		content.add(RET_CONTENT);
	}
	
	public void addContent(final String name, final boolean isDirectory, final long timestamp, final long size) {
		content.add(new FileContent(name,isDirectory,size,timestamp));
	}
	
	public void resortContent() {
		content.sort(CC[orderingMode.ordinal()][orderingDir.ordinal()]);
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
