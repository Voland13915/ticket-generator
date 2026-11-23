package tickets.ui;

/** Число со словом в правильной форме: «1 группа», «3 группы», «11 групп». */
final class Russian {

    private Russian() {
    }

    static String count(int n, String one, String few, String many) {
        return n + " " + plural(n, one, few, many);
    }

    static String plural(int n, String one, String few, String many) {
        int lastTwo = Math.abs(n) % 100;
        int last = lastTwo % 10;
        if (lastTwo >= 11 && lastTwo <= 14) {
            return many;
        }
        if (last == 1) {
            return one;
        }
        return last >= 2 && last <= 4 ? few : many;
    }
}
