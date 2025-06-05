document.addEventListener("DOMContentLoaded", () => {
  const booking_details = JSON.parse(localStorage.getItem("booking_details")) || {};
  const input_obj = JSON.parse(localStorage.getItem("user_inputs")) || {};
  const seatNo = parseInt(localStorage.getItem("selected_seat_id"));
  const busNo = parseInt(localStorage.getItem("selected_busID"));

  document.getElementById("tot_price").innerHTML = booking_details.tot_price || "1500.00";

  const today = new Date();
  const date_time = `${today.getHours()} : ${today.getMinutes()}  ${today.getDate()} - ${today.getMonth() + 1} - ${today.getFullYear()}`;

  document.getElementById("submitBtn").addEventListener("click", async function () {
    const name = document.getElementById("inputName").value.trim();
    const genderName = document.querySelector('input[name="gender"]:checked')?.value;
    const age = parseInt(document.getElementById("input").value.trim());
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phoneNumber").value.trim();

    if (!name) return alert("Please enter a name.");
    if (!genderName) return alert("Please select a gender.");
    if (!age || isNaN(age) || age <= 0) return alert("Please enter a valid age.");
    if (!/^\S+@\S+\.\S+$/.test(email)) return alert("Please enter a valid email address.");
    if (phone.length < 10) return alert("Please enter a valid phone number.");

    if (isNaN(seatNo) || isNaN(busNo)) {
      alert("Invalid seat or bus selection.");
      return;
    }

    const ticketNo = 200 + seatNo + busNo;
    localStorage.setItem("ticketNo", ticketNo);

    const ticketDetails = {
      name,
      gender: genderName,
      age,
      email,
      phone,
      booked_bus: booking_details.booked_bus,
      booked_seatID: booking_details.booked_seatID,
      booked_price: booking_details.tot_price,
      cur_Dt_time: date_time,
      user_points_input: input_obj,
      ticketNo: ticketNo
    };

    try {
      const res = await fetch("/api/tickets", {
        method: "POST",
        body: JSON.stringify(ticketDetails),
        headers: {
          "Content-Type": "application/json"
        }
      });

      if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);

      localStorage.setItem("booking_details", JSON.stringify(ticketDetails));
      storeUserData(name, age);

      // Redirect after success
      window.location.href = "/payment";
    } catch (err) {
      console.error("Error posting data:", err);
      alert("Failed to submit booking. Please try again.");
    }
  });

  function storeUserData(name, age) {
    const userData = { name, age };
    localStorage.setItem("user_data", JSON.stringify(userData));
  }
});
