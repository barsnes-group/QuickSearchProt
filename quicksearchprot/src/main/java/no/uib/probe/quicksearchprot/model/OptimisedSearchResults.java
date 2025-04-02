/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

import com.compomics.util.parameters.identification.search.DigestionParameters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class is a java bean class to store optimized search results
 *
 * @author Yehia Mokhtar Farag
 */
public class OptimisedSearchResults {

    private String digestionParameter=DigestionParameters.CleavageParameter.enzyme.name();
    private String enzymeName;
    private String enzymeSpecificity;
    private int maxMissedCleavage;
    private double precursorTolerance;
    private double fragmentTolerance;
    private int maxPrecursorCharge;
    private int minPrecursorCharge;
    private int maxIsotops;
    private int minIsotops;
    private TreeMap<Integer,ArrayList<String>>sortedVariableModificationsMap;
    private Set<String>refinedVariableModifications;
    private Set<String>refinedFixedModifications;

    public Set<String> getRefinedVariableModifications() {
        return refinedVariableModifications;
    }

    public void setRefinedVariableModifications(Set<String> refinedVariableModifications) {
        this.refinedVariableModifications = refinedVariableModifications;
    }

    public Set<String> getRefinedFixedModifications() {
        return refinedFixedModifications;
    }

    public void setRefinedFixedModifications(Set<String> refinedFixedModifications) {
        this.refinedFixedModifications = refinedFixedModifications;
    }

    public TreeMap<Integer, ArrayList<String>> getSortedVariableModificationsMap() {
        return sortedVariableModificationsMap;
    }

    public void setSortedVariableModificationsMap(TreeMap<Integer, ArrayList<String>> sortedVariableModificationsMap) {
        this.sortedVariableModificationsMap = sortedVariableModificationsMap;
    }
    /**
     * List of the expected variable modifications.
     */
    private final Map<String, Double> variableModifications = new LinkedHashMap<>(0);
    /**
     * List of the expected fixed modifications.
     */
    private final ArrayList<String> fixedModifications = new ArrayList<>(0);

    public void addVariableModifications(String variableModification, double value) {
        if (!variableModifications.containsKey(variableModification)) {
            this.variableModifications.put(variableModification, value);
        }
    }

    public void addFixedModifications(String fixedModification) {
        if (!fixedModifications.contains(fixedModification)) {
            this.fixedModifications.add(fixedModification);
        }
    }

    public ArrayList<String> getFixedModifications() {
        return fixedModifications;
    }

    public double getFragmentTolerance() {
        return fragmentTolerance;
    }

    public void setFragmentTolerance(double fragmentTolerance) {
        this.fragmentTolerance = fragmentTolerance;
    }

    public double getPrecursorTolerance() {
        return precursorTolerance;
    }

    public void setPrecursorTolerance(double precursorTolerance) {
        this.precursorTolerance = precursorTolerance;
    }
    private ArrayList<Integer> selectedForwardIons;
    private ArrayList<Integer> selectedRewindIons;

    public ArrayList<Integer> getSelectedForwardIons() {
        return selectedForwardIons;
    }

    public void setSelectedForwardIons(ArrayList<Integer> selectedForwardIons) {
        this.selectedForwardIons = selectedForwardIons;
    }

    public ArrayList<Integer> getSelectedRewindIons() {
        return selectedRewindIons;
    }

    public void setSelectedRewindIons(ArrayList<Integer> selectedRewindIons) {
        this.selectedRewindIons = selectedRewindIons;
    }

    public int getMaxMissedCleavage() {
        return maxMissedCleavage;
    }

    public void setMaxMissedCleavage(int maxMissedCleavage) {
        this.maxMissedCleavage = maxMissedCleavage;
    }

    public String getEnzymeSpecificity() {
        return enzymeSpecificity;
    }

    public void setEnzymeSpecificity(String enzymeSpecificity) {
        this.enzymeSpecificity = enzymeSpecificity;
    }

    public String getEnzymeName() {
        return enzymeName;
    }

    public void setEnzymeName(String enzymeName) {
        this.enzymeName = enzymeName;
    }

    public String getDigestionParameter() {
        return digestionParameter;
    }

    public void setDigestionParameter(String cleavageParameters) {
        this.digestionParameter = cleavageParameters;
    }

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

    public  Map<String, Double> getVariableModifications() {
        return variableModifications;
    }
}
