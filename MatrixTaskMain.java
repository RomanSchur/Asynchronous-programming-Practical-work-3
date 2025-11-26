import java.util.Scanner;
public class MatrixTaskMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int rows = getInt(scanner, "Введіть кількість рядків: ");
        int cols = getInt(scanner, "Введіть кількість стовпців: ");
        int min = getInt(scanner, "Min значення елемента: ");
        int max = getInt(scanner, "Max значення елемента: ");

        System.out.println("Генерація матриці...");
        int[][] matrix = MatrixTask.generateMatrix(rows, cols, min, max);
        MatrixTask.printMatrix(matrix);
        System.out.println("------------------------------------------------");

        //Запуск Work Stealing (Fork/Join)
        System.out.println("Запуск Work Stealing (Fork/Join)...");
        long startWS = System.nanoTime();

        long[] resWS = MatrixTask.solveWorkStealing(matrix);

        long endWS = System.nanoTime();
        double timeWS = (endWS - startWS) / 1_000_000.0;

        System.out.print("Результат WS: ");
        printShortResult(resWS);
        System.out.printf("Час виконання: %.4f мс\n", timeWS);
        System.out.println("------------------------------------------------");

        //Запуск Work Dealing (ExecutorService)
        System.out.println("Запуск Work Dealing (ExecutorService)...");
        try {
            long startWD = System.nanoTime();

            long[] resWD = MatrixTask.solveWorkDealing(matrix);

            long endWD = System.nanoTime();
            double timeWD = (endWD - startWD) / 1_000_000.0;

            System.out.print("Результат WD: ");
            printShortResult(resWD);
            System.out.printf("Час виконання: %.4f мс\n", timeWD);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static int getInt(Scanner sc, String msg) {
        System.out.print(msg);
        while (!sc.hasNextInt()) {
            System.out.println("Це не число. Спробуйте ще раз.");
            sc.next();
            System.out.print(msg);
        }
        return sc.nextInt();
    }
    private static void printShortResult(long[] arr) {
        if (arr.length <= 10) {
            System.out.print("[ ");
            for (long v : arr) System.out.print(v + " ");
            System.out.println("]");
        } else {
            System.out.println("[ " + arr[0] + ", " + arr[1] + " ... " + arr[arr.length-1] + " ] (всього " + arr.length + ")");
        }
    }
}