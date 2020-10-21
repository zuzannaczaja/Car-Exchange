package wyklad;

import java.util.ArrayList;
import java.util.Random;

public class GenethicAlgorithm {
    public static void main(String[] args){
        //byte [] osobniki = {0b01101, 0b11000, 0b01000, 0b10011};
        ArrayList<Byte> osobniki = new ArrayList<Byte>();
        Random rand = new Random();

        final int SIZE = 4;
        for(int i = 0; i < SIZE; i++){
            osobniki.add((byte)rand.nextInt(127));
            System.out.println("Wartosc = " + osobniki.get(i));
        }

        System.out.println();
        ArrayList<Float> ocena = new ArrayList<Float>();
        double suma = 0.0;
        for(int i = 0; i < SIZE; i++){
            ocena.add(-(float)Math.pow(osobniki.get(i), 2.0));
            suma += ocena.get(i);
            System.out.println("Ocena = " + ocena.get(i));
        }

        System.out.println();
        ArrayList<Double> p_reprodukcji = new ArrayList<Double>();
        for(int i = 0; i < SIZE; i++){
            p_reprodukcji.add(ocena.get(i) / suma);
            System.out.println("Pr = " + p_reprodukcji.get(i));
        }

        
    }
}
