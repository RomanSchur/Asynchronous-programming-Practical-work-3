import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FileSearch {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введіть шлях до директорії: ");
        String path = scanner.nextLine();
        File directory = new File(path);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Помилка: Директорія не існує або вказано файл замість папки.");
            return;
        }
        System.out.print("Введіть мінімальний розмір файлу (у байтах): ");
        while (!scanner.hasNextLong()) {
            System.out.println("Введіть коректне число.");
            scanner.next();
        }
        long sizeLimit = scanner.nextLong();

        System.out.println("\nПошук файлів...");
        ForkJoinPool pool = new ForkJoinPool();

        FileSizeTask task = new FileSizeTask(directory, sizeLimit); //коренева задача

        long start = System.nanoTime();
        int count = pool.invoke(task); //запуск задачі
        long end = System.nanoTime();

        System.out.println("------------------------------------------------");
        System.out.println("Знайдено файлів, більших за " + sizeLimit + " байт: " + count);
        System.out.printf("Час виконання: %.4f мс\n", (end - start) / 1_000_000.0);
    }
    //внутрішній клас для Fork/Join
    static class FileSizeTask extends RecursiveTask<Integer> {
        private final File dir;
        private final long limit;

        public FileSizeTask(File dir, long limit) {
            this.dir = dir;
            this.limit = limit;
        }
        @Override
        protected Integer compute() {
            int count = 0;
            List<FileSizeTask> subTasks = new ArrayList<>();

            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {//якщо це папка створюється нова задача
                        FileSizeTask subTask = new FileSizeTask(file, limit);
                        subTask.fork(); //асинхронний запуск
                        subTasks.add(subTask);
                    } else {
                        if (file.length() > limit) {//якщо це файл перевіряється умова (Work)
                            count++;
                        }
                    }
                }
            }
            for (FileSizeTask task : subTasks) {//результати з усіх підзадач
                count += task.join(); //очікування завершення і додавання результату
            }
            return count;
        }
    }
}