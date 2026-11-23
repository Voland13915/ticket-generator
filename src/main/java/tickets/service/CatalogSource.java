package tickets.service;

/** Поставщик каталога (группы, студенты, билеты). Ошибки возвращает в результате, а не бросает. */
@FunctionalInterface
public interface CatalogSource {

    LoadResult load();
}
