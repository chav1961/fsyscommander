package chav1961.fsyscommander;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.AbstractLocalizer.SupportedLanguages;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.interfacers.RefreshMode;

public class Settings {
	public enum PanelColumns {
		PanelColumn_1,
		PanelColumns_2,
		PanelColumns_3,
	}
	
	private final LoggerFacade	logger; 
	public SystemSettings		systemSettings = new SystemSettings();
	public PanelSettings		panelSettings = new PanelSettings();
	public Confirms				confirms = new Confirms();
	
	public Settings(final LoggerFacade logger) {
		this.logger = logger;
	}

	@LocaleResourceLocation("i18n:prop:chav1961/fsyscommander/i18n/i18n")
	@LocaleResource(value="settings.system",tooltip="settings.system.tt")	
	public class SystemSettings implements FormManager<Object,SystemSettings> {
		@LocaleResource(value="settings.system.currentLang",tooltip="settings.system.currentLang.tt")	
		@Format("")
		SupportedLanguages	lang = SupportedLanguages.ru; 
		@LocaleResource(value="settings.system.upperCaseOnCreation",tooltip="settings.system.upperCaseOnCreation.tt")	
		@Format("")
		boolean		upperCaseOnCreation = false;
		@LocaleResource(value="settings.system.saveCommandHistory",tooltip="settings.system.saveCommandHistory.tt")	
		@Format("")
		boolean		saveCommandHistory = false;
		@LocaleResource(value="settings.system.useMimeTypes",tooltip="settings.system.useMimeTypes.tt")	
		@Format("")
		boolean		useMimeTypes = false;
		@LocaleResource(value="settings.system.autoSave",tooltip="settings.system.autoSave.tt")	
		@Format("")
		boolean		autoSaveSettings = false;
		
		@Override
		public RefreshMode onField(SystemSettings inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
			// TODO Auto-generated method stub
			return RefreshMode.DEFAULT;
		}

		@Override
		public LoggerFacade getLogger() {
			return logger;
		}
	}
	
	@LocaleResourceLocation("i18n:prop:chav1961/fsyscommander/i18n/i18n")
	@LocaleResource(value="settings.panels",tooltip="settings.panels.tt")	
	public class PanelSettings implements FormManager<Object,PanelSettings> {
		@LocaleResource(value="settings.panels.columns",tooltip="settings.panels.columns.tt")	
		@Format("")
		PanelColumns	columns = PanelColumns.PanelColumn_1;
		@LocaleResource(value="settings.panels.allowFolderSelection",tooltip="settings.panels.allowFolderSelection.tt")	
		@Format("")
		boolean			allowFolderSelection = false;
		@LocaleResource(value="settings.panels.showTitle",tooltip="settings.panels.showTitle.tt")	
		@Format("")
		boolean			showColumnTitles = true;
		@LocaleResource(value="settings.panels.showState",tooltip="settings.panels.showState.tt")	
		@Format("")
		boolean			showStateString = true;
		
		@Override
		public RefreshMode onField(PanelSettings inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
			// TODO Auto-generated method stub
			return RefreshMode.DEFAULT;
		}
		
		@Override
		public LoggerFacade getLogger() {
			return logger;
		}
	}
	
	@LocaleResourceLocation("i18n:prop:chav1961/fsyscommander/i18n/i18n")
	@LocaleResource(value="settings.confirms",tooltip="settings.confirms.tt")	
	public class Confirms implements FormManager<Object,Confirms> {
		@LocaleResource(value="settings.confirms.onCopy",tooltip="settings.confirms.onCopy.tt")	
		@Format("")
		boolean		onCopy = false;
		@LocaleResource(value="settings.confirms.onBulkCopy",tooltip="settings.confirms.onBulkCopy.tt")	
		@Format("")
		boolean		onBulkCopy = true;
		@LocaleResource(value="settings.confirms.onMove",tooltip="settings.confirms.onMove.tt")	
		@Format("")
		boolean		onMove = false;
		@LocaleResource(value="settings.confirms.onBulkMove",tooltip="settings.confirms.onBulkMove.tt")	
		@Format("")
		boolean		onBulkMove = true;
		@LocaleResource(value="settings.confirms.onExit",tooltip="settings.confirms.onExit.tt")	
		@Format("")
		boolean		onExit = false;
		
		@Override
		public RefreshMode onField(Confirms inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
			// TODO Auto-generated method stub
			return RefreshMode.DEFAULT;
		}
		
		@Override
		public LoggerFacade getLogger() {
			return logger;
		}
	}
}
