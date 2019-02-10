package chav1961.fsyscommander.help;

import java.io.IOException;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.nanoservice.NanoServiceFactory;

public class HelpService extends NanoServiceFactory {

	public HelpService(final LoggerFacade facade, final SubstitutableProperties props) throws NullPointerException, IOException, ContentException, SyntaxException {
		super(facade, props);
		// TODO Auto-generated constructor stub
	}

}
