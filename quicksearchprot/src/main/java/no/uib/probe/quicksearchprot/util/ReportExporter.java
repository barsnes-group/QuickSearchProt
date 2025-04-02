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
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.ParameterScoreModel;

/**
 *
 * @author yfa041
 */
public class ReportExporter {

    public static void addElementToReport(String datasetId, String paramId, String paramOption, double idRate, double timeInSecond) {
        System.out.println("Report --->  datasetId: " + datasetId + "\tparamId:" + paramId + "\tparamOption:" + paramOption + "\tid_rate:" + idRate + "%\ttime:" + timeInSecond);

    }

    public static void printFullReport(File optimisedSearchParameterFile, SearchingSubDataset dataset, Advocate searchEngine, String datasetId) {

        IdentificationParameters optimisedSearchParameter;
        try {
            optimisedSearchParameter = IdentificationParameters.getIdentificationParameters(optimisedSearchParameterFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        System.out.println("-------------------------------" + datasetId + "(" + searchEngine.getName() + ")-----------------------------------------");
        if (dataset != null) {
        System.out.println("Spectra size        :\t" + dataset.getSubsetSize());
        }
        System.out.println("Digestion           :\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name());
        if (optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equals("enzyme")) {

            System.out.println("Enzyme              :\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName());
            System.out.println("Specificity         :\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getSpecificity(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()));
            System.out.println("Max Missed Cleavages:\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getnMissedCleavages(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()));
        }
        System.out.println("Fragment Ion Types  :\t" + optimisedSearchParameter.getSearchParameters().getForwardIons().get(0) + "-" + optimisedSearchParameter.getSearchParameters().getRewindIons().get(0));
        System.out.println("Precursor Accuracy  :\t" + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracyType().name());
        System.out.println("Fragment Accuracy   :\t" + optimisedSearchParameter.getSearchParameters().getFragmentIonAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getFragmentAccuracyType().name());
        System.out.println("PrecursorCharge     :\t" + optimisedSearchParameter.getSearchParameters().getMinChargeSearched() + " - " + optimisedSearchParameter.getSearchParameters().getMaxChargeSearched());
        System.out.println("Isotops             :\t" + optimisedSearchParameter.getSearchParameters().getMinIsotopicCorrection() + " - " + optimisedSearchParameter.getSearchParameters().getMaxIsotopicCorrection());
//            System.out.println("default Variable mod:\t" + optimisedSearchParameter.getSearchParameters() + "  Factor " + referenceFactor);
        String fm = "";
        if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications() != null) {
            for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications()) {
                fm += (fixedMod + "\n");
            }
        }
        System.out.println("Fixed Mod mod:\n" + fm);
        fm = "";
        if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications() != null) {
            for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications()) {
                fm += (v + "% \t" + "\n");
            }
        }
        System.out.println("Variable mod:\n" + fm);
        if (searchEngine.getIndex() == Advocate.xtandem.getIndex()) {
            XtandemParameters xtandemParameters = (XtandemParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
            System.out.println("---------------------------xtandem advanced-----------------------------");
            System.out.println("Spectrum Dynamic Range:\t" + xtandemParameters.getDynamicRange());
            System.out.println("Number of Peaks       :\t" + xtandemParameters.getnPeaks());
            System.out.println("MinimumFragmentMz     :\t" + xtandemParameters.getMinFragmentMz());
            System.out.println("Minimum Peaks         :\t" + xtandemParameters.getMinPeaksPerSpectrum());
            System.out.println("Use NoiseSuppression  :\t" + xtandemParameters.isUseNoiseSuppression() + "  (" + xtandemParameters.getMinPrecursorMass() + ")");
            System.out.println("Use Parent isotop exp :\t" + xtandemParameters.getParentMonoisotopicMassIsotopeError());
            System.out.println("Use QuickAcetyl       :\t" + xtandemParameters.isProteinQuickAcetyl());
            System.out.println("Use QuickPyrolidone   :\t" + xtandemParameters.isQuickPyrolidone());
            System.out.println("Use stP Bias          :\t" + xtandemParameters.isStpBias());

            System.out.println("Use Refinement        :\t" + xtandemParameters.isRefine());
            System.out.println("UnanticipatedCleavage :\t" + xtandemParameters.isRefineUnanticipatedCleavages());
            System.out.println("SimiEnzymaticCleavage :\t" + xtandemParameters.isRefineSemi());
            System.out.println("Potintial Modification:\t" + xtandemParameters.isPotentialModificationsForFullRefinment());
            System.out.println("Use PointMutations    :\t" + xtandemParameters.isRefinePointMutations());
            System.out.println("Use SnAPs             :\t" + xtandemParameters.isRefineSnaps());
            System.out.println("Spectrum Synthesis    :\t" + xtandemParameters.isRefineSpectrumSynthesis());

            System.out.println("------------------------------------------------------------------------");

            String rfm = "";
            if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications() != null) {
                for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications()) {
                    rfm += (fixedMod + "\n");
                }
            }
            System.out.println("Refined Fixed Mod mod:\n" + rfm);
            rfm = "";
            if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications() != null) {
                for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications()) {
                    rfm += (v + "\n");
                }
            }
            System.out.println("Refined Variable mod:\n" + rfm);
        } else if (searchEngine.getIndex() == Advocate.myriMatch.getIndex()) {
            MyriMatchParameters myriMatchParameters = (MyriMatchParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
            System.out.println("---------------------------MyriMatch advanced-----------------------------");
            System.out.println("Peptide Length (min-max):\t" + myriMatchParameters.getMinPeptideLength() + "-" + myriMatchParameters.getMaxPeptideLength());
            System.out.println("Precursor Mass (min-max):\t" + myriMatchParameters.getMinPrecursorMass() + "-" + myriMatchParameters.getMaxPrecursorMass());
            System.out.println("Max Variable PTM        :\t" + myriMatchParameters.getMaxDynamicMods());
            System.out.println("Fragmentaion Methods    :\t" + myriMatchParameters.getFragmentationRule());
            System.out.println("Enzymatic Terminals     :\t" + myriMatchParameters.getMinTerminiCleavages());
            System.out.println("Use smart + 3 model     :\t" + myriMatchParameters.getUseSmartPlusThreeModel());
            System.out.println("Compute xCorr           :\t" + myriMatchParameters.getComputeXCorr());
            System.out.println("TIC Cutoff  %           :\t" + myriMatchParameters.getTicCutoffPercentage());
            System.out.println("Num Of Inten Classes    :\t" + myriMatchParameters.getNumIntensityClasses());

            System.out.println("Class Size Multiplier   :\t" + myriMatchParameters.getClassSizeMultiplier());
            System.out.println("Number Of Batches       :\t" + myriMatchParameters.getNumberOfBatches());
            System.out.println("Max Peak Count          :\t" + myriMatchParameters.getMaxPeakCount());
        } else if (searchEngine.getIndex() == Advocate.sage.getIndex()) {
            SageParameters sageParameters = (SageParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
            System.out.println("---------------------------Sage advanced-----------------------------");
            System.out.println("Peptide Length (min-max):\t" + sageParameters.getMinPeptideLength() + "-" + sageParameters.getMaxPeptideLength());
            System.out.println("Fragment mz    (min-max):\t" + sageParameters.getMinFragmentMz() + "-" + sageParameters.getMaxFragmentMz());
            System.out.println("Peptide Mass            :\t" + sageParameters.getMinPeptideMass() + "-" + sageParameters.getMaxPeptideMass());
            System.out.println("Min Ion Index           :\t" + sageParameters.getMinIonIndex());
            System.out.println("Max Variable Mod        :\t" + sageParameters.getMaxVariableMods());
            System.out.println("Generate Decoy          :\t" + sageParameters.getGenerateDecoys());
            System.out.println("Deisotope               :\t" + sageParameters.getDeisotope());
            System.out.println("Chimeric Spectra        :\t" + sageParameters.getChimera());
            System.out.println("Wide window             :\t" + sageParameters.getWideWindow());
            System.out.println("Predect RT              :\t" + sageParameters.getPredictRt());

            System.out.println("Number of Peaks         :\t" + sageParameters.getMinPeaks() + "-" + sageParameters.getMaxPeaks());
            System.out.println("Min Mached Peaks        :\t" + sageParameters.getMinMatchedPeaks());
            System.out.println("Max Fragment Charge     :\t" + sageParameters.getMaxFragmentCharge());
        }

    }

    public static void exportFullReport(File optimisedSearchParameterFile, SearchingSubDataset dataset, Advocate searchEngine, String datasetId, String timeInMin, String initDsTime, Map<String, TreeSet<ParameterScoreModel>> parameterScoreMap) {
        if (dataset == null) {
            System.out.println("can not export un exist dataset " + datasetId);
            return;
        }
        String pathToRemoteResults ="C:\\Users\\yfa041\\OneDrive - University of Bergen\\quickprotResults";//dataset.getSubDataFolder()
        File reportFile = new File(pathToRemoteResults, dataset.getSubMsFile().getName()+"_"+searchEngine.getName() + "_.txt");
        IdentificationParameters optimisedSearchParameter;
        try {
            optimisedSearchParameter = IdentificationParameters.getIdentificationParameters(optimisedSearchParameterFile);
            if (!reportFile.exists()) {
                reportFile.createNewFile();
            } else {
                System.out.println("file exist and will re-write");
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
                        "Used Time  to init ds:\t" + initDsTime + "  Minutes\n");
                myWriter.write(
                        "Used Time           :\t" + timeInMin + "  Minutes\n");
                myWriter.write(
                        "Subset size         :\t" + dataset.getSubsetSize()+ "\n");
                myWriter.write(
                        "Digestion           :\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name() + "\n");
                if (optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equals("enzyme")) {
                    myWriter.write(
                            "Enzyme              :\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName() + "\n");
                    myWriter.write(
                            "Specificity         :\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getSpecificity(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()) + "\n");
                    myWriter.write(
                            "Max Missed Cleavages:\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getnMissedCleavages(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()) + "\n");
                }
                myWriter.write(
                        "Fragment Ion Types  :\t" + optimisedSearchParameter.getSearchParameters().getForwardIons().get(0) + "-" + optimisedSearchParameter.getSearchParameters().getRewindIons().get(0) + "\n");
                myWriter.write(
                        "Precursor Accuracy  :\t" + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracyType().name() + "\n");
                myWriter.write(
                        "Fragment Accuracy   :\t" + optimisedSearchParameter.getSearchParameters().getFragmentIonAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getFragmentAccuracyType().name() + "\n");
                myWriter.write(
                        "PrecursorCharge     :\t" + optimisedSearchParameter.getSearchParameters().getMinChargeSearched() + " - " + optimisedSearchParameter.getSearchParameters().getMaxChargeSearched() + "\n");
                myWriter.write(
                        "Isotops             :\t" + optimisedSearchParameter.getSearchParameters().getMinIsotopicCorrection() + " - " + optimisedSearchParameter.getSearchParameters().getMaxIsotopicCorrection() + "\n");
//            myWriter.write("default Variable mod:\t" + optimisedSearchParameter.getSearchParameters() + "  Factor " + referenceFactor);
                String fm = "";

                if (optimisedSearchParameter.getSearchParameters()
                        .getModificationParameters().getFixedModifications() != null) {
                    for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications()) {
                        fm += (fixedMod + "\n");
                    }
                }

                myWriter.write(
                        "Fixed Mod mod:\n" + fm);
                fm = "";

                if (optimisedSearchParameter.getSearchParameters()
                        .getModificationParameters().getVariableModifications() != null) {
                    for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications()) {
                        fm += (v + "\n");
                    }
                }

                myWriter.write(
                        "Variable mod:\n" + fm);
                if (searchEngine.getIndex()
                        == Advocate.xtandem.getIndex()) {
                    XtandemParameters xtandemParameters = (XtandemParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
                    myWriter.write("---------------------------xtandem advanced-----------------------------\n");
                    myWriter.write("Spectrum Dynamic Range:\t" + xtandemParameters.getDynamicRange() + "\n");
                    myWriter.write("Number of Peaks       :\t" + xtandemParameters.getnPeaks() + "\n");
                    myWriter.write("MinimumFragmentMz     :\t" + xtandemParameters.getMinFragmentMz() + "\n");
                    myWriter.write("Minimum Peaks         :\t" + xtandemParameters.getMinPeaksPerSpectrum() + "\n");
                    myWriter.write("Use NoiseSuppression  :\t" + xtandemParameters.isUseNoiseSuppression() + "  (" + xtandemParameters.getMinPrecursorMass() + ")\n");
                    myWriter.write("Use Parent isotop exp :\t" + xtandemParameters.getParentMonoisotopicMassIsotopeError() + "\n");
                    myWriter.write("Use QuickAcetyl       :\t" + xtandemParameters.isProteinQuickAcetyl() + "\n");
                    myWriter.write("Use QuickPyrolidone   :\t" + xtandemParameters.isQuickPyrolidone() + "\n");
                    myWriter.write("Use stP Bias          :\t" + xtandemParameters.isStpBias() + "\n");

                    myWriter.write("Use Refinement        :\t" + xtandemParameters.isRefine() + "\n");
                    myWriter.write("UnanticipatedCleavage :\t" + xtandemParameters.isRefineUnanticipatedCleavages() + "\n");
                    myWriter.write("SimiEnzymaticCleavage :\t" + xtandemParameters.isRefineSemi() + "\n");
                    myWriter.write("Potintial Modification:\t" + xtandemParameters.isPotentialModificationsForFullRefinment() + "\n");
                    myWriter.write("Use PointMutations    :\t" + xtandemParameters.isRefinePointMutations() + "\n");
                    myWriter.write("Use SnAPs             :\t" + xtandemParameters.isRefineSnaps() + "\n");
                    myWriter.write("Spectrum Synthesis    :\t" + xtandemParameters.isRefineSpectrumSynthesis() + "\n");

                    myWriter.write("------------------------------------------------------------------------\n");

                    String rfm = "";
                    if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications() != null) {
                        for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications()) {
                            rfm += (fixedMod + "\n");
                        }
                    }
                    myWriter.write("Refined Fixed Mod mod:\n" + rfm);
                    rfm = "";
                    if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications() != null) {
                        for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications()) {
                            rfm += (v + "\n");
                        }
                    }
                    myWriter.write("Refined Variable mod:\n" + rfm + "\n");
                } else if (searchEngine.getIndex()
                        == Advocate.myriMatch.getIndex()) {
                    MyriMatchParameters myriMatchParameters = (MyriMatchParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
                    myWriter.write("---------------------------MyriMatch advanced-----------------------------\n");
                    myWriter.write("Peptide Length (min-max):\t" + myriMatchParameters.getMinPeptideLength() + "-" + myriMatchParameters.getMaxPeptideLength() + "\n");
                    myWriter.write("Precursor Mass (min-max):\t" + myriMatchParameters.getMinPrecursorMass() + "-" + myriMatchParameters.getMaxPrecursorMass() + "\n");
                    myWriter.write("Max Variable PTM        :\t" + myriMatchParameters.getMaxDynamicMods() + "\n");
                    myWriter.write("Fragmentaion Methods    :\t" + myriMatchParameters.getFragmentationRule() + "\n");
                    myWriter.write("Enzymatic Terminals     :\t" + myriMatchParameters.getMinTerminiCleavages() + "\n");
                    myWriter.write("Use smart + 3 model     :\t" + myriMatchParameters.getUseSmartPlusThreeModel() + "\n");
                    myWriter.write("Compute xCorr           :\t" + myriMatchParameters.getComputeXCorr() + "\n");
                    myWriter.write("TIC Cutoff  %           :\t" + myriMatchParameters.getTicCutoffPercentage() + "\n");
                    myWriter.write("Num Of Inten Classes    :\t" + myriMatchParameters.getNumIntensityClasses() + "\n");

                    myWriter.write("Class Size Multiplier   :\t" + myriMatchParameters.getClassSizeMultiplier() + "\n");
                    myWriter.write("Number Of Batches       :\t" + myriMatchParameters.getNumberOfBatches() + "\n");
                    myWriter.write("Max Peak Count          :\t" + myriMatchParameters.getMaxPeakCount() + "\n");
                } else if (searchEngine.getIndex() == Advocate.sage.getIndex()) {
                    SageParameters sageParameters = (SageParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
                    myWriter.write("---------------------------Sage advanced-----------------------------");
                    myWriter.write("Peptide Length (min-max):\t" + sageParameters.getMinPeptideLength() + "-" + sageParameters.getMaxPeptideLength() + "\n");
                    myWriter.write("Fragment mz    (min-max):\t" + sageParameters.getMinFragmentMz() + "-" + sageParameters.getMaxFragmentMz() + "\n");
                    myWriter.write("Peptide Mass            :\t" + sageParameters.getMinPeptideMass() + "-" + sageParameters.getMaxPeptideMass() + "\n");
                    myWriter.write("Min Ion Index           :\t" + sageParameters.getMinIonIndex() + "\n");
                    myWriter.write("Max Variable Mod        :\t" + sageParameters.getMaxVariableMods() + "\n");
                    myWriter.write("Generate Decoy          :\t" + sageParameters.getGenerateDecoys() + "\n");
                    myWriter.write("Deisotope               :\t" + sageParameters.getDeisotope() + "\n");
                    myWriter.write("Chimeric Spectra        :\t" + sageParameters.getChimera() + "\n");
                    myWriter.write("Wide window             :\t" + sageParameters.getWideWindow() + "\n");

                    myWriter.write("Predect RT              :\t" + sageParameters.getPredictRt() + "\n");

                    myWriter.write("Number of Peaks         :\t" + sageParameters.getMinPeaks() + "-" + sageParameters.getMaxPeaks() + "\n");
                    myWriter.write("Min Mached Peaks        :\t" + sageParameters.getMinMatchedPeaks() + "\n");
                    myWriter.write("Max Fragment Charge     :\t" + sageParameters.getMaxFragmentCharge() + "\n");
                }
                myWriter.write("---------------------------advanced Parameter Objects-----------------------------");
                for (String param : parameterScoreMap.keySet()) {
                    myWriter.write(param + ":\t" + parameterScoreMap.get(param).toString() + "\n");
                }
            }
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
