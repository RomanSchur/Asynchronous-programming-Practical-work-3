import java.util.Random;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

public class MatrixTask {
    public static int[][] generateMatrix(int rows, int cols, int min, int max) {
        Random random = new Random();
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(max - min + 1) + min;
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        if (matrix.length > 20 || matrix[0].length > 20) {
            System.out.println("[Матриця занадто велика для повного виводу]");
            return;
        }
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.printf("%4d ", val);
            }
            System.out.println();
        }
    }

    //WORK STEALING (Fork/Join)
    static class ColumnSumAction extends RecursiveAction {
        private static final int THRESHOLD = 50; //якщо стовпців менше цього числа, рахуємо в одному потоці
        private final int[][] matrix;
        private final int startCol;
        private final int endCol;
        private final long[] results; // Сюди записуємо суми стовпців

        public ColumnSumAction(int[][] matrix, int startCol, int endCol, long[] results) {
            this.matrix = matrix;
            this.startCol = startCol;
            this.endCol = endCol;
            this.results = results;
        }

        @Override
        protected void compute() {
            if ((endCol - startCol) <= THRESHOLD) {//якщо задача достатньо мала , то вона виконується прямо тут
                for (int j = startCol; j < endCol; j++) {
                    long sum = 0;
                    for (int i = 0; i < matrix.length; i++) {
                        sum += matrix[i][j];
                    }
                    results[j] = sum;
                }
            } else {//якщо ні - ділимо задачу навпіл

                int mid = startCol + (endCol - startCol) / 2;
                ColumnSumAction leftTask = new ColumnSumAction(matrix, startCol, mid, results);
                ColumnSumAction rightTask = new ColumnSumAction(matrix, mid, endCol, results);
                invokeAll(leftTask, rightTask); // запускаємо обидві частини
            }
        }
    }

    public static long[] solveWorkStealing(int[][] matrix) {
        ForkJoinPool pool = new ForkJoinPool(); // Створюємо пул
        long[] results = new long[matrix[0].length];
        ColumnSumAction task = new ColumnSumAction(matrix, 0, matrix[0].length, results);// Створюємо кореневу задачу для всіх стовпців (від 0 до останнього)
        pool.invoke(task);
        return results;
    }

    //WORK DEALING (ExecutorService)
    public static long[] solveWorkDealing(int[][] matrix) throws InterruptedException {
        int cols = matrix[0].length;
        int nThreads = Runtime.getRuntime().availableProcessors();// кількість потоків = кількості ядер процесора
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        long[] results = new long[cols];

        int chunkSize = (int) Math.ceil((double) cols / nThreads);// скільки стовпців бере один потік

        for (int i = 0; i < nThreads; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(start + chunkSize, cols);

            if (start < end) {
                executor.submit(() -> {
                    for (int j = start; j < end; j++) {
                        long sum = 0;
                        for (int r = 0; r < matrix.length; r++) {
                            sum += matrix[r][j]; // Сумуємо елементи стовпця
                        }
                        results[j] = sum;
                    }
                });
            }
        }
        // Чекаємо завершення всіх потоків
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        return results;
    }
}