package chav1961.fsyscommander.interfaces;

import chav1961.fsyscommander.interfaces.FileContainer.OrderingDirection;
import chav1961.fsyscommander.interfaces.FileContainer.OrderingMode;

public enum OrderingModes {
	BY_NAME_ASC(OrderingMode.ByName,OrderingDirection.Ascending), 
	BY_EXTENSION_ASC(OrderingMode.ByExtension,OrderingDirection.Ascending),
	BY_SIZE_ASC(OrderingMode.BySize,OrderingDirection.Ascending),
	BY_DATE_ASC(OrderingMode.ByModificationDate,OrderingDirection.Ascending),
	BY_NAME_DESC(OrderingMode.ByName,OrderingDirection.Descending),
	BY_EXTENSION_DESC(OrderingMode.ByExtension,OrderingDirection.Descending), 
	BY_SIZE_DESC(OrderingMode.BySize,OrderingDirection.Descending),
	BY_DATE_DESC(OrderingMode.ByModificationDate,OrderingDirection.Descending),
	UNORDERED(OrderingMode.NoOrdering,OrderingDirection.NoMatter);
	
	private final OrderingMode 		mode;
	private final OrderingDirection	dir;
	
	OrderingModes(final OrderingMode mode, final OrderingDirection dir) {
		this.mode = mode;
		this.dir = dir;
	}
	
	public OrderingMode getOrderingMode() {
		return mode;
	}

	public OrderingDirection getOrderingDirection() {
		return dir;
	}
}
