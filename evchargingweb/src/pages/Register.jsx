//Register.jsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css"; // Reusing the same CSS file

export default function Register() {
  // --- State Hooks ---
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState(1); // default = StationOperator
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [nic, setNic] = useState("");
  const [stations, setStations] = useState([]);
  const [stationId, setStationId] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  // --- Handle registration form submission ---
  const handleRegister = async (e) => {
    e.preventDefault(); // Prevent default form submission
    setError("");       // Clear previous error
    setSuccess("");     // Clear previous success message
    setIsLoading(true); // Set loading state

    try {
      // --- Send registration request to API ---
      const response = await fetch("http://localhost:5082/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nic, firstName, lastName, email, password, role, stationId }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Registration failed");
      }

      // --- Set success message and redirect ---
      setSuccess("✅ Registration successful! Redirecting to login...");

      setTimeout(() => {
        navigate("/"); // Redirect to login
      }, 1500);
    } catch (err) {
      // --- Handle API error ---
      setError(err.message);
    } finally {
      setIsLoading(false); // Reset loading state
    }
  };

  // --- Fetch available charging stations on component mount ---
  useEffect(() => {
    const fetchStations = async () => {
      try {
        const res = await fetch("http://localhost:5082/api/chargingstations", {
          method: "GET",
          headers: { "Content-Type": "application/json" },
        });

        if (!res.ok) throw new Error("Failed to fetch stations");

        const data = await res.json();
        setStations(data); // Set fetched stations in state
        console.log("Stations fetched:", data);
      } catch (err) {
        console.error("Failed to fetch stations", err);
      }
    };

    fetchStations();
  }, []);


  return (
    <div className="login-container">
      <div className="login-card">
        {/* Header Section */}
        <div className="login-header">
          <div className="logo">
            <div className="logo-icon">⚡</div>
            <h1>EV Charge</h1>
          </div>
          <p className="tagline">Join Our Community</p>
        </div>

        {/* Form Section */}
        <form onSubmit={handleRegister} className="login-form">
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}
          
          <div className="form-group">
            <label htmlFor="firstName" className="form-label">First Name</label>
            <input
              id="firstName"
              type="text"
              className="form-input"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="Enter your first name"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="lastName" className="form-label">Last Name</label>
            <input
              id="lastName"
              type="text"
              className="form-input"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Enter your last name"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="nic" className="form-label">NIC</label>
            <input
              id="nic"
              type="text"
              className="form-input"
              value={nic}
              onChange={(e) => setNic(e.target.value)}
              placeholder="Enter your NIC"
              required
            />
          </div>

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

          <div className="form-group">
            <label htmlFor="role" className="form-label">Role</label>
            <select
              id="role"
              className="form-input"
              value={role}
              onChange={(e) => setRole(Number(e.target.value))}
              required
            >
              <option value={0}>Backoffice</option>
              <option value={1}>Station Operator</option>
            </select>
          </div>
            {role === 1 && (  // 1 = StationOperator
            <div className="form-group">
                <label htmlFor="stationId" className="form-label">Station</label>
                <select
                id="stationId"
                className="form-input"
                value={stationId}
                onChange={(e) => setStationId(e.target.value)}
                required
                >
                <option value="">Select a station</option>
                {stations.map((s) => (
                    <option key={s.id} value={s.id}>
                    {s.name} ({s.location})
                    </option>
                ))}
                </select>
            </div>
            )}

          <button 
            className={`login-button ${isLoading ? 'loading' : ''}`} 
            type="submit"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <div className="spinner"></div>
                Creating Account...
              </>
            ) : (
              'Create Account'
            )}
          </button>
        </form>

        {/* Footer Links */}
        <div className="login-footer">
          <p className="signup-text">
            Already have an account? <a href="/" className="signup-link">Login here</a>
          </p>
        </div>
      </div>
    </div>
  );
}