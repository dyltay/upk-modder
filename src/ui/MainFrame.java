package ui;


import io.parser.OperandTableParser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.stream.events.XMLEvent;
import model.modtree.ModOperandNode;

import model.modtree.ModTree;

import org.bounce.text.LineNumberMargin;
import org.bounce.text.ScrollableEditorPanel;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLFoldingMargin;
import org.bounce.text.xml.XMLScanner;
import org.bounce.text.xml.XMLStyleConstants;

import ui.dialogs.ReferenceUpdateDialog;
// use these for plain text
//import ui.editor.ModEditorKit;
// use these for broken text display
//import model.modelement3.ModElement;

/**
 * The application's primary frame.
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	/**
	 * The shared singleton instance of the application's main frame.
	 */
	public static MainFrame instance;
	
	/**
	 * The XML editor instance.
	 */
	private JEditorPane xmlEditor;

	/**
	 * The modfile editor instance.
	 */
	private JEditorPane modEditor;

	/**
	 * The modfile tree structure.
	 */
	private ModTree modTree;
	
	/**
	 * Constructs the application's main frame.
	 * @param title the title string appearing in the frame's title bar
	 */
	private MainFrame(String title) {
		// instantiate frame
		// TODO: add application icon
		super(title);
		
		// create and lay out the frame's components
		try {
			this.initComponents();
		} catch (Exception e) {
//			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
		
		// make closing the main frame terminate the application
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// adjust frame size
		this.pack();
		// center frame in screen
		this.setLocationRelativeTo(null);
		// show frame
		this.setVisible(true);
	}
	
	/**
	 * Returns the singleton instance of the application's main frame.
	 * @return the main frame instance
	 */
	public static MainFrame getInstance() {
		if (instance == null) {
			instance = new MainFrame(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + "");
		}
		return instance;
	}

	/**
	 * Creates and lays out the frame's components.
	 * @throws Exception if an I/O error occurs
	 */
	private void initComponents() throws Exception {
		// create and install menu bar
		this.setJMenuBar(this.createMenuBar());
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		
		// create and configure left-hand xml editor
		this.xmlEditor = new JEditorPane();
		
		XMLEditorKit xmlKit = new XMLEditorKit();
		xmlKit.setAutoIndentation(true);
		xmlKit.setTagCompletion(true);
		
//		xmlKit.setAutoIndentation(true);
		xmlEditor.setEditorKit(xmlKit);
		xmlEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		File xmlFile = new File("MyModPackage_v3.xml");    // new streamlined file version
		xmlEditor.read(new FileInputStream(xmlFile), xmlFile);
		
		ScrollableEditorPanel xmlPnl = new ScrollableEditorPanel(xmlEditor);
		JScrollPane xmlPane = new JScrollPane(xmlPnl,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		xmlPane.setPreferredSize(new Dimension(350, 600));

		JPanel rowHeader = new JPanel(new BorderLayout());
		rowHeader.add(new XMLFoldingMargin(xmlEditor), BorderLayout.EAST);
		rowHeader.add(new LineNumberMargin(xmlEditor), BorderLayout.WEST);
		xmlPane.setRowHeaderView(rowHeader);
		
		modEditor = new JEditorPane();
		modEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		JScrollPane modPane = new JScrollPane(modEditor,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modPane.setRowHeaderView(new LineNumberMargin(modEditor));
		modPane.setPreferredSize(new Dimension(650, 600));
		
		/*
		 * use this for working plain text -- X's original
		 */
//		modEditor.setEditorKit(new ui.editor.ModEditorKit());
		
		
		/*
		 * use this for broken ModDocument3 -- Amineri's experiment
		 */
		OperandTableParser parser = new OperandTableParser(Paths.get("config/operand_data.ini"));
		parser.parseFile();
//		modEditor.setEditorKit(new ui.modeditorkit.ModEditorKit()); // use this for broken ModDocument3

		File modFile = new File("test_mod_v3.upk_mod");    // new streamlined file version
//		modEditor.read(new FileInputStream(modFile), modFile);

//		Document modDocument = modEditor.getDocument();
//		modDocument.putProperty(PlainDocument.tabSizeAttribute, 4);
//		((model.moddocument3.ModDocument) modDocument).insertUpdate(null, null);

		/*
		 * Amineri's experiment with ModTree and Styled Document
		 */
		modEditor.setEditorKit(new ModStyledEditorKit() {
			@Override
			public ViewFactory getViewFactory() {
				// TODO Auto-generated method stub
				return new ViewFactory() {

			        public View create(Element elem) {
			            String kind = elem.getName();
			            if (kind != null) {
			                if (kind.equals(AbstractDocument.ContentElementName)) {
			                    return new LabelView(elem);
			                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
			                	return new ParagraphView(elem) {
//			                    	/* hack to prevent line wrapping */
//			                    	@Override
//									public void layout(int width, int height) {
//										super.layout(Short.MAX_VALUE, height);
//									}
//			                    	@Override
//									public float getMinimumSpan(int axis) {
//										return super.getPreferredSpan(axis);
//									}
			                    };
			                } else if (kind.equals(AbstractDocument.SectionElementName)) {
			                    return new BoxView(elem, View.Y_AXIS);
			                } else if (kind.equals(StyleConstants.ComponentElementName)) {
			                    return new ComponentView(elem);
			                } else if (kind.equals(StyleConstants.IconElementName)) {
			                    return new IconView(elem);
			                }
			            }

			            // default to text display
			            return new LabelView(elem);
			        }

			    };
			}
		}); 
		modEditor.read(new FileInputStream(modFile), modFile);
		Document modDocument = modEditor.getDocument();
		modDocument.putProperty(PlainDocument.tabSizeAttribute, 4);

		// create tree view of right-hand mod editor
		// FIXME: remove tree (or move it elsewhere), it's here for testing purposes for now
		this.modTree = new ModTree(modDocument);
//			final JTree modElemTree = new JTree((TreeNode) modDocument.getDefaultRootElement()); // draw from document
		final JTree modElemTree = new JTree(modTree.getRoot()); // draw from ModTree
		JScrollPane modElemTreePane = new JScrollPane(modElemTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modElemTreePane.setPreferredSize(new Dimension(350, 300));
		// configure look and feel of tree viewer
		modElemTree.setRootVisible(false);
		modElemTree.putClientProperty("JTree.lineStyle", "Angled");
		modElemTree.setShowsRootHandles(false);
		// Display alternate operand text info for opened ModOperandNodes
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			
			@Override
			public Component getTreeCellRendererComponent(
									JTree tree,
									Object value,
									boolean sel,
									boolean expanded,
									boolean leaf,
									int row,
									boolean hasFocus) {

					if(value instanceof ModOperandNode) {
							((ModOperandNode) value ).expanded = expanded;
							if(expanded) {
								
							}
					} 
					super.getTreeCellRendererComponent(
									tree, value, sel,
									expanded, leaf, row,
									hasFocus);
					return this;
				}		
		};
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		modElemTree.setCellRenderer(renderer);	
			
		View modRootView = modEditor.getUI().getRootView(modEditor);
		TreeNode modViewRoot = this.createViewBranch(modRootView);	// sorry about the names :S
		
		final JTree modViewTree = new JTree(modViewRoot);
		JScrollPane modViewTreePane = new JScrollPane(modViewTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modViewTreePane.setPreferredSize(new Dimension(350, 300));
		
		
		// install document listener to refresh tree on changes to the document
		modDocument.addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void insertUpdate(DocumentEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void changedUpdate(DocumentEvent evt) {
				this.updateTree(evt);
			}
			/** Updates the tree views on document changes */
			private void updateTree(DocumentEvent evt) {
				// reset mod tree
				((DefaultTreeModel) modElemTree.getModel()).setRoot(
						modTree.getRoot());
				// reset view tree
				((DefaultTreeModel) modViewTree.getModel()).setRoot(
						createViewBranch(modEditor.getUI().getRootView(modEditor)));

				// expand trees
//				for (int i = 0; i < modElemTree.getRowCount(); i++) {
//					modElemTree.expandRow(i);
//				}
				for (int i = 0; i < modViewTree.getRowCount(); i++) {
					modViewTree.expandRow(i);
				}
			}
		});

		// expand trees
//		for (int i = 0; i < modElemTree.getRowCount(); i++) {
//			modElemTree.expandRow(i);
//		}
//		for (int i = 0; i < modViewTree.getRowCount(); i++) {
//			modViewTree.expandRow(i);
//		}
		
		JSplitPane modTreeSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, modElemTreePane, modViewTreePane);
		
		// wrap editors in split pane with vertical divider
//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, xmlPane, modPane);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, modTreeSplit, modPane);
		
		contentPane.add(splitPane);
	}
	
	/**
	 * Build a tree node from the provided View and recursively adds all its
	 * child views as child nodes.
	 * @param parentView the view to wrap in a tree node
	 * @return the tree node wrapping the view
	 */
	private TreeNode createViewBranch(View parentView) {
		TreeNode parentNode = new DefaultMutableTreeNode(parentView);
		int viewCount = parentView.getViewCount();
		for (int i = 0; i < viewCount; i++) {
			View childView = parentView.getView(i);
			if (childView != null) {
				TreeNode childNode = createViewBranch(childView);
				((DefaultMutableTreeNode) parentNode).add((MutableTreeNode) childNode);
			}
		}
		return parentNode;
	}
	
	/**
	 * Creates and configures the main frame's menu bar.
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar() {
		
		// create menu bar
		JMenuBar menuBar = new JMenuBar();
		
		// create file menu
		JMenu fileMenu = new JMenu("File");
		
		// create file menu items
		// TODO: add icons
		JMenuItem newItem = new JMenuItem("New");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JEditorPane xmlEditor = MainFrame.this.xmlEditor;
				// clear XML editor contents
				xmlEditor.setText(null);
				// TODO: do some other clean-up tasks in the future, possibly prompt for confirmation
			}
		});
		
		JMenuItem openItem = new JMenuItem("Open File...");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openItem.addActionListener(new BrowseActionListener(this, Constants.XML_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				try {
					// TODO: possibly prompt for confirmation
					JEditorPane xmlEditor = MainFrame.this.xmlEditor;
					xmlEditor.read(new FileReader(file), file);
					
					final PlainDocument xmlDocument = (PlainDocument) xmlEditor.getDocument();
					xmlDocument.putProperty(PlainDocument.tabSizeAttribute, 2);
					xmlDocument.putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));
					
					xmlEditor.addCaretListener(new CaretListener() {
						@Override
						public void caretUpdate(CaretEvent evt) {
							int dot = evt.getDot();
							Element line = xmlDocument.getParagraphElement(dot);

							XMLScanner scanner;
							try {
								scanner = new XMLScanner(line.getDocument());
								scanner.setRange(line.getStartOffset(), line.getEndOffset());
								
								while (scanner.getEventType() != XMLEvent.END_ELEMENT) {
									scanner.scan();

									int startOffset = scanner.getStartOffset();
									int endOffset = scanner.getEndOffset();
									String text = xmlDocument.getText(startOffset, endOffset - startOffset);
									
									if (XMLStyleConstants.ELEMENT_NAME.equals(scanner.token)) {
										if (!"file".equals(text)) {
											break;
										}
									} else if (XMLStyleConstants.ATTRIBUTE_NAME.equals(scanner.token)) {
										if (!"name".equals(text)) {
											break;
										}
									} else if (XMLStyleConstants.ATTRIBUTE_VALUE.equals(scanner.token)) {
										JEditorPane modEditor = MainFrame.this.modEditor;
										// clear modfile editor
										modEditor.setText(null);
										// trim leading/trailing quotation marks
										String filename = text.replaceAll("^\"|\"$", "");
										// create file descriptor
										File file = new File(filename);
										// check whether file actually exists
										if (file.exists()) {
											// parse file into editor
											modEditor.read(new FileReader(file), file);
										} else {
											// show error text
											modEditor.setText("ERROR: File \'" + filename + "\' could not be found.");
										}
										// re-apply styling
										modEditor.getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
										
										break;
									}
								}
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					});
					
					// repaint parent (or rather great-grandparent) scroll pane to update row header
					xmlEditor.getParent().getParent().getParent().repaint();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		JMenuItem saveItem = new JMenuItem("Save File...");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveItem.setEnabled(false);
		
		JMenuItem exportItem = new JMenuItem("Export...");
		exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		exportItem.setEnabled(false);
		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// dispose main frame (thereby terminating the application)
				MainFrame.this.dispose();
			}
		});
		
		// TODO: implement functionality for other items
		
		// add items to file menu
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		fileMenu.add(exportItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		// create edit menu
		JMenu editMenu = new JMenu("Edit");
		
		JMenuItem updateItem = new JMenuItem("Update References...");
		updateItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				showRefUpdateDialog();
			}
		});
		
		// add items to edit menu
		editMenu.add(updateItem);
		
		// create help menu
		JMenu helpMenu = new JMenu("Help");
		
		// create help menu items
		JMenuItem helpItem = new JMenuItem("Help");
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpItem.setEnabled(false);
		
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				showAboutDialog();
			}
		});
		
		// add items to help menu
		helpMenu.add(helpItem);
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);
		
		// add menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	/**
	 * TODO: API
	 */
	private void showRefUpdateDialog() {
		new ReferenceUpdateDialog(this.modTree).setVisible(true);
	}

	/**
	 * Creates and displays the application's About dialog.
	 */
	private void showAboutDialog() {
		// TODO: prettify dialog... like, really, it's fugly
		JDialog aboutDlg = new JDialog(this, "About UPK Modder", true);
		
		Container contentPane = aboutDlg.getContentPane();
		
		JLabel aboutLbl = new JLabel(Constants.ABOUT_TEXT);
		aboutLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		contentPane.add(aboutLbl);
		
		aboutDlg.pack();
		aboutDlg.setResizable(false);
		aboutDlg.setLocationRelativeTo(this);
		aboutDlg.setVisible(true);
	}
	
}
