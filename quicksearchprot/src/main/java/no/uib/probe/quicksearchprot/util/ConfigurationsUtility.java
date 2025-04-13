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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.probe.quicksearchprot.QuickSearchProt;

/**
 *
 * @author Yehia Mokhtar Farag
 */
public class ConfigurationsUtility {

    public static Map<Advocate, List<String>> paramOrderMap = new HashMap<>();
    public static Set<Advocate> supportedSearchEngine = new LinkedHashSet<>();
    public static Set<String> datasettoTestSet = new LinkedHashSet<>();
    public static boolean cleanAll = false;
    public static Map<String, Boolean> searchOperationParameters = new LinkedHashMap<>();
    public static boolean useFullDataMode = false;

    public static void initConfig() {

        Path jarPath;
        try {
            jarPath = Paths.get(QuickSearchProt.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            System.out.println(" container " + jarPath + "  ");
            File container = new File(jarPath.toUri()).getParentFile();
            configurationsFile = new File(container, "configurations.json");
          
            XTANDEM_FOLDER = container.getAbsolutePath() + "\\searchengines\\XTandem\\windows\\windows_64bit";
            NOVOR_FOLDER = container.getAbsolutePath() + "\\searchengines\\Novor";
            DIRECTAG_FOLDER = container.getAbsolutePath() + "\\searchengines\\DirecTag\\windows\\windows_64bit";
            SAGE_FOLDER = container.getAbsolutePath() + "\\searchengines\\Sage\\windows\\";

            File workingfolder = new File(configurationsFile.getParentFile(), "workingfolder");
            workingfolder.mkdir();

            OUTPUT_FOLDER_PATH = workingfolder.getAbsolutePath();
            try (FileReader reader = new FileReader(configurationsFile)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                // Accessing elements in the JSON object
                Iterator<JsonElement> searchEngines = jsonObject.getAsJsonArray("supportedSearchEngine").iterator();

                while (searchEngines.hasNext()) {
                    String searchEngineName = searchEngines.next().getAsString();
                    supportedSearchEngine.add(Advocate.getAdvocate(searchEngineName));
                }
                cleanAll = jsonObject.get("cleanAll").getAsBoolean();
                System.out.println("Clean All: " + cleanAll);
                DATA_FOLDER = jsonObject.get("datasetFolderURL").getAsString();
                boolean testdataoption = false;
                if (DATA_FOLDER.equalsIgnoreCase("PATH\\TO\\DATA\\FOLDER\\")) {
                    DATA_FOLDER = container.getAbsolutePath() + "\\testdata\\";
                    testdataoption = true;
                }

                searchOperationParameters = new Gson().fromJson(jsonObject.getAsJsonObject("searchOpParameter"), new TypeToken<Map<String, Boolean>>() {
                }.getType());
                for (Advocate se : supportedSearchEngine) {
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

                Iterator<JsonElement> datasets = jsonObject.getAsJsonArray("datasets").iterator();
                while (datasets.hasNext()) {
                    String param = datasets.next().getAsString();
                    datasettoTestSet.add(param);
                    if (testdataoption) {
                        break;
                    }
                }

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

    /**
     * The resources folder.
     */
    public static String DATA_FOLDER = "D:\\Apps\\OptProt\\data\\";
    /**
     * The output folder.
     */
    public static String OUTPUT_FOLDER_PATH = "D:\\Apps\\OptProt\\data\\output";

//    public ConfigurationsUtility(File configurationsFile) {
//        File workingfolder = new File(configurationsFile.getParentFile(), "workingfolder");
//        workingfolder.mkdir();
//
//        OUTPUT_FOLDER_PATH = workingfolder.getAbsolutePath();
//        try (FileReader reader = new FileReader(configurationsFile)) {
//            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
//
//            // Accessing elements in the JSON object
//            System.out.println("Supported Search Engines: " + jsonObject.get("supportedSearchEngine"));
//            Iterator<JsonElement> searchEngines = jsonObject.getAsJsonArray("supportedSearchEngine").iterator();
//
//            while (searchEngines.hasNext()) {
//                String searchEngineName = searchEngines.next().getAsString();
//                supportedSearchEngine.add(Advocate.getAdvocate(searchEngineName));
//            }
//            cleanAll = jsonObject.get("cleanAll").getAsBoolean();
//            System.out.println("Clean All: " + cleanAll);
//            DATA_FOLDER = jsonObject.get("datasetFolderURL").getAsString();
//            System.out.println("data folder "+DATA_FOLDER);
//            System.exit(0);
//            searchOperationParameters = new Gson().fromJson(jsonObject.getAsJsonObject("searchOpParameter"), new TypeToken<Map<String, Boolean>>() {
//            }.getType());
//            System.out.println("DATA_FOLDER " + DATA_FOLDER);
//            for (Advocate se : supportedSearchEngine) {
//                String paramName = se.getName() + "ParamOrder";
//                Iterator<JsonElement> parameters = jsonObject.getAsJsonArray(paramName).iterator();
//                List<String> paramOrderList = new ArrayList<>();
//                while (parameters.hasNext()) {
//                    String param = parameters.next().getAsString();
//                    paramOrderList.add(param);
//                }
//                paramOrderMap.put(se, paramOrderList);
//
//            }
//            System.out.println("Parameter Order: " + paramOrderMap);
//            useFullDataMode = jsonObject.get("useFullDataMode").getAsBoolean();
//
//            Iterator<JsonElement> datasets = jsonObject.getAsJsonArray("datasets").iterator();
//            while (datasets.hasNext()) {
//                String param = datasets.next().getAsString();
//                datasettoTestSet.add(param);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public Map<Advocate, List<String>> getParamOrderMap() {
        return paramOrderMap;
    }

    public Set<Advocate> getSupportedSearchEngine() {
        return supportedSearchEngine;
    }

    public Set<String> getDatasettoTestSet() {
        return datasettoTestSet;
    }

    public boolean isCleanAll() {
        return cleanAll;
    }

    public Map<String, Boolean> getSearchOperationParameters() {
        return searchOperationParameters;
    }

    public boolean isUseFullDataMode() {
        return useFullDataMode;
    }

}
