import java.util.concurrent.Callable;

public class WorkDealing implements Callable<String> {
    
    private final int[][] matrix;
    private final int startRow;
    private final int endRow;

    public WorkDealing(int[][] matrix, int startRow, int endRow) {
        this.matrix = matrix;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public String call() {
        return search(matrix, startRow, endRow);
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