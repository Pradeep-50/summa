document.addEventListener("DOMContentLoaded", () => {

    const bookingMenuToggle = document.getElementById("dropup-menu-booking");
    const profileMenuToggle = document.getElementById("dropup-menu");
    const bookingDropdown = document.querySelector(".dropdown-menu-booking");
    const profileDropdown = document.querySelector(".dropdown-menu");

    if (bookingMenuToggle && bookingDropdown) {
        bookingMenuToggle.addEventListener("click", (e) => {
            e.preventDefault();
            const isVisible = bookingDropdown.style.display === "block";
            bookingDropdown.style.display = isVisible ? "none" : "block";
        });
    }

    if (profileMenuToggle && profileDropdown) {
        profileMenuToggle.addEventListener("click", (e) => {
            e.preventDefault();
            const isVisible = profileDropdown.style.display === "block";
            profileDropdown.style.display = isVisible ? "none" : "block";
        });
    }

    // Close dropdowns when clicking outside
    document.addEventListener("click", (e) => {
        if (bookingDropdown && !bookingMenuToggle?.contains(e.target) && !bookingDropdown.contains(e.target)) {
            bookingDropdown.style.display = "none";
        }
        if (profileDropdown && !profileMenuToggle?.contains(e.target) && !profileDropdown.contains(e.target)) {
            profileDropdown.style.display = "none";
        }
    });
});