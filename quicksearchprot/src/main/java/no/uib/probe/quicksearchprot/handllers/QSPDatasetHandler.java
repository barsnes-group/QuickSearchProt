package no.uib.probe.quicksearchprot.handllers;

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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.search.SearchExecuter;
import no.uib.probe.quicksearchprot.util.ConfidentTagSorter;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.util.QSProtWaitingHandler;
import no.uib.probe.quicksearchprot.util.SpectraUtilities;
import org.apache.commons.collections15.map.LinkedMap;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Handler class for QuickSearchProt dataset operations, including generating subsets,
 * filtering spectra, and preparing FASTA and MGF files for search engines.
 * 
 * <p>Provides utilities to:
 * <ul>
 *   <li>Count spectra in an MS file</li>
 *   <li>Generate subset MS/FASTA files based on identification results</li>
 *   <li>Run initial searches using different search engines</li>
 *   <li>Filter and extract confident spectra/tags</li>
 * </ul>
 * 
 * @author Yehia Mokhtar Farag
 */
public class QSPDatasetHandler {

    private final SearchInputSetting searchInputSetting;
    private int startIndex = 0;
    private File subFastaFile = null;

    private File subMsFile = null;


    /**
     * Constructs a handler with the given search input settings.
     * @param searchInputSetting user search input settings
     */
    public QSPDatasetHandler(SearchInputSetting searchInputSetting) {
        this.searchInputSetting = searchInputSetting;
    }

    /**
     * @return The search input settings for this handler
     */
    public SearchInputSetting getSearchInputSetting() {
        return searchInputSetting;
    }

    /**
     * Counts the total number of spectra in an MS file.
     * @param msFilePath Path to MS file
     * @return Number of spectra, or -1 if file does not exist
     */
    public static int countTotalSpectra(String msFilePath) {
        File msFile = new File(msFilePath);
        if (!msFile.exists()) {
            return -1;
        }
        final String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
        MsFileHandler msFileHandler = new MsFileHandler();
        try {
            msFileHandler.register(msFile, new QSProtWaitingHandler());
        } catch (IOException ex) {
            MainUtilities.QSProtWaitingHandler.addLogMassage(ex.getMessage());
        }
        return msFileHandler.getSpectrumTitles(fileNameWithoutExtension).length;
    }
/**
     * Generate a QuickSearchProt dataset by producing subset MGF and FASTA files and running an initial search.
     * 
     * @param datasetId dataset identifier
     * @param msFile original MS file
     * @param fastaFile original FASTA file
     * @param searchEngineToOptimise search engine to use
     * @param subDataFolder folder for subset data
     * @param identificationParametersFile search parameters file
     * @param wholeDataTest use full dataset (true) or subset (false)
     * @param useOreginalInputs use original input files without subsetting
     * @param subsetSize size of subset (-1 to auto select)
     * @param fullFasta use full fasta file
     * @return SearchingSubDataset with relevant information for further analysis
     */
    public SearchingSubDataset generateQSProtDataset(String datasetId, File msFile, File fastaFile, Advocate searchEngineToOptimise, File subDataFolder, File identificationParametersFile, boolean wholeDataTest, boolean fullFasta, boolean useOreginalInputs, int subsetSize) {
        double acceptedTagEvalue = Configurations.ACCEPTED_TAG_EVALUE;
        long start1 = System.currentTimeMillis();
        Advocate standeredReferenceSearchEngine = searchEngineToOptimise;
        SearchingSubDataset optProtDataset = new SearchingSubDataset();
        optProtDataset.setSubMsFile(msFile);
        optProtDataset.setSubFastaFile(fastaFile);

        TreeMap<String, File> subFilesMap = new TreeMap<>(Collections.reverseOrder());
        final String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
        MsFileHandler msFileHandler = new MsFileHandler();
        try {
            msFileHandler.register(msFile, new QSProtWaitingHandler());
        } catch (IOException ex) {
            if (subMsFile != null) {
                subMsFile.delete();
            }
            if (subFastaFile != null && !fullFasta) {
                subFastaFile.delete();
            }
            MainUtilities.QSProtWaitingHandler.addLogMassage(ex.getMessage());
        }

        String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);

        optProtDataset.setOreginalDatasetSpectraSize(spectrumTitles.length);

        if (useOreginalInputs) {
            subFastaFile = fastaFile;
            subMsFile = msFile;
        } else {
            // Set up subset file names
            if (!wholeDataTest) {
                subFastaFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint()+ "_" + fastaFile.getName());
                subMsFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_" + msFile.getName());

            } else {
                subFastaFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_Full_" + fastaFile.getName());
                subMsFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_Full_" + msFile.getName());

            }
            if (fullFasta) {
                subFastaFile = fastaFile;
            }
            System.out.println("sub folder " + subDataFolder.getAbsolutePath());
            for (File f : subDataFolder.listFiles()) {
                System.out.println("file name " + f.getAbsolutePath());
                if (f.getName().toLowerCase().endsWith(".par") || f.getName().toLowerCase().endsWith(".mgf") || f.getName().toLowerCase().endsWith(".fasta") || f.getName().toLowerCase().endsWith(".txt")) {
                    subFilesMap.put(f.getName().toLowerCase(), f);
                } else {
                    f.delete();
                }
            }

            boolean update = false;
            try {
                final IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE));
                // Generate subset MGF if needed
                if (!subMsFile.exists()) {
                    update = true;
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
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE));

                    int subSize;
                    List<Double> quartileSizeRatios;
                    quartileSizeRatios = this.getSectionRatios(msFile, fastaFile, identificationParameters);
                    Map<String, Spectrum> spectraMap = generatFilteredData(msFile, msFileHandler, quartileSizeRatios, wholeDataTest, searchEngineToOptimise.getIndex());
                    if (spectraMap.size() > 70000) {
                        Configurations.MIN_SUBSET_SIZE = 2000;
                    }
                    if (!wholeDataTest) {
                        if (subsetSize != -1) {
                            subSize = subsetSize;
                        } else if (searchEngineToOptimise.getIndex() == Advocate.sage.getIndex()) {
                            subSize = (int) Math.min(Configurations.MAX_SUBSET_SIZE, (double) spectraMap.size());
                        } else {
                            subSize = (int) Math.min(Configurations.MIN_SUBSET_SIZE, (double) spectraMap.size());//   
                        }
                        MainUtilities.QSProtWaitingHandler.addMainStepMassage("Selected subset size " + subSize + " spectra");
                        spectraMap = generateSubset(fileNameWithoutExtension, spectraMap, subSize, fastaFile, identificationParameters);
                    }
                    MainUtilities.cleanFolder(datasetId);
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start subset generating process ");
                    //create stabkle subMs file
                    subMsFile = generateMsSubFile(spectraMap, subMsFile);
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("done!");
                    subFastaFile.delete();
                }
                //create stabkle subfasta file
                if (!fullFasta && (!subFastaFile.exists() || update)) {
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start generating filtered Fasta file");
                    subFastaFile.createNewFile();
                    long start3 = System.currentTimeMillis();
                    searchInputSetting.setRunDirecTag(false);
                    searchInputSetting.setRunNovor(true);
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start Novor search");
                    final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + fileNameWithoutExtension + Configurations.getCurrentFileFingerprint();
                    File resultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, subMsFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE));
                    File NovorFile = new File(resultsFolder, IoUtil.removeExtension(subMsFile.getName()) + ".novor.csv");
                    Set<String> sequences = SpectraUtilities.getSequences(NovorFile);
                    long end3rd = System.currentTimeMillis();
                    String total = MainUtilities.msToTime(end3rd - start3) ;
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("done (Sequence number from nover " + sequences.size() + ")" + " time used : " + total);

                    long start4 = System.currentTimeMillis();
                    if (searchEngineToOptimise.getIndex() == Advocate.sage.getIndex() || true) {
                        File newSubFasta = new File(subFastaFile.getParent(), subFastaFile.getName().replace(".fasta", "_subFastaFile.fasta"));
                        newSubFasta = initSubFastaFile(newSubFasta, fastaFile, sequences);
                        FastaParameters fastaParameters = FastaParameters.inferParameters(subFastaFile.getAbsolutePath(), MainUtilities.QSProtWaitingHandler);
                        DecoyConverter.appendDecoySequences(newSubFasta, subFastaFile, fastaParameters, MainUtilities.QSProtWaitingHandler);
                        newSubFasta.delete();
                    } else {
                        subFastaFile = initSubFastaFile(subFastaFile, fastaFile, sequences);
                    }
                    long end4th = System.currentTimeMillis();
                    total = MainUtilities.msToTime(end4th - start4);
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Done generated filterd Fasta file, time used : " + total);

                    long end = System.currentTimeMillis();
                    total = MainUtilities.msToTime(end - start1) ;
                    System.out.println("Done processing the sub-data files, total time used : " + total);
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
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start initial reference search using  " + searchEngineToOptimise);

            // Run initial identification with user-selected SE
            final IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE));
            searchInputSetting.setSelectedSearchEngine(searchEngineToOptimise);
            final String option = "reference_run_default_" + searchEngineToOptimise;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + fileNameWithoutExtension;

            if (standeredReferenceSearchEngine.getIndex() == Advocate.sage.getIndex()) {
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
            subMsFileHandler.register(subMsFile, new QSProtWaitingHandler());
            optProtDataset.setSpectraTitiles(subMsFileHandler.getSpectrumTitles(subfileNameWithoutExtension));
            File resultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, subMsFile, subFastaFile, identificationParameters, new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE));

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
            MainUtilities.cleanFolder(datasetId);
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("done!");

        } catch (IOException ex) {
            if (subMsFile != null) subMsFile.delete();
            if (subFastaFile != null) subFastaFile.delete();
            ex.printStackTrace();
        }
        return optProtDataset;
    }

    /**
     * Helper to select spectra at regular intervals from a range for filtering.
     */
    private Map<String, Spectrum> substractSpectraFirstLevelDataFiltering(
            File msFile, MsFileHandler msFileHandler, int startIndex, double stepSize, int lastIndex) {
        Map<String, Spectrum> spectraMap = new LinkedHashMap<>();
        String msFileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
        String[] spectrumTitles = msFileHandler.getSpectrumTitles(msFileNameWithoutExtension);
        double stepAsInt = (int) stepSize;
        double remainFloatValue = stepSize - stepAsInt;
        double remainFloat = 0;

        for (int i = startIndex; i < lastIndex && i < spectrumTitles.length;) {
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

    /**
     * Write spectra to an MGF file.
     */
    private File generateMsSubFile(Map<String, Spectrum> spectraMap, File destinationFile) {
        try {
            if (destinationFile.exists()) destinationFile.delete();
            destinationFile.createNewFile();
            try (MgfFileWriter writer = new MgfFileWriter(destinationFile)) {
                for (String spectrumTitle : spectraMap.keySet()) {
                    Spectrum spectrum = spectraMap.get(spectrumTitle);
                    writer.writeSpectrum(spectrumTitle, spectrum);
                }
                writer.close();
            }
        } catch (IOException ex) {
            if (subMsFile != null) subMsFile.delete();
            if (subFastaFile != null) subFastaFile.delete();
            ex.printStackTrace();
        }
        return destinationFile;
    }

    /**
     * Get quartile/section ratios for filtering spectra, based on tag matches.
     */
    private List<Double> getSectionRatios(File msFile, File fastaFile, IdentificationParameters identificationParameters) {
        ArrayList<Double> arrayList = new ArrayList<>();
        try {
            final String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
            ArrayList<SpectrumMatch> matches = getTagMaches(msFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE), fileNameWithoutExtension);
            if (matches.isEmpty()) {
                System.out.println("there is no tags in the file ...very poor data " + fileNameWithoutExtension);
                return arrayList;
            }
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(msFile, MainUtilities.QSProtWaitingHandler);
            arrayList.clear();
            arrayList.addAll(SpectraUtilities.getTagSectionRatios(subMsFileHandler.getSpectrumTitles(IoUtil.removeExtension(msFile.getName())), matches));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return arrayList;
    }

    /**
     * Filter and select spectra by quartile ratios.
     */
    private Map<String, Spectrum> generatFilteredData(
            File msFile, MsFileHandler msFileHandler, List<Double> ratios, boolean full, int seIndex) {

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
            initStartIndex = lastIndex + 1;
            lastIndex = coverage * (i + 1);
            int subsetQuartileSize;
            double ratio = ratios.get(i);
            if (ratio <= 0.2 || ratio >= 0.8) {
                ratio = 1.0 - ratio;
            }
            subsetQuartileSize = (int) (ratio * coverage);
            double step = (double) coverage / (double) subsetQuartileSize;
            spectraMap.putAll(substractSpectraFirstLevelDataFiltering(msFile, msFileHandler, initStartIndex, step, lastIndex));
        }
        return spectraMap;
    }

    /**
     * Generate a confident subset of spectra (by tag) for search.
     */
    private Map<String, Spectrum> generateSubset(
            String fileNameWithoutExtension, Map<String, Spectrum> spectraMap, int subsetSize,
            File fastaFile, IdentificationParameters identificationParameters) {
        try {
            File destinationFile = new File(Configurations.WORKING_FOLDER_PATH, Configurations.DEFAULT_RESULT_NAME + "_temp_full_" + spectraMap.size() + "_-_" + fileNameWithoutExtension + ".mgf");
            if (destinationFile.exists()) destinationFile.delete();
            destinationFile.createNewFile();
            destinationFile = generateMsSubFile(spectraMap, destinationFile);
            final String subfileNameWithoutExtension = IoUtil.removeExtension(destinationFile.getName());
            Map<String, Spectrum> confidentSpectraSet = getSubSpectraWithConfidentTag(destinationFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE), subfileNameWithoutExtension, subsetSize, 0.5);
            return confidentSpectraSet;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Run DirecTag and get tag matches for a given MS and FASTA file.
     */
    private ArrayList<SpectrumMatch> getTagMaches(
            File destinationFile, File fastaFile, IdentificationParameters identificationParameters,
            File identificationParametersFile, String msFileNameWithoutExtension) {
        try {
            long start1 = System.currentTimeMillis();
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start DirecTag search");
            searchInputSetting.setRunDirecTag(true);
            String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + msFileNameWithoutExtension + Configurations.getCurrentFileFingerprint();
            File tempResultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, destinationFile, fastaFile, identificationParameters, identificationParametersFile);
            File direcTagFile = new File(tempResultsFolder, IoUtil.removeExtension(destinationFile.getName()) + ".tags");
            MainUtilities.QSProtWaitingHandler.addLogMassage("direct tag file " + tempResultsFolder.getAbsolutePath());
            if (!direcTagFile.exists()) {
                MainUtilities.QSProtWaitingHandler.addLogMassage("there is no tags in the file ...very poor data " + msFileNameWithoutExtension);
                return new ArrayList<>();
            }
            searchInputSetting.setRunDirecTag(false);
            IdfileReader idReader = IdfileReaderFactory.getInstance().getFileReader(direcTagFile);
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(destinationFile, MainUtilities.QSProtWaitingHandler);
            ArrayList<SpectrumMatch> matches = idReader.getAllSpectrumMatches(subMsFileHandler, MainUtilities.QSProtWaitingHandler, identificationParameters.getSearchParameters());
            long end3rd = System.currentTimeMillis();
            String total = MainUtilities.msToTime(end3rd - start1);
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("Done with DirecTag search, total number of tag matches " + matches.size() + " time used : " + total);
            return matches;
        } catch (IOException | SQLException | ClassNotFoundException | InterruptedException | JAXBException | XmlPullParserException | XMLStreamException ex) {
            Logger.getLogger(QSPDatasetHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    /**
     * Extract confident spectra (with good tag matches) for subsetting.
     */
    private Map<String, Spectrum> getSubSpectraWithConfidentTag(
            File destinationFile, File fastaFile, IdentificationParameters identificationParameters,
            File identificationParametersFile, String msFileNameWithoutExtension, int spectraSizeLimit, double highQualityRatio) {

        Set<ConfidentTagSorter> confidentSpectraSet = new TreeSet<>();
        Map<String, Spectrum> subSpectraMap = new LinkedHashMap<>();
        try {
            ArrayList<SpectrumMatch> matches = getTagMaches(destinationFile, fastaFile, identificationParameters, identificationParametersFile, msFileNameWithoutExtension);
            if (matches.isEmpty()) {
                System.out.println("there is no tags in the file ...very poor data " + msFileNameWithoutExtension);
                return subSpectraMap;
            }
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(destinationFile, MainUtilities.QSProtWaitingHandler);

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

    /**
     * Create a subset FASTA file containing only the given sequences.
     */
    private File initSubFastaFile(File tempSubFastaFile, File fastaFile, Set<String> sequences) {
        if (tempSubFastaFile.exists()) tempSubFastaFile.delete();
        SpectraUtilities.createSubFastaFile(fastaFile, tempSubFastaFile, sequences);
        return tempSubFastaFile;
    }
}