import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import transformations.ACLAHE;
import transformations.ACLAHEwDGC;
import transformations.CLAHE;
import transformations.HistogramEqualization;
import transformations.ITransformation;
import transformations.NoTransformation;
import utils.Utilities;

/**
 * The main class for ACLAHEwDGC application.
 * This program implements Automatic Contract Limited Adaptive Histogram Equalization with Dual Gamma Correction,
 * as proposed in Chang, Jung, Ke, Song and Hwang's paper.
 * @author JLepere2
 * @date 05/07/2018
 */
public class Main {
	
	/**
	 * The main method to run the application.
	 * @param args Command line arguments, not used.
	 */
	public static void main(String[] args) {
		
		// main frame for the application
		JFrame frame = new JFrame(FRAME_TITLE);
		frame.setLayout(new GridLayout(1, 2));
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// List for all the transformations to alert when a new image has been loaded
		allTransformations = new LinkedList<>();
		
		// stores the combo boxes to repaint on image change
		transformationComboBoxes = new LinkedList<>();
		
		// add left and right split image components
		frame.add(getImageSplitPanel());
		frame.add(getImageSplitPanel());
		
		// set the menu bar
		frame.setJMenuBar(getMenuBar());
		
		// show the frame
		frame.setVisible(true);
		
	}
	
	/**
	 * Creates the full image panel with Transformation selection box, parameter selection panel and image.
	 * @param m the Model component
	 */
	private static JPanel getImageSplitPanel() {
		
		/*
		 * - Combo Box
		 * - Parameter Selection Panel
		 * - Image
		 */
		
		// the main panel for this full split component
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// the image specific panel
		JPanel imagePanel = new JPanel(new BorderLayout());
		
		// the panel for the transformation specific parameter selection with a card layout
		JPanel parameterSelectionPanel = new JPanel(new CardLayout());
		
		// label for the image
		JLabel imageLabel = new JLabel();
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		imageLabel.setVerticalAlignment(JLabel.CENTER);
		
		// default image
		float[][][] defaultImage = Utilities.getImage(Utilities.images[0]);
		
		// transformations
		ITransformation[] transformations = new ITransformation[] {
			new NoTransformation(defaultImage, imageLabel)	,
			new HistogramEqualization(defaultImage, imageLabel),
			new CLAHE(defaultImage, imageLabel),
			new ACLAHE(defaultImage, imageLabel),
			new ACLAHEwDGC(defaultImage, imageLabel)
		};
		
		// add the transformations in this split panel to the global transformation holder
		for (ITransformation t : transformations) allTransformations.add(t);
		
		// add parameter selection panel for each transformation to card layout
		for (ITransformation transform : transformations) {
			parameterSelectionPanel.add(transform.getParameterPanel(), transform.toString());
		}
		
		// combo box
		JComboBox<ITransformation> transformationSelectionBox = new JComboBox<>(transformations);
		transformationSelectionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get the selected transformation
				ITransformation selectedTransformation = transformations[transformationSelectionBox.getSelectedIndex()];
				
				// change the parameter selection panel card based on the selected transformation
				CardLayout cl = (CardLayout) (parameterSelectionPanel.getLayout());
			    cl.show(parameterSelectionPanel, selectedTransformation.toString());
				
				// transform the image
				selectedTransformation.transform();
			}
		});
		
		// add combo box to list for image repainting
		transformationComboBoxes.add(transformationSelectionBox);
		
		// initialize the selection
		transformationSelectionBox.setSelectedIndex(0);
		
		// add components to image panel
		imagePanel.add(parameterSelectionPanel, BorderLayout.SOUTH);
		imagePanel.add(imageLabel, BorderLayout.CENTER);
		
		// add components to the main panel
		mainPanel.add(transformationSelectionBox, BorderLayout.NORTH);
		mainPanel.add(imagePanel, BorderLayout.CENTER);
		
		// return the panel
		return mainPanel;
		
	}
	
	public static JMenuBar getMenuBar() {
		
		/*
		 * Menu Bar
		 *   - File Menu
		 *      - Images
		 *         - Buzz.jpg
		 *         - Waterfal.jpg
		 *         - ... 
		 */
		
		// menus
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu imageMenu = new JMenu("Images");
		
		// add a menu item for each image 
		for (String imageName : Utilities.images) {
			
			// the menu item for this image
			JMenuItem imageItem = new JMenuItem(imageName);
			
			// notifies each transformation that a new image has been loaded
			imageItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// gets the image as hsb
					float[][][] hsbImage = Utilities.getImage(imageItem.getText());
					// notifies each transformation
					for (ITransformation t : allTransformations) t.newImage(hsbImage);
					// alert the combo boxes of a change so the current algorithm can execute on the new image
					for (JComboBox<ITransformation> comboBox : transformationComboBoxes) {
						comboBox.setSelectedIndex(comboBox.getSelectedIndex());
					}
				}
			});
			
			// add this menu item
			imageMenu.add(imageItem);
		}
		
		// compile menus
		fileMenu.add(imageMenu);
		menuBar.add(fileMenu);
		
		return menuBar;
	}
	
	private static final String FRAME_TITLE = "ACLAHEwDGC";
	private static final int FRAME_WIDTH = 1050;
	private static final int FRAME_HEIGHT = 675;
	private static List<ITransformation> allTransformations;
	private static List<JComboBox<ITransformation>> transformationComboBoxes;
	
}