package no.uib.probe.quicksearchprot.controllers;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import java.io.File;
import java.io.IOException;
import java.util.List;
import no.uib.probe.quicksearchprot.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.search.myrimatch.MyrimatchSearchHandler;
import no.uib.probe.quicksearchprot.search.sage.SageSearchHandler;
import no.uib.probe.quicksearchprot.search.xtandam.XTandemSearchHandler;

/**
 * Controller for managing search parameter optimization and search engine
 * execution.
 * <p>
 * This class handles the selection and configuration of search parameters based
 * on the selected search engine and input settings. It creates search parameter
 * files, optimizes parameters if required, and triggers the search process for
 * the selected engine.
 * </p>
 *
 * @author Yehia Mokhtar Farag
 */
public class SearchController {

    /**
     * Starts the auto-selection and optimization process for search parameters,
     * then triggers the appropriate search engine handler.
     *
     * @param searchingSubDataset the sub-dataset to be searched
     * @param searchInputSetting the search input settings (including search
     * engine and optimization options)
     * @param paramOrder the order in which to optimize/search parameters
     * @return the generated identification parameters file, or {@code null} if
     * an error occurred
     */
    public File startAutoSelectParamProcess(
            SearchingSubDataset searchingSubDataset,
            SearchInputSetting searchInputSetting,
            List<String> paramOrder
    ) {
        try {
            // Load the identification parameters from the provided search settings file
            IdentificationParameters identificationParameters
                    = IdentificationParameters.getIdentificationParameters(searchingSubDataset.getSearchSettingsFile());

            // Prepare the output parameter file path
            File generatedIdentificationParametersFile = new File(
                    searchingSubDataset.getSubDataFolder(), "QSProtSearchParameter.par"
            );

            // If the file exists, delete and recreate it
            if (generatedIdentificationParametersFile.exists()) {
                generatedIdentificationParametersFile.delete();
            }
            generatedIdentificationParametersFile.createNewFile();

            // Save the initial identification parameters
            IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

            int selectedEngineIndex = searchInputSetting.getSelectedSearchEngine().getIndex();

            // XTandem Engine
            if (selectedEngineIndex == Advocate.xtandem.getIndex()) {
                XTandemSearchHandler xtandemHandler = new XTandemSearchHandler(
                        searchingSubDataset, searchInputSetting, generatedIdentificationParametersFile
                );
                xtandemHandler.startProcess(paramOrder);

                // MyriMatch Engine
            } else if (selectedEngineIndex == Advocate.myriMatch.getIndex()) {
                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters()
                        .getAlgorithmSpecificParameters()
                        .get(Advocate.myriMatch.getIndex());

                // Optimize all parameters if requested
                if (searchInputSetting.isOptimizeAllParameters()) {
                    myriMatchParameters.setMaxDynamicMods(4);
                    myriMatchParameters.setNumberOfSpectrumMatches(1);
                }
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

                MyrimatchSearchHandler myrimatchHandler = new MyrimatchSearchHandler(
                        searchingSubDataset, searchInputSetting, generatedIdentificationParametersFile
                );
                myrimatchHandler.startProcess(paramOrder);

                // Sage Engine
            } else if (selectedEngineIndex == Advocate.sage.getIndex()) {
                SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters()
                        .getAlgorithmSpecificParameters()
                        .get(Advocate.sage.getIndex());

                // Optimize all parameters if requested
                if (searchInputSetting.isOptimizeAllParameters()) {
                    sageParameters.setMaxVariableMods(2);
                }
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

                SageSearchHandler sageHandler = new SageSearchHandler(
                        searchingSubDataset, searchInputSetting, generatedIdentificationParametersFile
                );
                sageHandler.startProcess(paramOrder);
            }

            return generatedIdentificationParametersFile;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
