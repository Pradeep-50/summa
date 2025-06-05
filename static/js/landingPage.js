let from, to, onwardDate, returnDate;

function searchBtn() {
    // Retrieve values from input fields
    from = document.querySelector("#input-label-from").value;
    to = document.querySelector("#input-label-to").value;
    onwardDate = document.querySelector("#input-label-onward-date").value;

    // Validate input fields
    if (from === "" || to === "" || onwardDate === "") {
        alert("Please fill in all the details first!");
        return false; // Indicate that the search was not successful
    }

    // Store values in localStorage
    localStorage.setItem("from", from);
    localStorage.setItem("to", to);
    localStorage.setItem("onwardDate", onwardDate);

    return true; // Indicate that the search was successful
}

// Example of how to use searchBtn in a form submission
document.querySelector("form").addEventListener("submit", (event) => {
    event.preventDefault(); // Prevent the default form submission behavior

    // Call searchBtn and check if it was successful
    if (searchBtn()) {
        // If successful, proceed with the fetch request or any other logic
        fetch('/search', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                pickPoint: from,
                dropPoint: to,
                date: onwardDate
            })
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
            window.open("../customer/availabilty_nd_booking.html", "_self");
        })
        .catch(error => console.error('Error:', error));
    }
});
