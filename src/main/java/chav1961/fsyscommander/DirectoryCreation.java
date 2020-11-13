package chav1961.fsyscommander;


import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.DirectoryCreation/chav1961/fsyscommander/i18n/i18n.xml")
@LocaleResource(value="directorycreation.title",tooltip="directorycreation.title.tt")	
public class DirectoryCreation implements FormManager<Object,DirectoryCreation> {
	private LoggerFacade	logger = null;
	
	@LocaleResource(value="directorycreation.name",tooltip="directorycreation.name.tt")	
	@Format("30ms")
	public String			name = "";
	
	public DirectoryCreation(final LoggerFacade logger) {
		this.logger = logger;
	}
	
	@Override
	public RefreshMode onField(final DirectoryCreation inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}
	
	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
}
