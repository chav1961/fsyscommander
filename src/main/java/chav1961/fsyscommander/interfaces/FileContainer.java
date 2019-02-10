package chav1961.fsyscommander.interfaces;

public interface FileContainer {
	boolean hasSelections();

	public enum OrderingMode {
		NoOrdering,
		ByName,
		BySize,
		ByModificationDate
	}
	
	public enum OrderingDirection {
		Ascending, Descending, NoMatter
	}
	
	public interface Content {
		String getName();
		boolean isDirectory();
		boolean isFile();
		boolean isSelected();
		void setSelection(boolean selection);
	}
	
	Iterable<Content> totalContent();
	Iterable<Content> selectedContent();
	
	OrderingMode getOrderingMode();
	void setOrderingMode(OrderingMode mode);
	OrderingDirection getOrderingDirection();
	void setOrderingDirection(OrderingDirection direction);
	boolean getVisibility();
	void setVisibility(boolean visibility);
}
