package no.uib.probe.quicksearchprot.controllers;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.OptProtDatasetHandler;
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

    private OptProtDatasetHandler optProtDatasetHandler;

    public Controller() {

    }
    private Map<Advocate, List<String>> paramOrderMap;
    private SearchInputSetting searchInputSetting;
    private QSProtInputsEntity projectEntity;

    public void initializedController(QSProtInputsEntity projectEntity) {
        ConfigurationsUtility.initConfig(projectEntity);
        if (projectEntity.getSearchParameterFilePath() == null) {
            projectEntity.setSearchParameterFilePath(ConfigurationsUtility.DEFAULT_QSPROT_SEARCH_PARAM_FILE);
        }
        this.projectEntity = projectEntity;
        paramOrderMap = ConfigurationsUtility.paramOrderMap;
        searchInputSetting = new SearchInputSetting();
        boolean all = ConfigurationsUtility.searchOperationParameters.get("optimizeAllParameters");//configUtil.getSearchOperationParameters().get("optimizeAllParameters");
        searchInputSetting.setOptimizeAllParameters(all);
        searchInputSetting.setOptimizeDigestionParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeDigestionParameter") || all);
        searchInputSetting.setOptimizeCleavageParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeCleavageParameter"));
        searchInputSetting.setOptimizeEnzymeParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeEnzymeParameter"));
        searchInputSetting.setOptimizeMaxMissCleavagesParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeMaxMissCleavagesParameter") || all);
        searchInputSetting.setOptimizeSpecificityParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeSpecificityParameter"));
        searchInputSetting.setOptimizeFragmentIonTypesParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeFragmentIonTypesParameter") || all);
        searchInputSetting.setOptimizePrecursorToleranceParameter(ConfigurationsUtility.searchOperationParameters.get("optimizePrecursorToleranceParameter") || all);
        searchInputSetting.setOptimizeFragmentToleranceParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeFragmentToleranceParameter") || all);
        searchInputSetting.setOptimizePrecursorChargeParameter(ConfigurationsUtility.searchOperationParameters.get("optimizePrecursorChargeParameter") || all);
        searchInputSetting.setOptimizeIsotopsParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeIsotopsParameter") || all);
        searchInputSetting.setOptimizeModificationParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeModificationParameter") || all);
        searchInputSetting.setOptimizeSageAdvancedParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeSageAdvancedParameter") || all);
        searchInputSetting.setOptimizeXtandemAdvancedParameter(ConfigurationsUtility.searchOperationParameters.get("optimizeXtandemAdvancedParameter") || all);

    }

    public void startDataProcessing() {
        try {

            for (String seName : projectEntity.getSearchEngineList()) {
                Advocate se = Advocate.xtandem;
                if (seName.equalsIgnoreCase("Sage")) {
                    se = Advocate.sage;
                }
                searchInputSetting.setSelectedSearchEngine(se);
                searchInputSetting.setDatasetId(projectEntity.getDatasetId());
                MainUtilities.cleanFolder(ConfigurationsUtility.WORKING_FOLDER_PATH);
                this.optProtDatasetHandler = new OptProtDatasetHandler(searchInputSetting);
                processDataset(projectEntity, paramOrderMap.get(se), false, ConfigurationsUtility.useFullDataMode, ConfigurationsUtility.useFullDataMode);
                MainUtilities.cleanFolder(ConfigurationsUtility.WORKING_FOLDER_PATH);
                System.gc();
            }

        } catch (Exception e) {
            MainUtilities.QSProtWaitingHandler.addLogMassage(e.getMessage());
        } finally {
            MainUtilities.QSProtWaitingHandler.endProgress();
        }
    }

    private void processDataset(QSProtInputsEntity projectEntity, List<String> paramOrder, boolean wholeDataTest, boolean fullFasta, boolean useOreginalInputs) {
        File msFile = new File(projectEntity.getInputSpectrumFilePath());
        File searchParamFile = new File(projectEntity.getSearchParameterFilePath());
        File fastaFile = new File(projectEntity.getInputFastaFilePath());
        File subDataFolder = new File(ConfigurationsUtility.SUBSET_DATA_FOLDER, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine().getName());
        if (!subDataFolder.exists()) {
            subDataFolder.mkdir();
        }
        MainUtilities.cleanFolder(ConfigurationsUtility.WORKING_FOLDER_PATH);
        long startDsInit = System.currentTimeMillis();
        MainUtilities.QSProtWaitingHandler.addLogMassage("Start generating sub-dataset files");
        SearchingSubDataset optProtDataset = optProtDatasetHandler.generateOptProtDataset(optProtDatasetHandler.getSearchInputSetting().getDatasetId(), msFile, fastaFile, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(), subDataFolder, searchParamFile, wholeDataTest, fullFasta, useOreginalInputs);
        long endDsInit = System.currentTimeMillis();
        String totalDsTime = MainUtilities.msToTime(endDsInit - startDsInit);
        MainUtilities.QSProtWaitingHandler.addLogMassage("done generating sub-dataset files (" + totalDsTime + " seconds)");
        optProtDataset.setSubDataFolder(subDataFolder);
        optProtDataset.setFullDataSpectaInput(wholeDataTest);
        File selectedSearchSettingsFile;
        if (projectEntity.isAdjustAllSearchParameters()) {
            selectedSearchSettingsFile = new File(Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE);
        } else {
            selectedSearchSettingsFile = searchParamFile;
        }
        optProtDataset.setSearchSettingsFile(selectedSearchSettingsFile);
        MainUtilities.cleanFolder(optProtDatasetHandler.getSearchInputSetting().getDatasetId());
        SearchController optProtSearchHandler = new SearchController();
        long start = System.currentTimeMillis();
        MainUtilities.QSProtWaitingHandler.addLogMassage("Start adjusting process");
        File generatedFile = optProtSearchHandler.startAutoSelectParamProcess(optProtDataset, optProtDatasetHandler.getSearchInputSetting(), paramOrder);
        long end = System.currentTimeMillis();
        String totalTime = MainUtilities.msToTime(end - start);
        MainUtilities.QSProtWaitingHandler.addLogMassage("done adjusting process (" + totalDsTime + " secounds)");
        if (generatedFile != null) {
            ReportExporter.exportFullReport(generatedFile, optProtDataset, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(), optProtDatasetHandler.getSearchInputSetting().getDatasetId(), totalTime, totalDsTime, optProtDataset.getParameterScoreMap());
            ReportExporter.printFullReport(generatedFile, optProtDataset, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(), optProtDatasetHandler.getSearchInputSetting().getDatasetId());
        }
        System.out.println("Total Elapsed Time for Init dataset : " + totalDsTime);
        System.out.println("Total Elapsed Time for optimizing the data in : " + totalTime);

    }

   

}
