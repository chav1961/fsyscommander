package chav1961.fsyscommander.settings;

import chav1961.fsyscommander.interfaces.Resettable;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.Application/chav1961/fsyscommander/i18n/i18n.xml")
@LocaleResource(value="settings.panels",tooltip="settings.panels.tt")	
public class PanelSettings implements FormManager<Object,PanelSettings>, Cloneable, Resettable {
	private LoggerFacade		logger = null;
	
	@LocaleResource(value="settings.panels.columns",tooltip="settings.panels.columns.tt")	
	@Format("m")
	public PanelColumns		columns = PanelColumns.PanelColumn_1;
	@LocaleResource(value="settings.panels.allowFolderSelection",tooltip="settings.panels.allowFolderSelection.tt")	
	@Format("")
	public boolean			allowFolderSelection = false;
	@LocaleResource(value="settings.panels.showTitle",tooltip="settings.panels.showTitle.tt")	
	@Format("")
	public boolean			showColumnTitles = true;
	@LocaleResource(value="settings.panels.showState",tooltip="settings.panels.showState.tt")	
	@Format("")
	public boolean			showStateString = true;
	
	public PanelSettings() {
	}
	
	@Override
	public RefreshMode onField(PanelSettings inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
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
		if (clone instanceof PanelSettings) {
			final PanelSettings	templ = (PanelSettings)clone;
			
			this.columns = templ.columns;
			this.allowFolderSelection = templ.allowFolderSelection;
			this.showColumnTitles = templ.showColumnTitles;
			this.showStateString = templ.showStateString;
		}
	}

	void setLogger(final LoggerFacade logger) {
		this.logger = logger;
	}
}