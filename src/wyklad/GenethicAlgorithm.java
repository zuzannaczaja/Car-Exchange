package wyklad;

import java.util.ArrayList;
import java.util.Random;

public class GenethicAlgorithm {
    public static void main(String[] args){
        //byte [] osobniki = {0b01101, 0b11000, 0b01000, 0b10011};
        ArrayList<Byte> osobniki = new ArrayList<Byte>();
        Random rand = new Random();

        final int SIZE = 10;
        for(int i = 0; i < SIZE; i++){
            osobniki.add((byte)rand.nextInt(127));
            System.out.println("Wartosc = " + osobniki.get(i));
        }

        System.out.println();
        ArrayList<Float> ocena = new ArrayList<Float>();
        double suma = 0.0;
        for(int i = 0; i < SIZE; i++){
            ocena.add(-(float)Math.pow(osobniki.get(i), 2.0) + (128 + 128));
            suma += ocena.get(i);
            System.out.println("Ocena = " + ocena.get(i));
        }

        System.out.println();
        ArrayList<Double> p_reprodukcji = new ArrayList<Double>();
        ArrayList<Double> Pr_reprodukcji = new ArrayList<Double>();
        double suma2 = 0.0;
        for(int i = 0; i < SIZE; i++){
            p_reprodukcji.add(ocena.get(i) / suma);
            suma2 += p_reprodukcji.get(i);
            Pr_reprodukcji.add(suma2);
            System.out.println("Pr = " + p_reprodukcji.get(i) + " dystr Pr = " + Pr_reprodukcji.get(i));
        }

        for(int i = 0; i < SIZE; i++){
            float wartosc_losowa = rand.nextFloat();
            int j = 0;
            //System.out.println("wartosc losowa = " + wartosc_losowa);
            while (Pr_reprodukcji.get(j) < wartosc_losowa){
                j++;
                //System.out.println("J = " + j);
            }
            System.out.println("Osobnik " + osobniki.get(j));
        }

        for(int i = 0; i < SIZE; i++){
            //osobniki.add((byte)8);
            System.out.println("Wartosc = " + osobniki.get(i));
        }

        
    }
}
