package game;

/**
 * Интерфейс для проверки на победителя
 * Использован интерфейс Chain of Responsobility(цепочка обязанностей) т. к. буден создана гуруппа объектов,
 * котора будет обрабатовать состояние поля.
 */
public interface WinnerCheckerInterface {
    public Player checkWinner();
}