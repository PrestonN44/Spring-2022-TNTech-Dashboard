package edu.tntech.csc2310.dashboard;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.tntech.csc2310.dashboard.data.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.TreeSet;

@RestController
public class ServiceBridge {

    private static final String apiKey = "DC879704-23B8-4100-8817-0FBF2F1ECA17";
    private static final String urlString = "https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Subject=%s&Term=%s&Key=%s";

    /**
     * Get all courses for a subject and term by accessing the TN Tech webservice, used by all other functions
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @return a list of all found course instances
     */
    private CourseInstance[] courses(String subject, String term) {

        String serviceString = String.format(urlString, subject.toUpperCase(), term, apiKey);
        Gson gson = new Gson();
        CourseInstance[] courses = null;

        try {
            URL url = new URL(serviceString);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            JsonReader jr = gson.newJsonReader(in);
            courses = gson.fromJson(jr, CourseInstance[].class);

            for (CourseInstance c: courses){
                c.setSubjectterm(term);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Get all courses offered at Tech for a user-provided term
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @return the list of all courses for the entire semester
     */
    @GetMapping("/allcourses")
    public SemesterSchedule allcourses(
            @RequestParam(value = "term", defaultValue = "na") String term
    ) {

        Gson gson = new Gson();
        CourseInstance[] gm = null;
        SemesterSchedule schedule = null;

        try {
            URL url = new URL("https://portapit.tntech.edu/express/api/unprotected/getCourseInfoByAPIKey.php?Key=" + apiKey + "&ALL&Term=" + term);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            JsonReader jr = gson.newJsonReader(in);
            gm = gson.fromJson(jr, CourseInstance[].class);

            for (CourseInstance c: gm){
                c.setSubjectterm(term);
            }

            SubjectTerm subjectTerm = new SubjectTerm("ALL", term);
            schedule = new SemesterSchedule(subjectTerm, gm);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    /**
     * Find all courses that are being offered within a department for a given term
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @return the list of all courses being offered that semester within a department
     */
    @GetMapping("/coursesbysubject")
    public SemesterSchedule coursesbysubject(
            @RequestParam(value = "subject", defaultValue = "CSC") String subject,
            @RequestParam(value = "term", defaultValue = "202210") String term
    ){
        CourseInstance[] courses = this.courses(subject, term);
        SubjectTerm subjectTerm = new SubjectTerm(subject, term);
        SemesterSchedule schedule = new SemesterSchedule(subjectTerm, courses);
        return schedule;
    }

    /**
     * Find all courses that are being taught by a particular faculty member for a given term
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param lastname Faculty member's last name
     * @param firstname Faculty member's first name
     * @return the list of all courses being taught by a faculty member for a term
     */
    @GetMapping("/coursesbyfaculty")
    public ArrayList<CourseInstance> coursesbyfaculty(
            @RequestParam(value = "subject", defaultValue = "CSC") String subject,
            @RequestParam(value = "term", defaultValue = "202210") String term,
            @RequestParam(value = "lastname", defaultValue = "Gannod") String lastname,
            @RequestParam(value = "firstname", defaultValue = "Gerald C") String firstname
    ) {

        CourseInstance[] courses = this.courses(subject, term);

        ArrayList<CourseInstance> list = new ArrayList<>();

        for (CourseInstance c: courses){
            Faculty f = c.getFaculty();
            if (f.getLastname() != null && f.getFirstname() != null) {
                if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase()))
                    list.add(c);
            }
        }
        return list;
    }

    /**
     * Find a class matching the provided section number and course
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param course Course number (2310, 1300, 3710...)
     * @param section Class' corresponding section number (2310-002, 1300-102...)
     * @return a single course instance that matched the course and section number
     */
    @GetMapping("/coursebysection")
    public CourseInstance coursebysection(
            @RequestParam(value = "subject", defaultValue = "CSC") String subject,
            @RequestParam(value = "term", defaultValue = "202210") String term,
            @RequestParam(value = "course", defaultValue = "2310") String course,
            @RequestParam(value = "section", defaultValue = "001") String section
    ) {
        CourseInstance[] courses = this.courses(subject, term);

        CourseInstance result = null;
        for (CourseInstance c: courses){
            if (c.getCOURSE().contentEquals(course) && c.getSECTION().contentEquals(section))
                result = c;
        }
        return result;
    }

    /**
     * Get all credit hours that were generated by a department for a term
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @return the department that was searched for along with the credit hours that were generated by it
     */
    @GetMapping("/schbydepartment")
    public SubjectCreditHours creditHours(
            @RequestParam(value = "subject", defaultValue = "CSC") String subject,
            @RequestParam(value = "term", defaultValue = "202210") String term
    ) {

        CourseInstance[] gm = this.courses(subject, term);
        int scrh = 0;

        for (CourseInstance i : gm){
            scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
        }
        SubjectCreditHours sch = new SubjectCreditHours(subject, term, scrh);
        return sch;
    }

    /**
     * Get all credit hours generated by a particular faculty member for a given term
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param lastname Faculty member's lastname
     * @param firstname Faculty member's firstname
     * @return the faculty member along with the credit hours they generated for a term
     */
    @GetMapping("/schbyfaculty")
    public FacultyCreditHours creditHoursByFaculty(
            @RequestParam(value = "subject", defaultValue = "CSC") String subject,
            @RequestParam(value = "term", defaultValue = "202210") String term,
            @RequestParam(value = "lastname", defaultValue = "Gannod") String lastname,
            @RequestParam(value = "firstname", defaultValue = "Gerald C") String firstname
    ) {
        CourseInstance[] courses = this.courses(subject, term);
        int scrh = 0;
        for (CourseInstance c : courses){
            Faculty f = c.getFaculty();
            if (f.getLastname() != null && f.getFirstname() != null) {
                if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase()))
                    scrh += c.getSTUDENTCOUNT() * c.getCREDITS();
            }
        }
        FacultyCreditHours sch = new FacultyCreditHours(subject, term, lastname, firstname, scrh);
        return sch;
    }

    /**
     * Find the total credit hours generated each term for a department for a range of terms.
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param beginterm Term code to begin calculating credit hours at
     * @param endterm Term code to end calculating credit hours at (inclusive)
     * @return the list of terms that were found in order for the given department and the respective generated credit hours for that term
     */
    @GetMapping("/schbydeptandterms")
    public SubjectCreditHours[] schbydeptandterms(
            @RequestParam(value = "subject", defaultValue = "na") String subject,
            @RequestParam(value = "term", defaultValue = "na") String term,
            @RequestParam(value = "beginterm", defaultValue = "na") String beginterm,
            @RequestParam(value = "endterm", defaultValue = "na") String endterm
    ) {
        SubjectCreditHours[] schArray;
        SubjectCreditHours sch = null;
        ArrayList<SubjectCreditHours> schList = new ArrayList<>();
        String currentTerm = beginterm;
        int scrh = 0;
        int year = Integer.parseInt(currentTerm.substring(0, 4));  // for incrementing

        while (Integer.parseInt(currentTerm) <= Integer.parseInt(endterm)) {
            CourseInstance[] gm = this.courses(subject, currentTerm);  // get courses

            scrh = 0;  // reset credit hours count
            for (CourseInstance i : gm) {  // get all credit hours for the department
                scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
            }

            sch = new SubjectCreditHours(subject, currentTerm, scrh);  // create new SubjectCreditHours object
            schList.add(sch);  // add it to the list

            if (currentTerm.substring(4).contentEquals("10")) { // spring->summer
                currentTerm = currentTerm.substring(0, 4) + "50";
            } else if (currentTerm.substring(4).contentEquals("50")) { // summer->fall
                currentTerm = currentTerm.substring(0, 4) + "80";
            } else if (currentTerm.substring(4).contentEquals("80")) { // fall->spring, need to increment year for new year
                year += 1;
                currentTerm = Integer.toString(year) + "10";
            }
        }

        schArray = new SubjectCreditHours[schList.size()];

        for (int i = 0; i < schList.size(); i++) {  // set array elements equal to list elements
            schArray[i] = schList.get(i);
        }

        return schArray;
    }

    /**
     * Find the total credit hours generated each term by a department for a given list of terms.
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param termlist List of term codes, seperated by commas
     * @return the list of user-provided terms and generated credit hours by the given department for those terms
     */
    @GetMapping("/schbydeptandtermlist")
    public SubjectCreditHours[] schbydeptandtermlist(
            @RequestParam(value = "subject", defaultValue = "na") String subject,
            @RequestParam(value = "term", defaultValue = "na") String term,
            @RequestParam(value = "termlist", defaultValue = "na") String termlist
    ) {
        SubjectCreditHours[] schArray;
        SubjectCreditHours sch = null;
        ArrayList<SubjectCreditHours> schList = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>();  // for holding all terms that were entered
        int scrh = 0;

        Scanner scan = new Scanner(termlist);  // scan through terms that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all terms that were entered
            terms.add(scan.next());
        }

        for (String t: terms) {  // for each term, get courses and calc credit hours generated
            CourseInstance[] gm = this.courses(subject, t);

            scrh = 0;
            for (CourseInstance i : gm) {
                scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
            }

            sch = new SubjectCreditHours(subject, t, scrh);
            schList.add(sch);  // add to list
        }

        schArray = new SubjectCreditHours[schList.size()];

        for (int i = 0; i < schList.size(); i++) {  // set array equal to list
            schArray[i] = schList.get(i);
        }

        return schArray;
    }

    /**
     * Find the total credit hours generated each term by a faculty member for a range of terms.
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param lastname Faculty member's lastname
     * @param firstname Faculty member's first name and middle name or inital (if applicable)
     * @param beginterm Term code to begin calculating credit hours at
     * @param endterm Term code to end calculating credit hours at (inclusive)
     * @return the list of terms that were found in order for the given faculty and their respective generated credit hours for that term
     */
    @GetMapping("/schbyfacultyandterms")
    public FacultyCreditHours[] schbyfacultyandterms (
            @RequestParam(value = "subject", defaultValue = "na") String subject,
            @RequestParam(value = "term", defaultValue = "na") String term,
            @RequestParam(value = "lastname", defaultValue = "na") String lastname,
            @RequestParam(value = "firstname", defaultValue = "na") String firstname,
            @RequestParam(value = "beginterm", defaultValue = "na") String beginterm,
            @RequestParam(value = "endterm", defaultValue = "na") String endterm
    ) {
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
                if (!firstname.contentEquals("na") && !lastname.contentEquals("na")) {  // if a full name was entered
                    if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                        if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                            scrh += i.getSTUDENTCOUNT() * i.getCREDITS();  // get credits generated for the class
                            firstname = f.getFirstname();
                            lastname = f.getLastname();
                        }
                    }
                }
                else if (firstname.contentEquals("na") && !lastname.contentEquals("na")) {  // only last name was entered
                    if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                        if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase())) {
                            scrh += i.getSTUDENTCOUNT() * i.getCREDITS();  // get credits generated for the class
                            lastname = f.getLastname();
                        }
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

        fchArray = new FacultyCreditHours[fchList.size()];

        for (int i = 0; i < fchList.size(); i++) {  // set array elements equal to list elements
            fchArray[i] = fchList.get(i);
        }

        return fchArray;
    }

    /**
     * Find the total credit hours generated each term by a faculty member for a given list of terms.
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param lastname Faculty member's lastname
     * @param firstname Faculty member's first name and middle name or initial (if applicable)
     * @param termlist List of term codes, seperated by commas
     * @return the list of user-provided terms and generated credit hours by the given faculty member for those terms
     */
    @GetMapping("/schbyfacultyandtermlist")
    public FacultyCreditHours[] schbyfacultyandtermlist (
            @RequestParam(value = "subject", defaultValue = "na") String subject,
            @RequestParam(value = "term", defaultValue = "na") String term,
            @RequestParam(value = "lastname", defaultValue = "na") String lastname,
            @RequestParam(value = "firstname", defaultValue = "na") String firstname,
            @RequestParam(value = "termlist", defaultValue = "na") String termlist
    ) {
        FacultyCreditHours[] fchArray;
        FacultyCreditHours fch = null;
        ArrayList<FacultyCreditHours> fchList = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>();
        int scrh = 0;

        Scanner scan = new Scanner(termlist);  // scan terms that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all terms that were entered
            terms.add(scan.next());
        }

        for (String t: terms) {  // get courses for each term entered and calc credit hours
            CourseInstance[] gm = this.courses(subject, t);

            scrh = 0;  // reset count
            for (CourseInstance i : gm) {
                Faculty f = i.getFaculty();
                if (!firstname.contentEquals("na") && !lastname.contentEquals("na")) {  // full name was entered
                    if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                        if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase()) && firstname.toLowerCase().contentEquals(f.getFirstname().toLowerCase())) {
                            scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
                            firstname = f.getFirstname();
                            lastname = f.getLastname();
                        }
                    }
                }
                else if (firstname.contentEquals("na") && !lastname.contentEquals("na")) {  // only last name was entered
                    if (f.getLastname() != null && f.getFirstname() != null) {  // null check
                        if (lastname.toLowerCase().contentEquals(f.getLastname().toLowerCase())) {
                            scrh += i.getSTUDENTCOUNT() * i.getCREDITS();
                            lastname = f.getLastname();
                        }
                    }
                }
            }

            fch = new FacultyCreditHours(subject, t, lastname, firstname, scrh);
            fchList.add(fch);  // add new fch to list
        }

        fchArray = new FacultyCreditHours[fchList.size()];

        for (int i = 0; i < fchList.size(); i++) {  // set array equal to list
            fchArray[i] = fchList.get(i);
        }

        return fchArray;
    }

    /**
     * Find the courses associated with a given list of crns for a given term.
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @param crnlist List of course crns, seperated by commas
     * @return the list of courses associated with the user-provided crns for a term
     */
    @GetMapping("/coursesbycrnlist")
    public CourseInstance[] coursesbycrnlist (
            @RequestParam(value = "term", defaultValue = "na") String term,
            @RequestParam(value = "crnlist", defaultValue = "na") String crnlist
    ) {
        CourseInstance[] courses = null;
        ArrayList<CourseInstance> foundCourses = new ArrayList<>();
        ArrayList<String> crns = new ArrayList<>();

        Scanner scan = new Scanner(crnlist);  // scan crns that were entered
        scan.useDelimiter(",");

        while (scan.hasNext()) {  // get all crns that were entered
            crns.add(scan.next());
        }

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
        courses = new CourseInstance[foundCourses.size()];

        for (int i = 0; i < foundCourses.size(); i++) {  // set course array equal to list
            courses[i] = foundCourses.get(i);
        }

        return courses;
    }

    /**
     * Find all faculty members in a given department for a term
     * @param subject Department code (CSC, MATH, ENGL, etc...)
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @return the list of all faculty members teaching in a given department for a term
     */
    @GetMapping("/facultybysubject")
    public Faculty[] facultybysubject (
            @RequestParam(value = "subject", defaultValue = "na") String subject,
            @RequestParam(value = "term", defaultValue = "na") String term
    ) {
        ArrayList<Faculty> foundFaculty = new ArrayList<>();
        Faculty[] faculty;

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
                    foundFaculty.add(i.getFaculty());  // add faculty
                }
            }
        }

        faculty = new Faculty[foundFaculty.size()];

        for (int i = 0; i < foundFaculty.size(); i++) {  // set faculty array equal to list
            faculty[i] = foundFaculty.get(i);
        }

        return faculty;
    }

    /**
     * Find all subjects that are offered at Tech for a given term
     * @param term Term code (year + "10" (Spring), + "50" (Summer), or + "80" (Fall))
     * @return the list of all subjects that are being offered for a given term
     */
    @GetMapping("/getallsubjects")
    public String[] getallsubjects (
            @RequestParam(value = "term", defaultValue = "na") String term
    ) {
        String[] subjects;
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
                }
            }
        }

        subjects = new String[foundSubjects.size()];

        for (int i = 0; i< foundSubjects.size(); i++) {  // set subject array equal to list
            subjects[i] = foundSubjects.get(i);
        }

        return subjects;
    }

}
