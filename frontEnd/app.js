const app = document.getElementById('root');

const container = document.createElement('div');
container.setAttribute('class', 'container');


var ips = ['http://10.0.0.5/', 'http://10.0.0.6/', 'http://10.0.0.7/']
var clientIDs = [];

var url = ips[Math.floor(Math.random() * 3)];

var request = new XMLHttpRequest();

//request.open('getClients', 'http://ec2-3-133-152-250.us-east-2.compute.amazonaws.com:81/getClients', true);

request.open('GET', (url + "getClients"), true);
request.onload = function () {

    // Begin accessing JSON data here
    var data = JSON.parse(this.response);
    clientIDs = [];
    if (request.status >= 200 && request.status < 400) {
        data.forEach(client => {
            console.log(client.id, client.name, client.balance);
            clientIDs.push(client.id);
            console.log(clientIDs);

            var tablecontent = document.getElementById('clientList');
            tablecontent.innerHTML += (
                "<tr> <td>" + client.id + "</td><td>" + client.name + "</td><td>" + client.balance + "</td></tr>"
            );

        });
    } else {
        const errorMessage = document.createElement('marquee');
        errorMessage.textContent = `Gah, it's not working!`;
        app.appendChild(errorMessage);
    }
};
request.send();

/**********************************************************************************************************************************************/

/******* REFRESH ******/

function handleRefresh(){
    console.log("Se ha hecho Click en el botón REFRESH");
    event.preventDefault();
    // $("#clientList").load(" #clientList"); console.log("clientList");
    
    var request = new XMLHttpRequest();
    var url = ips[Math.floor(Math.random() * 3)];
    //request.open('getClients', 'http://ec2-3-133-152-250.us-east-2.compute.amazonaws.com:81/getClients', true);

    clientIDs = [];

    request.open('GET', (url + "getClients"), true);
    request.onload = function () {

        // Begin accessing JSON data here
        var data = JSON.parse(this.response);
        if (request.status >= 200 && request.status < 400) {
            var tablecontent = document.getElementById('clientList');
            tablecontent.innerHTML="";
            data.forEach(client => {
                console.log(client.id, client.name, client.balance);
                clientIDs.push(client.id);
                console.log(clientIDs);
                tablecontent.innerHTML += (
                    "<tr> <td>" + client.id + "</td><td>" + client.name + "</td><td>" + client.balance + "</td></tr>"
                );
            });
        } else {
            const errorMessage = document.createElement('marquee');
            errorMessage.textContent = `Gah, it's not working!`;
            app.appendChild(errorMessage);
        }
    };
    request.send();
}

/**********************************************************************************************************************************************/

/******* CREATE ******/
function handleCreate(){
    event.preventDefault();
    console.log("Se ha hecho Click en el botón CREATE");

    var request = new XMLHttpRequest();
    var url = ips[Math.floor(Math.random() * 3)];//'http://10.0.0.6/';

    var input_create = document.getElementById('in_create').value;
    console.log(input_create);
    var data = "{\n  name: \" ".concat(input_create,"\" \n}");
    console.log(data);
    // request.open('GET', (url + "getClients"), true);

    request.addEventListener("readystatechange", function () {
        // COMENTAR PARA VER TRAZAS API
      if (this.readyState === 4) {
        console.log("AJAX_response" + this.responseText);
        document.getElementById('in_create').value = "";
        setTimeout(function(){
           //handleRefresh();
           $( "#miLista" ).load(window.location.href + " #miLista" );
           location.reload();

        }, 9000);
        //handleRefresh();

      }
    });
    console.log(url + "create");
    request.open("POST", (url + "create"),true);
    request.send(data);
   
};

/**********************************************************************************************************************************************/

/******* UPDATE ******/

function handleUpdate(){
    console.log("Se ha hecho Click en el botón UPDATE");
    event.preventDefault();

    var url = ips[Math.floor(Math.random() * 3)];//'http://10.0.0.6/';
    var request = new XMLHttpRequest();

    var input_update = document.getElementById('in_update').value;
    //console.log(input_update);

    var data = "{\n  id: " + input_update.split(",")[0] + "," + "\n ";
    data += "name: \"" + input_update.split(",")[1] +"\" ," + "\n ";
    data += "balance: " + input_update.split(",")[2] + "\n }";

    console.log(data);

    request.addEventListener("readystatechange", function () {
        if (this.readyState === 4) {
            console.log("AJAX_response " + this.responseText);
            document.getElementById('in_update').value = "";
            // COMENTAR PARA VER TRAZAS DE LA API
            setTimeout(function(){
               //handleRefresh();
               $( "#miLista" ).load(window.location.href + " #miLista" );
               location.reload();
            }, 9000); //handleRefresh(); 
        }
    });
    console.log(url + "update");
    request.open("POST", (url + "update"),true);
    request.send(data);

};

/**********************************************************************************************************************************************/

/******* DELETE ******/
function handleDelete(){
    console.log("Se ha hecho Click en el botón DELETE");
    event.preventDefault();

    var url = ips[Math.floor(Math.random() * 3)];//'http://10.0.0.6/';
    var request = new XMLHttpRequest();

    var input_delete = document.getElementById('in_delete').value;
    //console.log(in_delete);

    var data = "{\n  id: " + input_delete.split(",")[0] + "," + "\n ";
    data += "name: \"" + input_delete.split(",")[1] + "\" \n }";

    console.log(data);

    request.addEventListener("readystatechange", function () {
        if (this.readyState === 4) {
            console.log("AJAX_response " + this.responseText);
            document.getElementById('in_delete').value = "";
            setTimeout(function(){
               //handleRefresh();
               $( "#miLista" ).load(window.location.href + " #miLista" );
               location.reload();
            }, 9000); //handleRefresh(); 
        }
    });
    console.log(url + "delete");

    request.open("POST", (url + "delete"),true);
    request.send(data);
};


/**********************************************************************************************************************************************/

/******* READ ******/
function handleRead(){
    event.preventDefault();
    console.log("Se ha hecho Click en el botón READ CLIENT INFO");

    var request = new XMLHttpRequest();
    var url = ips[Math.floor(Math.random() * 3)];//'http://10.0.0.6/';

    var input_read = document.getElementById('in_read').value;
    console.log(input_read);

    //var data = "{\n  name: \" ".concat(input_create,"\" \n}");
    // SI EN EL INPUT HAY UN ID 
    console.log("INCLUEDES TEST");
    console.log(clientIDs);
    console.log(clientIDs.includes(parseInt(input_read,10)));
    if(clientIDs.includes(parseInt(input_read,10))){
        // "http://10.0.0.6/readById?id=
        url += "readById?id=" + input_read;
        console.log("URL para ID: " + url);
    } else  {
        url += "readByName?name=" + input_read;
    }
    
    //console.log(data);
    // request.open('GET', (url + "getClients"), true);

    request.addEventListener("readystatechange", function () {
      if (this.readyState === 4) {
        console.log("AJAX_response" + this.responseText);
        document.getElementById('in_read_resp').innerHTML = this.responseText;
        var input_read = document.getElementById('in_read').value="";

      }
    });
    console.log("URL pasada a peticion: " + url);
    request.open("GET",url,true);
    request.send();
   
};





