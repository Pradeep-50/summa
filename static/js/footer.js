function footer(){
    return`<div class="main-div8">
    <div class="div8-inner">
        <div>
        <ul>
            <li class="ul-li-head">About Nex-Bus</li>
            <li>About Us</li>
            <li>Contact Us</li>
            <li>Mobile Version</li>
            <li>Nex-Bus On Mobile</li>
            <li>Sitemap</li>
            <li>Offers</li>
            <li>Careers</li>
            <li>Values</li>
        </ul>
        </div>
        <div>
            <ul>
                <li class="ul-li-head">Info</li>
                <li>T & C</li>
                <li>Privacy Policy</li>
                <li>FAQ</li>
                <li>Blog</li>
                <li>Bus Oprator Registration</li>
                <li>Agent Registration</li>
                <li>Insurance Partner</li>
                <li>User Agreement</li>
            </ul>
        </div>
        <div>
            <ul>
                <li class="ul-li-head">Global Site</li>
                <li>India</li>
                <li>Singapor</li>
                <li>Malaysia</li>
                <li>Peru</li>
                <li>Indonesia</li>
                <li>Colombia</li>
            </ul>
        </div>
        <div>
            <ul>
                <li class="ul-li-head">Our Partener</li>
                <li>Goibibo</li>
                <li>Makemytrip</li>
            </ul>
        </div>
        <div class="red-bus">
            <img th:src="@{/images/logo.png}" alt="Nex-Bus">
            <p>Nex-Bus is the world's largest online bus ticket booking service trusted by over 25 million happy customers globally. Nex-Bus offers bus ticket booking through its website,iOS and Android mobile apps for all major routes.</p>
            <div class="img">
                <img th:src="@{/images/facebook.png}" alt=""><img src="./indexImages/twitter.png" alt="">
            </div>
            
            <p>Ⓒ 2022 ibibogroup All rights reserved</p>
        </div>
    </div>
</div>`
}

export default footer;