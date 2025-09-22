

package no.uib.probe.quicksearchprot.configurations;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main configurations needed for the sub search utilities
 *
 * @author Yehia Mokhtar Farag
 */
public class Configurations {

    public static final String DEFAULT_RESULT_NAME = "qsprot_results"; 
     public static Map<Advocate, List<String>> paramOrderMap = new HashMap<>();
//    public static Map<String, Boolean> searchOperationParameters = new LinkedHashMap<>();
    public static boolean useFullDataMode = false;
    public static String DATASET_MAIN_OUTPUT_FOLDER_PATH;
    public static String SUBSET_DATA_FOLDER;
      /**
     * The default search param file.
     */
    public static final String DEFAULT_QSPROT_SEARCH_PARAM_FILE = "default_optprot_search_settings.par";
/**
     * The search engine configuration folders.
     */
    public static String XTANDEM_FOLDER;
    public static String NOVOR_FOLDER;
    public static String DIRECTAG_FOLDER;
    public static String SAGE_FOLDER;
    public static File configurationsFile;

    public static String WORKING_FOLDER_PATH = "";

    
    private static String Dataset_Id;

    public static final String EXTRACT_MS_TYPE = "TA";//TA  WF
    public static final int EXTRACT_MAX_MS_SIZE = 2000;//3000
    public static int MIN_SUBSET_SIZE = 1500; //1500
    
    public static int MAX_SUBSET_SIZE = 3000;
    public static final int REFINED_MS_SIZE = 1000;//3000

    public static final double ACCEPTED_REFERENCE_ID_RATIO = 0.05;
    public static final double ACCEPTED_TAG_EVALUE = 0.01;
//    public static final double[] VALIDATED_ID_REF_DATA = null;

    public static String get_current_file_fingerprent() {
        return "_" + EXTRACT_MS_TYPE;
    }

    public static String getDataset_Id() {
        return Dataset_Id;
    }


    public Map<Advocate, List<String>> getParamOrderMap() {
        return paramOrderMap;
    }


    public boolean isUseFullDataMode() {
        return useFullDataMode;
    }
    public static void setDataset_Id(String aDataset_Id) {
        Dataset_Id = aDataset_Id;
    }

    
}
