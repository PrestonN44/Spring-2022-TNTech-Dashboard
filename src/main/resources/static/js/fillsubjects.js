function fillsubjects() {

    var termTextField = document.getElementById("termfield");

    if (termTextField.value == null)
        termTextField.value = "202210";

    var serviceURL = "getallsubjects?term=" + termTextField.value;

    let request = new XMLHttpRequest();
    var list = document.getElementById("subjectmenu");

    request.open("GET", serviceURL);
    request.send();
    request.onload = () => {
        if (request.status == 200) {
            jsondata = JSON.parse(request.response);

            jsondata.forEach(function (item) {
               let opt = document.createElement("option");
               if (item != null) {
                  opt.innerText = item;
                  opt.setAttribute("subject", item);
                  list.appendChild(opt);
               }
            });
        }
    }
}



