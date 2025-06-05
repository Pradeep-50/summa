const API_BASE_URL = 'http://localhost:9090/api';

// Utility to show/hide loading spinner
function toggleLoadingSpinner(show) {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.classList.toggle('d-none', !show);
    }
}

// Utility to show error message
function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.classList.remove('d-none');
        setTimeout(() => errorDiv.classList.add('d-none'), 5000);
    } else {
        console.error(message);
    }
}

// Get JWT token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Generic API call with authentication
async function apiCall(url, method = 'GET', body = null) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`,
    };
    const options = { method, headers };
    if (body) {
        options.body = JSON.stringify(body);
    }
    try {
        const response = await fetch(url, options);
        if (response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/admin/login';
            throw new Error('Session expired. Please log in again.');
        }
        if (response.status === 403) {
            throw new Error('You do not have permission to perform this action.');
        }
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `HTTP error! Status: ${response.status}`);
        }
        return response.status === 204 ? {} : await response.json();
    } catch (error) {
        showError(error.message);
        throw error;
    }
}

// Login
async function login(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    const email = document.getElementById('email')?.value;
    const password = document.getElementById('password')?.value;
    const role = document.getElementById('role')?.value;
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });
        if (!response.ok) throw new Error('Invalid credentials');
        const data = await response.json();
        localStorage.setItem('token', data.token);
        window.location.href = role === 'admin' ? '/admin/dashboard' : '/operator/dashboard';
    } catch (error) {
        showError('Login failed: ' + error.message);
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Logout
function logout() {
    localStorage.removeItem('token');
    window.location.href = '/admin/login';
}

// Fetch dashboard metrics
async function fetchDashboardMetrics() {
    toggleLoadingSpinner(true);
    try {
        const path = window.location.pathname;
        let metricsUrl = path.startsWith('/operator') ? `${API_BASE_URL}/operator/dashboard/metrics` : `${API_BASE_URL}/admin/dashboard/metrics`;
        const metrics = await apiCall(metricsUrl);
        document.getElementById('totalBuses').textContent = metrics.totalBuses || 0;
        document.getElementById('totalDrivers').textContent = metrics.totalDrivers || 0;
        document.getElementById('availableSeats').textContent = metrics.availableSeats || 0;
        document.getElementById('maintenanceDue').textContent = metrics.maintenanceDue || 0;
        document.getElementById('activeSchedules').textContent = metrics.activeSchedules || 0;
        document.getElementById('totalBookings').textContent = metrics.totalBookings || 0;
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Fetch seats by busId
async function fetchSeatsByBusId() {
    toggleLoadingSpinner(true);
    try {
        const busId = document.getElementById('filterBusId')?.value;
        const url = busId ? `${API_BASE_URL}/seats/bus/${busId}` : `${API_BASE_URL}/seats`;
        const seats = await apiCall(url);
        const container = document.getElementById('seatContainer');
        container.innerHTML = '';
        seats.forEach(seat => {
            const card = `
                <div class="col-md-4">
                    <div class="card mb-3">
                        <div class="card-body">
                            <h6 class="card-title">Seat #${seat.id}</h6>
                            <p><strong>Bus ID:</strong> ${seat.busId}</p>
                            <p><strong>Number:</strong> ${seat.seatNumber}</p>
                            <p><strong>Type:</strong> ${seat.type}</p>
                            <p><strong>Available:</strong> ${seat.isAvailable ? 'Yes' : 'No'}</p>
                            <button class="btn btn-warning btn-sm me-2" onclick="openUpdateSeatModal('${seat.id}')">Edit</button>
                            <button class="btn btn-danger btn-sm" onclick="deleteSeat('${seat.id}')">Delete</button>
                        </div>
                    </div>
                </div>`;
            container.innerHTML += card;
        });
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Open update seat modal
async function openUpdateSeatModal(id) {
    try {
        const seat = await apiCall(`${API_BASE_URL}/seats/${id}`);
        document.getElementById('updateSeatId').value = seat.id;
        document.getElementById('updateSeatBusId').value = seat.busId;
        document.getElementById('updateSeatNumber').value = seat.seatNumber;
        document.getElementById('updateSeatType').value = seat.type;
        const modal = new bootstrap.Modal(document.getElementById('modalUpdateSeat'));
        modal.show();
    } catch (error) {
        // Error handled by apiCall
    }
}

// Add seat
async function addSeat(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const seatData = {
            busId: parseInt(document.getElementById('seatBusId').value),
            seatNumber: document.getElementById('seatNumber').value,
            type: document.getElementById('seatType').value,
            isAvailable: true
        };
        await apiCall(`${API_BASE_URL}/seats`, 'POST', seatData);
        bootstrap.Modal.getInstance(document.getElementById('modalAddSeat')).hide();
        document.getElementById('formAddSeat').reset();
        fetchSeatsByBusId();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Update seat
async function updateSeat(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const seatData = {
            id: parseInt(document.getElementById('updateSeatId').value),
            busId: parseInt(document.getElementById('updateSeatBusId').value),
            seatNumber: document.getElementById('updateSeatNumber').value,
            type: document.getElementById('updateSeatType').value
        };
        await apiCall(`${API_BASE_URL}/seats/${seatData.id}`, 'PUT', seatData);
        bootstrap.Modal.getInstance(document.getElementById('modalUpdateSeat')).hide();
        fetchSeatsByBusId();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Delete seat
async function deleteSeat(id) {
    if (!confirm('Are you sure you want to delete this seat?')) return;
    toggleLoadingSpinner(true);
    try {
        await apiCall(`${API_BASE_URL}/seats/${id}`, 'DELETE');
        fetchSeatsByBusId();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Fetch routes
async function fetchRoutes() {
    toggleLoadingSpinner(true);
    try {
        const routes = await apiCall(`${API_BASE_URL}/routes`);
        const container = document.getElementById('routeContainer');
        container.innerHTML = '';
        routes.forEach(route => {
            const card = `
                <div class="col-md-4">
                    <div class="card mb-3">
                        <div class="card-body">
                            <h6 class="card-title">Route #${route.id}</h6>
                            <p><strong>Name:</strong> ${route.name}</p>
                            <button class="btn btn-primary btn-sm" onclick="openAssignRouteModal('${route.id}')">
                                Assign to Bus
                            </button>
                        </div>
                    </div>
                </div>`;
            container.innerHTML += card;
        });
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Search routes
async function searchRoutes() {
    toggleLoadingSpinner(true);
    try {
        const query = document.getElementById('searchRoute')?.value;
        const url = query ? `${API_BASE_URL}/routes/search?query=${encodeURIComponent(query)}` : `${API_BASE_URL}/routes`;
        const routes = await apiCall(url);
        const container = document.getElementById('routeContainer');
        container.innerHTML = '';
        routes.forEach(route => {
            const card = `
                <div class="col-md-4">
                    <div class="card mb-3">
                        <div class="card-body">
                            <h6 class="card-title">Route #${route.id}</h6>
                            <p><strong>Name:</strong> ${route.name}</p>
                            <button class="btn btn-primary btn-sm" onclick="openAssignRouteModal('${route.id}')">
                                Assign to Bus
                            </button>
                        </div>
                    </div>
                </div>`;
            container.innerHTML += card;
        });
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Open assign route modal
function openAssignRouteModal(routeId) {
    document.getElementById('assignRouteId').value = routeId;
    const modal = new bootstrap.Modal(document.getElementById('assign-bus-modal'));
    modal.show();
}

// Assign route
async function assignRoute(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const routeData = {
            routeId: parseInt(document.getElementById('assignRouteId').value),
            busId: parseInt(document.getElementById('busId').value)
        };
        await apiCall(`${API_BASE_URL}/routes/assign`, 'POST', routeData);
        bootstrap.Modal.getInstance(document.getElementById('assign-bus-modal')).hide();
        fetchRoutes();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Fetch buses
async function fetchBuses() {
    toggleLoadingSpinner(true);
    try {
        const buses = await apiCall(`${API_BASE_URL}/buses`);
        const container = document.getElementById('busContainer');
        container.innerHTML = '';
        buses.forEach(bus => {
            const card = `
                <div class="col-md-6">
                    <div class="card mb-3">
                        <div class="card-body">
                            <h6 class="card-title">Bus #${bus.id}</h6>
                            <p><strong>Number:</strong> ${bus.busNumber}</p>
                            <p><strong>Route:</strong> ${bus.routeId || 'N/A'}</p>
                            <p><strong>Seats:</strong> ${bus.totalSeats}</p>
                            <p><strong>Amenities:</strong> ${bus.amenities ? bus.amenities.join(', ') : 'None'}</p>
                            <button class="btn btn-warning btn-sm me-2" onclick="openUpdateBusModal('${bus.id}')">Edit</button>
                            <button class="btn btn-danger btn-sm me-2" onclick="deleteBus('${bus.id}')">Delete</button>
                            <button class="btn btn-primary btn-sm me-2" onclick="openAssignRouteModalForBus('${bus.id}')">Assign Route</button>
                            <button class="btn btn-primary btn-sm" onclick="openAssignAmenityModal('${bus.id}')">Assign Amenity</button>
                        </div>
                    </div>
                </div>`;
            container.innerHTML += card;
        });
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Open update bus modal
async function openUpdateBusModal(id) {
    try {
        const bus = await apiCall(`${API_BASE_URL}/buses/${id}`);
        document.getElementById('updateBusId').value = bus.id;
        document.getElementById('updateBusNumber').value = bus.busNumber;
        document.getElementById('updateBusRouteId').value = bus.routeId || '';
        document.getElementById('updateTotalSeats').value = bus.totalSeats;
        const modal = new bootstrap.Modal(document.getElementById('modalUpdateBus'));
        modal.show();
    } catch (error) {
        // Error handled by apiCall
    }
}

// Open assign route modal for buses
function openAssignRouteModalForBus(busId) {
    document.getElementById('assignRouteBusId').value = busId;
    const modal = new bootstrap.Modal(document.getElementById('modalAssignRoute'));
    modal.show();
}

// Open assign amenity modal
function openAssignAmenityModal(busId) {
    document.getElementById('assignAmenityBusId').value = busId;
    const modal = new bootstrap.Modal(document.getElementById('modalAssignAmenity'));
    modal.show();
}

// Add bus
async function addBus(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const busData = {
            busNumber: document.getElementById('busNumber').value,
            routeId: document.getElementById('busRouteId').value ? parseInt(document.getElementById('busRouteId').value) : null,
            totalSeats: parseInt(document.getElementById('totalSeats').value)
        };
        await apiCall(`${API_BASE_URL}/buses`, 'POST', busData);
        bootstrap.Modal.getInstance(document.getElementById('modalAddBus')).hide();
        document.getElementById('formAddBus').reset();
        fetchBuses();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Update bus
async function updateBus(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const busData = {
            id: parseInt(document.getElementById('updateBusId').value),
            busNumber: document.getElementById('updateBusNumber').value,
            routeId: document.getElementById('updateBusRouteId').value ? parseInt(document.getElementById('updateBusRouteId').value) : null,
            totalSeats: parseInt(document.getElementById('updateTotalSeats').value)
        };
        await apiCall(`${API_BASE_URL}/buses/${busData.id}`, 'PUT', busData);
        bootstrap.Modal.getInstance(document.getElementById('modalUpdateBus')).hide();
        fetchBuses();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Delete bus
async function deleteBus(id) {
    if (!confirm('Are you sure you want to delete this bus?')) return;
    toggleLoadingSpinner(true);
    try {
        await apiCall(`${API_BASE_URL}/buses/${id}`, 'DELETE');
        fetchBuses();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Assign route to bus
async function assignRouteToBus(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const routeData = {
            busId: parseInt(document.getElementById('assignRouteBusId').value),
            routeId: parseInt(document.getElementById('assignRouteId').value)
        };
        await apiCall(`${API_BASE_URL}/buses/assign-route`, 'POST', routeData);
        bootstrap.Modal.getInstance(document.getElementById('modalAssignRoute')).hide();
        fetchBuses();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Assign amenity
async function assignAmenity(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const amenityData = {
            busId: parseInt(document.getElementById('assignAmenityBusId').value),
            amenityId: parseInt(document.getElementById('amenityId').value)
        };
        await apiCall(`${API_BASE_URL}/buses/assign-amenity`, 'POST', amenityData);
        bootstrap.Modal.getInstance(document.getElementById('modalAssignAmenity')).hide();
        fetchBuses();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Fetch bookings by busId
async function fetchBookingsByBusId() {
    toggleLoadingSpinner(true);
    try {
        const busId = document.getElementById('filterBusId')?.value;
        const url = busId ? `${API_BASE_URL}/bookings/bus/${busId}` : `${API_BASE_URL}/bookings`;
        const bookings = await apiCall(url);
        const container = document.getElementById('bookingContainer');
        container.innerHTML = '';
        bookings.forEach(booking => {
            const card = `
                <div class="col-md-4">
                    <div class="card mb-3">
                        <div class="card-body">
                            <h6 class="card-title">Booking #${booking.id}</h6>
                            <p><strong>Bus ID:</strong> ${booking.busId}</p>
                            <p><strong>Customer:</strong> ${booking.customerName}</p>
                            <p><strong>Seat:</strong> ${booking.seatNumber}</p>
                            <p><strong>Status:</strong> ${booking.status}</p>
                            <button class="btn btn-danger btn-sm" onclick="cancelBooking('${booking.id}')" ${booking.status === 'CANCELLED' ? 'disabled' : ''}>Cancel</button>
                        </div>
                    </div>
                </div>`;
            container.innerHTML += card;
        });
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Cancel booking
async function cancelBooking(id) {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    toggleLoadingSpinner(true);
    try {
        await apiCall(`${API_BASE_URL}/bookings/${id}/cancel`, 'POST');
        fetchBookingsByBusId();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Fetch drivers
async function fetchDrivers() {
    toggleLoadingSpinner(true);
    try {
        const drivers = await apiCall(`${API_BASE_URL}/drivers`);
        const container = document.getElementById('driverContainer');
        container.innerHTML = '';
        drivers.forEach(driver => {
            const card = `
                <div class="col-md-4">
                    <div class="card mb-3">
                        <div class="card-body">
                            <h6 class="card-title">Driver #${driver.id}</h6>
                            <p><strong>Name:</strong> ${driver.name}</p>
                            <p><strong>Contact:</strong> ${driver.contactNumber}</p>
                            <p><strong>License:</strong> ${driver.licenseNumber}</p>
                            <p><strong>Bus:</strong> ${driver.busId || 'None'}</p>
                            <button class="btn btn-warning btn-sm me-2" onclick="openUpdateDriverModal('${driver.id}')">Edit</button>
                            <button class="btn btn-danger btn-sm me-2" onclick="deleteDriver('${driver.id}')">Delete</button>
                            <button class="btn btn-primary btn-sm" onclick="openAssignDriverModal('${driver.id}')">Assign Bus</button>
                        </div>
                    </div>
                </div>`;
            container.innerHTML += card;
        });
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Open update driver modal
async function openUpdateDriverModal(id) {
    try {
        const driver = await apiCall(`${API_BASE_URL}/drivers/${id}`);
        document.getElementById('updateDriverId').value = driver.id;
        document.getElementById('updateDriverName').value = driver.name;
        document.getElementById('updateContactNumber').value = driver.contactNumber;
        document.getElementById('updateLicenseNumber').value = driver.licenseNumber;
        const modal = new bootstrap.Modal(document.getElementById('modalUpdateDriver'));
        modal.show();
    } catch (error) {
        // Error handled by apiCall
    }
}

// Open assign driver modal
function openAssignDriverModal(driverId) {
    document.getElementById('assignDriverId').value = driverId;
    const modal = new bootstrap.Modal(document.getElementById('modalAssignDriver'));
    modal.show();
}

// Add driver
async function addDriver(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const driverData = {
            name: document.getElementById('driverName').value,
            contactNumber: document.getElementById('contactNumber').value,
            licenseNumber: document.getElementById('licenseNumber').value
        };
        await apiCall(`${API_BASE_URL}/drivers`, 'POST', driverData);
        bootstrap.Modal.getInstance(document.getElementById('modalAddDriver')).hide();
        document.getElementById('formAddDriver').reset();
        fetchDrivers();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Update driver
async function updateDriver(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const driverData = {
            id: parseInt(document.getElementById('updateDriverId').value),
            name: document.getElementById('updateDriverName').value,
            contactNumber: document.getElementById('updateContactNumber').value,
            licenseNumber: document.getElementById('updateLicenseNumber').value
        };
        await apiCall(`${API_BASE_URL}/drivers/${driverData.id}`, 'PUT', driverData);
        bootstrap.Modal.getInstance(document.getElementById('modalUpdateDriver')).hide();
        fetchDrivers();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Delete driver
async function deleteDriver(id) {
    if (!confirm('Are you sure you want to delete this driver?')) return;
    toggleLoadingSpinner(true);
    try {
        await apiCall(`${API_BASE_URL}/drivers/${id}`, 'DELETE');
        fetchDrivers();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Assign driver
async function assignDriver(event) {
    event.preventDefault();
    toggleLoadingSpinner(true);
    try {
        const driverData = {
            driverId: parseInt(document.getElementById('assignDriverId').value),
            busId: parseInt(document.getElementById('assignBusId').value)
        };
        await apiCall(`${API_BASE_URL}/drivers/${driverData.driverId}/assign`, 'POST', driverData);
        bootstrap.Modal.getInstance(document.getElementById('modalAssignDriver')).hide();
        fetchDrivers();
    } catch (error) {
        // Error handled by apiCall
    } finally {
        toggleLoadingSpinner(false);
    }
}

// Initialize page-specific functionality
document.addEventListener('DOMContentLoaded', () => {
    try {
        const path = window.location.pathname;
        if (path === '/operator/dashboard' || path === '/admin/dashboard') {
            fetchDashboardMetrics();
        } else if (path.includes('/operator/seats') || path.includes('/admin/seats')) {
            fetchSeatsByBusId();
            document.getElementById('formAddSeat')?.addEventListener('submit', addSeat);
            document.getElementById('formUpdateSeat')?.addEventListener('submit', updateSeat);
            document.getElementById('filterBusId')?.addEventListener('change', fetchSeatsByBusId);
        } else if (path.includes('/operator/routes') || path.includes('/admin/routes')) {
            fetchRoutes();
            document.getElementById('formAssignRoute')?.addEventListener('submit', assignRoute);
            document.getElementById('searchRoute')?.addEventListener('input', searchRoutes);
        } else if (path.includes('/operator/buses') || path.includes('/admin/buses')) {
            fetchBuses();
            document.getElementById('formAddBus')?.addEventListener('submit', addBus);
            document.getElementById('formUpdateBus')?.addEventListener('submit', updateBus);
            document.getElementById('formAssignRoute')?.addEventListener('submit', assignRouteToBus);
            document.getElementById('formAssignAmenity')?.addEventListener('submit', assignAmenity);
        } else if (path.includes('/operator/bookings') || path.includes('/admin/bookings')) {
            fetchBookingsByBusId();
            document.getElementById('filterBusId')?.addEventListener('change', fetchBookingsByBusId);
        } else if (path.includes('/operator/drivers') || path.includes('/admin/drivers')) {
            fetchDrivers();
            document.getElementById('formAddDriver')?.addEventListener('submit', addDriver);
            document.getElementById('formUpdateDriver')?.addEventListener('submit', updateDriver);
            document.getElementById('formAssignDriver')?.addEventListener('submit', assignDriver);
        } else if (path === '/admin/login' || path === '/operator/login') {
            document.getElementById('formLogin')?.addEventListener('submit', login);
        }
        // No initialization for /operator/about or /admin/about
    } catch (error) {
        console.error('Initialization error:', error);
    }
});