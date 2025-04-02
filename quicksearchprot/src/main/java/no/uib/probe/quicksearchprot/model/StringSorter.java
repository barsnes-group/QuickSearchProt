/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

import java.util.TreeSet;

/**
 *
 * @author yfa041
 */
public class StringSorter extends TreeSet<String> {

    @Override
    public String toString() {
        String value = "";
        for (String str : this) {
            value += str + "_";
        }

        return value; // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

}
