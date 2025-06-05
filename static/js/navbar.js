function navbar() {
  return `<div id="headerBlock">
    <article class="container">
      <nav class="leftNav">
        <section>
          <div class="logo">
            <a th:href="@{/imag/logo.png}"><img src="" alt="Nex-Bus"></a>
          </div>
          <div class="leftmenu">
          </div>
        </section>
      </nav>
      <!----right side menu-->
      <nav class="rightNav">
        <ul>
          <li><a th:href="@{/help}">Help</a></li>
          <li>
            <a href="#" id="dropup-menu-booking">Manage booking
            <span><i class="fas fa-chevron-down"></i> </span> 
            </a>
            <div class="dropdown-menu-booking">
              <div>
                <a class="dropdown-item" th:href="@{/cancel-ticket}">Cancel Ticket</a><br>
                <a class="dropdown-item" th:href="@{/show-ticket}">Show My Ticket</a><br>
                <a class="dropdown-item" th:href="@{/change-travel-date}">Change Travel Date</a><br>
              </div>
            </div>
          </li>
          <li id="Signup">
            <a href="#" id="dropup-menu">
              <span><i class="far fa-user-circle"></i></span>
              <span><i class="fas fa-chevron-down after_user_circle"></i></span>
            </a>
            <div class="dropdown-menu">
              <div class="drop">
                <a class="dropdown-item" th:href="@{/my-trips}">MY Trips</a><br>
                <a class="dropdown-item" th:href="@{/wallet-and-card}">Wallets</a><br>
                <a class="dropdown-item" th:href="@{/my-profile}">MY Profile</a><br>
                <a class="dropdown-item" th:href="@{/logout}">Sign Out</a><br>
              </div>          
            </div>
          </li>
        </ul>
      </nav>
    </article>
  </div>`;
}

export default navbar;