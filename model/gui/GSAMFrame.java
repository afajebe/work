package model.gui;

import core.InteractiveLargeScaleModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JOptionPane;
import core.SimulationProperties;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import java.io.File;

public class GSAMFrame extends JFrame implements GSAMGui {

    private InteractiveLargeScaleModel gsam;

    private GSAMGuiPanel guiPanel;

    private JMenuBar menuBar;

    private JMenu placeMenu;

    private JMenu interventionMenu;

    private JMenu antiviralMenu;

    private JMenuItem pauseRun;

    private JMenuItem resumeRun;

    private JMenu vaccinationMenu;

    private JMenuItem vaccinationValue;

    private JRadioButtonMenuItem vaccinationState;

    private JMenu socialDistanceMenu;

    private JMenu informationMenu;

    private JRadioButtonMenuItem informationState;

    private JMenu r0Menu;

    private JRadioButtonMenuItem[] roValues;
    private ButtonGroup r0ButtonGroup;

    private JRadioButtonMenuItem antiviralOption1;
    private JRadioButtonMenuItem antiviralOption2;
    private JRadioButtonMenuItem antiviralOption3;
    private ButtonGroup antiviralButtonGroup;

    private JRadioButtonMenuItem vaccinationOption1;
    private JRadioButtonMenuItem vaccinationOption2;
    private JRadioButtonMenuItem vaccinationOption3;
    private ButtonGroup vaccinationButtonGroup;

    private JRadioButtonMenuItem socialDistanceOption1;
    private JRadioButtonMenuItem socialDistanceOption2;
    private JRadioButtonMenuItem socialDistanceOption3;
    private ButtonGroup socialDistanceButtonGroup;

    private JRadioButtonMenuItem infoOn;
    private JRadioButtonMenuItem infoOff;
    private ButtonGroup infoButtonGroup;

    private JButton pauseButtonMenu;
    private JButton pauseButton;
    private JButton startButtonMenu;
    private JButton startButton;

    private int numberOfAntivirals;
    private int numberOfVaccines;
    private double roValue;
    private double socialDistanceValue;
    private boolean informationValue;

    String roundedValue;

    AntiviralRadioListener antiviralSelectedListener;
    VaccinationRadioListener vaccinationSelectedListener;
    SocialDistanceRadioListener socialDistanceSelectedListener;
    R0RadioListener r0SelectedListener;
    InformationRadioListener informationSelectedListener;
    StartButtonListener startSelectedListener;
    PauseButtonListener pauseSelectedListener;

    class StartButtonListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {

            if ("Start".equals(startButton.getText())) {
                // onOff = 0; // turn it off next instance
                startButton.setText("End");
                pauseButton.setVisible(true);
            } else {
                startButton.setText("Start");
                pauseButton.setVisible(false);
                // need to reset the pause button here
            }
        }
        
    }

    class PauseButtonListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if ("Pause".equals(pauseButton.getText())) {
                pauseButton.setText("Resume");
            } else {
                pauseButton.setText("Pause");
            }
        }
    }
    // Listens to the antiviral radio buttons
    class AntiviralRadioListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();

            switch (button.getText()) {
                case "None":
                    guiPanel.setAntiviralDrug("None");
                    break;
                case "Limited":
                    JFrame frame = new JFrame();
                    String result = JOptionPane.showInputDialog(frame,
                            "Enter number of antiviral dosages: ",
                            "Number of antiviral drugs available for the population", 1);
                    try {
                        numberOfAntivirals = Integer.parseInt(result);
                        guiPanel.setAntiviralDrug("Limited, " + result);

                    } catch (NumberFormatException err) {
                        //none integer value selected
                        String msg = "The value you entered is not an INTEGER.";
                        JOptionPane.showMessageDialog(null, msg, "Error", 1);
                    }
                    break;
                case "Random":
                    guiPanel.setAntiviralDrug("Random");
                    break;
            }
        }
    }

    // Listens to the vaccination radio buttons
    class VaccinationRadioListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();

            switch (button.getText()) {
                case "None":
                    guiPanel.setVaccination("None");
                    break;
                case "Limited":
                    JFrame frame = new JFrame();
                    String result = JOptionPane.showInputDialog(frame,
                            "Enter number of vaccine courses: ",
                            "Number of vaccines courses available for the population", 1);
                    try {
                        numberOfVaccines = Integer.parseInt(result);
                        guiPanel.setVaccination("Limited, " + result);

                    } catch (NumberFormatException err) {
                        //none integer value selected
                        String msg = "The value you entered is not an INTEGER.";
                        JOptionPane.showMessageDialog(null, msg, "Error", 1);
                    }
                    break;
                case "Random":
                    guiPanel.setVaccination("Random");
                    break;
            }
        }
    }

    // Listens to the social distance radio buttons
    class SocialDistanceRadioListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();

            switch (button.getText()) {
                case "None":
                    guiPanel.setSocialDist("None");
                    break;
                case "25%":
                    guiPanel.setSocialDist("25%");
                    break;
                case "75%":
                    guiPanel.setSocialDist("50%");
                    break;
            }
        }
    }

    // Listens to the r0 radio buttons
    class R0RadioListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();

            guiPanel.setR0(button.getText());
        }
    }

    // Listens to the information radio buttons
    class InformationRadioListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();

            switch (button.getText()) {
                case "Yes":
                    guiPanel.setInformation("Yes");
                    break;
                case "No":
                    guiPanel.setInformation("No");
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    public GSAMFrame() {
        super("Behavior - Disease Computational Model");
        this.setSize(980, 900);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.guiPanel = new GSAMGuiPanel();
        this.setContentPane(guiPanel);

        roValue = 0;

        addMenuBar();

//		this.numSickChart = new LiveUpdateChart("Number Sick", "Time", "Number Currently Sick");
//		this.newlySickChart = new LiveUpdateChart("Number Newly Infected", "Time", "Number Recently Infected");
//		JPanel panel = new JPanel();
//		panel.add(numSickChart.getChart());
//		numSickChart.getChart().setPreferredSize(new Dimension(450,300));
//		panel.add(newlySickChart.getChart());
//		newlySickChart.getChart().setPreferredSize(new Dimension(450,300));
//		this.setContentPane(panel);
        this.setVisible(true);
    }

    private void addMenuBar() {

        this.menuBar = new JMenuBar();
        this.placeMenu = buildPlaceMenu();

        this.startButtonMenu = buildStartButton();
        this.pauseButtonMenu = buildPauseButton();

        this.antiviralButtonGroup = new ButtonGroup();
        this.vaccinationButtonGroup = new ButtonGroup();
        this.socialDistanceButtonGroup = new ButtonGroup();
        this.r0ButtonGroup = new ButtonGroup();
        this.infoButtonGroup = new ButtonGroup();
        //this.interventionMenu = buildInterventionMenu();
        this.socialDistanceMenu = buildSocialDistanceMenu();
        this.antiviralMenu = buildAntiviralMenu();
        this.informationMenu = buildInformationMenu();
        this.vaccinationMenu = buildVaccinationMenu();
        this.r0Menu = buildR0Menu();

        // menuBar.add(placeMenu); // not needed
        menuBar.add(startButtonMenu);
        menuBar.add(pauseButtonMenu);
        menuBar.add(informationMenu);
        // menuBar.add(interventionMenu);
        menuBar.add(socialDistanceMenu);
        menuBar.add(antiviralMenu);
        menuBar.add(vaccinationMenu);
        menuBar.add(r0Menu);
        menuBar.add(Box.createHorizontalGlue());

        JMenu runControl = new JMenu("Pause/UnPause");
        this.pauseRun = new JMenuItem("Pause Run");
        pauseRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gsam.pause();
                resumeRun.setEnabled(true);
                pauseRun.setEnabled(false);
            }
        });
        runControl.add(pauseRun);

        this.resumeRun = new JMenuItem("Resume Run");
        resumeRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gsam.unpause();
                pauseRun.setEnabled(true);
                resumeRun.setEnabled(false);
            }
        });
        runControl.add(resumeRun);

        // menuBar.add(runControl);
        this.setJMenuBar(menuBar);

    }

    private JMenu buildPlaceMenu() {

        JMenu places = new JMenu("State/Nation Options");

        JMenuItem onlyUS = new JMenuItem("Full US Population");
        onlyUS.setEnabled(false);
        places.add(onlyUS);

        return places;
    }

    private JMenu buildInterventionMenu() {
        JMenu interventions = new JMenu("Social Distancing");

        JMenuItem noSocialDist = new JMenuItem("No Social Distancing");
        noSocialDist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gsam.setSocialDistance(0.0);
            }
        });
        interventions.add(noSocialDist);

        JMenuItem socialDist25 = new JMenuItem("25% Social Distancing");
        socialDist25.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gsam.setSocialDistance(.25);
            }
        });
        interventions.add(socialDist25);

        JMenuItem socialDist50 = new JMenuItem("50% Social Distancing");
        socialDist50.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gsam.setSocialDistance(.5);
            }
        });
        interventions.add(socialDist50);

        return interventions;
    }

    private JMenu buildSocialDistanceMenu() {
        JMenu socialDist = new JMenu("Social Distance");

        socialDistanceOption1 = new JRadioButtonMenuItem("None");
        socialDistanceOption2 = new JRadioButtonMenuItem("25%");
        socialDistanceOption3 = new JRadioButtonMenuItem("50%");

        socialDistanceButtonGroup.add(socialDistanceOption1);
        socialDistanceButtonGroup.add(socialDistanceOption2);
        socialDistanceButtonGroup.add(socialDistanceOption3);

        socialDist.add(socialDistanceOption1);
        socialDist.add(socialDistanceOption2);
        socialDist.add(socialDistanceOption3);

        // register an action listener for the radio buttons
        socialDistanceSelectedListener = new SocialDistanceRadioListener();
        socialDistanceOption1.addActionListener(socialDistanceSelectedListener);
        socialDistanceOption2.addActionListener(socialDistanceSelectedListener);
        socialDistanceOption3.addActionListener(socialDistanceSelectedListener);
        
        socialDist.setToolTipText("Set the social distance");

        return socialDist;
    }

    private JMenu buildAntiviralMenu() {
        JMenu antiviral = new JMenu("Antiviral Drugs");

        antiviralOption1 = new JRadioButtonMenuItem("None");
        antiviralOption2 = new JRadioButtonMenuItem("Limited");
        antiviralOption3 = new JRadioButtonMenuItem("Random");

        antiviralButtonGroup.add(antiviralOption1);
        antiviralButtonGroup.add(antiviralOption2);
        antiviralButtonGroup.add(antiviralOption3);

        antiviral.add(antiviralOption1);
        antiviral.add(antiviralOption2);
        antiviral.add(antiviralOption3);

        // register an action listener for the radio buttons
        antiviralSelectedListener = new AntiviralRadioListener();
        antiviralOption1.addActionListener(antiviralSelectedListener);
        antiviralOption2.addActionListener(antiviralSelectedListener);
        antiviralOption3.addActionListener(antiviralSelectedListener);
        
        antiviral.setToolTipText("Set the antiviral dosage to use");

        return antiviral;
    }

    private JMenu buildInformationMenu() {
        JMenu information = new JMenu("Information");

        infoOn = new JRadioButtonMenuItem("Yes");
        infoOff = new JRadioButtonMenuItem("No");

        infoButtonGroup.add(infoOn);
        infoButtonGroup.add(infoOff);

        information.add(infoOn);
        information.add(infoOff);

        // register an action listener for the radio buttons
        informationSelectedListener = new InformationRadioListener();
        infoOn.addActionListener(informationSelectedListener);
        infoOff.addActionListener(informationSelectedListener);
        information.setToolTipText("Select the information mode");
       
        return information;
    }

    private JMenu buildVaccinationMenu() {
        JMenu vaccination = new JMenu("Vaccinaton");

        vaccinationOption1 = new JRadioButtonMenuItem("None");
        vaccinationOption2 = new JRadioButtonMenuItem("Limited");
        vaccinationOption3 = new JRadioButtonMenuItem("Random");

        vaccinationButtonGroup.add(vaccinationOption1);
        vaccinationButtonGroup.add(vaccinationOption2);
        vaccinationButtonGroup.add(vaccinationOption3);

        // register an action listener for the radio buttons
        vaccinationSelectedListener = new VaccinationRadioListener();
        vaccinationOption1.addActionListener(vaccinationSelectedListener);
        vaccinationOption2.addActionListener(vaccinationSelectedListener);
        vaccinationOption3.addActionListener(vaccinationSelectedListener);

        vaccination.add(vaccinationOption1);
        vaccination.add(vaccinationOption2);
        vaccination.add(vaccinationOption3);
        
        vaccination.setToolTipText("Set the vaccination course to use");

        return vaccination;
    }

    private JButton buildStartButton() {
        
//        File imageCheck = new File("start-icon.png");
//        if (imageCheck.exists()) {
//            System.out.println("Image file found!");
//
//        } else {
//            System.out.println("Image file not found!");
//        }
        
//        BufferedImage img = null;
//        try {
//            img = ImageIO.read(getClass().getResource("images/start-icon.png"));
//            //startButton.setIcon(new ImageIcon(img));
//            Icon startIcon = new ImageIcon("start-icon.png");
//            startButton = new JButton("Start", startIcon);
//        } catch (Exception ex) {
//            // no icon
//             startButton = new JButton("Start");
//        }

        // initialize button & register an action listener for it
        startButton = new JButton("Start");
        startSelectedListener = new StartButtonListener();
        startButton.addActionListener(startSelectedListener);
        
        return startButton;
    }
    
    private JButton buildPauseButton() {
        // intialize pause button & register an action listener for it
        pauseButton = new JButton("Pause");
        pauseSelectedListener = new PauseButtonListener();
        pauseButton.addActionListener(pauseSelectedListener);
        pauseButton.setVisible(false);
        
        return pauseButton;
    }

    private JMenu buildR0Menu() {
        JMenu r0 = new JMenu("R0 value");

        double arrayStartValue = 1;
        double arrayEndValue = 3;
        double arraySpan = arrayEndValue - arrayStartValue;

        double stepSize = 0.2;
        double tempSize = arraySpan / stepSize;
        int arraySize = (int) tempSize;
        int precision = 100; // keep 2 digits precision

        roValues = new JRadioButtonMenuItem[arraySize + 1];
        r0SelectedListener = new R0RadioListener();
        // make the sub-menu
        for (int count = 1; count < roValues.length; count++) {

            arrayStartValue += stepSize;
            roValue = Math.floor(arrayStartValue * precision + .5) / precision;

            roundedValue = Double.toString(roValue);
            roValues[count] = new JRadioButtonMenuItem(roundedValue);
            roValues[count].setActionCommand(String.valueOf(count)); // I think I need to remove this
            r0ButtonGroup.add(roValues[count]);

            // register an action listener for the radio buttons
            roValues[count].addActionListener(r0SelectedListener);

            // bind it to the object to be returned
            r0.add(roValues[count]);
        }
        
        r0.setToolTipText("Select a Basic Reproductive Number, R0");

        return r0;
    }
    
    private void checkCurrentConfiguration() {
        // ensure that the config is set before running program
        
    }

    public void addGSAM() {

        this.gsam = new InteractiveLargeScaleModel(
                SimulationProperties.getDefaultProperties());

        gsam.addGui(this);
    }

    public void beginModeling() {
        gsam.beginModel();
    }

    @Override
    public void updateNumSick(int numCurrentlySick) {
        guiPanel.updateNumSick(numCurrentlySick);
        //this.numSickChart.addDataPoint(numCurrentlySick);
    }

    @Override
    public void updateTotalSick(int numNewlySick) {
        guiPanel.updateTotalSick(numNewlySick);
        //this.newlySickChart.addDataPoint(numNewlySick);
    }

    @Override
    public void updateMap(BufferedImage image) {
        guiPanel.updateMap(image);
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
