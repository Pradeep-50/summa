document.addEventListener('DOMContentLoaded', function () {
    const BACKEND_URL = 'http://localhost:9090';
    const token = window.authToken || sessionStorage.getItem('authToken') || '';

    if (window.authToken && !sessionStorage.getItem('authToken')) {
        sessionStorage.setItem('authToken', window.authToken);
    }

    // Sidebar toggle
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }
    if (mobileSidebarToggle) {
        mobileSidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // Navigation
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar-menu a').forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });

    // Fetch with auth
    async function fetchWithAuth(url, options = {}) {
        const headers = new Headers(options.headers || {});
        if (token) {
            headers.set('Authorization', `Bearer ${token}`);
        }
        headers.set('Content-Type', 'application/json');
        const response = await fetch(url, { ...options, headers });
        if (response.status === 401) {
            sessionStorage.removeItem('authToken');
            window.location.href = '/login';
        }
        return response;
    }

    // Populate select options
    async function populateSelect(selectId, endpoint, valueKey, textKey) {
        try {
            const response = await fetchWithAuth(`${BACKEND_URL}/${endpoint}`);
            const data = await response.json();
            const select = document.getElementById(selectId);
            select.innerHTML = '<option value="">Select...</option>';
            data.forEach(item => {
                const option = document.createElement('option');
                option.value = item[valueKey];
                option.textContent = item[textKey];
                select.appendChild(option);
            });
        } catch (error) {
            console.error(`Error populating ${selectId}:`, error);
        }
    }

    // Get passenger name
    async function getPassengerName(userId) {
        try {
            const response = await fetchWithAuth(`${BACKEND_URL}/api/passengers/${userId}`);
            const passenger = await response.json();
            return `${passenger.firstName} ${passenger.lastName}`;
        } catch (error) {
            console.error('Error fetching passenger name:', error);
            return `User #${userId}`;
        }
    }

    // Routes
    if (currentPath === '/api/routes') {
        let allRoutes = [];

        fetchWithAuth(`${BACKEND_URL}/api/routes`)
            .then(response => response.json())
            .then(routes => {
                allRoutes = routes;
                const tbody = document.querySelector('#routes-table tbody');
                tbody.innerHTML = '';
                if (routes.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="5">No routes found.</td></tr>';
                } else {
                    routes.forEach(route => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${route.routeId}</td>
                            <td>${route.startLocation}</td>
                            <td>${route.endLocation}</td>
                            <td>${route.distance} km</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary edit-route" data-id="${route.routeId}">Edit</button>
                                <button class="btn btn-sm btn-outline-danger delete-route" data-id="${route.routeId}">Delete</button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                    // Edit route
                    document.querySelectorAll('.edit-route').forEach(button => {
                        button.addEventListener('click', function () {
                            const routeId = this.dataset.id;
                            const route = allRoutes.find(r => r.routeId == routeId);
                            document.getElementById('routeName').value = route.routeName;
                            document.getElementById('sourceCity').value = route.startLocation;
                            document.getElementById('destinationCity').value = route.endLocation;
                            document.getElementById('distance').value = route.distance;

                            const form = document.getElementById('add-route-form');
                            form.action = `/api/routes/${routeId}/update`;
                            form.onsubmit = async (e) => {
                                e.preventDefault();
                                const routeData = {
                                    routeName: document.getElementById('routeName').value,
                                    startLocation: document.getElementById('sourceCity').value,
                                    endLocation: document.getElementById('destinationCity').value,
                                    distance: parseFloat(document.getElementById('distance').value)
                                };
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/api/routes/${routeId}`, {
                                        method: 'PUT',
                                        body: JSON.stringify(routeData)
                                    });
                                    alert('Route updated successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error updating route:', error);
                                    alert('Failed to update route');
                                }
                            };
                        });
                    });

                    // Delete route
                    document.querySelectorAll('.delete-route').forEach(button => {
                        button.addEventListener('click', async function () {
                            const routeId = this.dataset.id;
                            if (confirm('Are you sure you want to delete this route?')) {
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/api/routes/${routeId}`, { method: 'DELETE' });
                                    alert('Route deleted successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error deleting route:', error);
                                    alert('Failed to delete route');
                                }
                            }
                        });
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching routes:', error);
                document.querySelector('#routes-table tbody').innerHTML = '<tr><td colspan="5">Error loading routes.</td></tr>';
            });

        const addRouteForm = document.getElementById('add-route-form');
        if (addRouteForm) {
            addRouteForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const routeData = {
                    routeName: document.getElementById('routeName').value,
                    startLocation: document.getElementById('sourceCity').value,
                    endLocation: document.getElementById('destinationCity').value,
                    distance: parseFloat(document.getElementById('distance').value)
                };
                try {
                    await fetchWithAuth(`${BACKEND_URL}/api/routes`, {
                        method: 'POST',
                        body: JSON.stringify(routeData)
                    });
                    alert('Route created successfully');
                    window.location.reload();
                } catch (error) {
                    console.error('Error creating route:', error);
                    alert('Failed to create route');
                }
            });
        }
    }

    // Buses
    if (currentPath === '/api/buses') {
        let allBuses = [];

        fetchWithAuth(`${BACKEND_URL}/api/buses`)
            .then(response => response.json())
            .then(buses => {
                allBuses = buses;
                const tbody = document.querySelector('#buses-table tbody');
                tbody.innerHTML = '';
                if (buses.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6">No buses found.</td></tr>';
                } else {
                    buses.forEach(bus => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${bus.busId}</td>
                            <td>${bus.busNumber}</td>
                            <td>${bus.operatorId}</td>
                            <td>${bus.routeId || 'N/A'}</td>
                            <td>${bus.totalSeats}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary edit-bus" data-id="${bus.busId}">Edit</button>
                                <button class="btn btn-sm btn-outline-danger delete-bus" data-id="${bus.busId}">Delete</button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                    // Edit bus
                    document.querySelectorAll('.edit-bus').forEach(button => {
                        button.addEventListener('click', function () {
                            const busId = this.dataset.id;
                            const bus = allBuses.find(b => b.busId == busId);
                            document.getElementById('operatorId').value = bus.operatorId;
                            document.getElementById('assignedRoute').value = bus.routeId || '';
                            document.getElementById('busRegistration').value = bus.busNumber;
                            document.getElementById('totalSeats').value = bus.totalSeats;

                            const form = document.getElementById('add-bus-form');
                            form.action = `/api/buses/${busId}/update`;
                            form.onsubmit = async (e) => {
                                e.preventDefault();
                                const busData = {
                                    operatorId: parseInt(document.getElementById('operatorId').value),
                                    routeId: document.getElementById('assignedRoute').value ? parseInt(document.getElementById('assignedRoute').value) : null,
                                    busNumber: document.getElementById('busRegistration').value,
                                    totalSeats: parseInt(document.getElementById('totalSeats').value)
                                };
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/api/buses/${busId}`, {
                                        method: 'PUT',
                                        body: JSON.stringify(busData)
                                    });
                                    alert('Bus updated successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error updating bus:', error);
                                    alert('Failed to update bus');
                                }
                            };
                        });
                    });

                    // Delete bus
                    document.querySelectorAll('.delete-bus').forEach(button => {
                        button.addEventListener('click', async function () {
                            const busId = this.dataset.id;
                            if (confirm('Are you sure you want to delete this bus?')) {
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/api/buses/${busId}`, { method: 'DELETE' });
                                    alert('Bus deleted successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error deleting bus:', error);
                                    alert('Failed to delete bus');
                                }
                            }
                        });
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching buses:', error);
                document.querySelector('#buses-table tbody').innerHTML = '<tr><td colspan="6">Error loading buses.</td></tr>';
            });

        const addBusForm = document.getElementById('add-bus-form');
        if (addBusForm) {
            addBusForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const busData = {
                    operatorId: parseInt(document.getElementById('operatorId').value),
                    routeId: document.getElementById('assignedRoute').value ? parseInt(document.getElementById('assignedRoute').value) : null,
                    busNumber: document.getElementById('busRegistration').value,
                    totalSeats: parseInt(document.getElementById('totalSeats').value)
                };
                try {
                    await fetchWithAuth(`${BACKEND_URL}/api/buses`, {
                        method: 'POST',
                        body: JSON.stringify(busData)
                    });
                    alert('Bus created successfully');
                    window.location.reload();
                } catch (error) {
                    console.error('Error creating bus:', error);
                    alert('Failed to create bus');
                }
            });
        }
    }

    // Passengers
    if (currentPath === '/api/passengers') {
        let allPassengers = [];

        fetchWithAuth(`${BACKEND_URL}/api/passengers`)
            .then(response => response.json())
            .then(passengers => {
                allPassengers = passengers;
                const tbody = document.querySelector('#passengers-table tbody');
                tbody.innerHTML = '';
                if (passengers.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6">No passengers found.</td></tr>';
                } else {
                    passengers.forEach(passenger => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${passenger.userId}</td>
                            <td>${passenger.firstName} ${passenger.lastName}</td>
                            <td>${passenger.phoneNumber}</td>
                            <td>${passenger.email}</td>
                            <td>${passenger.role}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary edit-passenger" data-id="${passenger.userId}">Edit</button>
                                <button class="btn btn-sm btn-outline-danger delete-passenger" data-id="${passenger.userId}">Delete</button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                    // Edit passenger
                    document.querySelectorAll('.edit-passenger').forEach(button => {
                        button.addEventListener('click', function () {
                            const userId = this.dataset.id;
                            const passenger = allPassengers.find(p => p.userId == userId);
                            document.getElementById('passengerFirstName').value = passenger.firstName;
                            document.getElementById('passengerLastName').value = passenger.lastName;
                            document.getElementById('passengerContact').value = passenger.phoneNumber;
                            document.getElementById('passengerEmail').value = passenger.email;
                            document.getElementById('passengerRole').value = passenger.role;

                            const form = document.getElementById('add-passenger-form');
                            form.action = `/api/passengers/${userId}/update`;
                            form.onsubmit = async (e) => {
                                e.preventDefault();
                                const passengerData = {
                                    firstName: document.getElementById('passengerFirstName').value,
                                    lastName: document.getElementById('passengerLastName').value,
                                    phoneNumber: document.getElementById('passengerContact').value,
                                    email: document.getElementById('passengerEmail').value,
                                    role: document.getElementById('passengerRole').value
                                };
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/api/passengers/${userId}`, {
                                        method: 'PUT',
                                        body: JSON.stringify(passengerData)
                                    });
                                    alert('Passenger updated successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error updating passenger:', error);
                                    alert('Failed to update passenger');
                                }
                            };
                        });
                    });

                    // Delete passenger
                    document.querySelectorAll('.delete-passenger').forEach(button => {
                        button.addEventListener('click', async function () {
                            const userId = this.dataset.id;
                            if (confirm('Are you sure you want to delete this passenger?')) {
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/api/passengers/${userId}`, { method: 'DELETE' });
                                    alert('Passenger deleted successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error deleting passenger:', error);
                                    alert('Failed to delete passenger');
                                }
                            }
                        });
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching passengers:', error);
                document.querySelector('#passengers-table tbody').innerHTML = '<tr><td colspan="6">Error loading passengers.</td></tr>';
            });

        const addPassengerForm = document.getElementById('add-passenger-form');
        if (addPassengerForm) {
            addPassengerForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const passengerData = {
                    firstName: document.getElementById('passengerFirstName').value,
                    lastName: document.getElementById('passengerLastName').value,
                    phoneNumber: document.getElementById('passengerContact').value,
                    email: document.getElementById('passengerEmail').value,
                    role: document.getElementById('passengerRole').value
                };
                try {
                    await fetchWithAuth(`${BACKEND_URL}/api/users/register`, {
                        method: 'POST',
                        body: JSON.stringify(passengerData)
                    });
                    alert('Passenger created successfully');
                    window.location.reload();
                } catch (error) {
                    console.error('Error creating passenger:', error);
                    alert('Failed to create passenger');
                }
            });
        }
    }

    // Operators
    if (currentPath === '/api/operators') {
        let allOperators = [];

        fetchWithAuth(`${BACKEND_URL}/bus-operators`)
            .then(response => response.json())
            .then(operators => {
                allOperators = operators;
                const tbody = document.querySelector('#operators-table tbody');
                tbody.innerHTML = '';
                if (operators.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6">No operators found.</td></tr>';
                } else {
                    operators.forEach(operator => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${operator.operatorId}</td>
                            <td>${operator.operatorName}</td>
                            <td>${operator.contactNumber}</td>
                            <td>${operator.email}</td>
                            <td>${operator.role}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary edit-operator" data-id="${operator.operatorId}">Edit</button>
                                <button class="btn btn-sm btn-outline-danger delete-operator" data-id="${operator.operatorId}">Delete</button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                    // Edit operator
                    document.querySelectorAll('.edit-operator').forEach(button => {
                        button.addEventListener('click', function () {
                            const operatorId = this.dataset.id;
                            const operator = allOperators.find(o => o.operatorId == operatorId);
                            document.getElementById('operatorName').value = operator.operatorName;
                            document.getElementById('operatorContact').value = operator.contactNumber;
                            document.getElementById('operatorEmail').value = operator.email;
                            document.getElementById('operatorRole').value = operator.role;

                            const form = document.getElementById('add-operator-form');
                            form.action = `/api/operators/${operatorId}/update`;
                            form.onsubmit = async (e) => {
                                e.preventDefault();
                                const operatorData = {
                                    operatorName: document.getElementById('operatorName').value,
                                    contactNumber: document.getElementById('operatorContact').value,
                                    email: document.getElementById('operatorEmail').value,
                                    role: document.getElementById('operatorRole').value
                                };
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/bus-operators/${operatorId}`, {
                                        method: 'PUT',
                                        body: JSON.stringify(operatorData)
                                    });
                                    alert('Operator updated successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error updating operator:', error);
                                    alert('Failed to update operator');
                                }
                            };
                        });
                    });

                    // Delete operator
                    document.querySelectorAll('.delete-operator').forEach(button => {
                        button.addEventListener('click', async function () {
                            const operatorId = this.dataset.id;
                            if (confirm('Are you sure you want to delete this operator?')) {
                                try {
                                    await fetchWithAuth(`${BACKEND_URL}/bus-operators/${operatorId}`, { method: 'DELETE' });
                                    alert('Operator deleted successfully');
                                    window.location.reload();
                                } catch (error) {
                                    console.error('Error deleting operator:', error);
                                    alert('Failed to delete operator');
                                }
                            }
                        });
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching operators:', error);
                document.querySelector('#operators-table tbody').innerHTML = '<tr><td colspan="6">Error loading operators.</td></tr>';
            });

        const addOperatorForm = document.getElementById('add-operator-form');
        if (addOperatorForm) {
            addOperatorForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const operatorData = {
                    operatorName: document.getElementById('operatorName').value,
                    contactNumber: document.getElementById('operatorContact').value,
                    email: document.getElementById('operatorEmail').value,
                    role: document.getElementById('operatorRole').value
                };
                try {
                    await fetchWithAuth(`${BACKEND_URL}/bus-operators`, {
                        method: 'POST',
                        body: JSON.stringify(operatorData)
                    });
                    alert('Operator created successfully');
                    window.location.reload();
                } catch (error) {
                    console.error('Error creating operator:', error);
                    alert('Failed to create operator');
                }
            });
        }
    }

    // Bookings
    if (currentPath === '/api/bookings') {
        let allBookings = [];

        fetchWithAuth(`${BACKEND_URL}/bookings`)
            .then(response => response.json())
            .then(bookings => {
                allBookings = bookings;
                const tbody = document.querySelector('#bookings-table tbody');
                tbody.innerHTML = '';
                if (bookings.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="8">No bookings found.</td></tr>';
                } else {
                    Promise.all(bookings.map(booking =>
                        Promise.all([
                            getPassengerName(booking.userId),
                            fetchWithAuth(`${BACKEND_URL}/api/seats/${booking.seatId}`).then(res => res.json()),
                            fetchWithAuth(`${BACKEND_URL}/api/schedules/${booking.scheduleId}`).then(res => res.json()),
                            fetchWithAuth(`${BACKEND_URL}/api/buses/${booking.busId}`).then(res => res.json())
                        ]).then(([passengerName, seat, schedule, bus]) => ({
                            ...booking,
                            passengerName,
                            seatNumber: seat.seatNumber,
                            departure: schedule.departureTime,
                            route: bus.routeId ? `Route #${bus.routeId}` : 'N/A'
                        }))
                    )).then(enrichedBookings => {
                        renderBookings(enrichedBookings);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching bookings:', error);
                document.querySelector('#bookings-table tbody').innerHTML = '<tr><td colspan="8">Error loading bookings.</td></tr>';
            });

        function renderBookings(bookings) {
            const tbody = document.querySelector('#bookings-table tbody');
            tbody.innerHTML = '';
            bookings.forEach(booking => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${booking.bookingId}</td>
                    <td>${booking.passengerName}</td>
                    <td>${booking.route}</td>
                    <td>${new Date(booking.departure).toLocaleString()}</td>
                    <td>${booking.seatNumber}</td>
                    <td>₹${booking.fare}</td>
                    <td>${booking.status}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary edit-booking" data-id="${booking.bookingId}">Edit</button>
                        <button class="btn btn-sm btn-outline-danger cancel-booking" data-id="${booking.bookingId}">Cancel</button>
                    </td>
                `;
                tbody.appendChild(row);
            });

            // Edit booking
            document.querySelectorAll('.edit-booking').forEach(button => {
                button.addEventListener('click', function () {
                    const bookingId = this.dataset.id;
                    const booking = allBookings.find(b => b.bookingId == bookingId);
                    document.getElementById('bookingBus').value = booking.busId;
                    document.getElementById('bookingUser').value = booking.userId;
                    document.getElementById('bookingSeatNumber').value = booking.seatId;
                    document.getElementById('bookingSchedule').value = booking.scheduleId;
                    document.getElementById('bookingDepartureDate').value = booking.bookingDate.slice(0, 16);
                    document.getElementById('bookingFare').value = booking.fare;
                    document.getElementById('bookingStatus').value = booking.status;

                    const form = document.getElementById('new-booking-form');
                    form.action = `/api/bookings/${bookingId}/update`;
                    form.onsubmit = async (e) => {
                        e.preventDefault();
                        const bookingData = {
                            busId: parseInt(document.getElementById('bookingBus').value),
                            userId: parseInt(document.getElementById('bookingUser').value),
                            seatId: parseInt(document.getElementById('bookingSeatNumber').value),
                            scheduleId: parseInt(document.getElementById('bookingSchedule').value),
                            bookingDate: document.getElementById('bookingDepartureDate').value,
                            status: document.getElementById('bookingStatus').value,
                            fare: parseFloat(document.getElementById('bookingFare').value)
                        };
                        try {
                            await fetchWithAuth(`${BACKEND_URL}/bookings/${bookingId}`, {
                                method: 'PUT',
                                body: JSON.stringify(bookingData)
                            });
                            alert('Booking updated successfully');
                            window.location.reload();
                        } catch (error) {
                            console.error('Error updating booking:', error);
                            alert('Failed to update booking');
                        }
                    };
                });
            });

            // Cancel booking
            document.querySelectorAll('.cancel-booking').forEach(button => {
                button.addEventListener('click', async function () {
                    const bookingId = this.dataset.id;
                    if (confirm('Are you sure you want to cancel this booking?')) {
                        try {
                            await fetchWithAuth(`${BACKEND_URL}/bookings/${bookingId}/cancel`, { method: 'POST' });
                            alert('Booking cancelled successfully');
                            window.location.reload();
                        } catch (error) {
                            console.error('Error cancelling booking:', error);
                            alert('Failed to cancel booking');
                        }
                    }
                });
            });
        }

        const newBookingForm = document.getElementById('new-booking-form');
        if (newBookingForm) {
            newBookingForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const bookingData = {
                    busId: parseInt(document.getElementById('bookingBus').value),
                    userId: parseInt(document.getElementById('bookingUser').value),
                    seatId: parseInt(document.getElementById('bookingSeatNumber').value),
                    scheduleId: parseInt(document.getElementById('bookingSchedule').value),
                    bookingDate: document.getElementById('bookingDepartureDate').value,
                    status: document.getElementById('bookingStatus').value,
                    fare: parseFloat(document.getElementById('bookingFare').value)
                };
                try {
                    await fetchWithAuth(`${BACKEND_URL}/bookings`, {
                        method: 'POST',
                        body: JSON.stringify(bookingData)
                    });
                    alert('Booking created successfully');
                    window.location.reload();
                } catch (error) {
                    console.error('Error creating booking:', error);
                    alert('Failed to create booking');
                }
            });
        }
    }

    // Support Tickets
    if (currentPath === '/api/support') {
        let allTickets = [];

        fetchWithAuth(`${BACKEND_URL}/api/support`)
            .then(response => response.json())
            .then(tickets => {
                allTickets = tickets;
                const tbody = document.querySelector('#support-table tbody');
                tbody.innerHTML = '';
                if (tickets.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6">No tickets found.</td></tr>';
                } else {
                    Promise.all(tickets.map(ticket =>
                        getPassengerName(ticket.userId).then(passengerName => ({
                            ...ticket,
                            passengerName
                        }))
                    )).then(enrichedTickets => {
                        renderTickets(enrichedTickets);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching tickets:', error);
                document.querySelector('#support-table tbody').innerHTML = '<tr><td colspan="6">Error loading tickets.</td></tr>';
            });

        function renderTickets(tickets) {
            const tbody = document.querySelector('#support-table tbody');
            tbody.innerHTML = '';
            tickets.forEach(ticket => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${ticket.supportId}</td>
                    <td>${ticket.passengerName}</td>
                    <td>${ticket.subject}</td>
                    <td>${ticket.status}</td>
                    <td>${new Date(ticket.createdAt).toLocaleString()}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary edit-ticket" data-id="${ticket.supportId}">Edit</button>
                        <button class="btn btn-sm btn-outline-danger delete-ticket" data-id="${ticket.supportId}">Delete</button>
                    </td>
                `;
                tbody.appendChild(row);
            });

            // Edit ticket
            document.querySelectorAll('.edit-ticket').forEach(button => {
                button.addEventListener('click', function () {
                    const ticketId = this.dataset.id;
                    const ticket = allTickets.find(t => t.supportId == ticketId);
                    document.getElementById('ticketUser').value = ticket.userId;
                    document.getElementById('ticketSubject').value = ticket.subject;
                    document.getElementById('ticketIssue').value = ticket.description;
                    document.getElementById('ticketStatus').value = ticket.status;

                    const form = document.getElementById('new-ticket-form');
                    form.action = `/api/support/${ticketId}/update`;
                    form.onsubmit = async (e) => {
                        e.preventDefault();
                        const ticketData = {
                            userId: parseInt(document.getElementById('ticketUser').value),
                            subject: document.getElementById('ticketSubject').value,
                            description: document.getElementById('ticketIssue').value,
                            status: document.getElementById('ticketStatus').value
                        };
                        try {
                            await fetchWithAuth(`${BACKEND_URL}/api/support/${ticketId}`, {
                                method: 'PUT',
                                body: JSON.stringify(ticketData)
                            });
                            alert('Ticket updated successfully');
                            window.location.reload();
                        } catch (error) {
                            console.error('Error updating ticket:', error);
                            alert('Failed to update ticket');
                        }
                    };
                });
            });

            // Delete ticket
            document.querySelectorAll('.delete-ticket').forEach(button => {
                button.addEventListener('click', async function () {
                    const ticketId = this.dataset.id;
                    if (confirm('Are you sure you want to delete this ticket?')) {
                        try {
                            await fetchWithAuth(`${BACKEND_URL}/api/support/${ticketId}`, { method: 'DELETE' });
                            alert('Ticket deleted successfully');
                            window.location.reload();
                        } catch (error) {
                            console.error('Error deleting ticket:', error);
                            alert('Failed to delete ticket');
                        }
                    }
                });
            });
        }

        const newTicketForm = document.getElementById('new-ticket-form');
        if (newTicketForm) {
            newTicketForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const ticketData = {
                    userId: parseInt(document.getElementById('ticketUser').value),
                    subject: document.getElementById('ticketSubject').value,
                    description: document.getElementById('ticketIssue').value,
                    status: document.getElementById('ticketStatus').value
                };
                try {
                    await fetchWithAuth(`${BACKEND_URL}/api/support`, {
                        method: 'POST',
                        body: JSON.stringify(ticketData)
                    });
                    alert('Ticket created successfully');
                    window.location.reload();
                } catch (error) {
                    console.error('Error creating ticket:', error);
                    alert('Failed to create ticket');
                }
            });
        }
    }

    // Analytics
    if (currentPath === '/api/analytics') {
        // Fetch analytics data from backend
        fetchWithAuth(`${BACKEND_URL}/api/analytics`)
            .then(response => response.json())
            .then(data => {
                // Example: Render booking trends chart
                const ctx = document.getElementById('booking-trends').getContext('2d');
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: data.labels || ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                        datasets: [{
                            label: 'Bookings',
                            data: data.bookings || [0, 0, 0, 0, 0, 0],
                            borderColor: 'rgba(75, 192, 192, 1)',
                            fill: false
                        }]
                    },
                    options: {
                        responsive: true,
                        scales: {
                            y: { beginAtZero: true }
                        }
                    }
                });
                // Add more charts as needed
            })
            .catch(error => {
                console.error('Error fetching analytics data:', error);
                document.getElementById('booking-trends').parentElement.innerHTML = '<p>Error loading analytics data.</p>';
            });
    }

    // Audit
    if (currentPath === '/api/audit') {
        // Placeholder: Fetch audit logs
        console.log('Audit page loaded');
    }

    // Profile
    if (currentPath === '/api/profile') {
        // Placeholder: Fetch profile data
        console.log('Profile page loaded');
    }
});