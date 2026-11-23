package tickets.service;

/** Номер билета из журнала отсутствует среди корректных билетов tickets.docx. */
public class TicketUnavailableException extends Exception {

    public TicketUnavailableException(int number) {
        super("Билет № " + number + " уже выдан этому студенту, но его нет среди корректных билетов в tickets.docx. "
                + "Проверьте файл с билетами — запись в журнал не добавлена.");
    }
}
