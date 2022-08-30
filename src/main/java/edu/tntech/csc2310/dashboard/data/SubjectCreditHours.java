package edu.tntech.csc2310.dashboard.data;

public class SubjectCreditHours {

    private SubjectTerm subjectterm;
    private int creditHoursGenerated;

    public SubjectCreditHours(String subject, String term, int creditHoursGenerated) {
        this.subjectterm = new SubjectTerm(subject, term);
        this.creditHoursGenerated = creditHoursGenerated;
    }

    public SubjectTerm getSubjectTerm() {
        return subjectterm;
    }

    public int getCreditHoursGenerated() {
        return creditHoursGenerated;
    }
}
