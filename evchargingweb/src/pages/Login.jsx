//Login.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/Login.css";

export default function Login() {
  // --- State Hooks ---
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();

  // --- Handle form submission for login ---
  const handleLogin = async (e) => {
    e.preventDefault(); // Prevent default form submission
    setError("");       // Clear previous errors
    setIsLoading(true); // Set loading state

    try {
      // --- Send login request to API ---
      const response = await axios.post("http://localhost:5082/api/auth/login", {
        email,
        password,
      });

      const { token, user } = response.data;

      // --- Store JWT and user info in localStorage ---
      localStorage.setItem("token", token);
      localStorage.setItem("role", user.role);
      if (user.stationId) {
        localStorage.setItem("stationId", user.stationId);
      }

      // --- Redirect user based on role ---
      if (user.role === 0) {
        navigate("/dashboard/users"); // Backoffice starts at Users page
      } else if (user.role === 1) {
        navigate("/station-dashboard/bookings"); // Operator dashboard
      } else {
        navigate("/dashboard/evowner"); // EV Owner dashboard
      }
    } catch (err) {
      // --- Handle API error response ---
      setError(err.response?.data?.message || "Login failed");
    } finally {
      setIsLoading(false); // Reset loading state
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        {/* Header Section */}
        <div className="login-header">
          <div className="logo">
            <div className="logo-icon">⚡</div>
            <h1>EV Charge</h1>
          </div>
          <p className="tagline">Power Your Journey</p>
        </div>

        {/* Form Section */}
        <form onSubmit={handleLogin} className="login-form">
          {error && <div className="alert alert-error">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="email" className="form-label">Email Address</label>
            <input
              id="email"
              type="email"
              className="form-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              id="password"
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          <button 
            className={`login-button ${isLoading ? 'loading' : ''}`} 
            type="submit"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <div className="spinner"></div>
                Signing In...
              </>
            ) : (
              'Log In to Your Account'
            )}
          </button>
        </form>

        {/* Footer Links */}
        <div className="login-footer">
          <p className="signup-text">
            New to EV Charge? <a href="/register" className="signup-link">Create Account</a>
          </p>
        </div>
      </div>
    </div>
  );
}
