module chav1961.fsyscommander {
	requires chav1961.purelib;
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;

	opens chav1961.fsyscommander to chav1961.purelib;
	opens chav1961.fsyscommander.settings to chav1961.purelib;
}
