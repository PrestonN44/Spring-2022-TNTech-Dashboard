package edu.tntech.csc2310.dashboard;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.tntech.csc2310.dashboard.data.*;
import org.apache.tomcat.util.digester.ArrayStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ServiceBridgeTest {

    private static final String apiKey = "DC879704-23B8-4100-8817-0FBF2F1ECA17";
    private static final String urlString = "https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Subject=%s&Term=%s&Key=%s";

    private CourseInstance[] courses(String subject, String term) {

        String serviceString = String.format(urlString, subject.toUpperCase(), term, apiKey);
        Gson gson = new Gson();
        CourseInstance[] courses = null;

        try {
            URL url = new URL(serviceString);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            JsonReader jr = gson.newJsonReader(in);
            courses = gson.fromJson(jr, CourseInstance[].class);

            for (CourseInstance c : courses) {
                c.setSubjectterm(term);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }


    @org.junit.Test
    public void allcoursesFound() throws MalformedURLException {
        String term = "202210";
        Gson gson = new Gson();
        CourseInstance[] courses = null;
        SemesterSchedule schedule = null;

        try {
            URL url = new URL("https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Key=" + apiKey + "&ALL&Term=" + term);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            JsonReader jr = gson.newJsonReader(in);
            courses = gson.fromJson(jr, CourseInstance[].class);

            for (CourseInstance c : courses) {
                c.setSubjectterm(term);
                assertTrue(c.getSubjectterm().getTerm().contentEquals("202210"));
            }


            SubjectTerm subjectTerm = new SubjectTerm("ALL", term);
            schedule = new SemesterSchedule(subjectTerm, courses);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotNull(courses);

    }



    @org.junit.Test
    public void coursesbysubjectFound() throws MalformedURLException {
        String subject = "csc";  // valid case
        String term = "202210";
        CourseInstance[] gm = null;
        SubjectTerm subjectTerm = new SubjectTerm(subject, term);


        gm = courses(subject, term);

        for (CourseInstance c : gm) {
            c.setSubjectterm(term);
        }

        assertNotNull(gm);
        for (CourseInstance c : gm) {
            assertTrue(subjectTerm.getSubject().contentEquals("CSC"));
            assertTrue(subjectTerm.getTerm().contentEquals("202210"));
        }
        SemesterSchedule semesterSchedule = new SemesterSchedule(subjectTerm, gm);

        assertTrue(semesterSchedule.getSchedule() == gm);
        assertTrue(semesterSchedule.getSubjectTerm() == subjectTerm);

        subject = "abab"; // subject does not exist
        gm = null; // reset courses
        URL url = new URL(urlString + apiKey + "&Subject=" + subject.toUpperCase() + "&Term=" + term);
        gm = courses(subject, term);

        //assertTrue(gm. == 0);

    }


    @org.junit.Test
    public void coursesbyfacultyFound() throws MalformedURLException {
        String subject = "csc";
        String term = "202210";
        String firstname = "apRiL reNEE";
        String lastname = "CrocKETT";
        CourseInstance[] courses = this.courses(subject, term);

        ArrayList<CourseInstance> list = new ArrayList<>();

        for (CourseInstance c: courses){
            Faculty f = c.getFaculty();
            if (f.getLastname() != null && f.getFirstname() != null) {
                if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                    list.add(c);
                    assertTrue(c.getPROF().contentEquals("Crockett, April Renee"));
                }
            }
        }
        assertTrue(list.size() > 0);
        for (CourseInstance c: list) {
            assertNotNull(c.getFaculty());
            assertTrue(c.getFaculty().getFirstname().contentEquals("April Renee"));
            assertTrue(c.getFaculty().getLastname().contentEquals("Crockett"));
        }

        // FAIL TEST //
        firstname = "john";
        lastname = "doe";
        courses = this.courses(subject, term);

        list = new ArrayList<>();

        for (CourseInstance c: courses){
            Faculty f = c.getFaculty();
            if (f.getLastname() != null && f.getFirstname() != null) {
                if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                    list.add(c);
                }
            }
        }
        assertTrue(list.size() == 0);

    }


    @org.junit.Test
    public void coursebysectionFound() throws MalformedURLException {
        String subject = "mAtH";
        String term = "202210";
        String section = "006";
        String course = "1130";  //  'College Algebra' course taught by Sydney Clere

        CourseInstance cI = null;
        SubjectTerm subjectTerm = new SubjectTerm(subject, term);

        URL url = new URL(urlString + apiKey + "&Subject=" + subject.toUpperCase() + "&Term=" + term);
        CourseInstance[] courses = this.courses(subject, term);

        for (CourseInstance c: courses) {  // find course that matches section number
            if (c.getCOURSE() != null && c.getSECTION() != null) {  // null check
                if (c.getCOURSE().contentEquals(course) && c.getSECTION().contentEquals(section)) {
                    cI = c;
                    cI.setSubjectterm(term);
                }
            }
        }

        assertTrue(cI.getPROF().contentEquals("Clere, Sydney Danielle"));
        assertTrue(cI.getCOURSE().contentEquals("1130"));
        assertTrue(cI.getSECTION().contentEquals("006"));

        // TEST 2 //
        // using same subject and term
        section = "132";  // section does not exist for this course
        course = "1130";

        CourseInstance cI2 = null;

        for (CourseInstance c: courses) {  // find course that matches section number
            if (c.getCOURSE() != null && c.getSECTION() != null) {  // null check
                if (c.getCOURSE().contentEquals(course) && c.getSECTION().contentEquals(section)) {
                    cI2 = c;
                    cI2.setSubjectterm(term);
                }
            }
        }
        assertNull(cI2);
    }

    @org.junit.Test
    public void schbydepartmentFound() throws MalformedURLException {
        String subject = "ACCT";  // accounting
        String term = "202180";   // fall term, 2021
        SubjectTerm subjectTerm = new SubjectTerm(subject, term);
        SubjectCreditHours sch;
        int students = 0;
        double creditHours = 0;
        int totalSCH = 0;

        URL url = new URL(urlString + apiKey + "&Subject=" + subject.toUpperCase() + "&Term=" + term);
        CourseInstance[] courses = this.courses(subject, term);

        for (CourseInstance c: courses) {
            if (Float.toString(c.getCREDITS()) != null && Integer.toString(c.getSTUDENTCOUNT()) != null) {
                students = c.getSTUDENTCOUNT();
                creditHours = c.getCREDITS();
                totalSCH += (students * creditHours);
            }
        }
        sch = new SubjectCreditHours(subject, term, totalSCH);

        assertEquals(sch.getCreditHoursGenerated(), 2316, totalSCH);
    }

    @org.junit.Test
    public void schbyfacultyFound() throws MalformedURLException {
        String subject = "math";
        String term = "202210";
        String firstname = "marcus c";
        String lastname = "ROGERS";

        URL url = new URL(urlString + apiKey + "&Subject=" + subject.toUpperCase() + "&Term=" + term);
        CourseInstance[] courses = this.courses(subject, term);

        Faculty faculty = new Faculty(firstname, lastname);
        SubjectTerm subjectTerm = new SubjectTerm(subject, term);
        FacultyCreditHours fch;
        int students = 0;
        double creditHours = 0;
        int totalFCH = 0;

        for (CourseInstance c : courses) {
            if (c.getFaculty().getFirstname() != null && c.getFaculty().getLastname() != null && Float.toString(c.getCREDITS()) != null && Integer.toString(c.getSTUDENTCOUNT()) != null) {
                if (c.getFaculty().getFirstname().toLowerCase().contentEquals(firstname.toLowerCase()) &&
                        c.getFaculty().getLastname().toLowerCase().contentEquals(lastname.toLowerCase())) {
                    students = c.getSTUDENTCOUNT();
                    creditHours = c.getCREDITS();
                    totalFCH += (students * creditHours);
                }
            }
        }
        fch = new FacultyCreditHours(subject, term, lastname, firstname, totalFCH);

        assertTrue(students > 0);
        assertTrue(creditHours > 0);
        assertEquals(fch.getCreditHoursGenerated(), 255, totalFCH);

        // TEST 2 //
        // using same term
        firstname = "John";  // doesn't exist
        lastname = "Doe";

        url = new URL(urlString + apiKey + "&ALL&Term=" + term);  // using code 'ALL'
        courses = this.courses(subject, term);

        faculty = new Faculty(firstname, lastname);
        subjectTerm = new SubjectTerm("ALL", term);
        students = 0;  // reset count variables
        creditHours = 0;
        totalFCH = 0;

        for (CourseInstance c : courses) {
            if (c.getFaculty().getFirstname() != null && c.getFaculty().getLastname() != null && Float.toString(c.getCREDITS()) != null && Integer.toString(c.getSTUDENTCOUNT()) != null) {
                if (c.getFaculty().getFirstname().toLowerCase().contentEquals(firstname.toLowerCase()) &&
                        c.getFaculty().getLastname().toLowerCase().contentEquals(lastname.toLowerCase())) {
                    students = c.getSTUDENTCOUNT();
                    creditHours = c.getCREDITS();
                    totalFCH += (students * creditHours);
                }
            }
        }
        fch = new FacultyCreditHours(subject, term, lastname, firstname, totalFCH);

        assertTrue(students == 0);  // should not have been changed from 0
        assertTrue(creditHours == 0);
        assertEquals(fch.getCreditHoursGenerated(), 0, totalFCH);

    }

    @org.junit.Test
    public void schbydeptandtermsFound() {
        // SUCCEED CASE
        String beginterm = "202010";
        String endterm = "202210";
        String subject = "cSc";
        SubjectCreditHours sch = null;
        ArrayList<SubjectCreditHours> schList = new ArrayList<>();
        String currentTerm = beginterm;
        int scrh = 0;
        int year = 2020;  // starts at 2020, increments to 2022

        while (Integer.parseInt(currentTerm) <= Integer.parseInt(endterm)) {
            CourseInstance[] gm = this.courses(subject, currentTerm);  // get courses

            scrh = 0;  // reset credit hours count
            for (CourseInstance i : gm) {  // get all credit hours for the term
                scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
            }
            assertTrue(scrh > 0);

            sch = new SubjectCreditHours(subject, currentTerm, scrh);  // create new SubjectCreditHours object
            assertNotNull(sch);
            schList.add(sch);  // add it to the list
            assertTrue(schList.size() > 0);

            if (currentTerm.substring(4).contentEquals("10")) { // spring->summer
                currentTerm = currentTerm.substring(0, 4) + "50";
            } else if (currentTerm.substring(4).contentEquals("50")) { // summer->fall
                currentTerm = currentTerm.substring(0, 4) + "80";
            } else if (currentTerm.substring(4).contentEquals("80")) { // fall->spring, need to increment year for new year
                year += 1;
                currentTerm = Integer.toString(year) + "10";
                assertTrue(year > 2020);  // should be true after incrementing for the first time
            }
        }
        for (SubjectCreditHours s : schList) {
            assertTrue(s.getCreditHoursGenerated() > 0);
        }
        assertNotNull(schList);
        assertEquals(schList.size(), 7);  // 202010-202210 is 7 terms

        // FAIL CASE //
        beginterm = "202010";  // same term range
        endterm = "202210";
        subject = "cvbn";  // subject does not exist
        sch = null;
        schList = new ArrayList<>();
        currentTerm = beginterm;
        year = 2020;  // starts at 2020, increments to 2022

        while (Integer.parseInt(currentTerm) <= Integer.parseInt(endterm)) {
            CourseInstance[] gm = this.courses(subject, currentTerm);  // get courses

            scrh = 0;  // reset credit hours count
            for (CourseInstance i : gm) {  // get all credit hours for the term
                scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
            }
            assertTrue(scrh == 0);  // should not have been incremented

            sch = new SubjectCreditHours(subject, currentTerm, scrh);  // create new SubjectCreditHours object
            schList.add(sch);  // add it to the list
            assertTrue(sch.getCreditHoursGenerated() == 0);

            if (currentTerm.substring(4).contentEquals("10")) { // spring->summer
                currentTerm = currentTerm.substring(0, 4) + "50";
            } else if (currentTerm.substring(4).contentEquals("50")) { // summer->fall
                currentTerm = currentTerm.substring(0, 4) + "80";
            } else if (currentTerm.substring(4).contentEquals("80")) { // fall->spring, need to increment year for new year
                year += 1;
                currentTerm = Integer.toString(year) + "10";
                assertTrue(year > 2020);  // should be true after incrementing for the first time
            }

            // all elements in the list should have 0 credit hours generated
            for (SubjectCreditHours s : schList) {
                assertTrue(s.getCreditHoursGenerated() == 0);
            }
        }
    }


    @org.junit.Test
    public void schbydeptandtermlistFound() {
        // SUCCEED CASE //
        SubjectCreditHours sch = null;
        ArrayList<SubjectCreditHours> schList = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>();  // for holding all terms that were entered
        String termlist = "202210,201980,202150";  // order does not matter with this method
        String subject = "maTH";
        int scrh = 0;

        Scanner scan = new Scanner(termlist);  // scan through terms that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all terms that were entered
            terms.add(scan.next());
        }
        assertTrue(terms.get(0).contentEquals("202210"));
        assertTrue(terms.get(1).contentEquals("201980"));
        assertTrue(terms.get(2).contentEquals("202150"));


        for (String t: terms) {  // for each term, get courses and calc credit hours generated
            CourseInstance[] gm = this.courses(subject, t);

            scrh = 0;
            for (CourseInstance i : gm) {
                scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
            }
            assertTrue(scrh > 0);

            sch = new SubjectCreditHours(subject, t, scrh);
            schList.add(sch);  // add to list
            assertTrue(sch != null);
        }
        for (SubjectCreditHours s: schList) {
            assertTrue(s.getCreditHoursGenerated() > 0);
        }

        // FAIL CASE //
        sch = null;
        schList = new ArrayList<>();
        terms = new ArrayList<>();  // for holding all terms that were entered
        termlist = "202210,201980,2021502";  // term "2021502" does not exist
        subject = "maTH";  // same subject
        scrh = 0;

        scan = new Scanner(termlist);  // scan through terms that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all terms that were entered
            terms.add(scan.next());
        }
        assertTrue(terms.get(0).contentEquals("202210"));
        assertTrue(terms.get(1).contentEquals("201980"));
        assertTrue(terms.get(2).contentEquals("2021502"));  // does not exist


        for (String t: terms) {  // for each term, get courses and calc credit hours generated
            CourseInstance[] gm = this.courses(subject, t);

            scrh = 0;
            for (CourseInstance i : gm) {
                scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
            }
            if (!t.contentEquals(terms.get(2))) {  // if it is not the invalid term
                assertTrue(scrh > 0);
            }
            else {  // it is the invalid term
                assertTrue(scrh == 0);
            }

            sch = new SubjectCreditHours(subject, t, scrh);
            schList.add(sch);  // add to list
        }
        assertTrue(sch.getSubjectTerm().getSubject().contentEquals("MATH"));
        assertTrue(sch.getSubjectTerm().getTerm().contentEquals("2021502"));
        assertTrue(sch.getCreditHoursGenerated() == 0);
    }

    @org.junit.Test
    public void schbyfacultyandtermsFound() {
        // SUCCEED CASE //
        String beginterm = "201980";
        String endterm = "202150";
        String subject = "csc";
        String firstname = "April Renee";
        String lastname = "Crockett";
        FacultyCreditHours[] fchArray = null;
        FacultyCreditHours fch = null;
        ArrayList<FacultyCreditHours> fchList = new ArrayList<>();
        String currentTerm = beginterm;
        int scrh = 0;
        int year = Integer.parseInt(currentTerm.substring(0, 4));

        while (Integer.parseInt(currentTerm) <= Integer.parseInt(endterm)) {
            CourseInstance[] gm = this.courses(subject, currentTerm);

            scrh = 0;  // reset count
            for (CourseInstance i : gm) {
                Faculty f = i.getFaculty();
                if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                    if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                        scrh += i.getSTUDENTCOUNT() * i.getCREDITS();  // get credits generated for the class
                        assertTrue(i.getPROF().contentEquals("Crockett, April Renee"));
                        assertTrue(firstname.contentEquals(i.getFaculty().getFirstname()));
                        assertTrue(lastname.contentEquals(i.getFaculty().getLastname()));
                    }
                }
            }
            assertTrue(scrh > 0);
            fch = new FacultyCreditHours(subject, currentTerm, lastname, firstname, scrh);  // create new FacultyCreditHours
            fchList.add(fch);  // add to list

            if (currentTerm.substring(4).contentEquals("10")) { // spring->summer
                currentTerm = currentTerm.substring(0, 4) + "50";
            } else if (currentTerm.substring(4).contentEquals("50")) { // summer->fall
                currentTerm = currentTerm.substring(0, 4) + "80";
            } else if (currentTerm.substring(4).contentEquals("80")) { // fall->spring, need to increment year for new year
                year += 1;
                currentTerm = Integer.toString(year) + "10";
            }

        }
        assertTrue(fchList.size() == 6);  // 201980-202150 is 6 semesters

        if (fchList.size() > 0)
            fchArray = new FacultyCreditHours[fchList.size()];

        for (int i = 0; i < fchList.size(); i++) {
            fchArray[i] = fchList.get(i);
        }
        assertNotNull(fchArray);

        // FAIL CASE //
        // using same first and last name and subject, reversing begin and end term
        beginterm = "202150";
        endterm = "201980";  // end term is smaller than begin term, so this should not work
        fchArray = null;  // reset to null for test purposes
        fch = null;
        fchList = new ArrayList<>();
        currentTerm = beginterm;
        scrh = 0;
        year = Integer.parseInt(currentTerm.substring(0, 4));

        while (Integer.parseInt(currentTerm) <= Integer.parseInt(endterm)) {  // should not enter this
            CourseInstance[] gm = this.courses(subject, currentTerm);

            scrh = 0;  // reset count
            for (CourseInstance i : gm) {
                Faculty f = i.getFaculty();
                if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                    if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                        scrh += i.getSTUDENTCOUNT() * i.getCREDITS();  // get credits generated for the class
                    }
                }
            }

            fch = new FacultyCreditHours(subject, currentTerm, lastname, firstname, scrh);  // create new FacultyCreditHours
            fchList.add(fch);  // add to list

            if (currentTerm.substring(4).contentEquals("10")) { // spring->summer
                currentTerm = currentTerm.substring(0, 4) + "50";
            } else if (currentTerm.substring(4).contentEquals("50")) { // summer->fall
                currentTerm = currentTerm.substring(0, 4) + "80";
            } else if (currentTerm.substring(4).contentEquals("80")) { // fall->spring, need to increment year for new year
                year += 1;
                currentTerm = Integer.toString(year) + "10";
            }

        }
        assertTrue(fchList.size() == 0);  // should never have entered while loop

        if (fchList.size() > 0)  // will not enter, stays null
            fchArray = new FacultyCreditHours[fchList.size()];

        for (int i = 0; i < fchList.size(); i++) {
            fchArray[i] = fchList.get(i);
        }
        assertNull(fchArray);
    }

    @org.junit.Test
    public void schbyfacultyandtermlistFound() {
        // SUCCEED CASE //
        FacultyCreditHours[] fchArray;
        FacultyCreditHours fch = null;
        ArrayList<FacultyCreditHours> fchList = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>();
        String subject = "math";
        String termlist = "202150,201880,202010";
        String firstname = "Marcus C";
        String lastname = "Rogers";
        int scrh = 0;

        Scanner scan = new Scanner(termlist);  // scan terms that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all terms that were entered
            terms.add(scan.next());
        }
        assertTrue(terms.get(0).contentEquals("202150"));
        assertTrue(terms.get(1).contentEquals("201880"));
        assertTrue(terms.get(2).contentEquals("202010"));  // all valid terms

        for (String t: terms) {  // get courses for each term entered and calc credit hours
            CourseInstance[] gm = this.courses(subject, t);

            scrh = 0;  // reset count
            for (CourseInstance i : gm) {
                Faculty f = i.getFaculty();
                if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                    if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                        scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
                        assertTrue(i.getPROF().contentEquals("Rogers, Marcus C"));
                        assertTrue(i.getFaculty().getFirstname().contentEquals(firstname));
                        assertTrue(i.getFaculty().getLastname().contentEquals(lastname));
                    }
                }
            }
            assertTrue(scrh > 0);

            fch = new FacultyCreditHours(subject, t, lastname, firstname, scrh);
            fchList.add(fch);  // add new fch to list
            assertTrue(fchList.size() > 0);
        }

        // FAIL CASE //
        // same subject and faculty, with only 1 valid term
        fch = null;
        fchList = new ArrayList<>();
        terms = new ArrayList<>();

        termlist = "202170,20185000,202010";
        scrh = 0;

        scan = new Scanner(termlist);  // scan terms that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all terms that were entered
            terms.add(scan.next());
        }
        assertTrue(terms.get(0).contentEquals("202170"));
        assertTrue(terms.get(1).contentEquals("20185000"));
        assertTrue(terms.get(2).contentEquals("202010"));  // only valid term

        for (String t: terms) {  // get courses for each term entered and calc credit hours
            CourseInstance[] gm = this.courses(subject, t);

            scrh = 0;  // reset count
            for (CourseInstance i : gm) {
                Faculty f = i.getFaculty();
                if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                    if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                        scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
                        assertTrue(i.getPROF().contentEquals("Rogers, Marcus C"));
                        assertTrue(i.getFaculty().getFirstname().contentEquals(firstname));
                        assertTrue(i.getFaculty().getLastname().contentEquals(lastname));
                        assertTrue(t.contentEquals("202010"));  // only term it should enter the loop for
                    }
                }
            }

            if (scrh > 0) {
                fch = new FacultyCreditHours(subject, t, lastname, firstname, scrh);
                fchList.add(fch);  // add new fch to list
            }

        }
        assertTrue(fchList.size() == 1);  // only 1 term was valid
    }

    @org.junit.Test
    public void coursesbycrnlistFound() {
        String term = "202210";
        ArrayList<CourseInstance> foundCourses = new ArrayList<>();
        ArrayList<String> crns = new ArrayList<>();
        String crnlist = "13519,10218,12537"; // 13519 Principles of Computing, 10218 Transitional Algebra, 12537 English Composition I

        Scanner scan = new Scanner(crnlist);  // scan crns that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all crns that were entered
            crns.add(scan.next());
        }
        assertTrue(crns.get(0).contentEquals("13519")); // CSC course
        assertTrue(crns.get(1).contentEquals("10218")); // MATH course
        assertTrue(crns.get(2).contentEquals("12537")); // ENGL course

        // get all courses for a term, using subject code 'ALL'
        Gson gson = new Gson();
        CourseInstance[] gm = null;
        try {
            URL url = new URL("https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Key=" + apiKey + "&ALL&Term=" + term);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            JsonReader jr = gson.newJsonReader(in);
            gm = gson.fromJson(jr, CourseInstance[].class);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String c: crns) {  // for each crn entered, find course that matches it
            for (CourseInstance i : gm) {
                if (i.getCRN() != null) {  // null check
                    if (i.getCRN().contentEquals(c)) {
                        i.setSubjectterm(term);  // set subject term
                        foundCourses.add(i);  // add to courses list
                        break;
                    }
                }
            }
        }
        assertNotNull(foundCourses);
        assertTrue(foundCourses.size() == 3);  // 3 valid crns
        assertTrue(foundCourses.get(0).getTITLE().contentEquals("Principles of Computing"));
        assertTrue(foundCourses.get(1).getTITLE().contentEquals("Transitional Algebra"));
        assertTrue(foundCourses.get(2).getTITLE().contentEquals("English Composition I"));

        // FAIL CASE //
        // using same term, with only 1 valid crn
        foundCourses = new ArrayList<>();
        crns = new ArrayList<>();
        crnlist = "12121212,13519,-50"; // 13519 Principles of Computing is only valid crn

        scan = new Scanner(crnlist);  // scan crns that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all crns that were entered
            crns.add(scan.next());
        }
        assertTrue(crns.get(0).contentEquals("12121212"));
        assertTrue(crns.get(1).contentEquals("13519")); // only valid crn
        assertTrue(crns.get(2).contentEquals("-50"));

        for (String c: crns) {  // for each crn entered, find course that matches it
            for (CourseInstance i : gm) {
                if (i.getCRN() != null) {  // null check
                    if (i.getCRN().contentEquals(c)) {
                        i.setSubjectterm(term);  // set subject term
                        foundCourses.add(i);  // add to courses list
                        assertTrue(i.getCRN().contentEquals("13519"));
                        assertTrue(i.getTITLE().contentEquals("Principles of Computing"));
                        break;
                    }
                }
            }
        }
        assertNotNull(foundCourses);
        assertTrue(foundCourses.size() == 1);  // 1 valid crn
    }

    @org.junit.Test
    public void facultybysubjectFound() {
        // SUCCEED CASE //
        ArrayList<Faculty> foundFaculty = new ArrayList<>();
        Faculty[] faculty;
        String subject = "csc";
        String term = "202210";

        CourseInstance[] gm = this.courses(subject, term);

        int count;  // for checking if a faculty needs to be added or not
        for (CourseInstance i: gm) {
            count = 0;  // reset count
            if (i.getFaculty().getFirstname() != null && i.getFaculty().getLastname() != null) {  // null check
                if (foundFaculty.size() != 0) {  // if there is at least 1 element
                    for (Faculty f: foundFaculty) {
                        if (f.getFirstname().contentEquals(i.getFaculty().getFirstname()) &&
                                f.getLastname().contentEquals(i.getFaculty().getLastname())) {
                            count = 1;
                            break;
                        }
                    }
                    if (count == 0) {  // if count is 0, the faculty did not match anyone else in the list
                        foundFaculty.add(i.getFaculty());  // add faculty
                    }
                }
                else {  // first faculty needs to be added
                    assertTrue(i.getPROF().contentEquals("Gannod, Barbara ")); // first person in csc list for 202210 term
                    foundFaculty.add(i.getFaculty());  // add faculty
                }
            }
        }
        assertTrue(foundFaculty.get(0).getLastname().contentEquals("Gannod"));
        assertTrue(foundFaculty.get(1).getLastname().contentEquals("Focht"));
        assertTrue(foundFaculty.get(2).getLastname().contentEquals("Crockett"));

        // FAIL CASE //
        // same subject, invalid term
        foundFaculty = new ArrayList<>();
        faculty = null;
        term = "2022100";

        gm = this.courses(subject, term);

        for (CourseInstance i: gm) {
            count = 0;  // reset count
            if (i.getFaculty().getFirstname() != null && i.getFaculty().getLastname() != null) {  // null check
                if (foundFaculty.size() != 0) {  // if there is at least 1 element
                    for (Faculty f: foundFaculty) {
                        if (f.getFirstname().contentEquals(i.getFaculty().getFirstname()) &&
                                f.getLastname().contentEquals(i.getFaculty().getLastname())) {
                            count = 1;
                            break;
                        }
                    }
                    if (count == 0) {  // if count is 0, the faculty did not match anyone else in the list
                        foundFaculty.add(i.getFaculty());  // add faculty
                    }
                }
                else {  // first faculty needs to be added
                    foundFaculty.add(i.getFaculty());  // add faculty
                }
            }
        }
        if (foundFaculty.size() > 0) {
            faculty = new Faculty[foundFaculty.size()];
        }
        assertNull(faculty);
    }

    @org.junit.Test
    public void getallsubjectsFound() {
        // SUCCEED CASE //
        String term = "202010";
        ArrayList<String> foundSubjects = new ArrayList<>();

        // get all courses with code 'ALL'
        Gson gson = new Gson();
        CourseInstance[] gm = null;
        try {
            URL url = new URL("https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Key=" + apiKey + "&ALL&Term=" + term);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            JsonReader jr = gson.newJsonReader(in);
            gm = gson.fromJson(jr, CourseInstance[].class);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int count;  // to check if a subject was added
        for (CourseInstance i : gm) {
            count = 0;  // reset count
            i.setSubjectterm(term);  // set subject term
            if (i.getSubjectterm().getSubject() != null) {  // null check
                if (foundSubjects.size() != 0) {
                    for (String s : foundSubjects) {
                        if (s.contentEquals(i.getSubjectterm().getSubject())) {
                            count = 1;
                            break;
                        }
                    }
                    if (count == 0) {  // if count is 0, subject did not match and must be added to list
                        foundSubjects.add(i.getSubjectterm().getSubject());
                    }
                } else {  // first subject to be added to the list
                    foundSubjects.add(i.getSubjectterm().getSubject());
                    assertTrue(i.getSubjectterm().getSubject().contentEquals("ACCT")); // acct is first in list for 202010 term
                }
            }
        }
        assertTrue(foundSubjects.get(0).contentEquals("ACCT"));
        assertTrue(foundSubjects.get(1).contentEquals("ADMN"));
        assertTrue(foundSubjects.get(2).contentEquals("AGBE"));

        // FAIL CASE //
        // using invalid term
        term = "2020103";
        foundSubjects = new ArrayList<>();

        // get all courses with code 'ALL'
        gson = new Gson();
        gm = null;
        try {
            URL url = new URL("https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Key=" + apiKey + "&ALL&Term=" + term);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            JsonReader jr = gson.newJsonReader(in);
            gm = gson.fromJson(jr, CourseInstance[].class);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (CourseInstance i : gm) {
            count = 0;  // reset count
            i.setSubjectterm(term);  // set subject term
            if (i.getSubjectterm().getSubject() != null) {  // null check
                if (foundSubjects.size() != 0) {
                    for (String s : foundSubjects) {
                        if (s.contentEquals(i.getSubjectterm().getSubject())) {
                            count = 1;
                            break;
                        }
                    }
                    if (count == 0) {  // if count is 0, subject did not match and must be added to list
                        foundSubjects.add(i.getSubjectterm().getSubject());
                    }
                } else {  // first subject to be added to the list
                    foundSubjects.add(i.getSubjectterm().getSubject());
                }
            }
        }
        assertTrue(foundSubjects.size() == 0);

    }


}