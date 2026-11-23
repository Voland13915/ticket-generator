package tickets.service;

import java.util.List;

/**
 * Итог загрузки входных данных.
 *
 * @param catalog  null, если есть ошибки
 * @param errors   то, из-за чего работать нельзя
 * @param warnings пропущенные билеты и прочее, что работе не мешает
 */
public record LoadResult(Catalog catalog, List<String> errors, List<String> warnings) {

    public boolean ok() {
        return errors.isEmpty();
    }
}
