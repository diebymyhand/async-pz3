import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class Task2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        File dir = getValidDirectory(scanner);

        System.out.println("\nStarting search using ForkJoinPool (Work Stealing)...");

        // використовуємо ForkJoinPool для паралельного обходу директорій
        ForkJoinPool pool = new ForkJoinPool();
        
        long startTime = System.nanoTime();
        
        // запускаємо кореневу задачу
        List<File> images = pool.invoke(new SearchImage(dir));
        
        long endTime = System.nanoTime();
        
        System.out.println("--------------------------------------------------");
        System.out.println("Search completed.");
        System.out.println("Total images found: " + images.size());
        System.out.printf("Execution time: %.4f ms%n", (endTime - startTime) / 1_000_000.0);
        System.out.println("--------------------------------------------------");

        // відкриваємо останнє зображення, якщо список не порожній
        if (!images.isEmpty()) {
            File lastImage = images.get(images.size() - 1);
            System.out.println("Opening last image: " + lastImage.getAbsolutePath());
            openFile(lastImage);
        } else {
            System.out.println("No images found in this directory.");
        }
        
        scanner.close();
    }

    // метод валідації введення директорії
    private static File getValidDirectory(Scanner scanner) {
        while (true) {
            System.out.print("Enter directory path (e.g., C:\\Photos or /home/user/images): ");
            String path = scanner.nextLine().trim();
            File dir = new File(path);

            if (dir.exists() && dir.isDirectory()) {
                return dir;
            } else {
                System.out.println("Error: Path does not exist or is not a directory. Try again.");
            }
        }
    }

    // метод відкриття файлу
    private static void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Error: Desktop API is not supported on this system.");
            }
        } catch (IOException e) {
            System.out.println("Error opening file: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Error: Current platform does not support the OPEN action.");
        }
    }
}