// Function to request OTP from the server
async function requestOTP() {
    try {
        const response = await fetch('/api/otp/request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ /* Include any necessary data, e.g., user info */ })
        });
        const data = await response.json();
        if (data.status === 'success') {
            alert("Your OTP is " + data.otp); // Display OTP received from server
            console.log(data.otp);
            localStorage.setItem("OTP", JSON.stringify(data.otp)); // Store OTP in localStorage
        } else {
            alert("Failed to send OTP: " + data.message);
        }
    } catch (error) {
        console.error("Error requesting OTP:", error);
    }
}

// Function to verify OTP with the server
async function verifyOTP(inputOTP) {
    try {
        const response = await fetch('/api/otp/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ otp: inputOTP })
        });
        const data = await response.json();
        if (data.status === 'success') {
            alert("Login Successfully");
            document.getElementById("invalidOTP").style.display = "none";
            location.href = "../Components/Onboarding/homePage.html"; // Redirect on success
        } else {
            displayInvalidOTPMessage("Invalid OTP!");
        }
    } catch (error) {
        console.error("Error verifying OTP:", error);
        displayInvalidOTPMessage("An error occurred during verification.");
    }
}

// Function to display invalid OTP message
function displayInvalidOTPMessage(message) {
    const invalidOTP = document.getElementById("invalidOTP");
    invalidOTP.innerText = ""; // Clear previous messages
    const div = document.createElement("div");
    const invalid = document.createElement("p");
    invalid.textContent = message;
    invalid.style.color = "red";
    div.append(invalid);
    invalidOTP.append(div);
}

// Event listener for requesting OTP on signup button click
document.getElementById("ButtonSigup").addEventListener("click", function() {
    setTimeout(requestOTP, 2000); // Call requestOTP after 2 seconds
});

// Event listener for verifying OTP on button click
document.getElementById("ButtonOTPvarification").addEventListener("click", function() {
    const GetInput = document.getElementById("inputOTP").value;
    const GetOTP = JSON.parse(localStorage.getItem('OTP'));
    console.log(GetOTP);
    console.log(GetInput);

    if (GetOTP === GetInput) {
        verifyOTP(GetInput); // Call verifyOTP if local OTP matches input
    } else {
        displayInvalidOTPMessage("Invalid OTP!");
    }
});
