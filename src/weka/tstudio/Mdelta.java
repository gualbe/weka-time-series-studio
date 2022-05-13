/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;

import java.util.ArrayList;
import java.util.List;
import weka.core.Instances;

/**
 *
 * @author manum
 */
public class Mdelta {

    public List mDelta(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) {
        List<Double> listaMedia = new ArrayList();

        for (int m = 0; m < inst.size(); m++) {

            int i = 0;
            double media;
            dataset.get(m).add(new ArrayList());
            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add("MDELTA;");
            while (i < inst.get(m).numAttributes()) {
                if (attName.equals(inst.get(m).attribute(i).name())) {
                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {
                        int index = inst.get(m).attribute(i).index();
                        int j = rango1;
                        double diferencias = 0;
                        double suma = 0;

                        while (j < (rango2) && (k + j) < inst.get(m).numInstances()) {
                            if ((k + j + 1) < inst.get(m).numInstances()) {
                                diferencias = Math.abs(Integer.parseInt(inst.get(m).get(k + j).toString(index)) - Integer.parseInt(inst.get(m).get(k + j + 1).toString(index)));
                                suma = suma + diferencias;
                                // System.out.println("Suma: " + suma);
                            }
                            j++;
                        }
                        media = suma / (Math.abs(rango1 - rango2));
                        // System.out.println("Media ---> suma =  " + suma + "/nº valores= " + (Math.abs(rango1 - rango2)) + "|||| Resultado = " + media);

                        listaMedia.add(media);
                        dataset.get(m).get(finalPosition).add(media + ";");

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

    public double mdelta2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        double suma = 0;
        double mean = 0;
        double res;
        double diferencias = 0;
        double j = rango1;
        while (j < (rango2) && (row + j) < inst.get(m).numInstances()) {
            if ((row + j + 1) < inst.get(m).numInstances()) {
                diferencias = Math.abs(Integer.parseInt(inst.get(m).get((int) (row + j)).toString(indexAttribute)) - Integer.parseInt(inst.get(m).get((int) (row + j + 1)).toString(indexAttribute)));
                suma = suma + diferencias;
                // System.out.println("Suma: " + suma);
            }
            j++;
        }
        mean = suma / (Math.abs(rango1 - rango2));
        res = mean;
        return res;

    }
}
