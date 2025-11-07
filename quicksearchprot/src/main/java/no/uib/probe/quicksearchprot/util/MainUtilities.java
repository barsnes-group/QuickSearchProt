/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.util;

import com.compomics.util.gui.UtilitiesGUIDefaults;
import com.compomics.util.parameters.UtilitiesUserParameters;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.searchgui.OutputParameters;
import com.compomics.util.parameters.tools.ProcessingParameters;
import eu.isas.searchgui.SearchHandler;
import java.awt.Dimension;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.model.ResultScoreModel;

/**
 *
 * @author yfa041
 */
public class MainUtilities {

    private static ExecutorService displayExecuter;// = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static ExecutorService executor2;// = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static ExecutorService mainExecuter;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() / 2;
    private static ExecutorService executor;// = new ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
//    private static final TreeSet<Double> paramScoreSet = new TreeSet<>();

    static {
        System.out.println(" " + AVAILABLE_PROCESSORS + "  ");
        UtilitiesUserParameters userParameters = UtilitiesUserParameters.loadUserParameters();
        userParameters.setGzip(false);
        userParameters.setSearchGuiOutputParameters(OutputParameters.no_zip);
        userParameters.setRenameXTandemFile(true);
        UtilitiesUserParameters.saveUserParameters(userParameters);
        SearchHandler.setCloseProcessWhenDone(false);
        File resultsOutput = new File(Configurations.WORKING_FOLDER_PATH);
        resultsOutput.mkdir();
    }

//    public static synchronized TreeSet<Double> getParamScoreSet() {
//        return paramScoreSet;
//    }
//
//    public static synchronized void addToParamScoreSet(double score) {
//        paramScoreSet.add(score);
//    }
    private static final ProcessingParameters Processing_Parameters = new ProcessingParameters();
    public static final QSProtWaitingHandler QSProtWaitingHandler = new QSProtWaitingHandler();

    public static ProcessingParameters getProcessingParameter() {
        if (Processing_Parameters == null) {
//            Processing_Parameters.setnThreads(Runtime.getRuntime().availableProcessors());
            // Processing
//            Processing_Parameters.setnThreads(15);

        }
        return Processing_Parameters;
    }

    public static void getDisplayExecuter() {
//        if (displayExecuter != null) {
//            displayExecuter.shutdownNow();
//        }
//        displayExecuter = Executors.newFixedThreadPool(2);
//        return displayExecuter;
     
    }

    public static ExecutorService getMainExecuter() {
        if (mainExecuter != null) {
            mainExecuter.shutdownNow();
        }
        mainExecuter = Executors.newSingleThreadExecutor();
        return mainExecuter;
    }

    public static void resetLongExecutorService() {
        if (executor2 != null) {
            executor2.shutdownNow();
        }
        executor2 = new ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(AVAILABLE_PROCESSORS));
    }

    public static void resetExecutorService() {
        if (executor != null) {
            executor.shutdownNow();
        }
//        executor = Executors.newCachedThreadPool();
        executor = new ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(AVAILABLE_PROCESSORS));
//        executor = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    }

    public static void resetAllExecutorService() {
        displayExecuter.shutdownNow();
        executor.shutdownNow();
        executor2.shutdownNow();

    }
    private static int executorServiceCounter = 0;
    private static int executorServiceCounter2 = 0;

    public static ExecutorService getExecutorService() {
        if (executor == null || executorServiceCounter == 5) {
            executorServiceCounter = 0;
            resetExecutorService();
        }
        executorServiceCounter++;
        return executor;
    }

    public static ExecutorService getLongExecutorService() {
        if (executor2 == null || executorServiceCounter2 == 5) {
            executorServiceCounter2 = 0;
            resetLongExecutorService();
        }
        executorServiceCounter2++;
        return executor2;
    }

    public static void cleanFolder(String folderPath) {
        File outputFolder = new File(folderPath);
        deleteFolder(outputFolder);
        outputFolder.mkdir();
        System.gc();
    }

    public static void deleteFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }

            }

        }
        folder.delete();

    }

    public static int rundDouble(double args) {
        return (int) Math.round(args * 100.0 / 100.0);

    }

    public static void saveIdentificationParameters(IdentificationParameters identificationParameters, File identificationParametersFile) {
        try {
            Future<Boolean> f = MainUtilities.getExecutorService().submit(() -> {
                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                return true;
            });
            boolean scoreModel = f.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }

    public static String msToTime(double ms) {
        // Prompt the user to input the total seconds
        int seconds = (int) Math.round(ms / 1000);
        int S = seconds % 60;  // Calculate the remaining seconds
        int H = seconds / 60;  // Convert total seconds to minutes
        int M = H % 60;         // Calculate the remaining minutes
        H = H / 60;            // Convert total minutes to hours
        String time = (H + ":" + M + ":" + S);
        return time;

    }
    private static Map<String, ResultScoreModel> paramConfidentMap = new LinkedHashMap();

    public static void resetParamMap() {
        paramConfidentMap.clear();
    }

    public static void addToParameterResults(String parameterName, String parameterValue, double targtedScore, TreeSet<Double> scores) {
        double zScore = 1;
        double pvalue = 0.05;
        int percentage = 100;
        if (scores.size() >= 2) {
            zScore = StatisticsTests.calculateOneTailedZScore(targtedScore, scores.stream().mapToDouble(Double::doubleValue).toArray());
            pvalue = StatisticsTests.calculateOneTailedPValue(zScore);
            percentage = (int) StatisticsTests.convertZToPercentile(zScore);
        }
        ResultScoreModel result = new ResultScoreModel();
        result.setParamName(parameterName);
        result.setParamValue(parameterValue);
        result.setzScore(zScore);
        result.setPvalue(pvalue);
        result.setPercentage(percentage);
        if (targtedScore == 0 && scores.size() > 1) {
            result.setDefaultParameterValue(true);
        }
        paramConfidentMap.put(parameterName, result);
//        System.out.println(parameterName + ":   " + parameterValue + "   " + zScore + "   " + pvalue + "  " + percentage+"   targted "+targtedScore+"   "+scores);
//        System.exit(0);

    }

    public static String getConfidentAsString(String paramName) {

        if (paramConfidentMap.containsKey(paramName)) {
            return paramConfidentMap.get(paramName).toString();
        } else {
            return "\t\tPre-Selected";
        }

    }

}
