package no.uib.probe.quicksearchprot;

import com.compomics.util.gui.UtilitiesGUIDefaults;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import no.uib.probe.quicksearchprot.controllers.Controller;
import no.uib.probe.quicksearchprot.gui.QSPROTGUI;
import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;
import no.uib.probe.quicksearchprot.util.MainUtilities;

/**
 * The main application class for QuickSearchProt.
 * <p>
 * QuickSearchProt is a search settings optimization workflow targeting the optimization of
 * search settings for different proteomics search engines.
 * </p>
 *
 * @author Yehia Mokhtar Farag
 */
public class QuickSearchProtApp {

     /**
     * The main method to launch the QuickSearchProt application.
     *
     * @param args the command line arguments (unused)
     */
     public static void main(String[] args) {
        // Set the Nimbus look and feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(QSPROTGUI.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Create and display the UI in the Event Dispatch Thread
        java.awt.EventQueue.invokeLater(() -> {
            boolean nimbusLookAndFeelSet = false;

            try {
                // Try to set the default look and feel via the utility method
                nimbusLookAndFeelSet = UtilitiesGUIDefaults.setLookAndFeel();

                // Fix for the scroll bar thumb disappearing in Nimbus
                LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
                UIDefaults defaults = lookAndFeel.getDefaults();
                defaults.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));

                // Warn user if the look and feel couldn't be set
                if (!nimbusLookAndFeelSet) {
                    JOptionPane.showMessageDialog(null, """
                                                        Failed to set the default look and feel. Using backup look and feel.
                                                        QSProt will work but not look as good as it should...""",
                            "Look and Feel",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                // Initialize the main controller
                final Controller mainController = new Controller();

                // Instantiate and show the main GUI, overriding processData to handle data processing
                QSPROTGUI qsProtView = new QSPROTGUI() {
                    @Override
                    public void processData(QSProtInputsEntity projectEntity) {
                        // Update UI to show processing started
                        MainUtilities.getDisplayExecuter().submit(() -> {
                            MainUtilities.QSProtWaitingHandler.startProgress();                            
                            MainUtilities.QSProtWaitingHandler.addMainStepMassage(
                                    "----------  Start data processing  ----------"
                            );
                        });
                        // Process data in the background
                        MainUtilities.getDisplayExecuter().submit(() -> {
                            mainController.initializedController(projectEntity);
                            mainController.startDataProcessing();
                            updatePanelView(2);
                        });
                    }
                };
                qsProtView.setVisible(true);

            } catch (HeadlessException | IOException e) {
                // Log exception if any unexpected error occurs
                java.util.logging.Logger.getLogger(QuickSearchProtApp.class.getName())
                        .log(java.util.logging.Level.SEVERE, "Failed to start QuickSearchProtApp", e);
            }
        });
    }
}