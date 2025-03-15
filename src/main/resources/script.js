// Toggle between Login and Register forms
function toggleForm(isLogin) {
  const loginForm = document.getElementById("login");
  const registerForm = document.getElementById("register");
  const btnToggle = document.getElementById("btn");
  const otherOptions = document.getElementById("other");
  const loginButton = document.getElementById("log");
  const registerButton = document.getElementById("reg");

  loginForm.style.left = isLogin ? "50px" : "-400px";
  registerForm.style.left = isLogin ? "450px" : "50px";
  btnToggle.style.left = isLogin ? "0px" : "110px";
  otherOptions.style.visibility = isLogin ? "visible" : "hidden";
  loginButton.style.color = isLogin ? "#fff" : "#000";
  registerButton.style.color = isLogin ? "#000" : "#fff";
}

// Enable Register button based on Terms & Conditions checkbox
function toggleRegisterButton() {
  const isAgreed = document.getElementById("chkAgree").checked;
  const registerButton = document.getElementById("btnSubmit");
  registerButton.disabled = !isAgreed;
  registerButton.style.background = isAgreed ? "linear-gradient(to right, #FA4B37, #DF2771)" : "lightgray";
}

// Validate password match for Registration
function validateRegistration() {
  const password = document.getElementById("register-password").value;
  const confirmPassword = document.getElementById("register-confirm-password").value;

  if (password !== confirmPassword) {
    alert("Passwords do not match!");
    return false;
  }
  return true;
}

// Redirect to Google for OAuth (for demo purposes only; replace with a secure backend route)
function redirectToGoogle() {
  window.location.assign("https://accounts.google.com/signin/v2/identifier", "_blank");
}

// Scroll-based animation (for adding animations when elements appear in the viewport)
function scrollAppear() {
  const introText = document.querySelector('.side-text');
  const sideImage = document.querySelector('.sideImage');
  const screenPosition = window.innerHeight / 1.2;

  if (introText && introText.getBoundingClientRect().top < screenPosition) {
    introText.classList.add('side-text-appear');
  }
  if (sideImage && sideImage.getBoundingClientRect().top < screenPosition) {
    sideImage.classList.add('sideImage-appear');
  }
}

window.addEventListener('scroll', scrollAppear);
