package chav1961.fsyscommander.settings;

import java.awt.Color;

import chav1961.fsyscommander.interfaces.Resettable;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.json.ColorKeeper;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.Application/chav1961/fsyscommander/i18n/i18n.xml")
@LocaleResource(value="settings.highlights",tooltip="settings.highlights.tt")	
public class HighlightSettings implements FormManager<Object,HighlightSettings>, Cloneable, Resettable {
	private LoggerFacade		logger = null;

	@LocaleResource(value="settings.highlights.colorScheme",tooltip="settings.highlights.colorScheme.tt")	
	@Format("10m")
	public ApplicationColorScheme	scheme = ApplicationColorScheme.DARK;
	@LocaleResource(value="settings.highlights.directory",tooltip="settings.highlights.directory.tt")	
	@Format("10m")
	public ColorKeeper	directory = new ColorKeeper(Color.WHITE);
	@LocaleResource(value="settings.highlights.file",tooltip="settings.highlights.file.tt")	
	@Format("10m")
	public ColorKeeper	file = new ColorKeeper(Color.LIGHT_GRAY);
	@LocaleResource(value="settings.highlights.selectedDirectory",tooltip="settings.highlights.selectedDirectory.tt")	
	@Format("10m")
	public ColorKeeper	selectedDirectory = new ColorKeeper(Color.YELLOW);
	@LocaleResource(value="settings.highlights.selectedFile",tooltip="settings.highlights.selectedFile.tt")	
	@Format("10m")
	public ColorKeeper	selectedFile = new ColorKeeper(Color.YELLOW);
	
	@Override
	public RefreshMode onField(final HighlightSettings inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public Object copy() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public void reset(final Object clone) {
		if (clone instanceof HighlightSettings) {
			final HighlightSettings	templ = (HighlightSettings)clone;
			
			this.directory = templ.directory;
			this.file = templ.file;
			this.selectedDirectory = templ.selectedDirectory;
			this.selectedFile = templ.selectedFile;
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	void setLogger(final LoggerFacade logger) {
		this.logger = logger;
	}
}
