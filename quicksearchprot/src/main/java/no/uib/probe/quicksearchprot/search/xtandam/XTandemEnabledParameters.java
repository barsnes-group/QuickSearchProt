/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.search.xtandam;

import no.uib.probe.quicksearchprot.configurations.SearchEngineParameterConfigurations;

/**
 *
 * @author yfa041
 */
public class XTandemEnabledParameters {

    private final SearchEngineParameterConfigurations paramsToOptimize;
    public XTandemEnabledParameters() {
        this.paramsToOptimize=new SearchEngineParameterConfigurations();
        this.paramsToOptimize.disableSpecificNTermOnlyEnzyme();
        this.paramsToOptimize.disableSpecificCTermOnlyEnzyme();
        this.paramsToOptimize.disableMinCharge();
        this.paramsToOptimize.disableMinIsotop();
        this.paramsToOptimize.disableMaxIsotop();
    }

    public SearchEngineParameterConfigurations getParamsToOptimize() {
        return paramsToOptimize;
    }
    
}
