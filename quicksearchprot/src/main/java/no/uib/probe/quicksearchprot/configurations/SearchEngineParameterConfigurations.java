/*
 * SearchEngineParameterConfigurations.java
 *
 * This class manages the configuration and enable/disable state of various search engine parameters
 * for the QuickSearchProt application.
 *
 * @author Yehia Mokhtar Farag
 */
package no.uib.probe.quicksearchprot.configurations;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to enable or disable search engine parameters, enzymes, and modifications.
 * Maintains a map of parameter names to their enabled/disabled states.
 */
public class SearchEngineParameterConfigurations {

    /**
     * Map storing the enabled/disabled state of search engine parameters.
     * Key: Parameter name
     * Value: true if enabled, false if disabled
     */
    private final Map<String, Boolean> paramMap;

    /**
     * Initializes the parameter configuration with default values (mostly enabled).
     * Automatically adds enzymes and modifications.
     */
    public SearchEngineParameterConfigurations() {
        paramMap = new HashMap<>();

        // General search parameters
        paramMap.put("enzyme", true);
        paramMap.put("wholeProtein", true);
        paramMap.put("unSpecific", true);

        // Add all enzymes
        for (Enzyme enzyme : EnzymeFactory.getInstance().getEnzymes()) {
            paramMap.put(enzyme.getName(), true);
        }

        // Search specificity parameters
        paramMap.put("PredictRt", true);
        paramMap.put("specific", true);
        paramMap.put("semiSpecific", true);
        paramMap.put("specificNTermOnly", true);
        paramMap.put("specificCTermOnly", true);

        // Missed cleavage parameter combinations
        paramMap.put("missedCleavages", true);
        for (int i = 0; i <= 2; i++) {
            for (int j = 3; j <= 5; j++) {
                paramMap.put("[" + i + "]-[" + j + "]", true);
            }
        }

        // Other missed cleavage combinations
        for (int i = 1; i <= 2; i++) {
            for (int j = 3; j <= 5; j++) {
                paramMap.put("[" + i + "]-[" + j + "]", true);
            }
        }

        // Accuracy and charge parameters
        paramMap.put("fragmentAccuracy", true);
        paramMap.put("precursorAccuracy", true);
        paramMap.put("charge", true);
        paramMap.put("isotop", true);
        paramMap.put("reference", true);

        // Add PTM (Post Translational Modification) parameters
        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        List<String> mods = new ArrayList<>(ptmFactory.getModifications(ModificationCategory.Common_Biological));
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common));
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common_Artifact));
        for (String mod : mods) {
            paramMap.put(mod, true);
        }

        // Spectrum and peak parameters
        paramMap.put("spectrumDR", true);
        paramMap.put("peaksNum", true);
        paramMap.put("minimumFragmentMz", true);
        paramMap.put("minpeaksNum", true);
        paramMap.put("noiseSupression", true);
        paramMap.put("parentMonoisotopicMassIsotopeError", true);

        // Quick and refine search parameters
        paramMap.put("useQuickAcetyl", true);
        paramMap.put("useStpBias", true);
        paramMap.put("useQuickPyrolidone", true);
        paramMap.put("useRefine", true);
        paramMap.put("useRefineUnanticipatedCleavages", true);
        paramMap.put("useRefineSimiEnzymaticCleavage", true);
        paramMap.put("usePotintialModification", true);
        paramMap.put("useRefinePointMutations", true);
        paramMap.put("useRefineSnAPs", true);
        paramMap.put("useRefineSpectrumSynthesis", true);

        // Placeholders for unsupported/empty parameters
        paramMap.put("", false);
        paramMap.put(null, false);

        // Precursor mass parameters
        paramMap.put("maxPrecursorMass", true);
        paramMap.put("minPrecursorMass", true);

        // Peptide length and PTMs
        paramMap.put("minPeptideLength", true);
        paramMap.put("maxPeptideLength", true);
        paramMap.put("maxVarPTMs", true);

        // Fragmentation, batch, and class parameters
        paramMap.put("fragmentationMethod", true);
        paramMap.put("enzymatricTerminals", true);
        paramMap.put("useSmartPlus3Model", true);
        paramMap.put("computeXCorr", true);
        paramMap.put("TICCutoff", true);
        paramMap.put("NumberOfIntensityClasses", true);
        paramMap.put("classSizeMultiplier", true);
        paramMap.put("NumberOfBatches", true);
        paramMap.put("maxPeakCount", true);

        // Additional fragment and peptide mass parameters
        paramMap.put("minFragmentMz", true);
        paramMap.put("maxFragmentMz", true);
        paramMap.put("minPeptideMass", true);
        paramMap.put("maxPeptideMass", true);
        paramMap.put("Deisotope", true);
        paramMap.put("minPeaks", true);
        paramMap.put("maxPeaks", true);
        paramMap.put("maxFragmentCharge", true);
        paramMap.put("ionMinIndex", true);
        paramMap.put("generateDecoy", true);
        paramMap.put("minMatchedPeaks", true);
        paramMap.put("Chimera", true);
        paramMap.put("WideWindow", true);

        // Charge combinations
        for (int i = 1; i < 5; i++) {
            for (int j = 2; j <= 5; j++) {
                if (j > i) {
                    paramMap.put("charge-" + i + "," + j, true);
                }
            }
        }
    }

    // ---- Parameter Disabling Methods ----

    /** Disables the 'isotop' parameter. */
    public void disableMinIsotop() {
        paramMap.replace("isotop", false);
    }

    /** Disables the 'isotop' parameter (alias). */
    public void disableMaxIsotop() {
        paramMap.replace("isotop", false);
    }

    /**
     * Disables charge range parameters (charge-X,Y where X in 2-4 and Y > X).
     */
    public void disableMinCharge() {
        for (int i = 2; i < 5; i++) {
            for (int j = 2; j <= 5; j++) {
                if (j > i) {
                    paramMap.replace("charge-" + i + "," + j, false);
                }
            }
        }
    }

    /** Disables the 'missedCleavages' parameter. */
    public void disableMissedCleavages() {
        paramMap.replace("missedCleavages", false);
    }

    /**
     * Checks if a parameter is enabled.
     * @param param the parameter name
     * @return true if enabled, false otherwise
     */
    public boolean isEnabledParam(String param) {
        Boolean enabled = paramMap.get(param);
        return enabled != null ? enabled : false;
    }

    /** Disables the 'specificEnzyme' parameter. */
    public void disableSpecificEnzyme() {
        paramMap.replace("specificEnzyme", false);
    }

    /** Disables the 'semiSpecificEnzyme' parameter. */
    public void disableSemiSpecificEnzyme() {
        paramMap.replace("semiSpecificEnzyme", false);
    }

    /** Disables the 'specificNTermOnly' parameter. */
    public void disableSpecificNTermOnlyEnzyme() {
        paramMap.replace("specificNTermOnly", false);
    }

    /**
     * Checks if the 'specificCTermOnly' enzyme parameter is enabled.
     * @return true if enabled, false otherwise
     */
    public boolean isSpecificCTermOnlyEnzyme() {
        Boolean enabled = paramMap.get("specificCTermOnly");
        return enabled != null ? enabled : false;
    }

    /** Disables the 'specificCTermOnly' parameter. */
    public void disableSpecificCTermOnlyEnzyme() {
        paramMap.replace("specificCTermOnly", false);
    }

    /** Disables the 'enzyme' parameter. */
    public void disableEnzyme() {
        paramMap.replace("enzyme", false);
    }

    /**
     * Sets the 'wholeProtein' parameter state.
     * @param wholeProtein true to enable, false to disable
     */
    public void setWholeProtein(boolean wholeProtein) {
        paramMap.replace("wholeProtein", wholeProtein);
    }

    /**
     * Disables the 'unSpecific' parameter.
     * @param unSpecific (unused, kept for API compatibility)
     */
    public void disableUnSpecific(boolean unSpecific) {
        paramMap.replace("unSpecific", false);
    }

    /**
     * Disables any parameter by name.
     * @param paramName the parameter name
     */
    public void disableParam(String paramName) {
        if (paramMap.containsKey(paramName)) {
            paramMap.replace(paramName, false);
        } else {
            System.out.println("param " + paramName + " not supported");
        }
    }

    /**
     * Disables a specific enzyme by its Enzyme object.
     * @param enzyme the enzyme to disable
     */
    public void disableEnzyme(Enzyme enzyme) {
        paramMap.replace(enzyme.getName(), false);
    }

    /**
     * Disables all fragment type parameters ([X]-[Y] combinations).
     */
    public void disableFragmentTypes() {
        for (int i = 0; i <= 2; i++) {
            for (int j = 3; j <= 5; j++) {
                paramMap.put("[" + i + "]-[" + j + "]", false);
            }
        }
        for (int i = 1; i <= 2; i++) {
            for (int j = 3; j <= 5; j++) {
                paramMap.put("[" + i + "]-[" + j + "]", false);
            }
        }
    }
}