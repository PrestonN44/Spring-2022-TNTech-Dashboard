package edu.tntech.csc2310.dashboard.data;

public class FacultyCreditHours extends SubjectCreditHours{

    private Faculty faculty;

    public FacultyCreditHours(String subject, String term, String lastname, String firstname, int creditHoursGenerated) {
        super(subject, term, creditHoursGenerated);
        this.faculty = new Faculty(firstname, lastname);
    }

    public Faculty getFaculty(){
        return this.faculty;
    }

}
