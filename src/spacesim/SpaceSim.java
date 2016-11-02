/**
 * The main SpaceSim file, which contains the main method, defines the GUI, and
 * performs the time step.
 * 
 * The functionalities that currently exist are:
 * Start/pause
 * Add planets
 * View paths as lines or shadows
 * Zoom and change simulation speed
 * Change the force law
 * Select bodies by clicking on them
 * Modify the selected body's attributes
 * Add custom bodies
 * Have the camera follow a body
 */

package spacesim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import spacesim.Body.Shadow;

public class SpaceSim extends javax.swing.JFrame {
    public static double GRAV_CONST = .5; // Was 0.5 
    private double massOfSun = 500; // Was 10000 to be realistic, 500
    private Body sun;
    public CopyOnWriteArrayList<Body> bodies;
    
    private double t=0, dt=0.5;
    private Date simDate;
    private TimerTask gameTimerTask;
    private int calcFreq = 5;
    private int loopTime = 0;
    
    private int cubeL = 10000;
    
    private int clickX, clickY;
    private int dWindowX, dWindowY;
    private double zZoomBase = 1.001;
    //private double zZoomPower = 1.0/3.0;
    
    private Body selectedBody;
    private String[] names = Names.names;
    private int nameCnt = 0;
    private int sunCnt = 1;
    
    public int forceLaw = -2;
    
    private HashMap<String, Color> colors = Names.generateColors();
    
    /**
     * Initialize the simulation. Starts off with only a sun
     */
    public SpaceSim() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        initComponents();
        
        bodies = new CopyOnWriteArrayList<>();
        addBody("Sun", massOfSun, .001, 0, 0, 0, 0, 0, 0);
        sun = bodies.get(0);
        
        for (String c : colors.keySet()) {
            colorComboBoxEdit.addItem(c);
            colorComboBoxAdd.addItem(c);
        }
        colorComboBoxAdd.setSelectedItem("Black");
        
        setEditor(bodies.get(0));
        simDate = new Date(0);
        setupKeyMaps();
    }
    
    /**
     * Controls the keys in the GUI:
     * Space - starts and pauses the game
     * Escape - deselect any bodies
     */
    private void setupKeyMaps() {
        JPanel c = (JPanel) ((JLayeredPane) ((JRootPane) this.getComponents()[0]).getComponents()[1]).getComponents()[0];
        for (Component com : c.getComponents()) {
            if (com.getClass() == JButton.class || com.getClass() == JCheckBox.class || com.getClass() == JToggleButton.class) {
                ((JComponent) com).getInputMap(JComponent.WHEN_FOCUSED).put(
                        KeyStroke.getKeyStroke("SPACE"), "none");
            }
            if (com.getClass() == JTabbedPane.class) {
                for (Component com2 : ((JTabbedPane) com).getComponents()) {
                    for (Component com3 : ((JPanel) com2).getComponents()) {
                        if (com3.getClass() == JButton.class || com3.getClass() == JCheckBox.class || com3.getClass() == JToggleButton.class) {
                            ((JComponent) com3).getInputMap(JComponent.WHEN_FOCUSED).put(
                                    KeyStroke.getKeyStroke("SPACE"), "none");
                        }
                    }
                }
            }
        }
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("SPACE"), "spacePressed");
        getRootPane().getActionMap().put("spacePressed", new AbstractAction(){
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                changeButton.doClick();
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "escapePressed");
        getRootPane().getActionMap().put("escapePressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                bodiesComboBox.setSelectedIndex(-1);
            }
        });
        
        sunRadioButton.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        sunRadioButton.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                sunRadioButton.setSelected(true);
            }
        });
        
        planetRadioButton.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        planetRadioButton.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                planetRadioButton.setSelected(true);
            }
        });
        
        addNewBodyButton.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        addNewBodyButton.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewBodyButton.doClick();
            }
        });
        
        bodiesComboBox.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        bodiesComboBox.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                bodiesComboBox.setPopupVisible(!bodiesComboBox.isPopupVisible());
            }
        });
        
        colorComboBoxAdd.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        colorComboBoxAdd.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                colorComboBoxAdd.setPopupVisible(!colorComboBoxAdd.isPopupVisible());
            }
        });
        
        moveableCheckBoxEdit.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        moveableCheckBoxEdit.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                moveableCheckBoxEdit.setSelected(!moveableCheckBoxEdit.isSelected());
            }
        });
        
        moveableCheckBoxAdd.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterPressed");
        moveableCheckBoxAdd.getActionMap().put("enterPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                moveableCheckBoxAdd.setSelected(!moveableCheckBoxAdd.isSelected());
            }
        });
    }
    
    /**
     * Perform the time step
     */
    private void step() {
        // Set up the times
        loopTime += calcFreq;
        t += dt*speedSlider.getValue();
        simDate.setTime(loopTime);
        
        if (loopTime % 250 == 0) {
            timeField.setValue(simDate);
        }
        
        mainPanel.repaint();
        
        // Check for collisions
        ArrayList<Body> toBeRemoved = new ArrayList<>();
        for (Body b1 : bodies) {
            for (Body b2 : bodies) {
                if (b1 != b2 && areTouching(b1, b2)) {
                    Body big = b1.mass > b2.mass ? b1 : b2;
                    Body small = b1 == big ? b2 : b1;
                    
                    if (toBeRemoved.contains(small)) {
                        continue;
                    } else {
                        toBeRemoved.add(small);
                    }
                    
                    double newvx = (big.state.vx*big.mass+small.state.vx*small.mass)/(big.mass+small.mass);
                    double newvy = (big.state.vy*big.mass+small.state.vy*small.mass)/(big.mass+small.mass);
                    double newvz = (big.state.vz*big.mass+small.state.vz*small.mass)/(big.mass+small.mass);
                    double newden = (big.DENSITY*big.mass+small.DENSITY*small.mass)/(big.mass+small.mass);
                    big.mass += small.mass;
                    big.DENSITY = newden;
                    big.setRadiusFromMass();
                    big.state.vx = newvx;
                    big.state.vy = newvy;
                    big.state.vz = newvz;
                }
            }
            
            // Check for out of bounds
            if (!toBeRemoved.contains(b1)) {
                if (Math.abs(b1.state.x) > cubeL || Math.abs(b1.state.y) > cubeL
                        || Math.abs(b1.state.z) > cubeL/2) {
                    toBeRemoved.add(b1);
                }
            }
        }
        
        // Remove all that need to be removed
        for (Body b : toBeRemoved) {
            bodies.remove(b);
            totalBodiesCounter.setText(""+bodies.size());
            bodiesComboBox.removeItem(b.name);
        }
        
        // Calculate the next position
        for (Body b : bodies) {
            if (b.moveable) {
                b.nextState.x = b.state.x;
                b.nextState.y = b.state.y;
                b.nextState.z = b.state.z;
                b.nextState.vx = b.state.vx;
                b.nextState.vy = b.state.vy;
                b.nextState.vz = b.state.vz;
                
                b.updateBody(t, dt*speedSlider.getValue());
                if (pathsComboBox.getSelectedItem().equals("Lines") && loopTime % 20 == 0) {
                    b.addPos();
                } else if (pathsComboBox.getSelectedItem().equals("Dots") && loopTime % 100 == 0) {
                    b.addPos();
                }
            }
        }
        
        // Set each next position
        for (Body b : bodies) {
            if (b.moveable) {
                b.state.x = b.nextState.x;
                b.state.y = b.nextState.y;
                b.state.z = b.nextState.z;
                b.state.vx = b.nextState.vx;
                b.state.vy = b.nextState.vy;
                b.state.vz = b.nextState.vz;
            }
        }
        
        // Update the edit window
        Body bufferedBody = selectedBody;
        if (bufferedBody != null) {
            if (loopTime % 50 == 0) {
                xFieldEdit.setValue(bufferedBody.state.x);
                yFieldEdit.setValue(bufferedBody.state.y);
                zFieldEdit.setValue(bufferedBody.state.z);
                vxFieldEdit.setValue(bufferedBody.state.vx);
                vyFieldEdit.setValue(bufferedBody.state.vy);
                vzFieldEdit.setValue(bufferedBody.state.vz);
                massFieldEdit.setValue(bufferedBody.mass);
                radiusFieldEdit.setValue(bufferedBody.r);
            }
            
            // Follow the body
            if (followBodyButton.isSelected()) {
                setWindowToBody(bufferedBody);
            }
        }
    }
    
    private boolean areTouching(Body b1, Body b2) {
        double dx = b2.state.x - b1.state.x;
        double dy = b2.state.y - b1.state.y;
        double dz = b2.state.z - b1.state.z;
        double d = Math.sqrt(dx*dx+dy*dy+dz*dz);
        
        return d <= b1.r+b2.r;
    }
    
    private void addBody(String type, double mass, double density,
            double x, double y, double z, double dx, double dy, double dz) {
        Body body = new Body(this, type, mass, density, x, y, z, dx, dy, dz);
        body.setRadiusFromMass();
        
        if (type.equals("Sun")) {
            body.name = "Sun "+sunCnt;
            sunCnt++;
        } else {
            String[] bodyNames = new String[bodies.size()];
            for (int i = 0; i < bodies.size(); i++) {
                bodyNames[i] = bodies.get(i).name;
            }
            
            while (Arrays.asList(bodyNames).contains(names[nameCnt])) {
                nameCnt++;
            }
            body.name = names[nameCnt];
        }
        
        bodiesComboBox.addItem(body.name);
        bodies.add(body);
        totalBodiesCounter.setText(""+bodies.size());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bodyTypeButtonGroup = new javax.swing.ButtonGroup();
        this.mainPanel = new MainPanel();
        javax.swing.JPanel mainPanel = this.mainPanel;
        infoLabel = new javax.swing.JLabel();
        timeField = new javax.swing.JFormattedTextField();
        timeLabel = new javax.swing.JLabel();
        changeButton = new javax.swing.JButton();
        addPlentsLabel = new javax.swing.JLabel();
        numberOfPlanetsLabel = new javax.swing.JLabel();
        numPlanetsSpinner = new javax.swing.JSpinner();
        addPlanetsButton = new javax.swing.JButton();
        zoomSlider = new javax.swing.JSlider();
        zoomLabel = new javax.swing.JLabel();
        pathsLabel = new javax.swing.JLabel();
        inLabel = new javax.swing.JLabel();
        outLabel = new javax.swing.JLabel();
        totalBodiesLabel = new javax.swing.JLabel();
        totalBodiesCounter = new javax.swing.JTextField();
        speedLabel = new javax.swing.JLabel();
        speedSlider = new javax.swing.JSlider();
        slowLabel = new javax.swing.JLabel();
        fastLabel = new javax.swing.JLabel();
        pathsComboBox = new javax.swing.JComboBox();
        editAddTabbedPane = new javax.swing.JTabbedPane();
        editPanel = new javax.swing.JPanel();
        posChangeButton = new javax.swing.JButton();
        velChangeButton = new javax.swing.JButton();
        vzLabelEdit = new javax.swing.JLabel();
        nameChangeButton = new javax.swing.JButton();
        yFieldEdit = new javax.swing.JFormattedTextField();
        moveableCheckBoxEdit = new javax.swing.JCheckBox();
        xFieldEdit = new javax.swing.JFormattedTextField();
        vzFieldEdit = new javax.swing.JFormattedTextField();
        vxFieldEdit = new javax.swing.JFormattedTextField();
        zFieldEdit = new javax.swing.JFormattedTextField();
        vyFieldEdit = new javax.swing.JFormattedTextField();
        posLabelEdit = new javax.swing.JLabel();
        velLabelEdit = new javax.swing.JLabel();
        nameLabelEdit = new javax.swing.JLabel();
        nameFieldEdit = new javax.swing.JTextField();
        xLabelEdit = new javax.swing.JLabel();
        yLabelEdit = new javax.swing.JLabel();
        zLabelEdit = new javax.swing.JLabel();
        vyLabelEdit = new javax.swing.JLabel();
        vxLabelEdit = new javax.swing.JLabel();
        colorComboBoxEdit = new javax.swing.JComboBox<>();
        radiusLabelEdit = new javax.swing.JLabel();
        bodiesComboBox = new javax.swing.JComboBox<>();
        massLabelEdit = new javax.swing.JLabel();
        massChangeButtonEdit = new javax.swing.JButton();
        massFieldEdit = new javax.swing.JFormattedTextField();
        radiusFieldEdit = new javax.swing.JFormattedTextField();
        colorLabel = new javax.swing.JLabel();
        radiusChangeButton = new javax.swing.JButton();
        deleteBodyButton = new javax.swing.JButton();
        locateBodyButton = new javax.swing.JButton();
        followBodyButton = new javax.swing.JToggleButton();
        addPanel = new javax.swing.JPanel();
        radiusLabelAdd = new javax.swing.JLabel();
        massLabelAdd = new javax.swing.JLabel();
        colorComboBoxAdd = new javax.swing.JComboBox<>();
        vzLabelAdd = new javax.swing.JLabel();
        posLabelAdd = new javax.swing.JLabel();
        velLabelAdd = new javax.swing.JLabel();
        nameLabelAdd = new javax.swing.JLabel();
        yFieldAdd = new javax.swing.JFormattedTextField();
        nameFieldAdd = new javax.swing.JTextField();
        xLabelAdd = new javax.swing.JLabel();
        moveableCheckBoxAdd = new javax.swing.JCheckBox();
        yLabelAdd = new javax.swing.JLabel();
        xFieldAdd = new javax.swing.JFormattedTextField();
        zLabelAdd = new javax.swing.JLabel();
        vzFieldAdd = new javax.swing.JFormattedTextField();
        vxFieldAdd = new javax.swing.JFormattedTextField();
        vyLabelAdd = new javax.swing.JLabel();
        zFieldAdd = new javax.swing.JFormattedTextField();
        vxLabelAdd = new javax.swing.JLabel();
        vyFieldAdd = new javax.swing.JFormattedTextField();
        massFieldAdd = new javax.swing.JFormattedTextField();
        radiusFieldAdd = new javax.swing.JFormattedTextField();
        colorLabelAdd = new javax.swing.JLabel();
        addNewBodyButton = new javax.swing.JButton();
        typeLabel = new javax.swing.JLabel();
        sunRadioButton = new javax.swing.JRadioButton();
        planetRadioButton = new javax.swing.JRadioButton();
        removeAllButton = new javax.swing.JButton();
        removeAllLabel = new javax.swing.JLabel();
        removeAllPlanetsCheckBox = new javax.swing.JCheckBox();
        removeAllSunsCheckBox = new javax.swing.JCheckBox();
        forceLawLabel = new javax.swing.JLabel();
        forceLawComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Universal Gravitation Simulator");

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                mainPanelMouseWheelMoved(evt);
            }
        });
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainPanelMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mainPanelMousePressed(evt);
            }
        });
        mainPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mainPanelMouseDragged(evt);
            }
        });

        infoLabel.setText(" ");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGap(0, 868, Short.MAX_VALUE)
                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(infoLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        timeField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("HH:mm:ss"))));
        timeField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        timeField.setText("00:00:00");
        timeField.setFocusable(false);

        timeLabel.setText("Time:");

        changeButton.setText("Start");
        changeButton.setMaximumSize(new java.awt.Dimension(61, 23));
        changeButton.setMinimumSize(new java.awt.Dimension(61, 23));
        changeButton.setPreferredSize(new java.awt.Dimension(61, 23));
        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });

        addPlentsLabel.setText("Add Planets:");

        numberOfPlanetsLabel.setText("Number of Planets");

        numPlanetsSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 0, 50, 1));

        addPlanetsButton.setText("Go");
        addPlanetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlanetsButtonActionPerformed(evt);
            }
        });

        zoomSlider.setMajorTickSpacing(10);
        zoomSlider.setValue(10);
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
            }
        });

        zoomLabel.setText("Zoom:");

        pathsLabel.setText("Paths:");

        inLabel.setText("In");

        outLabel.setText("Out");

        totalBodiesLabel.setText("Bodies:");

        totalBodiesCounter.setEditable(false);
        totalBodiesCounter.setBackground(new java.awt.Color(255, 255, 255));
        totalBodiesCounter.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        totalBodiesCounter.setText("0");

        speedLabel.setText("Speed:");

        speedSlider.setMajorTickSpacing(1);
        speedSlider.setMaximum(30);
        speedSlider.setMinimum(1);
        speedSlider.setValue(1);
        speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedSliderStateChanged(evt);
            }
        });

        slowLabel.setText("Slow");

        fastLabel.setText("Fast");

        pathsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Paths", "Dots", "Lines" }));
        pathsComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                pathsComboBoxItemStateChanged(evt);
            }
        });

        editAddTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                editAddTabbedPaneStateChanged(evt);
            }
        });

        posChangeButton.setText("Change");
        posChangeButton.setEnabled(false);
        posChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posChangeButtonActionPerformed(evt);
            }
        });

        velChangeButton.setText("Change");
        velChangeButton.setEnabled(false);
        velChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                velChangeButtonActionPerformed(evt);
            }
        });

        vzLabelEdit.setText("z:");

        nameChangeButton.setText("Change");
        nameChangeButton.setEnabled(false);
        nameChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameChangeButtonActionPerformed(evt);
            }
        });

        yFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        yFieldEdit.setEnabled(false);
        yFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yFieldEditActionPerformed(evt);
            }
        });

        moveableCheckBoxEdit.setText("Moveable");
        moveableCheckBoxEdit.setEnabled(false);
        moveableCheckBoxEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveableCheckBoxEditActionPerformed(evt);
            }
        });

        xFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        xFieldEdit.setEnabled(false);
        xFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xFieldEditActionPerformed(evt);
            }
        });

        vzFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
        vzFieldEdit.setEnabled(false);
        vzFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vzFieldEditActionPerformed(evt);
            }
        });

        vxFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
        vxFieldEdit.setEnabled(false);
        vxFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vxFieldEditActionPerformed(evt);
            }
        });

        zFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        zFieldEdit.setEnabled(false);
        zFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zFieldEditActionPerformed(evt);
            }
        });

        vyFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
        vyFieldEdit.setEnabled(false);
        vyFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vyFieldEditActionPerformed(evt);
            }
        });

        posLabelEdit.setText("Position:");

        velLabelEdit.setText("Velocity:");

        nameLabelEdit.setText("Name:");

        nameFieldEdit.setEnabled(false);
        nameFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldEditActionPerformed(evt);
            }
        });

        xLabelEdit.setText("x:");

        yLabelEdit.setText("y:");

        zLabelEdit.setText("z:");

        vyLabelEdit.setText("y:");

        vxLabelEdit.setText("x:");

        colorComboBoxEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorComboBoxEditActionPerformed(evt);
            }
        });

        radiusLabelEdit.setText("Radius:");

        bodiesComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bodiesComboBoxItemStateChanged(evt);
            }
        });

        massLabelEdit.setText("Mass:");

        massChangeButtonEdit.setText("Change");
        massChangeButtonEdit.setEnabled(false);
        massChangeButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                massChangeButtonEditActionPerformed(evt);
            }
        });

        massFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
        massFieldEdit.setEnabled(false);
        massFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                massFieldEditActionPerformed(evt);
            }
        });

        radiusFieldEdit.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
        radiusFieldEdit.setEnabled(false);
        radiusFieldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radiusFieldEditActionPerformed(evt);
            }
        });

        colorLabel.setText("Color:");

        radiusChangeButton.setText("Change");
        radiusChangeButton.setEnabled(false);
        radiusChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radiusChangeButtonActionPerformed(evt);
            }
        });

        deleteBodyButton.setText("Delete Body");
        deleteBodyButton.setEnabled(false);
        deleteBodyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBodyButtonActionPerformed(evt);
            }
        });

        locateBodyButton.setText("Locate Body");
        locateBodyButton.setEnabled(false);
        locateBodyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locateBodyButtonActionPerformed(evt);
            }
        });

        followBodyButton.setText("Folow Body");
        followBodyButton.setEnabled(false);
        followBodyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                followBodyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editPanelLayout.createSequentialGroup()
                        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(editPanelLayout.createSequentialGroup()
                                .addComponent(posLabelEdit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(editPanelLayout.createSequentialGroup()
                                        .addComponent(xLabelEdit)
                                        .addGap(57, 57, 57))
                                    .addGroup(editPanelLayout.createSequentialGroup()
                                        .addComponent(yLabelEdit)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(xFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(yFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, editPanelLayout.createSequentialGroup()
                                        .addComponent(zLabelEdit)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(posChangeButton))
                        .addGap(18, 18, 18)
                        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(velChangeButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editPanelLayout.createSequentialGroup()
                                .addComponent(vyLabelEdit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(vyFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editPanelLayout.createSequentialGroup()
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(editPanelLayout.createSequentialGroup()
                                        .addComponent(velLabelEdit)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(vxLabelEdit))
                                    .addComponent(vzLabelEdit))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(vxFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(vzFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(bodiesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(editPanelLayout.createSequentialGroup()
                            .addComponent(nameLabelEdit)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(nameFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(nameChangeButton)))
                    .addGroup(editPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editPanelLayout.createSequentialGroup()
                                .addComponent(massLabelEdit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(editPanelLayout.createSequentialGroup()
                                        .addComponent(massFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(radiusLabelEdit))
                                    .addComponent(massChangeButtonEdit))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(editPanelLayout.createSequentialGroup()
                                        .addComponent(radiusChangeButton)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editPanelLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(radiusFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(editPanelLayout.createSequentialGroup()
                                .addComponent(moveableCheckBoxEdit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(deleteBodyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(editPanelLayout.createSequentialGroup()
                                .addComponent(colorLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(colorComboBoxEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(editPanelLayout.createSequentialGroup()
                                .addComponent(locateBodyButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(followBodyButton)))))
                .addContainerGap())
        );
        editPanelLayout.setVerticalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(bodiesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabelEdit)
                    .addComponent(nameFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameChangeButton))
                .addGap(18, 18, 18)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posLabelEdit)
                    .addComponent(xLabelEdit)
                    .addComponent(velLabelEdit)
                    .addComponent(vxLabelEdit)
                    .addComponent(xFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vxFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yLabelEdit)
                    .addComponent(vyLabelEdit)
                    .addComponent(yFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vyFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zLabelEdit)
                    .addComponent(vzLabelEdit)
                    .addComponent(vzFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posChangeButton)
                    .addComponent(velChangeButton))
                .addGap(18, 18, 18)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(massLabelEdit)
                    .addComponent(radiusLabelEdit)
                    .addComponent(massFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radiusFieldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(massChangeButtonEdit)
                    .addComponent(radiusChangeButton))
                .addGap(18, 18, 18)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel)
                    .addComponent(colorComboBoxEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(moveableCheckBoxEdit)
                    .addComponent(deleteBodyButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locateBodyButton)
                    .addComponent(followBodyButton))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        editAddTabbedPane.addTab("Edit/View", editPanel);

        radiusLabelAdd.setText("Radius:");

        massLabelAdd.setText("Mass:");

        vzLabelAdd.setText("z:");

        posLabelAdd.setText("Position:");

        velLabelAdd.setText("Velocity:");

        nameLabelAdd.setText("Name:");

        yFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        xLabelAdd.setText("x:");

        moveableCheckBoxAdd.setSelected(true);
        moveableCheckBoxAdd.setText("Moveable");

        yLabelAdd.setText("y:");

        xFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        zLabelAdd.setText("z:");

        vzFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));

        vxFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));

        vyLabelAdd.setText("y:");

        zFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        vxLabelAdd.setText("x:");

        vyFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));

        massFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));

        radiusFieldAdd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));

        colorLabelAdd.setText("Color:");

        addNewBodyButton.setText("Add New Body");
        addNewBodyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewBodyButtonActionPerformed(evt);
            }
        });

        typeLabel.setText("Type:");

        bodyTypeButtonGroup.add(sunRadioButton);
        sunRadioButton.setText("Sun");

        bodyTypeButtonGroup.add(planetRadioButton);
        planetRadioButton.setSelected(true);
        planetRadioButton.setText("Planet");

        javax.swing.GroupLayout addPanelLayout = new javax.swing.GroupLayout(addPanel);
        addPanel.setLayout(addPanelLayout);
        addPanelLayout.setHorizontalGroup(
            addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addPanelLayout.createSequentialGroup()
                                .addComponent(massLabelAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(massFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(radiusLabelAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(radiusFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(addPanelLayout.createSequentialGroup()
                                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(moveableCheckBoxAdd)
                                    .addGroup(addPanelLayout.createSequentialGroup()
                                        .addComponent(colorLabelAdd)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(colorComboBoxAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addNewBodyButton))
                    .addGroup(addPanelLayout.createSequentialGroup()
                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addPanelLayout.createSequentialGroup()
                                .addComponent(posLabelAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(addPanelLayout.createSequentialGroup()
                                        .addComponent(xLabelAdd)
                                        .addGap(57, 57, 57))
                                    .addGroup(addPanelLayout.createSequentialGroup()
                                        .addComponent(yLabelAdd)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(xFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(yFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addPanelLayout.createSequentialGroup()
                                        .addComponent(zLabelAdd)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addPanelLayout.createSequentialGroup()
                                        .addComponent(vyLabelAdd)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(vyFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addPanelLayout.createSequentialGroup()
                                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(addPanelLayout.createSequentialGroup()
                                                .addComponent(velLabelAdd)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(vxLabelAdd))
                                            .addComponent(vzLabelAdd))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(vxFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(vzFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(addPanelLayout.createSequentialGroup()
                                .addComponent(nameLabelAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nameFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(addPanelLayout.createSequentialGroup()
                                .addComponent(typeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(planetRadioButton)
                                    .addComponent(sunRadioButton))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        addPanelLayout.setVerticalGroup(
            addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabelAdd)
                    .addComponent(nameFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sunRadioButton)
                    .addComponent(typeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(planetRadioButton)
                .addGap(17, 17, 17)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posLabelAdd)
                    .addComponent(xLabelAdd)
                    .addComponent(velLabelAdd)
                    .addComponent(vxLabelAdd)
                    .addComponent(xFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vxFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yLabelAdd)
                    .addComponent(vyLabelAdd)
                    .addComponent(yFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vyFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zLabelAdd)
                    .addComponent(vzLabelAdd)
                    .addComponent(vzFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(massLabelAdd)
                    .addComponent(radiusLabelAdd)
                    .addComponent(massFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radiusFieldAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabelAdd)
                    .addComponent(colorComboBoxAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(moveableCheckBoxAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addNewBodyButton)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        editAddTabbedPane.addTab("Add Body", addPanel);

        removeAllButton.setText("Remove");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });

        removeAllLabel.setText("Remove All:");

        removeAllPlanetsCheckBox.setText("Planets");

        removeAllSunsCheckBox.setText("Suns");

        forceLawLabel.setText("Force Law:");

        forceLawComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1/r^3", "1/r^2", "1/r", "r", "r^2" }));
        forceLawComboBox.setSelectedItem("1/r^2");
        forceLawComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                forceLawComboBoxItemStateChanged(evt);
            }
        });
        forceLawComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forceLawComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeLabel)
                                .addGap(109, 109, 109))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeField, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(changeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addPlentsLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(numberOfPlanetsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numPlanetsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addPlanetsButton)))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pathsLabel)
                            .addComponent(pathsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zoomLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(inLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(outLabel)))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(slowLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(speedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fastLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(speedLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(forceLawComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(forceLawLabel))
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(removeAllLabel)
                                .addGap(105, 105, 105))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(removeAllSunsCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeAllPlanetsCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                .addComponent(totalBodiesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(totalBodiesCounter, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editAddTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editAddTabbedPane)
                    .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(zoomLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(speedLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(addPlentsLabel)
                                    .addComponent(pathsLabel))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(timeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(changeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(numberOfPlanetsLabel)
                                .addComponent(numPlanetsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(addPlanetsButton)
                                .addComponent(inLabel)
                                .addComponent(pathsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(slowLabel)
                                .addComponent(outLabel))
                            .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(speedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeAllLabel)
                            .addComponent(forceLawLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeAllSunsCheckBox)
                            .addComponent(totalBodiesLabel)
                            .addComponent(totalBodiesCounter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(removeAllButton)
                            .addComponent(removeAllPlanetsCheckBox)
                            .addComponent(fastLabel)
                            .addComponent(forceLawComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        if (changeButton.getText().equals("Start")) {
            gameTimerTask = new TimerTask() {
                @Override
                public void run() {
                    step();
                }
            };
            
            nameChangeButton.setEnabled(false);
            posChangeButton.setEnabled(false);
            velChangeButton.setEnabled(false);
            massChangeButtonEdit.setEnabled(false);
            radiusChangeButton.setEnabled(false);
            
            nameFieldEdit.setFocusable(false);
            xFieldEdit.setFocusable(false);
            yFieldEdit.setFocusable(false);
            zFieldEdit.setFocusable(false);
            vxFieldEdit.setFocusable(false);
            vyFieldEdit.setFocusable(false);
            vzFieldEdit.setFocusable(false);
            massFieldEdit.setFocusable(false);
            radiusFieldEdit.setFocusable(false);
            
            new Timer().scheduleAtFixedRate(gameTimerTask, 0, calcFreq);
            changeButton.setText("Pause");
        } else {
            if (selectedBody != null) {
                nameChangeButton.setEnabled(true);
                posChangeButton.setEnabled(true);
                velChangeButton.setEnabled(true);
                massChangeButtonEdit.setEnabled(true);
                radiusChangeButton.setEnabled(true);
            
                nameFieldEdit.setFocusable(true);
                xFieldEdit.setFocusable(true);
                yFieldEdit.setFocusable(true);
                zFieldEdit.setFocusable(true);
                vxFieldEdit.setFocusable(true);
                vyFieldEdit.setFocusable(true);
                vzFieldEdit.setFocusable(true);
                massFieldEdit.setFocusable(true);
                radiusFieldEdit.setFocusable(true);
            }
            
            gameTimerTask.cancel();
            changeButton.setText("Start");
        }
    }//GEN-LAST:event_changeButtonActionPerformed

    private void addPlanetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlanetsButtonActionPerformed
        for (int i = 0; i < (int) numPlanetsSpinner.getValue(); i++) {
            double x = (Math.random()*6-3)*mainPanel.getWidth();
            double y = (Math.random()*6-3)*mainPanel.getHeight();
            double z = (Math.random())*mainPanel.getHeight();
            /*double x = Math.random()*.5*mainPanel.getWidth();
            double y = Math.random()*.5*mainPanel.getHeight();
            double z = Math.random()*.5*mainPanel.getWidth();*/
            
            double dx = getWidth()/2.0 - x;
            double dy = y - getHeight()/2.0;
            double dz = - z;
            double r = Math.sqrt(dx*dx+dy*dy+dz*dz);
            
            /*
            // get vx and vy from v
            double vx = Math.random()*.4*dy/r;
            double vy = Math.random()*.4*dx/r;
            */
            
            /*
            // get vx and vy from v, use less than escape velocity
            double vx = Math.random()*0.8*Math.sqrt(2*GRAV_CONST*massOfSun/r)*dy/r;
            double vy = Math.random()*0.8*Math.sqrt(2*GRAV_CONST*massOfSun/r)*dx/r;
            */
            
            // get vx and vy from v, use close to perfect circle
            //double vx = (Math.random()*4+2)*Math.sqrt(GRAV_CONST*massOfSun/r)*dy/r;
            //double vy = (Math.random()*4+2)*Math.sqrt(GRAV_CONST*massOfSun/r)*dx/r;
            
            // get vx and vy from v, use perfect circle depending on the force law
            double vx = Math.sqrt(GRAV_CONST*massOfSun*Math.pow(r, forceLaw+1))*dy/r;
            double vy = Math.sqrt(GRAV_CONST*massOfSun*Math.pow(r, forceLaw+1))*dx/r;
            double vz = Math.sqrt(GRAV_CONST*massOfSun*Math.pow(r, forceLaw+1))*dz/r;
            
            vx *= Math.random()*.7+.65;
            vy *= Math.random()*.7+.65;
            vz *= Math.random()*.7+.65;
            
            addBody("Planet", Math.random()*0.29+0.01, Math.random()*0.00004+0.00008, x, y, z, vx, vy, vz);
        }
        mainPanel.repaint();
    }//GEN-LAST:event_addPlanetsButtonActionPerformed

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        /*double dZoom = 1/(zMult/zoomSlider.getValue());
        dWindowX /= dZoom;
        dWindowY /= dZoom;
        
        zMult = zoomSlider.getValue();*/
        mainPanel.repaint();
    }//GEN-LAST:event_zoomSliderStateChanged

    private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
        /*for (Body b : bodies) {
            b.state.vx *= (1.0*speedSlider.getValue()/vMult);
            b.state.vy *= (1.0*speedSlider.getValue()/vMult);
            b.state.vz *= (1.0*speedSlider.getValue()/vMult);
        }
        GRAV_CONST *= Math.pow(1.0*speedSlider.getValue()/vMult, 2);
        vMult = speedSlider.getValue();*/
        
        /*if (changeButton.getText().equals("Pause")) {
            gameTimerTask.cancel();
            gameTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        step();
                    }
                };
            new Timer().scheduleAtFixedRate(gameTimerTask, 0, calcFreq);
        }*/
    }//GEN-LAST:event_speedSliderStateChanged

    private void mainPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMouseDragged
        double zoom = 1/(zoomSlider.getValue()/10.0);
        dWindowX = (int) ((evt.getX() - clickX)/zoom);
        dWindowY = (int) ((evt.getY() - clickY)/zoom);
        
        if (followBodyButton.isSelected()) {
            followBodyButton.setSelected(false);
        }
        
        mainPanel.repaint();
    }//GEN-LAST:event_mainPanelMouseDragged

    private void mainPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMousePressed
        double zoom = 1/(zoomSlider.getValue()/10.0);
        clickX = (int) (evt.getX() - dWindowX*zoom);
        clickY = (int) (evt.getY() - dWindowY*zoom);
    }//GEN-LAST:event_mainPanelMousePressed

    private void mainPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_mainPanelMouseWheelMoved
        zoomSlider.setValue(zoomSlider.getValue()+evt.getWheelRotation());
    }//GEN-LAST:event_mainPanelMouseWheelMoved

    private void pathsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_pathsComboBoxItemStateChanged
        for (Body b : bodies) {
            b.pastPos = new CopyOnWriteArrayList<>();
        }
        mainPanel.repaint();
    }//GEN-LAST:event_pathsComboBoxItemStateChanged

    private void mainPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMouseClicked
        double zoom = 1/(zoomSlider.getValue()/10.0);
        for (Body b : bodies) {
            double zZoom = Math.pow(zZoomBase, -1*b.state.z);
            double x3D = 2*(b.state.x);
            double y3D = (b.state.y) + b.state.z;
            
            double gX = zoom*(dWindowX + x3D) + mainPanel.getWidth()/2.0;
            double gY = zoom*(dWindowY + y3D) + mainPanel.getHeight()/2.0;
            double gr = b.r*zoom*zZoom;

            double dx = evt.getX() - gX;
            double dy = evt.getY() - gY;
            
            if (dx*dx+dy*dy < gr*gr) {
                bodiesComboBox.setSelectedItem(b.name);
                editAddTabbedPane.setSelectedComponent(editPanel);
            }
        }
    }//GEN-LAST:event_mainPanelMouseClicked

    private void radiusChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radiusChangeButtonActionPerformed
        double r = Double.parseDouble(radiusFieldEdit.getText());
        selectedBody.r = r;
        double density = (3.0 * selectedBody.mass) / (r*r*r * 4.0 * Math.PI);
        selectedBody.DENSITY = density;
        
        mainPanel.repaint();
        
    }//GEN-LAST:event_radiusChangeButtonActionPerformed

    private void radiusFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radiusFieldEditActionPerformed
        double r = Double.parseDouble(radiusFieldEdit.getText());
        selectedBody.r = r;
        double density = (3.0 * selectedBody.mass) / (r*r*r * 4.0 * Math.PI);
        selectedBody.DENSITY = density;
        
        mainPanel.repaint();
    }//GEN-LAST:event_radiusFieldEditActionPerformed

    private void massFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_massFieldEditActionPerformed
        double mass = Double.parseDouble(massFieldEdit.getText());
        selectedBody.mass = mass;
        double density = (3.0 * mass) / (selectedBody.r*selectedBody.r*selectedBody.r * 4.0 * Math.PI);
        selectedBody.DENSITY = density;
        
        if (selectedBody == sun) {
            massOfSun = selectedBody.mass;
        }
    }//GEN-LAST:event_massFieldEditActionPerformed

    private void massChangeButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_massChangeButtonEditActionPerformed
        double mass = Double.parseDouble(massFieldEdit.getText());
        selectedBody.mass = mass;
        double density = (3.0 * mass) / (selectedBody.r*selectedBody.r*selectedBody.r * 4.0 * Math.PI);
        selectedBody.DENSITY = density;
        
        if (selectedBody == sun) {
            massOfSun = selectedBody.mass;
        }
    }//GEN-LAST:event_massChangeButtonEditActionPerformed

    private void colorComboBoxEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorComboBoxEditActionPerformed
        if (selectedBody != null) {
            selectedBody.color = (String) colorComboBoxEdit.getSelectedItem();
            mainPanel.repaint();
        }
    }//GEN-LAST:event_colorComboBoxEditActionPerformed

    private void nameFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldEditActionPerformed
        changeName();
    }//GEN-LAST:event_nameFieldEditActionPerformed

    private void vyFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vyFieldEditActionPerformed
        selectedBody.state.vy = Double.parseDouble(vyFieldEdit.getText());
    }//GEN-LAST:event_vyFieldEditActionPerformed

    private void zFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zFieldEditActionPerformed
        selectedBody.state.z = Double.parseDouble(zFieldEdit.getText());
        mainPanel.repaint();
    }//GEN-LAST:event_zFieldEditActionPerformed

    private void vxFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vxFieldEditActionPerformed
        selectedBody.state.vx = Double.parseDouble(vxFieldEdit.getText());
    }//GEN-LAST:event_vxFieldEditActionPerformed

    private void vzFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vzFieldEditActionPerformed
        selectedBody.state.vz = Double.parseDouble(vzFieldEdit.getText());
    }//GEN-LAST:event_vzFieldEditActionPerformed

    private void xFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xFieldEditActionPerformed
        selectedBody.state.x = Double.parseDouble(xFieldEdit.getText());
        mainPanel.repaint();
    }//GEN-LAST:event_xFieldEditActionPerformed

    private void moveableCheckBoxEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveableCheckBoxEditActionPerformed
        selectedBody.moveable = moveableCheckBoxEdit.isSelected();
    }//GEN-LAST:event_moveableCheckBoxEditActionPerformed

    private void yFieldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yFieldEditActionPerformed
        selectedBody.state.y = Double.parseDouble(yFieldEdit.getText());
        mainPanel.repaint();
    }//GEN-LAST:event_yFieldEditActionPerformed

    private void nameChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameChangeButtonActionPerformed
        changeName();
    }//GEN-LAST:event_nameChangeButtonActionPerformed

    private void velChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_velChangeButtonActionPerformed
        selectedBody.state.vx = Double.parseDouble(vxFieldEdit.getText());
        selectedBody.state.vy = Double.parseDouble(vyFieldEdit.getText());
        selectedBody.state.vz = Double.parseDouble(vzFieldEdit.getText());
    }//GEN-LAST:event_velChangeButtonActionPerformed

    private void posChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posChangeButtonActionPerformed
        selectedBody.state.x = Double.parseDouble(xFieldEdit.getText());
        selectedBody.state.y = Double.parseDouble(yFieldEdit.getText());
        selectedBody.state.z = Double.parseDouble(zFieldEdit.getText());
        mainPanel.repaint();
    }//GEN-LAST:event_posChangeButtonActionPerformed

    private void addNewBodyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewBodyButtonActionPerformed
        String[] bodyNames = new String[bodies.size()];
        for (int i = 0; i < bodies.size(); i++) {
            bodyNames[i] = bodies.get(i).name;
        }
        
        if (Arrays.asList(bodyNames).contains(nameFieldAdd.getText())) {
            JOptionPane.showMessageDialog(null, "That name is already taken", "Invalid", JOptionPane.INFORMATION_MESSAGE);
        } else if (xFieldAdd.getText().equals("") || yFieldAdd.getText().equals("") || zFieldAdd.getText().equals("") ||
                vxFieldAdd.getText().equals("") || vyFieldAdd.getText().equals("") || vzFieldAdd.getText().equals("") ||
                massFieldAdd.getText().equals("") || radiusFieldAdd.getText().equals("") ||
                nameFieldAdd.getText().equals("")) {
        
            JOptionPane.showMessageDialog(null, "You must fill all fields", "Invalid", JOptionPane.INFORMATION_MESSAGE);
        } else if (Double.parseDouble(massFieldAdd.getText()) <= 0) {
            JOptionPane.showMessageDialog(null, "The mass must be positive", "Invalid", JOptionPane.INFORMATION_MESSAGE);
        } else if (Double.parseDouble(radiusFieldAdd.getText()) <= 0) {
            JOptionPane.showMessageDialog(null, "The radius must be positive", "Invalid", JOptionPane.INFORMATION_MESSAGE);
        }else {
            String type = sunRadioButton.isSelected() ? "Sun" : "Planet";
            double x, y, z, vx, vy, vz, mass, r;
            x = Double.parseDouble(xFieldAdd.getText());
            y = Double.parseDouble(yFieldAdd.getText());
            z = Double.parseDouble(zFieldAdd.getText());
            vx = Double.parseDouble(vxFieldAdd.getText());
            vy = Double.parseDouble(vyFieldAdd.getText());
            vz = Double.parseDouble(vzFieldAdd.getText());
            mass = Double.parseDouble(massFieldAdd.getText());
            r = Double.parseDouble(radiusFieldAdd.getText());
            
            double density = (3.0 * mass) / (r*r*r * 4.0 * Math.PI);
            
            Body body = new Body(this, type, mass, density, x, y, z, vx, vy, vz);
            body.setRadiusFromMass();
            body.name = nameFieldAdd.getText();
            body.color = (String) colorComboBoxAdd.getSelectedItem();
            body.moveable = moveableCheckBoxAdd.isSelected();
            
            bodies.add(body);
            totalBodiesCounter.setText(""+bodies.size());
            
            editAddTabbedPane.setSelectedComponent(editPanel);
            bodiesComboBox.addItem(body.name);
            bodiesComboBox.setSelectedItem(body.name);
            
            mainPanel.repaint();
        }
    }//GEN-LAST:event_addNewBodyButtonActionPerformed

    private void deleteBodyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBodyButtonActionPerformed
        bodies.remove(selectedBody);
        bodiesComboBox.removeItem(selectedBody.name);
        totalBodiesCounter.setText(""+bodies.size());
        
        mainPanel.repaint();
    }//GEN-LAST:event_deleteBodyButtonActionPerformed

    private void bodiesComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bodiesComboBoxItemStateChanged
        followBodyButton.setSelected(false);
        boolean bodySelected = false;
        for (Body b : bodies) {
            if (b.name.equals((String) bodiesComboBox.getSelectedItem())) {
                setEditor(b);
                bodySelected = true;
                break;
            }
        } if (!bodySelected) {
            setEditor(null);
        }
    }//GEN-LAST:event_bodiesComboBoxItemStateChanged

    private void editAddTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_editAddTabbedPaneStateChanged
        if (editAddTabbedPane.getSelectedComponent().equals(editPanel)) {
            ArrayList<Component> editOrder = new ArrayList<>(12);
            editOrder.add(bodiesComboBox);
            editOrder.add(nameFieldEdit);
            editOrder.add(xFieldEdit);
            editOrder.add(yFieldEdit);
            editOrder.add(zFieldEdit);
            editOrder.add(vxFieldEdit);
            editOrder.add(vyFieldEdit);
            editOrder.add(vzFieldEdit);
            editOrder.add(massFieldEdit);
            editOrder.add(radiusFieldEdit);
            editOrder.add(colorComboBoxEdit);
            editOrder.add(moveableCheckBoxEdit);
            setFocusTraversalPolicy(new CustomFocusTraversalPolicy(editOrder));
            
            nameFieldAdd.setText("");
            planetRadioButton.setSelected(true);
            xFieldAdd.setText("");
            yFieldAdd.setText("");
            zFieldAdd.setText("");
            vxFieldAdd.setText("");
            vyFieldAdd.setText("");
            vzFieldAdd.setText("");
            massFieldAdd.setText("");
            radiusFieldAdd.setText("");
            colorComboBoxAdd.setSelectedItem("Black");
            moveableCheckBoxAdd.setSelected(true);
            
        } else if (editAddTabbedPane.getSelectedComponent().equals(addPanel)) {
            ArrayList<Component> addOrder = new ArrayList<>(14);
            addOrder.add(nameFieldAdd);
            addOrder.add(sunRadioButton);
            addOrder.add(planetRadioButton);
            addOrder.add(xFieldAdd);
            addOrder.add(yFieldAdd);
            addOrder.add(zFieldAdd);
            addOrder.add(vxFieldAdd);
            addOrder.add(vyFieldAdd);
            addOrder.add(vzFieldAdd);
            addOrder.add(massFieldAdd);
            addOrder.add(radiusFieldAdd);
            addOrder.add(colorComboBoxAdd);
            addOrder.add(moveableCheckBoxAdd);
            addOrder.add(addNewBodyButton);
            setFocusTraversalPolicy(new CustomFocusTraversalPolicy(addOrder));
        }
    }//GEN-LAST:event_editAddTabbedPaneStateChanged

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        ArrayList<Body> toBeRemoved = new ArrayList<>();

        for (Body b: bodies) {
            if ((removeAllSunsCheckBox.isSelected() && b.type.equals("Sun")) ||
                (removeAllPlanetsCheckBox.isSelected() && b.type.equals("Planet"))) {
                toBeRemoved.add(b);
            }
        }
        
        for (Body b : toBeRemoved) {
            bodies.remove(b);
            if (b == selectedBody) {
                selectedBody = null;
            }
            bodiesComboBox.removeItem(b.name);
            
        }
        
        totalBodiesCounter.setText(""+bodies.size());
        mainPanel.repaint();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void locateBodyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locateBodyButtonActionPerformed
        setWindowToBody(selectedBody);
    }//GEN-LAST:event_locateBodyButtonActionPerformed

    private void followBodyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_followBodyButtonActionPerformed
        setWindowToBody(selectedBody);
    }//GEN-LAST:event_followBodyButtonActionPerformed

    private void forceLawComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forceLawComboBoxActionPerformed
        //if ()
    }//GEN-LAST:event_forceLawComboBoxActionPerformed

    private void forceLawComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_forceLawComboBoxItemStateChanged
        if (evt.getStateChange() == 1) {
            switch ((String)evt.getItem()) {
                case "1/r^3": forceLaw = -3; break;
                case "1/r^2": forceLaw = -2; break;
                case "1/r": forceLaw = -1; break;
                case "r": forceLaw = 1; break;
                case "r^2": forceLaw = 2; break;
            }
        }
    }//GEN-LAST:event_forceLawComboBoxItemStateChanged

    private void setWindowToBody(Body b) {
        double x3D = 2*(b.state.x);
        double y3D = (b.state.y) + b.state.z;
        
        dWindowX = (int) (-1*x3D);
        dWindowY = (int) (-1*y3D);
        
        mainPanel.repaint();
    }
    
    private void setEditor(Body b) {
        selectedBody = b;
        
        if (b != null) {
            nameFieldEdit.setEnabled(true);
            xFieldEdit.setEnabled(true);
            yFieldEdit.setEnabled(true);
            zFieldEdit.setEnabled(true);
            vxFieldEdit.setEnabled(true);
            vyFieldEdit.setEnabled(true);
            vzFieldEdit.setEnabled(true);
            massFieldEdit.setEnabled(true);
            radiusFieldEdit.setEnabled(true);
            moveableCheckBoxEdit.setEnabled(true);
            colorComboBoxEdit.setEnabled(true);

            nameFieldEdit.setText(b.name);
            xFieldEdit.setValue(b.state.x);
            yFieldEdit.setValue(b.state.y);
            zFieldEdit.setValue(b.state.z);
            vxFieldEdit.setValue(b.state.vx);
            vyFieldEdit.setValue(b.state.vy);
            vzFieldEdit.setValue(b.state.vz);
            massFieldEdit.setValue(b.mass);
            radiusFieldEdit.setValue(b.r);
            colorComboBoxEdit.setSelectedItem(b.color);
            moveableCheckBoxEdit.setSelected(b.moveable);

            deleteBodyButton.setEnabled(true);
            locateBodyButton.setEnabled(true);
            followBodyButton.setEnabled(true);

            if (changeButton.getText().equals("Start")) {
                nameChangeButton.setEnabled(true);
                posChangeButton.setEnabled(true);
                velChangeButton.setEnabled(true);
                massChangeButtonEdit.setEnabled(true);
                radiusChangeButton.setEnabled(true);
            }
        } else {
            nameFieldEdit.setEnabled(false);
            xFieldEdit.setEnabled(false);
            yFieldEdit.setEnabled(false);
            zFieldEdit.setEnabled(false);
            vxFieldEdit.setEnabled(false);
            vyFieldEdit.setEnabled(false);
            vzFieldEdit.setEnabled(false);
            massFieldEdit.setEnabled(false);
            radiusFieldEdit.setEnabled(false);
            moveableCheckBoxEdit.setEnabled(false);
            colorComboBoxEdit.setEnabled(false);

            nameFieldEdit.setText("");
            xFieldEdit.setText("");
            yFieldEdit.setText("");
            zFieldEdit.setText("");
            vxFieldEdit.setText("");
            vyFieldEdit.setText("");
            vzFieldEdit.setText("");
            massFieldEdit.setText("");
            radiusFieldEdit.setText("");
            moveableCheckBoxEdit.setSelected(false);
            colorComboBoxEdit.setSelectedIndex(-1);

            nameChangeButton.setEnabled(false);
            posChangeButton.setEnabled(false);
            velChangeButton.setEnabled(false);
            massChangeButtonEdit.setEnabled(false);
            radiusChangeButton.setEnabled(false);
            deleteBodyButton.setEnabled(false);
            locateBodyButton.setEnabled(false);
            followBodyButton.setEnabled(false);
        }
    }
    
    private void changeName() {
        String[] bodyNames = new String[bodies.size()];
        for (int i = 0; i < bodies.size(); i++) {
            bodyNames[i] = bodies.get(i).name;
        }
        
        if (Arrays.asList(bodyNames).contains(nameFieldEdit.getText())) {
            JOptionPane.showMessageDialog(null, "That name is already taken", "Invalid", JOptionPane.INFORMATION_MESSAGE);
            nameFieldEdit.setText(selectedBody.name);
        } else {
            String oldName = selectedBody.name;
            selectedBody.name = nameFieldEdit.getText();
            
            bodiesComboBox.addItem(nameFieldEdit.getText());
            bodiesComboBox.setSelectedItem(nameFieldEdit.getText());
            bodiesComboBox.removeItem(oldName);
        }
    }
        
    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SpaceSim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceSim().setVisible(true);
            }
        });
    }
    
    private class MainPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            double zoom = 1/(zoomSlider.getValue()/10.0);
            
            ArrayList<SpaceObject> everything = new ArrayList<>();
            everything.addAll(bodies);
            
            g.setColor(Color.BLUE);
            for (Body b : bodies) {
                if (pathsComboBox.getSelectedItem().equals("Lines")) {
                    for (int i = 1; i < b.pastPos.size()-1; i++) {
                        Shadow preS = b.pastPos.get(i-1);
                        Integer[] posBefore = new Integer[]{(int)preS.state.x, (int)preS.state.y, (int)preS.state.z};
                        
                        Shadow nowS = b.pastPos.get(i);
                        Integer[] pos = new Integer[]{(int)nowS.state.x, (int)nowS.state.y, (int)nowS.state.z};
                        
                        double x3D = 2*(pos[0]);
                        double realXPos = zoom*(dWindowX + x3D);
                        double y3D = (pos[1]) + pos[2];
                        double realYPos = zoom*(dWindowY + y3D);
                        double x3DPast = 2*(posBefore[0]);
                        double realXPosPast = zoom*(dWindowX + x3DPast);
                        double y3DPast = (posBefore[1]) + posBefore[2];
                        double realYPosPast = zoom*(dWindowY + y3DPast);
                        
                        g.drawLine((int) (realXPosPast + getWidth()/2.0),
                                (int) (realYPosPast + getHeight()/2.0),
                                (int) (realXPos + getWidth()/2.0),
                                (int) (realYPos + getHeight()/2.0));
                    }
                } else {
                    everything.addAll(b.pastPos);
                }
            }
            
            SpaceObject[] spaceObjects = everything.toArray(new SpaceObject[0]);
            SpaceObject[] copy = Arrays.copyOf(spaceObjects, spaceObjects.length);
            try {
                    Arrays.sort(copy, new Comparator<SpaceObject>() {
                    @Override
                    public int compare(SpaceObject b1, SpaceObject b2) {
                        return (int) Math.signum(b2.state.z - b1.state.z);
                    }
                });
            } catch(IllegalArgumentException e) {}
                
            for (SpaceObject so : copy) {
                g.setColor(colors.get(so.color));
                double zZoom = Math.pow(zZoomBase, -1*so.state.z);
                double x3D = 2*(so.state.x);
                double y3D = (so.state.y) + so.state.z;
                
                double realXPos = zoom*(dWindowX + x3D);
                double realYPos = zoom*(dWindowY + y3D);
                
                double r = so.r*zoom*zZoom;
                g.fillOval((int) (realXPos - r + getWidth()/2.0),
                        (int) (realYPos - r + getHeight()/2.0),
                        (int) (2*r),
                        (int) (2*r));
            }
        }
    }
    
    private class CustomFocusTraversalPolicy extends FocusTraversalPolicy {
        private ArrayList<Component> order;
        
        public CustomFocusTraversalPolicy(ArrayList<Component> order) {
            this.order = order;
        }
        
        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            int i = (order.indexOf(aComponent)+1) % order.size();
            return order.get(i);
        }

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            int i = order.indexOf(aComponent) - 1;
            if (i < 0) {
                i = order.size()-1;
            }
            return order.get(i);
        }

        @Override
        public Component getFirstComponent(Container aContainer) {
            return order.get(0);
        }

        @Override
        public Component getLastComponent(Container aContainer) {
            return order.get(order.size()-1);
        }

        @Override
        public Component getDefaultComponent(Container aContainer) {
            return order.get(0);
        }
    }
    
    private MainPanel mainPanel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewBodyButton;
    private javax.swing.JPanel addPanel;
    private javax.swing.JButton addPlanetsButton;
    private javax.swing.JLabel addPlentsLabel;
    private javax.swing.JComboBox<String> bodiesComboBox;
    private javax.swing.ButtonGroup bodyTypeButtonGroup;
    private javax.swing.JButton changeButton;
    private javax.swing.JComboBox<String> colorComboBoxAdd;
    private javax.swing.JComboBox<String> colorComboBoxEdit;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JLabel colorLabelAdd;
    private javax.swing.JButton deleteBodyButton;
    private javax.swing.JTabbedPane editAddTabbedPane;
    private javax.swing.JPanel editPanel;
    private javax.swing.JLabel fastLabel;
    private javax.swing.JToggleButton followBodyButton;
    private javax.swing.JComboBox forceLawComboBox;
    private javax.swing.JLabel forceLawLabel;
    private javax.swing.JLabel inLabel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton locateBodyButton;
    private javax.swing.JButton massChangeButtonEdit;
    private javax.swing.JFormattedTextField massFieldAdd;
    private javax.swing.JFormattedTextField massFieldEdit;
    private javax.swing.JLabel massLabelAdd;
    private javax.swing.JLabel massLabelEdit;
    private javax.swing.JCheckBox moveableCheckBoxAdd;
    private javax.swing.JCheckBox moveableCheckBoxEdit;
    private javax.swing.JButton nameChangeButton;
    private javax.swing.JTextField nameFieldAdd;
    private javax.swing.JTextField nameFieldEdit;
    private javax.swing.JLabel nameLabelAdd;
    private javax.swing.JLabel nameLabelEdit;
    private javax.swing.JSpinner numPlanetsSpinner;
    private javax.swing.JLabel numberOfPlanetsLabel;
    private javax.swing.JLabel outLabel;
    private javax.swing.JComboBox pathsComboBox;
    private javax.swing.JLabel pathsLabel;
    private javax.swing.JRadioButton planetRadioButton;
    private javax.swing.JButton posChangeButton;
    private javax.swing.JLabel posLabelAdd;
    private javax.swing.JLabel posLabelEdit;
    private javax.swing.JButton radiusChangeButton;
    private javax.swing.JFormattedTextField radiusFieldAdd;
    private javax.swing.JFormattedTextField radiusFieldEdit;
    private javax.swing.JLabel radiusLabelAdd;
    private javax.swing.JLabel radiusLabelEdit;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JLabel removeAllLabel;
    private javax.swing.JCheckBox removeAllPlanetsCheckBox;
    private javax.swing.JCheckBox removeAllSunsCheckBox;
    private javax.swing.JLabel slowLabel;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JSlider speedSlider;
    private javax.swing.JRadioButton sunRadioButton;
    private javax.swing.JFormattedTextField timeField;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JTextField totalBodiesCounter;
    private javax.swing.JLabel totalBodiesLabel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JButton velChangeButton;
    private javax.swing.JLabel velLabelAdd;
    private javax.swing.JLabel velLabelEdit;
    private javax.swing.JFormattedTextField vxFieldAdd;
    private javax.swing.JFormattedTextField vxFieldEdit;
    private javax.swing.JLabel vxLabelAdd;
    private javax.swing.JLabel vxLabelEdit;
    private javax.swing.JFormattedTextField vyFieldAdd;
    private javax.swing.JFormattedTextField vyFieldEdit;
    private javax.swing.JLabel vyLabelAdd;
    private javax.swing.JLabel vyLabelEdit;
    private javax.swing.JFormattedTextField vzFieldAdd;
    private javax.swing.JFormattedTextField vzFieldEdit;
    private javax.swing.JLabel vzLabelAdd;
    private javax.swing.JLabel vzLabelEdit;
    private javax.swing.JFormattedTextField xFieldAdd;
    private javax.swing.JFormattedTextField xFieldEdit;
    private javax.swing.JLabel xLabelAdd;
    private javax.swing.JLabel xLabelEdit;
    private javax.swing.JFormattedTextField yFieldAdd;
    private javax.swing.JFormattedTextField yFieldEdit;
    private javax.swing.JLabel yLabelAdd;
    private javax.swing.JLabel yLabelEdit;
    private javax.swing.JFormattedTextField zFieldAdd;
    private javax.swing.JFormattedTextField zFieldEdit;
    private javax.swing.JLabel zLabelAdd;
    private javax.swing.JLabel zLabelEdit;
    private javax.swing.JLabel zoomLabel;
    private javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
}
