//
// jsondata: array of CourseInstance
//

function drawScheduleTable(jsondata) {

    var data = new google.visualization.DataTable();
    data.addColumn('string', 'CRN');
    data.addColumn('string', 'Subject');
    data.addColumn('string', 'Course');
    data.addColumn('string', 'Section');
    data.addColumn('number', 'Credits');
    data.addColumn('string', 'Time');
    data.addColumn('string', 'Where');
    data.addColumn('string', 'Instructor');
    data.addColumn('string', 'Enrollment');
    data.addColumn('string', 'Title');

    if (jsondata.length > 0){
        for (let i = 0; i < jsondata.length; i++){
            var rowdata = jsondata[i];

            var faculty = rowdata.faculty.firstname + " " + rowdata.faculty.lastname;
            if (rowdata.faculty.firstname == null)
                faculty = "STAFF";
            var building;
            if (rowdata.building == null)
                building = "N/A"
            else
                building = rowdata.building + " " + rowdata.room;
            var classTime = rowdata.classdays + " " + rowdata.starttime + rowdata.startam_PM + " - " + rowdata.endtime + rowdata.endam_PM;
            if (rowdata.classdays == null){
                classTime = 'N/A'
            }
            var enrollment = rowdata.studentcount + "/" + rowdata.maximumstudents;
            row = [ rowdata.crn, rowdata.subjectterm.subject, rowdata.course, rowdata.section, rowdata.credits,
                classTime, building, faculty.toString(), enrollment, rowdata.title];

            console.log(row);
            data.addRow(row);
        }
    }
    return data;
}
