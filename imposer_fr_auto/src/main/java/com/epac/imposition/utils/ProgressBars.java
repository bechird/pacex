package com.epac.imposition.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.hibernate.cfg.SetSimpleValueTypeSecondPass;

public final class ProgressBars extends JFrame {

    private JProgressBar myProgressBar;
	private JLabel label;


    // STEP 2 ---------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------
    private class MyTask extends SwingWorker<Void, Void> {

          public Void doInBackground(){
                // Take 3 seconds to calculate the task's length (the call "Thread.sleep(3000)" simulates the calculation)
                try{
                      Thread.sleep(3000);
                }
                catch(InterruptedException e){}


                // Start incrementing the progress property when the task's length is known
                for(int i=0; i<=100; ++i){
                      try{
                            Thread.sleep(30);

                            // Update the "progress" property (the value should always be between 0 and 100)
                            setProgress(i);
                      }
                      catch(InterruptedException e){}
                }
                return null;
          }

    }
    // ------------------------------------------------------------------------------------------------------------


    public ProgressBars(){
          init();
          addComponents();

          setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          setSize(390, 170);
          setVisible(true);

          // Create and run the task when the frame is visible
          //createAndRunTask();
    }

    public void setLabel(String text){
    	label.setForeground(Color.BLACK);
    	label.setText(text);
    }

    private void addComponents() {
          // Create the content pane
          JPanel contentPane = new JPanel();
          contentPane.setLayout(new BorderLayout(10,10));
          label = new JLabel();
          label.setText("Initializing...");
        
          // STEP 1 ----------------------------------------------------------------------------------------------
          // -------------------------------------------------------------------------------------------------------
          // Set up the progress bar
          myProgressBar = new JProgressBar(0, 100);
          myProgressBar.setValue(0);
          myProgressBar.setStringPainted(true);
          myProgressBar.setIndeterminate(true); // Set the indeterminate property to true
          myProgressBar.setString(""); // Do not display the percentage

          myProgressBar.setLocation(0, 50);
          // Add the progress bar to the content pane
          contentPane.add(label, BorderLayout.NORTH);
          contentPane.add(myProgressBar, BorderLayout.SOUTH);
          
          // -------------------------------------------------------------------------------------------------------


          // Add the content pane to the JFrame
          contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
          getContentPane().add(contentPane, BorderLayout.CENTER);
          JButton close = new JButton("Close");
          JPanel btnPanel = new JPanel();
          btnPanel.add(close);
          btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
          getContentPane().add(btnPanel, BorderLayout.SOUTH);
          close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
      
    }


    // STEP 3 ---------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------
    private void createAndRunTask(){
          // Create the SwingWorker subclass instance
          MyTask myTask = new MyTask();

          // Register a PropertyChangeListener on it
          myTask.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                      if(e.getPropertyName().equals("progress")){
                            int progress = (Integer) e.getNewValue();

                            if(progress == 0){
                                  myProgressBar.setIndeterminate(true);
                            }
                            else{
                                  myProgressBar.setIndeterminate(false);
                                  myProgressBar.setString(null); // Display the percentage

                                  // Update the progress bar's value with the value of the progress property.
                                  myProgressBar.setValue(progress);
                            }
                      }
                }
          });

          // Call the method execute to run the task in a background thread
          myTask.execute();
    }
    // -------------------------------------------------------------------------------------------------------------


    private void init() {
          try{
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
          }
          catch(Exception e){
                e.printStackTrace();
          }

          setTitle("Imposition tool ");
          setLocationRelativeTo(null);
    }


    public static void main(String[] args){
          SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                      new ProgressBars();
                }
          });
    }

	public void setError(String string) {
		label.setForeground(Color.RED);
		label.setText(string);
	}

	public void stop() {
		myProgressBar.setIndeterminate(false);
		myProgressBar.setValue(100);
	}
}