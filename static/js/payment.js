const bookingDetails = JSON.parse(localStorage.getItem("booking_details")) || /*[[${bookingDetails}]]*/ {};

// Populate DOM elements with booking details or fallbacks
document.getElementById("city_start").innerText = localStorage.getItem("from") || /*[[${bookingDetails.from}]]*/ "From";
document.getElementById("city_end").innerText = localStorage.getItem("to") || /*[[${bookingDetails.to}]]*/ "To";
document.getElementById("city_end1").innerText = localStorage.getItem("to") || /*[[${bookingDetails.to}]]*/ "To";
document.getElementById("departure").innerText = localStorage.getItem("date") || /*[[${bookingDetails.date}]]*/ "Date";
document.getElementById("seat_no").innerText = localStorage.getItem("seats") || /*[[${bookingDetails.seats}]]*/ "Seat";
document.getElementById("company_name").innerText = localStorage.getItem("company_name") || /*[[${bookingDetails.company_name}]]*/ "Bus Co.";
document.getElementById("bus-name").innerText = localStorage.getItem("bus_name") || /*[[${bookingDetails.bus_name}]]*/ "Bus Name";
document.getElementById("city_name").innerText = localStorage.getItem("boarding_point") || /*[[${bookingDetails.boarding_point}]]*/ "Boarding";
document.getElementById("name_age").innerText = localStorage.getItem("name_age") || /*[[${bookingDetails.name_age}]]*/ "Passenger";
document.getElementById("total_price").innerText = "₹" + (localStorage.getItem("total_price") || /*[[${bookingDetails.total_price}]]*/ "0");
document.getElementById("total_price1").innerText = "₹" + (localStorage.getItem("total_price") || /*[[${bookingDetails.total_price}]]*/ "0");

// Promo code application
document.getElementById("promoButton").addEventListener("click", () => {
  const code = document.getElementById("offerCode").value;
  if (code === "NexBus30") {
    Swal.fire({
      icon: "success",
      title: "Success",
      text: "30% discount applied!",
    });
    let price = parseFloat(localStorage.getItem("total_price") || /*[[${bookingDetails.total_price}]]*/ 0);
    let discount = price * 0.3;
    document.getElementById("discount").innerText = "₹" + discount.toFixed(2);
    document.getElementById("total_price").innerText = "₹" + (price - discount).toFixed(2);
    localStorage.setItem("pay", (price - discount).toFixed(2));
    document.getElementById("offerCode").value = "";
  } else {
    Swal.fire({
      icon: "error",
      title: "Invalid Offer Code",
      text: "Please enter a valid offer code",
    });
    document.getElementById("offerCode").value = "";
  }
});

// Razorpay payment initiation
document.getElementById("paynow").addEventListener("click", async () => {
  let totalPrice = parseFloat(localStorage.getItem("pay") || /*[[${bookingDetails.total_price}]]*/ 0) * 100; // Convert to paise
  if (isNaN(totalPrice) || totalPrice <= 0) {
    Swal.fire({
      icon: "error",
      title: "Invalid Amount",
      text: "Total price is invalid or zero.",
    });
    return;
  }

  const keyId = document.getElementById("rzp_key_id").value;
  const email = /*[[${bookingDetails.email}]]*/ "" || localStorage.getItem("email") || "";
  const name = /*[[${bookingDetails.name_age}]]*/ "" || localStorage.getItem("name_age") || "";
  const contact = localStorage.getItem("contact") || "";

  try {
    // Fetch order ID from server
    const orderResponse = await fetch("/api/payment/createOrder", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        amount: totalPrice,
        currency: "INR",
        receipt: `ticket-${bookingDetails.ticketNo || "unknown"}`,
      }),
    });
    const orderData = await orderResponse.json();
    if (!orderData.orderId) {
      throw new Error("Failed to create order ID");
    }

    // Razorpay checkout options
    const options = {
      key: keyId,
      amount: totalPrice,
      currency: "INR",
      name: "Nex-Bus",
      description: `Ticket Payment for ${bookingDetails.ticketNo || "N/A"}`,
      order_id: orderData.orderId,
      image: "/image/logo.png",
      handler: async (response) => {
        try {
          // Verify payment on server and send email
          const verifyResponse = await fetch("/api/payment/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_order_id: response.razorpay_order_id,
              razorpay_signature: response.razorpay_signature,
              bookingDetails: bookingDetails,
            }),
          });
          const verifyData = await verifyResponse.json();
          if (verifyData.status === "success") {
            Swal.fire({
              icon: "success",
              title: "Booking Confirmed",
              text: `Ticket No: ${bookingDetails.ticketNo || "N/A"}. A confirmation email has been sent to ${email || "your email"}.`,
            });
            setTimeout(() => {
              window.location.href = /*[[@{/html/homePage}]]*/ "/html/homePage";
            }, 2000);
          } else {
            Swal.fire({
              icon: "error",
              title: "Payment Verification Failed",
              text: verifyData.message,
            });
          }
        } catch (error) {
          Swal.fire({
            icon: "error",
            title: "Error",
            text: "Payment processing error: " + error.message,
          });
        }
      },
      prefill: {
        name: name,
        email: email,
        contact: contact,
      },
      theme: {
        color: "#d32f2f",
      },
    };

    const rzp = new Razorpay(options);
    rzp.on("payment.failed", (response) => {
      Swal.fire({
        icon: "error",
        title: "Payment Failed",
        text: `Error: ${response.error.description}`,
      });
    });
    rzp.open();
  } catch (error) {
    Swal.fire({
      icon: "error",
      title: "Error",
      text: `Failed to initiate payment: ${error.message}`,
    });
  }
});