import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.StringBuilder;

public class Game {
    String word;
    boolean[] guessedChars;
    int turn;
    List<Player> players;

    static String[] words = { "APPLE", "BANANA", "CANDLE", "DOLPHIN", "ELEPHANT", "FLOWER", "GUITAR", "HAMMER", "IGLOO",
            "JAGUAR", "KANGAROO", "LEMON", "MONKEY", "NEEDLE", "OCTOPUS", "PENGUIN", "QUILT", "RAINBOW", "SPIDER",
            "TIGER", "UMBRELLA", "VIOLIN", "WATERMELON", "XYLOPHONE", "YAK", "ZEBRA", "ANCHOR", "BUTTERFLY",
            "CATERPILLAR", "DRAGONFLY", "EGGPLANT", "FIREFLY", "GIRAFFE", "HUMMINGBIRD", "ICEBERG", "JELLYFISH",
            "KOALA", "LIGHTHOUSE", "MUSHROOM", "NARWHAL", "OSTRICH", "PARROT", "QUOKKA", "RHINOCEROS", "SEAHORSE",
            "TOUCAN", "UNICORN", "VULTURE", "WOLF", "YACHT" };

    public Game(List<Player> players) {
        Random rand = new Random();
        word = words[rand.nextInt(words.length)];
        guessedChars = new boolean['Z' - 'A' + 1];
        for (int i = 0; i < guessedChars.length; i++) {
            guessedChars[i] = false;
        }
        turn = 0;
        this.players = players;
    }

    public void run() {
        updatePlayers();
        while (true) {
            Player player = players.get(turn);
            String guess = player.readLine("It's your turn:");
            while (true) {
                if (guess == null)
                    break;
                guess = guess.toUpperCase();

                if (guess.length() == 1) {
                    char guessChar = guess.charAt(0);
                    if ('A' > guessChar || 'Z' < guessChar) {
                        guess = player.readLine("Invalid char, try again:");
                        continue;
                    }
                    guessedChars[guessChar - 'A'] = true;
                    break;
                } else {
                    if (guess.equals(word)) {
                        for (Player p : players) {
                            p.writeLine("Game end player " + player.getName() + " won!");
                            if (p != player)
                                p.loseGame();
                        }
                        player.winGame();
                        return;
                    } else {
                        break;
                    }

                }
            }
            turn = (turn + 1) % players.size();
            updatePlayers();
        }
    }

    String mangleWord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            sb.append(guessedChars[c - 'A'] ? c : '_');
        }
        return sb.toString();
    }

    void updatePlayer(Player player) {
        String mangled = mangleWord();
        StringBuilder sb = new StringBuilder();
        sb.append("Guessed letters: ");
        boolean first = true;
        for (char c = 'A'; c <= 'Z'; c++) {
            if (guessedChars[c - 'A']) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(c);
            }
        }
        player.writeLine("=================");
        player.writeLine(mangled);
        player.writeLine(sb.toString());
    }

    void updatePlayers() {
        for (Player p : players) {
            updatePlayer(p);
        }
    }
}