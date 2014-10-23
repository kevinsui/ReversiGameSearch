import java.io.*;

public class Main {

  static final int MAX = 8;

  public static void main (String[] args) {
    try {
      // parse file line by line and extract necessary data
      BufferedReader br = new BufferedReader(new FileReader("input.txt"));
      int task = Integer.parseInt(br.readLine());
      char player = br.readLine().charAt(0);
      int depth = Integer.parseInt(br.readLine());
      char[][] board = new char[MAX][MAX];
      for (int i = 0; i < MAX; i++) {
        String row = br.readLine();
        for (int j = 0; j < MAX; j++) {
          board[i][j] = row.charAt(j);
        }
      }
      br.close();
      // call corresponding task to run search
      Search search = new Search(board, depth, player);
      switch (task) {
        case 1: search.greedy();
                break;
        case 2: search.minimax();
                break;
        case 3: search.alphabeta();
                break;
      }
    } catch(IOException e) {
      System.out.println("Failed to read file.");
    }
  }

}