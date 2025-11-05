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
public class SelectedSearchParametersEntity {

    private boolean modifications = true;
    private boolean digestionType = false;
    private boolean digestion = false;
    private boolean enzyme = true;
    private boolean specificity = false;
    private boolean maxMissCleavages = true;
    private boolean fragmentIonTypes = true;

    private boolean precursorTolerance = true;
    private boolean fragmentTolerance = true;
    private boolean precursorCharge = true;
    private boolean isotops = true;

    private boolean sageAdvanced = true;
    private boolean xtandemAdvanced = true;

    public boolean isModifications() {
        return modifications;
    }

    public void setModifications(boolean modifications) {
        this.modifications = modifications;
    }

    public boolean isDigestion() {
        return digestion;
    }

    public void setDigestion(boolean digestion) {
        this.digestion = digestion;
    }

    public boolean isEnzyme() {
        return enzyme;
    }

    public void setEnzyme(boolean enzyme) {
        this.enzyme = enzyme;
    }

    public boolean isSpecificity() {
        return specificity;
    }

    public void setSpecificity(boolean specificity) {
        this.specificity = specificity;
    }

    public boolean isMaxMissCleavages() {
        return maxMissCleavages;
    }

    public void setMaxMissCleavages(boolean maxMissCleavages) {
        this.maxMissCleavages = maxMissCleavages;
    }

    public boolean isFragmentIonTypes() {
        return fragmentIonTypes;
    }

    public void setFragmentIonTypes(boolean fragmentIonTypes) {
        this.fragmentIonTypes = fragmentIonTypes;
    }

    public boolean isPrecursorTolerance() {
        return precursorTolerance;
    }

    public void setPrecursorTolerance(boolean precursorTolerance) {
        this.precursorTolerance = precursorTolerance;
    }

    public boolean isFragmentTolerance() {
        return fragmentTolerance;
    }

    public void setFragmentTolerance(boolean fragmentTolerance) {
        this.fragmentTolerance = fragmentTolerance;
    }

    public boolean isPrecursorCharge() {
        return precursorCharge;
    }

    public void setPrecursorCharge(boolean precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    public boolean isIsotops() {
        return isotops;
    }

    public void setIsotops(boolean isotops) {
        this.isotops = isotops;
    }

    public boolean isSageAdvanced() {
        return sageAdvanced;
    }

    public void setSageAdvanced(boolean sageAdvanced) {
        this.sageAdvanced = sageAdvanced;
    }

    public boolean isXtandemAdvanced() {
        return xtandemAdvanced;
    }

    public void setXtandemAdvanced(boolean xtandemAdvanced) {
        this.xtandemAdvanced = xtandemAdvanced;
    }

    public boolean isAtleastOneSelection() {
        return (modifications || digestion || enzyme || specificity || maxMissCleavages || fragmentIonTypes || precursorTolerance || fragmentTolerance || precursorCharge || isotops || xtandemAdvanced || sageAdvanced);

    }

    public boolean isDigestionType() {
        return digestionType;
    }

    public void setDigestionType(boolean digestionType) {
        this.digestionType = digestionType;
    }

}
