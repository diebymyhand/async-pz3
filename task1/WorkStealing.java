import java.util.concurrent.RecursiveTask;

public class WorkStealing extends RecursiveTask<String> {
    // поріг, нижче якого задача виконується синхронно
    private static final int THRESHOLD = 100;
    
    private final int[][] matrix;
    private final int startRow;
    private final int endRow;

    public WorkStealing(int[][] matrix, int startRow, int endRow) {
        this.matrix = matrix;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    protected String compute() {
        // базовий випадок: якщо діапазон малий, шукаємо прямо
        if ((endRow - startRow) <= THRESHOLD) {
            return search(matrix, startRow, endRow);
        }

        // рекурсивний випадок: ділимо задачу навпіл
        int mid = startRow + (endRow - startRow) / 2;
        
        WorkStealing task1 = new WorkStealing(matrix, startRow, mid);
        WorkStealing task2 = new WorkStealing(matrix, mid, endRow);

        task1.fork(); // запускаємо першу частину асинхронно
        String res2 = task2.compute(); // виконуємо другу частину 
        String res1 = task1.join(); // чекаємо на результат першої

        // повертаємо перший знайдений результат
        if (res1 != null) {
            return res1;
        } 
        return res2;
    }

    // локальний метод пошуку
    private String search(int[][] matrix, int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == (i + j)) {
                    return String.format("Element found: %d at position [%d][%d]", matrix[i][j], i, j);
                }
            }
        }
        return null;
    }
}