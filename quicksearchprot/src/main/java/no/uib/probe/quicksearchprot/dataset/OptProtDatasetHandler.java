/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.dataset;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.converters.DecoyConverter;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.io.identification.IdfileReaderFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileWriter;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.IoUtil;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.DirecTagParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import com.compomics.util.parameters.identification.tool_specific.XtandemParameters;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.model.ConfidentTagSorter;
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.search.SearchExecuter;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.util.OptProtWaitingHandler;
import no.uib.probe.quicksearchprot.util.SpectraUtilities;
import org.apache.commons.collections15.map.LinkedMap;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author yfa041
 */
public class OptProtDatasetHandler {

    private final SearchInputSetting searchInputSetting;
    private int startIndex = 0;
    private File subFastaFile = null;

    private File subMsFile = null;
//    private int counter = 0;
    private double acceptedTagEvalue;
//    private boolean smallDataset;

    public OptProtDatasetHandler(SearchInputSetting searchInputSetting) {
        this.searchInputSetting = searchInputSetting;
    }

    public SearchInputSetting getSearchInputSetting() {
        return searchInputSetting;
    }

    public SearchingSubDataset generateOptProtDataset(String datasetId, File msFile, File fastaFile, Advocate searchEngineToOptimise, File subDataFolder, File identificationParametersFile, boolean wholeDataTest, boolean fullFasta, boolean useOreginalInputs) {
        acceptedTagEvalue = Configurations.ACCEPTED_TAG_EVALUE;

        long start1 = System.currentTimeMillis();
        Advocate standeredReferenceSearchEngine = searchEngineToOptimise;
        SearchingSubDataset optProtDataset = new SearchingSubDataset();
        optProtDataset.setSubMsFile(msFile);
        optProtDataset.setSubFastaFile(fastaFile);
        TreeMap<String, File> subFilesMap = new TreeMap<>(Collections.reverseOrder());
        final String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
        MsFileHandler msFileHandler = new MsFileHandler();
        try {
            msFileHandler.register(msFile, new OptProtWaitingHandler());

        } catch (IOException ex) {
            if (subMsFile != null) {
                subMsFile.delete();
            }
            if (subFastaFile != null && !fullFasta) {
                subFastaFile.delete();
            }
            ex.printStackTrace();
        }

        String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);

        optProtDataset.setOreginalDatasetSpectraSize(spectrumTitles.length);
        if (useOreginalInputs) {
            subFastaFile = fastaFile;
            subMsFile = msFile;
        } else {
            if (!wholeDataTest) {
                subFastaFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_" + fastaFile.getName());
                subMsFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_" + msFile.getName());

            } else {
                subFastaFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_Full_" + fastaFile.getName());
                subMsFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_Full_" + msFile.getName());

            }
            if (fullFasta) {
                subFastaFile = fastaFile;
            }
            for (File f : subDataFolder.listFiles()) {
                if (f.getName().toLowerCase().endsWith(".par") || f.getName().toLowerCase().endsWith(".mgf") || f.getName().toLowerCase().endsWith(".fasta") || f.getName().toLowerCase().endsWith(".txt")) {
                    subFilesMap.put(f.getName().toLowerCase(), f);
                } else {
                    f.delete();
                }
            }

            boolean update = false;
            try {
                final IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE));
                if (!subMsFile.exists()) {
                    update = true;
                    System.out.println("sub ms not exist");
                    subMsFile.createNewFile();
                    //initial param to handel only one time
                    SearchParameters searchParameters = identificationParameters.getSearchParameters();
                    searchParameters.getModificationParameters().clearVariableModifications();
                    searchParameters.getModificationParameters().clearFixedModifications();
                    //initial direcTag param
                    DirecTagParameters direcTagParameters = (DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex());
                    direcTagParameters.setMaxTagCount(1);
                    direcTagParameters.setTagLength(3);
                    direcTagParameters.setNumChargeStates(4);
                    direcTagParameters.setDuplicateSpectra(false);
                    direcTagParameters.setUseChargeStateFromMS(false);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE));
                    //generate subset of spectra 
                    //init Q1
                    int subSize;
                    List<Double> quartileSizeRatios;
                    quartileSizeRatios = this.getSectionRatios(msFile, fastaFile, identificationParameters);

                    Map<String, Spectrum> spectraMap = generatFilteredData(msFile, msFileHandler, quartileSizeRatios, wholeDataTest, searchEngineToOptimise.getIndex());
                    if (spectraMap.size() > 70000) {
                        Configurations.MIN_SUBSET_SIZE = 2000;
                    }
                    if (!wholeDataTest) {
                        if (searchEngineToOptimise.getIndex() == Advocate.sage.getIndex()) {
                            subSize = (int) Math.min(Configurations.MAX_SUBSET_SIZE, (double) spectraMap.size());
                        } else {
                            subSize = (int) Math.min(Configurations.MIN_SUBSET_SIZE, (double) spectraMap.size());//   
                        }
                        System.out.println("final subset size " + subSize);

//                    spectraMap = generatFinalSubset(spectraMap, subSize);
                        spectraMap = generateSubset(datasetId, fileNameWithoutExtension, spectraMap, subSize, fastaFile, identificationParameters);
                    }
                    MainUtilities.cleanOutputFolder(datasetId);

                    //create stabkle subMs file
                    subMsFile = generateMsSubFile(spectraMap, subMsFile);

                    subFastaFile.delete();
                }
                //create stabkle subfasta file
                if (!fullFasta && (!subFastaFile.exists() || update)) {
                    subFastaFile.createNewFile();
                    long start3 = System.currentTimeMillis();
                    searchInputSetting.setRunDirecTag(false);
                    searchInputSetting.setRunNovor(true);
                    final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + fileNameWithoutExtension + Configurations.get_current_file_fingerprent();
                    File resultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, subMsFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE));
                    File NovorFile = new File(resultsFolder, IoUtil.removeExtension(subMsFile.getName()) + ".novor.csv");
                    Set<String> sequences = SpectraUtilities.getSequences(NovorFile);
                    System.out.println("sequence from nover " + sequences.size());
                    long end3rd = System.currentTimeMillis();
                    double total = (end3rd - start3) / 1000.0;
                    System.out.println("process III ( Novor ) in seconds: " + total);
                    long start4 = System.currentTimeMillis();
                    if (searchEngineToOptimise.getIndex() == Advocate.sage.getIndex() || true) {
                        File newSubFasta = new File(subFastaFile.getParent(), subFastaFile.getName().replace(".fasta", "_subFastaFile.fasta"));
                        newSubFasta = initSubFastaFile(newSubFasta, fastaFile, sequences);
                        FastaParameters fastaParameters = FastaParameters.inferParameters(subFastaFile.getAbsolutePath(), MainUtilities.OptProt_Waiting_Handler);
                        DecoyConverter.appendDecoySequences(newSubFasta, subFastaFile, fastaParameters, MainUtilities.OptProt_Waiting_Handler);
                        newSubFasta.delete();
                    } else {
                        subFastaFile = initSubFastaFile(subFastaFile, fastaFile, sequences);
                    }
                    long end4th = System.currentTimeMillis();
                    total = (end4th - start4) / 1000.0;
                    System.out.println("process IV ( Generated Fasta) in seconds: " + total);

                    long end = System.currentTimeMillis();
                    total = (end - start1) / 1000.0;
                    System.out.println("Total Elapsed Time for initInputSubSetFiles in seconds: " + total);
                }
            } catch (IOException ex) {
                if (subMsFile != null) {
                    subMsFile.delete();
                }
                if (subFastaFile != null) {
                    subFastaFile.delete();
                }
                ex.printStackTrace();
            }
        }

        if (subFastaFile != null) {
            optProtDataset.setSubFastaFile(subFastaFile);
        }
        if (subMsFile != null) {
            optProtDataset.setSubMsFile(subMsFile);
        }
        try {

            //run initial identification with user selected SE
            final IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE));
            searchInputSetting.setSelectedSearchEngine(searchEngineToOptimise);
            final String option = "reference_run_default_" + searchEngineToOptimise;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + fileNameWithoutExtension;

            if (standeredReferenceSearchEngine.getIndex() == Advocate.sage.getIndex()) {
//                subFastaFile=fastaFile;
                SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
                sageParameters.setMaxVariableMods(0);
                sageParameters.setNumPsmsPerSpectrum(1);
                sageParameters.setGenerateDecoys(false);

            } else if (standeredReferenceSearchEngine.getIndex() == Advocate.xtandem.getIndex()) {
                XtandemParameters xtandemParameters = (XtandemParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
                xtandemParameters.setProteinQuickAcetyl(false);
                xtandemParameters.setQuickPyrolidone(false);
                xtandemParameters.setStpBias(false);
                xtandemParameters.setRefine(false);
                xtandemParameters.setOutputResults("all");

            }
            String subfileNameWithoutExtension = IoUtil.removeExtension(subMsFile.getName());
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(subMsFile, new OptProtWaitingHandler());
            //init spectra map
            optProtDataset.setSpectraTitiles(subMsFileHandler.getSpectrumTitles(subfileNameWithoutExtension));
            File resultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, subMsFile, subFastaFile, identificationParameters, new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE));
            //          if(MainUtilities.OptProt_Waiting_Handler.isRunFinished())
            List<SpectrumMatch> validatedMaches = SpectraUtilities.getValidatedIdentificationResults(resultsFolder, subMsFile, searchEngineToOptimise, identificationParameters);

            if (validatedMaches == null || validatedMaches.isEmpty()) {
                System.out.println("Error in the system please restart!");
                System.exit(0);
            }
            optProtDataset.setDefaultSettingIdentificationNum(validatedMaches.size());
            optProtDataset.updateValidatedIdRefrenceData(validatedMaches);
            MainUtilities.deleteFolder(resultsFolder);

            int total = subMsFileHandler.getSpectrumTitles(subfileNameWithoutExtension).length;
            optProtDataset.setSubsetSize(total);
            MainUtilities.cleanOutputFolder(datasetId);

            //one more search for threshold?
//            identificationParameters.getSearchParameters().setFragmentIonAccuracy(0.01);
//            resultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, subMsFile, subFastaFile, identificationParameters, new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE));
//            validatedMaches = SpectraUtilities.getValidatedIdentificationResults(resultsFolder, subMsFile, searchEngineToOptimise, identificationParameters);
//            RawScoreModel rawScore = SpectraUtilities.getComparableRawScore(optProtDataset, validatedMaches, Advocate.xtandem, false, "");//(optProtDataset, resultOutput, optProtDataset.getSubMsFile(), Advocate.sage, tempIdParam, updateDataReference);
//            optProtDataset.setComparisonsThreshold(-1.0*rawScore.getFinalScore());
        } catch (IOException ex) {
            if (subMsFile != null) {
                subMsFile.delete();
            }
            if (subFastaFile != null) {
                subFastaFile.delete();
            }
            ex.printStackTrace();
        }
        return optProtDataset;
    }

    private Map<String, Spectrum> substractSpectraFirstLevelDataFiltering(File msFile, MsFileHandler msFileHandler, int startIndex, double stepSize, int lastIndex) {
        Map<String, Spectrum> spectraMap = new LinkedHashMap<>();
        String msFileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
        String[] spectrumTitles = msFileHandler.getSpectrumTitles(msFileNameWithoutExtension);
        double stepAsInt = (int) stepSize;
        double remainFloatValue = stepSize - stepAsInt;
        double remainFloat = 0;

        for (int i = startIndex; i < lastIndex && i < spectrumTitles.length;) {
//            System.out.println("no.uib.probe.optprot.dataset.OptProtDatasetHandler.substractSpectraFirstLevelDataFiltering() "+startIndex+"  "+i+"  "+spectrumTitles[i]);
            Spectrum spectrum = msFileHandler.getSpectrum(msFileNameWithoutExtension, spectrumTitles[i]);
            spectraMap.put(spectrumTitles[i], spectrum);
            i += stepAsInt;
            remainFloat += remainFloatValue;
            if (remainFloat >= 1.0) {
                i++;
                remainFloat = 1.0 - remainFloat;
            }
        }
        return spectraMap;
    }

    private File generateMsSubFile(Map<String, Spectrum> spectraMap, File destinationFile) {

        try {

            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            destinationFile.createNewFile();
            try (MgfFileWriter writer = new MgfFileWriter(destinationFile)) {
                for (String spectrumTitle : spectraMap.keySet()) {
                    Spectrum spectrum = spectraMap.get(spectrumTitle);
                    writer.writeSpectrum(spectrumTitle, spectrum);
                }
                writer.close();
            }

        } catch (IOException ex) {
            if (subMsFile != null) {
                subMsFile.delete();
            }
            if (subFastaFile != null) {
                subFastaFile.delete();
            }
            ex.printStackTrace();
        }
        return destinationFile;

    }

    private List<Double> getSectionRatios(File msFile, File fastaFile, IdentificationParameters identificationParameters) {
        ArrayList<Double> arrayList = new ArrayList<>();
        try {
//            arrayList.add(0.25);
//            arrayList.add(0.25);
//            arrayList.add(0.25);
//            arrayList.add(0.25);
//            arrayList.add(1.0);

            final String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());

            ArrayList<SpectrumMatch> matches = getTagMaches(msFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE), fileNameWithoutExtension);
//            System.out.println("Maches sizes 1 --->>"+matches.size());
            if (matches.isEmpty()) {
                System.out.println("there is no tags in the file ...very poor data " + fileNameWithoutExtension);
                return arrayList;

            }
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(msFile, MainUtilities.OptProt_Waiting_Handler);
            arrayList.clear();
            arrayList.addAll(SpectraUtilities.getTagSectionRatios(subMsFileHandler.getSpectrumTitles(IoUtil.removeExtension(msFile.getName())), matches));
        } catch (IOException ex) {
            Logger.getLogger(OptProtDatasetHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arrayList;
    }

    private Map<String, Spectrum> generatFilteredData(File msFile, MsFileHandler msFileHandler, List<Double> ratios, boolean full, int seIndex) {

        final String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
        String[] fullSpectrumTitiles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);
        int initStartIndex = 0;
        double factor = 1.0 / (double) ratios.size();
        int coverage = (int) Math.round(fullSpectrumTitiles.length * factor);
        int lastIndex = -1;
        int left = fullSpectrumTitiles.length - (coverage * (ratios.size() - 1));

        Map<String, Spectrum> spectraMap = new LinkedMap<>();
        for (int i = 0; i < ratios.size(); i++) {
            if (i == (ratios.size() - 1)) {
                coverage = Math.max(coverage, left);
            }
            //init sub quartile 1
            initStartIndex = lastIndex + 1;
            lastIndex = coverage * (i + 1);
            int subsetQuartileSize;

//            if (full && seIndex == Advocate.xtandem.getIndex()) {
//                subsetQuartileSize = (int) ((ratios.get(i)) * coverage);
//            } else {
            double ratio = ratios.get(i);
            if (ratio <= 0.2 || ratio >= 0.8) {
                ratio = 1.0 - ratio;

            }
            subsetQuartileSize = (int) (ratio * coverage);//;//(1 - ratios.get(i))
//            }
            double step = (double) coverage / (double) subsetQuartileSize;
            System.out.println("total " + fullSpectrumTitiles.length + "  start index " + initStartIndex + "  coverage " + coverage + "  factor " + factor + "   last index " + lastIndex + "  left " + left + "   " + spectraMap.size() + "   ratios " + ratios);
            spectraMap.putAll(substractSpectraFirstLevelDataFiltering(msFile, msFileHandler, initStartIndex, step, lastIndex));

        }
        return spectraMap;
    }

    private Map<String, Spectrum> generateSubset(String datasetId, String fileNameWithoutExtension, Map<String, Spectrum> spectraMap, int subsetSize, File fastaFile, IdentificationParameters identificationParameters) {

        try {
            //generate submsFile
            File destinationFile = new File(MainUtilities.GET_WORKING_FOLDER_PATH(datasetId), Configurations.DEFAULT_RESULT_NAME + "_temp_full_" + spectraMap.size() + "_-_" + fileNameWithoutExtension + ".mgf");
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            destinationFile.createNewFile();
            destinationFile = generateMsSubFile(spectraMap, destinationFile);
            final String subfileNameWithoutExtension = IoUtil.removeExtension(destinationFile.getName());
            //run direct tag and get confident tags    File destinationFile, File fastaFile, IdentificationParameters identificationParameters, File identificationParametersFile, String msFileNameWithoutExtension, int spectraSizeLimit
            Map<String, Spectrum> confidentSpectraSet = getSubSpectraWithConfidentTag(destinationFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE), subfileNameWithoutExtension, subsetSize, 1);

            return confidentSpectraSet;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private ArrayList<SpectrumMatch> getTagMaches(File destinationFile, File fastaFile, IdentificationParameters identificationParameters, File identificationParametersFile, String msFileNameWithoutExtension) {
        try {
            searchInputSetting.setRunDirecTag(true);
            String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + msFileNameWithoutExtension + Configurations.get_current_file_fingerprent();
            File tempResultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, destinationFile, fastaFile, identificationParameters, identificationParametersFile);
            File direcTagFile = new File(tempResultsFolder, IoUtil.removeExtension(destinationFile.getName()) + ".tags");
            if (!direcTagFile.exists()) {
                System.out.println("there is no tags in the file ...very poor data " + msFileNameWithoutExtension);
                return new ArrayList<>();
            }
            searchInputSetting.setRunDirecTag(false);
            IdfileReader idReader = IdfileReaderFactory.getInstance().getFileReader(direcTagFile);
            System.out.println("file name " + msFileNameWithoutExtension + "   " + IoUtil.removeExtension(destinationFile.getName()));
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(destinationFile, MainUtilities.OptProt_Waiting_Handler);
            ArrayList<SpectrumMatch> matches = idReader.getAllSpectrumMatches(subMsFileHandler, MainUtilities.OptProt_Waiting_Handler, identificationParameters.getSearchParameters());
            return matches;
        } catch (IOException | SQLException | ClassNotFoundException | InterruptedException | JAXBException | XmlPullParserException | XMLStreamException ex) {
            Logger.getLogger(OptProtDatasetHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();

    }

    private Map<String, Spectrum> getSubSpectraWithConfidentTag(File destinationFile, File fastaFile, IdentificationParameters identificationParameters, File identificationParametersFile, String msFileNameWithoutExtension, int spectraSizeLimit, double highQualityRatio) {
        Set<ConfidentTagSorter> confidentSpectraSet = new TreeSet<>();
        Map<String, Spectrum> subSpectraMap = new LinkedHashMap<>();
        try {
            ArrayList<SpectrumMatch> matches = getTagMaches(destinationFile, fastaFile, identificationParameters, identificationParametersFile, msFileNameWithoutExtension);
//            System.out.println("Maches sizes 2 --->>"+matches.size());
            if (matches.isEmpty()) {
                System.out.println("there is no tags in the file ...very poor data " + msFileNameWithoutExtension);
                return subSpectraMap;
            }

            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(destinationFile, MainUtilities.OptProt_Waiting_Handler);

            for (SpectrumMatch sm : matches) {
                TagAssumption tag = sm.getAllTagAssumptions().toList().get(0);
                confidentSpectraSet.add(new ConfidentTagSorter(tag.getScore(), sm.getSpectrumTitle(), subMsFileHandler.getSpectrum(msFileNameWithoutExtension, sm.getSpectrumTitle())));
            }
            File cms = new File(destinationFile.getParent(), destinationFile.getName().replace(".mgf", ".cms"));
            cms.delete();
            double highQualitylimit = (double) spectraSizeLimit * highQualityRatio;
            double avgQualityLimit = spectraSizeLimit - highQualitylimit;

            double highQualityCounter = 0;
            double avgQualityCounter = 0;
            double totalCounter = 0;

            for (ConfidentTagSorter tag : confidentSpectraSet) {
                if (tag.getValue() <= 0.01 && highQualityCounter <= highQualitylimit) {
                    highQualityCounter++;
                    totalCounter++;
                    subSpectraMap.put(tag.getTitle(), tag.getSpectrum());
                } else if (tag.getValue() > 0.01 && tag.getValue() <= 0.1 && avgQualityCounter <= avgQualityLimit) {
                    avgQualityCounter++;
                    totalCounter++;
                    subSpectraMap.put(tag.getTitle(), tag.getSpectrum());
                } else if (totalCounter <= spectraSizeLimit) {
                    subSpectraMap.put(tag.getTitle(), tag.getSpectrum());
                    totalCounter++;
                }
            }

            if (subSpectraMap.size() < spectraSizeLimit) {
                String[] titiles = subMsFileHandler.getSpectrumTitles(msFileNameWithoutExtension);
                for (String str : titiles) {
                    if (!subSpectraMap.containsKey(str)) {
                        subSpectraMap.put(str, subMsFileHandler.getSpectrum(msFileNameWithoutExtension, str));
                    }
                    if (subSpectraMap.size() >= spectraSizeLimit) {
                        break;
                    }
                }
            }
            if (subSpectraMap.size() < spectraSizeLimit) {
                String[] titiles = subMsFileHandler.getSpectrumTitles(msFileNameWithoutExtension);
                for (String str : titiles) {
                    if (!subSpectraMap.containsKey(str)) {
                        subSpectraMap.put(str, subMsFileHandler.getSpectrum(msFileNameWithoutExtension, str));
                    }
                    if (subSpectraMap.size() >= spectraSizeLimit) {
                        break;
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return subSpectraMap;
    }

    private File initSubFastaFile(File tempSubFastaFile, File fastaFile, Set<String> sequences) {

        if (tempSubFastaFile.exists()) {
            tempSubFastaFile.delete();
        }
        SpectraUtilities.createSubFastaFile(fastaFile, tempSubFastaFile, sequences);
        return tempSubFastaFile;

    }

}
