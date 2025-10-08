// StationDashboard.jsx
import { Outlet, Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Container, Navbar } from "react-bootstrap";

export default function StationDashboard() {
  const navigate = useNavigate();
  const [stationId, setStationId] = useState(null);
  const [stationName, setStationName] = useState("");

  useEffect(() => {
    const token = localStorage.getItem("token");
    const sid = localStorage.getItem("stationId");
    const sname = localStorage.getItem("stationName"); // optional
    if (!token) navigate("/");
    if (sid) setStationId(sid);
    if (sname) setStationName(sname);
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("stationId");
    localStorage.removeItem("stationName");
    navigate("/");
  };

  return (
    <div
      className="d-flex flex-column flex-md-row"
      style={{
        minHeight: "100vh",
        backgroundColor: "#f8f9fa",
      }}
    >
      {/* Sidebar */}
      <aside
        className="d-flex flex-column text-white p-3 shadow-sm"
        style={{
          width: "260px",
          background: "linear-gradient(180deg, #1e293b 0%, #0f172a 100%)",
        }}
      >
        <div className="mb-4 text-center">
          <h4 className="fw-bold text-uppercase mb-0">⚡ Station</h4>
          <small className="text-light opacity-75">
            {stationName || "Dashboard"}
          </small>
        </div>

        <nav className="nav flex-column gap-2">
          <CustomNavLink to="bookings" label="Bookings" icon="📅" />
        </nav>

        <div className="mt-auto text-center">
          <button
            onClick={handleLogout}
            className="btn btn-outline-danger w-100 mt-3 fw-semibold"
          >
            Logout
          </button>
          <small className="d-block mt-2 text-secondary">
            ID: {stationId || "—"}
          </small>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-grow-1">
        <Navbar
          bg="white"
          expand="lg"
          className="shadow-sm border-bottom sticky-top"
        >
          <Container fluid>
            <Navbar.Brand className="fw-semibold text-dark">
              Operator Dashboard
            </Navbar.Brand>
          </Container>
        </Navbar>

        <Container fluid className="p-4">
          {/* Dynamic outlet for child routes */}
          <Outlet context={{ stationId }} />
        </Container>
      </main>
    </div>
  );
}

/* Small helper component for consistent nav links */
function CustomNavLink({ to, label, icon }) {
  return (
    <Link
      to={to}
      className="text-decoration-none text-light px-3 py-2 rounded transition"
      style={{
        display: "flex",
        alignItems: "center",
        gap: "8px",
        transition: "background 0.2s ease",
      }}
      onMouseEnter={(e) => (e.currentTarget.style.background = "#334155")}
      onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
    >
      <span>{icon}</span>
      <span className="fw-medium">{label}</span>
    </Link>
  );
}
