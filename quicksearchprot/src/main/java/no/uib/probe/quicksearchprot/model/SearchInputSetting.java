/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

import com.compomics.util.experiment.identification.Advocate;
import java.io.File;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.search.myrimatch.MyriMatchEnabledParameters;
import no.uib.probe.quicksearchprot.search.sage.SageParameterOrderSettings;
import no.uib.probe.quicksearchprot.search.sage.SageEnabledParameters;
import no.uib.probe.quicksearchprot.search.xtandam.XtandemParameterOrderSettings;
import no.uib.probe.quicksearchprot.search.xtandam.XTandemEnabledParameters;
import no.uib.probe.quicksearchprot.util.ConfigurationsUtility;

/**
 *
 * @author yfa041
 */
public class SearchInputSetting {

    private String datasetId;

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    String digestionParameterOpt;

    public String getDigestionParameterOpt() {
        return digestionParameterOpt;
    }

    public void setDigestionParameterOpt(String digestionParameterOpt) {
        this.digestionParameterOpt = digestionParameterOpt;
    }
    private final XTandemEnabledParameters XTandemEnabledParameters = new XTandemEnabledParameters();
    private final MyriMatchEnabledParameters MyriMatchEnabledParameters = new MyriMatchEnabledParameters();
    private final SageEnabledParameters SageEnabledParameters = new SageEnabledParameters();
    private boolean runOmssa = false;
    private boolean runXTandem = false;
    private boolean runMsgf = false;
    private boolean runMsAmanda = false;
    private boolean runMyriMatch = false;
    private boolean runComet = false;
    private boolean runTide = false;
    private boolean runAndromeda = false;
    private boolean runMetaMorpheus = false;
    private boolean runSage = false;
    private boolean runNovor = false;
    private boolean runDirecTag = false;
    private File omssaFolder = null;
    private final File xTandemFolder = new File(ConfigurationsUtility.XTANDEM_FOLDER);
    private File msgfFolder = null;
    private File msAmandaFolder = null;
//    private final File myriMatchFolder = new File(Configurations.MYRIMATCH_FOLDER);
//    private final File cometFolder = new File(ConfigurationsUtility.COMET_FOLDER);
    private File tideFolder = null;
    private File tideIndexLocation = null;
    private File andromedaFolder = null;
    private File metaMorpheusFolder = null;
    private File sageFolder = new File(ConfigurationsUtility.SAGE_FOLDER);
    private final File novorFolder = new File(ConfigurationsUtility.NOVOR_FOLDER);
    private final File direcTagFolder = new File(ConfigurationsUtility.DIRECTAG_FOLDER);

    private File makeblastdbFolder = null;

    //parameter to optimise
    private boolean optimizeDigestionParameter;
//    private boolean optimizeEnzymeParameter;
    private boolean optimizeSpecificityParameter = false;
    private boolean optimizeMaxMissCleavagesParameter = false;
    private boolean optimizeEnzymeParameter = false;

    private boolean optimizeFragmentIonTypesParameter;
    private boolean optimizePrecursorToleranceParameter;
    private boolean optimizeFragmentToleranceParameter;
    private boolean optimizeIsotopsParameter;
    private boolean optimizeModificationParameter;
    private boolean recalibrateSpectraParameter;
    private boolean optimizeXtandemAdvancedParameter;
    private boolean optimizeMyriMatchAdvancedParameter;
    private boolean optimizeSageAdvancedParameter;
    private boolean optimizeCleavageParameter = false;

    public boolean isOptimizeCleavageParameter() {
        return optimizeCleavageParameter;
    }

    public void setOptimizeCleavageParameter(boolean optimizeCleavageParameter) {
        this.optimizeCleavageParameter = optimizeCleavageParameter;
    }

    public boolean isOptimizeSageAdvancedParameter() {
        return optimizeSageAdvancedParameter || optimizeAllParameters;
    }

    public SageParameterOrderSettings getSageOptProtAdvancedSearchParameters() {
        return sageOptProtAdvancedSearchParameters;
    }

    public void setOptimizeSageAdvancedParameter(boolean optimizeSageAdvancedParameter) {
        this.optimizeSageAdvancedParameter = optimizeSageAdvancedParameter;
    }
    private Advocate selectedSearchEngine = Advocate.xtandem;
    private boolean optimizeAllParameters;

    public boolean isOptimizeAllParameters() {
        return optimizeAllParameters;
    }

    public void setOptimizeAllParameters(boolean optimizeAllParameters) {
        this.optimizeAllParameters = optimizeAllParameters;
    }

    public Advocate getSelectedSearchEngine() {
        return selectedSearchEngine;
    }

    public void setSelectedSearchEngine(Advocate searchEngine) {
        this.selectedSearchEngine = searchEngine;
        if (searchEngine == Advocate.xtandem) {
            this.setRunXTandem(true);
        } else if (searchEngine == Advocate.myriMatch) {
            this.setRunMyriMatch(true);
        } else if (searchEngine == Advocate.direcTag) {
            this.setRunDirecTag(true);
        } else if (searchEngine == Advocate.sage) {
            this.setRunSage(true);
        }
    }
    private final SageParameterOrderSettings sageOptProtAdvancedSearchParameters = new SageParameterOrderSettings();
    private final XtandemParameterOrderSettings xtandemOptProtAdvancedSearchParameters = new XtandemParameterOrderSettings();

    public boolean isOptimizeXtandemAdvancedParameter() {
        return optimizeXtandemAdvancedParameter || optimizeAllParameters;
    }

    public boolean isOptimizeMyriMatchAdvancedParameter() {
        return optimizeMyriMatchAdvancedParameter || optimizeAllParameters;
    }

    public void setOptimizeMyriMatchAdvancedParameter(boolean optimizeMyriMatchAdvancedParameter) {
        this.optimizeMyriMatchAdvancedParameter = optimizeMyriMatchAdvancedParameter || optimizeAllParameters;
    }

    public void setOptimizeXtandemAdvancedParameter(boolean optimizeXtandemAdvancedParameter) {
        this.optimizeXtandemAdvancedParameter = optimizeXtandemAdvancedParameter || optimizeAllParameters;
    }

    public boolean isRecalibrateSpectraParameter() {
        return recalibrateSpectraParameter || optimizeAllParameters;
    }

    public void setRecalibrateSpectraParameter(boolean recalibrateSpectraParameter) {
        this.recalibrateSpectraParameter = recalibrateSpectraParameter;
    }

    public XTandemEnabledParameters getXTandemEnabledParameters() {
        return XTandemEnabledParameters;
    }

    public boolean isOptimizeModificationParameter() {
        return optimizeModificationParameter || optimizeAllParameters;
    }

    public void setOptimizeModificationParameter(boolean optimizeModificationParameter) {
        this.optimizeModificationParameter = optimizeModificationParameter;
    }
    private boolean optimizePrecursorChargeParameter;

    public boolean isOptimizeFragmentToleranceParameter() {
        return optimizeFragmentToleranceParameter || optimizeAllParameters;
    }

    public void setOptimizeFragmentToleranceParameter(boolean optimizeFragmentToleranceParameter) {
        this.optimizeFragmentToleranceParameter = optimizeFragmentToleranceParameter;
    }

    public boolean isOptimizePrecursorToleranceParameter() {
        return optimizePrecursorToleranceParameter || optimizeAllParameters;
    }

    public void setOptimizePrecursorToleranceParameter(boolean optimizePrecursorToleranceParameter) {
        this.optimizePrecursorToleranceParameter = optimizePrecursorToleranceParameter;
    }

    public boolean isOptimizeDigestionParameter() {
        return optimizeDigestionParameter || optimizeAllParameters;
    }

    public void setOptimizeDigestionParameter(boolean optimizeDigestionParameter) {
        this.optimizeDigestionParameter = optimizeDigestionParameter;
    }

//    public boolean isOptimizeEnzymeParameter() {
//        return optimizeEnzymeParameter || optimizeAllParameters;
//    }
//
//    public void setOptimizeEnzymeParameter(boolean optimizeEnzymeParameter) {
//        this.optimizeEnzymeParameter = optimizeEnzymeParameter;
//    }
//
//    public boolean isOptimizeSpecificityParameter() {
//        return optimizeSpecificityParameter || optimizeAllParameters;
//    }
//
//    public void setOptimizeSpecificityParameter(boolean optimizeSpecificityParameter) {
//        this.optimizeSpecificityParameter = optimizeSpecificityParameter;
//    }
//
//    public boolean isOptimizeMaxMissCleavagesParameter() {
//        return optimizeMaxMissCleavagesParameter || optimizeAllParameters;
//    }
//
//    public void setOptimizeMaxMissCleavagesParameter(boolean optimizeMaxMissCleavagesParameter) {
//        this.optimizeMaxMissCleavagesParameter = optimizeMaxMissCleavagesParameter || optimizeAllParameters;
//    }
    public boolean isOptimizeFragmentIonTypesParameter() {
        return optimizeFragmentIonTypesParameter || optimizeAllParameters;
    }

    public void setOptimizeFragmentIonTypesParameter(boolean optimizeFragmentIonTypesParameter) {
        this.optimizeFragmentIonTypesParameter = optimizeFragmentIonTypesParameter;
    }

    public boolean isRunOmssa() {
        return runOmssa;
    }

    public void setRunOmssa(boolean runOmssa) {
        resetActiveSearchEngines();
        this.runOmssa = runOmssa;
    }

    public boolean isRunXTandem() {
        return runXTandem;
    }

    public void setRunXTandem(boolean runXTandem) {
        resetActiveSearchEngines();
        this.runXTandem = runXTandem;
    }

    public boolean isRunMsgf() {
        return runMsgf;
    }

    public void setRunMsgf(boolean runMsgf) {
        resetActiveSearchEngines();
        this.runMsgf = runMsgf;
    }

    public boolean isRunMsAmanda() {
        return runMsAmanda;
    }

    public void setRunMsAmanda(boolean runMsAmanda) {
        resetActiveSearchEngines();
        this.runMsAmanda = runMsAmanda;
    }

    public boolean isRunMyriMatch() {
        return runMyriMatch;
    }

    public void setRunMyriMatch(boolean runMyriMatch) {
        resetActiveSearchEngines();
        this.runMyriMatch = runMyriMatch;
    }

    public boolean isRunComet() {
        return runComet;
    }

    public void setRunComet(boolean runComet) {
        resetActiveSearchEngines();
        this.runComet = runComet;
    }

    public boolean isRunTide() {
        return runTide;
    }

    public void setRunTide(boolean runTide) {
        resetActiveSearchEngines();
        this.runTide = runTide;
    }

    public boolean isRunAndromeda() {
        return runAndromeda;
    }

    public void setRunAndromeda(boolean runAndromeda) {
        this.runAndromeda = runAndromeda;
    }

    public boolean isRunMetaMorpheus() {
        return runMetaMorpheus;
    }

    public void setRunMetaMorpheus(boolean runMetaMorpheus) {
        resetActiveSearchEngines();
        this.runMetaMorpheus = runMetaMorpheus;
    }

    public boolean isRunSage() {
        return runSage;
    }

    public void setRunSage(boolean runSage) {
        resetActiveSearchEngines();
        this.runSage = runSage;
    }

    public boolean isRunNovor() {
        return runNovor;
    }

    public void setRunNovor(boolean runNovor) {
        resetActiveSearchEngines();
        this.runNovor = runNovor;
    }

    public boolean isRunDirecTag() {
        return runDirecTag;
    }

    public void setRunDirecTag(boolean runDirecTag) {
        resetActiveSearchEngines();
        this.runDirecTag = runDirecTag;
    }

    public File getOmssaFolder() {
        return omssaFolder;
    }

    public void setOmssaFolder(File omssaFolder) {

        this.omssaFolder = omssaFolder;
    }

    public File getxTandemFolder() {
        return xTandemFolder;
    }

    public File getMsgfFolder() {
        return msgfFolder;
    }

    public void setMsgfFolder(File msgfFolder) {
        this.msgfFolder = msgfFolder;
    }

    public File getMsAmandaFolder() {
        return msAmandaFolder;
    }

    public void setMsAmandaFolder(File msAmandaFolder) {
        this.msAmandaFolder = msAmandaFolder;
    }

    public File getMyriMatchFolder() {
        return new File("");
    }

    public File getCometFolder() {
        return new File("");
    }

    public File getTideFolder() {
        return tideFolder;
    }

    public void setTideFolder(File tideFolder) {
        this.tideFolder = tideFolder;
    }

    public File getTideIndexLocation() {
        return tideIndexLocation;
    }

    public void setTideIndexLocation(File tideIndexLocation) {
        this.tideIndexLocation = tideIndexLocation;
    }

    public File getAndromedaFolder() {
        return andromedaFolder;
    }

    public void setAndromedaFolder(File andromedaFolder) {
        this.andromedaFolder = andromedaFolder;
    }

    public File getMetaMorpheusFolder() {
        return metaMorpheusFolder;
    }

    public void setMetaMorpheusFolder(File metaMorpheusFolder) {
        this.metaMorpheusFolder = metaMorpheusFolder;
    }

    public File getSageFolder() {
        return sageFolder;
    }

    public void setSageFolder(File sageFolder) {
        this.sageFolder = sageFolder;
    }

    public File getNovorFolder() {
        return novorFolder;
    }

    public File getDirecTagFolder() {
        return direcTagFolder;
    }

    public File getMakeblastdbFolder() {
        return makeblastdbFolder;
    }

    public void setMakeblastdbFolder(File makeblastdbFolder) {
        this.makeblastdbFolder = makeblastdbFolder;
    }

    private void resetActiveSearchEngines() {
        runOmssa = false;
        runXTandem = false;
        runMsgf = false;
        runMsAmanda = false;
        runMyriMatch = false;
        runComet = false;
        runTide = false;
        runAndromeda = false;
        runMetaMorpheus = false;
        runSage = false;
        runNovor = false;
        runDirecTag = false;

    }

    public boolean isOptimizePrecursorChargeParameter() {
        return optimizePrecursorChargeParameter || optimizeAllParameters;
    }

    public void setOptimizePrecursorChargeParameter(boolean optimizePrecursorChargeParameter) {
        this.optimizePrecursorChargeParameter = optimizePrecursorChargeParameter || optimizeAllParameters;
    }

    public boolean isOptimizeIsotopsParameter() {
        return optimizeIsotopsParameter || optimizeAllParameters;
    }

    public void setOptimizeIsotopsParameter(boolean optimizeIsotopsParameter) {
        this.optimizeIsotopsParameter = optimizeIsotopsParameter;
    }

    public XtandemParameterOrderSettings getXtandemOptProtAdvancedSearchParameters() {
        return xtandemOptProtAdvancedSearchParameters;
    }

    public MyriMatchEnabledParameters getMyriMatchEnabledParameters() {
        return MyriMatchEnabledParameters;
    }

    public SageEnabledParameters getSageEnabledParameters() {
        return SageEnabledParameters;
    }

    public boolean isOptimizeMaxMissCleavagesParameter() {
        return optimizeMaxMissCleavagesParameter;//||isOptimizeEnzymeParameter();
    }

    public void setOptimizeMaxMissCleavagesParameter(boolean optimizeMaxMissCleavagesParameter) {
        this.optimizeMaxMissCleavagesParameter = optimizeMaxMissCleavagesParameter;
    }

    public boolean isOptimizeEnzymeParameter() {
        return optimizeEnzymeParameter;
    }

    public void setOptimizeEnzymeParameter(boolean optimizeEnzymeParameter) {
        this.optimizeEnzymeParameter = optimizeEnzymeParameter;
    }

    public boolean isOptimizeSpecificityParameter() {
        return optimizeSpecificityParameter;
    }

    public void setOptimizeSpecificityParameter(boolean optimizeSpecificityParameter) {
        this.optimizeSpecificityParameter = optimizeSpecificityParameter;
    }

}
