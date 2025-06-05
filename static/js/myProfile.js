document.addEventListener("DOMContentLoaded", async () => {
    const rightPanel = document.querySelector(".right-panel");
    const rightPanelTwo = document.querySelector(".right-paneltwo");
    const editButton = document.getElementById("edittap");
    const cancelButton = document.getElementById("cancelbutton");
    const saveButton = document.getElementById("savebutton");
    const phoneNumber = localStorage.getItem("number") || "Not Provided";

    // Display phone number (read-only)
    document.getElementById("numberPrint").innerText = phoneNumber;
    document.getElementById("numberPrint2").innerText = phoneNumber;

    // Fetch and display profile
    async function displayProfile() {
        try {
            const response = await fetch("/api/profile/get", {
                method: "GET",
                headers: { "Content-Type": "application/json" },
            });
            const data = await response.json();
            if (data.status === "success") {
                const profile = data.profile || {};
                document.getElementById("nameUser Print").innerText = profile.userName || "User  Name";
                document.getElementById("User NameleftPanel").innerText = profile.userName || "User  Name";
                document.getElementById("datePrint").innerText = profile.dateOfBirth || "Not Set";
                document.getElementById("genderPrint").innerText = profile.gender || "Not Set";
                document.getElementById("emailPrint").innerText = profile.email || "Not Set";
            } else {
                showAlert("error", "Error", data.message || "Failed to load profile");
            }
        } catch (error) {
            showAlert("error", "Error", `Failed to load profile: ${error.message}`);
        }
    }

    // Initial display
    await displayProfile();

    // Edit button click
    editButton.addEventListener("click", () => {
        rightPanel.style.display = "none";
        rightPanelTwo.style.display = "block";

        // Populate edit form
        const profile = {
            userName: document.getElementById("nameUser Print").innerText,
            dateOfBirth: document.getElementById("datePrint").innerText,
            gender: document.getElementById("genderPrint").innerText,
            email: document.getElementById("emailPrint").innerText,
        };
        document.getElementById("userName").value = profile.userName !== "User  Name" ? profile.userName : "";
        document.getElementById("date").value = profile.dateOfBirth !== "Not Set" ? profile.dateOfBirth : "";
        document.getElementById("gender").value = profile.gender !== "Not Set" ? profile.gender : "";
        document.getElementById("email").value = profile.email !== "Not Set" ? profile.email : "";
    });

    // Cancel button click
    cancelButton.addEventListener("click", () => {
        rightPanel.style.display = "block";
        rightPanelTwo.style.display = "none";
    });

    // Save button click
    saveButton.addEventListener("click", async () => {
        const userName = document.getElementById("userName").value.trim();
        const dateOfBirth = document.getElementById("date").value;
        const gender = document.getElementById("gender").value;
        const email = document.getElementById("email").value.trim();

        if (!userName || !email) {
            showAlert("error", "Invalid Input", "Name and email are required.");
            return;
        }

        try {
            const response = await fetch("/api/profile/update", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ userName, dateOfBirth, gender, email, phoneNumber }),
            });
            const data = await response.json();
            if (data.status === "success") {
                showAlert("success", "Profile Updated", "Your profile has been saved successfully.");
                rightPanel.style.display = "block";
                rightPanelTwo.style.display = "none";
                await displayProfile();
            } else {
                showAlert("error", "Error", data.message || "Failed to update profile");
            }
        } catch (error) {
            showAlert("error", "Error", `Failed to update profile: ${error.message}`);
        }
    });

    // Function to show alerts using SweetAlert
    function showAlert(icon, title, text) {
        Swal.fire({
            icon: icon,
            title: title,
            text: text,
        });
    }
});
