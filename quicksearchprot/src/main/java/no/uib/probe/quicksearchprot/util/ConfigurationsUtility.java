package no.uib.probe.quicksearchprot.util;

import com.compomics.util.experiment.identification.Advocate;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.uib.probe.quicksearchprot.QuickSearchProtApp;
import no.uib.probe.quicksearchprot.configurations.Configurations;

import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;

/**
 *
 * @author Yehia Mokhtar Farag
 */
public class ConfigurationsUtility {

    public static void initConfig(QSProtInputsEntity projectEntity) {
        Path jarPath;
        try {
            jarPath = Paths.get(QuickSearchProtApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File container = new File(jarPath.toUri()).getParentFile();
            Configurations.configurationsFile = new File(container, "configurations.json");
            Configurations.XTANDEM_FOLDER = container.getAbsolutePath() + "\\searchengines\\XTandem\\windows\\windows_64bit";
            Configurations.NOVOR_FOLDER = container.getAbsolutePath() + "\\searchengines\\Novor";
            Configurations.DIRECTAG_FOLDER = container.getAbsolutePath() + "\\searchengines\\DirecTag\\windows\\windows_64bit";
            Configurations.SAGE_FOLDER = container.getAbsolutePath() + "\\searchengines\\Sage\\windows\\";

            //create output folder structure
            File datasetMainOutputFolder = new File(projectEntity.getOutputFolderPath(), projectEntity.getDatasetId());
            datasetMainOutputFolder.mkdir();
            
       
            
            
            File subDatafolder = new File(datasetMainOutputFolder, "subsetFiles");
            subDatafolder.mkdir();
            File workingfolder = new File(datasetMainOutputFolder, "workingfolder");
            
            
            
            workingfolder.mkdir();
            Configurations.DATASET_MAIN_OUTPUT_FOLDER_PATH = datasetMainOutputFolder.getAbsolutePath();
            Configurations.SUBSET_DATA_FOLDER = subDatafolder.getAbsolutePath();
            Configurations.WORKING_FOLDER_PATH = workingfolder.getAbsolutePath();
            try (FileReader reader = new FileReader(Configurations.configurationsFile)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();               
                for (String seName : projectEntity.getSearchEngineList()) {
                    Advocate se = Advocate.xtandem;
                    if (seName.equalsIgnoreCase("Sage")) {
                        se = Advocate.sage;
                    }
                    String paramName = se.getName() + "ParamOrder";
                    Iterator<JsonElement> parameters = jsonObject.getAsJsonArray(paramName).iterator();
                    List<String> paramOrderList = new ArrayList<>();
                    while (parameters.hasNext()) {
                        String param = parameters.next().getAsString();
                        paramOrderList.add(param);
                    }
                    Configurations.paramOrderMap.put(se, paramOrderList);

                }
                Configurations.useFullDataMode = jsonObject.get("useFullDataMode").getAsBoolean();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }

    }

}
