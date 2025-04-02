package no.uib.probe.quicksearchprot.search;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import java.io.File;
import java.io.IOException;
import java.util.List;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.search.myrimatch.MyrimatchSearchHandler;
import no.uib.probe.quicksearchprot.search.sage.SageSearchHandler;
import no.uib.probe.quicksearchprot.search.xtandam.XTandemSearchHandler;

/**
 *
 * @author yfa041
 */
public class SearchController {

    public File startAutoSelectParamProcess(SearchingSubDataset searchingSubDataset, SearchInputSetting searchInputSetting, List<String> paramOrder) {
        try {
            final File generatedIdentificationParametersFile;
            IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(searchingSubDataset.getSearchSettingsFile());
            if (searchInputSetting.isOptimizeAllParameters()) {
                generatedIdentificationParametersFile = new File(searchingSubDataset.getSubDataFolder(), Configurations.DEFAULT_RESULT_NAME + "_" + Configurations.DEFAULT_OPTPROT_SEARCH_SETTINGS_FILE_NAME.replace(".par", "_optAll.par"));
            } else {
                generatedIdentificationParametersFile = new File(searchingSubDataset.getSubDataFolder(), searchingSubDataset.getSearchSettingsFile().getName());
            }
            if (generatedIdentificationParametersFile.exists()) {
                generatedIdentificationParametersFile.delete();
            }
            generatedIdentificationParametersFile.createNewFile();
            IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
            if (searchInputSetting.getSelectedSearchEngine().getIndex() == Advocate.xtandem.getIndex()) {
                XTandemSearchHandler xTandemOptProtSearchOptimizer = new XTandemSearchHandler(searchingSubDataset, searchInputSetting, generatedIdentificationParametersFile);
                xTandemOptProtSearchOptimizer.startProcess(paramOrder);
            } else if (searchInputSetting.getSelectedSearchEngine().getIndex() == Advocate.myriMatch.getIndex()) {
                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
               if(searchInputSetting.isOptimizeAllParameters()){
                myriMatchParameters.setMaxDynamicMods(4);
                myriMatchParameters.setNumberOfSpectrumMatches(1);
               }
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
                MyrimatchSearchHandler myrimatchOptProtSearchOptimizer = new MyrimatchSearchHandler(searchingSubDataset, searchInputSetting, generatedIdentificationParametersFile);
                myrimatchOptProtSearchOptimizer.startProcess(paramOrder);
            } else if (searchInputSetting.getSelectedSearchEngine().getIndex() == Advocate.sage.getIndex()) {
                SageParameters myriMatchParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
               if(searchInputSetting.isOptimizeAllParameters()){
                myriMatchParameters.setMaxVariableMods(2);
               }
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
                SageSearchHandler sageOptProtSearchOptimizer = new SageSearchHandler(searchingSubDataset, searchInputSetting, generatedIdentificationParametersFile);
                sageOptProtSearchOptimizer.startProcess(paramOrder);
                
            }
            return generatedIdentificationParametersFile;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;

    }
}
