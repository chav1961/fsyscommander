package chav1961.fsyscommander.settings;

import chav1961.fsyscommander.interfaces.Resettable;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.FileKeeper;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.Application/chav1961/fsyscommander/i18n/i18n.xml")
@LocaleResource(value="settings.viewer",tooltip="settings.viewer.tt")	
public class ViewerSettings implements FormManager<Object,ViewerSettings>, Cloneable, Resettable {
	private LoggerFacade		logger = null;
	
	@LocaleResource(value="settings.viewer.type",tooltip="settings.viewer.type.tt")	
	@Format("m")
	public ViewAndEditorType	type = ViewAndEditorType.INTERNAL; 
	@LocaleResource(value="settings.viewer.file",tooltip="settings.viewer.file.tt")	
	@Format("30s")
	public FileKeeper			externalFile = new FileKeeper("./");
	@LocaleResource(value="settings.viewer.clone",tooltip="settings.viewer.clone.tt")	
	@Format("1")
	public boolean				cloneContent = true;
	
	public ViewerSettings() {
	}

	@Override
	public RefreshMode onField(final ViewerSettings inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}
	
	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	@Override
	public Object copy() throws CloneNotSupportedException {
		return super.clone();
	} 

	@Override
	public void reset(final Object clone) {
		if (clone instanceof ViewerSettings) {
			final ViewerSettings	templ = (ViewerSettings)clone;

			this.type = templ.type;
			this.externalFile = templ.externalFile;
			this.cloneContent = templ.cloneContent;
		}
	}

	void setLogger(final LoggerFacade logger) {
		this.logger = logger;
	}
}
