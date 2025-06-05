let ticket_container = document.getElementById("ticket_container");

document.querySelector(".search_btn").addEventListener("click", () => {
  let ticket_no = document.querySelector(".ticket_no").value;
  get_ticket(ticket_no);
});

async function get_ticket(ticket_no) {
  try {
    let url = `/api/tickets/${ticket_no}`;
    let res = await fetch(url);
    let ticket = await res.json();
    sidplayTicket(ticket);
  } catch (err) {
    console.log("Error occurred while fetching ticket:", err);
  }
}

function sidplayTicket(ticket) {
  ticket_container.innerHTML = "";

  if (!ticket) {
    let msg = document.createElement("p");
    msg.innerText = "No Ticket found!";
    msg.classList.add("center");
    msg.style.color = "red";
    ticket_container.append(msg);
    return;
  }

  let bus = ticket.booked_bus[0];

  ticket_container.innerHTML = `
    <div id="ticketCard">
      <h3 class="center"><span>${ticket.user_points_input.pickPoint}</span> to <span>${ticket.user_points_input.dropPoint}</span></h3>
      <p class="center">Bus Name : <span>${bus.bus_name}</span></p>
      <div id="tickNo_price" class="row">
        <p class="bold">Ticket No : <span>${ticket.ticketN0}</span></p>
        <p class="bold">Total Price : <span class="bold">Rs. ${ticket.booked_price}</span></p>
      </div>
      <div id="times" class="row">
        <p>Boarding Time : <span>${bus.time_in}</span></p>
        <p>Dropping Time : <span>${bus.time_out}</span></p>
      </div>
      <div id="bus_det" class="row">
        <p>Bus Service : <span>${bus.company}</span></p>
        <p>Seat No : <span>${ticket.booked_seatID}</span></p>
      </div>
      <div id="userDetail">
        <div class="row">
          <p>Traveller Name : <span>${ticket.name}</span></p>
          <p>Phone No : <span>${ticket.phone}</span></p>
        </div>
      </div>
      <p class="bold center">Booked at : <span>${ticket.cur_Dt_time}</span></p>
      <button id="ticketCancel_btn" class="ticketCancel_btn">Cancel Ticket</button>
    </div>
  `;

  document.querySelector(".ticketCancel_btn").addEventListener("click", async (event) => {
    event.preventDefault();
    await deleteTicket(ticket.id);
    await initBus(bus.id, ticket.booked_seatID);
    alert(`${ticket.ticketN0} Ticket has been deleted successfully`);
    ticket_container.innerHTML = "";
  });
}

async function deleteTicket(ticketID) {
  try {
    await fetch(`/api/tickets/${ticketID}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    });
  } catch (err) {
    console.error("Error while deleting ticket:", err);
  }
}

async function initBus(bus_id, seatID) {
  let busData = await getBus(bus_id);
  deleteSeat(busData, seatID);
}

function deleteSeat(busData, seatID) {
  let seat_ind = busData.booked_seats.indexOf(seatID);
  if (seat_ind !== -1) {
    busData.booked_seats.splice(seat_ind, 1);
    patch_busSeat(busData, busData.id);
  }
}

async function getBus(bus_id) {
  try {
    let res = await fetch(`/api/bus/${bus_id}`);
    return await res.json();
  } catch (err) {
    console.log("Error getting bus data:", err);
  }
}

async function patch_busSeat(updObj, busID) {
  try {
    await fetch(`/api/bus/${busID}`, {
      method: "PATCH",
      body: JSON.stringify(updObj),
      headers: {
        "Content-Type": "application/json",
      },
    });
  } catch (err) {
    console.log("Error patching bus seats:", err);
  }
}
