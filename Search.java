import java.io.*;
import java.util.*;

public class Search {

  final int MAX = 8;
  final int INFINITY = 2147483647;
  final Coordinate root = new Coordinate(-2, -2);
  final Coordinate pass = new Coordinate(-1, -1);
  
  final char[][] initialBoard;
  final int[][] boardWeight;
  final String[][] boardNode;

  public char player;
  public char opponent;
  public int searchDepth = 0;
  public int alpha = -INFINITY;
  public int beta = INFINITY;
  public ArrayList<String> log;

  public Search(char[][] board, int depth, char player) {
    this.initialBoard = board;
    this.searchDepth = depth;
    this.player = player;
    if (player == 'X')
      this.opponent = 'O';
    else
      this.opponent = 'X';
    this.log = new ArrayList<String>();
    boardWeight = new int[][]{{99, -8, 8, 6, 6, 8, -8, 99},
                              {-8, -24, -4, -3, -3, -4, -24, -8},
                              {8, -4, 7, 4, 4, 7, -4, 8},
                              {6, -3, 4, 0, 0, 4, -3, 6},
                              {6, -3, 4, 0, 0, 4, -3, 6},
                              {8, -4, 7, 4, 4, 7, -4, 8},
                              {-8, -24, -4, -3, -3, -4, -24, -8},
                              {99, -8, 8, 6, 6, 8, -8, 99}};
    boardNode = new String[][]{{"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"},
                               {"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2"},
                               {"a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3"},
                               {"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4"},
                               {"a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5"},
                               {"a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6"},
                               {"a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7"},
                               {"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"}};    
  }

  public class Coordinate {
    int x;
    int y;
    public Coordinate (int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

/**
 * Helper Functions
 */

  public char[][] copyBoard(char[][] original) {
    char[][] board = new char[MAX][MAX];
    for (int i = 0; i < MAX; i++) {
      for (int j = 0; j < MAX; j++) {
        board[i][j] = original[i][j];
      }
    }
    return board;
  }

  public char[][] putPiece(char[][] original, Coordinate coor, char player) {
    // create new board and flip coordinate to player
    char[][] newBoard = copyBoard(original);
    newBoard[coor.y][coor.x] = player;
    // flip pieces in same row
    int firstX = coor.x;
    int lastX = coor.x;
    for (int i = coor.x; i >= 0; i--) {
      if (newBoard[coor.y][i] == '*')
        break;
      else if (newBoard[coor.y][i] == player) 
        firstX = i;
    }
    for (int i = coor.x; i < MAX; i++) {
      if (newBoard[coor.y][i] == '*')
        break;
      else if (newBoard[coor.y][i] == player) 
        lastX = i; 
    }
    for (int i = firstX; i <= lastX; i++) {
      newBoard[coor.y][i] = player;
    }
    // flip pieces in same column
    int firstY = coor.y;
    int lastY = coor.y;
    for (int j = coor.y; j >= 0; j--) {
      if (newBoard[j][coor.x] == '*')
        break;
      else if (newBoard[j][coor.x] == player) 
        firstY = j;
    }
    for (int j = coor.y; j < MAX; j++) {
      if (newBoard[j][coor.x] == '*')
        break;
      else if (newBoard[j][coor.x] == player) 
        lastY = j; 
    }
    for (int j = firstY; j <= lastY; j++) {
      newBoard[j][coor.x] = player;
    }
    // flip pieces in first diagonal
    Coordinate first = coor;
    Coordinate last = coor;
    int x = coor.x;
    int y = coor.y;
    while (x >= 0 && y >= 0) {
      if (newBoard[y][x] == '*')
        break;
      else if (newBoard[y][x] == player)
        first = new Coordinate(x, y);
      x--;
      y--;
    }
    x = coor.x;
    y = coor.y;
    while (x < MAX && y < MAX) {
      if (newBoard[y][x] == '*')
        break;
      else if (newBoard[y][x] == player)
        last = new Coordinate(x, y);
      x++;
      y++;
    }
    while (first.x != last.x) {
      newBoard[first.y][first.x] = player;
      first.x++;
      first.y++;
    }
    // flip pieces in second diagonal
    first = coor;
    last = coor;
    x = coor.x;
    y = coor.y;
    while (x >= 0 && y < MAX) {
      if (newBoard[y][x] == '*')
        break;
      else if (newBoard[y][x] == player)
        first = new Coordinate(x, y);
      x--;
      y++;
    }
    x = coor.x;
    y = coor.y;
    while (x < MAX && y >= 0) {
      if (newBoard[y][x] == '*')
        break;
      else if (newBoard[y][x] == player)
        last = new Coordinate(x, y);
      x++;
      y--;
    }
    while (first.x != last.x) {
      newBoard[first.y][first.x] = player;
      first.x++;
      first.y--;
    }
    return newBoard;
  }

  public int getBoardWeight(char[][] board, char player) {
    int playerWeight = 0;
    int opponentWeight = 0;
    for (int i = 0; i < MAX; i ++) {
      for (int j = 0; j < MAX; j++) {
        if (board[i][j] == player) {
          playerWeight += boardWeight[i][j];
        } else if (board[i][j] != '*') {
          opponentWeight += boardWeight[i][j];
        }
      }
    }
    return playerWeight - opponentWeight;
  }

  public ArrayList<Coordinate> getValidMoves(char[][] board, char player) {
    ArrayList<Coordinate> validMoves = new ArrayList<Coordinate>();
    for (int y = 0; y < MAX; y++) {
      for (int x = 0; x < MAX; x++) {
        if(board[y][x] != '*') {
          continue;
        }
        Boolean valid = false;
        // check left row
        int curX = x-1;
        while (curX >= 0) {
          if (board[y][curX] == '*')
            break;
          else if (board[y][curX] == player) {
            if (x - curX > 1)
              valid = true;
            break;
          }
          curX --;
        }
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
        // check right row
        curX = x+1;
        while (curX < MAX) {
          if (board[y][curX] == '*')
            break;
          else if (board[y][curX] == player) {
            if (curX - x > 1)
              valid = true;
            break;
          }
          curX ++;
        }
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
        // check top column
        int curY = y-1;
        while (curY >= 0) {
          if (board[curY][x] == '*')
            break;
          else if (board[curY][x] == player) {
            if (y - curY > 1)
              valid = true;
            break;
          }
          curY --;
        }   
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
        // check bottom column
        curY = y+1;
        while (curY < MAX) {
          if (board[curY][x] == '*')
            break;
          else if (board[curY][x] == player) {
            if (curY - y > 1)
              valid = true;
            break;
          }
          curY ++;
        }   
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }  
        // check upper left diagonal
        curX = x-1;
        curY = y-1;
        while (curX >= 0 && curY >= 0) {
          if (board[curY][curX] == '*')
            break;
          else if (board[curY][curX] == player) {
            if (x - curX > 1)
              valid = true;
            break;
          }
          curX --;
          curY --;
        }
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
        // check upper right diagonal
        curX = x+1;
        curY = y-1;
        while (curX < MAX && curY >= 0) {
          if (board[curY][curX] == '*')
            break;
          else if (board[curY][curX] == player) {
            if (curX - x > 1)
              valid = true;
            break;
          }
          curX ++;
          curY --;
        }
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
        // check bottom left diagonal
        curX = x-1;
        curY = y+1;
        while (curX >= 0 && curY < MAX) {
          if (board[curY][curX] == '*')
            break;
          else if (board[curY][curX] == player) {
            if (x - curX > 1)
              valid = true;
            break;
          }
          curX --;
          curY ++;
        }
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
        // check bottom right diagonal
        curX = x+1;
        curY = y+1;
        while (curX < MAX && curY < MAX) {
          if (board[curY][curX] == '*')
            break;
          else if (board[curY][curX] == player) {
            if (curX - x > 1)
              valid = true;
            break;
          }
          curX ++;
          curY ++;
        }
        if (valid) {
          validMoves.add(new Coordinate(x, y));
          continue;    
        }
      }
    }
    return validMoves;
  }

  public void outputBoard(char[][] board) {
    try {
      PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
      for (int i = 0; i < MAX; i++) {
        for (int j = 0; j < MAX; j++) {
          // System.out.print(board[i][j] + " ");
          writer.print(board[i][j]);
        }
        // System.out.print('\n');
        writer.print('\n');
      }
      if (log != null) {
        for (String entry : log) {
          // System.out.println(entry);
          writer.println(entry);
        }
      }
      writer.close();
    } catch (IOException e) {
      System.out.println("Error printing results to file.");
    }
  }

/**
 * Greedy
 */

  public void greedy() {
    char[][] board = copyBoard(this.initialBoard);
    int maxWeight = -INFINITY;
    char[][] bestPlay = null;
    // check possible plays for each of the pieces on the board
    ArrayList<Coordinate> moves = getValidMoves(board, this.player);
    for (Coordinate coor : moves) {
      char[][] newBoard = putPiece(board, coor, this.player);
      int newWeight = getBoardWeight(newBoard, this.player);
      if (newWeight > maxWeight) {
        maxWeight = newWeight;
        bestPlay = copyBoard(newBoard);
      }
    }
    // check if a decision was found
    if (bestPlay != null) {
      outputBoard(bestPlay);
    } else {
      outputBoard(board);
    }
  }

/**
 * Minimax
 */

  public void minimax() {
    char[][] board = copyBoard(this.initialBoard);
    // initialize log
    log.add("Node,Depth,Value");
    // get all valid moves
    ArrayList<Coordinate> moves = getValidMoves(board, this.player);
    ArrayList<Coordinate> opposingMoves = getValidMoves(board, this.opponent);
    // check for end game scenario
    if (moves.isEmpty() && opposingMoves.isEmpty()) {
      int weight = getBoardWeight(board, this.player);
      addMinimaxLog(root, 0, weight);
      outputBoard(board);
      return;
    }
    int bestValue = -INFINITY;
    Coordinate bestPlay = null;
    // expand game tree
    for (Coordinate coor : moves) {
      char[][] newBoard = copyBoard(board);
      newBoard = putPiece(newBoard, coor, this.player);
      addMinimaxLog(root, 0, bestValue);
      int value = minimaxRecursive(newBoard, coor, 1, false);
      if (value > bestValue) {
        bestValue = value;
        bestPlay = coor;
      }
    }
    if (bestPlay != null) {
      board = putPiece(board, bestPlay, this.player);
      addMinimaxLog(root, 0, bestValue);
    } else {
      addMinimaxLog(root, 0, bestValue);
      int value = minimaxRecursive(board, pass, 1, false);
      addMinimaxLog(root, 0, value);
    }
    outputBoard(board);
  }

  public int minimaxRecursive(char[][] board, Coordinate coor, int depth, Boolean max) {
    // base case for recursive function
    if (depth == this.searchDepth) {
      // get weight of the board and return value
      int weight = getBoardWeight(board, this.player);
      addMinimaxLog(coor, this.searchDepth, weight);
      return weight;
    }
    // get lists of possible next moves
    ArrayList<Coordinate> children = getValidMoves(board, this.player);
    ArrayList<Coordinate> opposingChildren = getValidMoves(board, this.opponent);
    // check for end game scenario
    if (children.isEmpty() && opposingChildren.isEmpty()) {
      int weight = getBoardWeight(board, this.player);
      addMinimaxLog(coor, depth, weight);
      return weight;
    }
    // max level: choose max value from the children
    if (max) {
      int bestValue = -INFINITY;
      if (children.isEmpty()) {
        addMinimaxLog(coor, depth, bestValue);
        int value = minimaxRecursive(board, pass, depth+1, false);
        addMinimaxLog(coor, depth, value);
        return value;
      }
      for (Coordinate child : children) {
        char[][] newBoard = copyBoard(board);
        newBoard = putPiece(newBoard, child, this.player);
        addMinimaxLog(coor, depth, bestValue);
        int value = minimaxRecursive(newBoard, child, depth+1, false);
        bestValue = Math.max(value, bestValue);
      }
      addMinimaxLog(coor, depth, bestValue);
      return bestValue;
    } 
    // min level: choose min value from the children
    else {
      int bestValue = INFINITY;
      if (opposingChildren.isEmpty()) {
        addMinimaxLog(coor, depth, bestValue);
        int value = minimaxRecursive(board, pass, depth+1, true);
        addMinimaxLog(coor, depth, value);
        return value;
      }
      for (Coordinate child : opposingChildren) {
        char[][] newBoard = copyBoard(board);
        newBoard = putPiece(newBoard, child, this.opponent);
        addMinimaxLog(coor, depth, bestValue);
        int value = minimaxRecursive(newBoard, child, depth+1, true);
        bestValue = Math.min(value, bestValue);
      }
      addMinimaxLog(coor, depth, bestValue);
      return bestValue;
    }
  }

  public void addMinimaxLog(Coordinate coor, int depth, int value) {
    // get coordinate for the node
    String node;
    if (coor == root)
      node = "root";
    else if (coor == pass)
      node = "pass";
    else
      node = boardNode[coor.y][coor.x];
    // add log string
    if (value == INFINITY)
      this.log.add(node + "," + depth + ",Infinity");
    else if (value == -INFINITY)
      this.log.add(node + "," + depth + ",-Infinity");
    else
      this.log.add(node + "," + depth + "," + value);
  }

/**
 * Alpha-Beta
 */

  public void alphabeta() {
    char[][] board = copyBoard(this.initialBoard);
    // initialize log
    log.add("Node,Depth,Value,Alpha,Beta");
    // get all valid moves
    ArrayList<Coordinate> moves = getValidMoves(board, this.player);
    ArrayList<Coordinate> opposingMoves = getValidMoves(board, this.opponent);
    // check for end game scenario
    if (moves.isEmpty() && opposingMoves.isEmpty()) {
      int weight = getBoardWeight(board, this.player);
      addAlphabetaLog(root, 0, weight);
      outputBoard(board);
      return;
    }
    int bestValue = -INFINITY;
    Coordinate bestPlay = null;
    // expand game tree
    for (Coordinate coor : moves) {
      char[][] newBoard = copyBoard(board);
      newBoard = putPiece(newBoard, coor, this.player);
      addAlphabetaLog(root, 0, bestValue);
      int value = alphabetaRecursive(newBoard, coor, 1, false);
      if (value > bestValue) {
        bestValue = value;
        bestPlay = coor;
      }
      this.alpha = Math.max(bestValue, this.alpha);
      this.beta = INFINITY;
    }
    if (bestPlay != null) {
      board = putPiece(board, bestPlay, this.player);
      addAlphabetaLog(root, 0, bestValue);
    } else {
      addAlphabetaLog(root, 0, bestValue);
      int value = alphabetaRecursive(board, pass, 1, false);
      addAlphabetaLog(root, 0, value);
    }
    outputBoard(board);
  }

  public int alphabetaRecursive(char[][] board, Coordinate previous, int depth, Boolean max) {
    // base case for recursive function
    if (depth == this.searchDepth) {
      // get weight of the board and return value
      int weight = getBoardWeight(board, this.player);
      addAlphabetaLog(previous, depth, weight);
      return weight;
    }
    // get list of next possible moves
    ArrayList<Coordinate> children = getValidMoves(board, this.player);
    ArrayList<Coordinate> opposingChildren = getValidMoves(board, this.opponent);
    // check for end game scenario
    if (children.isEmpty() && opposingChildren.isEmpty()) {
      int weight = getBoardWeight(board, this.player);
      addAlphabetaLog(previous, depth, weight);
      return weight;
    }
    // max level: choose max value from the children
    if (max) {
      // skip move if no possible moves for max
      if (children.isEmpty()) {
        addAlphabetaLog(previous, depth, this.alpha);
        int value = alphabetaRecursive(board, pass, depth+1, false);
        // this.alpha = Math.max(value, this.alpha);
        // this.beta = Math.min(value, this.beta);
        addAlphabetaLog(previous, depth, value);
        return value;
      }
      // go through each of the players children and expand nodes
      int maxValue = -INFINITY;
      for (Coordinate child : children) {
        char[][] newBoard = copyBoard(board);
        newBoard = putPiece(newBoard, child, this.player);
        addAlphabetaLog(previous, depth, maxValue);
        int value = alphabetaRecursive(newBoard, child, depth+1, false);  
        maxValue = Math.max(value, maxValue);
        if (maxValue >= this.beta) {
          addAlphabetaLog(previous, depth, maxValue);
          return maxValue;
        }
        this.alpha = Math.max(maxValue, alpha);
      }
      addAlphabetaLog(previous, depth, maxValue);
      return maxValue;
    }
    // min level: choose min value from the children
    else {
      // skip move if no possible moves for min
      if (opposingChildren.isEmpty()) {
        addAlphabetaLog(previous, depth, this.beta);
        int value = alphabetaRecursive(board, pass, depth+1, true);
        // this.beta = Math.min(value, this.beta);
        // this.alpha = Math.max(value, this.alpha);
        addAlphabetaLog(previous, depth, value);
        return value;
      }
      // go through each of the opponents children and expand nodes
      int minValue = INFINITY;
      for (Coordinate child : opposingChildren) {
        char[][] newBoard = copyBoard(board);
        newBoard = putPiece(newBoard, child, this.opponent);
        addAlphabetaLog(previous, depth, minValue);
        int value = alphabetaRecursive(newBoard, child, depth+1, true);
        minValue = Math.min(value, minValue);
        if (minValue <= this.alpha) {
          addAlphabetaLog(previous, depth, minValue);
          return minValue;
        }
        this.beta = Math.min(minValue, this.beta);
      }
      addAlphabetaLog(previous, depth, minValue);
      return minValue;
    }
  }

  public void addAlphabetaLog(Coordinate coor, int depth, int value) {
    // get coordinate for the node
    String node;
    String valueString;
    String alphaString = "-Infinity";
    String betaString = "Infinity";
    // get node
    if (coor == root)
      node = "root";
    else if (coor == pass)
      node = "pass";
    else
      node = boardNode[coor.y][coor.x];
    // get value
    if (value == INFINITY)
      valueString = "Infinity";
    else if (value == -INFINITY)
      valueString = "-Infinity";
    else 
      valueString = Integer.toString(value);
    // get alpha
    if (this.alpha > -INFINITY)
      alphaString = Integer.toString(this.alpha);
    // get BETA
    if (this.beta < INFINITY)
      betaString = Integer.toString(this.beta);
    this.log.add(node + "," + depth + "," + valueString + "," + alphaString + "," + betaString);
  }

}