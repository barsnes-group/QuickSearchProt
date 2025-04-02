/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.search.sage;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yfa041
 */
public class SageParameterOrderSettings {

    public static List<String> Get_Sage_Parameters_List() {
        List<String> paramOrder = new ArrayList<>();
////       
////      
        paramOrder.add("DigestionParameter");
        paramOrder.add("FragmentIonTypesParameter");
        paramOrder.add("PrecursorChargeParameter");
        paramOrder.add("IsotopParameter");     
        paramOrder.add("PrecursorToleranceParameter");
        paramOrder.add("FragmentToleranceParameter");
        paramOrder.add("SageAdvancedParameter_A");  
        paramOrder.add("ModificationParameter");
        paramOrder.add("SageAdvancedParameter_B");
//  
        return paramOrder;
    }

    private boolean optAll = false;
    private boolean optSpectrumDynamicRange = false;
    private boolean optSpectrumNumbPeaks = false;
    private boolean optSpectrumMinimumFragment = false;
    private boolean optSpectrumMinimumPeaks = false;
    private boolean optNoiseSuppression = false;
    private boolean optMinimumPrecursorMass = false;
    private boolean optParentIsotopeExpansion = false;

    private double optSpectrumDynamicRangeValue;
    private int optSpectrumNumbPeaksValue;
    private double optSpectrumMinimumFragmentValue;
    private int optSpectrumMinimumPeaksValue;
    private boolean optNoiseSuppressionValue;
    private double optMinimumPrecursorMassValue;
    private boolean optParentIsotopeExpansionValue;

    private boolean optQuickAcetyl;
    private boolean optQuickPyrolidone;
    private boolean optstPBias;
    private boolean optQuickAcetylValue;
    private boolean optQuickPyrolidoneValue;
    private boolean optstPBiasValue;
    private boolean optstPTMComplexity;
    private double optstPTMComplexityValue;

    private boolean optUseRefine;
    private boolean optRefineUnanticipatedCleavage;
    private boolean optRefineSimiEnzymaticCleavage;
    private boolean optRefinePotintialModification;
    private boolean optRefinePointMutations;
    private boolean optRefineSnAPs;
    private boolean optRefineSpectrumSynthesis;

    private boolean optUseRefineValue;
    private boolean optRefineUnanticipatedCleavageValue;
    private boolean optRefineSimiEnzymaticCleavageValue;
    private boolean optRefinePotintialModificationValue;
    private boolean optRefinePointMutationsValue;
    private boolean optRefineSnAPsValue;
    private boolean optRefineSpectrumSynthesisValue;

    public boolean isOptUseRefine() {
        return optUseRefine;
    }

    public void setOptUseRefine(boolean optUseRefine) {
        this.optUseRefine = optUseRefine;
    }

    public boolean isOptRefineUnanticipatedCleavage() {
        return optRefineUnanticipatedCleavage || optAll;
    }

    public void setOptRefineUnanticipatedCleavage(boolean optRefineUnanticipatedCleavage) {
        this.optRefineUnanticipatedCleavage = optRefineUnanticipatedCleavage;
    }

    public boolean isOptRefineSimiEnzymaticCleavage() {
        return optRefineSimiEnzymaticCleavage || optAll;
    }

    public void setOptRefineSimiEnzymaticCleavage(boolean optRefineSimiEnzymaticCleavage) {
        this.optRefineSimiEnzymaticCleavage = optRefineSimiEnzymaticCleavage;
    }

    public boolean isOptRefinePotintialModification() {
        return optRefinePotintialModification || optAll;
    }

    public void setOptRefinePotintialModification(boolean optRefinePotintialModification) {
        this.optRefinePotintialModification = optRefinePotintialModification;
    }

    public boolean isOptRefinePointMutations() {
        return optRefinePointMutations || optAll;
    }

    public void setOptRefinePointMutations(boolean optRefinePointMutations) {
        this.optRefinePointMutations = optRefinePointMutations;
    }

    public boolean isOptRefineSnAPs() {
        return optRefineSnAPs || optAll;
    }

    public void setOptRefineSnAPs(boolean optRefineSnAPs) {
        this.optRefineSnAPs = optRefineSnAPs;
    }

    public boolean isOptRefineSpectrumSynthesis() {
        return optRefineSpectrumSynthesis || optAll;
    }

    public void setOptRefineSpectrumSynthesis(boolean optRefineSpectrumSynthesis) {
        this.optRefineSpectrumSynthesis = optRefineSpectrumSynthesis;
    }

    public boolean isOptUseRefineValue() {
        return optUseRefineValue;
    }

    public void setOptUseRefineValue(boolean optUseRefineValue) {
        this.optUseRefineValue = optUseRefineValue;
    }

    public boolean isOptRefineUnanticipatedCleavageValue() {
        return optRefineUnanticipatedCleavageValue;
    }

    public void setOptRefineUnanticipatedCleavageValue(boolean optRefineUnanticipatedCleavageValue) {
        this.optRefineUnanticipatedCleavageValue = optRefineUnanticipatedCleavageValue;
    }

    public boolean isOptRefineSimiEnzymaticCleavageValue() {
        return optRefineSimiEnzymaticCleavageValue;
    }

    public void setOptRefineSimiEnzymaticCleavageValue(boolean optRefineSimiEnzymaticCleavageValue) {
        this.optRefineSimiEnzymaticCleavageValue = optRefineSimiEnzymaticCleavageValue;
    }

    public boolean isOptRefinePotintialModificationValue() {
        return optRefinePotintialModificationValue;
    }

    public void setOptRefinePotintialModificationValue(boolean optRefinePotintialModificationValue) {
        this.optRefinePotintialModificationValue = optRefinePotintialModificationValue;
    }

    public boolean isOptRefinePointMutationsValue() {
        return optRefinePointMutationsValue;
    }

    public void setOptRefinePointMutationsValue(boolean optRefinePointMutationsValue) {
        this.optRefinePointMutationsValue = optRefinePointMutationsValue;
    }

    public boolean isOptRefineSnAPsValue() {
        return optRefineSnAPsValue;
    }

    public void setOptRefineSnAPsValue(boolean optRefineSnAPsValue) {
        this.optRefineSnAPsValue = optRefineSnAPsValue;
    }

    public boolean isOptRefineSpectrumSynthesisValue() {
        return optRefineSpectrumSynthesisValue || optAll;
    }

    public void setOptRefineSpectrumSynthesisValue(boolean optRefineSpectrumSynthesisValue) {
        this.optRefineSpectrumSynthesisValue = optRefineSpectrumSynthesisValue;
    }

    public double getOptSpectrumMinimumFragmentValue() {
        return optSpectrumMinimumFragmentValue;
    }

    public void setOptSpectrumMinimumFragmentValue(double optSpectrumMinimumFragmentValue) {
        this.optSpectrumMinimumFragmentValue = optSpectrumMinimumFragmentValue;
    }

    public int getOptSpectrumMinimumPeaksValue() {
        return optSpectrumMinimumPeaksValue;
    }

    public void setOptSpectrumMinimumPeaksValue(int optSpectrumMinimumPeaksValue) {
        this.optSpectrumMinimumPeaksValue = optSpectrumMinimumPeaksValue;
    }

    public double getOptMinimumPrecursorMassValue() {
        return optMinimumPrecursorMassValue;
    }

    public void setOptMinimumPrecursorMassValue(double optMinimumPrecursorMassValue) {
        this.optMinimumPrecursorMassValue = optMinimumPrecursorMassValue;
    }

    public void setOptSpectrumDynamicRangeValue(double optSpectrumDynamicRangeValue) {
        this.optSpectrumDynamicRangeValue = optSpectrumDynamicRangeValue;
    }

    public int getOptSpectrumNumbPeaksValue() {
        return optSpectrumNumbPeaksValue;
    }

    public void setOptSpectrumNumbPeaksValue(int optSpectrumNumbPeaksValue) {
        this.optSpectrumNumbPeaksValue = optSpectrumNumbPeaksValue;
    }

    public double getOptSpectrumDynamicRangeValue() {
        return optSpectrumDynamicRangeValue;
    }

    public boolean isOptAll() {
        return optAll;
    }

    public void setOptAll(boolean optAll) {
        this.optAll = optAll;
    }

    public boolean isOptSpectrumDynamicRange() {
        return optSpectrumDynamicRange || optAll;
    }

    public void setOptSpectrumDynamicRange(boolean optSpectrumDynamicRange) {
        this.optSpectrumDynamicRange = optSpectrumDynamicRange;
    }

    public boolean isOptSpectrumNumbPeaks() {
        return optSpectrumNumbPeaks || optAll;
    }

    public void setOptSpectrumNumbPeaks(boolean optSpectrumNumbPeaks) {
        this.optSpectrumNumbPeaks = optSpectrumNumbPeaks;
    }

    public boolean isOptSpectrumMinimumFragment() {
        return optSpectrumMinimumFragment || optAll;
    }

    public void setOptSpectrumMinimumFragment(boolean optSpectrumMinimumFragment) {
        this.optSpectrumMinimumFragment = optSpectrumMinimumFragment;
    }

    public boolean isOptSpectrumMinimumPeaks() {
        return optSpectrumMinimumPeaks || optAll;
    }

    public void setOptSpectrumMinimumPeaks(boolean optSpectrumMinimumPeaks) {
        this.optSpectrumMinimumPeaks = optSpectrumMinimumPeaks;
    }

    public boolean isOptNoiseSuppression() {
        return optNoiseSuppression || optAll;
    }

    public void setOptNoiseSuppression(boolean optNoiseSuppression) {
        this.optNoiseSuppression = optNoiseSuppression;
    }

    public boolean isOptMinimumPrecursorMass() {
        return optMinimumPrecursorMass || optAll;
    }

    public void setOptMinimumPrecursorMass(boolean optMinimumPrecursorMass) {
        this.optMinimumPrecursorMass = optMinimumPrecursorMass;
    }

    public boolean isOptParentIsotopeExpansion() {
        return optParentIsotopeExpansion || optAll;
    }

    public void setOptParentIsotopeExpansion(boolean optParentIsotopeExpansion) {
        this.optParentIsotopeExpansion = optParentIsotopeExpansion;
    }

    public boolean isOptNoiseSuppressionValue() {
        return optNoiseSuppressionValue;
    }

    public void setOptNoiseSuppressionValue(boolean optNoiseSuppressionValue) {
        this.optNoiseSuppressionValue = optNoiseSuppressionValue;
    }

    public boolean isOptParentIsotopeExpansionValue() {
        return optParentIsotopeExpansionValue;
    }

    public void setOptParentIsotopeExpansionValue(boolean optParentIsotopeExpansionValue) {
        this.optParentIsotopeExpansionValue = optParentIsotopeExpansionValue;
    }

    public boolean isOptQuickAcetyl() {
        return optQuickAcetyl || optAll;
    }

    public void setOptQuickAcetyl(boolean optQuickAcetyl) {
        this.optQuickAcetyl = optQuickAcetyl;
    }

    public boolean isOptQuickPyrolidone() {
        return optQuickPyrolidone || optAll;
    }

    public void setOptQuickPyrolidone(boolean optQuickPyrolidone) {
        this.optQuickPyrolidone = optQuickPyrolidone;
    }

    public boolean isOptstPBias() {
        return optstPBias || optAll;
    }

    public void setOptstPBias(boolean optstPBias) {
        this.optstPBias = optstPBias;
    }

    public boolean isOptQuickAcetylValue() {
        return optQuickAcetylValue;
    }

    public void setOptQuickAcetylValue(boolean optQuickAcetylValue) {
        this.optQuickAcetylValue = optQuickAcetylValue;
    }

    public boolean isOptQuickPyrolidoneValue() {
        return optQuickPyrolidoneValue;
    }

    public void setOptQuickPyrolidoneValue(boolean optQuickPyrolidoneValue) {
        this.optQuickPyrolidoneValue = optQuickPyrolidoneValue;
    }

    public boolean isOptstPBiasValue() {
        return optstPBiasValue || optAll;
    }

    public void setOptstPBiasValue(boolean optstPBiasValue) {
        this.optstPBiasValue = optstPBiasValue;
    }

    public boolean isOptstPTMComplexity() {
        return optstPTMComplexity || optAll;
    }

    public void setOptstPTMComplexity(boolean optstPTMComplexity) {
        this.optstPTMComplexity = optstPTMComplexity;
    }

    public double getOptstPTMComplexityValue() {
        return optstPTMComplexityValue;
    }

    public void setOptstPTMComplexityValue(double optstPTMComplexityValue) {
        this.optstPTMComplexityValue = optstPTMComplexityValue;
    }

}
