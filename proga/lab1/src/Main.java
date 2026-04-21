import java.util.Random;


public class Main {
    public static long[] z  = new long[11];
    public static double[] x = new double[11];
    public static double[][] z1 = new double [6][11];
    public static Random rand = new Random();


    public static void main(String[] args) {
        for(int i = 0; i < 11; i++) {
            z[i] = (6 + i);
        }
        for(int i = 0; i < 11; i++) {
            x[i] = -2 + 10 * rand.nextDouble();
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 11; j++) {
                z1[i][j] = fillR(i, j);
            }
        }
        printOut();
    }
    public static double fillR(int i, int j){
        double res;
        if (z[i] == 5) {
            res = Math.log(Math.pow(Math.cos(Math.log(Math.pow(((5 + Math.abs(x[j])) / 3),x[j]))), 2));
        } else if (z[i] == 9 || z[i] == 13 || z[i] == 15) {
            res = Math.asin(Math.sin(Math.sin(Math.log(Math.abs(x[j])))));
        } else{
            res = Math.pow(1-Math.log(Math.pow(Math.tan(Math.pow(Math.sin(x[j]),(Math.pow(((1/4 -x[j]) / x[j]), 2)-1)/ Math.PI)), 2)) / 2, 3);
        }
        return res;
    }
    public static void printOut(){
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 11; j++) {
                System.out.printf("%10.5f ", z1[i][j]);
            }
            System.out.println();
        }
    }
}