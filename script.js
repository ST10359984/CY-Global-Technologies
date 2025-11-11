document.addEventListener('DOMContentLoaded', function() {
    function toggleDropdown() {
        const dropdownContent = document.querySelector('.dropdown-content');
        if (dropdownContent) {
            dropdownContent.classList.toggle('show');
        }
    }

    function closeDropdownOutsideClick(event) {
        const dropdownButton = document.querySelector('.dropbtn');
        const dropdownContent = document.querySelector('.dropdown-content');
        if (dropdownButton && dropdownContent && !event.target.matches('.dropbtn')) {
            if (dropdownContent.classList.contains('show')) {
                dropdownContent.classList.remove('show');
            }
        }
    }

    const dropdownButton = document.querySelector('.dropbtn');
    if (dropdownButton) {
        dropdownButton.addEventListener('click', toggleDropdown);
    }

    window.addEventListener('click', closeDropdownOutsideClick);

    const priceRangeInput = document.getElementById('price-range');
    const priceValueSpan = document.getElementById('price-value');
    const brandSelect = document.getElementById('brand');
    const productTypeSelect = document.getElementById('product-type');
    const conditionAllCheckbox = document.getElementById('all');
    const conditionNewCheckbox = document.getElementById('new');
    const conditionSecondHandCheckbox = document.getElementById('second-hand');
    const searchProductsInput = document.getElementById('search-products');
    const sortProductsSelect = document.getElementById('sort-products');
    const productGridContainer = document.querySelector('.product-grid');

    let initialProductOrder = [];
    if (productGridContainer) {
        initialProductOrder = Array.from(productGridContainer.children);
    }

    function applyFiltersAndSort() {
        if (!document.querySelector('.products-page')) {
            return;
        }

        const price = priceRangeInput ? parseInt(priceRangeInput.value) : null;
        const brand = brandSelect ? brandSelect.value.toLowerCase() : '';
        const productType = productTypeSelect ? productTypeSelect.value.toLowerCase() : '';
        const showAllConditions = conditionAllCheckbox ? conditionAllCheckbox.checked : true;
        const isNew = conditionNewCheckbox ? conditionNewCheckbox.checked : false;
        const isSecondHand = conditionSecondHandCheckbox ? conditionSecondHandCheckbox.checked : false;
        const searchTerm = searchProductsInput ? searchProductsInput.value.toLowerCase() : '';
        const sortBy = sortProductsSelect ? sortProductsSelect.value : 'default';

        let filteredProducts = initialProductOrder.filter(card => {
            const cardBrand = card.dataset.brand ? card.dataset.brand.toLowerCase() : '';
            const cardPrice = parseFloat(card.dataset.price);
            const cardType = card.dataset.type ? card.dataset.type.toLowerCase() : '';
            const cardConditionElement = card.querySelector('.condition');
            const cardCondition = cardConditionElement ? (cardConditionElement.classList.contains('new') ? 'new' : 'second-hand') : '';
            const cardTitleElement = card.querySelector('h4'); 
            const cardName = cardTitleElement ? cardTitleElement.textContent.toLowerCase() : '';

            const priceMatch = (price === null || isNaN(price)) || cardPrice <= price;
            const brandMatch = !brand || cardBrand.includes(brand);
            const typeMatch = !productType || cardType === productType;
            const conditionMatch = showAllConditions ||
                                    (isNew && cardCondition === 'new') ||
                                    (isSecondHand && cardCondition === 'second-hand');
            const searchMatch = !searchTerm || cardName.includes(searchTerm);

            return priceMatch && brandMatch && typeMatch && conditionMatch && searchMatch;
        });

        if (sortBy !== 'default') {
            filteredProducts.sort((a, b) => {
                const nameA = a.dataset.name.toLowerCase();
                const nameB = b.dataset.name.toLowerCase();
                const priceA = parseFloat(a.dataset.price);
                const priceB = parseFloat(b.dataset.price);

                if (sortBy === 'name-asc') {
                    return nameA.localeCompare(nameB);
                } else if (sortBy === 'name-desc') {
                    return nameB.localeCompare(nameA);
                } else if (sortBy === 'price-asc') {
                    return priceA - priceB;
                } else if (sortBy === 'price-desc') {
                    return priceB - priceA;
                }
                return 0;
            });
        }

        productGridContainer.innerHTML = '';

        filteredProducts.forEach(card => {
            productGridContainer.appendChild(card);
            card.style.display = 'block';
        });

        initialProductOrder.forEach(card => {
            if (!filteredProducts.includes(card)) {
                card.style.display = 'none';
            }
        });
    }

    if (priceRangeInput && priceValueSpan) {
        priceRangeInput.addEventListener('input', function() {
            priceValueSpan.textContent = this.value;
            applyFiltersAndSort();
        });
    }

    const filterElements = [brandSelect, productTypeSelect, conditionAllCheckbox, conditionNewCheckbox, conditionSecondHandCheckbox, searchProductsInput];
    filterElements.forEach(element => {
        if (element) {
            element.addEventListener('change', applyFiltersAndSort);
            element.addEventListener('input', applyFiltersAndSort);
        }
    });

    if (sortProductsSelect) {
        sortProductsSelect.addEventListener('change', applyFiltersAndSort);
    }

    if (document.querySelector('.products-page')) {
        applyFiltersAndSort();
    }

    window.trackRepair = function() {
        const repairTrackIdInput = document.getElementById('repair-track-id');
        const repairStatusDiv = document.getElementById('repair-status');
        if (repairTrackIdInput && repairStatusDiv) {
            const repairId = repairTrackIdInput.value;
            repairStatusDiv.textContent = `Tracking ID: ${repairId} - Status: Checking database...`;

            setTimeout(() => {
                const statuses = {
                    '12345': 'In Progress',
                    '67890': 'Ready for Pickup',
                    '54321': 'Pending Parts'
                };
                const status = statuses[repairId] || 'Not Found';
                repairStatusDiv.textContent = `Tracking ID: ${repairId} - Status: ${status}`;
            }, 1000);
        }
    }

    const internetCafeForm = document.getElementById('internet-cafe-booking-form');
    const internetCafeConfirmation = document.getElementById('internet-cafe-booking-confirmation');

    if (internetCafeForm) {
        internetCafeForm.addEventListener('submit', (event) => {
            event.preventDefault();

            if (document.getElementById('cafe-booking-date')) document.getElementById('cafe-booking-date').textContent = document.getElementById('date').value;
            if (document.getElementById('cafe-booking-time')) document.getElementById('cafe-booking-time').textContent = document.getElementById('time').value;
            if (document.getElementById('cafe-booking-duration')) document.getElementById('cafe-booking-duration').textContent = document.getElementById('duration').value;
            if (document.getElementById('cafe-booking-users')) document.getElementById('cafe-booking-users').textContent = document.getElementById('num-users').value;

            if (internetCafeConfirmation) internetCafeConfirmation.style.display = 'block';
            internetCafeForm.style.display = 'none';
        });
    }

    const phoneRepairForm = document.getElementById('phone-repair-booking-form');
    const phoneRepairConfirmation = document.getElementById('phone-repair-booking-confirmation');

    if (phoneRepairForm) {
        phoneRepairForm.addEventListener('submit', (event) => {
            event.preventDefault();

            if (document.getElementById('repair-booking-name')) document.getElementById('repair-booking-name').textContent = document.getElementById('name').value;
            if (document.getElementById('repair-booking-phone')) document.getElementById('repair-booking-phone').textContent = document.getElementById('phone').value;
            if (document.getElementById('repair-booking-email')) document.getElementById('repair-booking-email').textContent = document.getElementById('email').value;
            if (document.getElementById('repair-booking-device')) document.getElementById('repair-booking-device').textContent = document.getElementById('device').value;
            if (document.getElementById('repair-booking-problem')) document.getElementById('repair-booking-problem').textContent = document.getElementById('problem').value;

            if (phoneRepairConfirmation) phoneRepairConfirmation.style.display = 'block';
            phoneRepairForm.style.display = 'none';
        });
    }

    const uploadDocumentsBtn = document.getElementById('upload-documents-btn');
    const printUploadModal = document.getElementById('print-upload-modal');
    const closePrintModalBtn = document.getElementById('close-print-modal');
    const printUploadForm = document.getElementById('print-upload-form');

    if (uploadDocumentsBtn && printUploadModal) {
        uploadDocumentsBtn.addEventListener('click', () => {
            printUploadModal.classList.add('show');
        });
    }

    if (closePrintModalBtn && printUploadModal) {
        closePrintModalBtn.addEventListener('click', () => {
            printUploadModal.classList.remove('show');
        });
    }

    if (printUploadModal) {
        printUploadModal.addEventListener('click', (event) => {
            if (event.target === printUploadModal) {
                printUploadModal.classList.remove('show');
            }
        });
    }

    if (printUploadForm) {
        printUploadForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const fileInput = document.getElementById('print-files-input');
            const files = fileInput ? fileInput.files : [];

            if (files.length === 0) {
                showMessageBox('Error', 'Please select at least one file to upload.', true);
                return;
            }

            console.log('Uploading files for printing:', files);

            showMessageBox('Success', `Successfully submitted ${files.length} file(s) for printing!`, false);
            
            if (printUploadModal) {
                printUploadModal.classList.remove('show');
            }
            if (fileInput) {
                fileInput.value = '';
            }
        });
    }

    function showMessageBox(title, message, isError = true) {
        const overlay = document.getElementById('message-box-overlay');
        const titleElement = document.getElementById('message-box-title');
        const textElement = document.getElementById('message-box-text');
        const okButton = document.getElementById('message-box-ok-button');

        if (!overlay || !titleElement || !textElement || !okButton) {
            console.error("Message box HTML elements not found. Ensure IDs are correct and HTML is loaded.");
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

    const userProfileDiv = document.getElementById('user-profile');
    const loginPromptDiv = document.getElementById('login-prompt');
    const logoutButton = document.getElementById('logout-button');

    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

    if (isLoggedIn) {
        const userData = {
            name: localStorage.getItem('userName') || 'John Doe',
            email: localStorage.getItem('userEmail') || 'john.doe@example.com',
            phone: '123-456-7890',
        };

        if (document.getElementById('profile-name')) document.getElementById('profile-name').textContent = userData.name;
        if (document.getElementById('profile-email')) document.getElementById('profile-email').textContent = userData.email;
        if (document.getElementById('profile-phone')) document.getElementById('profile-phone').textContent = userData.phone;

        if (userProfileDiv) userProfileDiv.style.display = 'block';
        if (loginPromptDiv) loginPromptDiv.style.display = 'none';

        if (logoutButton) {
            logoutButton.addEventListener('click', () => {
                localStorage.removeItem('isLoggedIn');
                localStorage.removeItem('userName');
                localStorage.removeItem('userEmail');
                window.location.href = 'login.html';
            });
        }
    } else {
        if (userProfileDiv) userProfileDiv.style.display = 'none';
        if (loginPromptDiv) loginPromptDiv.style.display = 'block';
    }

    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', (event) => {
            event.preventDefault();

            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            if (email === 'test@example.com' && password === 'password') {
                localStorage.setItem('isLoggedIn', 'true');
                localStorage.setItem('userName', 'Test User');
                localStorage.setItem('userEmail', email);
                window.location.href = 'profile.html';
            } else {
                showMessageBox('Error', 'Invalid credentials. Please try again.', true);
            }
        });
    }

    const registerForm = document.getElementById('register-form');
    const sendOtpButton = document.getElementById('send-otp-button');

    if (sendOtpButton) {
        sendOtpButton.addEventListener('click', async () => {
            const email = document.getElementById('register-email').value.trim();
            if (!email) {
                showMessageBox('Error', 'Please enter your email to send OTP.', true);
                return;
            }

            sendOtpButton.disabled = true;
            sendOtpButton.textContent = 'Sending OTP...';

            try {
                const response = await fetch('/api/send-otp', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email })
                });

                const data = await new Promise(resolve => setTimeout(() => {
                    if (Math.random() > 0.2) {
                        resolve({ success: true, message: 'OTP sent successfully to your email.' });
                    } else {
                        resolve({ success: false, message: 'Failed to send OTP. Please try again.' });
                    }
                }, 1500));

                if (data.success) {
                    showMessageBox('Success', data.message, false);
                } else {
                    showMessageBox('Error', data.message, true);
                }
            } catch (error) {
                console.error('Error sending OTP:', error);
                showMessageBox('Error', 'Network error or unable to send OTP. Please try again.', true);
            } finally {
                sendOtpButton.disabled = false;
                sendOtpButton.textContent = 'Send OTP';
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const name = document.getElementById('register-name').value.trim();
            const surname = document.getElementById('register-surname').value.trim();
            const email = document.getElementById('register-email').value.trim();
            const otp = document.getElementById('register-otp').value.trim();
            const password = document.getElementById('register-password').value;
            const confirmPassword = document.getElementById('register-confirm-password').value;
            const registerButton = registerForm.querySelector('button[type="submit"]');

            if (!name || !surname || !email || !otp || !password || !confirmPassword) {
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

            registerButton.disabled = true;
            registerButton.textContent = 'Registering...';

            try {
                const response = await fetch('/api/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, surname, email, otp, password })
                });

                const data = await new Promise(resolve => setTimeout(() => {
                    if (Math.random() > 0.3) {
                        resolve({ success: true, message: 'Registration successful!' });
                    } else {
                        resolve({ success: false, message: 'OTP verification failed or user already exists.' });
                    }
                }, 2000));

                if (data.success) {
                    localStorage.setItem('isLoggedIn', 'true');
                    localStorage.setItem('userName', name + ' ' + surname);
                    localStorage.setItem('userEmail', email);

                    showMessageBox('Success', data.message + ' Redirecting to profile...', false);
                    setTimeout(() => {
                        window.location.href = 'profile.html';
                    }, 1500);
                } else {
                    showMessageBox('Error', data.message, true);
                }
            } catch (error) {
                console.error('Error during registration:', error);
                showMessageBox('Error', 'Network error or registration failed. Please try again.', true);
            } finally {
                registerButton.disabled = false;
                registerButton.textContent = 'Register';
            }
        });
    }

    const forgotPasswordForm = document.getElementById('forgot-password-form');
    const sendOtpForgotPasswordButton = document.getElementById('send-otp-forgot-password');

    if (sendOtpForgotPasswordButton) {
        sendOtpForgotPasswordButton.addEventListener('click', async () => {
            const emailInput = document.getElementById('forgot-password-email');
            const email = emailInput ? emailInput.value.trim() : '';

            if (!email) {
                showMessageBox('Error', 'Please enter your email to send OTP.', true);
                return;
            }

            sendOtpForgotPasswordButton.disabled = true;
            sendOtpForgotPasswordButton.textContent = 'Sending OTP...';

            try {
                const response = await fetch('/api/send-otp-forgot-password', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email })
                });

                const data = await new Promise(resolve => setTimeout(() => {
                    if (Math.random() > 0.2) {
                        resolve({ success: true, message: 'Password reset OTP sent to your email.' });
                    } else {
                        resolve({ success: false, message: 'Failed to send OTP. Please check your email and try again.' });
                    }
                }, 1500));

                if (data.success) {
                    showMessageBox('Success', data.message, false);
                } else {
                    showMessageBox('Error', data.message, true);
                }
            } catch (error) {
                console.error('Error sending forgot password OTP:', error);
                showMessageBox('Error', 'Network error or unable to send OTP. Please try again.', true);
            } finally {
                sendOtpForgotPasswordButton.disabled = false;
                sendOtpForgotPasswordButton.textContent = 'Send OTP';
            }
        });
    }

    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const name = document.getElementById('forgot-password-name') ? document.getElementById('forgot-password-name').value.trim() : '';
            const surname = document.getElementById('forgot-password-surname') ? document.getElementById('forgot-password-surname').value.trim() : '';
            const email = document.getElementById('forgot-password-email') ? document.getElementById('forgot-password-email').value.trim() : '';
            const otp = document.getElementById('forgot-password-otp') ? document.getElementById('forgot-password-otp').value.trim() : '';
            const newPassword = document.getElementById('forgot-password-new-password') ? document.getElementById('forgot-password-new-password').value : '';
            const confirmNewPassword = document.getElementById('forgot-password-confirm-new-password') ? document.getElementById('forgot-password-confirm-new-password').value : '';
            const changePasswordButton = forgotPasswordForm.querySelector('button[type="submit"]');

            if (!name || !surname || !email || !otp || !newPassword || !confirmNewPassword) {
                showMessageBox('Error', 'Please fill in all fields.', true);
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

            changePasswordButton.disabled = true;
            changePasswordButton.textContent = 'Changing Password...';

            try {
                const response = await fetch('/api/reset-password', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, otp, newPassword })
                });

                const data = await new Promise(resolve => setTimeout(() => {
                    if (Math.random() > 0.3) {
                        resolve({ success: true, message: 'Password changed successfully!' });
                    } else {
                        resolve({ success: false, message: 'OTP verification failed or password change failed.' });
                    }
                }, 2000));

                if (data.success) {
                    showMessageBox('Success', data.message + ' Redirecting to login...', false);
                    setTimeout(() => {
                        window.location.href = 'login.html';
                    }, 1500);
                } else {
                    showMessageBox('Error', data.message, true);
                }
            } catch (error) {
                console.error('Error changing password:', error);
                showMessageBox('Error', 'Network error or password change failed. Please try again.', true);
            } finally {
                changePasswordButton.disabled = false;
                changePasswordButton.textContent = 'Change Password';
            }
        });
    }
});
