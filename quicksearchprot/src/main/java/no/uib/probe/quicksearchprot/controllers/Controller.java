package no.uib.probe.quicksearchprot.controllers;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import java.util.List;
import java.util.Map;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.handllers.QSPDatasetHandler;
import no.uib.probe.quicksearchprot.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.util.ConfigurationsUtility;
import no.uib.probe.quicksearchprot.util.ReportExporter;

/**
 * Main controller class for orchestrating the QuickSearchProt pipeline. Handles
 * initialization, data processing, and parameter optimization.
 *
 * @author Yehia Mokhtar Farag
 */
public class Controller {

    /**
     * Handler for managing dataset operations.
     */
    private QSPDatasetHandler optProtDatasetHandler;

    /**
     * Map specifying the order of parameters for each search engine.
     */
    private Map<Advocate, List<String>> paramOrderMap;

    /**
     * Search input settings for the current project.
     */
    private SearchInputSetting searchInputSetting;

    /**
     * Entity holding all project input details.
     */
    private QSProtInputsEntity projectEntity;

    /**
     * Default constructor.
     */
    public Controller() {
        // No initialization required here, see initializedController.
    }

    /**
     * Initializes the controller with the provided project entity. Sets up
     * configuration, search input settings, and parameter adjustment options.
     *
     * @param projectEntity The project input entity containing configurations
     * and data paths.
     */
    public void initializedController(QSProtInputsEntity projectEntity) {
        ConfigurationsUtility.initConfig(projectEntity);

        if (projectEntity.getSearchParameterFilePath() == null) {
            projectEntity.setSearchParameterFilePath(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE);
        }

        this.projectEntity = projectEntity;
        this.paramOrderMap = Configurations.paramOrderMap;

        // Initialize search input setting
        this.searchInputSetting = new SearchInputSetting();
        boolean adjustAll = projectEntity.isAdjustAllSearchParameters();

        searchInputSetting.setOptimizeAllParameters(adjustAll);
        searchInputSetting.setOptimizeDigestionParameter(projectEntity.getParamsToAdjust().isDigestion() || adjustAll);
        searchInputSetting.setOptimizeCleavageParameter(projectEntity.getParamsToAdjust().isDigestion());
        searchInputSetting.setOptimizeEnzymeParameter(projectEntity.getParamsToAdjust().isEnzyme());
        searchInputSetting.setOptimizeMaxMissCleavagesParameter(projectEntity.getParamsToAdjust().isMaxMissCleavages() || adjustAll);
        searchInputSetting.setOptimizeSpecificityParameter(projectEntity.getParamsToAdjust().isSpecificity());
        searchInputSetting.setOptimizeFragmentIonTypesParameter(projectEntity.getParamsToAdjust().isFragmentIonTypes() || adjustAll);
        searchInputSetting.setOptimizePrecursorToleranceParameter(projectEntity.getParamsToAdjust().isPrecursorTolerance() || adjustAll);
        searchInputSetting.setOptimizeFragmentToleranceParameter(projectEntity.getParamsToAdjust().isFragmentTolerance() || adjustAll);
        searchInputSetting.setOptimizePrecursorChargeParameter(projectEntity.getParamsToAdjust().isPrecursorCharge() || adjustAll);
        searchInputSetting.setOptimizeIsotopsParameter(projectEntity.getParamsToAdjust().isIsotops() || adjustAll);
        searchInputSetting.setOptimizeModificationParameter(projectEntity.getParamsToAdjust().isModifications() || adjustAll);
        searchInputSetting.setOptimizeSageAdvancedParameter(projectEntity.getParamsToAdjust().isSageAdvanced() || adjustAll);
        searchInputSetting.setOptimizeXtandemAdvancedParameter(projectEntity.getParamsToAdjust().isXtandemAdvanced() || adjustAll);
    }

    /**
     * Starts the data processing workflow, iterating over each selected search
     * engine. Handles cleaning folders, initializing datasets, and reporting.
     */
    public void startDataProcessing() {
        long start = System.currentTimeMillis();
        try {
            for (String seName : projectEntity.getSearchEngineList()) {
                System.out.println("search engine list " + projectEntity.getSearchEngineList());
                Advocate searchEngine = Advocate.xtandem;
                if ("Sage".equalsIgnoreCase(seName)) {
                    searchEngine = Advocate.sage;
                }

                long startSE = System.currentTimeMillis();
                searchInputSetting.setSelectedSearchEngine(searchEngine);
                searchInputSetting.setDatasetId(projectEntity.getDatasetId());

                MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);

                this.optProtDatasetHandler = new QSPDatasetHandler(searchInputSetting);
                MainUtilities.QSProtWaitingHandler.addMainStepMassage(
                        "****** Start the process for " + searchEngine.getName() + " search engine ******");

                processDataset(
                        projectEntity,
                        paramOrderMap.get(searchEngine),
                        false,
                        Configurations.useFullDataMode,
                        Configurations.useFullDataMode
                );

                MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);

                long endSE = System.currentTimeMillis();
                String totalSETime = MainUtilities.msToTime(endSE - startSE);

                MainUtilities.QSProtWaitingHandler.addMainStepMassage(
                        "Total time for process data with " + searchEngine.getName() + " search engine  : " + totalSETime);
                MainUtilities.QSProtWaitingHandler.addMainStepMassage("*******done *******");
                System.gc();
            }
        } catch (Exception e) {
            MainUtilities.QSProtWaitingHandler.addLogMassage(e.getMessage());
        } finally {
            MainUtilities.QSProtWaitingHandler.endProgress();
            long end = System.currentTimeMillis();
            String totalTime = MainUtilities.msToTime(end - start);
            MainUtilities.QSProtWaitingHandler.addMainStepMassage(
                    "Total elapsed time for process all the data : " + totalTime);
            MainUtilities.QSProtWaitingHandler.addMainStepMassage("Done!");
        }
    }

    /**
     * Processes a dataset for a specific search engine, performing sub-dataset
     * generation and parameter optimization.
     *
     * @param projectEntity Project input entity with all paths and
     * configurations.
     * @param paramOrder Ordered list of parameters for optimization.
     * @param wholeDataTest Flag to indicate if the whole data set should be
     * used.
     * @param fullFasta Flag to indicate if the full FASTA database is used.
     * @param useOriginalInputs Flag to indicate if original inputs should be
     * used.
     */
    private void processDataset(
            QSProtInputsEntity projectEntity,
            List<String> paramOrder,
            boolean wholeDataTest,
            boolean fullFasta,
            boolean useOriginalInputs
    ) {
        File msFile = new File(projectEntity.getInputSpectrumFilePath());
        File searchParamFile = new File(projectEntity.getSearchParameterFilePath());
        File fastaFile = new File(projectEntity.getInputFastaFilePath());
        
         System.out.println("Path to Configurations.SUBSET_DATA_FOLDER "+Configurations.SUBSET_DATA_FOLDER);
        
        File subDataFolder = new File(
                Configurations.SUBSET_DATA_FOLDER,
                optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine().getName()
        );

        if (!subDataFolder.exists()) {
            subDataFolder.mkdir();
        }
        MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);

        long startDsInit = System.currentTimeMillis();
        MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start preparing sub-dataset files");

        // Generate the sub-dataset for optimization
        SearchingSubDataset optProtDataset = optProtDatasetHandler.generateQSProtDataset(
                optProtDatasetHandler.getSearchInputSetting().getDatasetId(),
                msFile, fastaFile, optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(),
                subDataFolder,
                searchParamFile,
                wholeDataTest,
                fullFasta,
                useOriginalInputs,
                projectEntity.getSubSetSize()
        );

        long endDsInit = System.currentTimeMillis();
        String totalDsTime = MainUtilities.msToTime(endDsInit - startDsInit);

        MainUtilities.QSProtWaitingHandler.addMainStepMassage(
                "done preparing sub-dataset files (" + totalDsTime + ")");

        optProtDataset.setSubDataFolder(subDataFolder);
        optProtDataset.setFullDataSpectaInput(wholeDataTest);

        // Set the search settings file
        File selectedSearchSettingsFile = projectEntity.isAdjustAllSearchParameters()
                ? new File(Configurations.DEFAULT_QSPROT_SEARCH_PARAM_FILE)
                : searchParamFile;
        optProtDataset.setSearchSettingsFile(selectedSearchSettingsFile);

        MainUtilities.cleanFolder(optProtDatasetHandler.getSearchInputSetting().getDatasetId());

        // Start the parameter optimization process
        SearchController optProtSearchHandler = new SearchController();
        long start = System.currentTimeMillis();
        MainUtilities.QSProtWaitingHandler.addMainStepMassage("Start adjusting parameters process");
        File generatedFile = optProtSearchHandler.startAutoSelectParamProcess(
                optProtDataset,
                optProtDatasetHandler.getSearchInputSetting(),
                paramOrder
        );
        long end = System.currentTimeMillis();
        String totalTime = MainUtilities.msToTime(end - start);

        MainUtilities.QSProtWaitingHandler.addMainStepMassage(
                "done adjusting process (" + totalDsTime + ")");

        if (generatedFile != null) {

            ReportExporter.exportFullReport(
                    generatedFile,
                    optProtDataset,
                    optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(),
                    optProtDatasetHandler.getSearchInputSetting().getDatasetId(), totalTime, totalDsTime
            );
            ReportExporter.printFullReport(
                    generatedFile,
                    optProtDataset,
                    optProtDatasetHandler.getSearchInputSetting().getSelectedSearchEngine(),
                    optProtDatasetHandler.getSearchInputSetting().getDatasetId()
            );
        }

        MainUtilities.QSProtWaitingHandler.addLogMassage(
                "Total Elapsed Time for generating the sub-set : " + totalDsTime);
        MainUtilities.QSProtWaitingHandler.addLogMassage(
                "Total Elapsed Time for optimizing the data  : " + totalTime);
    }
}
