/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yehia
 */
public class QSProtInputsEntity {

    private String searchParameterFilePath;
    private String inputSpectrumFilePath;
    private String inputFastaFilePath;
    private String outputFolderPath;
    private String datasetId;

    private boolean reGenerateSubset;
    private boolean adjustAllSearchParameters;

    private int subSetSize=-1;

    private final List<String> searchEngineList = new ArrayList<>();
    private SelectedSearchParametersEntity paramsToAdjust;

    public String getSearchParameterFilePath() {
        return searchParameterFilePath;
    }

    public void setSearchParameterFilePath(String searchParameterFilePath) {
        this.searchParameterFilePath = searchParameterFilePath;
    }

    public String getInputSpectrumFilePath() {
        return inputSpectrumFilePath;
    }

    public void setInputSpectrumFilePath(String inputSpectrumFilePath) {
        this.inputSpectrumFilePath = inputSpectrumFilePath;
    }

    public String getInputFastaFilePath() {
        return inputFastaFilePath;
    }

    public void setInputFastaFilePath(String inputFastaFilePath) {
        this.inputFastaFilePath = inputFastaFilePath;
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }

    public void setOutputFolderPath(String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
    }

    public boolean isReGenerateSubset() {
        return reGenerateSubset;
    }

    public void setReGenerateSubset(boolean reGenerateSubset) {
        this.reGenerateSubset = reGenerateSubset;
    }

    public boolean isAdjustAllSearchParameters() {
        return adjustAllSearchParameters;
    }

    public void setAdjustAllSearchParameters(boolean adjustAllSearchParameters) {
        this.adjustAllSearchParameters = adjustAllSearchParameters;
    }

    public int getSubSetSize() {
        return subSetSize;
    }

    public void setSubSetSize(int subSetSize) {
        this.subSetSize = subSetSize;
    }

    public List<String> getSearchEngineList() {
        return searchEngineList;
    }

    public void addSearchEngine(String searchEngine) {
        this.searchEngineList.add(searchEngine);
    }

    public void removeSearchEngine(String searchEngine) {
        this.searchEngineList.remove(searchEngine);
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public SelectedSearchParametersEntity getParamsToAdjust() {
        return paramsToAdjust;
    }

    public void setParamsToAdjust(SelectedSearchParametersEntity paramsToAdjust) {
        this.paramsToAdjust = paramsToAdjust;
    }
}
