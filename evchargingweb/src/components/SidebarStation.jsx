import { NavLink, useNavigate } from "react-router-dom";
import { useState } from "react";
import { Button, Nav } from "react-bootstrap";
import {
  CalendarCheck,
  Building2,
  LogOut,
  List,
  X,
} from "lucide-react";

export default function SidebarStation() {
  const [open, setOpen] = useState(true);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <div
      style={{
        width: open ? "250px" : "70px",
        backgroundColor: "#1c1c1c",
        color: "#fff",
        minHeight: "100vh",
        transition: "width 0.3s ease",
        display: "flex",
        flexDirection: "column",
        justifyContent: "space-between",
      }}
    >
      {/* Header */}
      <div style={{ padding: "20px", borderBottom: "1px solid #333" }}>
        <div
          style={{
            display: "flex",
            justifyContent: open ? "space-between" : "center",
            alignItems: "center",
          }}
        >
          {open && <h5 className="m-0">⚡ Station Panel</h5>}
          <Button
            variant="outline-light"
            size="sm"
            onClick={() => setOpen(!open)}
            style={{
              backgroundColor: "transparent",
              border: "none",
              color: "#ccc",
            }}
          >
            {open ? <X size={18} /> : <List size={18} />}
          </Button>
        </div>
      </div>

      {/* Navigation */}
      <div style={{ padding: "20px 10px" }}>
        <Nav className="flex-column">
          <NavLink
            to="bookings"
            className={({ isActive }) =>
              `d-flex align-items-center gap-2 mb-3 text-decoration-none ${
                isActive ? "text-info fw-bold" : "text-light"
              }`
            }
          >
            <CalendarCheck size={18} />
            {open && <span>Bookings</span>}
          </NavLink>
          
          <NavLink
            to="slot-calendar"
            className={({ isActive }) =>
              `d-flex align-items-center gap-2 mb-3 text-decoration-none ${
                isActive ? "text-info fw-bold" : "text-light"
              }`
            }
          >
            <CalendarCheck size={18} />
            {open && <span>SlotCalendar</span>}
          </NavLink>

          <NavLink
            to="profile"
            className={({ isActive }) =>
              `d-flex align-items-center gap-2 mb-3 text-decoration-none ${
                isActive ? "text-info fw-bold" : "text-light"
              }`
            }
          >
            <Building2 size={18} />
            {open && <span>Station Info</span>}
          </NavLink>
        </Nav>
      </div>

      {/* Logout */}
      <div
        style={{
          padding: "20px",
          borderTop: "1px solid #333",
          textAlign: open ? "left" : "center",
        }}
      >
        <Button
          variant="danger"
          size="sm"
          className="d-flex align-items-center gap-2 w-100 justify-content-center"
          onClick={handleLogout}
        >
          <LogOut size={16} />
          {open && "Logout"}
        </Button>
      </div>
    </div>
  );
}
