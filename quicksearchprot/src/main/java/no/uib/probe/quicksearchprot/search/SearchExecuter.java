/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.search;

import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.io.identification.IdfileReaderFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.io.IoUtil;
import com.compomics.util.parameters.identification.IdentificationParameters;
import eu.isas.searchgui.SearchHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.OptProtDatasetHandler;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author yfa041
 */
public class SearchExecuter {

    /**
     * The compomics PTM factory.
     */
    private static final ModificationFactory ptmFactory = ModificationFactory.getInstance();

    /**
     *
     * @param processId
     * @param searchInputSetting
     * @param msFile
     * @param fastaFile
     * @param tempIdParam
     * @param identificationParametersFile
     * @return results Folder
     */
    public synchronized static File executeSearch(String processId, SearchInputSetting searchInputSetting, File msFile, File fastaFile, IdentificationParameters tempIdParam, File identificationParametersFile) {

        if (searchInputSetting.isRunNovor() || searchInputSetting.isRunDirecTag()) {
            //remove terminal variable modifications andd add common before run novor         
            List<String> toRemoveMod = new ArrayList<>();
            for (String mod : tempIdParam.getSearchParameters().getModificationParameters().getVariableModifications()) {
                if (ptmFactory.getModification(mod).getModificationType().isNTerm() || ptmFactory.getModification(mod).getModificationType().isCTerm()) {
                    System.out.println("terminal : " + mod);
                    toRemoveMod.add(mod);
                }
            }
            for (String mod : toRemoveMod) {
                tempIdParam.getSearchParameters().getModificationParameters().removeVariableModification(mod);
            }
        }

        File resultOutput = new File(MainUtilities.GET_WORKING_FOLDER_PATH(searchInputSetting.getDatasetId()), processId);
        resultOutput.mkdir();
        File tempSearchEngineFolder = new File(MainUtilities.GET_WORKING_FOLDER_PATH(searchInputSetting.getDatasetId()), processId + "_temp");
        tempSearchEngineFolder.mkdir();

        ArrayList<File> msFileInList = new ArrayList<>();
        msFileInList.add(msFile);
        SearchHandler.setTempSearchEngineFolderPath(tempSearchEngineFolder.getAbsolutePath());
        SearchHandler.setTempFolderPath(null);
        SearchHandler searchHandler = new SearchHandler(tempIdParam, resultOutput, tempSearchEngineFolder, processId, msFileInList,
                fastaFile, new ArrayList<>(),
                identificationParametersFile,
                searchInputSetting.isRunOmssa(),
                searchInputSetting.isRunXTandem(),
                searchInputSetting.isRunMsgf(),
                searchInputSetting.isRunMsAmanda(),
                searchInputSetting.isRunMyriMatch(),
                searchInputSetting.isRunComet(),
                searchInputSetting.isRunTide(),
                searchInputSetting.isRunAndromeda(),
                searchInputSetting.isRunMetaMorpheus(),
                searchInputSetting.isRunSage(),
                searchInputSetting.isRunNovor(),
                searchInputSetting.isRunDirecTag(),
                searchInputSetting.getOmssaFolder(),
                searchInputSetting.getxTandemFolder(),
                searchInputSetting.getMsgfFolder(),
                searchInputSetting.getMsAmandaFolder(),
                searchInputSetting.getMyriMatchFolder(),
                searchInputSetting.getCometFolder(),
                searchInputSetting.getTideFolder(),
                searchInputSetting.getTideIndexLocation(),
                searchInputSetting.getAndromedaFolder(),
                searchInputSetting.getMetaMorpheusFolder(),
                searchInputSetting.getSageFolder(),
                searchInputSetting.getNovorFolder(),
                searchInputSetting.getDirecTagFolder(),
                searchInputSetting.getMakeblastdbFolder(),
                MainUtilities.getProcessingParameter()
        );

        try {
            searchHandler.startSearch(MainUtilities.OptProt_Waiting_Handler);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        File resultsFile = searchHandler.getResultsFolder();

        return resultsFile;

    }

    public static ArrayList<SpectrumMatch> getTagMaches(File destinationFile, File fastaFile, IdentificationParameters identificationParameters, File identificationParametersFile, String msFileNameWithoutExtension, SearchInputSetting searchInputSetting, boolean validOnly) {
        try {
            searchInputSetting.setRunDirecTag(true);
            String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + msFileNameWithoutExtension + Configurations.get_current_file_fingerprent();
            File tempResultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, destinationFile, fastaFile, identificationParameters, identificationParametersFile);
            File direcTagFile = new File(tempResultsFolder, IoUtil.removeExtension(destinationFile.getName()) + ".tags");
            if (!direcTagFile.exists()) {
                System.out.println("there is no tags in the file ...very poor data " + msFileNameWithoutExtension);
//                //delete previos sub mgf and cms files
//                destinationFile.delete();
//                File cms = new File(destinationFile.getParent(), destinationFile.getName().replace(".mgf", ".cms"));
//                cms.delete();
//                //not enough confident tag , increase the number 
//                substractSpectraWithConfidentTag(msFile, fastaFile, startIndex, maxSpectraNumber + 500, msFileHandler, identificationParameters, identificationParametersFile);
                return new ArrayList<>();
            }

            IdfileReader idReader = IdfileReaderFactory.getInstance().getFileReader(direcTagFile);
            System.out.println("file name " + msFileNameWithoutExtension + "   " + IoUtil.removeExtension(destinationFile.getName()));
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(destinationFile, MainUtilities.OptProt_Waiting_Handler);
            ArrayList<SpectrumMatch> matches = idReader.getAllSpectrumMatches(subMsFileHandler, MainUtilities.OptProt_Waiting_Handler, identificationParameters.getSearchParameters());
            if (validOnly) {
                ArrayList<SpectrumMatch> vmatches = new ArrayList<>();
                for (SpectrumMatch sm : matches) {
                    for (TagAssumption ta : sm.getAllTagAssumptions().toList()) {
                        if (ta.getScore() <= 0.01) {
                            sm.setBestTagAssumption(ta);                          
                            vmatches.add(sm);
                        }
                    }
                }
               return vmatches;
               
            }
            return matches;
        } catch (IOException | SQLException | ClassNotFoundException | InterruptedException | JAXBException | XmlPullParserException | XMLStreamException ex) {
            Logger.getLogger(OptProtDatasetHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();

    }

}
