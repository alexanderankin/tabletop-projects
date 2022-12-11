package info.ankin.tt.initial;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Gobblet {
    public static void main(String[] args) {
        Game game = new Game();

        try {
            game
                    .move(new NewMove(1, new Point(0, 0))).print()
                    .move(new NewMove(1, new Point(3, 3))).print()
                    .move(new NewMove(1, new Point(0, 3))).print()
                    .move(new NewMove(1, new Point(3, 0))).print()
                    .move(new NewMove(0, new Point(0, 1))).print()
                    .move(new NewMove(2, new Point(3, 1))).print()
                    .move(new BoardMove(new Point(0, 0), new Point(3, 0))).print()
                    .move(new BoardMove(new Point(3, 3), new Point(0, 3))).print()
                    .move(new NewMove(2, new Point(0, 2))).print()
                    .move(new NewMove(1, new Point(0, 0))).print()
                    .move(new NewMove(0, new Point(1, 2))).print()
                    .move(new NewMove(0, new Point(0, 0))).print()
                    .move(new NewMove(1, new Point(1, 1))).print()
                    .move(new BoardMove(new Point(0, 3), new Point(3, 3))).print()
            ;
        } catch (VictoryException e) {
            System.out.println(e.getWinner() + " won:");
            game.print();
        } catch (RuleException e) {
            System.out.println("some rules were broken: " + e.getMessage());
            game.print();
            e.printStackTrace();
        }
    }

    enum Player {
        ONE(1),
        TWO(2),
        ;

        private final int number;

        Player(int number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "p" + number;
        }
    }

    interface Move {
        Point to();
    }

    static class Game {
        static final int SIZES = 4;
        static final int BANKS = 3;
        static final int BOARD_DIMENSION = 4;
        static final int WIN_LENGTH = BOARD_DIMENSION;
        static final List<Map.Entry<Integer, Integer>> DIRECTIONS_INC_DIAGONAL = List.of(
                Map.entry(0, 1),
                Map.entry(1, 1),
                Map.entry(1, 0),
                Map.entry(1, -1),
                Map.entry(0, -1),
                Map.entry(-1, -1),
                Map.entry(-1, 0),
                Map.entry(-1, 1));
        private final ArrayList<ArrayList<ArrayList<Piece>>> board;
        private final Hand handONE = new Hand(Player.ONE);
        private final Hand handTWO = new Hand(Player.TWO);
        private Player next;

        public Game() {
            next = Player.ONE;
            board = IntStream.range(0, BOARD_DIMENSION)
                    .boxed().map(i -> IntStream.range(0, BOARD_DIMENSION)
                            .boxed().map(j -> new ArrayList<Piece>(SIZES))
                            .collect(Collectors.toCollection(ArrayList::new)))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        public Game move(BoardMove move) {
            ArrayList<Piece> from = getPosition(move.from());
            if (from.isEmpty()) throw new RuleException("moving from empty place", next, move);

            Piece toRemove = from.get(from.size() - 1);

            if (toRemove.player() != next) throw new RuleException("Can't move the other player's piece", next, move);
            from.remove(from.size() - 1);

            checkForVictory();
            return move(move, toRemove);
        }

        public Game move(NewMove move) {
            var piece = (next == Player.ONE ? handONE : handTWO).pop(move.bank());
            return move(move, piece);
        }

        private Game move(Move move, Piece piece) {
            List<Piece> position = getPosition(move.to());
            if (!position.isEmpty()) {
                Piece top = position.get(position.size() - 1);
                if (!piece.covers(top)) throw new RuleException(piece + " doesn't cover " + top, next, move);
            }
            position.add(piece);
            next = next == Player.ONE ? Player.TWO : Player.ONE;
            checkForVictory();
            return this;
        }

        private ArrayList<Piece> getPosition(Point point) {
            return board.get(point.row()).get(point.col());
        }

        private void checkForVictory() {
            List<List<Player>> tops = board.stream().map(r -> r.stream().map(s -> s.size() == 0 ? null : s.get(s.size() - 1).player()).toList()).toList();
            for (int i = 0; i < tops.size(); i++) {
                for (int j = 0; j < tops.get(0).size(); j++) {
                    Player player = tops.get(i).get(j);
                    if (player == null) continue;

                    for (var direction : DIRECTIONS_INC_DIAGONAL) {
                        boolean winInDirection = true;
                        for (int k = 1; k < WIN_LENGTH; k++) {
                            int place_i = i + (k * direction.getKey());
                            int place_j = j + (k * direction.getValue());

                            if (
                                    place_i >= BOARD_DIMENSION ||
                                            place_i < 0 ||
                                            place_j >= BOARD_DIMENSION ||
                                            place_j < 0 ||
                                            tops.get(place_i).get(place_j) != player) {
                                winInDirection = false;
                                break;
                            } else {
                                System.out.print("");
                            }
                        }

                        if (winInDirection) {
                            throw new VictoryException(player);
                        }
                    }
                }
            }
        }

        public Game print() {
            System.out.println("player ONE: " + handONE);
            System.out.println("player TWO: " + handTWO);
            System.out.println("--- board: ---");
            int max = 0;
            for (var r : board) {
                for (var c : r) {
                    max = Math.max(max, String.valueOf(c).length());
                }
            }
            for (var r : board) {
                for (var c : r) {
                    System.out.printf(String.format("%" + (max + 1) + "s", c));
                }
                System.out.println();
            }
            System.out.println("--- ---");
            return this;
        }
    }

    record Point(int row, int col) {
    }

    record BoardMove(Point from, Point to) implements Move {
    }

    record NewMove(int bank, Point to) implements Move {
    }

    static class Hand {
        private final LinkedList<LinkedList<Piece>> board;

        public Hand(Player player) {
            board = IntStream.range(0, Game.BANKS)
                    .boxed().map(i -> IntStream.range(0, Game.SIZES)
                            .boxed().map(j -> new Piece(Game.SIZES - j, player))
                            .collect(Collectors.toCollection(LinkedList::new)))
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        public Piece pop(int i) {
            return board.get(i).pop();
        }

        @Override
        public String toString() {
            return board.toString();
        }
    }

    record Piece(int piece, Player player) {
        public boolean covers(Piece top) {
            return piece > top.piece();
        }

        @Override
        public String toString() {
            return player + "(" + piece + ")";
        }
    }

    static class VictoryException extends RuntimeException {
        private final Player winner;

        VictoryException(Player winner) {
            this.winner = winner;
        }

        public Player getWinner() {
            return winner;
        }
    }

    static class RuleException extends RuntimeException {
        public RuleException(String explanation, Player whoBroke, Move move) {
            super(explanation + " (occurred on " + whoBroke + "'s move: " + move);
        }
    }
}
