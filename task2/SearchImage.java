import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

// використовуємо RecursiveTask, оскільки нам потрібно повернути результат,
// реалізація через Work Stealing ідеально підходить для нерівномірних структур директорій
public class SearchImage extends RecursiveTask<List<File>> {

    private final File directory;

    public SearchImage(File directory) {
        this.directory = directory;
    }

    @Override
    protected List<File> compute() {
        List<File> images = new ArrayList<>();
        List<SearchImage> subTasks = new ArrayList<>();

        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // якщо це директорія, створюємо нову підзадачу Fork
                    // ForkJoinPool керуватиме цими задачами через механізм Work Stealing
                    SearchImage task = new SearchImage(file);
                    task.fork(); // додаємо задачу в чергу
                    subTasks.add(task);
                } else {
                    // перевіряємо чи є файл зображенням
                    if (isImage(file)) {
                        images.add(file);
                    }
                }
            }
        }

        // чекаємо завершення всіх підзадач і робимо Join
        for (SearchImage task : subTasks) {
            images.addAll(task.join());
        }

        return images;
    }

    // метод для перевірки розширення файлу
    private boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || 
               name.endsWith(".png") || 
               name.endsWith(".gif") || 
               name.endsWith(".bmp") ||
               name.endsWith(".webp");
    }
}
