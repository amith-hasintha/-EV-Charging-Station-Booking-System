// StationDashboard.jsx
import { Outlet, Link, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Container, Navbar } from "react-bootstrap";

export default function StationDashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const [stationId, setStationId] = useState(null);
  const [stationName, setStationName] = useState("");

  useEffect(() => {
    const token = localStorage.getItem("token");
    const sid = localStorage.getItem("stationId");
    const sname = localStorage.getItem("stationName");
    if (!token) navigate("/");
    if (sid) setStationId(sid);
    if (sname) setStationName(sname);
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("stationId");
    localStorage.removeItem("stationName");
    localStorage.removeItem("role");
    navigate("/");
  };

  return (
    <div style={{
      minHeight: "100vh",
      background: "linear-gradient(135deg, #1B263B 0%, #121212 100%)",
      display: "flex"
    }}>
      {/* Sidebar */}
      <aside style={{
        width: "280px",
        background: "#F9FAFB",
        borderRight: "1px solid rgba(255, 255, 255, 0.1)",
        padding: "24px",
        display: "flex",
        flexDirection: "column",
        boxShadow: "0 0 20px rgba(0, 0, 0, 0.1)"
      }}>
        {/* Header */}
        <div style={{ 
          display: "flex", 
          alignItems: "center", 
          gap: "12px", 
          marginBottom: "32px",
          paddingBottom: "16px",
          borderBottom: "1px solid #E5E7EB"
        }}>
          <div style={{
            fontSize: "2rem",
            background: "linear-gradient(135deg, #00C853 0%, #00B4D8 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
            backgroundClip: "text"
          }}>
            ⚡
          </div>
          <div>
            <h5 style={{ 
              margin: 0, 
              color: "#121212",
              fontWeight: "700",
              fontSize: "1.25rem"
            }}>
              Station Panel
            </h5>
            <p style={{ 
              margin: 0, 
              color: "#6C757D",
              fontSize: "0.85rem"
            }}>
              {stationName || "Operator Dashboard"}
            </p>
          </div>
        </div>

        {/* Navigation */}
        <nav style={{ flex: 1 }}>
          <CustomNavLink 
            to="bookings" 
            label="Bookings" 
            icon="📅" 
            isActive={location.pathname.includes('bookings')}
          />
        </nav>

        {/* Footer */}
        <div style={{ 
          marginTop: "auto",
          paddingTop: "16px",
          borderTop: "1px solid #E5E7EB"
        }}>
          <div style={{ 
            textAlign: "center", 
            marginBottom: "16px",
            color: "#6C757D",
            fontSize: "0.85rem"
          }}>
            Station ID: <strong>{stationId || "—"}</strong>
          </div>
          <button
            onClick={handleLogout}
            style={{
              width: "100%",
              background: "linear-gradient(135deg, #DC3545 0%, #C82333 100%)",
              color: "white",
              border: "none",
              padding: "12px",
              borderRadius: "12px",
              fontWeight: "600",
              cursor: "pointer",
              transition: "all 0.3s ease"
            }}
            onMouseEnter={(e) => {
              e.target.style.transform = "translateY(-2px)";
              e.target.style.boxShadow = "0 5px 15px rgba(220, 53, 69, 0.3)";
            }}
            onMouseLeave={(e) => {
              e.target.style.transform = "translateY(0)";
              e.target.style.boxShadow = "none";
            }}
          >
            🚪 Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main style={{ 
        flex: 1,
        display: "flex",
        flexDirection: "column"
      }}>
        <Navbar style={{
          background: "#F9FAFB",
          borderBottom: "1px solid #E5E7EB",
          padding: "16px 24px",
          boxShadow: "0 2px 10px rgba(0, 0, 0, 0.05)"
        }}>
          <Container fluid>
            <Navbar.Brand style={{ 
              fontWeight: "700",
              color: "#121212",
              fontSize: "1.5rem",
              display: "flex",
              alignItems: "center",
              gap: "12px"
            }}>
              <div style={{
                fontSize: "1.8rem",
                background: "linear-gradient(135deg, #00C853 0%, #00B4D8 100%)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
                backgroundClip: "text"
              }}>
                🏢
              </div>
              Operator Dashboard
            </Navbar.Brand>
          </Container>
        </Navbar>

        <Container fluid style={{ 
          padding: "24px",
          flex: 1,
          background: "linear-gradient(135deg, #f8f3f3ff 0%, #F9FAFB 100%)"
        }}>
          <Outlet context={{ stationId }} />
        </Container>
      </main>
    </div>
  );
}

/* Custom Nav Link Component */
function CustomNavLink({ to, label, icon, isActive }) {
  const baseStyle = {
    display: "flex",
    alignItems: "center",
    gap: "12px",
    padding: "12px 16px",
    borderRadius: "12px",
    marginBottom: "8px",
    textDecoration: "none",
    fontWeight: "500",
    transition: "all 0.3s ease",
    border: "none",
    color: "#121212"
  };

  const activeStyle = {
    ...baseStyle,
    background: "linear-gradient(135deg, #00C853 0%, #00B4D8 100%)",
    color: "white",
    transform: "translateX(8px)"
  };

  const hoverStyle = {
    background: "linear-gradient(135deg, #00C85320 0%, #00B4D820 100%)",
    color: "#121212",
    transform: "translateX(8px)"
  };

  return (
    <Link
      to={to}
      style={isActive ? activeStyle : baseStyle}
      onMouseEnter={(e) => {
        if (!isActive) {
          e.target.style.background = hoverStyle.background;
          e.target.style.color = hoverStyle.color;
          e.target.style.transform = hoverStyle.transform;
        }
      }}
      onMouseLeave={(e) => {
        if (!isActive) {
          e.target.style.background = "";
          e.target.style.color = baseStyle.color;
          e.target.style.transform = "";
        }
      }}
    >
      <span style={{ fontSize: "1.2rem" }}>{icon}</span>
      <span>{label}</span>
    </Link>
  );
}