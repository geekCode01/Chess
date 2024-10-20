    package org.example;

    import java.util.Objects;
    import java.util.Scanner;

    public class Main {
        // Enum for Move Status
        enum MoveStatus {
            SUCCESS, FAILURE, ILLEGAL_MOVE;
        }
        // Class representing a color of the pieces
        enum Color {
            WHITE, BLACK
        }
        static class Position {
            int row;
            int col;

            public Position(int row, int col) {
                this.row = row;
                this.col = col;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true; // Check reference equality
                if (obj == null || getClass() != obj.getClass()) return false; // Ensure same class
                Position other = (Position) obj; // Safe cast
                return row == other.row && col == other.col; // Compare rows and columns
            }

            @Override
            public int hashCode() {
                return Objects.hash(row, col); // Generate hash code based on row and col
            }

            @Override
            public String toString() {
                return String.format("Position[row=%d, col=%d]", row, col); // User-friendly representation
            }

            /* public int[] getRowColDifferences(Position other) {
                return new int[]{
                        Math.abs(this.row - other.row),
                        Math.abs(this.col - other.col)
                };
            }
             */
        }

        // Abstract class representing a chess Piece
        //subclasses must implement specific behavior
        abstract static class Piece {
            protected Color color;
            protected Position position;

            protected Piece(Color color, Position position) {
                this.color = color;
                this.position = position;
            }

            public Color getColor() {
                return color;
            }

            public Position getPosition() {
                return position;
            }

            public void setPosition(Position position) {
                this.position = position;
            }

            // Abstract method to be implemented by each piece to define their movement behavior
            public abstract boolean isValidMove(Position newPosition, Board board);

            // Represent the piece with a symbol for drawing the board
            public abstract char getSymbol();
        }

        // Concrete class for King
        static class King extends Piece {
            public King(Color color, Position position) {
                //Calls the constructor of the parent class (Piece) to set the color and position of the King
                super(color, position);
            }

            // Define how the King moves
            @Override
            public boolean isValidMove(Position newPosition, Board board) {
                //Calculates the absolute difference in rows between the current position and the new position
                int rowDiff = Math.abs(newPosition.row - this.position.row);
                //Calculates the absolute difference in columns.
                int colDiff = Math.abs(newPosition.col - this.position.col);
                // King moves only one square in any direction
                return (rowDiff <= 1 && colDiff <= 1);
            }

            @Override
            public char getSymbol() {
                return color == Color.WHITE ? 'K' : 'k';
            }
        }

        //moves diagonally
        static class Bishop extends Piece {
            public Bishop(Color color, Position position) {
                super(color, position);
            }

            @Override
            public boolean isValidMove(Position newPosition, Board board) {
                //we can make func common in position class and use everywhere
                /* int[] diffs = this.position.getRowColDifferences(newPosition);
                int rowDiff = diffs[0];
                int colDiff = diffs[1];
                 */

                int rowDiff = Math.abs(newPosition.row - this.position.row);
                int colDiff = Math.abs(newPosition.col - this.position.col);
                /*If the Bishop is at (3, 3) and tries to move to (5, 5):
                rowDiff = Math.abs(5 - 3) = 2
                colDiff = Math.abs(5 - 3) = 2
                */
                // Bishop moves diagonally
                if (rowDiff == colDiff) {
                    // Check if the path is clear
                    return isPathClear(newPosition, board);
                }
                return false;
            }

            private boolean isPathClear(Position newPosition, Board board) {
                //calculates whether to increment or decrement the row and column
                // indices when moving toward the new position.
                /* (3, 3) to (5, 5):
                    rowStep will be 1 (moving down).
                    colStep will also be 1 (moving right).
                */
                int rowStep = (newPosition.row - this.position.row) > 0 ? 1 : -1;
                int colStep = (newPosition.col - this.position.col) > 0 ? 1 : -1;
                int row = this.position.row + rowStep;
                int col = this.position.col + colStep;

                //checks every position along the diagonal path from the current position
                while (row != newPosition.row && col != newPosition.col) {
                    //if it finds any piece blocking the path return false
                    if (board.getPiece(new Position(row, col)) != null) {
                        return false; // There's a piece blocking the path
                    }
                    row += rowStep;
                    col += colStep;
                }
                return true;
            }

            @Override
            public char getSymbol() {
                return color == Color.WHITE ? 'B' : 'b';
            }
        }

        //moves hor or vertically
        static class Rook extends Piece {
            public Rook(Color color, Position position) {
                // Constructor for Rook, takes color and position
                super(color, position);
            }

            @Override
            public boolean isValidMove(Position newPosition, Board board) {
                // Calculate the difference in rows and columns
                int rowDiff = Math.abs(newPosition.row - this.position.row);
                int colDiff = Math.abs(newPosition.col - this.position.col);

                // Rook moves in straight lines: either rows or columns must be the same
                if (rowDiff == 0 || colDiff == 0) {
                    return isPathClear(newPosition, board); // Check if the path is clear for the move
                }
                return false; // Invalid move if neither row nor column is the same
            }

            private boolean isPathClear(Position newPosition, Board board) {
                // Determine the step direction for rows and columns
                //vertical step direction
                /*
                * Rook located at position (4, 4)
                * it wants to move to (4, 7)
                * int rowStep = Integer.compare(4 - 4, 0); // rowStep = Integer.compare(0, 0) => 0
                  int colStep = Integer.compare(7 - 4, 0); // colStep = Integer.compare(3, 0) => 1
                  *
                  * int row = 4 + 0; // row remains 4
                    int col = 4 + 1; // col becomes 5
                    * The loop will check positions (4, 5), (4, 6), and then reach (4, 7)
                * */
                int rowStep = Integer.compare(newPosition.row - this.position.row, 0); // 1, -1, or 0
                // horizontal step direction
                int colStep = Integer.compare(newPosition.col - this.position.col, 0); // 1, -1, or 0

                // Start checking from the position next to the rook towards the new position
                int row = this.position.row + rowStep;
                int col = this.position.col + colStep;

                // Loop until the rook reaches the new position
                while (row != newPosition.row || col != newPosition.col) {
                    // If a piece is found in the path, the move is blocked
                    if (board.getPiece(new Position(row, col)) != null) {
                        return false; // There's a piece blocking the path
                    }
                    // Move to the next position in the direction of the move
                    row += rowStep;
                    col += colStep;
                }
                return true; // The path is clear for the move
            }

            @Override
            public char getSymbol() {
                return color == Color.WHITE ? 'R' : 'r';
            }
        }

        static class Queen extends Piece {
            public Queen(Color color, Position position) {
                super(color, position);
            }

            @Override
            public boolean isValidMove(Position newPosition, Board board) {
                // Calculate the absolute differences in rows and columns
                int rowDiff = Math.abs(newPosition.row - this.position.row);
                int colDiff = Math.abs(newPosition.col - this.position.col);

                // Queen moves like a Rook or a Bishop
                if (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) {
                    return isPathClear(newPosition, board);
                }
                return false;
            }

            private boolean isPathClear(Position newPosition, Board board) {
                //vertical step direction
                // If the new position is directly above or below (same column), rowStep is set to 0.
                //If moving down, it's set to 1; if moving up, it's set to -1.
                int rowStep = Integer.compare(newPosition.row - this.position.row, 0);
                // horizontal step direction
                int colStep = Integer.compare(newPosition.col - this.position.col, 0);

                // Start checking from the position next to the queen towards the new position
                int row = this.position.row + rowStep;
                int col = this.position.col + colStep;

                // Loop until the queen reaches the new position
                while (row != newPosition.row || col != newPosition.col) {
                    if (board.getPiece(new Position(row, col)) != null) {
                        return false; // There's a piece blocking the path
                    }
                    // Move to the next position in the direction of the move
                    row += rowStep;
                    col += colStep;
                }
                return true;
            }

            @Override
            public char getSymbol() {
                return color == Color.WHITE ? 'Q' : 'q';
            }
        }

        static class Knight extends Piece {
            public Knight(Color color, Position position) {
                super(color, position);
            }

            @Override
            public boolean isValidMove(Position newPosition, Board board) {
                int rowDiff = Math.abs(newPosition.row - this.position.row);
                int colDiff = Math.abs(newPosition.col - this.position.col);

                // Knight moves in an "L" shape
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            }

            @Override
            public char getSymbol() {
                return color == Color.WHITE ? 'N' : 'n';
            }
        }

        static class Pawn extends Piece {
            public Pawn(Color color, Position position) {
                super(color, position);
            }

            @Override
            public boolean isValidMove(Position newPosition, Board board) {
                int direction = (color == Color.WHITE) ? -1 : 1; // White moves up, Black moves down
                int startRow = (color == Color.WHITE) ? 6 : 1; // Starting row for pawns

                // Normal move (one square forward)
                if (newPosition.row == this.position.row + direction && newPosition.col == this.position.col) {
                    // Check if the destination square is empty
                    return board.getPiece(newPosition) == null; // Must be an empty square
                }

                // Initial double move (two squares forward)
                if (this.position.row == startRow && newPosition.row == this.position.row + 2 * direction && newPosition.col == this.position.col) {
                    // Check both squares in front for emptiness
                    return board.getPiece(new Position(this.position.row + direction, this.position.col)) == null &&
                            board.getPiece(newPosition) == null;
                }

                // Capture (diagonal move)
                if (newPosition.row == this.position.row + direction &&
                        Math.abs(newPosition.col - this.position.col) == 1) {
                    // The destination must have a piece and it must be an opponent's piece
                    return board.getPiece(newPosition) != null;
                }

                return false; // Invalid move
            }

            @Override
            public char getSymbol() {
                return color == Color.WHITE ? 'P' : 'p';
            }
        }



        // Singleton class for Board
        static class Board {
            private static Board instance;
            private Piece[][] board;

            private Board() {
                board = new Piece[8][8]; // 8x8 board
            }

            // Singleton instance getter
            public static Board getInstance() {
                if (instance == null) {
                    instance = new Board();
                }
                return instance;
            }

            // Add a piece to the board
            public void placePiece(Piece piece, Position position) {
                board[position.row][position.col] = piece;
            }

            // Get the piece at a given position
            public Piece getPiece(Position position) {
                return board[position.row][position.col];
            }

            // Move a piece
            public MoveStatus movePiece(Piece piece, Position newPosition) {
                if (piece.isValidMove(newPosition, this)) {
                    Piece target = getPiece(newPosition);
                    if (target == null || target.getColor() != piece.getColor()) {
                        board[piece.position.row][piece.position.col] = null; // Clear old position
                        piece.setPosition(newPosition); // Update piece position
                        board[newPosition.row][newPosition.col] = piece; // Set new position
                        return MoveStatus.SUCCESS;
                    }
                }
                return MoveStatus.ILLEGAL_MOVE;
            }

            // Draw the current state of the board
            public void drawBoard() {
                System.out.println("  a b c d e f g h");
                for (int row = 0; row < 8; row++) {
                    System.out.print((8 - row) + " ");
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == null) {
                            System.out.print(". ");
                        } else {
                            System.out.print(board[row][col].getSymbol() + " ");
                        }
                    }
                    System.out.println(8 - row);
                }
                System.out.println("  a b c d e f g h");
            }
        }

        // Class for Player
        static class Player {
            private String name;
            private Color color;

            public Player(String name, Color color) {
                this.name = name;
                this.color = color;
            }

            public Color getColor() {
                return color;
            }

            public String getName() {
                return name;
            }
        }

        public static class GameInitializer {
            private GameInitializer(){}
            public static void initializePieces(Board board) {
                // Place Kings
                board.placePiece(new King(Color.WHITE, new Position(7, 4)), new Position(7, 4));
                board.placePiece(new King(Color.BLACK, new Position(0, 4)), new Position(0, 4));

                // Place Queens
                board.placePiece(new Queen(Color.WHITE, new Position(7, 3)), new Position(7, 3));
                board.placePiece(new Queen(Color.BLACK, new Position(0, 3)), new Position(0, 3));

                // Place Bishops
                board.placePiece(new Bishop(Color.WHITE, new Position(7, 2)), new Position(7, 2));
                board.placePiece(new Bishop(Color.WHITE, new Position(7, 5)), new Position(7, 5));
                board.placePiece(new Bishop(Color.BLACK, new Position(0, 2)), new Position(0, 2));
                board.placePiece(new Bishop(Color.BLACK, new Position(0, 5)), new Position(0, 5));

                // Place Rooks
                board.placePiece(new Rook(Color.WHITE, new Position(7, 0)), new Position(7, 0));
                board.placePiece(new Rook(Color.WHITE, new Position(7, 7)), new Position(7, 7));
                board.placePiece(new Rook(Color.BLACK, new Position(0, 0)), new Position(0, 0));
                board.placePiece(new Rook(Color.BLACK, new Position(0, 7)), new Position(0, 7));

                // Place Knights
                board.placePiece(new Knight(Color.WHITE, new Position(7, 1)), new Position(7, 1));
                board.placePiece(new Knight(Color.WHITE, new Position(7, 6)), new Position(7, 6));
                board.placePiece(new Knight(Color.BLACK, new Position(0, 1)), new Position(0, 1));
                board.placePiece(new Knight(Color.BLACK, new Position(0, 6)), new Position(0, 6));

                // Place Pawns
                for (int col = 0; col < 8; col++) {
                    board.placePiece(new Pawn(Color.WHITE, new Position(6, col)), new Position(6, col));
                    board.placePiece(new Pawn(Color.BLACK, new Position(1, col)), new Position(1, col));
                }
            }
        }


        // Class representing the Game
        static class Game {
            private final Board board;
            private final Player whitePlayer;
            private final Player blackPlayer;
            private Player currentPlayer;
            private boolean gameFinished = false;

            public Game(Player whitePlayer, Player blackPlayer) {
                this.board = Board.getInstance();
                this.whitePlayer = whitePlayer;
                this.blackPlayer = blackPlayer;
                this.currentPlayer = whitePlayer; // White starts first
            }

            // Switch turns between players
            public void switchTurn() {
                currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
            }

            // Method to handle a move
            public MoveStatus makeMove(Piece piece, Position newPosition) {
                if (piece.getColor() != currentPlayer.getColor()) {
                    System.out.println("Not your turn!");
                    return MoveStatus.FAILURE;
                }
                // Check if move is valid
                MoveStatus moveStatus = board.movePiece(piece, newPosition);
                if (moveStatus == MoveStatus.SUCCESS) {
                    // After move, check if a king is captured
                    checkKingCaptured();

                    // Switch turn only if the game isn't finished
                    if (!gameFinished) {
                        switchTurn();
                    }
                }
                return moveStatus;
            }

            // Check if either King is captured and declare a winner
            private void checkKingCaptured() {
                Piece whiteKing = findKing(Color.WHITE);
                Piece blackKing = findKing(Color.BLACK);

                if (whiteKing == null) {
                    System.out.println(blackPlayer.getName() + " wins! The White King was captured.");
                    gameFinished = true;
                } else if (blackKing == null) {
                    System.out.println(whitePlayer.getName() + " wins! The Black King was captured.");
                    gameFinished = true;
                }
            }

            // Find a King on the board based on color
            private Piece findKing(Color color) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        Piece piece = board.getPiece(new Position(row, col));
                        if (piece instanceof King && piece.getColor() == color) {
                            return piece;
                        }
                    }
                }
                return null;
            }

            public Player getCurrentPlayer() {
                return currentPlayer;
            }
        }

        static class MoveProcessor {
            private final Board board;
            private final Game game;

            public MoveProcessor(Board board, Game game) {
                this.board = board;
                this.game = game;
            }

            public boolean processMove(String move) {
                String[] parts = move.split(" ");
                if (parts.length != 2) {
                    System.out.println("Invalid input format. Use 'e2 e4' format.");
                    return false;
                }

                Position from = parsePosition(parts[0]);
                Position to = parsePosition(parts[1]);
                if (from == null || to == null) {
                    System.out.println("Invalid positions. Try again.");
                    return false;
                }

                Piece piece = board.getPiece(from);
                if (piece == null) {
                    System.out.println("No piece at the given position.");
                    return false;
                }

                MoveStatus status = game.makeMove(piece, to);
                if (status != MoveStatus.SUCCESS) {
                    System.out.println("Illegal move. Try again.");
                    return false;
                }

                return true; // Move was successful
            }

            // Convert the user input (e.g., "e2") into a board position
            private Position parsePosition(String input) {
                if (input.length() != 2) return null;
                char col = input.charAt(0);
                char row = input.charAt(1);
                int rowPos = 8 - Character.getNumericValue(row);
                int colPos = col - 'a';
                if (rowPos < 0 || rowPos > 7 || colPos < 0 || colPos > 7) {
                    return null;
                }
                return new Position(rowPos, colPos);
            }
        }

        public static void main(String[] args) {
            // Create players
            Scanner scanner = new Scanner(System.in);

            Player whitePlayer = new Player("White", Color.WHITE);
            Player blackPlayer = new Player("Black", Color.BLACK);

            // Initialize the game
            Game game = new Game(whitePlayer, blackPlayer);

            // Place initial pieces on the board
            Board board = Board.getInstance();

            // Initialize pieces using GameInitializer
            GameInitializer.initializePieces(board);

            // Draw the initial board
            board.drawBoard();

            // Create the MoveProcessor instance
            MoveProcessor moveProcessor = new MoveProcessor(board, game);

            // Main game loop
            while (true) {
                Player currentPlayer = game.getCurrentPlayer();
                System.out.println(currentPlayer.getName() + "'s move (e.g., e2 e4): ");
                String move = scanner.nextLine();

                if (move.equalsIgnoreCase("stop")) {
                    System.out.println("Game has been stopped. Thank you for playing!");
                    break; // Exit the loop
                }

                if (moveProcessor.processMove(move)) {
                    board.drawBoard(); // Draw updated board after each successful move
                }

            }
        }
    }