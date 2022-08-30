package edu.tntech.csc2310.dashboard.data;

public class CourseInstance {

    private String CRN;
    private String DEPARTMENT;
    private String COURSE;
    private String SECTION;
    private float CREDITS;
    private boolean ISTIMEDETERMINED;
    private String STARTTIME;
    private String STARTAM_PM;
    private String ENDTIME;
    private String ENDAM_PM;
    private String CLASSDAYS;
    private boolean ISLOCDETERMINED;
    private String BUILDING;
    private String ROOM;
    private boolean ISONLINE;
    private String PROF;
    private Faculty faculty;
    private int STUDENTCOUNT;
    private int MAXIMUMSTUDENTS;
    private boolean ISOPEN;
    private String TITLE;
    private SubjectTerm subjectterm;

    public void setSubjectterm(String term){
        if (this.subjectterm == null){
            this.subjectterm = new SubjectTerm(this.DEPARTMENT, term);
        }
    }

    public SubjectTerm getSubjectterm() {
        return subjectterm;
    }

    public String getCRN() {
        return CRN;
    }

    public Faculty getFaculty() {
        if (this.faculty == null)
            this.faculty = new Faculty(this.PROF);
        return this.faculty;
    }

    public String getCOURSE() {
        return COURSE;
    }

    public String getSECTION() {
        return SECTION;
    }

    public float getCREDITS() {
        return CREDITS;
    }

    public boolean isISTIMEDETERMINED() {
        return ISTIMEDETERMINED;
    }

    public String getSTARTTIME() {
        return STARTTIME;
    }

    public String getSTARTAM_PM() {
        return STARTAM_PM;
    }

    public String getENDTIME() {
        return ENDTIME;
    }

    public String getENDAM_PM() {
        return ENDAM_PM;
    }

    public String getCLASSDAYS() {
        return CLASSDAYS;
    }

    public boolean isISLOCDETERMINED() {
        return ISLOCDETERMINED;
    }

    public String getBUILDING() {
        return BUILDING;
    }

    public String getROOM() {
        return ROOM;
    }

    public boolean isISONLINE() {
        return ISONLINE;
    }

    public String getPROF() {
        return PROF;
    }

    public int getSTUDENTCOUNT() {
        return STUDENTCOUNT;
    }

    public int getMAXIMUMSTUDENTS() {
        return MAXIMUMSTUDENTS;
    }

    public boolean isISOPEN() {
        return ISOPEN;
    }

    public String getTITLE() {
        return TITLE;
    }
}
