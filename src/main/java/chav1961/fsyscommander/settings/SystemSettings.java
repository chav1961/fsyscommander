package chav1961.fsyscommander.settings;


import chav1961.fsyscommander.interfaces.Resettable;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.Application/chav1961/fsyscommander/i18n/i18n.xml")
@LocaleResource(value="settings.system",tooltip="settings.system.tt")	
public class SystemSettings implements FormManager<Object,SystemSettings>, Cloneable, Resettable {
	private LoggerFacade		logger = null;
	
	@LocaleResource(value="settings.system.currentLang",tooltip="settings.system.currentLang.tt")	
	@Format("m")
	public SupportedLanguages	lang = SupportedLanguages.ru; 
	@LocaleResource(value="settings.system.upperCaseOnCreation",tooltip="settings.system.upperCaseOnCreation.tt")	
	@Format("")
	public boolean				upperCaseOnCreation = false;
	@LocaleResource(value="settings.system.saveCommandHistory",tooltip="settings.system.saveCommandHistory.tt")	
	@Format("")
	public boolean				saveCommandHistory = false;
	@LocaleResource(value="settings.system.useMimeTypes",tooltip="settings.system.useMimeTypes.tt")	
	@Format("")
	public boolean				useMimeTypes = false;
	@LocaleResource(value="settings.system.autoSave",tooltip="settings.system.autoSave.tt")	
	@Format("")
	public boolean				autoSaveSettings = false;
	
	public SystemSettings() {
	}
	
	@Override
	public RefreshMode onField(final SystemSettings inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
		if (clone instanceof SystemSettings) {
			final SystemSettings	templ = (SystemSettings)clone;
			
			this.lang = templ.lang;
			this.upperCaseOnCreation = templ.upperCaseOnCreation;
			this.saveCommandHistory = templ.saveCommandHistory;
			this.useMimeTypes = templ.useMimeTypes;
			this.autoSaveSettings = templ.autoSaveSettings;
		}
	}

	void setLogger(final LoggerFacade logger) {
		this.logger = logger;
	}
}