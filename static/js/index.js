import navbar from "./navbar.js";
let header = document.getElementById("header");
header.innerHTML = navbar();

// Handle form submission
document.querySelector("form").addEventListener("submit", (event) => {
  event.preventDefault(); // Prevent the default form submission behavior

  // Get input values
  let from = document.querySelector("#input-label-from").value;
  let to = document.querySelector("#input-label-to").value;
  let date = document.querySelector("#input-label-onward-date").value; // Changed variable name to 'date' (lowercase)

  // Validate input fields
  if (from === "" || to === "" || date === "") {
    alert("Please fill in all the details first!");
  } else {
    let obj = {
      pickPoint: from,
      dropPoint: to,
      date: date,
    };

    // Send data to the backend
    fetch('/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(obj)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return response.json();
    })
    .then(data => {
      // Handle the response data if needed
      console.log('Success:', data);
      // Redirect to the availability and booking page
      window.open("../../Bus checking and booking/Bus ticket booking Module/availabilty_nd_booking.html", "_self");
    })
    .catch(error => console.error('Error:', error));
  }
});

// Manage booking dropdown
let flg = false;
document.getElementById("dropup-menu-booking").addEventListener("click", function() {
  flg = !flg; // Toggle the flag
  const dropdownMenu = document.querySelector(".dropdown-menu-booking");
  dropdownMenu.style.display = flg ? "flex" : "none"; // Show or hide the dropdown
  dropdownMenu.style.backgroundColor = 'white';
  dropdownMenu.style.position = "absolute";
});

// Manage profile dropdown
let flg2 = false;
document.getElementById("dropup-menu").addEventListener("click", function() {
  flg2 = !flg2; // Toggle the flag
  const dropdownMenu = document.querySelector(".dropdown-menu");
  dropdownMenu.style.display = flg2 ? "flex" : "none"; // Show or hide the dropdown
  dropdownMenu.style.backgroundColor = 'white';
  dropdownMenu.style.position = "absolute";
});
