package edu.tntech.csc2310.dashboard.data;

public class Faculty implements Comparable<Faculty>{

    public String firstname;
    public String lastname;

    public Faculty(String firstname, String lastname) {
        this.firstname = firstname.trim();
        this.lastname = lastname.trim();
    }

    public Faculty(String iname){
        if (iname != null) {
            String[] name = iname.split(",");
            this.lastname = name[0].trim();
            this.firstname = name[1].trim();
        }
    }

    public Faculty(){
        this.firstname = null;
        this.lastname = null;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    @Override
    public int compareTo(Faculty o) {
        int result = 0;
        if (o.firstname != null && o.lastname != null && this.lastname != null && this.firstname != null){
            if (!this.lastname.contentEquals(o.lastname))
                result = this.lastname.compareTo(o.lastname);
            else
                result = this.firstname.compareTo(o.firstname);
        } else {
            if (this.lastname != null && this.firstname != null)
                result = -1;
            else
                result = 1;
        }
        return result;
    }

    public String toString(){
        return this.lastname + ", " + this.firstname;
    }
}
