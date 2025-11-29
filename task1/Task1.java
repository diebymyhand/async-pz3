import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Task1 {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // отримання коректних даних від користувача
        int rows = getValidInt("Enter number of rows: ", 1, 50000);
        int cols = getValidInt("Enter number of columns: ", 1, 50000);
        
        System.out.println("Max sum of indices = " + (rows + cols));
        int minVal = getValidInt("Enter min random value: ", -1000, Integer.MAX_VALUE);
        int maxVal = getValidInt("Enter max random value: ", minVal, Integer.MAX_VALUE);

        // генерація матриці
        System.out.println("\nGenerating matrix...");
        int[][] matrix = generateMatrix(rows, cols, minVal, maxVal);
        System.out.println("Matrix generated.");
        
        if (rows <= 20 && cols <= 20) {
            printMatrix(matrix);
        }

        System.out.println("\n--- Starting tests ---");

        // запуск підходу Work Stealing
        runWorkStealing(matrix);

        // запуск підходу Work Dealing
        runWorkDealing(matrix);
        
        scanner.close();
    }

    // Work Stealing (Fork/Join Framework)
    private static void runWorkStealing(int[][] matrix) {
        System.out.println("\n[Work Stealing] Running via ForkJoinPool...");
        ForkJoinPool fjp = new ForkJoinPool();
        
        long startTime = System.nanoTime();
        
        // викликаємо зовнішній клас WorkStealing
        String result = fjp.invoke(new WorkStealing(matrix, 0, matrix.length));
        
        long endTime = System.nanoTime();
        printResult("Work Stealing", result, endTime - startTime);
        
        fjp.shutdown(); 
    }

    // Work Dealing (ExecutorService & ThreadPool)
    private static void runWorkDealing(int[][] matrix) {
        System.out.println("\n[Work Dealing] Running via FixedThreadPool...");
        
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        List<Callable<String>> tasks = new ArrayList<>();

        // ділимо роботу на рівні шматки
        int rows = matrix.length;
        int chunkSize = (int) Math.ceil((double) rows / cores);

        for (int i = 0; i < cores; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, rows);
            
            if (start < end) {
                tasks.add(new WorkDealing(matrix, start, end));
            }
        }

        long startTime = System.nanoTime();
        String result = null;

        try {
            List<Future<String>> futures = executor.invokeAll(tasks);
            
            for (Future<String> future : futures) {
                try {
                    String res = future.get(); 
                    if (res != null) {
                        result = res;
                        break; 
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        long endTime = System.nanoTime();
        printResult("Work Dealing", result, endTime - startTime);
    }

    // перевіряємо коректність введених даних 
    private static int getValidInt(String prompt, int min, int max) {
        int input;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Error: Number must be in range [" + min + " - " + max + "]");
                }
            } else {
                System.out.println("Error: Please enter an integer.");
                scanner.next(); 
            }
        }
    }

    private static int[][] generateMatrix(int rows, int cols, int min, int max) {
        int[][] matrix = new int[rows][cols];
        
        // виокристовуємо ThreadLocalRandom замість звичайного Random, 
        // бо він уникає конкуренції у багатопотокових середовищах, що робить його швидшим
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(min, max + 1);
            }
        }
        return matrix;
    }

    private static void printResult(String method, String result, long durationNs) {
        double durationMs = durationNs / 1_000_000.0;
        System.out.println("--------------------------------------------------");
        System.out.println("Method: " + method);
        System.out.println("Result: " + (result != null ? result : "Not found"));
        System.out.printf("Execution time: %.4f ms%n", durationMs);
        System.out.println("--------------------------------------------------");
    }

    private static void printMatrix(int[][] matrix) {
        System.out.println("Matrix:");
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.printf("%4d ", val);
            }
            System.out.println();
        }
    }
}