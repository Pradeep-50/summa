import getData from "./getBus_data.js";
import { upperDeck, lowerDeck } from "./getSeats.js";

async function checking_point() {
  let input_obj = JSON.parse(localStorage.getItem("user_inputs"));
  let selected_busID = localStorage.getItem("selected_busID");
  let selected_seat_id = localStorage.getItem("selected_seat_id");
  selected_seat_id = +selected_seat_id;

  let buses_data = await getData();
  let seats_data = selected_seat_id <= 48 ? await upperDeck() : await lowerDeck();

  let selected_bus = buses_data.find((el) => selected_busID == el.id);
  let selected_seat = seats_data.find((el) => selected_seat_id == el.id);

  let boardPoint_div = document.getElementById("boardPoint_div");
  let dropPoint_div = document.getElementById("dropPoint_div");
  let boarding_points = document.querySelector(".boarding-points");
  let continue_btn = document.getElementById("continue_btn");
  
  let div1 = document.createElement("div");
  div1.innerHTML = `<input type="checkbox"><p class="bold" id="pick_time">${selected_bus.time_in} </p><p class="bold" id="boarding-point"> ${input_obj.pickPoint}</p>`;
  
  let div2 = document.createElement("div");
  div2.innerHTML = `<input type="checkbox"><p class="bold" id="drop_time">${selected_bus.time_out}</p><p class="bold" id="dropping-point"> ${input_obj.dropPoint}</p>`;
  
  boarding_points.innerHTML = "";
  boarding_points.append(div1);
  
  boardPoint_div.addEventListener("click", () => {
    boarding_points.innerHTML = "";
    boarding_points.append(div1);
  });
  
  dropPoint_div.addEventListener("click", () => {
    boarding_points.innerHTML = "";
    boarding_points.append(div2);
  });
  
  continue_btn.addEventListener("click", () => {
    document.querySelector("#seats_contents_right-2").style.display = "none";
    let confirm_booking = document.querySelector("#confirm_booiking_box");
    confirm_booking.style.display = "flex";
    
    let seatNo = localStorage.getItem("selected_seat_id");
    let sum_price = selected_seat.extra_price + selected_bus.price;
    
    confirm_booking.innerHTML = `
      <p>Boarding Point - <span id="b_point">${input_obj.pickPoint}</span></p><span id="b_time">${selected_bus.time_in}</span>
      <p>Dropping Point - <span id="b_point">${input_obj.dropPoint}</span></p><span id="d_time">${selected_bus.time_out}</span>
      <p>Seat No. : ${seatNo}</p>
      <p>Fare : <span id="fare" class="bold">${selected_bus.price}+Extra Seat Price (${selected_seat.extra_price}) = ${sum_price}</span></p>
      <button class="btn" id="proceed_btn">Proceed to Book</button>
    `;
    
    let proceed_btn = document.getElementById("proceed_btn");
    proceed_btn.addEventListener("click", async () => {
      seatNo = parseInt(seatNo);
      if (selected_bus.booked_seats.includes(seatNo)) {
        alert("Selected Seat Already Booked!!");
      } else {
        selected_bus.booked_seats.push(seatNo);
        
        let booking_details = {
          tot_price: sum_price,
          booked_bus: selected_bus,
          booked_seatID: selected_seat_id
        };
        
        localStorage.setItem("booking_details", JSON.stringify(booking_details));
        await patch_ele(selected_bus, selected_busID);
        window.open("../Pages/passenger.html", "_self");
      }
    });
  });
}

async function patch_ele(updObj, selected_busID) {
  try {
    let res = await fetch(`/api/buses/${selected_busID}`, { // Adjust the endpoint according to your Spring MVC controller
      method: "PATCH",
      body: JSON.stringify(updObj),
      headers: {
        "Content-Type": "application/json",
      },
    });
    if (!res.ok) throw new Error('Network response was not ok');
  } catch (err) {
    console.error("Error updating bus data:", err);
  }
}

export default checking_point;