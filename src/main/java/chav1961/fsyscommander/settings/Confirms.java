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
@LocaleResource(value="settings.confirms",tooltip="settings.confirms.tt")	
public class Confirms implements FormManager<Object,Confirms>, Cloneable, Resettable {
	private LoggerFacade		logger = null;
	
	@LocaleResource(value="settings.confirms.onCopy",tooltip="settings.confirms.onCopy.tt")	
	@Format("")
	public boolean			onCopy = false;
	@LocaleResource(value="settings.confirms.onBulkCopy",tooltip="settings.confirms.onBulkCopy.tt")	
	@Format("")
	public boolean			onBulkCopy = true;
	@LocaleResource(value="settings.confirms.onMove",tooltip="settings.confirms.onMove.tt")	
	@Format("")
	public boolean			onMove = false;
	@LocaleResource(value="settings.confirms.onBulkMove",tooltip="settings.confirms.onBulkMove.tt")	
	@Format("")
	public boolean			onBulkMove = true;
	@LocaleResource(value="settings.confirms.onDelete",tooltip="settings.confirms.onDelete.tt")	
	@Format("")
	public boolean			onDelete = false;
	@LocaleResource(value="settings.confirms.onBulkDelete",tooltip="settings.confirms.onBulkDelete.tt")	
	@Format("")
	public boolean			onBulkDelete = true;
	@LocaleResource(value="settings.confirms.onExit",tooltip="settings.confirms.onExit.tt")	
	@Format("")
	public boolean			onExit = false;
	
	public Confirms() {
	}
	
	@Override
	public RefreshMode onField(Confirms inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
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
		if (clone instanceof Confirms) {
			final Confirms	templ = (Confirms)clone;
			
			this.onCopy = templ.onCopy;
			this.onBulkCopy = templ.onBulkCopy;
			this.onMove = templ.onMove;
			this.onBulkMove = templ.onBulkMove;
			this.onDelete = templ.onDelete;
			this.onBulkDelete = templ.onBulkDelete;
			this.onExit = templ.onExit;
		}
	}
	
	void setLogger(final LoggerFacade logger) {
		this.logger = logger;
	}
}