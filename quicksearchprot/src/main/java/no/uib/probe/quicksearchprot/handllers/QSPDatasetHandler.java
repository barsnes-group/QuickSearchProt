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
import org.apache.poi.ss.formula.atp.Switch;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Handler class for QuickSearchProt dataset operations, including generating
 * subsets, filtering spectra, and preparing FASTA and MGF files for search
 * engines.
 *
 * <p>
 * Provides utilities to:
 * <ul>
 * <li>Count spectra in an MS file</li>
 * <li>Generate subset MS/FASTA files based on identification results</li>
 * <li>Run initial searches using different search engines</li>
 * <li>Filter and extract confident spectra/tags</li>
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
     *
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
     *
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
     * Generate a QuickSearchProt dataset by producing subset MGF and FASTA
     * files and running an initial search.
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
     * @param cleanIfExist
     * @return SearchingSubDataset with relevant information for further
     * analysis
     */
    public SearchingSubDataset generateQSProtDataset(String datasetId, File msFile, File fastaFile, Advocate searchEngineToOptimise, File subDataFolder, File identificationParametersFile, boolean wholeDataTest, boolean fullFasta, boolean useOreginalInputs, int subsetSize) {
        long start1 = System.currentTimeMillis();
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
        //used for analysis only not in the production version
        if (useOreginalInputs) {
            subFastaFile = fastaFile;
            subMsFile = msFile;
        } else {
            // Set up subset file names
            if (!wholeDataTest) {
                subFastaFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_" + fastaFile.getName());
                subMsFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_" + msFile.getName());             
                  
            } else {
                subFastaFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_Full_" + fastaFile.getName());
                subMsFile = new File(subDataFolder, Configurations.DEFAULT_RESULT_NAME + Configurations.getCurrentFileFingerprint() + "_Full_" + msFile.getName());

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

                    //Check subset size 
                    //Case use default recommeneded dataset setsize                    
                    if (subsetSize == -1) {
                        subsetSize = this.calculateRecommendSubsetSize(searchEngineToOptimise, optProtDataset.getOreginalDatasetSpectraSize());
                    }
                    MainUtilities.QSProtWaitingHandler.setCurrentProgressValue(5);
                    //calculate #sections                
                    int sectionNum = this.calculateSectionNums(subsetSize);
                    List<Double> quartileSizeRatios;
                    quartileSizeRatios = this.getSectionRatios(sectionNum, msFile, fastaFile, identificationParameters);
                    Map<String, Spectrum> spectraMap = generatFilteredData(msFile, msFileHandler, quartileSizeRatios);
                    MainUtilities.QSProtWaitingHandler.setCurrentProgressValue(15);
                    if (!wholeDataTest) {
                        MainUtilities.QSProtWaitingHandler.addLogMassage("Selected subset size " + subsetSize + " spectra");
                        MainUtilities.QSProtWaitingHandler.addMainStepMassage("Select spectra subset data");
                        spectraMap = generateSubset(fileNameWithoutExtension, spectraMap, subsetSize, fastaFile, identificationParameters);
                    }
                    MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
                    MainUtilities.QSProtWaitingHandler.setCurrentProgressValue(30);
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Generate subset file (#spectra:" + subsetSize + ")");

                    //create subMs file
                    System.out.println("----------generate subms file-------------------");
                    subMsFile = generateMsSubFile(spectraMap, subMsFile);
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("done!");
                    MainUtilities.QSProtWaitingHandler.setCurrentProgressValue(50);
                    optProtDataset.setSubsetSize(subsetSize);
                    subFastaFile.delete();
                } else {
                    MsFileHandler msFileHandler2 = new MsFileHandler();
                    try {
                        msFileHandler2.register(subMsFile, new QSProtWaitingHandler());
                    } catch (IOException ex) {
                        MainUtilities.QSProtWaitingHandler.addLogMassage(ex.getMessage());
                    }
                    final String fileNameWithoutExtension2 = IoUtil.removeExtension(subMsFile.getName());
                    String[] spectrumTitles2 = msFileHandler.getSpectrumTitles(fileNameWithoutExtension2);
                    optProtDataset.setSubsetSize(spectrumTitles2.length);
                }
                //create stabkle subfasta file
                if (!fullFasta && (!subFastaFile.exists() || update)) {
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start processing filtered-FASTA data");
                    subFastaFile.createNewFile();
                    long start3 = System.currentTimeMillis();
                    searchInputSetting.setRunDirecTag(false);
                    searchInputSetting.setRunNovor(true);
                    MainUtilities.QSProtWaitingHandler.addLogMassage("Start Novor search");
                    final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + fileNameWithoutExtension + Configurations.getCurrentFileFingerprint();
                    File resultsFolder = SearchExecuter.executeSearch(updatedName, searchInputSetting, subMsFile, fastaFile, identificationParameters, new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE));
                    File NovorFile = new File(resultsFolder, IoUtil.removeExtension(subMsFile.getName()) + ".novor.csv");
                    Set<String> sequences = SpectraUtilities.getSequences(NovorFile);
                    long end3rd = System.currentTimeMillis();
                    String total = MainUtilities.msToTime(end3rd - start3);
                    MainUtilities.QSProtWaitingHandler.addLogMassage(" done (Sequence number from nover " + sequences.size() + ")" + " time used : " + total);
                    MainUtilities.QSProtWaitingHandler.setCurrentProgressValue(75);
                    long start4 = System.currentTimeMillis();
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Generate filtered-FASTA file (#sequences from Novor:" + sequences.size() + ")");
                    File newSubFasta = new File(subFastaFile.getParent(), subFastaFile.getName().replace(".fasta", "_subFastaFile.fasta"));
                    newSubFasta = initSubFastaFile(newSubFasta, fastaFile, sequences);
                    FastaParameters fastaParameters = FastaParameters.inferParameters(subFastaFile.getAbsolutePath(), MainUtilities.QSProtWaitingHandler);
                    DecoyConverter.appendDecoySequences(newSubFasta, subFastaFile, fastaParameters, MainUtilities.QSProtWaitingHandler);
                    newSubFasta.delete();

                    long end4th = System.currentTimeMillis();
                    total = MainUtilities.msToTime(end4th - start4);
                    MainUtilities.QSProtWaitingHandler.addLogMassage("---------- Done genertaing subset and filtered input data(time used : " + total + ") ----------");
                    MainUtilities.QSProtWaitingHandler.addMainStepMassage("Done");
                    long end = System.currentTimeMillis();
                    total = MainUtilities.msToTime(end - start1);
                    System.out.println("Done processing the sub-data files, total time used : " + total);
                    MainUtilities.QSProtWaitingHandler.setCurrentProgressValue(100);
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
        return optProtDataset;
    }

    /**
     * calculate subsetSize based on subset size and search engine
     *
     * @param searchEngineToOptimise the user selected search engine to adjust
     * @param fullDatasetSize the size (#spectra) in the original input data-set
     */
    private int calculateRecommendSubsetSize(Advocate searchEngineToOptimise, int fullDatasetSize) {
        //for xtandem se the selected range between 1500 to 2000 spectra
        if (searchEngineToOptimise.getIndex() == Advocate.xtandem.getIndex()) {
            if (fullDatasetSize <= 1500) {
                return fullDatasetSize;
            }
            if (fullDatasetSize >= 100000) {
                return 2000;
            } else {
                return 1500;
                //return SpectraUtilities.scaleValue(fullDatasetSize,1500 , 100000, 1500, 2000);
            }
        } else if (searchEngineToOptimise.getIndex() == Advocate.sage.getIndex()) {
            if (fullDatasetSize <= 3000) {
                return fullDatasetSize;
            } //            if(fullDatasetSize>=100000)
            //                return 3500;
            else {
                return 3000;
                // return SpectraUtilities.scaleValue(fullDatasetSize,1500 , 100000, 3000, 3500);
            }
        }
        return fullDatasetSize;
    }

    /**
     * calculate subsetSize based on subset size and search engine
     *
     * @param subsetsize the size (#spectra) in the subset
     */
    private int calculateSectionNums(int subsetsize) {
        //min section numbers is 4 and max section number is 20  //and max spectra number  per section 500
        double sNum;
        double specPerSecNum = 500;
        double initialSecNumber = (double) subsetsize / specPerSecNum;
        if (initialSecNumber < 4) {
            sNum = 4;
        } else if (initialSecNumber > 20) {
            sNum = 20;
        } else {
            sNum = initialSecNumber;

        }

        return (int) sNum;
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
            System.out.println("error is here " + destinationFile.getAbsolutePath() + "  " + destinationFile.exists());
            ex.printStackTrace();
        }
        return destinationFile;
    }

    /**
     * Get quartile/section ratios for filtering spectra, based on tag matches.
     */
    private List<Double> getSectionRatios(int sectionNum, File msFile, File fastaFile, IdentificationParameters identificationParameters) {
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
            arrayList.addAll(SpectraUtilities.getTagSectionRatios(sectionNum, subMsFileHandler.getSpectrumTitles(IoUtil.removeExtension(msFile.getName())), matches));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return arrayList;
    }

    /**
     * Filter and select spectra by quartile ratios.
     */
    private Map<String, Spectrum> generatFilteredData(
            File msFile, MsFileHandler msFileHandler, List<Double> ratios) {
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
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
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
            MainUtilities.QSProtWaitingHandler.addLogMassage("Start DirecTag search");
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
            MainUtilities.QSProtWaitingHandler.addLogMassage("Done with DirecTag search, total number of tag matches " + matches.size() + " time used : " + total);
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
        if (tempSubFastaFile.exists()) {
            tempSubFastaFile.delete();
        }
        SpectraUtilities.createSubFastaFile(fastaFile, tempSubFastaFile, sequences);
        return tempSubFastaFile;
    }
}
