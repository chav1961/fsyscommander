package chav1961.fsyscommander;



import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import chav1961.fsyscommander.help.HelpService;
import chav1961.fsyscommander.interfaces.FileContainer;
import chav1961.fsyscommander.interfaces.FileContainer.Content;
import chav1961.fsyscommander.interfaces.OrderingModes;
import chav1961.fsyscommander.interfaces.Resettable;
import chav1961.fsyscommander.settings.Settings;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.json.JsonSerializer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileSystemChanger;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

public class Application extends JFrame implements LocaleChangeListener {
	private static final long 		serialVersionUID = -8313830909078065332L;
	
	public static final String		ARG_PORT = "port";
	public static final String		ARG_ROOT = "root";

	private static final String		SETTINGS_NAME = ".fsyscommander";
	
	private static final String		APPLICATION_TITLE = "Application.title";
	private static final String		HELP_TITLE = "Application.help.title";
	private static final String		HELP_CONTENT = "Application.help.content";
	private static final Icon[]		AVATAR = {null, null, null, new ImageIcon(Application.class.getResource("avatar.jpg"))};

	private static final String		CONFIRMATION_TITLE = "Confirmation.title";
	private static final String		CONFIRMATION_EXIT = "Confirmation.exit";
	
	private static final String		TAB_PANEL = "panel";
	private static final String		TAB_VIEWER = "viewer";
	private static final String		TAB_EDITOR = "editor";

	private static final int		STATE_ORDINAL = 0;
	private static final int		STATE_IN_VIEW = 1;
	private static final int		STATE_IN_EDIT = 2;

	private static final FormManager<Object,Object>	DUMMY_MGR = new FormManager<Object,Object>() {
										@Override
										public RefreshMode onRecord(Action action, Object oldRecord, Object oldId, Object newRecord, Object newId) throws FlowException, LocalizationException {
											return RefreshMode.DEFAULT;
										}
								
										@Override
										public RefreshMode onField(Object inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
											return RefreshMode.DEFAULT;
										}
								
										@Override
										public RefreshMode onAction(Object inst, Object id, String actionName, Object parameter) throws FlowException, LocalizationException {
											return RefreshMode.DEFAULT;
										}
								
										@Override
										public LoggerFacade getLogger() {
											return null;
										}
									};

	@FunctionalInterface
	private interface SettingsCallback {
		void processOK();
	}
									
	
	private final Localizer			localizer;
	private final CountDownLatch	latch;
	private final Settings			settings;
	private final JsonSerializer<Settings>	ser; 
	private final InetSocketAddress	addr;
	private final JStateString		state;
	private final JPanel			content = new JPanel(new BorderLayout());
	private final ConsoleOutput		console = new ConsoleOutput();		 
	private final JPanel			screen = new JPanel(new CardLayout());
	private final JPanel			container = new JPanel(new GridLayout(1,2));
	private final FileViewer		viewer = new FileViewer();	
	private final FileEditor		editor = new FileEditor();	
	private final CommandString		commandString = new CommandString(console);
	private final JPanel			bottomArea = new JPanel(new GridLayout(2,1));
	private final JMenuBar			menu;
	private final FileContainer		leftContainer = new ViewerAsTable();
	private final FileContainer		rightContainer = new ViewerAsTable();
	
	private int						currentState = STATE_ORDINAL;
	private FileSystemInterface		leftFsi = null, rightFsi = null;
	private FileContainer			currentContainer = leftContainer;
	private String					selectionTemplate = "*.*";
	private OrderingModes			leftOrdering = OrderingModes.BY_NAME_ASC, rightOrdering = OrderingModes.BY_NAME_ASC;  
	
	public Application(final Localizer parent, final ContentMetadataInterface model, final CountDownLatch latch, final InetSocketAddress addr) throws IOException, LocalizationException {
		this.latch = latch;
		this.addr = addr;
		this.localizer = LocalizerFactory.getLocalizer(model.getRoot().getLocalizerAssociated());
		this.state = new JStateString(this.localizer);
		this.state.setBorder(new LineBorder(Color.BLACK));
		this.menu = SwingUtils.toJComponent(model.byUIPath(URI.create(ContentMetadataInterface.UI_SCHEME+":/model/navigation.top.mainmenu")),JMenuBar.class);

		try{this.ser = JsonSerializer.buildSerializer(Settings.class);
		} catch (EnvironmentException exc) {
			state.message(Severity.error,exc,"Error on builing JSON serializator: "+exc.getMessage());
			throw new IOException(exc);
		}

		final File	settingsFile = new File(SETTINGS_NAME);
		
		if (settingsFile.exists() && settingsFile.isFile() && settingsFile.canRead()) {
			try(final InputStream		is = new FileInputStream(settingsFile);
				final Reader			rdr = new InputStreamReader(is,"UTF-8");
				final JsonStaxParser	pars = new JsonStaxParser(rdr)) {
				
				pars.next();
				
				try{this.settings = ser.deserialize(pars);
				} catch (ContentException exc) {
					state.message(Severity.error,exc,"Error on loading previous settings: "+exc.getMessage());
					throw new IOException(exc);
				}
			}
		}
		else {
			this.settings = new Settings();
		}
		this.settings.setLogger(state);
		
		parent.push(localizer);
		content.setOpaque(true);
		setContentPane(content);
		
		((JComponent)leftContainer).setBorder(new LineBorder(Color.WHITE));
		container.add(((JComponent)leftContainer));
		((JComponent)rightContainer).setBorder(new LineBorder(Color.WHITE));
		container.add(((JComponent)rightContainer));
        container.setOpaque(false);
        
		bottomArea.add(commandString);
		bottomArea.add(state);

		screen.add(container,TAB_PANEL);
		screen.add(viewer,TAB_VIEWER);
		screen.add(editor,TAB_EDITOR);
		((CardLayout)screen.getLayout()).show(screen,TAB_PANEL);
        screen.setOpaque(false);

        final JLayeredPane layeredPane = new JLayeredPane();
        
        layeredPane.add(console, new Integer(0));
        layeredPane.add(screen, new Integer(1));
        layeredPane.addComponentListener(new ComponentListener() {
			@Override public void componentShown(ComponentEvent e) {}
			@Override public void componentMoved(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
			
			@Override
			public void componentResized(ComponentEvent e) {
				final Rectangle	rect = layeredPane.getBounds();
				
		        screen.setBounds(0,0,rect.width,rect.height);
		        console.setBounds(0,0,rect.width,rect.height);
			}
		});
		
		getContentPane().add(menu,BorderLayout.NORTH);
		getContentPane().add(layeredPane,BorderLayout.CENTER);
		getContentPane().add(bottomArea,BorderLayout.SOUTH);
		
		SwingUtils.assignActionListeners(this.menu,this);
		SwingUtils.assignExitMethod4MainWindow(this,()->{fileExit();});
		SwingUtils.centerMainWindow(this,0.75f);
		localizer.addLocaleChangeListener(this);
		fillLocalizedStrings();
		console.println("preved!");
		pack();
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	/*
	 * Left panel
	 */
	
	@OnAction("action:/left.asTable")
	private void leftAsTable() {
	}
	
	@OnAction("action:/left.asTree")
	private void leftAsTree() {
	}
	
	@OnAction("action:/left.sort.name.asc")
	private void leftSortModesNameAsc() {
		leftOrdering = OrderingModes.BY_NAME_ASC;
		refreshLeftPanel();
	}

	@OnAction("action:/left.sort.ext.asc")
	private void leftSortModesExtAsc() {
		leftOrdering = OrderingModes.BY_EXTENSION_ASC;
		refreshLeftPanel();
	}
	
	@OnAction("action:/left.sort.date.asc")
	private void leftSortModesDateAsc() {
		leftOrdering = OrderingModes.BY_DATE_ASC;
		refreshLeftPanel();
	}

	@OnAction("action:/left.sort.size.asc")
	private void leftSortModesSizeAsc() {
		leftOrdering = OrderingModes.BY_SIZE_ASC;
		refreshLeftPanel();
	}

	@OnAction("action:/left.sort.name.desc")
	private void leftSortModesNameDesc() {
		leftOrdering = OrderingModes.BY_NAME_DESC;
		refreshLeftPanel();
	}

	@OnAction("action:/left.sort.ext.desc")
	private void leftSortModesExtDesc() {
		leftOrdering = OrderingModes.BY_EXTENSION_DESC;
		refreshLeftPanel();
	}
	
	@OnAction("action:/left.sort.date.desc")
	private void leftSortModesDateDesc() {
		leftOrdering = OrderingModes.BY_DATE_DESC;
		refreshLeftPanel();
	}

	@OnAction("action:/left.sort.size.desc")
	private void leftSortModesSizeDesc() {
		leftOrdering = OrderingModes.BY_SIZE_DESC;
		refreshLeftPanel();
	}

	@OnAction("action:/left.sort.unordered")
	private void leftSortModesUnordered() {
		leftOrdering = OrderingModes.UNORDERED;
		refreshLeftPanel();
	}
	
	@OnAction("action:/left.onoffpanel")
	private void leftOnOff() {
		leftContainer.setVisibility(!leftContainer.getVisibility());
	}

	@OnAction("action:/left.changeFS")
	private void leftChangeFS() {
		try{final URI	newFsi = JFileSystemChanger.ask(this,localizer);
		
			if (newFsi != null) {
				if (leftFsi != null) {
					leftFsi.close();
				}
				leftFsi = FileSystemFactory.createFileSystem(newFsi);
			}
		} catch (LocalizationException | ContentException | IOException e) {
			state.message(Severity.error,e,"Error on change file system: "+e.getMessage());
		}
	}

	private void refreshLeftPanel() {
		// TODO Auto-generated method stub
		
	}

	
	/*
	 * Files
	 */

	@OnAction("action:/files.view")
	private void fileView() {
		switch (currentState) {
			case STATE_ORDINAL	:
				currentState = STATE_IN_VIEW;
				((CardLayout)screen.getLayout()).show(screen,TAB_VIEWER);
				break;
			case STATE_IN_VIEW	:
				break;
			case STATE_IN_EDIT	:
				break;
		}
	}	
	
	@OnAction("action:/files.edit")
	private void fileEdit() {
		switch (currentState) {
			case STATE_ORDINAL	:
				currentState = STATE_IN_EDIT;
				((CardLayout)screen.getLayout()).show(screen,TAB_EDITOR);
				break;
			case STATE_IN_VIEW	:
				break;
			case STATE_IN_EDIT	:
				break;
		}
	}	
	
	@OnAction("action:/files.copy")
	private void fileCopy() {
	}	
	
	@OnAction("action:/files.move")
	private void fileMove() {
	}	
	
	@OnAction("action:/files.mkdir")
	private void fileMkDir() {
	}	
	
	@OnAction("action:/files.remove")
	private void fileRemove() {
	}	

	@OnAction("action:/files.mount")
	private void fileMount() {
	}	
	
	@OnAction("action:/files.unmount")
	private void fileUnmount() {
	}	

	@OnAction("action:/files.select")
	private void fileSelect() {
	}	

	@OnAction("action:/files.unselect")
	private void fileUnselect() {
	}	
	
	@OnAction("action:/files.invselect")
	private void fileInvSelect() {
		for (Content item : currentContainer.totalContent()) {
			item.setSelection(!item.isSelected());
		}
	}	
	
	@OnAction("action:/files.restselect")
	private void fileRestSelect() {
	}	
	
	@OnAction("action:/files.exit")
	private void fileExit() throws LocalizationException {
		if (!settings.confirms.onExit || askConfirm(CONFIRMATION_EXIT)) {
			if (leftFsi != null) {
				try{leftFsi.close();
				    leftFsi = null;
				} catch (IOException e) {
					state.message(Severity.warning,e.getLocalizedMessage(),e);
				}
			}
			if (rightFsi != null) {
				try{rightFsi.close();
					rightFsi = null;
				} catch (IOException e) {
					state.message(Severity.warning,e.getLocalizedMessage(),e);
				}
			}
			setVisible(false);
			dispose();
			latch.countDown();
		}
	}

	/*
	 * Commands
	 */

	@OnAction("action:/command.findfile")
	private void commandFindFile() {
		
	}
	
	@OnAction("action:/command.findfolders")
	private void commandFindFolders() {
		
	}
	
	@OnAction("action:/command.comparefolders")
	private void commandCompareFolders() {
		
	}
	
	@OnAction("action:/command.swappanels")
	private void commandSwapPanels() {
		
	}
	
	@OnAction("action:/command.onoffpanels")
	private void commandOnOffPanels() {
		
	}
	
	
	/*
	 * Settings
	 */
	
	@OnAction("action:/settings.system")
	private void systemSettings() {
		final SupportedLanguages			oldLang = settings.systemSettings.lang; 
		
		callSettings(settings.systemSettings,new Dimension(300,150),()->{
			if (settings.systemSettings.lang != oldLang) {
				try{localizer.setCurrentLocale(settings.systemSettings.lang.getLocale());
				} catch (LocalizationException e) {
					state.message(Severity.error,e,"Error on parameter settings: "+e.getMessage());
				}
			}			
		});
	}
	
	@OnAction("action:/settings.panel")
	private void panelSettings() {
		callSettings(settings.panelSettings,new Dimension(400,150),()->{});
	}
	
	@OnAction("action:/settings.viewer")
	private void viewerSettings() {
		callSettings(settings.viewerSettings,new Dimension(400,150),()->{});
	}
	
	@OnAction("action:/settings.editor")
	private void editorSettings() {
		callSettings(settings.editorSettings,new Dimension(400,150),()->{});
	}
	
	@OnAction("action:/settings.colorsAndHighlighting")
	private void colorsAndHighlightingSettings() {
		callSettings(settings.highlightSettings,new Dimension(300,170),()->{});
	}
	
	@OnAction("action:/settings.confirm")
	private void confirmSettings() {
		callSettings(settings.confirms,new Dimension(300,200),()->{});
	}
	
	@OnAction("action:/settings.save")
	private void saveSettings() {
		try(final OutputStream			os = new FileOutputStream(SETTINGS_NAME);
			final Writer				wr = new OutputStreamWriter(os,"UTF-8");
			final JsonStaxPrinter		prn = new JsonStaxPrinter(wr)) {
				
			ser.serialize(settings,prn);
			prn.flush();
			state.message(Severity.info,"Current settings saved successfully");
		} catch (PrintingException | IOException exc) {
			state.message(Severity.error,exc,"Error on store settings "+exc.getMessage());
		}
	}

	/*
	 * Right panel
	 */
	
	@OnAction("action:/right.asTable")
	private void rightAsTable() {
		
	}
	
	@OnAction("action:/right.asTree")
	private void rightAsTree() {
		
	}
	
	@OnAction("action:/right.sort.name.asc")
	private void rightSortModesNameAsc() {
		rightOrdering = OrderingModes.BY_NAME_ASC;
		refreshRightPanel();
	}

	@OnAction("action:/right.sort.ext.asc")
	private void rightSortModesExtAsc() {
		rightOrdering = OrderingModes.BY_EXTENSION_ASC;
		refreshRightPanel();
	}
	
	@OnAction("action:/right.sort.date.asc")
	private void rightSortModesDateAsc() {
		rightOrdering = OrderingModes.BY_DATE_ASC;
		refreshRightPanel();
	}

	@OnAction("action:/right.sort.size.asc")
	private void rightSortModesSizeAsc() {
		rightOrdering = OrderingModes.BY_SIZE_ASC;
		refreshRightPanel();
	}

	@OnAction("action:/right.sort.name.desc")
	private void rightSortModesNameDesc() {
		rightOrdering = OrderingModes.BY_NAME_DESC;
		refreshRightPanel();
	}

	@OnAction("action:/right.sort.ext.desc")
	private void rightSortModesExtDesc() {
		rightOrdering = OrderingModes.BY_EXTENSION_DESC;
		refreshRightPanel();
	}
	
	@OnAction("action:/right.sort.date.desc")
	private void rightSortModesDateDesc() {
		rightOrdering = OrderingModes.BY_DATE_DESC;
		refreshRightPanel();
	}

	@OnAction("action:/right.sort.size.desc")
	private void rightSortModesSizeDesc() {
		rightOrdering = OrderingModes.BY_SIZE_DESC;
		refreshRightPanel();
	}

	@OnAction("action:/right.sort.unordered")
	private void rightSortModesUnordered() {
		rightOrdering = OrderingModes.UNORDERED;
		refreshRightPanel();
	}
	
	
	@OnAction("action:/right.onoffpanel")
	private void rightOnOff() {
		rightContainer.setVisibility(!rightContainer.getVisibility());
	}

	@OnAction("action:/right.changeFS")
	private void rightChangeFS() {
		try{final URI	newFsi = JFileSystemChanger.ask(this,localizer);
			
			if (newFsi != null) {
				if (rightFsi != null) {
					rightFsi.close();
				}
				rightFsi = FileSystemFactory.createFileSystem(newFsi);
			}
		} catch (LocalizationException | ContentException | IOException e) {
			state.message(Severity.error,e,"Error on change file system: "+e.getMessage());
		}
	}

	private void refreshRightPanel() {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Help
	 */
	
	@OnAction("action:/help.guide")
	private void helpGuide() throws LocalizationException, IOException {
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(URI.create("http://"+addr.getHostName()+":"+addr.getPort()));
		}
		else {
			state.message(Severity.warning, "Desktop is not supported to start browser...");
		}
	}
	
	@OnAction("action:/help.update")
	private void updateSoft() throws LocalizationException, IOException {
		
	}
	
	@OnAction("action:/help.about")
	private void about() throws LocalizationException, IOException {
		final JEditorPane	pane = new JEditorPane("text/html","");
		
		pane.setEditable(false);
		pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		pane.setPreferredSize(new Dimension(400,300));
		pane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try{Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (URISyntaxException | IOException exc) {
						exc.printStackTrace();
					}
				}
			}
		});
		try(final Reader	rdr = localizer.getContent(HELP_CONTENT,PureLibSettings.MIME_CREOLE_TEXT,PureLibSettings.MIME_HTML_TEXT)) {
			pane.read(rdr, null);
		}
		new JLocalizedOptionPane(localizer,AVATAR).message(this, pane, HELP_TITLE, JOptionPane.INFORMATION_MESSAGE);
	}

	
	@OnAction("action:/escape")
	private void escape() {
		switch (currentState) {
			case STATE_ORDINAL	:
				if (commandString.getText().length() > 0) {
					commandString.setText("");
				}				
				break;
			case STATE_IN_VIEW	:
				currentState = STATE_ORDINAL; 
				((CardLayout)screen.getLayout()).show(screen,TAB_PANEL);
				break;
			case STATE_IN_EDIT	:
				currentState = STATE_ORDINAL; 
				((CardLayout)screen.getLayout()).show(screen,TAB_PANEL);
				break;
		}
	}	

	private <T> void callSettings(final T settings, final Dimension size, final SettingsCallback callback) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(settings.getClass());
			final AutoBuiltForm<T>			form = new AutoBuiltForm(mdi,localizer,settings,(FormManager<?,T>)settings);
			final Object					clone = ((Resettable)settings).copy();
			
			for (Module m : form.getUnnamedModules()) {
				this.getClass().getModule().addExports(settings.getClass().getPackageName(),m);
			}
			form.setPreferredSize(size);
			if (AutoBuiltForm.ask(this,localizer,form)) {
				callback.processOK();
				if (this.settings.systemSettings.autoSaveSettings) {
					saveSettings();
				}
			}
			else {
				((Resettable)settings).reset(clone);
			}
		} catch (LocalizationException | ContentException | CloneNotSupportedException e) {
			state.message(Severity.error,e,"Error on parameter settings: "+e.getMessage());
		}
	}
	
	private boolean askConfirm(final String localizedMessage) throws LocalizationException {
		return new JLocalizedOptionPane(localizer).confirm(this,localizedMessage,CONFIRMATION_TITLE,JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	private boolean askConfirm(final LocalizedFormatter localizedFormatter) throws LocalizationException {
		return new JLocalizedOptionPane(localizer).confirm(this,localizedFormatter,CONFIRMATION_TITLE,JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
	
	
	private void fillLocalizedStrings() throws LocalizationException {
		setTitle(localizer.getValue(APPLICATION_TITLE));
		if (menu instanceof LocaleChangeListener) {
			((LocaleChangeListener)menu).localeChanged(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
	}

	public static void main(String[] args) throws ContentException, IOException, InterruptedException, NullPointerException, EnvironmentException {
		final ArgParser		ap = new ApplicationArgParser().parse(args);
		final Properties	props = Utils.mkProps(NanoServiceFactory.NANOSERVICE_PORT,ap.getValue(ARG_PORT,Integer.class).toString()
												 ,NanoServiceFactory.NANOSERVICE_ROOT,ap.getValue(ARG_ROOT,URI.class).toString());
		
		
		try(final InputStream		is = Application.class.getResourceAsStream("application.xml");
			final LoggerFacade		log = new SystemErrLoggerFacade();
			final Localizer			parent = new PureLibLocalizer();
			final HelpService		hs = new HelpService(log, new SubstitutableProperties(props))) {
			final CountDownLatch 	latch = new CountDownLatch(1);
			
			hs.start();
			new Application(parent,ContentModelFactory.forXmlDescription(is),latch,new InetSocketAddress("localhost",ap.getValue(ARG_PORT,Integer.class))).setVisible(true);
			latch.await();
			hs.stop();
		}
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgDescription[]	PARM_LIST = {
							 new IntegerArg(ARG_PORT,false,"port to open nanoservice for help with browsers",12321)
							,new URIArg(ARG_ROOT,false,"root to open nanoservice for help with browsers","fsys:"+Application.class.getResource("Application.class")+"!../../../chav1961/fsyscommander/static")
							};	
		
		public ApplicationArgParser() {
			super(PARM_LIST);
		}
	}
}
