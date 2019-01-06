package chav1961.fsyscommander;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.XMLDescribedApplication;

public class Application extends JFrame {
	private static final long serialVersionUID = -8313830909078065332L;

	private final JPanel	leftContainer = new JPanel();
	private final JPanel	rightContainer = new JPanel();
//	private final JMenuBar	menuBar; 
	
	public Application(final ContentMetadataInterface model) {
		final JPanel		container = new JPanel(new GridLayout(1,2));
		final Dimension		screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		leftContainer.setBorder(new LineBorder(Color.black));
		container.add(leftContainer);
		rightContainer.setBorder(new LineBorder(Color.black));
		container.add(rightContainer);
		getContentPane().add(container,BorderLayout.CENTER);
		setSize(screen.width*3/4,screen.height*3/4);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	

	public static void main(String[] args) throws EnvironmentException, IOException {
		try(final InputStream	is = Application.class.getResourceAsStream("application.xml")) {
			
			new Application(ContentModelFactory.forXmlDescription(is)).setVisible(true);
		}
	}
}
