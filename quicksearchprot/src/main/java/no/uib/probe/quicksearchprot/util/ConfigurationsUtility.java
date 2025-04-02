package no.uib.probe.quicksearchprot.util;

import com.compomics.util.experiment.identification.Advocate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author yfa041
 */
public class ConfigurationsUtility {

    private Map<Advocate, List<String>> paramOrderMap = new HashMap<>();
    private Set<Advocate> supportedSearchEngine = new LinkedHashSet<>();
    private Set<String> datasettoTestSet = new LinkedHashSet<>();
    private boolean cleanAll = false;
    private Map<String, Boolean> searchOperationParameters = new LinkedHashMap<>();
    private boolean useFullDataMode = false;

    /**
     * The search engine configuration folders.
     */
    public static final String XTANDEM_FOLDER;
    public static final String NOVOR_FOLDER;
    public static final String DIRECTAG_FOLDER;
    public static final String SAGE_FOLDER;

    static {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");

        File container = new File(currentDir).getParentFile();

        XTANDEM_FOLDER = container.getAbsolutePath() + "\\searchengines\\XTandem\\windows\\windows_64bit";
        NOVOR_FOLDER = container.getAbsolutePath() + "\\searchengines\\Novor";
        DIRECTAG_FOLDER = container.getAbsolutePath() + "\\searchengines\\DirecTag\\windows\\windows_64bit";
        SAGE_FOLDER = container.getAbsolutePath() + "\\searchengines\\Sage\\windows\\";
        System.out.println("sage folder " + SAGE_FOLDER);
    }

    /**
     * The resources folder.
     */
    public static String DATA_FOLDER = "D:\\Apps\\OptProt\\data\\";
    /**
     * The output folder.
     */
    public static String OUTPUT_FOLDER_PATH = "D:\\Apps\\OptProt\\data\\output";

    public ConfigurationsUtility(File configurationsFile) {
        File workingfolder = new File(configurationsFile.getParentFile(), "workingfolder");
        workingfolder.mkdir();

        OUTPUT_FOLDER_PATH = workingfolder.getAbsolutePath();
        try (FileReader reader = new FileReader(configurationsFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Accessing elements in the JSON object
            System.out.println("Supported Search Engines: " + jsonObject.get("supportedSearchEngine"));
            Iterator<JsonElement> searchEngines = jsonObject.getAsJsonArray("supportedSearchEngine").iterator();

            while (searchEngines.hasNext()) {
                String searchEngineName = searchEngines.next().getAsString();
                supportedSearchEngine.add(Advocate.getAdvocate(searchEngineName));
            }
            cleanAll = jsonObject.get("cleanAll").getAsBoolean();
            System.out.println("Clean All: " + cleanAll);
            DATA_FOLDER = jsonObject.get("datasetFolderURL").getAsString();
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
            System.out.println("Parameter Order: " + paramOrderMap);
            useFullDataMode = jsonObject.get("useFullDataMode").getAsBoolean();

            Iterator<JsonElement> datasets = jsonObject.getAsJsonArray("datasets").iterator();
            while (datasets.hasNext()) {
                String param = datasets.next().getAsString();
                datasettoTestSet.add(param);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
