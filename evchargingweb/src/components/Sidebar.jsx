//Sidebar.jsx
import { Nav, Button } from "react-bootstrap";
import { Link, useLocation, useNavigate } from "react-router-dom";

export default function Sidebar() {
  // React Router hooks for current path and navigation
  const location = useLocation();
  const navigate = useNavigate();

  // Logout handler: clears authentication data and navigates to home
  const handleLogout = () => {
    localStorage.removeItem("token"); // Clear token
    sessionStorage.clear(); // Clear session storage
    navigate("/"); // Redirect to home/login page
  };

  // Base style for navigation links
  const navLinkStyle = {
    color: "#121212",
    padding: "12px 16px",
    borderRadius: "12px",
    marginBottom: "8px",
    fontWeight: "500",
    transition: "all 0.3s ease",
    border: "none",
    textDecoration: "none",
    display: "block",
  };

  // Style for active/current navigation link
  const activeNavLinkStyle = {
    ...navLinkStyle,
    background: "linear-gradient(135deg, #00C853 0%, #00B4D8 100%)",
    color: "white",
    transform: "translateX(8px)",
  };

  // Style applied on hover for non-active links
  const hoverStyle = {
    background: "linear-gradient(135deg, #00C85320 0%, #00B4D820 100%)",
    color: "#121212",
    transform: "translateX(8px)",
  };

  // Sidebar container
  return (
    <div
      style={{
        background: "#F9FAFB",
        borderRight: "1px solid rgba(255, 255, 255, 0.1)",
        minHeight: "100vh",
        padding: "24px",
        width: "280px",
        boxShadow: "0 0 20px rgba(0, 0, 0, 0.1)",
        position: "relative",
      }}
    >
      {/* Header */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "12px",
          marginBottom: "32px",
          paddingBottom: "16px",
          borderBottom: "1px solid #E5E7EB",
        }}
      >
        <div
          style={{
            fontSize: "2rem",
            background: "linear-gradient(135deg, #00C853 0%, #00B4D8 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
            backgroundClip: "text",
          }}
        >
          ⚡
        </div>
        <h5
          style={{
            margin: 0,
            color: "#121212",
            fontWeight: "700",
            fontSize: "1.25rem",
          }}
        >
          Admin Panel
        </h5>
      </div>

      {/* Navigation Links */}
      <Nav className="flex-column">
        <Nav.Link
          as={Link}
          to="/dashboard/users"
          style={
            location.pathname === "/dashboard/users"
              ? activeNavLinkStyle
              : navLinkStyle
          }
          onMouseEnter={(e) => {
            if (location.pathname !== "/dashboard/users") {
              e.target.style.background = hoverStyle.background;
              e.target.style.color = hoverStyle.color;
              e.target.style.transform = hoverStyle.transform;
            }
          }}
          onMouseLeave={(e) => {
            if (location.pathname !== "/dashboard/users") {
              e.target.style.background = "";
              e.target.style.color = navLinkStyle.color;
              e.target.style.transform = "";
            }
          }}
        >
          👥 Manage Users
        </Nav.Link>

        <Nav.Link
          as={Link}
          to="/dashboard/stations"
          style={
            location.pathname === "/dashboard/stations"
              ? activeNavLinkStyle
              : navLinkStyle
          }
          onMouseEnter={(e) => {
            if (location.pathname !== "/dashboard/stations") {
              e.target.style.background = hoverStyle.background;
              e.target.style.color = hoverStyle.color;
              e.target.style.transform = hoverStyle.transform;
            }
          }}
          onMouseLeave={(e) => {
            if (location.pathname !== "/dashboard/stations") {
              e.target.style.background = "";
              e.target.style.color = navLinkStyle.color;
              e.target.style.transform = "";
            }
          }}
        >
          🏢 Manage Stations
        </Nav.Link>

        <Nav.Link
          as={Link}
          to="/dashboard/ev-owners"
          style={
            location.pathname === "/dashboard/ev-owners"
              ? activeNavLinkStyle
              : navLinkStyle
          }
          onMouseEnter={(e) => {
            if (location.pathname !== "/dashboard/ev-owners") {
              e.target.style.background = hoverStyle.background;
              e.target.style.color = hoverStyle.color;
              e.target.style.transform = hoverStyle.transform;
            }
          }}
          onMouseLeave={(e) => {
            if (location.pathname !== "/dashboard/ev-owners") {
              e.target.style.background = "";
              e.target.style.color = navLinkStyle.color;
              e.target.style.transform = "";
            }
          }}
        >
          🚗 Manage EV Owners
        </Nav.Link>
      </Nav>

      {/* Logout Button */}
      <div
        style={{
          position: "absolute",
          bottom: "64px",
          left: "24px",
          right: "24px",
        }}
      >
        <Button
          variant="outline-danger"
          onClick={handleLogout}
          style={{
            width: "100%",
            borderRadius: "12px",
            padding: "10px 0",
            fontWeight: "500",
            transition: "all 0.3s ease",
            border: "1px solid #ef4444",
          }}
          onMouseEnter={(e) => {
            e.target.style.background =
              "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)";
            e.target.style.color = "white";
          }}
          onMouseLeave={(e) => {
            e.target.style.background = "transparent";
            e.target.style.color = "#ef4444";
          }}
        >
          🚪 Logout
        </Button>
      </div>

      {/* Footer */}
      <div
        style={{
          position: "absolute",
          bottom: "24px",
          left: "24px",
          right: "24px",
          paddingTop: "8px",
          borderTop: "1px solid #E5E7EB",
        }}
      >
        <p
          style={{
            color: "#6C757D",
            fontSize: "0.85rem",
            margin: 0,
            textAlign: "center",
          }}
        >
          Power Your Journey
        </p>
      </div>
    </div>
  );
}
