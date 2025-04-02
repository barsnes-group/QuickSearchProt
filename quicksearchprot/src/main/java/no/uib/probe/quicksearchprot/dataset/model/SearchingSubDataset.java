/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.dataset.model;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import no.uib.probe.quicksearchprot.model.ParameterScoreModel;
import no.uib.probe.quicksearchprot.model.RawScoreModel;

/**
 *
 * @author yfa041
 */
public class SearchingSubDataset {

    private Map<String, TreeSet<ParameterScoreModel>> parameterScoreMap;

    public Map<String, TreeSet<ParameterScoreModel>> getParameterScoreMap() {
        return parameterScoreMap;
    }

    public void setParameterScoreMap(Map<String, TreeSet<ParameterScoreModel>> parameterScoreMap) {
        this.parameterScoreMap = parameterScoreMap;
    }

    public int getIdentifiedPSMsNumber() {
        return identifiedPSMsNumber;
    }

    public void setIdentifiedPSMsNumber(int identifiedPSMsNumber) {
        this.identifiedPSMsNumber = identifiedPSMsNumber;
    }

    public File getSubDataFolder() {
        return subDataFolder;
    }

    public void setSubDataFolder(File subDataFolder) {
        this.subDataFolder = subDataFolder;
    }
    private File subDataFolder;
    private File subMsFile;
    private File subFastaFile;
    private File oreginalFastaFile;
    private boolean fullDataSpectaInput;
    private Set<String> potintialVariableMod;
    private final Map<String, Double> fullSpectraScore = new LinkedHashMap<>();
    private List<SpectrumMatch> vaildatedPsmMaches = new ArrayList<>();

    public List<SpectrumMatch> getVaildatedPsmMaches() {
        return vaildatedPsmMaches;
    }

   
    

    public void setSpectraTitiles(String[] titiles) {
        for (String titile : titiles) {
            fullSpectraScore.put(titile, 0.0);
        }
    }

    public boolean isFullDataSpectaInput() {
        return fullDataSpectaInput;
    }

    public void setFullDataSpectaInput(boolean fullDataSpectaInput) {
        this.fullDataSpectaInput = fullDataSpectaInput;
    }

    public File getOreginalFastaFile() {
        return oreginalFastaFile;
    }

    public void setOreginalFastaFile(File oreginalFastaFile) {
        this.oreginalFastaFile = oreginalFastaFile;
    }
    private File searchSettingsFile;

    public File getSearchSettingsFile() {
        return searchSettingsFile;
    }

    public void setSearchSettingsFile(File searchSettingsFile) {
        this.searchSettingsFile = searchSettingsFile;
    }
    private int defaultSettingIdentificationNum;
    private int subsetSize;
    private int tempIdentificationNum;
    private double processDelay;
    private boolean highResolutionMassSpectrometers = true;
    private RawScoreModel currentScoreModel;

    public void setActiveScoreModel(RawScoreModel scoreModel) {

        this.currentScoreModel = scoreModel;
        this.updateValidatedIdRefrenceData(currentScoreModel.getSpectrumMatchResult());
    }

    public void updateValidatedIdRefrenceData(List<SpectrumMatch> validatedIdRefrenceData) {
        this.resetSpectraScoreMap();
        this.identifiedPSMsNumber = validatedIdRefrenceData.size();
        this.vaildatedPsmMaches=validatedIdRefrenceData;
        for (SpectrumMatch sm : validatedIdRefrenceData) {
            fullSpectraScore.replace(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
            
        }

    }

    private String datasetId;

    public double getProcessDelay() {
        return processDelay;
    }

    public void setProcessDelay(double processDelay) {
        this.processDelay = processDelay;
    }

    public int getTempIdentificationNum() {
        return tempIdentificationNum;
    }

    public void setTempIdentificationNum(int tempIdentificationNum) {
        this.tempIdentificationNum = tempIdentificationNum;
    }

    private int oreginalDatasetSpectraSize;
    private int identifiedPSMsNumber;

//    private final List<Double> comparisonsThresholdList = new ArrayList<>();
//    private double basicComparisonThreshold=0.0;

    public int getOreginalDatasetSpectraSize() {
        return oreginalDatasetSpectraSize;
    }

//    public double getBasicComparisonThreshold() {
//        return basicComparisonThreshold;
//    }

    public void setOreginalDatasetSpectraSize(int oreginalDatasetSpectraSize) {
        this.oreginalDatasetSpectraSize = oreginalDatasetSpectraSize;
    }

    public int getSubsetSize() {
        return subsetSize;
    }

    public void setSubsetSize(int subsetSize) {
        this.subsetSize = subsetSize;
    }

    public synchronized double getIdentificationRate() {
        return identifiedPSMsNumber * 100.0 / subsetSize;
    }

    public int getDefaultSettingIdentificationNum() {
        return defaultSettingIdentificationNum;
    }

    public synchronized int getActiveIdentificationNum() {
        return identifiedPSMsNumber;
    }

    public void setDefaultSettingIdentificationNum(int defaultSettingIdentificationNum) {
        this.defaultSettingIdentificationNum = defaultSettingIdentificationNum;
    }

    public File getSubMsFile() {
        return subMsFile;
    }

    public void setSubMsFile(File subMsFile) {
        this.subMsFile = subMsFile;
    }

    public File getSubFastaFile() {
        return subFastaFile;
    }

    public void setSubFastaFile(File subFastaFile) {
        this.subFastaFile = subFastaFile;
    }

    public boolean isHighResolutionMassSpectrometers() {
        return highResolutionMassSpectrometers;
    }

    public void setHighResolutionMassSpectrometers(boolean highResolutionMassSpectrometers) {
        this.highResolutionMassSpectrometers = highResolutionMassSpectrometers;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

   
    public Set<String> getPotintialVariableMod() {
        return potintialVariableMod;
    }

    public void setPotintialVariableMod(Set<String> potintialVariableMod) {
        this.potintialVariableMod = potintialVariableMod;
    }
//
//    public double getComparisonsThreshold(int level) {
//        level = Math.min(level, 5);
//        return comparisonsThresholdList.get(level);
//    }

//    public List<Double> getComparisonsThresholdList() {
//        return comparisonsThresholdList;
//    }

//    public void setComparisonsThreshold(double comparisonsThreshold0, double comparisonsThreshold1, double comparisonsThreshold2, double comparisonsThreshold3, double comparisonsThreshold4, double max) {
////        if (comparisonsThreshold == 0.0) {
//        comparisonsThresholdList.clear();
//        basicComparisonThreshold= Math.round((0.05*max*100.0))/100.0;
//        this.comparisonsThresholdList.add(comparisonsThreshold0);
//        this.comparisonsThresholdList.add(comparisonsThreshold1);
//        this.comparisonsThresholdList.add(comparisonsThreshold2);
//        this.comparisonsThresholdList.add(comparisonsThreshold3);
//        this.comparisonsThresholdList.add(comparisonsThreshold4);
//        this.comparisonsThresholdList.add(max * 2.0);
////        }
////        System.out.println("comparison score " + comparisonsThreshold);
//
//    }
    public void updateMaxScore(double max){
//        System.out.println("max score "+max+"  "+basicComparisonThreshold);
//    basicComparisonThreshold=Math.max(0.05*max,basicComparisonThreshold);
    }

    public RawScoreModel getCurrentScoreModel() {
        return currentScoreModel;
    }

    public void setCurrentScoreModel(RawScoreModel currentScoreModel) {
        this.currentScoreModel = currentScoreModel;
    }

    private void resetSpectraScoreMap() {
        for (String titile : fullSpectraScore.keySet()) {
            fullSpectraScore.replace(titile, 0.0);
        }

    }

    public Map<String, Double> getFullSpectraScore() {
        return fullSpectraScore;
    }

}
