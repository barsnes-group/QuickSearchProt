package no.uib.probe.quicksearchprot.configurations;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the main configuration parameters for QuickSearchProt sub-search utilities.
 * 
 * <p>
 * This class is used to hold static configuration options and parameters
 * that are accessed from various locations in the application.
 * </p>
 * 
 * <h2>Usage</h2>
 * <pre>
 * // Set dataset ID
 * Configurations.setDataset_Id("Dataset_001");
 * // Access default result name
 * String resultName = Configurations.DEFAULT_RESULT_NAME;
 * </pre>
 * 
 * @author Yehia Mokhtar Farag
 */
public class Configurations {

    /** Default name for result files. */
    public static final String DEFAULT_RESULT_NAME = "qsprot_results";

    /** 
     * Mapping between search engine advocates and their parameter order.
     * Used to ensure correct parameter parsing for different engines.
     */
    public static Map<Advocate, List<String>> paramOrderMap = new HashMap<>();

    /** Flag to determine if full data mode is enabled. */
    public static boolean useFullDataMode = false;

    /** Path to the main output folder for data-sets. */
    public static String DATASET_MAIN_OUTPUT_FOLDER_PATH;

    /** Path to the folder containing subset data. */
    public static String SUBSET_DATA_FOLDER;

    /** The default search parameter file name. */
    public static final String DEFAULT_QSPROT_SEARCH_PARAM_FILE = "default_optprot_search_settings.par";

    /** Path to the X!Tandem search engine configuration folder. */
    public static String XTANDEM_FOLDER;

    /** Path to the Novor search engine configuration folder. */
    public static String NOVOR_FOLDER;

    /** Path to the DirecTag search engine configuration folder. */
    public static String DIRECTAG_FOLDER;

    /** Path to the Sage search engine configuration folder. */
    public static String SAGE_FOLDER;

    /** The main configurations file. */
    public static File configurationsFile;

    /** Working folder path used by the pipeline. */
    public static String WORKING_FOLDER_PATH = "";

    /** Internal data-set identifier (set with setDataset_Id). */
    private static String Dataset_Id;

    /** Extraction type for MS data (e.g., "TA" or "WF"). */
    public static final String EXTRACT_MS_TYPE = "TA";

    /** Maximum number of MS spectra to extract. */
    public static final int EXTRACT_MAX_MS_SIZE = 2000;

    /** Minimum allowed subset size for processing. */
    public static int MIN_SUBSET_SIZE = 1500;

    /** Maximum allowed subset size for processing. */
    public static int MAX_SUBSET_SIZE = 3000;

    /** Refined MS data size for further analysis. */
    public static final int REFINED_MS_SIZE = 1000;

    /** Minimum accepted ratio for reference IDs during validation. */
    public static final double ACCEPTED_REFERENCE_ID_RATIO = 0.05;

    /** Maximum accepted e-value for tag validation. */
    public static final double ACCEPTED_TAG_EVALUE = 0.01;

    /**
     * Generates a file fingerprint string based on the current MS extraction type.
     *
     * @return fingerprint string for current configuration
     */
    public static String getCurrentFileFingerprint() {
        return "_" + EXTRACT_MS_TYPE;
    }

    /**
     * Returns the current data-set identifier.
     * 
     * @return the data-set ID
     */
    public static String getDataset_Id() {
        return Dataset_Id;
    }

    /**
     * Sets the current data-set identifier.
     * 
     * @param aDataset_Id the data-set ID to set
     */
    public static void setDataset_Id(String aDataset_Id) {
        Dataset_Id = aDataset_Id;
    }

    /**
     * Returns the parameter order map.
     * 
     * @return the map of Advocate to parameter order list
     */
    public Map<Advocate, List<String>> getParamOrderMap() {
        return paramOrderMap;
    }

    /**
     * Returns whether full data mode is enabled.
     * 
     * @return true if full data mode is enabled; false otherwise
     */
    public boolean isUseFullDataMode() {
        return useFullDataMode;
    }
}