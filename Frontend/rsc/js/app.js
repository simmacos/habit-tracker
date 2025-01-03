document.addEventListener('DOMContentLoaded', function() {
    const loginBtn = document.getElementById('loginBtn');
    const userProfile = document.getElementById('userProfile');
    const userImg = document.getElementById('userImg');
    const userName = document.getElementById('userName');
    const profileDropdown = document.querySelector('.profile-dropdown');
    const logoutBtn = document.getElementById('logoutBtn');

    // Check login status on page load
    checkLoginStatus();

    // Login button click handler
    loginBtn.addEventListener('click', function() {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    });

    // Toggle dropdown
    document.querySelector('.profile-trigger')?.addEventListener('click', function(e) {
        e.stopPropagation();
        profileDropdown.classList.toggle('active');
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function() {
        profileDropdown?.classList.remove('active');
    });

    // Prevent dropdown from closing when clicking inside
    document.querySelector('.dropdown-menu')?.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    // Logout handling
    logoutBtn?.addEventListener('click', function() {
        fetch('http://localhost:8080/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        })
        .then(() => {
            window.location.reload();
        })
        .catch(err => console.error('Logout error:', err));
    });

    function checkLoginStatus() {
        fetch('http://localhost:8080/api/auth/user', {
            credentials: 'include'  // Important for cookies
        })
        .then(res => res.json())
        .then(data => {
            console.log('Auth status:', data);
            if (data.name) {
                showUserProfile(data);
            }
        })
        .catch(err => {
            console.error('Error checking auth:', err);
            loginBtn.classList.remove('hidden');
            userProfile.classList.add('hidden');
        });
    }

    function showUserProfile(user) {
        loginBtn.classList.add('hidden');
        userProfile.classList.remove('hidden');
        userImg.src = user.picture;
        userName.textContent = user.name;
    }
});