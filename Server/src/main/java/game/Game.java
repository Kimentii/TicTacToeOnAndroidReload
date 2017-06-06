package game;

public class Game {
    /**
     * Игроки
     */
    private Player[] players;
    /**
     * Поле
     */
    private Square[][] field;
    /**
     * начата ли игра?
     */
    private boolean started;
    /**
     * Текущий игрок
     */
    private Player activePlayer;
    /**
     * Считает колличество заполненных ячеек
     */
    private int filled;
    /**
     * Всего ячеек
     */
    private int squareCount;
    /**
     * Для проверки поля
     */
    private WinnerCheckerInterface[] winnerCheckers;

    /**
     * Конструктор
     */
    public Game() {
        field = new Square[3][3];
        squareCount = 0;
        // заполнение поля
        for (int i = 0, l = field.length; i < l; i++) {
            for (int j = 0, l2 = field[i].length; j < l2; j++) {
                field[i][j] = new Square();
                squareCount++;
            }
        }
        players = new Player[2];                            //Два игрока
        started = false;                                    //Игра не начата
        activePlayer = null;                                //Текущий игрок
        filled = 0;                                         //Ни одной ячейки не заполнено
        /**
         * Создание классов для поиска победителя
         */
        winnerCheckers = new WinnerCheckerInterface[4];
        winnerCheckers[0] = new WinnerCheckerHorizontal(this);
        winnerCheckers[1] = new WinnerCheckerVertical(this);
        winnerCheckers[2] = new WinnerCheckerDiagonalLeft(this);
        winnerCheckers[3] = new WinnerCheckerDiagonalRight(this);
    }

    /**
     * Начало игры
     */
    public void start() {
        resetPlayers();
        started = true;
    }

    /**
     * Присваивание игрокам их символа
     */
    private void resetPlayers() {
        players[0] = new Player("X");
        players[1] = new Player("O");
        setCurrentActivePlayer(players[0]);
    }

    public Square[][] getField() {
        return field;
    }

    /**
     * @param player - активный игрок
     */
    private void setCurrentActivePlayer(Player player) {
        activePlayer = player;
    }

    /**
     * Передать ход другому игроку
     *
     * @param x
     * @param y
     * @return - можно ли походить игроку
     */
    public boolean makeTurn(int x, int y) {
        if (field[x][y].isFilled()) {
            return false;                                       //Если ячейка заполнена, то ходить нельзя
        }
        field[x][y].fill(getCurrentActivePlayer());             //Заполнение ячейки символом игрока
        filled++;                                               //Увеличение заполненных ячеек
        switchPlayers();                                        //Передача управления другому игроку
        return true;
    }

    /**
     * Пердача управления другому игрокуы
     */
    public void switchPlayers() {
        activePlayer = (activePlayer == players[0]) ? players[1] : players[0];
    }

    /**
     * @return активного игрока
     */
    public Player getCurrentActivePlayer() {
        return activePlayer;
    }

    /**
     * Заполнена ли игровая доска
     *
     * @return
     */
    public boolean isFieldFilled() {
        return squareCount == filled;
    }

    /**
     * Проверка на победителя
     *
     * @return игрок - победитель
     */
    public Player checkWinner() {
        for (WinnerCheckerInterface winChecker : winnerCheckers) {
            Player winner = winChecker.checkWinner();
            if (winner != null) {
                return winner;
            }
        }
        return null;
    }

    /**
     * Сброс игры
     */
    public void reset() {
        resetField();
        resetPlayers();
    }

    /**
     * Сброс игрового поля
     */
    private void resetField() {
        for (int i = 0, l = field.length; i < l; i++) {
            for (int j = 0, l2 = field[i].length; j < l2; j++) {
                field[i][j].fill(null);
            }
        }
        filled = 0;
    }

}