package game;
/**
 * Проверка поля на заполнение строки одним элементом
 */
public class WinnerCheckerHorizontal implements WinnerCheckerInterface {
    private Game game;

    public WinnerCheckerHorizontal(Game game) {
        this.game = game;
    }

    public Player checkWinner() {
        Square[][] field = game.getField();
        Player currPlayer;
        Player lastPlayer = null;
        for (int i = 0; i < field.length; i++) {
            lastPlayer = null;
            int successCounter = 1;
            for (int j = 0; j < field[i].length; j++) {
                currPlayer = field[i][j].getPlayer();
                if (currPlayer == lastPlayer && (currPlayer != null && lastPlayer != null)) {
                    successCounter++;
                    if (successCounter == field[i].length) {
                        return currPlayer;
                    }
                }
                lastPlayer = currPlayer;
            }
        }
        return null;
    }
}