document.addEventListener('DOMContentLoaded', function() {
    const loggedInUserEmail = localStorage.getItem('loggedInUserEmail');
    const userCartKey = loggedInUserEmail ? `cart_${loggedInUserEmail}` : 'cart_guest';

    let cart = JSON.parse(localStorage.getItem(userCartKey)) || [];
    const cartItemsContainer = document.getElementById('cart-items');
    const cartTotalElement = document.getElementById('cart-total');
    const cartCountElement = document.getElementById('cart-count');

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

    function updateCartCount() {
        if (cartCountElement) {
            const totalItems = cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
            cartCountElement.textContent = totalItems;
            if (totalItems === 0) {
                cartCountElement.style.display = 'none';
            } else {
                cartCountElement.style.display = 'inline-block';
            }
        }
    }

    window.addToCart = function(product) {
        const existingProductIndex = cart.findIndex(item => item.name === product.name);

        if (existingProductIndex > -1) {
            cart[existingProductIndex].quantity = (cart[existingProductIndex].quantity || 1) + 1;
        } else {
            cart.push({ ...product, quantity: 1 });
        }
        localStorage.setItem(userCartKey, JSON.stringify(cart));
        showMessageBox('Success', `${product.name} added to cart!`, false);
        updateCartCount();
    };

    window.removeFromCart = function(productName) {
        cart = cart.filter(item => item.name !== productName);
        localStorage.setItem(userCartKey, JSON.stringify(cart));
        showMessageBox('Info', `${productName} removed from cart.`, false);
        displayCart();
        updateCartCount();
    };

    function displayCart() {
        if (!cartItemsContainer || !cartTotalElement) {
            return;
        }

        cartItemsContainer.innerHTML = '';
        let total = 0;

        if (cart.length === 0) {
            cartItemsContainer.innerHTML = '<li class="text-center text-gray-500 py-4">Your cart is empty.</li>';
        } else {
            cart.forEach(item => {
                const listItem = document.createElement('li');
                listItem.className = 'cart-item flex justify-between items-center py-2 border-b border-gray-200';
                
                const itemPrice = parseFloat(item.price) * (item.quantity || 1);
                total += itemPrice;

                listItem.innerHTML = `
                    <span class="text-lg font-medium">${item.name} (x${item.quantity || 1})</span>
                    <span class="text-lg font-semibold">R${itemPrice.toFixed(2)}</span>
                    <button onclick="removeFromCart('${item.name}')" class="remove-item-btn bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-md text-sm">Remove</button>
                `;
                cartItemsContainer.appendChild(listItem);
            });
        }
        cartTotalElement.textContent = `R${total.toFixed(2)}`;
    }

    window.checkout = function() {
        if (cart.length === 0) {
            showMessageBox('Error', 'Your cart is empty. Please add items before checking out.', true);
            return;
        }

        const totalAmount = parseFloat(cartTotalElement.textContent.replace('R', ''));
        
        console.log('Initiating checkout for total:', totalAmount);
        console.log('Cart contents:', cart);

        showMessageBox('Info', `Redirecting to payment for R${totalAmount.toFixed(2)}... (Simulation)`, false);

        setTimeout(() => {
            const simulatedPaymentGatewayUrl = `https://example.com/payment-gateway?amount=${totalAmount.toFixed(2)}&currency=ZAR&orderId=${Date.now()}`;
            window.location.href = simulatedPaymentGatewayUrl;
        }, 1500); 

        localStorage.removeItem(userCartKey);
        cart = [];
        updateCartCount();
        displayCart();
    };

    window.clearCurrentCart = function() {
        console.log(`Clearing cart for key: ${userCartKey}`);
        localStorage.removeItem(userCartKey);
        cart = [];
        updateCartCount();
        if (document.querySelector('.cart-page')) {
            displayCart();
        }
    };

    updateCartCount();
    if (document.querySelector('.cart-page')) {
        displayCart();
    }
});
