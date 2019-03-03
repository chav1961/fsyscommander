package chav1961.fsyscommander;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import chav1961.purelib.ui.swing.terminal.Term;

public class CommandString extends JTextField {
	private static final long serialVersionUID = 5425988726733500648L;

	enum OsType {
		Windows, Linux, MacOs, Other 
	}
	
	private final ExecutorService	es = Executors.newFixedThreadPool(2,new ThreadFactory() {
										@Override
										public Thread newThread(final Runnable r) {
											final Thread	t = new Thread(r);
											
											t.setName("command line stdout/stderr receiver");
											t.setDaemon(true);
											return t;
										}
									});
	@SuppressWarnings("unused")
	private final Term				output;
	private final List<String>		history = new ArrayList<>();
	private final String			codePage;
	private final int[]				last = {0};

	public CommandString(final Term output) {
		if (output == null) {
			throw new NullPointerException("Terminal output can't be null");
		}
		else {
			this.output = output;
			this.codePage = queryCodePage();
			setColumns(20);
			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"clear");
			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"process");
			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),"prev");
			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),"next");
			getActionMap().put("clear",new AbstractAction(){private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					setText("");
				}
			});
			getActionMap().put("prev",new AbstractAction(){private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					if (last[0] >= 0 && last[0] < history.size()) {
						if (last[0] < history.size() - 1) {
							last[0]++;
						}
						setText(history.get(last[0]));
					}
				}
			});
			getActionMap().put("next",new AbstractAction(){private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					if (last[0] >= 0 && last[0] < history.size()) {
						if (last[0] > 0) {
							last[0]--;
						}
						setText(history.get(last[0]));
					}
				}
			});
			getActionMap().put("process",new AbstractAction(){private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					final String	command = getText().trim();
					final String[]	items; 
					
					if (!command.isEmpty()) {
						try{final Process	p;
							
							switch (detectedOsType()) {
								case Linux:
								case MacOs:
								case Other:
								case Windows:
									items = parseCommand("cmd /s /c "+command);
									break;
								default:
									throw new UnsupportedOperationException();
							}
							synchronized(output) {
								output.println("\037[34m"+command+"\037[37m");
							}
							p = new ProcessBuilder(items).start();
							es.submit(()->{
								try(final InputStream		is = p.getInputStream();
									final Reader			rdr = new InputStreamReader(is,codePage);
									final BufferedReader	brdr = new BufferedReader(rdr)) {
									
									String	line;
									
									while((line = brdr.readLine()) != null) {
										synchronized(output) {
											output.println(line);
										}
									}
								} catch (IOException exc) {	
								}
								return null;
							});
							es.submit(()->{
								try(final InputStream		is = p.getErrorStream();
									final Reader			rdr = new InputStreamReader(is,codePage);
									final BufferedReader	brdr = new BufferedReader(rdr)) {
									
									String	line;
									
									while((line = brdr.readLine()) != null) {
										synchronized(output) {
											output.println(line);
										}
									}
								} catch (IOException exc) {								
									exc.printStackTrace();
								}
								return null;
							});
							p.getOutputStream().close();
							
							p.waitFor();
							if (history.size() == 0 || !command.equals(history.get(0))) {
								history.add(0,command);
							}
							setText("");
							last[0] = 0;
						} catch (IOException | InterruptedException exc) {
							exc.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	private String queryCodePage() {
		final Process	p;
		final String[]	result = new String[] {Charset.defaultCharset().name()};
		
		try{switch (detectedOsType()) {
				case Linux:
				case MacOs:
				case Other:
				case Windows:
					p = new ProcessBuilder("cmd","/s","/c","chcp").start();
					es.submit(()->{
						try(final InputStream		is = p.getInputStream();
							final Reader			rdr = new InputStreamReader(is);
							final BufferedReader	brdr = new BufferedReader(rdr)) {
							
							String	line;
							
							while((line = brdr.readLine()) != null) {
								final String[]	items = line.trim().split("\\ ");
								
								if (items.length > 0) {
									try{result[0] = "cp"+Integer.valueOf(items[items.length-1]);
									} catch (NumberFormatException exc) {
									}
								}
							}
						} catch (IOException exc) {	
						}
						return null;
					});
					p.waitFor();
					break;
				default:
					throw new UnsupportedOperationException();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return result[0];
	}

	private static String[] parseCommand(final String string) {
		return string.split(" ");
	}

	private static OsType detectedOsType() {
		return OsType.Windows;
	}
}
