package game;

public class Square {
    private Player player = null;                               //Указатель на ирока, заполневшего ячейку

    /**
     * Заполнение ячейки
     *
     * @param player - игрок, который заполняет ячейку
     */
    public void fill(Player player) {
        this.player = player;
    }

    /**
     * Заполнена ли ячейка?
     *
     * @return
     */
    public boolean isFilled() {
        if (player != null) {
            return true;
        }
        return false;
    }

    public Player getPlayer() {
        return player;
    }
}