package no.uib.probe.quicksearchprot.gui;

import com.compomics.util.experiment.identification.Advocate;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import no.uib.probe.quicksearchprot.handllers.QSPDatasetHandler;
import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;
import no.uib.probe.quicksearchprot.util.MainUtilities;

/**
 * Abstract GUI class for QuickSearchProt main application window.
 * <p>
 * Handles user input, UI actions, and input validation for project setup.
 * Subclasses must implement {@link #processData(QSProtInputsEntity)} for
 * workflow.
 *
 * @author Yehia Farag //
 */
public abstract class QSPROTGUI extends javax.swing.JFrame {

    /**
     * Creates new form QSPROTGUI
     */
    // Tracks the last used directory in file dialogs.
    private String lastSelectedDirectory = "D:\\QuickSearchProt\\testdata\\data\\PXD028427";// "/";
    // Holds user input parameters for processing.
    private  QSProtInputsEntity inputEntity = new QSProtInputsEntity();
    private final ImageProgressBar updatedProgressBar;

    /**
     * Creates the main application GUI and initializes all UI components.
     */
    public QSPROTGUI() {
        initComponents();

        updatedProgressBar = new ImageProgressBar();
        updatedProgressBar.setVisible(true);
        updatedProgressBar.setSize(new Dimension(800, 25));
        updatedProgressBar.setOpaque(true);
        progressBarContainer.add(updatedProgressBar);

        jTextField1.setText("joyo");
        searchSettingsParFileLabel.setText("D:\\QuickSearchProt\\testdata\\PXD028427\\PRIDE_Search.par");
        mgfFileLable.setText("D:\\QuickSearchProt\\testdata\\PXD028427\\qExactive01819.mgf");
        fastaLabel.setText("D:\\QuickSearchProt\\testdata\\PXD028427\\uniprot-human-reviewed-trypsin-june-2021_concatenated_target_decoy.fasta");
        outputFolderLabel.setText("D:\\QuickSearchProt\\testdata\\PXD028427\\yoyo");
        jButton1ActionPerformed(null);
        jButton2ActionPerformed(null);
        jButton3ActionPerformed(null);
        jButton4ActionPerformed(null);
        setupUI();

    }

    /**
     * Set up the UI defaults and listeners.
     */
    private void setupUI() {
        setBackground(Color.WHITE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Subset size slider setup
        jSlider1.setMinimum(1500);
        jSlider1.setValue(1500);
        jSlider1.setMaximum(300000);
        jLabel5.setText(Integer.toString(jSlider1.getValue()));
        jSlider1.addChangeListener((ChangeEvent e) -> {
            jLabel5.setText(Integer.toString(jSlider1.getValue()));
            inputEntity.setSubSetSize(jSlider1.getValue());
        });

        // Adjustment mode radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(jRadioButton1);
        group.add(jRadioButton2);
        jRadioButton1.setSelected(true);

        // Default settings
        jCheckBox5.setSelected(true);
        spectrumLabel.setForeground(Color.BLACK);
        jSlider1.setEnabled(!jCheckBox5.isSelected());
        jLabel5.setEnabled(!jCheckBox5.isSelected());
        inputEntity.setReGenerateSubset(!jCheckBox5.isSelected());
        if (!jCheckBox5.isSelected()) {
            jCheckBox4.setSelected(true);
            jCheckBox4.setEnabled(false);
        } else {
            jCheckBox4.setEnabled(true);
        }

        // Set up main log/progress panels for MainUtilities
        MainUtilities.QSProtWaitingHandler.setMainPrgressBar(updatedProgressBar);
        MainUtilities.QSProtWaitingHandler.setMainLogTextPanel(logTextArea);
        MainUtilities.QSProtWaitingHandler.setMainOutputTextPanel(outputTextArea);
        MainUtilities.QSProtWaitingHandler.setMainProcessesTextPanel(mainProcessTextArea);

        // Ensure auto-scroll for log/output areas
        DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret caret2 = (DefaultCaret) outputTextArea.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret caret3 = (DefaultCaret) mainProcessTextArea.getCaret();
        caret3.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        logTextArea.setEditable(false);
        jTabbedPane1.setSelectedIndex(0);
        setLocationRelativeTo(null);

        // Set window icon
        ImageIcon myAppImage = loadIcon();
        if (myAppImage != null) {
            setIconImage(myAppImage.getImage());
        }

        jButton1.setEnabled(false);
        jRadioButton1ActionPerformed(null);
        jCheckBox4ActionPerformed(null);

//        jCheckBox5ActionPerformed(null);
    }

    /**
     * Loads the application icon.
     *
     * @return The ImageIcon object, or null if not found.
     */
    private ImageIcon loadIcon() {
        return new ImageIcon(getClass().getResource("/qsprot_transparent.png"));
    }

    /**
     * Updates the main panel view to the given tab index and ensures caret
     * policy.
     *
     * @param viewIndex selected tab index (0: log, 1: main steps, 2: output)
     */
    public void updatePanelView(int viewIndex) {
        MainUtilities.getDisplayExecuter().submit(() -> {
            jTabbedPane1.setSelectedIndex(viewIndex);
            switch (viewIndex) {
                case 0 ->
                    ((DefaultCaret) logTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                case 1 ->
                    ((DefaultCaret) mainProcessTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                case 2 ->
                    ((DefaultCaret) outputTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                default ->
                    throw new AssertionError("Invalid tab index");
            }
           
            jTabbedPane1.repaint();
            System.out.println("updated view to "+viewIndex);
        });

    }

    /**
     * Reactive processing button after the process is done
     *
     */
    public void reactivateProcessingBtn() {
        this.jButton5.setEnabled(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        inputOutputPanel = new javax.swing.JPanel();
        searchSettingsLabel = new javax.swing.JLabel();
        spectrumLabel = new javax.swing.JLabel();
        databaseLabel = new javax.swing.JLabel();
        outputLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        searchSettingsFileLabel = new javax.swing.JLabel();
        mgfFileLable = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        fastaLabel = new javax.swing.JLabel();
        outputFolderLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        searchSettingsParFileLabel = new javax.swing.JLabel();
        inputOutputPanel1 = new javax.swing.JPanel();
        projectNameLabel = new javax.swing.JLabel();
        jCheckBox4 = new javax.swing.JCheckBox();
        jSlider1 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jCheckBox5 = new javax.swing.JCheckBox();
        jButton5 = new javax.swing.JButton();
        inputOutputPanel3 = new javax.swing.JPanel();
        searchSettingsFileLabel4 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        inputOutputPanel2 = new javax.swing.JPanel();
        searchSettingsFileLabel3 = new javax.swing.JLabel();
        searchEnginesLabel = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        mainProcessTextArea = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        projectNameLabel_ = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        progressBarContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setAutoscrolls(true);
        jPanel1.setMaximumSize(new java.awt.Dimension(823, 670));
        jPanel1.setMinimumSize(new java.awt.Dimension(823, 670));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/QS_icon_141_125.png"))); // NOI18N

        welcomeLabel.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        welcomeLabel.setText("<html><b><b style=\"color:#0B78A2\">Q</b>uick<b style=\"color:#E6580B\">S</b>earchProt</b><p style=\"font-size: 11px;margin-top: 10px;\">Automatic selection of search parameter values for mass spectrometry-based search engines.</p></html>");

        inputOutputPanel.setBackground(new java.awt.Color(255, 255, 255));
        inputOutputPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        inputOutputPanel.setMaximumSize(new java.awt.Dimension(1000, 150));
        inputOutputPanel.setMinimumSize(new java.awt.Dimension(1000, 150));

        searchSettingsLabel.setText("Search Settings");

        spectrumLabel.setText("Spectrum File");

        databaseLabel.setText("Database File");

        outputLabel.setText("Output Folder");

        jButton1.setText("Add");
        jButton1.setToolTipText("To enable select (Selected parameters to adjust option) from adjustment mode panel)");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        mgfFileLable.setText("     Currently only MGF files supported");
        mgfFileLable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jButton2.setText("Add");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Add");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Add");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        fastaLabel.setText("     FASTA file formate");
        fastaLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        fastaLabel.setMaximumSize(new java.awt.Dimension(201, 18));
        fastaLabel.setMinimumSize(new java.awt.Dimension(201, 18));
        fastaLabel.setPreferredSize(new java.awt.Dimension(201, 18));

        outputFolderLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        outputFolderLabel.setLabelFor(jButton4);
        outputFolderLabel.setText("     Generated data location");
        outputFolderLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        outputFolderLabel.setMaximumSize(new java.awt.Dimension(201, 18));
        outputFolderLabel.setMinimumSize(new java.awt.Dimension(201, 18));
        outputFolderLabel.setPreferredSize(new java.awt.Dimension(201, 18));

        jLabel2.setText("<html><b style=\"color: black\">Input/Output</b></html>");

        searchSettingsParFileLabel.setText("     Search settings file generated by SearchGUI tool (.par)");
        searchSettingsParFileLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        searchSettingsParFileLabel.setMaximumSize(new java.awt.Dimension(201, 18));
        searchSettingsParFileLabel.setMinimumSize(new java.awt.Dimension(201, 18));
        searchSettingsParFileLabel.setPreferredSize(new java.awt.Dimension(201, 18));

        javax.swing.GroupLayout inputOutputPanelLayout = new javax.swing.GroupLayout(inputOutputPanel);
        inputOutputPanel.setLayout(inputOutputPanelLayout);
        inputOutputPanelLayout.setHorizontalGroup(
            inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputOutputPanelLayout.createSequentialGroup()
                        .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchSettingsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spectrumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(databaseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jButton2)
                            .addComponent(jButton3)
                            .addComponent(jButton4))
                        .addGap(18, 18, 18)
                        .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(fastaLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                            .addComponent(mgfFileLable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(searchSettingsParFileLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputFolderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(inputOutputPanelLayout.createSequentialGroup()
                .addGap(240, 240, 240)
                .addComponent(searchSettingsFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 758, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        inputOutputPanelLayout.setVerticalGroup(
            inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchSettingsLabel)
                    .addComponent(jButton1)
                    .addComponent(searchSettingsFileLabel)
                    .addComponent(searchSettingsParFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spectrumLabel)
                    .addComponent(mgfFileLable)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseLabel)
                    .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(fastaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputLabel)
                    .addComponent(jButton4)
                    .addComponent(outputFolderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        inputOutputPanel1.setBackground(new java.awt.Color(255, 255, 255));
        inputOutputPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        inputOutputPanel1.setMaximumSize(new java.awt.Dimension(1092, 81));
        inputOutputPanel1.setMinimumSize(new java.awt.Dimension(1092, 81));

        jCheckBox4.setText("Re-generate subset");
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });

        jLabel4.setText("Subset size");

        jLabel5.setText("jLabel5");

        jLabel6.setText("<html><b style=\"color: black\">Subset settings</b></html>");

        jCheckBox5.setText("Use suggested size");
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout inputOutputPanel1Layout = new javax.swing.GroupLayout(inputOutputPanel1);
        inputOutputPanel1.setLayout(inputOutputPanel1Layout);
        inputOutputPanel1Layout.setHorizontalGroup(
            inputOutputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(inputOutputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputOutputPanel1Layout.createSequentialGroup()
                        .addGroup(inputOutputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(inputOutputPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckBox4)
                                .addGap(61, 61, 61)
                                .addComponent(jCheckBox5))
                            .addGroup(inputOutputPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)))
                        .addGap(0, 0, 0)
                        .addComponent(projectNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 758, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        inputOutputPanel1Layout.setVerticalGroup(
            inputOutputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputOutputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputOutputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectNameLabel)
                .addGap(0, 0, 0))
        );

        jButton5.setText("<html><b style=\"color: black\">Process</b></html>");
        jButton5.setMaximumSize(new java.awt.Dimension(130, 81));
        jButton5.setMinimumSize(new java.awt.Dimension(130, 81));
        jButton5.setPreferredSize(new java.awt.Dimension(130, 81));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        inputOutputPanel3.setBackground(new java.awt.Color(255, 255, 255));
        inputOutputPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        inputOutputPanel3.setMaximumSize(new java.awt.Dimension(937, 81));
        inputOutputPanel3.setMinimumSize(new java.awt.Dimension(937, 81));
        inputOutputPanel3.setPreferredSize(new java.awt.Dimension(937, 81));

        jRadioButton1.setText("Adjust all parameters");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton2.setText("Select parameters to adjust");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jLabel7.setText("<html><b>Adjustment mode</b></html>");

        javax.swing.GroupLayout inputOutputPanel3Layout = new javax.swing.GroupLayout(inputOutputPanel3);
        inputOutputPanel3.setLayout(inputOutputPanel3Layout);
        inputOutputPanel3Layout.setHorizontalGroup(
            inputOutputPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputOutputPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputOutputPanel3Layout.createSequentialGroup()
                        .addGroup(inputOutputPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jRadioButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRadioButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchSettingsFileLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 758, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        inputOutputPanel3Layout.setVerticalGroup(
            inputOutputPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanel3Layout.createSequentialGroup()
                .addGroup(inputOutputPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputOutputPanel3Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(searchSettingsFileLabel4)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputOutputPanel3Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jRadioButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        inputOutputPanel2.setBackground(new java.awt.Color(255, 255, 255));
        inputOutputPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        inputOutputPanel2.setMaximumSize(new java.awt.Dimension(858, 81));
        inputOutputPanel2.setMinimumSize(new java.awt.Dimension(858, 81));
        inputOutputPanel2.setPreferredSize(new java.awt.Dimension(858, 81));

        searchEnginesLabel.setText("<html><b>Search Engine</b></html>");

        jCheckBox2.setText("Sage");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jCheckBox3.setText("X! Tandem");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout inputOutputPanel2Layout = new javax.swing.GroupLayout(inputOutputPanel2);
        inputOutputPanel2.setLayout(inputOutputPanel2Layout);
        inputOutputPanel2Layout.setHorizontalGroup(
            inputOutputPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputOutputPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputOutputPanel2Layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(searchSettingsFileLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 758, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(searchEnginesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox3))
                .addContainerGap())
        );
        inputOutputPanel2Layout.setVerticalGroup(
            inputOutputPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputOutputPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(searchEnginesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputOutputPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(searchSettingsFileLabel3)
                    .addGroup(inputOutputPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox2)
                        .addGap(4, 4, 4)
                        .addComponent(jCheckBox3)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        logTextArea.setBackground(new java.awt.Color(255, 255, 255));
        logTextArea.setColumns(20);
        logTextArea.setLineWrap(true);
        logTextArea.setRows(5);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        logTextArea.setEnabled(false);
        logTextArea.setMaximumSize(new java.awt.Dimension(1000, 150));
        jScrollPane1.setViewportView(logTextArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Log ", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane3.setMaximumSize(new java.awt.Dimension(232, 84));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(232, 84));
        jScrollPane3.setName(""); // NOI18N
        jScrollPane3.setPreferredSize(new java.awt.Dimension(232, 84));

        mainProcessTextArea.setBackground(new java.awt.Color(255, 255, 255));
        mainProcessTextArea.setColumns(20);
        mainProcessTextArea.setRows(5);
        mainProcessTextArea.setWrapStyleWord(true);
        mainProcessTextArea.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        mainProcessTextArea.setEnabled(false);
        jScrollPane3.setViewportView(mainProcessTextArea);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Main steps", jPanel3);

        outputTextArea.setBackground(new java.awt.Color(255, 255, 255));
        outputTextArea.setColumns(20);
        outputTextArea.setRows(5);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        outputTextArea.setEnabled(false);
        jScrollPane4.setViewportView(outputTextArea);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Output", jPanel5);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jPanel4.setMaximumSize(new java.awt.Dimension(799, 36));
        jPanel4.setMinimumSize(new java.awt.Dimension(799, 36));

        projectNameLabel_.setText("<html><b>Project name</b></html></b></html>");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(projectNameLabel_, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 653, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameLabel_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        progressBarContainer.setMaximumSize(new java.awt.Dimension(1000, 25));
        progressBarContainer.setMinimumSize(new java.awt.Dimension(1000, 25));
        progressBarContainer.setPreferredSize(new java.awt.Dimension(1000, 25));

        javax.swing.GroupLayout progressBarContainerLayout = new javax.swing.GroupLayout(progressBarContainer);
        progressBarContainer.setLayout(progressBarContainerLayout);
        progressBarContainerLayout.setHorizontalGroup(
            progressBarContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        progressBarContainerLayout.setVerticalGroup(
            progressBarContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(inputOutputPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(inputOutputPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(inputOutputPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addComponent(inputOutputPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(progressBarContainer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(inputOutputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(inputOutputPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(inputOutputPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(inputOutputPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBarContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
  /**
     * Handle "Re-generate subset" checkbox action.
     */
    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed

        inputEntity.setReGenerateSubset(jCheckBox4.isSelected());
        inputEntity.setSubSetSize(-1);
    }//GEN-LAST:event_jCheckBox4ActionPerformed
    /**
     * Handle "Process" button click.
     */
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        progressBarContainer.add(updatedProgressBar);
        updatePanelView(1);
        Thread t = null;
        try {
            if (validateInputs()) {
                MainUtilities.getDisplayExecuter().submit(() -> {
                    this.jButton5.setEnabled(false);
                    MainUtilities.QSProtWaitingHandler.addLogMassage("Process started");
                    
                });

                t = new Thread(() -> {
                    processData(inputEntity);    
                });
                t.start();

            }
        } catch (RuntimeException e) {
            MainUtilities.QSProtWaitingHandler.addMainErrorMassage("Error during the data processing...Re-process the data!");
            if (t != null) {
                t.interrupt();
            }
        } finally {

        }

    }//GEN-LAST:event_jButton5ActionPerformed
    /**
     * Handle "Use suggested size" checkbox action.
     */
    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
        spectrumLabel.setForeground(Color.BLACK);
        if (!jCheckBox5.isSelected() && inputEntity.getInputSpectrumFilePath() != null) {
            MainUtilities.getDisplayExecuter().submit(() -> {
                int count = QSPDatasetHandler.countTotalSpectra(inputEntity.getInputSpectrumFilePath());
                jSlider1.setMaximum(count);
            });
        } else if (inputEntity.getInputSpectrumFilePath() == null) {
            spectrumLabel.setForeground(Color.RED);
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : Input MGF file (.mgf) is required to enable subset size selection!");
            jCheckBox5.setSelected(true);
            jCheckBox5ActionPerformed(null);
        }
        jSlider1.setEnabled(!jCheckBox5.isSelected());
        jLabel5.setEnabled(!jCheckBox5.isSelected());
        inputEntity.setReGenerateSubset(!jCheckBox5.isSelected());
        if (!jCheckBox5.isSelected()) {
            jCheckBox4.setSelected(true);
            jCheckBox4.setEnabled(false);

        } else {
            jCheckBox4.setEnabled(true);
        }
        jCheckBox4ActionPerformed(null);
    }//GEN-LAST:event_jCheckBox5ActionPerformed
    // Placeholders for unused checkbox actions (may be used for future features)
    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed

    }//GEN-LAST:event_jCheckBox3ActionPerformed
    // Placeholders for unused checkbox actions (may be used for future features)
    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox2ActionPerformed
    /**
     * Handle output folder selection.
     */
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(lastSelectedDirectory));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            outputFolderLabel.setText("     " + selectedFile.getAbsolutePath());
            lastSelectedDirectory = selectedFile.getPath();
            inputEntity.setOutputFolderPath(lastSelectedDirectory);
        } else {
            outputFolderLabel.setText("     Generated data location");
            inputEntity.setOutputFolderPath(null);
        }
    }//GEN-LAST:event_jButton4ActionPerformed
    /**
     * Handle FASTA file selection.
     */
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("FASTA file format (.fasta)", "fasta");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File(lastSelectedDirectory));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fastaLabel.setText("     " + selectedFile.getAbsolutePath());
            lastSelectedDirectory = selectedFile.getPath();
            inputEntity.setInputFastaFilePath(lastSelectedDirectory);
        } else {
            fastaLabel.setText("     FASTA file format");
            inputEntity.setInputFastaFilePath(null);
        }
    }//GEN-LAST:event_jButton3ActionPerformed
    /**
     * Handle MGF spectrum file selection.
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Only MGF files supported (.mgf)", "mgf");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File(lastSelectedDirectory));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            mgfFileLable.setText("     " + selectedFile.getAbsolutePath());
            lastSelectedDirectory = selectedFile.getPath();
            inputEntity.setInputSpectrumFilePath(lastSelectedDirectory);
        } else {
            mgfFileLable.setText("     Currently only MGF files supported");
            inputEntity.setInputSpectrumFilePath(null);
        }
    }//GEN-LAST:event_jButton2ActionPerformed
    /**
     * Handle Search Parameter file selection.
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Search settings file generated by SearchGUI tool (.par)", "par");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File(lastSelectedDirectory));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            lastSelectedDirectory = selectedFile.getPath();
            searchSettingsParFileLabel.setText("     " + selectedFile.getAbsolutePath());
            inputEntity.setSearchParameterFilePath(lastSelectedDirectory);
        } else {
            searchSettingsParFileLabel.setText("     Search settings file generated by SearchGUI tool (.par)");
            inputEntity.setSearchParameterFilePath(null);
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    /**
     * "Adjust all parameters" mode selected.
     */
    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        searchSettingsFileLabel.setEnabled(!jRadioButton1.isSelected());
        jButton1.setEnabled(!jRadioButton1.isSelected());
        inputEntity.setAdjustAllSearchParameters(jRadioButton1.isSelected());
    }//GEN-LAST:event_jRadioButton1ActionPerformed
    /**
     * "Select parameters to adjust" mode selected. Opens parameter selection
     * dialog.
     */
    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        searchSettingsFileLabel.setEnabled(!jRadioButton1.isSelected());
        jButton1.setEnabled(!jRadioButton1.isSelected());
        inputEntity.setAdjustAllSearchParameters(jRadioButton1.isSelected());
        setEnabled(false);

        SelectParametersPanel selectPanel = new SelectParametersPanel();
        selectPanel.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!selectPanel.getParametersToAdjust().isAtleastOneSelection()) {
                    jRadioButton1.setSelected(true);
                }
                QSPROTGUI.this.setEnabled(true);
                selectPanel.dispose();
                inputEntity.setParamsToAdjust(selectPanel.getParametersToAdjust());
            }
        });
        selectPanel.setVisible(true);

    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * Validate the user input fields. Sets error highlighting and logs error
     * messages as needed.
     *
     * @return true if all required fields are valid, false otherwise
     */
    private boolean validateInputs() {
        // Reset label colors
        projectNameLabel_.setForeground(Color.BLACK);
        searchSettingsParFileLabel.setForeground(Color.BLACK);
        searchSettingsLabel.setForeground(Color.BLACK);
        spectrumLabel.setForeground(Color.BLACK);
        fastaLabel.setForeground(Color.BLACK);
        databaseLabel.setForeground(Color.BLACK);
        outputLabel.setForeground(Color.BLACK);
        searchEnginesLabel.setForeground(Color.BLACK);
        refillProjectInfo();

        boolean valid = true;

        // Project name
        String projectName = jTextField1.getText();
        if (projectName == null || projectName.trim().isEmpty()) {
            projectNameLabel_.setForeground(Color.RED);
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : The project name is missing!");
            valid = false;
            inputEntity.setDatasetId(null);
        } else {
            inputEntity.setDatasetId(projectName.replace(" ", "_"));
        }

        // Search parameter file (if not adjusting all params)
        if (inputEntity.getSearchParameterFilePath() == null && !inputEntity.isAdjustAllSearchParameters()) {
            searchSettingsLabel.setForeground(Color.RED);
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : Search Parameter File (.par) is required!");
            valid = false;
        }

        // Input spectrum file
        if (inputEntity.getInputSpectrumFilePath() == null) {
            spectrumLabel.setForeground(Color.RED);
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : Input MGF file (.mgf) is required!");
            valid = false;
        }

        // Input FASTA file
        if (inputEntity.getInputFastaFilePath() == null) {
            databaseLabel.setForeground(Color.RED);
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : Input Sequence database file (.fasta) is required!");
            valid = false;
        }

        // Output folder
        if (inputEntity.getOutputFolderPath() == null) {
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : Select output folder!");
            outputLabel.setForeground(Color.RED);
            valid = false;
        }

        // Search engines
        inputEntity.getSearchEngineList().clear();
        if (jCheckBox2.isSelected()) {
            inputEntity.addSearchEngine(Advocate.sage.getName());
        }
        if (jCheckBox3.isSelected()) {
            inputEntity.addSearchEngine(Advocate.xtandem.getName());
        }
        if (!jCheckBox2.isSelected() && !jCheckBox3.isSelected()) {
            searchEnginesLabel.setForeground(Color.RED);
            MainUtilities.QSProtWaitingHandler.addLogMassage("Error : Select at least 1 search engine!");
            valid = false;
        }

        MainUtilities.QSProtWaitingHandler.addLogMassage(" ");
        return valid;
    }
    private void refillProjectInfo(){
//    jRadioButton1ActionPerformed(null);
//    inputEntity.setInputSpectrumFilePath(lastSelectedDirectory);
    
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
//        this.setComponentEnabled(this, enabled);
    }

    public void setComponentEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setComponentEnabled(child, enabled);
            }
        }
    }

    /**
     * Subclasses must implement this to process the input parameters.
     *
     * @param projectEntity The user inputs, validated and prepared.
     */
    public abstract void processData(QSProtInputsEntity projectEntity);

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JLabel fastaLabel;
    private javax.swing.JPanel inputOutputPanel;
    private javax.swing.JPanel inputOutputPanel1;
    private javax.swing.JPanel inputOutputPanel2;
    private javax.swing.JPanel inputOutputPanel3;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JTextArea mainProcessTextArea;
    private javax.swing.JLabel mgfFileLable;
    private javax.swing.JLabel outputFolderLabel;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JPanel progressBarContainer;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JLabel projectNameLabel_;
    private javax.swing.JLabel searchEnginesLabel;
    private javax.swing.JLabel searchSettingsFileLabel;
    private javax.swing.JLabel searchSettingsFileLabel3;
    private javax.swing.JLabel searchSettingsFileLabel4;
    private javax.swing.JLabel searchSettingsLabel;
    private javax.swing.JLabel searchSettingsParFileLabel;
    private javax.swing.JLabel spectrumLabel;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}
