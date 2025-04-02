/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.dataset.model;

import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;

/**
 *
 * @author yfa041
 */
public class ConfidentTagSorter implements Comparable<ConfidentTagSorter> {

    private final Double value;
    private final String title;
    private final Spectrum spectrum;

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public ConfidentTagSorter(double value, String title, Spectrum spectrum) {
        this.value = value;
        this.title = title;
        this.spectrum = spectrum;
    }

    @Override
    public int compareTo(ConfidentTagSorter o) {
        return this.value.compareTo(o.value);
    }

    public double getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

}
