google.charts.load('current', {'packages':['table']});

var inputDropdown;

function drawCoursesBySubject() {

    var subjectTextField = document.getElementById("subjectfield");
    var termTextField = document.getElementById("termfield");

    inputDropdown = document.getElementById("subjectmenu");

    selectedValue = inputDropdown.options[inputDropdown.selectedIndex];
    var subject = selectedValue.getAttribute("subject");

    var serviceURL = "coursesbysubject?subject=" +
        subject + "&term=" + termTextField.value;

    let request = new XMLHttpRequest();

    if (subject != null && termTextField.value != null) {

        request.open("GET", serviceURL);
        request.send();
        request.onload = () => {
            if (request.status == 200) {
                jsondata = JSON.parse(request.response).schedule;
                var data = drawScheduleTable(jsondata);
                var table = new google.visualization.Table(document.getElementById('table_div'));

                table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});
            }
        }
    }

    var schServiceURL = "schbydepartment?subject=" +
        subject + "&term=" + termTextField.value;
    console.log(schServiceURL);

    let schRequest = new XMLHttpRequest();

    var cardname = document.getElementById("cardname");

    var inputDropdown = document.getElementById("subjectmenu");
    var selectedValue = inputDropdown.options[inputDropdown.selectedIndex];

    cardname.innerText = selectedValue.value;

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