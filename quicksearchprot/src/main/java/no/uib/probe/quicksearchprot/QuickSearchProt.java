package no.uib.probe.quicksearchprot;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.util.ConfigurationsUtility;
import no.uib.probe.quicksearchprot.util.MainUtilities;

/**
 * This app is search settings optimization workflow that aim to optimize search
 * settings for different Proteomics search engines
 *
 * @author Yehia Mokhtar Farag
 */
public class QuickSearchProt {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Get the current working directory
                String currentDir = System.getProperty("user.dir");

                File container = new File(currentDir).getParentFile();
                File configurationsFile = new File(container, "configurations.json");
                // Print the directory path
                ConfigurationsUtility configUtil = new ConfigurationsUtility(configurationsFile);
      

                Map<Advocate, List<String>> paramOrderMap = configUtil.getParamOrderMap();

                Set<Advocate> supportedSearchEngine = configUtil.getSupportedSearchEngine();

                Set<String> datasettoTestSet = configUtil.getDatasettoTestSet();

                boolean cleanAll = configUtil.isCleanAll();
                SearchInputSetting searchOpParameter = new SearchInputSetting();
               
                boolean useFullFasta = configUtil.isUseFullDataMode();
                boolean useOreginalInputs = configUtil.isUseFullDataMode();
                System.out.println("configUtil.getSearchOperationParameters() "+configUtil.getSearchOperationParameters());
                boolean all = configUtil.getSearchOperationParameters().get("optimizeAllParameters");
                searchOpParameter.setOptimizeAllParameters(all);
               
                searchOpParameter.setOptimizeDigestionParameter(configUtil.getSearchOperationParameters().get("optimizeDigestionParameter") || all);
                searchOpParameter.setOptimizeCleavageParameter(configUtil.getSearchOperationParameters().get("optimizeCleavageParameter"));
                searchOpParameter.setOptimizeEnzymeParameter(configUtil.getSearchOperationParameters().get("optimizeEnzymeParameter"));
                searchOpParameter.setOptimizeMaxMissCleavagesParameter(configUtil.getSearchOperationParameters().get("optimizeMaxMissCleavagesParameter") || all);
                searchOpParameter.setOptimizeSpecificityParameter(configUtil.getSearchOperationParameters().get("optimizeSpecificityParameter"));
                searchOpParameter.setOptimizeFragmentIonTypesParameter(configUtil.getSearchOperationParameters().get("optimizeFragmentIonTypesParameter") || all);
                searchOpParameter.setOptimizePrecursorToleranceParameter(configUtil.getSearchOperationParameters().get("optimizePrecursorToleranceParameter") || all);
                searchOpParameter.setOptimizeFragmentToleranceParameter(configUtil.getSearchOperationParameters().get("optimizeFragmentToleranceParameter") || all);
                searchOpParameter.setOptimizePrecursorChargeParameter(configUtil.getSearchOperationParameters().get("optimizePrecursorChargeParameter") || all);
                searchOpParameter.setOptimizeIsotopsParameter(configUtil.getSearchOperationParameters().get("optimizeIsotopsParameter") || all);
                searchOpParameter.setOptimizeModificationParameter(configUtil.getSearchOperationParameters().get("optimizeModificationParameter") || all);
                searchOpParameter.setOptimizeSageAdvancedParameter(configUtil.getSearchOperationParameters().get("optimizeSageAdvancedParameter") || all);
                searchOpParameter.setOptimizeXtandemAdvancedParameter(configUtil.getSearchOperationParameters().get("optimizeXtandemAdvancedParameter") || all);
//            searchOpParameter.setRecalibrateSpectraParameter(false);
                boolean sub = !configUtil.isUseFullDataMode();
                boolean full = configUtil.isUseFullDataMode();
////////       
                for (Advocate se : supportedSearchEngine) {
                    MainUtilities.getParamScoreSet().clear();
                    searchOpParameter.setSelectedSearchEngine(se);
                    for (String datasetId : datasettoTestSet) {
                        searchOpParameter.setDatasetId(datasetId);
                        MainUtilities.cleanOutputFolder(datasetId);
                        if (sub) {
                            System.out.println("--------------------------------------------------------- ds " + datasetId + "----------------------------------------------");
                            runDataset(datasetId, cleanAll, paramOrderMap.get(se), searchOpParameter, false, useFullFasta, false);
                            MainUtilities.cleanOutputFolder(datasetId);
                            MainUtilities.getParamScoreSet().clear();
                            System.gc();
                        }
                        if (full) {
                            System.out.println("---------------------------------------------------------full-" + datasetId + "----------------------------------------------");
                            runDataset(datasetId, false, paramOrderMap.get(se), searchOpParameter, true, useFullFasta, useOreginalInputs);
                            MainUtilities.cleanOutputFolder(datasetId);
                            MainUtilities.getParamScoreSet().clear();
                            System.gc();
                        }
                        MainUtilities.getParamScoreSet().clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MainUtilities.cleanOutputFolder("");
                System.exit(0);
            }
        }
        );
    }

    private static void runDataset(String datasetId, boolean cleanAll, List<String> paramOrder, SearchInputSetting searchOpParameter, boolean wholeDataTest, boolean fullFasta, boolean useOreginalInputs) {
        ArrayList<File> msFiles = new ArrayList<>();
        File datasetFolder = new File(Configurations.GET_DATA_FOLDER() + datasetId);//  
        File searchParamFile = null;
        File fastaFile = null; 
        for (File f : datasetFolder.listFiles()) {
           
            if (cleanAll) {
                if (f.isDirectory() && f.getName().equals(searchOpParameter.getSelectedSearchEngine().getName())) {
                    for (File ff : f.listFiles()) {
                        if (ff.getName().startsWith(Configurations.DEFAULT_RESULT_NAME) && !ff.getName().endsWith(".txt")) {
                            System.out.println("to be deleted files " + ff.getAbsolutePath());
                            ff.delete();

                        }
                    }
                }
            }

            if (f.getName().endsWith(".mgf")) {
                msFiles.add(f);
            } else if (f.getName().endsWith(".fasta")) {
                fastaFile = f;
            } else if (f.getName().endsWith(".par")) {
                searchParamFile = f;
            }
        }

        Controller controller = new Controller(searchOpParameter);
        controller.processDataset(msFiles.get(0), fastaFile, searchParamFile, wholeDataTest, fullFasta, paramOrder, useOreginalInputs);
        MainUtilities.cleanOutputFolder(datasetId);

    }
}
