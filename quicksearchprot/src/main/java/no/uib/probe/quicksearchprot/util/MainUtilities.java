/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.util;

import com.compomics.util.parameters.UtilitiesUserParameters;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.searchgui.OutputParameters;
import com.compomics.util.parameters.tools.ProcessingParameters;
import eu.isas.searchgui.SearchHandler;
import java.io.File;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.uib.probe.quicksearchprot.configurations.Configurations;

/**
 *
 * @author yfa041
 */
public class MainUtilities {

    private static ExecutorService executor2;// = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() / 2;
    private static ExecutorService executor;// = new ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
    private static final TreeSet<Double> paramScoreSet = new TreeSet<>();


    static {
        System.out.println(" " + AVAILABLE_PROCESSORS + "  ");
        UtilitiesUserParameters userParameters = UtilitiesUserParameters.loadUserParameters();
        userParameters.setGzip(false);
        userParameters.setSearchGuiOutputParameters(OutputParameters.no_zip);
        userParameters.setRenameXTandemFile(true);
        UtilitiesUserParameters.saveUserParameters(userParameters);
        SearchHandler.setCloseProcessWhenDone(false);
        File resultsOutput = new File(MainUtilities.GET_WORKING_FOLDER_PATH(""));
//        if (resultsOutput.exists()) {
////            cleanFolder(resultsOutput);
//        }
        resultsOutput.mkdir();
    }

    public static synchronized TreeSet<Double> getParamScoreSet() {
        return paramScoreSet;
    }

    public static synchronized void addToParamScoreSet(double score) {
        paramScoreSet.add(score);
    }
    private static final ProcessingParameters Processing_Parameters = new ProcessingParameters();
    public static final OptProtWaitingHandler OptProt_Waiting_Handler = new OptProtWaitingHandler();

    public static ProcessingParameters getProcessingParameter() {
        if (Processing_Parameters == null) {
//            Processing_Parameters.setnThreads(Runtime.getRuntime().availableProcessors());
            // Processing
//            Processing_Parameters.setnThreads(15);

        }
        return Processing_Parameters;
    }

    public static void resetLongExecutorService() {
        if (executor2 != null) {
            executor2.shutdownNow();
        }
        executor2 = new ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(AVAILABLE_PROCESSORS));
//        executor = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    }

    public static void resetExecutorService() {
        if (executor != null) {
            executor.shutdownNow();
        }
//        executor = Executors.newCachedThreadPool();
        executor = new ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(AVAILABLE_PROCESSORS));
//        executor = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
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

    public static void cleanOutputFolder(String datasetId) {

        File outputFolder = new File(GET_WORKING_FOLDER_PATH(datasetId));
        deleteFolder(outputFolder);
        outputFolder.mkdir();
        System.gc();
    }

    public static String GET_WORKING_FOLDER_PATH(String datasetId) {
        if (datasetId.equalsIgnoreCase("")) {
            return Configurations.GET_OUTPUT_FOLDER_PATH();
        } else {
            File f = new File(Configurations.GET_OUTPUT_FOLDER_PATH(), datasetId);
            if (!f.exists()) {
                f.mkdir();
            }
            return f.getAbsolutePath();
        }

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
        // Calculate the hours, minutes, and seconds
        int S = seconds % 60;  // Calculate the remaining seconds
        int H = seconds / 60;  // Convert total seconds to minutes
        int M = H % 60;         // Calculate the remaining minutes
        H = H / 60;            // Convert total minutes to hours
        // Display the time in the format HH:MM:SS
        String time = (H + ":" + M + ":" + S);
        return time;

    }

}
