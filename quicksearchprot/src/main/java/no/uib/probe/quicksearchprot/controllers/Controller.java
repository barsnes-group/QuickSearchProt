package no.uib.probe.quicksearchprot.controllers;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import java.util.List;
import java.util.Map;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.QSPDatasetHandler;
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.search.SearchController;
import no.uib.probe.quicksearchprot.util.ConfigurationsUtility;
import no.uib.probe.quicksearchprot.util.ReportExporter;

/**
 *
 * @author yfa041
 */
public class Controller {

    private QSPDatasetHandler optProtDatasetHandler;

    public Controller() {

    }
    private Map<Advocate, List<String>> paramOrderMap;
    private SearchInputSetting searchInputSetting;
    private QSProtInputsEntity projectEntity;

    public void initializedController(QSProtInputsEntity projectEntity) {
        ConfigurationsUtility.initConfig(projectEntity);
        if (projectEntity.getSearchParameterFilePath() == null) {
            projectEntity.setSearchParameterFilePath(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE);
        }
        this.projectEntity = projectEntity;
        paramOrderMap = Configurations.paramOrderMap;
        searchInputSetting = new SearchInputSetting();
        boolean all = Configurations.searchOperationParameters.get("optimizeAllParameters");//configUtil.getSearchOperationParameters().get("optimizeAllParameters");
        searchInputSetting.setOptimizeAllParameters(all);
        searchInputSetting.setOptimizeDigestionParameter(Configurations.searchOperationParameters.get("optimizeDigestionParameter") || all);
        searchInputSetting.setOptimizeCleavageParameter(Configurations.searchOperationParameters.get("optimizeCleavageParameter"));
        searchInputSetting.setOptimizeEnzymeParameter(Configurations.searchOperationParameters.get("optimizeEnzymeParameter"));
        searchInputSetting.setOptimizeMaxMissCleavagesParameter(Configurations.searchOperationParameters.get("optimizeMaxMissCleavagesParameter") || all);
        searchInputSetting.setOptimizeSpecificityParameter(Configurations.searchOperationParameters.get("optimizeSpecificityParameter"));
        searchInputSetting.setOptimizeFragmentIonTypesParameter(Configurations.searchOperationParameters.get("optimizeFragmentIonTypesParameter") || all);
        searchInputSetting.setOptimizePrecursorToleranceParameter(Configurations.searchOperationParameters.get("optimizePrecursorToleranceParameter") || all);
        searchInputSetting.setOptimizeFragmentToleranceParameter(Configurations.searchOperationParameters.get("optimizeFragmentToleranceParameter") || all);
        searchInputSetting.setOptimizePrecursorChargeParameter(Configurations.searchOperationParameters.get("optimizePrecursorChargeParameter") || all);
        searchInputSetting.setOptimizeIsotopsParameter(Configurations.searchOperationParameters.get("optimizeIsotopsParameter") || all);
        searchInputSetting.setOptimizeModificationParameter(Configurations.searchOperationParameters.get("optimizeModificationParameter") || all);
        searchInputSetting.setOptimizeSageAdvancedParameter(Configurations.searchOperationParameters.get("optimizeSageAdvancedParameter") || all);
        searchInputSetting.setOptimizeXtandemAdvancedParameter(Configurations.searchOperationParameters.get("optimizeXtandemAdvancedParameter") || all);

    }

    public void startDataProcessing() {
        long start = System.currentTimeMillis();
        try {

            for (String seName : projectEntity.getSearchEngineList()) {
                Advocate se = Advocate.xtandem;
                if (seName.equalsIgnoreCase("Sage")) {
                    se = Advocate.sage;
                }
                long startSE = System.currentTimeMillis();
                searchInputSetting.setSelectedSearchEngine(se);
                searchInputSetting.setDatasetId(projectEntity.getDatasetId());
                MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
                this.optProtDatasetHandler = new QSPDatasetHandler(searchInputSetting);
                MainUtilities.QSProtWaitingHandler.addMainStepMassage("****** Start the process for " + se.getName() + " search engine ******");
                processDataset(projectEntity, paramOrderMap.get(se), false, Configurations.useFullDataMode, Configurations.useFullDataMode);
                MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
                long endSE = System.currentTimeMillis();
                String totalSETime = MainUtilities.msToTime(endSE - startSE);
                MainUtilities.QSProtWaitingHandler.addMainStepMassage("Total time for process data with " + se.getName() + " search engine  : " + totalSETime);
                MainUtilities.QSProtWaitingHandler.addMainStepMassage("*******done *******");
                System.gc();
            }

        } catch (Exception e) {
            MainUtilities.QSProtWaitingHandler.addLogMassage(e.getMessage());
        } finally {
            MainUtilities.QSProtWaitingHandler.endProgress();
            long end = System.currentTimeMillis();
            String totalTime = MainUtilities.msToTime(end - start);
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("Total elapsed time for process all the data : " + totalTime);
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("Done!");
            
        }
    }

    private void processDataset(QSProtInputsEntity projectEntity, List<String> paramOrder, boolean wholeDataTest, boolean fullFasta, boolean useOreginalInputs) {
        File msFile = new File(projectEntity.getInputSpectrumFilePath());
        File searchParamFile = new File(projectEntity.getSearchParameterFilePath());
        File fastaFile = new File(projectEntity.getInputFastaFilePath());
        File subDataFolder = new File(Configurations.SUBSET_DATA_FOLDER, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine().getName());
        if (!subDataFolder.exists()) {
            subDataFolder.mkdir();
        }
        MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
        long startDsInit = System.currentTimeMillis();
        MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start preparing sub-dataset files");
        SearchingSubDataset optProtDataset = optProtDatasetHandler.generateQSProtDataset(optProtDatasetHandler.getSearchInputSetting().getDatasetId(), msFile, fastaFile, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(), subDataFolder, searchParamFile, wholeDataTest, fullFasta, useOreginalInputs,projectEntity.getSubSetSize());
        long endDsInit = System.currentTimeMillis();
        String totalDsTime = MainUtilities.msToTime(endDsInit - startDsInit);
        MainUtilities.QSProtWaitingHandler.addMainStepMassage("done preparing sub-dataset files (" + totalDsTime + ")");
        optProtDataset.setSubDataFolder(subDataFolder);
        optProtDataset.setFullDataSpectaInput(wholeDataTest);
        File selectedSearchSettingsFile;
        if (projectEntity.isAdjustAllSearchParameters()) {
            selectedSearchSettingsFile = new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE);
        } else {
            selectedSearchSettingsFile = searchParamFile;
        }
        optProtDataset.setSearchSettingsFile(selectedSearchSettingsFile);
        MainUtilities.cleanFolder(optProtDatasetHandler.getSearchInputSetting().getDatasetId());
        SearchController optProtSearchHandler = new SearchController();
        long start = System.currentTimeMillis();      
        MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start adjusting parameters process");        
        File generatedFile = optProtSearchHandler.startAutoSelectParamProcess(optProtDataset, optProtDatasetHandler.getSearchInputSetting(), paramOrder);
        long end = System.currentTimeMillis();
        String totalTime = MainUtilities.msToTime(end - start);
        MainUtilities.QSProtWaitingHandler.addMainStepMassage("done adjusting process (" + totalDsTime + ")");
        if (generatedFile != null) {
            ReportExporter.exportFullReport(generatedFile, optProtDataset, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(), optProtDatasetHandler.getSearchInputSetting().getDatasetId(), totalTime, totalDsTime, optProtDataset.getParameterScoreMap());
            ReportExporter.printFullReport(generatedFile, optProtDataset, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(), optProtDatasetHandler.getSearchInputSetting().getDatasetId());
        }
        MainUtilities.QSProtWaitingHandler.addLogMassage("Total Elapsed Time for generating the sub-set : " + totalDsTime);
       MainUtilities.QSProtWaitingHandler.addLogMassage("Total Elapsed Time for optimizing the data  : " + totalTime);

    }

}
