/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.util;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileWriter;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.RecalibrationUtils;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.parameters.identification.IdentificationParameters;
import static eu.isas.peptideshaker.followup.RecalibrationExporter.getRecalibratedFileName;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import no.uib.probe.quicksearchprot.customclasses.CustRunMzDeviation;

/**
 *
 * @author yfa041
 */
public class SpectralRecalibrationUtilitiy {

    /**
     * Map of the runs errors.
     */
    private static final HashMap<String, CustRunMzDeviation> runMzDeviationMap = new HashMap<>();

    /**
     * Writes the recalibrated spectra in files named according to
     * getRecalibratedFileName in the given folder.
     *
     * @param recalibratePrecursors boolean indicating whether precursor ions
     * shall be recalibrated
     * @param recalibrateFragmentIons boolean indicating whether fragment ions
     * shall be recalibrated
     * @param folder folder where recalibrated files shall be written
     * @param matches
     * @param sequenceProvider the sequence provider
     * @param spectrumProvider the spectrum provider
     * @param identificationParameters the identification parameters
     * @return ArrayList files containing recalibrated spectra
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public static ArrayList<File> writeRecalibratedSpectra(
            boolean recalibratePrecursors,
            boolean recalibrateFragmentIons,
            File folder,
            ArrayList<SpectrumMatch> matches,
            SequenceProvider sequenceProvider,
            SpectrumProvider spectrumProvider,
            IdentificationParameters identificationParameters
    ) throws IOException {
        ArrayList<File> recalibratedSpectrums = new ArrayList<>();
        for (String fileNameWithoutExtension : spectrumProvider.getOrderedFileNamesWithoutExtensions()) {

            estimateErrors(
                    fileNameWithoutExtension,
                    matches,
                    sequenceProvider,
                    spectrumProvider,
                    identificationParameters
            );
            runMzDeviationMap.get(fileNameWithoutExtension);
            File file = new File(folder, getRecalibratedFileName(fileNameWithoutExtension + ".mgf"));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String spectrumTitle : spectrumProvider.getSpectrumTitles(fileNameWithoutExtension)) {
                    Spectrum recalibratedSpectrum = recalibrateSpectrum(
                            fileNameWithoutExtension,
                            spectrumTitle,
                            spectrumProvider,
                            recalibratePrecursors,
                            recalibrateFragmentIons
                    );
                    String recalibratedSpectrumAsMgf = MgfFileWriter.asMgf(spectrumTitle, recalibratedSpectrum);
                    writer.write(recalibratedSpectrumAsMgf);
                    writer.flush();
                }

                clearErrors(fileNameWithoutExtension);

            }

            recalibratedSpectrums.add(file);

        }

        return recalibratedSpectrums;

    }

    /**
     * Estimates the file m/z errors and displays the progress in a waiting
     * handler.Shall be done before calibration.The information generated can be
     * cleared from the mapping using clearErrors(String spectrumFileName).The
     * progress will only be updated, max value is the number of spectra
     *
     * @param spectrumFileNameWithoutExtension the name of the file of the run
     * @param matches
     * @param sequenceProvider the sequence provider
     * @param spectrumProvider the spectrum provider
     * @param identificationParameters the identification parameters
     */
    public static void estimateErrors(
            String spectrumFileNameWithoutExtension,
            ArrayList<SpectrumMatch> matches,
            SequenceProvider sequenceProvider,
            SpectrumProvider spectrumProvider,
            IdentificationParameters identificationParameters
    ) {

        CustRunMzDeviation fileErrors = new CustRunMzDeviation(
                spectrumFileNameWithoutExtension,
                matches,
                sequenceProvider,
                spectrumProvider,
                identificationParameters
        );

        runMzDeviationMap.put(spectrumFileNameWithoutExtension, fileErrors);

    }

    /**
     * Recalibrates a spectrum.
     *
     * @param fileName the name of the file where to find the spectrum
     * @param spectrumTitle the title of the spectrum
     * @param spectrumProvider the spectrum provider
     * @param recalibratePrecursor boolean indicating whether precursors shall
     * be recalibrated
     * @param recalibrateFragmentIons boolean indicating whether fragment ions
     * shall be recalibrated
     *
     * @return a recalibrated spectrum
     */
    private static Spectrum recalibrateSpectrum(
            String fileName,
            String spectrumTitle,
            SpectrumProvider spectrumProvider,
            boolean recalibratePrecursor,
            boolean recalibrateFragmentIons
    ) {

        CustRunMzDeviation runError = runMzDeviationMap.get(fileName);
        if (runError == null) {
            throw new IllegalArgumentException("No m/z deviation statistics found for spectrum file " + fileName + ".");
        }

        Spectrum originalSpectrum = spectrumProvider.getSpectrum(
                fileName,
                spectrumTitle
        );
        double mzCorrection = recalibratePrecursor ? runError.getPrecursorMzCorrection(
                originalSpectrum.precursor.mz,
                originalSpectrum.precursor.rt)
                : 0.0;

        Precursor newPrecursor = RecalibrationUtils.getRecalibratedPrecursor(originalSpectrum.precursor, mzCorrection, 0.0);

        double[] newFragmentMz = recalibrateFragmentIons
                ? runError.recalibrateFragmentMz(
                        originalSpectrum.precursor.rt,
                        originalSpectrum.mz
                )
                : originalSpectrum.mz;

        return new Spectrum(
                newPrecursor,
                newFragmentMz,
                originalSpectrum.intensity,
                originalSpectrum.getSpectrumLevel()
        );
    }

    /**
     * Clears the loaded error statistics for the given file name in order to
     * save memory.
     *
     * @param spectrumFileName the spectrum file name
     */
    private static void clearErrors(
            String spectrumFileName
    ) {
        runMzDeviationMap.remove(spectrumFileName);

    }

}
