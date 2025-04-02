/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.configurations;

import static no.uib.probe.quicksearchprot.util.ConfigurationsUtility.DATA_FOLDER;
import static no.uib.probe.quicksearchprot.util.ConfigurationsUtility.OUTPUT_FOLDER_PATH;

/**
 * Main configurations needed for the sub search utilities
 *
 * @author Yehia Mokhtar Farag
 */
public class Configurations {

    
    /**
     * The configurations folder.
     * @return 
     */
//    public static final String CONFIG_FOLDER = null;
   

    public static String GET_DATA_FOLDER() {
        return  DATA_FOLDER;
    }
    public static final String DEFAULT_RESULT_NAME = "optsearch_results";

    
    
    private static String current_dataset_folder_id="";//"\\";

    public static String GET_OUTPUT_FOLDER_PATH() {
        return OUTPUT_FOLDER_PATH+""+current_dataset_folder_id;
    }
    /**
     * The default search param file.
     */
    public static final String DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE_NAME = "default_optprot_search_settings.par";

    /**
     * The default search param file.
     */
    public static final String DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE = "D:\\Apps\\OptProt\\data\\default_optprot_search_settings.par";
    /**
     * The active search param file.
     */
//    public static File ACTIVE_SEARCH_SETTINGS_FILE;

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

    public static void setDataset_Id(String aDataset_Id) {
        Dataset_Id = aDataset_Id;
    }

    public static String getCurrent_dataset_folder_id() {
        return current_dataset_folder_id;
    }

    public static void SET_ACTIVE_DATASET_FOLDER_id(String Current_dataset_folder_id) {
        current_dataset_folder_id = Current_dataset_folder_id;
    }

}
