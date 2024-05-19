import java.util.List;
import java.util.Random;
import java.lang.StringBuilder;

public class Game {

    // Secret word
    String word;

    // List of letters guessed or not
    boolean[] guessedChars;

    // Index of the player playing now
    int turn;

    // List of the players
    List<Player> players;

    int skipCount = 0;

    // List of possible words to guess
    static String[] words = { "APPLE", "BANANA", "CANDLE", "DOLPHIN", "ELEPHANT", "FLOWER", "GUITAR", "HAMMER", "IGLOO",
            "JAGUAR", "KANGAROO", "LEMON", "MONKEY", "NEEDLE", "OCTOPUS", "PENGUIN", "QUILT", "RAINBOW", "SPIDER",
            "TIGER", "UMBRELLA", "VIOLIN", "WATERMELON", "XYLOPHONE", "YAK", "ZEBRA", "ANCHOR", "BUTTERFLY",
            "CATERPILLAR", "DRAGONFLY", "EGGPLANT", "FIREFLY", "GIRAFFE", "HUMMINGBIRD", "ICEBERG", "JELLYFISH",
            "KOALA", "LIGHTHOUSE", "MUSHROOM", "NARWHAL", "OSTRICH", "PARROT", "QUOKKA", "RHINOCEROS", "SEAHORSE",
            "TOUCAN", "UNICORN", "VULTURE", "WOLF", "YACHT" };

    public Game(List<Player> players) {
        // Choose random word
        Random rand = new Random();
        this.word = words[rand.nextInt(words.length)];
        System.out.println("new game with word: " + this.word);
        // Initialize list of letters to false
        guessedChars = new boolean['Z' - 'A' + 1];
        for (int i = 0; i < guessedChars.length; i++) {
            guessedChars[i] = false;
        }
        turn = 0;
        this.players = players;
    }

    public void run() {
        // Initializes game for every player
        updatePlayers();
        while (true) {
            Player player = players.get(turn);
            String guess = player.readLine("It's your turn:");
            while (true) {
                // If there is no guess then go to the next player
                if (guess == null) {
                    skipCount++;
                    System.out.println("skip count: " + skipCount);
                    if (skipCount == this.players.size()) {
                        System.out.println("ending game");
                        end(null);
                        return;
                    }
                    break;
                }
                skipCount = 0;
                guess = guess.toUpperCase();
                // If guess is a letter
                if (guess.length() == 0) {
                    guess = player.readLine("Invalid guess, try again:");
                    continue;
                } else if (guess.length() == 1) {
                    char guessChar = guess.charAt(0);
                    // If char is not a valid letter then try again
                    if ('A' > guessChar || 'Z' < guessChar) {
                        guess = player.readLine("Invalid char, try again:");
                        continue;
                    }
                    guessedChars[guessChar - 'A'] = true;
                    // If word is complet then end game
                    if (mangleWord().equals(word)) {
                        end(player);
                        return;
                    }
                    break;
                }
                // If guess is a word
                else {
                    boolean invalid = false;
                    for (int i = 0; i < word.length(); i++) {
                        char guessChar = word.charAt(i);
                        if ('A' > guessChar || 'Z' < guessChar) {
                            invalid = true;
                            guess = player.readLine("Invalid guess, try again:");
                            break;
                        }
                    }
                    if (invalid)
                        continue;
                    // If guess is correct then end game
                    if (guess.equals(word)) {
                        end(player);
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

    // Creates the word mangled
    String mangleWord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            sb.append(guessedChars[c - 'A'] ? c : '_');
        }
        return sb.toString();
    }

    // Updates the game state for a specific player.
    void updatePlayer(Player player, boolean mangle) {
        String mangled = mangle ? mangleWord() : word;
        // Create list of guessed letters to print
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

    void updatePlayer(Player player) {
        updatePlayer(player, true);
    }

    // Updates the game state for all players.
    void updatePlayers() {
        for (Player p : players) {
            updatePlayer(p);
        }
    }

    void end(Player winner) {
        for (Player p : players) {
            p.isPlaying = false;
            if (winner != null) {
                updatePlayer(p, false);
                if (p != winner) {
                    p.writeLine("Game end player " + winner.getName() + " won!");
                    p.loseGame();
                } else {
                    p.writeLine("Game end you won!");
                    p.winGame();
                }
            } else {
                p.writeLine("Game end all players skipped!");
            }
        }
    }
}