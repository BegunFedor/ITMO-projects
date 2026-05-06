let getTime = document.getElementById("time");

function checkTime(i) {
    if (i < 10) {
        i = "0" + i;
    }
    return i;
}

function startTime() {
    let today = new Date();
    let h = today.getHours();
    let m = today.getMinutes();
    let s = today.getSeconds();
    m = checkTime(m);
    s = checkTime(s);
    getTime.innerHTML = h + ":" + m + ":" + s;
    t = setTimeout(function() {
        startTime();
    }, 5000);
}

startTime();

switch (new Date().getDay()) {
    case 0:
        document.querySelector(".sunday").classList.add("glow");
        break;
    case 1:
        document.querySelector(".monday").classList.add("glow");
        break;
    case 2:
        document.querySelector(".tuesday").classList.add("glow");
        break;
    case 3:
        document.querySelector(".wednesday").classList.add("glow");
        break;
    case 4:
        document.querySelector(".thursday").classList.add("glow");
        break;
    case 5:
        document.querySelector(".friday").classList.add("glow");
        break;
    case 6:
        document.querySelector(".saturday").classList.add("glow");
}

let month = document.querySelector(".month");
let day = document.querySelector(".day");
let year = document.querySelector(".year");
let date = new Date();

let months = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
];
month.innerHTML = months[date.getMonth()];
day.innerHTML = date.getDate();
year.innerHTML = date.getFullYear();
