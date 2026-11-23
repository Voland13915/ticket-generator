package tickets.model;

import java.util.List;

/** Билет из tickets.docx: номер и список вопросов. */
public record Ticket(int number, List<String> questions) {

    public Ticket {
        questions = List.copyOf(questions);
    }
}
