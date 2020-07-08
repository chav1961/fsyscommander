package chav1961.fsyscommander.interfaces;

public interface FileContainer extends Navigable {
	boolean hasSelections();

	public enum OrderingMode {
		NoOrdering,
		ByName,
		ByExtension,
		BySize,
		ByModificationDate
	}
	
	public enum OrderingDirection {
		Ascending, Descending, NoMatter
	}
	
	public interface Content {
		String getName();
		boolean isDirectory();
		long getSize();
		long getTimestamp();
		boolean isSelected();
		void setSelection(boolean selection);
	}
	
	Iterable<Content> totalContent();
	Iterable<Content> selectedContent();
	
	Content currentContent();
	
	OrderingMode getOrderingMode();
	void setOrderingMode(OrderingMode mode);
	OrderingDirection getOrderingDirection();
	void setOrderingDirection(OrderingDirection direction);
	boolean getVisibility();
	void setVisibility(boolean visibility);
}
