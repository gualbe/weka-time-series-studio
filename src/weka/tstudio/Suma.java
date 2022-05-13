/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;

import java.util.ArrayList;
import java.util.List;
import weka.core.Attribute;
import weka.core.Instances;

/**
 *
 * @author manum
 */
public class Suma {

    double res = Double.NaN;

    public List sum(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) {
        List<Integer> listaSuma = new ArrayList();

        for (int m = 0; m < inst.size(); m++) {
            dataset.get(m).add(new ArrayList());

            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add("Suma;");

            int i = 0;
            while (i < inst.get(m).numAttributes()) {

                if (attName.equals(inst.get(m).attribute(i).name())) {
                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {
                        int index = inst.get(m).attribute(i).index();
                        int j = rango1;
                        int suma = 0;
                        while (j <= (rango2) && (k + j) < inst.get(m).numInstances()) {
                            suma = suma + Integer.parseInt(inst.get(m).get(k + j).toString(index));
                            System.out.println("Valor----> " + inst.get(m).get(k + j).toString(index));
                            System.out.println("Suma: " + suma);
                            j++;
                        }
                        dataset.get(m).get(finalPosition).add(suma + ";");
                    }

                }
                i++;
            }

        }

        for (int i = 0; i <= dataset.get(0).size() - 1; i++) {
            for (int j = 0; j < dataset.size(); j++) {

                System.out.print(dataset.get(j).get(i));

            }
            System.out.println("");
        }
        return dataset;
    }

    public double sum2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        double suma = 0;
        int i = (int) rango1;
        while (i <= (int) rango2 && (row + i) < inst.get(m).numInstances()) {
           // System.out.println("EEE-->" + inst.get(m).get(i + row).toString(indexAttribute));
            suma = suma + Double.parseDouble(inst.get(m).get(i + row).toString(indexAttribute));
            res = suma;
            i++;
        }
        return res;

    }
}
