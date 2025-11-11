// --- FIREBASE IMPORTS (All required modules for the entire site) ---
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { 
    getAuth, 
    onAuthStateChanged, 
    signOut,
    signInAnonymously,
    signInWithCustomToken,
    createUserWithEmailAndPassword,
    signInWithEmailAndPassword,
    sendPasswordResetEmail
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { 
    getFirestore, 
    doc, 
    getDoc, 
    setDoc, 
    updateDoc, 
    addDoc,
    collection, 
    getDocs, 
    query, 
    where,
    onSnapshot,
    serverTimestamp,
    deleteDoc,
    limit
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { 
    getStorage, 
    ref, 
    uploadBytesResumable, 
    getDownloadURL 
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-storage.js";


// --- FIREBASE INITIALIZATION ---
const firebaseConfig = JSON.parse(typeof __firebase_config !== 'undefined' ? __firebase_config : '{}');
const __initial_auth_token = typeof __initial_auth_token !== 'undefined' ? __initial_auth_token : null;

let auth, db, storage;
let currentUserId = null;
let cartUnsubscribe = null;
let cartItemsCache = {};

try {
    const app = initializeApp(firebaseConfig);
    auth = getAuth(app);
    db = getFirestore(app);
    storage = getStorage(app);
    console.log("Firebase services initialized.");
} catch (e) {
    console.error("Firebase Init Failed:", e);
    showMessage("Error", "Could not connect to services. Please refresh.", true);
}


// --- GLOBAL UTILITIES (Modal Box) ---

window.showMessage = function(title, message, isError = false) {
    const overlay = document.getElementById('message-box-overlay');
    const titleEl = document.getElementById('message-box-title');
    const textEl = document.getElementById('message-box-text');
    
    if (!overlay || !titleEl || !textEl) {
        console.error(`Message Box UI Missing: ${title} - ${message}`);
        alert(`${title}: ${message}`);
        return;
    }

    titleEl.textContent = title;
    textEl.textContent = message;
    titleEl.className = isError ? 'error-title' : 'success-title';
    
    overlay.classList.add('show');
}
document.addEventListener('DOMContentLoaded', () => {
    const okButton = document.getElementById('message-box-ok-button');
    const overlay = document.getElementById('message-box-overlay');
    if (okButton && overlay) {
        okButton.addEventListener('click', () => overlay.classList.remove('show'));
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) overlay.classList.remove('show');
        });
    }
});


// --- DARK MODE LOGIC ---

function initializeTheme() {
    const themeToggle = document.getElementById('theme-toggle-btn');
    const html = document.documentElement;

    const setMode = (isDark) => {
        html.classList.toggle('dark', isDark);
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
        if (typeof lucide !== 'undefined') lucide.createIcons();
    };

    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            setMode(!html.classList.contains('dark'));
        });
    }

    const storedTheme = localStorage.getItem('theme');
    if (storedTheme) {
        setMode(storedTheme === 'dark');
    } else {
        setMode(window.matchMedia('(prefers-color-scheme: dark)').matches);
    }
}


// --- AUTHENTICATION & HEADER STATE ---

function initializeAuthListener() {
    const navLogin = document.getElementById('login-link');
    const navRegister = document.getElementById('register-link');
    const navLogout = document.getElementById('logout-link');
    const navProfile = document.getElementById('profile-link');
    const navAdmin = document.getElementById('admin-link');
    
    (async () => {
        try {
            if (__initial_auth_token) {
                await signInWithCustomToken(auth, __initial_auth_token);
            } else {
                await signInAnonymously(auth);
            }
        } catch (e) {
            console.error("Auth token error:", e);
        }
    })();

    onAuthStateChanged(auth, async (user) => {
        if (user && !user.isAnonymous) {
            currentUserId = user.uid;
            
            if (navLogin) navLogin.style.display = 'none';
            if (navRegister) navRegister.style.display = 'none';
            if (navLogout) navLogout.style.display = 'block';
            if (navProfile) navProfile.style.display = 'block';

            try {
                const userDoc = await getDoc(doc(db, 'users', user.uid));
                if (userDoc.exists() && userDoc.data().role === 'admin') {
                    if (navAdmin) navAdmin.style.display = 'block';
                    if (window.location.pathname.endsWith('index.html') && !window.location.pathname.endsWith('admin.html')) {
                        // Prevent redirecting multiple times
                        const path = window.location.pathname;
                        if (path === '/' || path.endsWith('index.html')) {
                             window.location.href = 'admin.html'; 
                        }
                    }
                } else {
                    if (navAdmin) navAdmin.style.display = 'none';
                }
            } catch (e) {
                console.error("Could not check admin status:", e);
            }
            
            startCartListener(user.uid);
        } else {
            currentUserId = null;
            if (navLogin) navLogin.style.display = 'block';
            if (navRegister) navRegister.style.display = 'block';
            if (navLogout) navLogout.style.display = 'none';
            if (navProfile) navProfile.style.display = 'none';
            if (navAdmin) navAdmin.style.display = 'none';
            
            if (cartUnsubscribe) cartUnsubscribe();
            updateCartCount(0);
        }
    });

    if (navLogout) {
        navLogout.addEventListener('click', async (e) => {
            e.preventDefault();
            await signOut(auth);
            await signInAnonymously(auth);
            showMessage("Logged Out", "You have been logged out.", false);
            window.location.href = 'index.html';
        });
    }
}


// --- CART & PRODUCT LOGIC ---

function getCartRef() {
    if (!currentUserId) return null;
    return collection(db, 'users', currentUserId, 'cart');
}

function startCartListener(uid) {
    if (cartUnsubscribe) cartUnsubscribe(); 

    const cartRef = getCartRef();
    if (!cartRef) return;

    cartUnsubscribe = onSnapshot(cartRef, (snapshot) => {
        let count = 0;
        cartItemsCache = {};

        snapshot.forEach(doc => {
            const item = doc.data();
            count += item.quantity || 1;
            cartItemsCache[doc.id] = item;
        });
        
        updateCartCount(count);
        
        // Trigger cart page update if it's the current page
        if (window.location.pathname.endsWith('cart.html') && window.displayCartItems) {
            window.displayCartItems();
        }
    });
}

function updateCartCount(count) {
    const cartCountElement = document.getElementById('cart-count');
    if (cartCountElement) {
        cartCountElement.textContent = count;
        cartCountElement.classList.toggle('show', count > 0);
    }
}

window.addToCart = async function(productId, productData) {
    if (!currentUserId) {
        showMessage("Notice", "Please log in to add items to your cart.", false);
        return;
    }
    const cartRef = getCartRef();
    if (!cartRef) return;
    
    const itemRef = doc(cartRef, productId);
    
    try {
        const itemDoc = await getDoc(itemRef);
        
        if (itemDoc.exists()) {
            await updateDoc(itemRef, {
                quantity: (itemDoc.data().quantity || 1) + 1
            });
        } else {
            await setDoc(itemRef, {
                productId: productId,
                name: productData.name,
                price: productData.price,
                imageRes: productData.imageRes || 'https://placehold.co/400x300/E0E0E0/424242?text=NO+IMG',
                quantity: 1
            });
        }
        showMessage("Success", `${productData.name} added to cart!`, false);
    } catch (err) {
        showMessage("Error", "Could not add to cart: " + err.message, true);
    }
}


document.addEventListener('DOMContentLoaded', () => {
    initializeTheme();
    initializeAuthListener();
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
});


window.auth = auth;
window.db = db;
window.storage = storage;
window.currentUserId = currentUserId;
window.getDoc = getDoc;
window.collection = collection;
window.getDocs = getDocs;
window.query = query;
window.where = where;
window.updateDoc = updateDoc;
window.deleteDoc = deleteDoc;
window.setDoc = setDoc;
window.serverTimestamp = serverTimestamp;
window.createUserWithEmailAndPassword = createUserWithEmailAndPassword;
window.signInWithEmailAndPassword = signInWithEmailAndPassword;
window.sendPasswordResetEmail = sendPasswordResetEmail;
window.getCartRef = getCartRef;
window.getCartItemsCache = () => cartItemsCache; 
window.getStorage = () => storage;