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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import no.uib.probe.quicksearchprot.QuickSearchProtApp;
import no.uib.probe.quicksearchprot.model.QSProtInputsEntity;

/**
 *
 * @author Yehia Mokhtar Farag
 */
public class ConfigurationsUtility {

    public static Map<Advocate, List<String>> paramOrderMap = new HashMap<>();
    public static Map<String, Boolean> searchOperationParameters = new LinkedHashMap<>();
    public static boolean useFullDataMode = false;
    public static String DATASET_MAIN_OUTPUT_FOLDER_PATH;
    public static String SUBSET_DATA_FOLDER;
      /**
     * The default search param file.
     */
    public static final String DEFAULT_QSPROT_SEARCH_PARAM_FILE = "default_optprot_search_settings.par";

    public static void initConfig(QSProtInputsEntity projectEntity) {
        Path jarPath;
        try {
            jarPath = Paths.get(QuickSearchProtApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            File container = new File(jarPath.toUri()).getParentFile();
            configurationsFile = new File(container, "configurations.json");
            XTANDEM_FOLDER = container.getAbsolutePath() + "\\searchengines\\XTandem\\windows\\windows_64bit";
            NOVOR_FOLDER = container.getAbsolutePath() + "\\searchengines\\Novor";
            DIRECTAG_FOLDER = container.getAbsolutePath() + "\\searchengines\\DirecTag\\windows\\windows_64bit";
            SAGE_FOLDER = container.getAbsolutePath() + "\\searchengines\\Sage\\windows\\";

            //create output folder structure
           File datasetMainOutputFolder = new File(projectEntity.getOutputFolderPath(), projectEntity.getDatasetId());
            datasetMainOutputFolder.mkdir();
          File subDatafolder = new File(datasetMainOutputFolder, "subsetFiles");
            subDatafolder.mkdir();
            File workingfolder = new File(datasetMainOutputFolder, "workingfolder");
            workingfolder.mkdir();
            DATASET_MAIN_OUTPUT_FOLDER_PATH=datasetMainOutputFolder.getAbsolutePath();
            SUBSET_DATA_FOLDER=subDatafolder.getAbsolutePath();
            WORKING_FOLDER_PATH = workingfolder.getAbsolutePath();
            try (FileReader reader = new FileReader(configurationsFile)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                searchOperationParameters = new Gson().fromJson(jsonObject.getAsJsonObject("searchOpParameter"), new TypeToken<Map<String, Boolean>>() {
                }.getType());
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
                    paramOrderMap.put(se, paramOrderList);

                }
                useFullDataMode = jsonObject.get("useFullDataMode").getAsBoolean();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * The search engine configuration folders.
     */
    public static String XTANDEM_FOLDER;
    public static String NOVOR_FOLDER;
    public static String DIRECTAG_FOLDER;
    public static String SAGE_FOLDER;
    public static File configurationsFile;

    static {

    }

    public static String WORKING_FOLDER_PATH = "";

    public Map<Advocate, List<String>> getParamOrderMap() {
        return paramOrderMap;
    }

    public Map<String, Boolean> getSearchOperationParameters() {
        return searchOperationParameters;
    }

    public boolean isUseFullDataMode() {
        return useFullDataMode;
    }

}
