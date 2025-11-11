document.addEventListener('DOMContentLoaded', function() {
    // This showMessageBox function is included for completeness.
    // In a production environment, ensure it's loaded only once globally
    // or through a shared utility script to avoid duplication.
    function showMessageBox(title, message, isError = true) {
        const overlay = document.getElementById('message-box-overlay');
        const titleElement = document.getElementById('message-box-title');
        const textElement = document.getElementById('message-box-text');
        const okButton = document.getElementById('message-box-ok-button');

        if (!overlay || !titleElement || !textElement || !okButton) {
            console.error("MessageBox: HTML elements not found. Ensure IDs are correct and HTML is loaded.");
            alert(`${title}: ${message}`);
            return;
        }

        titleElement.textContent = title;
        textElement.textContent = message;
        titleElement.style.color = isError ? 'red' : 'green';

        overlay.classList.add('show');

        okButton.onclick = () => {
            overlay.classList.remove('show');
        };

        overlay.onclick = (event) => {
            if (event.target === overlay) {
                overlay.classList.remove('show');
            }
        };
    }

    // Profile page related elements
    const userProfileDiv = document.getElementById('user-profile');
    const loginPromptDiv = document.getElementById('login-prompt');
    const logoutButton = document.getElementById('logout-button');

    // Profile display elements
    const profilePictureDisplay = document.getElementById('profile-picture-display');
    const profileFullNameDisplay = document.getElementById('profile-full-name');
    const profileEmailDisplay = document.getElementById('profile-email-display');
    const profilePhoneDisplay = document.getElementById('profile-phone-display');
    const profileInfoDisplay = document.querySelector('.profile-info-display');
    const editProfileBtn = document.getElementById('edit-profile-btn');

    // Profile edit elements
    const profileInfoEdit = document.querySelector('.profile-info-edit');
    const editProfilePictureInput = document.getElementById('edit-profile-picture');
    const editNameInput = document.getElementById('edit-name');
    const editSurnameInput = document.getElementById('edit-surname');
    const editPhoneInput = document.getElementById('edit-phone');
    const saveProfileBtn = document.getElementById('save-profile-btn');
    const cancelEditBtn = document.getElementById('cancel-edit-btn');

    // Header menu elements for dynamic visibility
    const profileLink = document.getElementById('profile-link');
    const loginLink = document.getElementById('login-link');
    const registerLink = document.getElementById('register-link');

    /**
     * Updates the visibility of header menu links based on login status.
     */
    function updateHeaderMenuVisibility() {
        const isLoggedIn = localStorage.getItem('loggedInUserEmail') !== null;

        if (profileLink) {
            profileLink.style.display = isLoggedIn ? 'block' : 'none';
        }
        if (loginLink) {
            loginLink.style.display = isLoggedIn ? 'none' : 'block';
        }
        if (registerLink) {
            registerLink.style.display = isLoggedIn ? 'none' : 'block';
        }
    }

    /**
     * Renders or updates the profile details on the profile page.
     * @param {object} currentUser The user object to display.
     */
    function renderProfile(currentUser) {
        if (!currentUser) {
            if (userProfileDiv) userProfileDiv.style.display = 'none';
            if (loginPromptDiv) loginPromptDiv.style.display = 'block';
            return;
        }

        // Update display mode elements
        if (profilePictureDisplay) {
            profilePictureDisplay.src = currentUser.profilePicture || 'https://placehold.co/120x120/cccccc/333333?text=Profile';
        }
        if (profileFullNameDisplay) {
            profileFullNameDisplay.textContent = `${currentUser.name || ''} ${currentUser.surname || ''}`;
        }
        if (profileEmailDisplay) {
            profileEmailDisplay.textContent = currentUser.email || '';
        }
        if (profilePhoneDisplay) {
            profilePhoneDisplay.textContent = currentUser.phone || 'N/A';
        }
        
        // Pre-fill edit mode inputs
        if (editNameInput) editNameInput.value = currentUser.name || '';
        if (editSurnameInput) editSurnameInput.value = currentUser.surname || '';
        if (editPhoneInput) editPhoneInput.value = currentUser.phone || '';

        // Show profile section, hide login prompt
        if (userProfileDiv) userProfileDiv.style.display = 'block';
        if (loginPromptDiv) loginPromptDiv.style.display = 'none';

        // Set initial view state to display mode
        if (profileInfoDisplay) profileInfoDisplay.style.display = 'block';
        if (profileInfoEdit) profileInfoEdit.style.display = 'none';
    }

    const loggedInUserEmail = localStorage.getItem('loggedInUserEmail');

    if (loggedInUserEmail) {
        const users = JSON.parse(localStorage.getItem('users')) || [];
        const currentUser = users.find(user => user.email === loggedInUserEmail);

        // If a user is logged in, but their data is missing (e.g., localStorage cleared manually)
        if (!currentUser) {
            localStorage.removeItem('loggedInUserEmail'); // Clear invalid login state
            updateHeaderMenuVisibility(); // Revert menu
            if (userProfileDiv) userProfileDiv.style.display = 'none';
            if (loginPromptDiv) loginPromptDiv.style.display = 'block';
            return; // Stop further execution for profile logic
        }

        renderProfile(currentUser);

        // Event listener for "Edit Profile" button
        if (editProfileBtn) {
            editProfileBtn.addEventListener('click', () => {
                if (profileInfoDisplay) profileInfoDisplay.style.display = 'none';
                if (profileInfoEdit) profileInfoEdit.style.display = 'block';
            });
        }

        // Event listener: "Cancel" button in edit mode
        if (cancelEditBtn) {
            cancelEditBtn.addEventListener('click', () => {
                if (profileInfoDisplay) profileInfoDisplay.style.display = 'block';
                if (profileInfoEdit) profileInfoEdit.style.display = 'none';
                const users = JSON.parse(localStorage.getItem('users')) || [];
                const currentUser = users.find(user => user.email === loggedInUserEmail);
                if (currentUser) {
                    if (editNameInput) editNameInput.value = currentUser.name || '';
                    if (editSurnameInput) editSurnameInput.value = currentUser.surname || '';
                    if (editPhoneInput) editPhoneInput.value = currentUser.phone || '';
                }
            });
        }

        // Event listener: "Save Changes" button
        if (saveProfileBtn) {
            saveProfileBtn.addEventListener('click', () => {
                const updatedName = editNameInput ? editNameInput.value.trim() : '';
                const updatedSurname = editSurnameInput ? editSurnameInput.value.trim() : '';
                const updatedPhone = editPhoneInput ? editPhoneInput.value.trim() : '';
                let updatedProfilePicture = profilePictureDisplay ? profilePictureDisplay.src : '';

                if (!updatedName || !updatedSurname) {
                    showMessageBox('Error', 'Name and Surname cannot be empty.', true);
                    return;
                }

                let users = JSON.parse(localStorage.getItem('users')) || [];
                const userIndex = users.findIndex(user => user.email === loggedInUserEmail);

                if (userIndex > -1) {
                    if (editProfilePictureInput && editProfilePictureInput.files.length > 0) {
                        const file = editProfilePictureInput.files[0];
                        const reader = new FileReader();
                        reader.onload = function(e) {
                            updatedProfilePicture = e.target.result;
                            
                            users[userIndex].name = updatedName;
                            users[userIndex].surname = updatedSurname;
                            users[userIndex].phone = updatedPhone;
                            users[userIndex].profilePicture = updatedProfilePicture;
                            localStorage.setItem('users', JSON.stringify(users));

                            renderProfile(users[userIndex]);
                            showMessageBox('Success', 'Profile updated successfully!', false);
                        };
                        reader.readAsDataURL(file);
                    } else {
                        users[userIndex].name = updatedName;
                        users[userIndex].surname = updatedSurname;
                        users[userIndex].phone = updatedPhone;
                        
                        localStorage.setItem('users', JSON.stringify(users));
                        renderProfile(users[userIndex]);
                        showMessageBox('Success', 'Profile updated successfully!', false);
                    }
                } else {
                    showMessageBox('Error', 'Could not find user data. Please log in again.', true);
                }

                if (profileInfoDisplay) profileInfoDisplay.style.display = 'block';
                if (profileInfoEdit) profileInfoEdit.style.display = 'none';
            });
        }

        // Event listener for "Logout" button
        if (logoutButton) {
            logoutButton.addEventListener('click', () => {
                localStorage.removeItem('loggedInUserEmail');
                updateHeaderMenuVisibility(); 
                window.location.href = 'login.html';
            });
        }

    } else {
        // User not logged in, show login prompt
        if (userProfileDiv) userProfileDiv.style.display = 'none';
        if (loginPromptDiv) loginPromptDiv.style.display = 'block';
    }

    // Login Functionality
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', (event) => {
            event.preventDefault();

            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            const users = JSON.parse(localStorage.getItem('users')) || [];
            const foundUser = users.find(user => user.email === email && user.password === password);

            if (foundUser) {
                localStorage.setItem('loggedInUserEmail', email);
                updateHeaderMenuVisibility(); 
                showMessageBox('Success', `Welcome, ${foundUser.name}! Redirecting to profile...`, false);
                setTimeout(() => {
                    window.location.href = 'profile.html';
                }, 1500);
            } else {
                showMessageBox('Error', 'Invalid email or password. Please try again.', true);
            }
        });
    }

    // Registration Functionality
    const registerForm = document.getElementById('register-form');

    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const name = document.getElementById('register-name').value.trim();
            const surname = document.getElementById('register-surname').value.trim();
            const email = document.getElementById('register-email').value.trim();
            const password = document.getElementById('register-password').value;
            const confirmPassword = document.getElementById('register-confirm-password').value;
            const registerButton = registerForm.querySelector('button[type="submit"]');

            if (!name || !surname || !email || !password || !confirmPassword) {
                showMessageBox('Error', 'Please fill in all fields.', true);
                return;
            }

            if (password !== confirmPassword) {
                showMessageBox('Error', 'Passwords do not match.', true);
                return;
            }

            if (password.length < 6) {
                showMessageBox('Error', 'Password must be at least 6 characters long.', true);
                return;
            }

            let users = JSON.parse(localStorage.getItem('users')) || [];

            if (users.some(user => user.email === email)) {
                showMessageBox('Error', 'An account with this email already exists.', true);
                return;
            }

            const newUser = {
                name: name,
                surname: surname,
                email: email,
                password: password,
                phone: '',
                profilePicture: ''
            };
            users.push(newUser);
            localStorage.setItem('users', JSON.stringify(users));

            localStorage.setItem('loggedInUserEmail', email);
            updateHeaderMenuVisibility(); 

            showMessageBox('Success', 'Registration successful! Redirecting to profile...', false);
            setTimeout(() => {
                window.location.href = 'profile.html';
            }, 1500);

            registerButton.disabled = false;
            registerButton.textContent = 'Register';
        });
    }

    // Forgot Password Functionality
    const forgotPasswordForm = document.getElementById('forgot-password-form');

    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const name = document.getElementById('forgot-password-name') ? document.getElementById('forgot-password-name').value.trim() : '';
            const surname = document.getElementById('forgot-password-surname') ? document.getElementById('forgot-password-surname').value.trim() : '';
            const email = document.getElementById('forgot-password-email') ? document.getElementById('forgot-password-email').value.trim() : '';
            const newPassword = document.getElementById('forgot-password-new-password') ? document.getElementById('forgot-password-new-password').value : '';
            const confirmNewPassword = document.getElementById('forgot-password-confirm-new-password') ? document.getElementById('forgot-password-confirm-new-password').value : '';
            const changePasswordButton = forgotPasswordForm.querySelector('button[type="submit"]');

            if (!name || !surname || !email || !newPassword || !confirmNewPassword) {
                showMessageBox('Error', 'Please fill in all required fields.', true);
                return;
            }

            if (newPassword !== confirmNewPassword) {
                showMessageBox('Error', 'New passwords do not match.', true);
                return;
            }

            if (newPassword.length < 6) {
                showMessageBox('Error', 'New password must be at least 6 characters long.', true);
                return;
            }

            let users = JSON.parse(localStorage.getItem('users')) || [];
            const userIndex = users.findIndex(user =>
                user.email === email && user.name === name && user.surname === surname
            );

            if (userIndex > -1) {
                users[userIndex].password = newPassword;
                localStorage.setItem('users', JSON.stringify(users));
                showMessageBox('Success', 'Password changed successfully! Redirecting to login...', false);
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1500);
            } else {
                showMessageBox('Error', 'User with provided details not found.', true);
            }

            changePasswordButton.disabled = false;
            changePasswordButton.textContent = 'Change Password';
        });
    }

    // Call this function initially to set the correct menu state on page load for all pages
    updateHeaderMenuVisibility();
});
