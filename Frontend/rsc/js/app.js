document.addEventListener('DOMContentLoaded', function() {
    // Elementi esistenti per l'autenticazione
    const loginBtn = document.getElementById('loginBtn');
    const userProfile = document.getElementById('userProfile');
    const userImg = document.getElementById('userImg');
    const userName = document.getElementById('userName');
    const profileDropdown = document.querySelector('.profile-dropdown');
    const logoutBtn = document.getElementById('logoutBtn');

    // Nuovi elementi per habits
    const addHabitBtn = document.getElementById('addHabitBtn');
    const modal = document.getElementById('addHabitModal');
    const closeModalBtn = document.getElementById('closeModal');
    const cancelHabitBtn = document.getElementById('cancelHabit');
    const saveHabitBtn = document.getElementById('saveHabit');
    const weekDaysButtons = document.querySelectorAll('.day-btn');
    const isHobbyToggle = document.getElementById('isHobby');
    const habitsList = document.getElementById('habitsList');
    const hobbiesList = document.getElementById('hobbiesList');
    const hobbiesSection = document.getElementById('hobbiesSection');

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
            document.body.style.overflow = 'hidden';
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!profileDropdown?.contains(e.target)) {
            profileDropdown?.classList.remove('active');
            if (isMobile) {
                document.body.style.overflow = '';
            }
        }
    });

    // Prevent dropdown from closing when clicking inside
    document.querySelector('.dropdown-menu')?.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    // Modal Management
    addHabitBtn?.addEventListener('click', openModal);
    closeModalBtn?.addEventListener('click', closeModal);
    cancelHabitBtn?.addEventListener('click', closeModal);

    // Close modal when clicking outside
    modal?.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeModal();
        }
    });

    // Week days selection
    weekDaysButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            if (isHobbyToggle.checked) return;
            this.classList.toggle('active');
        });
    });

    // Hobby toggle handling
    isHobbyToggle?.addEventListener('change', function() {
        if (this.checked) {
            weekDaysButtons.forEach(btn => {
                btn.classList.add('active');
                btn.style.opacity = '0.7';
                btn.style.cursor = 'not-allowed';
            });
        } else {
            weekDaysButtons.forEach(btn => {
                btn.style.opacity = '1';
                btn.style.cursor = 'pointer';
            });
        }
    });

    // Save habit
    saveHabitBtn?.addEventListener('click', saveHabit);

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
        .then(res => {
            if (!res.ok) throw new Error('Network response was not ok');
            return res.json();
        })
        .then(data => {
            console.log('Auth status:', data);
            if (data.name) {
                showUserProfile(data);
                loadHabits();
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

    function loadHabits() {
        fetch('http://localhost:8080/api/habits?includeHobbies=true', {
            credentials: 'include'
        })
        .then(res => res.json())
        .then(habits => {
            const today = new Date().getDay(); // 0-6 (Sunday-Saturday)
            
            // Filtra gli habits per mostrare solo quelli schedulati per oggi e gli hobby
            const filteredHabits = habits.filter(habit => 
                habit.isHobby || habit.schedule[today] === '1'
            );
    
            const regularHabits = filteredHabits.filter(h => !h.isHobby);
            const hobbies = filteredHabits.filter(h => h.isHobby);
    
            // Render regular habits
            habitsList.innerHTML = regularHabits.length > 0 
                ? regularHabits.map(habit => createHabitCard(habit)).join('')
                : '<div class="no-habits">No habits scheduled for today</div>';
    
            // Only show hobbies section if there are hobbies
            if (hobbies.length > 0) {
                hobbiesSection.style.display = 'block';
                hobbiesList.innerHTML = hobbies.map(habit => createHabitCard(habit)).join('');
            } else {
                hobbiesSection.style.display = 'none';
            }
    
            addHabitCardListeners();
        })
        .catch(err => {
            console.error('Error loading habits:', err);
            habitsList.innerHTML = '<div class="error">Error loading habits</div>';
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

    function createHabitCard(habit) {
        // Controlla se l'habit è stata completata oggi
        const isCompletedToday = habit.completions?.some(completion => {
            const completionDate = completion.id.completionDate;
            const today = new Date().toISOString().split('T')[0];
            return completionDate === today;
        });
    
        return `
            <div class="habit-card" data-id="${habit.id}">
                <input type="checkbox" class="habit-checkbox" ${isCompletedToday ? 'checked' : ''}>
                <div class="habit-content">
                    <div class="habit-info">
                        <div class="habit-title">${habit.name}</div>
                        <div class="habit-meta">${habit.isHobby ? 'Flexible' : getScheduleDisplay(habit.schedule)}</div>
                    </div>
                    <div class="habit-streak">
                        <i class="fas fa-fire"></i>
                        <span>0 days</span>
                    </div>
                </div>
            </div>
        `;
    }    

    function getScheduleDisplay(schedule) {
        if (!schedule) return '';
        
        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const activeDays = schedule.split('')
            .map((active, index) => active === '1' ? days[index] : null)
            .filter(Boolean); // Questo rimuove tutti i valori null/undefined/false
        
        if (activeDays.length === 7) return 'Daily';
        if (activeDays.length === 0) return 'No days selected';
        return activeDays.join(', ');
    }    
    

    function openModal() {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        resetModalForm();
    }

    function closeModal() {
        modal.classList.remove('active');
        document.body.style.overflow = '';
        resetModalForm();
    }

    function resetModalForm() {
        document.getElementById('habitName').value = '';
        document.getElementById('habitDesc').value = '';
        isHobbyToggle.checked = false;
        weekDaysButtons.forEach(btn => {
            btn.classList.add('active');
            btn.style.opacity = '1';
            btn.style.cursor = 'pointer';
        });
    }

    function getScheduleFromButtons() {
        return Array.from(weekDaysButtons)
            .map(btn => btn.classList.contains('active') ? '1' : '0')
            .join('');
    }

    function saveHabit() {
        const name = document.getElementById('habitName').value.trim();
        const description = document.getElementById('habitDesc').value.trim();
        const isHobby = isHobbyToggle.checked;
        const schedule = isHobby ? '1111111' : getScheduleFromButtons();

        if (!name) {
            alert('Please enter a habit name');
            return;
        }

        const habitData = {
            name: name,
            description: description,
            isHobby: isHobby,
            schedule: schedule
        };

        fetch('http://localhost:8080/api/habits', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(habitData)
        })
        .then(res => {
            if (!res.ok) throw new Error('Network response was not ok');
            return res.json();
        })
        .then(habit => {
            console.log('Habit saved:', habit);
            closeModal();
            loadHabits();
        })
        .catch(err => {
            console.error('Error saving habit:', err);
            alert('Error saving habit. Please try again.');
        });
    }

    function addHabitCardListeners() {
        document.querySelectorAll('.habit-checkbox').forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const habitId = this.closest('.habit-card').dataset.id;
                toggleHabitCompletion(habitId);
            });
        });

        document.querySelectorAll('.habit-card').forEach(card => {
            card.addEventListener('click', function(e) {
                if (!e.target.classList.contains('habit-checkbox')) {
                    const habitId = this.dataset.id;
                    showHabitDetails(habitId);
                }
            });
        });
    }

    function toggleHabitCompletion(habitId) {
        const today = new Date().toISOString().split('T')[0];
        fetch(`http://localhost:8080/api/habits/${habitId}/completions/toggle?date=${today}`, {
            method: 'POST',
            credentials: 'include'
        })
        .then(res => {
            if (!res.ok) throw new Error('Network response was not ok');
            return res.json();
        })
        .then(data => {
            // Ricarica solo dopo conferma dal server
            loadHabits();
        })
        .catch(err => {
            console.error('Error toggling completion:', err);
            // Ripristina lo stato del checkbox in caso di errore
            loadHabits();
        });
    }
    

    function showHabitDetails(habitId) {
        const modal = document.getElementById('habitDetailsModal');
        const viewMode = document.getElementById('viewMode');
        const editMode = document.getElementById('editMode');
        const editBtn = document.getElementById('editHabitBtn');
        const saveEditBtn = document.getElementById('saveEditBtn');
        
        fetch(`http://localhost:8080/api/habits/${habitId}`, {
            credentials: 'include'
        })
        .then(res => res.json())
        .then(habit => {
            // Set title
            document.getElementById('habitDetailTitle').textContent = habit.name;
            
            // Set description with markdown
            const descriptionHtml = marked.parse(habit.description || '');
            document.getElementById('habitDescription').innerHTML = descriptionHtml;
    
            // Show modal
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';

            modal.addEventListener('click', function(e) {
                // Se il click è sul modal (sfondo) e non sul suo contenuto
                if (e.target === modal) {
                    closeDetailsModal();
                }
            });
    
            // Edit mode
            editBtn.onclick = () => {
                viewMode.classList.add('hidden');
                editMode.classList.remove('hidden');
                editBtn.classList.add('hidden');
                saveEditBtn.classList.remove('hidden');
                
                // Populate edit form
                document.getElementById('editDescription').value = habit.description;
                const weekDays = document.querySelectorAll('#editWeekDays .day-btn');
                habit.schedule.split('').forEach((active, index) => {
                    weekDays[index].classList.toggle('active', active === '1');
                });
            };
    
            // Save changes
            saveEditBtn.onclick = () => {
                const updatedSchedule = Array.from(document.querySelectorAll('#editWeekDays .day-btn'))
                    .map(btn => btn.classList.contains('active') ? '1' : '0')
                    .join('');
                
                const updatedDescription = document.getElementById('editDescription').value;
    
                fetch(`http://localhost:8080/api/habits/${habitId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify({
                        ...habit,
                        schedule: updatedSchedule,
                        description: updatedDescription
                    })
                })
                .then(res => res.json())
                .then(() => {
                    loadHabits();
                    closeDetailsModal();
                });
            };
    
            // Close modal handler
            document.getElementById('closeDetailsModal').onclick = closeDetailsModal;
    
            // Delete handler
            document.getElementById('deleteHabitBtn').onclick = () => {
                if (confirm('Are you sure you want to delete this habit?')) {
                    fetch(`http://localhost:8080/api/habits/${habitId}`, {
                        method: 'DELETE',
                        credentials: 'include'
                    })
                    .then(() => {
                        loadHabits();
                        closeDetailsModal();
                    });
                }
            };
        });
    
        function closeDetailsModal() {
            modal.classList.remove('active');
            document.body.style.overflow = '';
            viewMode.classList.remove('hidden');
            editMode.classList.add('hidden');
            editBtn.classList.remove('hidden');
            saveEditBtn.classList.add('hidden');
        }
    }    
    

    // Add mobile close button
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
});
