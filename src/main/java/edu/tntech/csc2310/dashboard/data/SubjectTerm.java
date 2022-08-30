package edu.tntech.csc2310.dashboard.data;

public class SubjectTerm {

    private String subject;
    private String term;

    public SubjectTerm(String subject, String term) {
        this.subject = subject.toUpperCase();
        this.term = term;
    }

    public String getSubject() {
        return subject;
    }

    public String getTerm() {
        return term;
    }
}
