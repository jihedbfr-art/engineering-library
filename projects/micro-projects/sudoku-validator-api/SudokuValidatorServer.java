import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class SudokuValidatorServer {
    static boolean isValid(int[][] grid) {
        for (int i = 0; i < 9; i++) {
            if (!isUnique(grid[i])) return false;
            int[] col = new int[9];
            for (int j = 0; j < 9; j++) col[j] = grid[j][i];
            if (!isUnique(col)) return false;
        }
        for (int br = 0; br < 9; br += 3) {
            for (int bc = 0; bc < 9; bc += 3) {
                int[] box = new int[9];
                int k = 0;
                for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) box[k++] = grid[br + i][bc + j];
                if (!isUnique(box)) return false;
            }
        }
        return true;
    }

    static boolean isUnique(int[] values) {
        boolean[] seen = new boolean[10];
        for (int v : values) {
            if (v == 0) continue;
            if (seen[v]) return false;
            seen[v] = true;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        int[][] sample = {
            {5,3,0,0,7,0,0,0,0},{6,0,0,1,9,5,0,0,0},{0,9,8,0,0,0,0,6,0},
            {8,0,0,0,6,0,0,0,3},{4,0,0,8,0,3,0,0,1},{7,0,0,0,2,0,0,0,6},
            {0,6,0,0,0,0,2,8,0},{0,0,0,4,1,9,0,0,5},{0,0,0,0,8,0,0,7,9}
        };
        System.out.println("Grille valide : " + isValid(sample));
    }
}
