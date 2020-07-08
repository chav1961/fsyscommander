package chav1961.fsyscommander.interfaces;

public interface Navigable {
	int getContainerSize();
	int getPageSize();
	int getCurrentLocation();
	Navigable lineDown();
	Navigable lineUp();
	Navigable pageDown();
	Navigable pageUp();
	Navigable toTop();
	Navigable toBottom();
}
