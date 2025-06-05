// Sidebar toggle
const toggleSidebar = () => {
    const sidebar = document.querySelector('.sidebar');
    sidebar.classList.toggle('sidebar-hidden');
    const mainContent = document.querySelector('.flex-1');
    mainContent.classList.toggle('ml-0');
    mainContent.classList.toggle('ml-64');
    // Store sidebar state in session storage
    sessionStorage.setItem('sidebarHidden', sidebar.classList.contains('sidebar-hidden'));
};

// Restore sidebar state on page load and set up event listeners
document.addEventListener('DOMContentLoaded', () => {
    const sidebarHidden = sessionStorage.getItem('sidebarHidden') === 'true';
    if (sidebarHidden) {
        toggleSidebar();
    }
    document.getElementById('toggleSidebar').addEventListener('click', toggleSidebar);
});

// Search and Notifications functions (placeholders for now)
window.filterSearch = (type) => {
    console.log('Filtering search by:', type);
    // TODO: Fetch search results via AJAX
};

window.filterNotifications = (type) => {
    console.log('Filtering notifications by:', type);
    // TODO: Fetch notifications via AJAX
};

window.markAllRead = () => {
    console.log('Marking all notifications as read');
    // TODO: Send POST request to mark all read
};

window.clearAllNotifications = () => {
    console.log('Clearing all notifications');
    // TODO: Send POST request to clear all
};