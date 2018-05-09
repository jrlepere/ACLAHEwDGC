package utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Creates a panel that contains a label, a slider and a current value label
 * @author JLepere2
 * @date 05/09/2018
 */
public class SliderPanel extends JPanel {

	/**
	 * Creates a slider panel with a label, a slider and a current value label.
	 * @param parameterName the name of the parameter
	 * @param initialValueIndex the index of the initial value
	 * @param values an array of possible values for the slider
	 */
	public SliderPanel(String parameterName, int[] values, int initialValueIndex) {
		super(new BorderLayout());
		
		// current value
		currentValue = values[initialValueIndex];
		
		// parameter name label
		JLabel parameterNameLabel = new JLabel(" " + parameterName + ": ");
		parameterNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		parameterNameLabel.setPreferredSize(new Dimension(75, 25));
		
		// label for the current value
		JLabel currentValueLabel = new JLabel(" " + currentValue + " ");
		currentValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		currentValueLabel.setPreferredSize(new Dimension(35, 25));
		
		// slider
		int minValue = 0;
		int maxValue = values.length - 1;
		valueSlider = new JSlider(minValue, maxValue, initialValueIndex);
		valueSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				currentValue = values[valueSlider.getValue()];
				currentValueLabel.setText(""+currentValue);
			}
		});
		
		// add components to panel
		this.add(parameterNameLabel, BorderLayout.WEST);
		this.add(valueSlider, BorderLayout.CENTER);
		this.add(currentValueLabel, BorderLayout.EAST);
		
	}
	
	/**
	 * Gets the current value of the slider.
	 * @return the current value of the slider
	 */
	public int getCurrentValue() {
		return currentValue;
	}
	
	/**
	 * Add a change listener to this slider
	 * @param l the change listener
	 */
	public void addChangeListener(ChangeListener l) {
		valueSlider.addChangeListener(l);
	}
	
	private int currentValue;
	private JSlider valueSlider;
	private static final long serialVersionUID = 1L;
	
}
