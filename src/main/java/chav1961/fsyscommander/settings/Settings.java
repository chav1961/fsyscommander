package chav1961.fsyscommander.settings;

import chav1961.purelib.basic.interfaces.LoggerFacade;

public class Settings {
	public SystemSettings		systemSettings = new SystemSettings();
	public PanelSettings		panelSettings = new PanelSettings();
	public ViewerSettings		viewerSettings = new ViewerSettings(); 
	public EditorSettings		editorSettings = new EditorSettings(); 
	public HighlightSettings	highlightSettings = new HighlightSettings();
	public Confirms				confirms = new Confirms();

	private LoggerFacade		logger = null; 

	public Settings() {
	}	
	
	public LoggerFacade getLogger() {
		return logger;
	}
	
	public void setLogger(final LoggerFacade logger) {
		this.logger = logger;
		systemSettings.setLogger(logger);
		panelSettings.setLogger(logger);
		viewerSettings.setLogger(logger);
		editorSettings.setLogger(logger);
		highlightSettings.setLogger(logger);
		confirms.setLogger(logger);
	}
}
