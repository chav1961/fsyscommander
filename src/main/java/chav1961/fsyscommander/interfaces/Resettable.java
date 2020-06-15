package chav1961.fsyscommander.interfaces;

public interface Resettable {
	Object copy() throws CloneNotSupportedException;
	void reset(Object clone);
}
