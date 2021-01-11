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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.util.regex.Pattern;

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

import chav1961.fsyscommander.interfaces.FileContainer;
import chav1961.fsyscommander.interfaces.FileContainer.Content;
import chav1961.fsyscommander.interfaces.OKCallback;
import chav1961.fsyscommander.interfaces.OrderingModes;
import chav1961.fsyscommander.interfaces.Resettable;
import chav1961.fsyscommander.settings.Confirms;
import chav1961.fsyscommander.settings.Settings;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
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
	
	public static final String		ARG_SERVER = "server";
	public static final String		ARG_PORT = "port";
	public static final String		ARG_ROOT = "root";

	private static final String		HELP_ROOT = "/help";
	private static final URI		HELP_LOCATION = URI.create("fsys:file:./src/main/resources/chav1961/fsyscommander/static");
	
	private static final String		SETTINGS_NAME = ".fsyscommander";
	
	private static final String		INITIAL_FILESYSTEM = "fsys:"+new File("./").getAbsoluteFile().toURI().toString();
	
	private static final String		APPLICATION_TITLE = "Application.title";
	private static final String		HELP_TITLE = "Application.help.title";
	private static final String		HELP_CONTENT = "Application.help.content";
	private static final Icon[]		AVATAR = {null, null, null, new ImageIcon(Application.class.getResource("avatar.jpg"))};

	private static final String		CONFIRMATION_TITLE = "Confirmation.title";
	private static final String		CONFIRMATION_COPY_BULK_MESSAGE = "Confirmation.copy.bulk.message";
	private static final String		CONFIRMATION_COPY_MESSAGE = "Confirmation.copy.message";
	private static final String		CONFIRMATION_MOVE_BULK_MESSAGE = "Confirmation.move.bulk.message";
	private static final String		CONFIRMATION_MOVE_MESSAGE = "Confirmation.move.message";
	private static final String		CONFIRMATION_DELETE_BULK_MESSAGE = "Confirmation.delete.bulk.message";
	private static final String		CONFIRMATION_DELETE_MESSAGE = "Confirmation.delete.message";
	private static final String		CONFIRMATION_EXIT = "Confirmation.exit";
	
	private static final String		TAB_PANEL = "panel";
	private static final String		TAB_VIEWER = "viewer";
	private static final String		TAB_EDITOR = "editor";

	private static final int		STATE_ORDINAL = 0;
	private static final int		STATE_IN_VIEW = 1;
	private static final int		STATE_IN_EDIT = 2;

	private static final FormManager<Object,Object>	DUMMY_MGR = new FormManager<Object,Object>() {
										@Override
										public RefreshMode onRecord(RecordAction action, Object oldRecord, Object oldId, Object newRecord, Object newId) throws FlowException, LocalizationException {
											return RefreshMode.DEFAULT;
										}
								
										@Override
										public RefreshMode onField(Object inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
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

	private final Localizer			localizer;
	private final CountDownLatch	latch;
	private final Settings			settings;
	private final JsonSerializer<Settings>	ser; 
	private final InetSocketAddress	addr;
	private final URI				helpRoot;
	private final JStateString		state;
	private final JPanel			content = new JPanel(new BorderLayout());
	private final ConsoleOutput		console = new ConsoleOutput();		 
	private final JPanel			screen = new JPanel(new CardLayout());
	private final JPanel			container = new JPanel(new GridLayout(1,2));
	private final FileViewer		viewer;	
	private final FileEditor		editor;	
	private final CommandString		commandString = new CommandString(console);
	private final JPanel			bottomArea = new JPanel(new GridLayout(2,1));
	private final JMenuBar			menu;
	private FileContainer			leftContainer;
	private FileContainer			rightContainer;
	private final SelectionMask		mask;
	private final DirectoryCreation	directory;

	private boolean					helpServerStarted = false;
	private int						currentState = STATE_ORDINAL;
	private FileSystemInterface		leftFsi = null, rightFsi = null;
	private FileContainer			currentContainer = leftContainer;
	private String					selectionTemplate = "*.*";
	private OrderingModes			leftOrdering = OrderingModes.BY_NAME_ASC, rightOrdering = OrderingModes.BY_NAME_ASC;
	private boolean					leftVisibility = true, rightVisibility = true, totalVisibility = true;
	private boolean					leftContainerFirst = true;
	
	public Application(final Localizer parent, final ContentMetadataInterface model, final CountDownLatch latch, final InetSocketAddress helpServerAddr, final URI helpRoot) throws IOException, LocalizationException {
		this.latch = latch;
		this.addr = helpServerAddr;
		this.helpRoot = helpRoot;
		this.localizer = LocalizerFactory.getLocalizer(model.getRoot().getLocalizerAssociated());
		this.state = new JStateString(this.localizer);
		this.state.setBorder(new LineBorder(Color.BLACK));
		this.mask = new SelectionMask(state); 
		this.directory = new DirectoryCreation(state); 
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
		
		leftContainer = new ViewerAsTable(settings.highlightSettings,(c)->processContentItems(leftContainer,leftFsi,c));
		rightContainer = new ViewerAsTable(settings.highlightSettings,(c)->processContentItems(rightContainer,rightFsi,c));
		((JComponent)leftContainer).setBorder(new LineBorder(Color.WHITE));
		((JComponent)leftContainer).addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {}
			@Override public void focusGained(FocusEvent e) {currentContainer = leftContainer;}
		});
		container.add(((JComponent)leftContainer));
		((JComponent)rightContainer).setBorder(new LineBorder(Color.WHITE));
		((JComponent)rightContainer).addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {}
			@Override public void focusGained(FocusEvent e) {currentContainer = rightContainer;}
		});
		container.add(((JComponent)rightContainer));
		container.setOpaque(false);

    	this.viewer = new FileViewer(settings.highlightSettings,()->{
    					((CardLayout)screen.getLayout()).show(screen,TAB_PANEL);
    					((JComponent)currentContainer).requestFocusInWindow();
    				});	
    	this.editor = new FileEditor(settings.highlightSettings,()->{
    					((CardLayout)screen.getLayout()).show(screen,TAB_PANEL);
    					((JComponent)currentContainer).requestFocusInWindow();
    				});	
        
		bottomArea.add(commandString);
		bottomArea.add(state);

		screen.add(container,TAB_PANEL);
		screen.add(viewer,TAB_VIEWER);
		screen.add(editor,TAB_EDITOR);
		((CardLayout)screen.getLayout()).show(screen,TAB_PANEL);
        screen.setOpaque(false);

        final JLayeredPane layeredPane = new JLayeredPane();
        
        layeredPane.add(console, Integer.valueOf(0));
        layeredPane.add(screen, Integer.valueOf(1));
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
		
		leftFsi = FileSystemFactory.createFileSystem(URI.create(INITIAL_FILESYSTEM));
		((ViewerAsTable)leftContainer).fillContent(leftFsi);
		rightFsi = FileSystemFactory.createFileSystem(URI.create(INITIAL_FILESYSTEM));
		((ViewerAsTable)rightContainer).fillContent(rightFsi);
		
		currentContainer = leftContainer;
		((JComponent)currentContainer).requestFocusInWindow();
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
		state.message(Severity.warning, "Not implemented yet...");
	}
	
	@OnAction("action:/left.asTree")
	private void leftAsTree() {
		state.message(Severity.warning, "Not implemented yet...");
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
		leftVisibility = !leftVisibility;
		refreshPanelVisibility();
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
		leftContainer.setOrderingMode(leftOrdering.getOrderingMode());
		leftContainer.setOrderingDirection(leftOrdering.getOrderingDirection());
	}

	
	/*
	 * Files
	 */

	@OnAction("action:/files.view")
	private void fileView() throws IOException {
		switch (currentState) {
			case STATE_ORDINAL	:
				final Content 	item = currentContainer.currentContent();
			
				if (!item.isDirectory()) {
					try(final FileSystemInterface	fsi = leftFsi.clone().open(item.getName());
						final InputStream			is = fsi.read()) {
						
						viewer.loadContent(is);
					}
					currentState = STATE_IN_VIEW;
					((CardLayout)screen.getLayout()).show(screen,TAB_VIEWER);
					viewer.requestFocusInWindow();
					break;
				}
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
				state.message(Severity.warning, "Not implemented yet...");
				break;
			case STATE_IN_VIEW	:
				break;
			case STATE_IN_EDIT	:
				break;
		}
	}	
	
	@OnAction("action:/files.copy")
	private void fileCopy() throws LocalizationException, IOException {
		if (currentContainer == leftContainer) {
			fileCopy(leftFsi,rightFsi,settings.confirms,leftContainer,rightContainer);
			((ViewerAsTable)rightContainer).fillContent(rightFsi);
		}
		else {
			fileCopy(rightFsi,leftFsi,settings.confirms,rightContainer,leftContainer);
			((ViewerAsTable)leftContainer).fillContent(leftFsi);
		}
	}	
	
	private void fileCopy(final FileSystemInterface from, final FileSystemInterface to, final Confirms confirms, final FileContainer fromContainer, final FileContainer toContainer) throws LocalizationException {
		try {
			if (fromContainer.hasSelections()) {
				int	count = 0;
				
				for (Content item : fromContainer.selectedContent()) {
					count++;
				}
				
				if (!confirms.onBulkCopy || askConfirm(new LocalizedFormatter(CONFIRMATION_COPY_BULK_MESSAGE,count,to.getPath()))) {
					try {state.start("Copying...",count);
					
						count = 0;
						for (Content item : fromContainer.selectedContent()) {
							try(final FileSystemInterface	fsi = from.clone().open(item.getName())) {
								fsi.copy(to);
							}
							if (!state.processed(++count)) {
								break;
							}
						}
					} finally {
						state.end();
						state.message(Severity.info, "Completed");
					}
				}
			}
			else {
				final Content	content = fromContainer.currentContent();
				
				if (!confirms.onCopy ||  askConfirm(new LocalizedFormatter(CONFIRMATION_COPY_MESSAGE,content.getName(),to.getPath()))) {
					try(final FileSystemInterface	fsi = from.clone().open(content.getName())) {
						fsi.copy(to);
					}
					state.message(Severity.info, "Completed");
				}
			}
		} catch (IOException exc) {
			state.message(Severity.error, "I/O error while copying: "+exc.getLocalizedMessage());
		}
	}

	@OnAction("action:/files.move")
	private void fileMove() throws LocalizationException, IOException {
		if (currentContainer == leftContainer) {
			fileMove(leftFsi,rightFsi,settings.confirms,leftContainer,rightContainer);
		}
		else {
			fileMove(rightFsi,leftFsi,settings.confirms,rightContainer,leftContainer);
		}
		((ViewerAsTable)leftContainer).fillContent(leftFsi);
		((ViewerAsTable)rightContainer).fillContent(rightFsi);
	}	

	private void fileMove(final FileSystemInterface from, final FileSystemInterface to, final Confirms confirms, final FileContainer fromContainer, final FileContainer toContainer) throws LocalizationException {
		try {
			if (fromContainer.hasSelections()) {
				int	count = 0;
				
				for (Content item : fromContainer.selectedContent()) {
					count++;
				}
				
				if (!confirms.onBulkMove || askConfirm(new LocalizedFormatter(CONFIRMATION_MOVE_BULK_MESSAGE,count,to.getPath()))) {
					try {state.start("Moving...",count);
					
						count = 0;
						for (Content item : fromContainer.selectedContent()) {
							try(final FileSystemInterface	fsi = from.clone().open(item.getName())) {
								fsi.move(to);
							}
							if (!state.processed(++count)) {
								break;
							}
						}
					} finally {
						state.end();
						state.message(Severity.info, "Completed");
					}
				}
			}
			else {
				final Content	content = fromContainer.currentContent();
				
				if (!confirms.onMove ||  askConfirm(new LocalizedFormatter(CONFIRMATION_MOVE_MESSAGE,content.getName(),to.getPath()))) {
					try(final FileSystemInterface	fsi = from.clone().open(content.getName())) {
						fsi.move(to);
					}
					state.message(Severity.info, "Completed");
				}
			}
		} catch (IOException exc) {
			state.message(Severity.error, "I/O error while moving: "+exc.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/files.mkdir")
	private void fileMkDir() {
		callSettings(directory,new Dimension(400,60),()->{
			if (directory.name != null && !directory.name.isEmpty()) {
				try(final FileSystemInterface	fsi = (currentContainer == leftContainer ? leftFsi : rightFsi).clone().open(directory.name)) {
					if (fsi.exists()) {
						if (fsi.isFile()) {
							state.message(Severity.warning, "Name ["+directory.name+"] already exists and is a file!");
						}
						else {
							state.message(Severity.warning, "Directory ["+directory.name+"] already exists!");
						}
					}
					else {
						fsi.mkDir();
						((ViewerAsTable)currentContainer).fillContent(currentContainer == leftContainer ? leftFsi : rightFsi);
						state.message(Severity.info, "Directory ["+directory.name+"] creates successfully");
					}
				} catch (IOException e) {
					state.message(Severity.warning, "Directory ["+directory.name+"] was not created, "+e.getLocalizedMessage());
				}
			}
			else {
				state.message(Severity.info, "Directory name can't be empty!["+directory.name+"] creates successfully");
			}
		});
	}	
	
	@OnAction("action:/files.remove")
	private void fileRemove() throws IOException, LocalizationException {
		if (currentContainer == leftContainer) {
			fileRemove(leftFsi,settings.confirms,leftContainer);
			((ViewerAsTable)leftContainer).fillContent(leftFsi);
		}
		else {
			fileRemove(rightFsi,settings.confirms,rightContainer);
			((ViewerAsTable)rightContainer).fillContent(rightFsi);
		}
	}	

	private void fileRemove(final FileSystemInterface fsi, final Confirms confirms, final FileContainer container) throws LocalizationException {
		try {
			if (container.hasSelections()) {
				int	count = 0;
				
				for (Content item : container.selectedContent()) {
					count++;
				}
				
				if (!confirms.onBulkDelete || askConfirm(new LocalizedFormatter(CONFIRMATION_DELETE_BULK_MESSAGE,count))) {
					try {state.start("Moving...",count);
					
						count = 0;
						for (Content item : container.selectedContent()) {
							try(final FileSystemInterface	entity = fsi.clone().open(item.getName())) {
								entity.deleteAll();
							}
							if (!state.processed(++count)) {
								break;
							}
						}
					} finally {
						state.end();
						state.message(Severity.info, "Completed");
					}
				}
			}
			else {
				final Content	content = container.currentContent();
				
				if (!confirms.onDelete ||  askConfirm(new LocalizedFormatter(CONFIRMATION_DELETE_MESSAGE,content.getName()))) {
					try(final FileSystemInterface	entity = fsi.clone().open(content.getName())) {
						entity.deleteAll();
					}
					state.message(Severity.info, "Completed");
				}
			}
		} catch (IOException exc) {
			state.message(Severity.error, "I/O error while deleting: "+exc.getLocalizedMessage());
		}
	}

	@OnAction("action:/files.mount")
	private void fileMount() {
		state.message(Severity.warning, "Not implemented yet...");
	}	
	
	@OnAction("action:/files.unmount")
	private void fileUnmount() {
		state.message(Severity.warning, "Not implemented yet...");
	}	

	@OnAction("action:/files.select")
	private void fileSelect() {
		callSettings(mask,new Dimension(200,60),()->{
			final Pattern	p = Pattern.compile(Utils.fileMask2Regex(mask.mask));

			for (Content c : currentContainer.totalContent()) {
				if (p.matcher(c.getName()).matches()) {
					c.setSelection(true);
				}
			}
		});
	}	

	@OnAction("action:/files.unselect")
	private void fileUnselect() {
		callSettings(mask,new Dimension(200,60),()->{
			final Pattern	p = Pattern.compile(Utils.fileMask2Regex(mask.mask));

			for (Content c : currentContainer.totalContent()) {
				if (p.matcher(c.getName()).matches()) {
					c.setSelection(false);
				}
			}
		});
	}	
	
	@OnAction("action:/files.invselect")
	private void fileInvSelect() {
		for (Content item : currentContainer.totalContent()) {
			item.setSelection(!item.isSelected());
		}
	}	
	
	@OnAction("action:/files.restselect")
	private void fileRestSelect() {
		for (Content item : currentContainer.totalContent()) {
			item.setSelection(false);
		}
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
			if (helpServerStarted) {
				try{PureLibSettings.uninstallHelpContent(HELP_ROOT);
				} catch (ContentException | IOException e) {
				}
			}
			latch.countDown();
		}
	}

	/*
	 * Commands
	 */

	@OnAction("action:/command.findfile")
	private void commandFindFile() {
		state.message(Severity.warning, "Not implemented yet...");
	}
	
	@OnAction("action:/command.findfolders")
	private void commandFindFolders() {
		state.message(Severity.warning, "Not implemented yet...");
	}
	
	@OnAction("action:/command.comparefolders")
	private void commandCompareFolders() {
		state.message(Severity.warning, "Not implemented yet...");
	}
	
	@OnAction("action:/command.swappanels")
	private void commandSwapPanels() {
		container.remove((JComponent)leftContainer);
		container.remove((JComponent)rightContainer);
		leftContainerFirst = !leftContainerFirst;
		if (leftContainerFirst) {
			container.add(((JComponent)leftContainer));
			container.add(((JComponent)rightContainer));
		}
		else {
			container.add(((JComponent)rightContainer));
			container.add(((JComponent)leftContainer));
		}
	}
	
	@OnAction("action:/command.onoffpanels")
	private void commandOnOffPanels() {
		totalVisibility = !totalVisibility;
		refreshPanelVisibility();
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
		callSettings(settings.highlightSettings,new Dimension(300,170),()->{refreshLeftPanel(); refreshRightPanel();});
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
		state.message(Severity.warning, "Not implemented yet...");
	}
	
	@OnAction("action:/right.asTree")
	private void rightAsTree() {
		state.message(Severity.warning, "Not implemented yet...");
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
		rightVisibility = !rightVisibility;
		refreshPanelVisibility();
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
		rightContainer.setOrderingMode(rightOrdering.getOrderingMode());
		rightContainer.setOrderingDirection(rightOrdering.getOrderingDirection());
	}
	
	/*
	 * Help
	 */
	
	@OnAction("action:/help.guide")
	private void helpGuide() throws LocalizationException, IOException {
		if (Desktop.isDesktopSupported()) {
			if (!helpServerStarted) {
				try{PureLibSettings.installHelpContent(HELP_ROOT,FileSystemFactory.createFileSystem(helpRoot));
					helpServerStarted = true;
				} catch (ContentException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
			Desktop.getDesktop().browse(URI.create("http://"+addr.getHostName()+":"+addr.getPort()+"/help/index.html"));
		}
		else {
			state.message(Severity.warning, "Desktop is not supported to start browser...");
		}
	}
	
	@OnAction("action:/help.update")
	private void updateSoft() throws LocalizationException, IOException {
		state.message(Severity.warning, "Not implemented yet...");
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

	private void processContentItems(final FileContainer container, final FileSystemInterface fsi, final Content content) throws IOException {
		if (content.isDirectory()) {
			fsi.open(content.getName());
			((ViewerAsTable)container).fillContent(fsi);
		}
	}

	private void refreshPanelVisibility() {
		leftContainer.setVisibility(leftVisibility && totalVisibility);
		rightContainer.setVisibility(rightVisibility && totalVisibility);
	}
	
	private <T> void callSettings(final T settings, final Dimension size, final OKCallback callback) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(settings.getClass());
			final AutoBuiltForm<T>			form = new AutoBuiltForm(mdi,localizer,PureLibSettings.INTERNAL_LOADER,settings,(FormManager<?,T>)settings);
			final Object					clone = settings instanceof Resettable ? ((Resettable)settings).copy() : null;
			
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
			else if (clone != null) {
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
			final Localizer			parent = new PureLibLocalizer()) {
			final CountDownLatch 	latch = new CountDownLatch(1);
			
			new Application(parent,ContentModelFactory.forXmlDescription(is),latch,new InetSocketAddress(ap.getValue(ARG_SERVER,String.class),ap.getValue(ARG_PORT,Integer.class)),ap.getValue(ARG_ROOT,URI.class)).setVisible(true);
			latch.await();
		}
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgDescription[]	PARM_LIST = {
							 new IntegerArg(ARG_PORT,false,"port to open nanoservice for help with browsers",PureLibSettings.instance().getProperty(PureLibSettings.HTTP_SERVER_PORT,int.class))
							,new StringArg(ARG_SERVER,false,"server address to open nanoservice for help with browsers","localhost")
							,new URIArg(ARG_ROOT,false,"root to open nanoservice for help with browsers",HELP_LOCATION.toString())
							};	
		
		public ApplicationArgParser() {
			super(PARM_LIST);
		}
	}
}
