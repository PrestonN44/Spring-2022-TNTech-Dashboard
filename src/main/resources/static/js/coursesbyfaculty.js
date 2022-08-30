google.charts.load('current', {'packages':['table']});

function drawCoursesByFaculty() {

    //var subjectTextField = document.getElementById("subjectfield");
    var termTextField = document.getElementById("termfield");

    inputDropdown = document.getElementById("codemenu");
    subjectDropdown = document.getElementById("subjectmenu");

    selectedValue = inputDropdown.options[inputDropdown.selectedIndex];
    var lastTextField = selectedValue.getAttribute("lastname");
    var firstTextField = selectedValue.getAttribute("firstname");

    selectedSubject = subjectDropdown.options[subjectDropdown.selectedIndex];
    var subjectTextField = selectedSubject.getAttribute("subject");

    var serviceURL = "coursesbyfaculty?subject=" +
        (subjectTextField).toUpperCase() +
        "&term=" + termTextField.value +
        "&lastname=" + lastTextField +
        "&firstname=" + firstTextField;

    console.log(serviceURL);

    let coursesRequest = new XMLHttpRequest();

    coursesRequest.open("GET", serviceURL);
    coursesRequest.send();
    coursesRequest.onload = () => {
        if (coursesRequest.status == 200) {
            jsondata = JSON.parse(coursesRequest.response);
            var data = drawScheduleTable(jsondata);
            var table = new google.visualization.Table(document.getElementById('table_div'));
            table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});
        }
    }

    var schServiceURL = "schbyfaculty?subject=" +
        subjectTextField.toUpperCase() +
        "&term=" + termTextField.value +
        "&lastname=" + lastTextField +
        "&firstname=" + firstTextField;
    console.log(schServiceURL);

    let schRequest = new XMLHttpRequest();

    changecard();

    schRequest.open("GET", schServiceURL);
    schRequest.send();
    schRequest.onload = () => {
        if (schRequest.status == 200) {
            jsondata = JSON.parse(schRequest.response);
            var schTotal = document.getElementById("schTotal");
            schTotal.innerText = "Student credit hours: " + jsondata.creditHoursGenerated;
        }
    }

}


function findGetParameter(parameterName) {
    var result = null,
        tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
            tmp = item.split("=");
            if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        });
    return result;
}