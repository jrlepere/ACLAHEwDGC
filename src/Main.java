import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import transformations.ACLAHE;
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
		
		// add left and right split image components
		frame.add(getImageSplitPanel());
		frame.add(getImageSplitPanel());
		
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
		float[][][] defaultImage = Utilities.getDefaultImage();
		
		// transformations
		ITransformation[] transformations = new ITransformation[] {
			new NoTransformation(defaultImage, imageLabel)	,
			new HistogramEqualization(defaultImage, imageLabel),
			new CLAHE(defaultImage, imageLabel),
			new ACLAHE(defaultImage, imageLabel)
		};
		
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
	
	private static final String FRAME_TITLE = "ACLAHEwDGC";
	private static final int FRAME_WIDTH = 1050;
	private static final int FRAME_HEIGHT = 675;
	
}