/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.search.myrimatch;

import no.uib.probe.quicksearchprot.configurations.SearchEngineParameterConfigurations;

/**
 *
 * @author yfa041
 */
public class MyriMatchEnabledParameters {

    private final SearchEngineParameterConfigurations paramsToOptimize;
    public MyriMatchEnabledParameters() {
        this.paramsToOptimize=new SearchEngineParameterConfigurations();
        this.paramsToOptimize.disableFragmentTypes();
        this.paramsToOptimize.disableSpecificCTermOnlyEnzyme();
        this.paramsToOptimize.disableSpecificNTermOnlyEnzyme();
        this.paramsToOptimize.disableMinCharge();
//        this.paramsToOptimize.disableMinIsotop();
//        this.paramsToOptimize.disableMaxIsotop();
this.paramsToOptimize.disableParam("specificNTermOnly");
this.paramsToOptimize.disableParam("specificCTermOnly");
    }

    public SearchEngineParameterConfigurations getParamsToOptimize() {
        return paramsToOptimize;
    }
    
}
