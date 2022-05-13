/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;

import java.util.List;
import weka.core.Instances;

/**
 *
 * @author manum
 */
public class Shift {

    public double shift(List<Instances> inst, String attName, double rango1, int row, int indexAttribute, int m) {
        System.out.println("shift: "+rango1);
        double res;
        double valor;
        res = Double.parseDouble(inst.get(m).get(row+(int)rango1).toString(indexAttribute));
        return res;
}
}
