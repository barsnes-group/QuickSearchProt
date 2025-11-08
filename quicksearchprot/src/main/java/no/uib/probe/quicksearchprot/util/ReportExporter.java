package no.uib.probe.quicksearchprot.util;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import com.compomics.util.parameters.identification.tool_specific.XtandemParameters;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.model.SearchingSubDataset;

/**
 *
 * @author yfa041
 */
public class ReportExporter {

    private static final String[] ions = new String[]{"a", "b", "c", "x", "y", "z"};

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
        MainUtilities.QSProtWaitingHandler.addOutputMassage("\t\u2605\u2605\u2605\u2605\u2605 " + datasetId.toUpperCase() + " ( " + searchEngine.getName().toUpperCase() + " ) \u2605\u2605\u2605\u2605\u2605\n");
        if (dataset != null) {
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Subset Size:\t\t" + dataset.getSubsetSize() + "\n");
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("""
                                                             Parameter\t\tValue\t\tScore\tComments 
                                                             """);

        MainUtilities.QSProtWaitingHandler.addOutputMassage("Digestion:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name() + "\t" + MainUtilities.getConfidentAsString("Digestion"));
        if (optimisedSearchParameter.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equals("enzyme")) {

            MainUtilities.QSProtWaitingHandler.addOutputMassage("Enzyme:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName() + "\t" + MainUtilities.getConfidentAsString("Enzyme"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Specificity:\t\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getSpecificity(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()) + "\t" + MainUtilities.getConfidentAsString("Specificity"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Max Missed Cleavages:\t" + optimisedSearchParameter.getSearchParameters().getDigestionParameters().getnMissedCleavages(optimisedSearchParameter.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()) + "\t" + MainUtilities.getConfidentAsString("MaxMissedCleavages"));
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragment Ion Types:\t" + ions[optimisedSearchParameter.getSearchParameters().getForwardIons().get(0)] + " and " + ions[optimisedSearchParameter.getSearchParameters().getRewindIons().get(0)] + "\t" + MainUtilities.getConfidentAsString("FragmentIonTypes"));
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Precursor Accuracy:\t" + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getPrecursorAccuracyType().name() + "\t" + MainUtilities.getConfidentAsString("PrecursorAccuracy"));
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragment Accuracy:\t" + optimisedSearchParameter.getSearchParameters().getFragmentIonAccuracy() + " " + optimisedSearchParameter.getSearchParameters().getFragmentAccuracyType().name() + "\t" + MainUtilities.getConfidentAsString("FragmentAccuracy"));
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Precursor Charge:\t" + optimisedSearchParameter.getSearchParameters().getMinChargeSearched() + " to " + optimisedSearchParameter.getSearchParameters().getMaxChargeSearched() + "\t" + MainUtilities.getConfidentAsString("PrecursorCharge"));
        MainUtilities.QSProtWaitingHandler.addOutputMassage("Isotops:\t\t" + optimisedSearchParameter.getSearchParameters().getMinIsotopicCorrection() + " to " + optimisedSearchParameter.getSearchParameters().getMaxIsotopicCorrection() + "\t" + MainUtilities.getConfidentAsString("Isotops"));
//            MainUtilities.QSProtWaitingHandler.addOutputMassage("default Variable mod:\t" + optimisedSearchParameter.getSearchParameters() + "  Factor " + referenceFactor);
        String fm = "";
        if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications() != null) {
            for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getFixedModifications()) {
                fm += ("\t" + fixedMod + "" + MainUtilities.getConfidentAsString(fixedMod) + "\n\t");
            }
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("\nFixed Modifications:" + fm);
        fm = "";
        if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications() != null) {
            for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getVariableModifications()) {
                String t = "";
                if (v.length() < 16) {
                    t = "\t";
                }
                fm += ("\t" + v + t + MainUtilities.getConfidentAsString(v) + "\n\t");
            }
        }
        MainUtilities.QSProtWaitingHandler.addOutputMassage("\nVariable Modifications:" + fm);
        if (searchEngine.getIndex() == Advocate.xtandem.getIndex()) {
            XtandemParameters xtandemParameters = (XtandemParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("\n\t\u2605\u2605\u2605\u2605\u2605 X! Tandem Advanced Parameters \u2605\u2605\u2605\u2605\u2605 \n");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("""
                                                             Parameter\t\tValue\t\tScore\tComments 
                                                             """);

            MainUtilities.QSProtWaitingHandler.addOutputMassage("Spectrum Dynamic Range:\t" + xtandemParameters.getDynamicRange() + "\t" + MainUtilities.getConfidentAsString("SpectrumDynamicRange"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Number of Peaks:\t" + xtandemParameters.getnPeaks() + "\t" + MainUtilities.getConfidentAsString("NumberofPeaks"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Minimum Fragment Mz:\t" + xtandemParameters.getMinFragmentMz() + "\t" + MainUtilities.getConfidentAsString("MinimumFragmentMz"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Minimum Peaks:\t" + xtandemParameters.getMinPeaksPerSpectrum() + "\t" + MainUtilities.getConfidentAsString("MinimumPeaks"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Noise Suppression:\t" + xtandemParameters.isUseNoiseSuppression() + "  (" + xtandemParameters.getMinPrecursorMass() + ")" + "\t" + MainUtilities.getConfidentAsString("NoiseSuppression"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Parent isotop:\t\t" + xtandemParameters.getParentMonoisotopicMassIsotopeError() + "\t" + MainUtilities.getConfidentAsString("Parentisotop"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Quick Acetyl:\t\t" + xtandemParameters.isProteinQuickAcetyl() + "\t" + MainUtilities.getConfidentAsString("QuickAcetyl"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Quick Pyrolidone:\t" + xtandemParameters.isQuickPyrolidone() + "\t" + MainUtilities.getConfidentAsString("QuickPyrolidone"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("stP Bias:\t\t" + xtandemParameters.isStpBias() + "\t" + MainUtilities.getConfidentAsString("stPBias"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Refinement:\t\t" + xtandemParameters.isRefine() + "\t" + MainUtilities.getConfidentAsString("Refinement"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Unanticipated Cleavage:\t" + xtandemParameters.isRefineUnanticipatedCleavages() + "\t" + MainUtilities.getConfidentAsString("UnanticipatedCleavage"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Simi-Enzymatic Cleavage:\t" + xtandemParameters.isRefineSemi() + "\t" + MainUtilities.getConfidentAsString("SimiEnzymaticCleavage"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Potintial Modification:\t" + xtandemParameters.isPotentialModificationsForFullRefinment() + "\t" + MainUtilities.getConfidentAsString("PotintialModification"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Point Mutations:\t\t" + xtandemParameters.isRefinePointMutations() + "\t" + MainUtilities.getConfidentAsString("PointMutations"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("SnAPs:\t\t" + xtandemParameters.isRefineSnaps() + "\t" + MainUtilities.getConfidentAsString("SnAPs"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Spectrum Synthesis:\t" + xtandemParameters.isRefineSpectrumSynthesis() + "\t" + MainUtilities.getConfidentAsString("SpectrumSynthesis"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("------------------------------------------------------------------------");
            String rfm = "";
            if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications() != null) {
                for (String fixedMod : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementFixedModifications()) {
                    rfm += ("\t" + fixedMod + "" + MainUtilities.getConfidentAsString(fixedMod) + "\n\t");
                }
            }
            MainUtilities.QSProtWaitingHandler.addOutputMassage("\nRefined Fixed Modification:" + rfm);
            rfm = "";
            if (optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications() != null) {
                for (String v : optimisedSearchParameter.getSearchParameters().getModificationParameters().getRefinementVariableModifications()) {
                    String t = "";
                    if (v.length() < 16) {
                        t = "\t";
                    }
                    rfm += ("\t" + v + t + MainUtilities.getConfidentAsString(v) + "\n\t");
                }
            }
            MainUtilities.QSProtWaitingHandler.addOutputMassage("\nRefined Variable Modification:" + rfm);
        } else if (searchEngine.getIndex() == Advocate.myriMatch.getIndex()) {
            MyriMatchParameters myriMatchParameters = (MyriMatchParameters) optimisedSearchParameter.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("--------------------------- MyriMatch advanced -----------------------------");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Peptide Length (min-max):\t" + myriMatchParameters.getMinPeptideLength() + " to " + myriMatchParameters.getMaxPeptideLength());
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Precursor Mass (min-max):\t" + myriMatchParameters.getMinPrecursorMass() + " to " + myriMatchParameters.getMaxPrecursorMass());
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

            MainUtilities.QSProtWaitingHandler.addOutputMassage("\n\t\u2605\u2605\u2605\u2605\u2605 Sage Advanced Parameters \u2605\u2605\u2605\u2605\u2605 \n");
            MainUtilities.QSProtWaitingHandler.addOutputMassage("""
                                                             Parameter\t\tValue\t\tScore\tComments 
                                                             """);

            MainUtilities.QSProtWaitingHandler.addOutputMassage("Peptide Length (min-max):\t" + sageParameters.getMinPeptideLength() + " to " + sageParameters.getMaxPeptideLength() + "\t" + MainUtilities.getConfidentAsString("PeptideLength"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Fragment MZ    (min-max):\t" + sageParameters.getMinFragmentMz() + " to " + sageParameters.getMaxFragmentMz() + "\t" + MainUtilities.getConfidentAsString("FragmentMZ"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Peptide Mass:\t\t" + sageParameters.getMinPeptideMass() + " to " + sageParameters.getMaxPeptideMass() + "\t" + MainUtilities.getConfidentAsString("PeptideMass"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Min Ion Index:\t\t" + sageParameters.getMinIonIndex() + "\t" + MainUtilities.getConfidentAsString("MinIonIndex"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Max Variable Modification:\t" + sageParameters.getMaxVariableMods() + "\t" + MainUtilities.getConfidentAsString("MaxVariableModification"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Generate Decoy:\t" + sageParameters.getGenerateDecoys() + "\t" + MainUtilities.getConfidentAsString("GenerateDecoy"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Deisotope:\t\t" + sageParameters.getDeisotope() + "\t" + MainUtilities.getConfidentAsString("Deisotope"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Chimeric Spectra:\t" + sageParameters.getChimera() + "\t" + MainUtilities.getConfidentAsString("ChimericSpectra"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Wide Window:\t\t" + sageParameters.getWideWindow() + "\t" + MainUtilities.getConfidentAsString("WideWindow"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Predect RT:\t\t" + sageParameters.getPredictRt() + "\t" + MainUtilities.getConfidentAsString("PredectRT"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Number of Peaks:\t" + sageParameters.getMinPeaks() + " to " + sageParameters.getMaxPeaks() + "\t" + MainUtilities.getConfidentAsString("NumberofPeaks"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("Min Mached Peaks:\t" + sageParameters.getMinMatchedPeaks() + "\t" + MainUtilities.getConfidentAsString("MinMachedPeaks"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage(("Max Fragment Charge:\t" + sageParameters.getMaxFragmentCharge()).replace("null", "") + "\t" + MainUtilities.getConfidentAsString("MaxFragmentCharge"));
            MainUtilities.QSProtWaitingHandler.addOutputMassage("----------------------------------------------------------------------------------------------------------");
        }
        MainUtilities.resetParamMap();

    }

    public static void exportFullReport() {
        String separting = ",";
        try {
            String pathToRemoteResults = Configurations.SUBSET_DATA_FOLDER;//dataset.getSubDataFolder()
//            pathToRemoteResults = "D:\\QuickSearchProt\\testdata\\data\\PXD028427\\Test_Dataset_1\\subsetFiles";

            String[] inttextArr = MainUtilities.QSProtWaitingHandler.getMainOutputTextPanel().getText().replace("\t\t", separting).replace("\t", separting).split("----------------------------------------------------------------------------------------------------------");
            for (String inttext : inttextArr) {
                String se = "XTANDEM";
                if (inttext.contains("SAGE")) {
                    se = "SAGE";
                }
                File reportFile = new File(pathToRemoteResults, "QuickSearchProt_results_" + se + "_.csv");
                if (reportFile.exists()) {
                    reportFile.delete();
                }
                reportFile.createNewFile();
                String[] textArr = inttext.split("\n");
//              //  System.out.println(text);
                try (FileWriter myWriter = new FileWriter(reportFile)) {
                    for (String text : textArr) {
                        if (text.trim().isEmpty() || text.equalsIgnoreCase(separting) || text.contains("???") || text.contains("\u2605")) {
                            continue;
                        }
                        if (text.startsWith(separting) && text.split(separting).length == 3) {
                            text = text.replace(separting, separting + separting);
                            text = text.replaceFirst(separting + separting, separting);
                            myWriter.append(text + "\n");
                        } else {
                            myWriter.append(text + "\n");
                        }
                    }
                }
                MainUtilities.QSProtWaitingHandler.addLogMassage("Successfully wrote to the file.");
            }
        } catch (IOException e) {
            MainUtilities.QSProtWaitingHandler.addLogMassage("An error occurred.");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        String text = "	????? TEST_DATASET_1 ( SAGE ) ?????\n"
                + "\n"
                + "\n"
                + "Subset Size:		3000\n"
                + "\n"
                + "\n"
                + "Parameter		Value		Score	Comments\n"
                + "\n"
                + "\n"
                + "Digestion:		enzyme			Pre-Selected\n"
                + "\n"
                + "Enzyme:		Trypsin		99 %	Default\n"
                + "\n"
                + "Specificity:		Specific			Pre-Selected\n"
                + "\n"
                + "Max Missed Cleavages:	2		100 %	\n"
                + "\n"
                + "Fragment Ion Types:	b-y			Pre-Selected\n"
                + "\n"
                + "Precursor Accuracy:	10.0 PPM			Pre-Selected\n"
                + "\n"
                + "Fragment Accuracy:	0.02 DA			Pre-Selected\n"
                + "\n"
                + "Precursor Charge:	2 - 4			Pre-Selected\n"
                + "\n"
                + "Isotops:		0 - 1			Pre-Selected\n"
                + "\n"
                + "\n"
                + "Fixed Modifications:	Carbamidomethylation of C		Pre-Selected\n"
                + "	\n"
                + "\n"
                + "\n"
                + "Variable Modifications:	Oxidation of M			Pre-Selected\n"
                + "		Phosphorylation of S		Pre-Selected\n"
                + "		Phosphorylation of T		Pre-Selected\n"
                + "		Phosphorylation of Y		Pre-Selected\n"
                + "	\n"
                + "\n"
                + "\n"
                + "	????? Sage Advanced Parameters ????? \n"
                + "\n"
                + "\n"
                + "Parameter		Value		Score	Comments\n"
                + "\n"
                + "\n"
                + "Peptide Length (min-max):	8-30			Pre-Selected\n"
                + "\n"
                + "Fragment MZ    (min-max):	200.0-2000.0			Pre-Selected\n"
                + "\n"
                + "Peptide Mass:		600.0-5000.0			Pre-Selected\n"
                + "\n"
                + "Min Ion Index:		2			Pre-Selected\n"
                + "\n"
                + "Max Variable Modification:	2			Pre-Selected\n"
                + "\n"
                + "Generate Decoy:	No			Pre-Selected\n"
                + "\n"
                + "Deisotope:		No			Pre-Selected\n"
                + "\n"
                + "Chimeric Spectra:	No			Pre-Selected\n"
                + "\n"
                + "Wide Window:		No			Pre-Selected\n"
                + "\n"
                + "Predect RT:		Yes			Pre-Selected\n"
                + "\n"
                + "Number of Peaks:	15-150			Pre-Selected\n"
                + "\n"
                + "Min Mached Peaks:	4			Pre-Selected\n"
                + "\n"
                + "Max Fragment Charge:				Pre-Selected\n"
                + "";
        MainUtilities.QSProtWaitingHandler.getMainOutputTextPanel().setText(text);
        exportFullReport();
    }
}
