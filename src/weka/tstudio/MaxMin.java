/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import weka.core.Attribute;
import weka.core.Instances;

/**
 *
 * @author manum
 */
public class MaxMin {
double res=Double.NaN;
    public List max(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) throws ParseException {
        List listaMax = new ArrayList();
        for (int m = 0; m < inst.size(); m++) {
            dataset.get(m).add(new ArrayList());

            int i = 0;
            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add("Max;");
            while (i < inst.get(m).numAttributes()) {
                if (attName.equals(inst.get(m).attribute(i).name())) {

                    String type = Attribute.typeToString(inst.get(m).attribute(i));

                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {

                        int index = inst.get(m).attribute(i).index();

                        if (type == "date") {
                            Date fecha = new Date();

                            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                            Date max = formato.parse(inst.get(m).get(rango1).toString(index));
                            int j = rango1;
                            while (j <= (rango2) && (k + j) < inst.get(m).numInstances()) {
                                fecha = formato.parse(inst.get(m).get(k + j).toString(index));
                                if (fecha.before(max)) {
                                    max = fecha;
                                }
                                j++;
                            }
                            dataset.get(m).get(finalPosition).add(max + ";");

                            listaMax.add(formato.format(max));

                        } else {
                            if ((k + rango1) < inst.get(m).numInstances()) {
                                int max = Integer.parseInt(inst.get(m).get(k + rango1).toString(index));

                                int j = rango1;
                                while (j <= (rango2) && (k + j) < inst.get(m).numInstances()) {
                                    if (Integer.parseInt(inst.get(m).get(k + j).toString(index)) > max) {
                                        max = Integer.parseInt(inst.get(m).get(k + j).toString(index));
                                    }
                                    j++;
                                }

                                listaMax.add(max);
                                dataset.get(m).get(finalPosition).add(max + ";");
                            }
                        }
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

    public List min(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) {
        List<Integer> listaMin = new ArrayList();
        for (int m = 0; m < inst.size(); m++) {
            int i = 0;
            dataset.get(m).add(new ArrayList());
            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add("Min;");
            while (i < inst.get(m).numAttributes()) {
                if (attName.equals(inst.get(m).attribute(i).name())) {
                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {
                        int index = inst.get(m).attribute(i).index();
                        if ((k + rango1) < inst.get(m).numInstances()) {
                            int min = Integer.parseInt(inst.get(m).get(k + rango1).toString(index));
                            int j = rango1;
                            while (j <= (rango2) && (k + j) < inst.get(m).numInstances()) {
                                if (Integer.parseInt(inst.get(m).get(k + j).toString(index)) < min) {
                                    min = Integer.parseInt(inst.get(m).get(k + j).toString(index));
                                }
                                j++;
                            }
                            listaMin.add(min);
                            dataset.get(m).get(finalPosition).add(min + ";");
                        }
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

    public double min2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        double min = Double.parseDouble(inst.get(m).get((int) (rango1 + row)).toString(indexAttribute));
        int i = (int) rango1;
            while (i <= (rango2) && (row + i) < inst.get(m).numInstances()) {
            if (Integer.parseInt(inst.get(m).get(i+row).toString(indexAttribute)) < min) {
                System.out.println("EEE-->" + inst.get(m).get(i + row).toString(indexAttribute));
                min = Integer.parseInt(inst.get(m).get(i + row).toString(indexAttribute));
                res = min;
            }
            else{
                res = min;
            }
            i++;
        }
        return res;
    }
    
    public double max2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        System.out.println("ENTRA EN MAX2");
        System.out.println("Rango1--> "+rango1);
        System.out.println(Double.parseDouble(inst.get(m).get((int) (rango1 + row)).toString(indexAttribute)));
        double max = Double.parseDouble(inst.get(m).get((int) (rango1 + row)).toString(indexAttribute));
        System.out.println("max--> "+max);
        int i = (int) rango1;
        while(i <= (int) rango2 && (row + i) < inst.get(m).numInstances()){
            if (Integer.parseInt(inst.get(m).get(i+row).toString(indexAttribute)) > max) {
                System.out.println("EEE-->" + inst.get(m).get(i + row).toString(indexAttribute));
                max = Integer.parseInt(inst.get(m).get(i + row).toString(indexAttribute));
                res = max;
            }
            else{
                res = max;
            }
            i++;
        }
        return res;
    }
}
