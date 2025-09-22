/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.util;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import com.compomics.util.parameters.identification.tool_specific.XtandemParameters;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.ParameterScoreModel;

/**
 *
 * @author yfa041
 */
public class ReportExporter {
    
    private static final String[] ions = new String[]{"a", "b", "c","x", "y", "z"};
        

    public static void addElementToReport(String datasetId, String paramId, String paramOption, double idRate, double timeInSecond) {
        MainUtilities.QSProtWaitingHandler.addLogMassage("Report --->  datasetId: " + datasetId + "\tparamId:" + paramId + "\tparamOption:" + paramOption + "\tid_rate:" + idRate + "%\ttime:" + timeInSecond);

    }

    public static void printFullReport(File optimisedSearchParameterFile, SearchingSubDataset dataset, Advocate searchEngine, String datasetId) {

        IdentificationParameters optimisedSearchParameter;
        try {
            optimisedSearchParameter = IdentificationParameters.getIdentificationParameters(optimisedSearchParameterFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
       MainUtilities.QSProtWaitingHandler.addOutputMassage("-------------------------------" + datasetId + "(" + searchEngine.getName() + ")-----------------------------------------");
        if (dataset != null) {
       MainUtilities.QSProtWaitingHandler.addOutputMassage("Spectra size:\t\t" + dataset.getSubsetSize());
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Digestion:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name());
        if (optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equals("enzyme")) {

            MainUtilities.QSProtWaitingHandler.addOutputMassage("Enzyme:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Specificity:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getSpecificity(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Max Missed Cleavages:\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getnMissedCleavages(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()));
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragment Ion Types:\t" + ions[optimisedSearchParameter.getSearchParameters().getForwardIons().get(0)] + "-" + ions[optimisedSearchParameter.getSearchParameters().getRewindIons().get(0)]);
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Precursor Accuracy:\t" + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracyType().name());
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragment Accuracy:\t" + optimisedSearchParameter.getSearchParameters().getFragmentIonAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getFragmentAccuracyType().name());
        MainUtilities.QSProtWaitingHandler.addOutputMassage("PrecursorCharge:\t" + optimisedSearchParameter.getSearchParameters().getMinChargeSearched() + " - " + optimisedSearchParameter.getSearchParameters().getMaxChargeSearched());
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Isotops:\tv\t" + optimisedSearchParameter.getSearchParameters().getMinIsotopicCorrection() + " - " + optimisedSearchParameter.getSearchParameters().getMaxIsotopicCorrection());
//            MainUtilities.QSProtWaitingHandler.addOutputMassage("default Variable mod:\t" + optimisedSearchParameter.getSearchParameters() + "  Factor " + referenceFactor);
        String fm = "";
        if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications() != null) {
            for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications()) {
                fm += ("\t\t" +fixedMod + "\n");
            }
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Fixed Modifications:\n" + fm);
        fm = "";
        if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications() != null) {
            for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications()) {
                fm += ("\t\t" + v + "\n");
            }
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Variable Modifications:\n" + fm);
        if (searchEngine.getIndex() == Advocate.xtandem.getIndex()) {
            XtandemParameters xtandemParameters = (XtandemParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("---------------------------xtandem advanced-----------------------------");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Spectrum Dynamic Range:\t" + xtandemParameters.getDynamicRange());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Number of Peaks:\t" + xtandemParameters.getnPeaks());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("MinimumFragmentMz:\t" + xtandemParameters.getMinFragmentMz());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Minimum Peaks:\t" + xtandemParameters.getMinPeaksPerSpectrum());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("NoiseSuppression:\t" + xtandemParameters.isUseNoiseSuppression() + "  (" + xtandemParameters.getMinPrecursorMass() + ")");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Parent isotop exp:\t" + xtandemParameters.getParentMonoisotopicMassIsotopeError());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("QuickAcetyl:\t\t" + xtandemParameters.isProteinQuickAcetyl());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("QuickPyrolidone:\t" + xtandemParameters.isQuickPyrolidone());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("stP Bias:\t\t" + xtandemParameters.isStpBias());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Refinement:\t\t" + xtandemParameters.isRefine());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("UnanticipatedCleavage:\t" + xtandemParameters.isRefineUnanticipatedCleavages());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("SimiEnzymaticCleavage:\t" + xtandemParameters.isRefineSemi());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Potintial Modification:\t" + xtandemParameters.isPotentialModificationsForFullRefinment());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("PointMutations:\t" + xtandemParameters.isRefinePointMutations());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("SnAPs:\t\t" + xtandemParameters.isRefineSnaps());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Spectrum Synthesis:\t" + xtandemParameters.isRefineSpectrumSynthesis());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("------------------------------------------------------------------------");
            String rfm = "";
            if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications() != null) {
                for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications()) {
                    rfm += ("\t\t"+fixedMod + "\n");
                }
            }
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Refined Fixed Modification:\n" + rfm);
            rfm = "";
            if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications() != null) {
                for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications()) {
                    rfm += ("\t\t"+v + "\n");
                }
            }
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Refined Variable Modification:\n" + rfm);
        } else if (searchEngine.getIndex() == Advocate.myriMatch.getIndex()) {
            MyriMatchParameters myriMatchParameters = (MyriMatchParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("---------------------------MyriMatch advanced-----------------------------");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Peptide Length (min-max):\t" + myriMatchParameters.getMinPeptideLength() + "-" + myriMatchParameters.getMaxPeptideLength());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Precursor Mass (min-max):\t" + myriMatchParameters.getMinPrecursorMass() + "-" + myriMatchParameters.getMaxPrecursorMass());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Max Variable PTM        :\t" + myriMatchParameters.getMaxDynamicMods());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragmentaion Methods    :\t" + myriMatchParameters.getFragmentationRule());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Enzymatic Terminals     :\t" + myriMatchParameters.getMinTerminiCleavages());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("smart + 3 model     :\t" + myriMatchParameters.getUseSmartPlusThreeModel());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Compute xCorr           :\t" + myriMatchParameters.getComputeXCorr());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("TIC Cutoff  %           :\t" + myriMatchParameters.getTicCutoffPercentage());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Num Of Inten Classes    :\t" + myriMatchParameters.getNumIntensityClasses());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Class Size Multiplier   :\t" + myriMatchParameters.getClassSizeMultiplier());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Number Of Batches       :\t" + myriMatchParameters.getNumberOfBatches());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Max Peak Count          :\t" + myriMatchParameters.getMaxPeakCount());
        } else if (searchEngine.getIndex() == Advocate.sage.getIndex()) {
            SageParameters sageParameters = (SageParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("---------------------------Sage advanced-----------------------------");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Peptide Length (min-max):\t" + sageParameters.getMinPeptideLength() + "-" + sageParameters.getMaxPeptideLength());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragment MZ    (min-max):\t" + sageParameters.getMinFragmentMz() + "-" + sageParameters.getMaxFragmentMz());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Peptide Mass:\t\t" + sageParameters.getMinPeptideMass() + "-" + sageParameters.getMaxPeptideMass());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Min Ion Index:\t\t" + sageParameters.getMinIonIndex());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Max Variable Modification:\t" + sageParameters.getMaxVariableMods());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Generate Decoy:\t" + sageParameters.getGenerateDecoys());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Deisotope:\t\t" + sageParameters.getDeisotope());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Chimeric Spectra:\t" + sageParameters.getChimera());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Wide Window:\t\t" + sageParameters.getWideWindow());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Predect RT:\t\t" + sageParameters.getPredictRt());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Number of Peaks:\t" + sageParameters.getMinPeaks() + "-" + sageParameters.getMaxPeaks());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Min Mached Peaks:\t" + sageParameters.getMinMatchedPeaks());
            MainUtilities.QSProtWaitingHandler.addOutputMassage(("Max Fragment Charge:\t" + sageParameters.getMaxFragmentCharge()).replace("null",""));
        }

    }

    public static void exportFullReport(File optimisedSearchParameterFile, SearchingSubDataset dataset, Advocate searchEngine, String datasetId, String timeInMin, String initDsTime) {
        if (dataset == null) {
            MainUtilities.QSProtWaitingHandler.addLogMassage("can not export data (dataset " + datasetId+" not exist)");
            return;
        }
        String pathToRemoteResults =Configurations.SUBSET_DATA_FOLDER;//dataset.getSubDataFolder()
        File reportFile = new File(pathToRemoteResults,"QSProt_results_"+datasetId+"_"+searchEngine.getName() + "_.txt");
        IdentificationParameters optimisedSearchParameter;
        try {
            optimisedSearchParameter = IdentificationParameters.getIdentificationParameters(optimisedSearchParameterFile);
            if (!reportFile.exists()) {
                reportFile.createNewFile();
            } else {
                MainUtilities.QSProtWaitingHandler.addLogMassage("file exist and will re-write");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            try (FileWriter myWriter = new FileWriter(reportFile)) {
                myWriter.write(
                        "-------------------------------" + datasetId + "(" + searchEngine.getName() + ")-----------------------------------------\n");
                myWriter.write(
                        "Used Time  to generate subset :\t" + initDsTime + "  Minutes\n");
                myWriter.write(
                        "Processing Time:\t\t" + timeInMin + "  Minutes\n");
                myWriter.write(
                        "Subset size:\t\t\t" + dataset.getSubsetSize()+ "\n");
                myWriter.write(
                        "Digestion:\t\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name() + "\n");
                if (optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equals("enzyme")) {
                    myWriter.write(
                            "Enzyme:\t\t\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName() + "\n");
                    myWriter.write(
                            "Specificity:\t\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getSpecificity(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()) + "\n");
                    myWriter.write(
                            "Max Missed Cleavages:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getnMissedCleavages(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()) + "\n");
                }
                myWriter.write(
                        "Fragment Ion Types:\t\t" + ions[optimisedSearchParameter.getSearchParameters().getForwardIons().get(0)] + "-" + ions[optimisedSearchParameter.getSearchParameters().getRewindIons().get(0)] + "\n");
                myWriter.write(
                        "Precursor Accuracy:\t\t" + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracyType().name() + "\n");
                myWriter.write(
                        "Fragment Accuracy:\t\t" + optimisedSearchParameter.getSearchParameters().getFragmentIonAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getFragmentAccuracyType().name() + "\n");
                myWriter.write(
                        "PrecursorCharge:\t\t" + optimisedSearchParameter.getSearchParameters().getMinChargeSearched() + " - " + optimisedSearchParameter.getSearchParameters().getMaxChargeSearched() + "\n");
                myWriter.write(
                        "Isotops:\t\t\t" + optimisedSearchParameter.getSearchParameters().getMinIsotopicCorrection() + " - " + optimisedSearchParameter.getSearchParameters().getMaxIsotopicCorrection() + "\n");
//            myWriter.write("default Variable mod:\t" + optimisedSearchParameter.getSearchParameters() + "  Factor " + referenceFactor);
                String fm = "";

                if (optimisedSearchParameter.getSearchParameters()
                        .getModificationParameters().getFixedModifications() != null) {
                    for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications()) {
                        fm += ("\t\t\t\t"+fixedMod + "\n");
                    }
                }

                myWriter.write(
                        "Fixed Modifications:\n" + fm);
                fm = "";

                if (optimisedSearchParameter.getSearchParameters()
                        .getModificationParameters().getVariableModifications() != null) {
                    for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications()) {
                        fm += ("\t\t\t\t"+v + "\n");
                    }
                }

                myWriter.write(
                        "Variable Modifications:\n" + fm);
                if (searchEngine.getIndex()
                        == Advocate.xtandem.getIndex()) {
                    XtandemParameters xtandemParameters = (XtandemParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
                    myWriter.write("---------------------------xtandem advanced-----------------------------\n");
                    myWriter.write("Spectrum Dynamic Range:\t" + xtandemParameters.getDynamicRange() + "\n");
                    myWriter.write("Number of Peaks:\t" + xtandemParameters.getnPeaks() + "\n");
                    myWriter.write("MinimumFragmentMz:\t" + xtandemParameters.getMinFragmentMz() + "\n");
                    myWriter.write("Minimum Peaks:\t\t" + xtandemParameters.getMinPeaksPerSpectrum() + "\n");
                    myWriter.write("NoiseSuppression:\t" + xtandemParameters.isUseNoiseSuppression() + "  (" + xtandemParameters.getMinPrecursorMass() + ")\n");
                    myWriter.write("Parent isotop exp:\t" + xtandemParameters.getParentMonoisotopicMassIsotopeError() + "\n");
                    myWriter.write("QuickAcetyl:\t\t" + xtandemParameters.isProteinQuickAcetyl() + "\n");
                    myWriter.write("QuickPyrolidone:\t" + xtandemParameters.isQuickPyrolidone() + "\n");
                    myWriter.write("stP Bias:\t\t" + xtandemParameters.isStpBias() + "\n");

                    myWriter.write("Refinement:\t\t" + xtandemParameters.isRefine() + "\n");
                    myWriter.write("UnanticipatedCleavage:\t" + xtandemParameters.isRefineUnanticipatedCleavages() + "\n");
                    myWriter.write("SimiEnzymaticCleavage:\t" + xtandemParameters.isRefineSemi() + "\n");
                    myWriter.write("Potintial Modification:\t" + xtandemParameters.isPotentialModificationsForFullRefinment() + "\n");
                    myWriter.write("Point Mutations:\t" + xtandemParameters.isRefinePointMutations() + "\n");
                    myWriter.write("SnAPs:\t\t\t" + xtandemParameters.isRefineSnaps() + "\n");
                    myWriter.write("Spectrum Synthesis:\t" + xtandemParameters.isRefineSpectrumSynthesis() + "\n");

                    myWriter.write("------------------------------------------------------------------------\n");

                    String rfm = "";
                    if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications() != null) {
                        for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications()) {
                            rfm += ("\t\t\t"+fixedMod + "\n");
                        }
                    }
                    myWriter.write("Refined Fixed Modifications:\n" + rfm);
                    rfm = "";
                    if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications() != null) {
                        for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications()) {
                            rfm += ("\t\t\t"+v + "\n");
                        }
                    }
                    myWriter.write("Refined Variable Modification:\n" + rfm + "\n");
                } else if (searchEngine.getIndex()
                        == Advocate.myriMatch.getIndex()) {
                    MyriMatchParameters myriMatchParameters = (MyriMatchParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
                    myWriter.write("---------------------------MyriMatch advanced-----------------------------\n");
                    myWriter.write("Peptide Length (min-max):\t" + myriMatchParameters.getMinPeptideLength() + "-" + myriMatchParameters.getMaxPeptideLength() + "\n");
                    myWriter.write("Precursor Mass (min-max):\t" + myriMatchParameters.getMinPrecursorMass() + "-" + myriMatchParameters.getMaxPrecursorMass() + "\n");
                    myWriter.write("Max Variable PTM        :\t" + myriMatchParameters.getMaxDynamicMods() + "\n");
                    myWriter.write("Fragmentaion Methods    :\t" + myriMatchParameters.getFragmentationRule() + "\n");
                    myWriter.write("Enzymatic Terminals     :\t" + myriMatchParameters.getMinTerminiCleavages() + "\n");
                    myWriter.write("smart + 3 model     :\t" + myriMatchParameters.getUseSmartPlusThreeModel() + "\n");
                    myWriter.write("Compute xCorr           :\t" + myriMatchParameters.getComputeXCorr() + "\n");
                    myWriter.write("TIC Cutoff  %:\t" + myriMatchParameters.getTicCutoffPercentage() + "\n");
                    myWriter.write("Num Of Inten Classes:\t" + myriMatchParameters.getNumIntensityClasses() + "\n");

                    myWriter.write("Class Size Multiplier:\t" + myriMatchParameters.getClassSizeMultiplier() + "\n");
                    myWriter.write("Number Of Batches:\t" + myriMatchParameters.getNumberOfBatches() + "\n");
                    myWriter.write("Max Peak Count:\t" + myriMatchParameters.getMaxPeakCount() + "\n");
                } else if (searchEngine.getIndex() == Advocate.sage.getIndex()) {
                    SageParameters sageParameters = (SageParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
                    myWriter.write("---------------------------Sage advanced-----------------------------\n");
                    myWriter.write("Peptide Length(min-max):\t" + sageParameters.getMinPeptideLength() + "-" + sageParameters.getMaxPeptideLength() + "\n");
                    myWriter.write("Fragment MZ(min-max):\t\t" + sageParameters.getMinFragmentMz() + "-" + sageParameters.getMaxFragmentMz() + "\n");
                    myWriter.write("Peptide Mass:\t\t\t" + sageParameters.getMinPeptideMass() + "-" + sageParameters.getMaxPeptideMass() + "\n");
                    myWriter.write("Min Ion Index:\t\t\t" + sageParameters.getMinIonIndex() + "\n");
                    myWriter.write("Max Variable Modification:\t" + sageParameters.getMaxVariableMods() + "\n");
                    myWriter.write("Generate Decoy:\t\t\t" + sageParameters.getGenerateDecoys() + "\n");
                    myWriter.write("Deisotope:\t\t\t" + sageParameters.getDeisotope() + "\n");
                    myWriter.write("Chimeric Spectra:\t\t" + sageParameters.getChimera() + "\n");
                    myWriter.write("Wide window :\t\t\t" + sageParameters.getWideWindow() + "\n");

                    myWriter.write("Predect RT:\t\t\t" + sageParameters.getPredictRt() + "\n");

                    myWriter.write("Number of Peaks:\t\t" + sageParameters.getMinPeaks() + "-" + sageParameters.getMaxPeaks() + "\n");
                    myWriter.write("Min Mached Peaks :\t\t" + sageParameters.getMinMatchedPeaks() + "\n");
                    myWriter.write(("Max Fragment Charge:\t\t" + sageParameters.getMaxFragmentCharge() + "\n").replace("null", " "));
                }
//                myWriter.write("---------------------------advanced Parameter Objects-----------------------------");
//                for (String param : parameterScoreMap.keySet()) {
//                    myWriter.write(param + ":\t" + parameterScoreMap.get(param).toString() + "\n");
//                }
            }
            MainUtilities.QSProtWaitingHandler.addLogMassage("Successfully wrote to the file.");
        } catch (IOException e) {
            MainUtilities.QSProtWaitingHandler.addLogMassage("An error occurred.");
            e.printStackTrace();
        }
    }
}
