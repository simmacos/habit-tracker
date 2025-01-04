document.addEventListener('DOMContentLoaded', function() {
    const loginBtn = document.getElementById('loginBtn');
    const userProfile = document.getElementById('userProfile');
    const userImg = document.getElementById('userImg');
    const userName = document.getElementById('userName');
    const profileDropdown = document.querySelector('.profile-dropdown');
    const logoutBtn = document.getElementById('logoutBtn');
    const isMobile = window.innerWidth <= 600;

    // Check login status on page load
    checkLoginStatus();

    // Login button click handler
    loginBtn.addEventListener('click', function() {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    });

    // Toggle dropdown with mobile support
    document.querySelector('.profile-trigger')?.addEventListener('click', function(e) {
        e.stopPropagation();
        profileDropdown.classList.toggle('active');
        if (isMobile) {
            document.body.style.overflow = 'hidden'; // Prevent scroll on mobile
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!profileDropdown?.contains(e.target)) {
            profileDropdown?.classList.remove('active');
            if (isMobile) {
                document.body.style.overflow = ''; // Restore scroll
            }
        }
    });

    // Prevent dropdown from closing when clicking inside
    document.querySelector('.dropdown-menu')?.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    // Add close button for mobile
    if (isMobile) {
        const closeBtn = document.createElement('button');
        closeBtn.className = 'dropdown-item';
        closeBtn.innerHTML = '<i class="fas fa-times"></i> Close';
        closeBtn.addEventListener('click', function() {
            profileDropdown.classList.remove('active');
            document.body.style.overflow = '';
        });
        document.querySelector('.dropdown-menu')?.prepend(closeBtn);
    }

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
            credentials: 'include'
        })
        .then(res => res.json())
        .then(data => {
            console.log('Auth status:', data);
            if (data.name) {
                showUserProfile(data);
            } else {
                loginBtn.classList.remove('hidden');
                userProfile.classList.add('hidden');
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
        
        if (user.picture) {
            const img = new Image();
            img.onload = function() {
                userImg.src = user.picture;
            };
            img.onerror = function() {
                console.error('Failed to load profile picture');
                userImg.src = 'data:image/svg+xml;charset=UTF-8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23E4E4DE"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>';
            };
            img.src = user.picture;
        }
        
        userName.textContent = user.name;
    }
});
