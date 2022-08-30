google.charts.load('current', {'packages':['table']});

function drawCourseBySection() {

//    var subjectTextField = document.getElementById("subjectfield");
    var termTextField = document.getElementById("termfield");
    var courseTextField = document.getElementById("coursefield");
    var sectionTextField = document.getElementById("sectionfield");

    inputDropdown = document.getElementById("subjectmenu");

    selectedValue = inputDropdown.options[inputDropdown.selectedIndex];
    var subject = selectedValue.getAttribute("subject");

    var serviceURL = "coursebysection?subject=" + subject +
        "&term=" + termTextField.value +
        "&course=" + courseTextField.value +
        "&section=" + sectionTextField.value;

    console.log(serviceURL);

    let request = new XMLHttpRequest();

    request.open("GET", serviceURL);
    request.send();
    request.onload = () => {
        if (request.status == 200) {
            jsondata = JSON.parse(request.response);
            var jdAsArray = [];
            jdAsArray.push(jsondata);
            console.log(jsondata);
            console.log(jdAsArray);
            var data = drawScheduleTable(jdAsArray);
            var table = new google.visualization.Table(document.getElementById('table_div'));
            table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});
        }
    }

}
