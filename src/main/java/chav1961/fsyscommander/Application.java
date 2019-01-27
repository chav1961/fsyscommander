package chav1961.fsyscommander;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.XMLDescribedApplication;
import chav1961.purelib.ui.swing.SwingModelUtils;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JStateString;

public class Application extends JFrame implements LocaleChangeListener {
	private static final long 	serialVersionUID = -8313830909078065332L;
	private static final String	APPLICATION_TITLE = "Application.title";
	private static final String	HELP_TITLE = "Application.help.title";
	private static final String	HELP_CONTENT = "Application.help.content";
	private static final Icon[]	AVATAR = {null, null, null, new ImageIcon(Application.class.getResource("avatar.jpg"))};

	private final Localizer		localizer;
	private final JStateString	state;
	private final JPanel		container = new JPanel(new GridLayout(1,2));
	private final ViewerAsTable	leftContainer = new ViewerAsTable();
	private final ViewerAsTable	rightContainer = new ViewerAsTable();
	private final JTextField	commandString = new JTextField();
	private final JPanel		bottomArea = new JPanel(new GridLayout(2,1));
	private final JMenuBar		menu;
	
	public Application(final ContentMetadataInterface model) throws IOException, LocalizationException {
		this.localizer = LocalizerFactory.getLocalizer(model.getRoot().getLocalizerAssociated());
		this.state = new JStateString(this.localizer);
		this.state.setBorder(new LineBorder(Color.BLACK));
		this.menu = SwingModelUtils.toMenuEntity(model.byUIPath(URI.create(ContentMetadataInterface.UI_SCHEME+":/model/navigation.top.mainmenu")),JMenuBar.class);
		leftContainer.setBorder(new LineBorder(Color.WHITE));
		container.add(leftContainer);
		rightContainer.setBorder(new LineBorder(Color.WHITE));
		container.add(rightContainer);
		bottomArea.add(commandString);
		bottomArea.add(state);

		getContentPane().add(menu,BorderLayout.NORTH);
		getContentPane().add(container,BorderLayout.CENTER);
		getContentPane().add(bottomArea,BorderLayout.SOUTH);

		SwingUtils.assignActionListeners(this.menu,this);
		SwingUtils.assignExitMethod4MainWindow(this,()->{exit();});
		SwingUtils.centerMainWindow(this,0.75f);
		localizer.addLocaleChangeListener(this);
		fillLocalizedStrings();
		container.setBackground(Color.cyan);
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@OnAction("action:/left.onoffpanel")
	private void leftOnOff() {
		leftContainer.setVisible(!leftContainer.isVisible());
	}
	
	@OnAction("action:/files.exit")
	private void exit() {
		setVisible(false);
		dispose();
	}

	@OnAction("action:/right.onoffpanel")
	private void rightOnOff() {
		rightContainer.setVisible(!rightContainer.isVisible());
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

	private void fillLocalizedStrings() throws LocalizationException {
		setTitle(localizer.getValue(APPLICATION_TITLE));
	}

	public static void main(String[] args) throws EnvironmentException, IOException {
		try(final InputStream	is = Application.class.getResourceAsStream("application.xml")) {
			
			new Application(ContentModelFactory.forXmlDescription(is)).setVisible(true);
		}
	}

}
