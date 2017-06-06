package game;

/**
 * Игрок
 */

public class Player {
    /**
     * Символ игрока
     */
    private String name;

    /**
     * Конструктор
     */
    public Player(String name) {
        this.name = name;
    }

    /**
     * Функция получения имени игрока
     */
    public String getName() {
        return name;
    }
}
