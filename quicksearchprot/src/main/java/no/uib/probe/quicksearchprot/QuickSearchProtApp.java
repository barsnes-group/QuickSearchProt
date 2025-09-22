package no.uib.probe.quicksearchprot;

import com.compomics.util.gui.UtilitiesGUIDefaults;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import no.uib.probe.quicksearchprot.controllers.Controller;
import no.uib.probe.quicksearchprot.gui.QSPROTGUI;
import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;
import no.uib.probe.quicksearchprot.util.MainUtilities;

/**
 * This app is search settings optimization workflow that aim to optimize search
 * settings for different Proteomics search engines
 *
 * @author Yehia Mokhtar Farag
 */
public class QuickSearchProtApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */    
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(QSPROTGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {

            boolean numbusLookAndFeelSet;
            try {

                numbusLookAndFeelSet = UtilitiesGUIDefaults.setLookAndFeel();
                // fix for the scroll bar thumb disappearing...
                LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
                UIDefaults defaults = lookAndFeel.getDefaults();
                defaults.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
                if (!numbusLookAndFeelSet) {
                    JOptionPane.showMessageDialog(null, """
                                                    Failed to set the default look and feel. Using backup look and feel.
                                                    QSProt will work but not look as good as it should...""", "Look and Feel",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                final Controller mainController = new Controller();

                QSPROTGUI QSProtView = new QSPROTGUI() {
                    @Override
                    public void processData(QSProtInputsEntity projectEntity) {
                        MainUtilities.getDisplayExecuter().submit(() -> {
                            MainUtilities.QSProtWaitingHandler.startProgress();
                            MainUtilities.QSProtWaitingHandler.addMainStepMassage("*******  Start data processing  *******");
                        });
                        MainUtilities.getDisplayExecuter().submit(() -> {
                            mainController.initializedController(projectEntity);
                            mainController.startDataProcessing();
                            updatePanelView(2);
                        });
                    }

                };
                QSProtView.setVisible(true);

            } catch (Exception e) {
            }
        });
    }

}
