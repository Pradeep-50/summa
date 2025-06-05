document.addEventListener("DOMContentLoaded", async () => {
    const tripsContainer = document.querySelector(".trips_det");
    const userNameElement = document.getElementById("userName");

    // Fetch and display user profile
    try {
        const profileResponse = await fetch("/api/profile/get", {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });
        const profileData = await profileResponse.json();
        if (profileData.status === "success") {
            userNameElement.innerText = profileData.profile.userName || "User ";
        }
    } catch (error) {
        console.error("Error fetching user name:", error);
    }

    // Function to display trips
    async function displayTrips() {
        try {
            const response = await fetch("/api/trips", {
                method: "GET",
                headers: { "Content-Type": "application/json" }
            });
            const data = await response.json();

            if (data.status === "success" && data.trips.length > 0) {
                tripsContainer.innerHTML = ""; // Clear previous trips
                data.trips.forEach(trip => {
                    const ticketCard = createTicketCard(trip);
                    tripsContainer.appendChild(ticketCard);
                });
            } else {
                tripsContainer.innerHTML = '<p class="center">No trips found.</p>';
            }
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: `Failed to load trips: ${error.message}`
            });
        }
    }

    // Function to create a ticket card
    function createTicketCard(trip) {
        const ticketCard = document.createElement("div");
        ticketCard.className = "ticketCard"; // Use class instead of id for multiple elements
        ticketCard.innerHTML = `
            <div class="row">
                <p class="bold">Ticket ID: ${trip.ticketId}</p>
                <p>Status: ${trip.status}</p>
            </div>
            <div class="row">
                <p>From: ${trip.source}</p>
                <p>To: ${trip.destination}</p>
            </div>
            <p>Date: ${trip.travelDate}</p>
            <p>Passenger: ${trip.passengerName}</p>
            ${trip.status === "Confirmed" ? '<button class="cancel-button">Cancel</button>' : ''}
        `;

        // Add event listener for cancel button if trip is confirmed
        if (trip.status === "Confirmed") {
            ticketCard.querySelector(".cancel-button").addEventListener("click", () => handleCancelTrip(trip.ticketId));
        }

        return ticketCard;
    }

    // Function to handle trip cancellation
    async function handleCancelTrip(ticketId) {
        try {
            const cancelResponse = await fetch("/api/ticket/cancel", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ticketId })
            });
            const cancelData = await cancelResponse.json();
            if (cancelData.status === "success") {
                Swal.fire({
                    icon: "success",
                    title: "Trip Cancelled",
                    text: "Your trip has been cancelled successfully."
                });
                await displayTrips(); // Refresh the trips list
            } else {
                Swal.fire({
                    icon: "error",
                    title: "Cancellation Failed",
                    text: cancelData.message || "Unable to cancel trip."
                });
            }
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: `Cancellation error: ${error.message}`
            });
        }
    }

    // Initial call to display trips
    await displayTrips();
});
