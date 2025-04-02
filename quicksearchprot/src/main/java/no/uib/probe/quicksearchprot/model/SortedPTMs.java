/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

/**
 *
 * @author yfa041
 */
public class SortedPTMs implements Comparable<SortedPTMs> {

    private final String name;
    private final double score;
    private final double targetAAFrequency;
    private boolean sortBasedOnScore = true;

    public SortedPTMs(String name, double score, double targetAAFrequency) {
        this.name = name;
        this.score = score;
        this.targetAAFrequency = targetAAFrequency;
    }

    public double getTargetAAFrequency() {
        return targetAAFrequency;
    }

    @Override
    public int compareTo(SortedPTMs o) {
        if (sortBasedOnScore) {
            return Double.compare(score, o.score);
        } else {
            System.out.println("sort updated");
            return Double.compare(targetAAFrequency,o.targetAAFrequency);
        }
    }

    public void setSortBasedOnScore(boolean sortBasedOnScore) {
        this.sortBasedOnScore = sortBasedOnScore;
    }

    @Override
    public String toString() {
        return name + "  " + score;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

}
