package no.uib.probe.quicksearchprot.model;

/*
 * OptimisedSearchResults.java
 * 
 * Licensed under the Apache License, Version 2.0.
 * See the license in the project root for license information.
 *
 * This class is a Java bean for storing optimized search results relevant to proteomics searches.
 * It encapsulates search parameters such as enzyme, modifications, ion types, and tolerance values.
 *
 * 
 */
import com.compomics.util.parameters.identification.search.DigestionParameters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Encapsulates the parameters and results from an optimized protein search.
 * This class includes settings for digestion, modifications, ion types, and
 * mass tolerances.
 * @author Yehia Mokhtar Farag
 */
public class OptimisedSearchResults {

    // Digestion parameters
    /**
     * The digestion parameter, initialized with the enzyme cleavage parameter
     * name.
     */
    private String digestionParameter = DigestionParameters.CleavageParameter.enzyme.name();

    /**
     * Name of the enzyme used for digestion.
     */
    private String enzymeName;

    /**
     * The specificity of the enzyme.
     */
    private String enzymeSpecificity;

    /**
     * The maximal number of missed cleavages.
     */
    private int maxMissedCleavage;

    // Precursor and fragment tolerances
    /**
     * Precursor mass tolerance (typically in Da or ppm).
     */
    private double precursorTolerance;

    /**
     * Fragment mass tolerance (typically in Da).
     */
    private double fragmentTolerance;

    // Precursor charge and isotope distribution
    /**
     * Maximal charge for precursor ions.
     */
    private int maxPrecursorCharge;

    /**
     * Minimal charge for precursor ions.
     */
    private int minPrecursorCharge;

    /**
     * Maximal number of isotopes considered.
     */
    private int maxIsotops;

    /**
     * Minimal number of isotopes considered.
     */
    private int minIsotops;

    // Modifications
    /**
     * Sorted mapping of variable modifications based on (usually) their
     * position or score.
     */
    private TreeMap<Integer, ArrayList<String>> sortedVariableModificationsMap;

    /**
     * Set of refined variable modifications selected by downstream processes.
     */
    private Set<String> refinedVariableModifications;

    /**
     * Set of refined fixed modifications selected by downstream processes.
     */
    private Set<String> refinedFixedModifications;

    /**
     * List of expected variable modifications with their corresponding values
     * (mass delta, etc.).
     */
    private final Map<String, Double> variableModifications = new LinkedHashMap<>(0);

    /**
     * List of the expected fixed modifications.
     */
    private final ArrayList<String> fixedModifications = new ArrayList<>(0);

    // Ion selections
    /**
     * The list of selected forward (N-terminal) ions.
     */
    private ArrayList<Integer> selectedForwardIons;

    /**
     * The list of selected rewind (C-terminal) ions.
     */
    private ArrayList<Integer> selectedRewindIons;

    // Accessors and mutators (getters/setters)
    // --- Digestion and enzyme ---
    public String getDigestionParameter() {
        return digestionParameter;
    }

    public void setDigestionParameter(String digestionParameter) {
        this.digestionParameter = digestionParameter;
    }

    public String getEnzymeName() {
        return enzymeName;
    }

    public void setEnzymeName(String enzymeName) {
        this.enzymeName = enzymeName;
    }

    public String getEnzymeSpecificity() {
        return enzymeSpecificity;
    }

    public void setEnzymeSpecificity(String enzymeSpecificity) {
        this.enzymeSpecificity = enzymeSpecificity;
    }

    public int getMaxMissedCleavage() {
        return maxMissedCleavage;
    }

    public void setMaxMissedCleavage(int maxMissedCleavage) {
        this.maxMissedCleavage = maxMissedCleavage;
    }

    // --- Tolerances ---
    public double getPrecursorTolerance() {
        return precursorTolerance;
    }

    public void setPrecursorTolerance(double precursorTolerance) {
        this.precursorTolerance = precursorTolerance;
    }

    public double getFragmentTolerance() {
        return fragmentTolerance;
    }

    public void setFragmentTolerance(double fragmentTolerance) {
        this.fragmentTolerance = fragmentTolerance;
    }

    // --- Precursor charge and isotopes ---
    public int getMaxPrecursorCharge() {
        return maxPrecursorCharge;
    }

    public void setMaxPrecursorCharge(int maxPrecursorCharge) {
        this.maxPrecursorCharge = maxPrecursorCharge;
    }

    public int getMinPrecursorCharge() {
        return minPrecursorCharge;
    }

    public void setMinPrecursorCharge(int minPrecursorCharge) {
        this.minPrecursorCharge = minPrecursorCharge;
    }

    public int getMaxIsotops() {
        return maxIsotops;
    }

    public void setMaxIsotops(int maxIsotops) {
        this.maxIsotops = maxIsotops;
    }

    public int getMinIsotops() {
        return minIsotops;
    }

    public void setMinIsotops(int minIsotops) {
        this.minIsotops = minIsotops;
    }

    // --- Modifications ---
    /**
     * Gets the mapping of sorted variable modifications.
     *
     * @return sorted variable modifications map.
     */
    public TreeMap<Integer, ArrayList<String>> getSortedVariableModificationsMap() {
        return sortedVariableModificationsMap;
    }

    /**
     * Sets the mapping of sorted variable modifications.
     *
     * @param sortedVariableModificationsMap the sorted variable modifications.
     */
    public void setSortedVariableModificationsMap(TreeMap<Integer, ArrayList<String>> sortedVariableModificationsMap) {
        this.sortedVariableModificationsMap = sortedVariableModificationsMap;
    }

    /**
     * Gets the set of refined variable modifications.
     *
     * @return
     */
    public Set<String> getRefinedVariableModifications() {
        return refinedVariableModifications;
    }

    /**
     * Sets the set of refined variable modifications.
     *
     * @param refinedVariableModifications
     */
    public void setRefinedVariableModifications(Set<String> refinedVariableModifications) {
        this.refinedVariableModifications = refinedVariableModifications;
    }

    /**
     * Gets the set of refined fixed modifications.
     *
     * @return
     */
    public Set<String> getRefinedFixedModifications() {
        return refinedFixedModifications;
    }

    /**
     * Sets the set of refined fixed modifications.
     *
     * @param refinedFixedModifications
     */
    public void setRefinedFixedModifications(Set<String> refinedFixedModifications) {
        this.refinedFixedModifications = refinedFixedModifications;
    }

    /**
     * Gets the variable modifications (name to delta mass or value).
     *
     * @return
     */
    public Map<String, Double> getVariableModifications() {
        return variableModifications;
    }

    /**
     * Adds a variable modification if not already present.
     *
     * @param variableModification
     * @param value
     */
    public void addVariableModification(String variableModification, double value) {
        if (!variableModifications.containsKey(variableModification)) {
            this.variableModifications.put(variableModification, value);
        }
    }

    /**
     * Gets the list of fixed modifications.
     *
     * @return
     */
    public ArrayList<String> getFixedModifications() {
        return fixedModifications;
    }

    /**
     * Adds a fixed modification if not already present.
     *
     * @param fixedModification
     */
    public void addFixedModification(String fixedModification) {
        if (!fixedModifications.contains(fixedModification)) {
            this.fixedModifications.add(fixedModification);
        }
    }

    // --- Ion selections ---
    /**
     * Gets selected forward ion series.
     *
     * @return
     */
    public ArrayList<Integer> getSelectedForwardIons() {
        return selectedForwardIons;
    }

    /**
     * Sets selected forward ion series.
     *
     * @param selectedForwardIons
     */
    public void setSelectedForwardIons(ArrayList<Integer> selectedForwardIons) {
        this.selectedForwardIons = selectedForwardIons;
    }

    /**
     * Gets selected rewind (reverse, C-terminal) ion series.
     *
     * @return
     */
    public ArrayList<Integer> getSelectedRewindIons() {
        return selectedRewindIons;
    }

    /**
     * Sets selected rewind (reverse, C-terminal) ion series.
     *
     * @param selectedRewindIons
     */
    public void setSelectedRewindIons(ArrayList<Integer> selectedRewindIons) {
        this.selectedRewindIons = selectedRewindIons;
    }
}
