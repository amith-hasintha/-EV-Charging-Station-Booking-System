import { useEffect, useState, useMemo } from "react";
import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import { Button, Modal, Form, Alert } from "react-bootstrap";
import "react-big-calendar/lib/css/react-big-calendar.css";

const localizer = momentLocalizer(moment);

export default function SlotCalendar() {
  const [slots, setSlots] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [newSlot, setNewSlot] = useState({ startTime: "", endTime: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const token = localStorage.getItem("token");
  const stationId = localStorage.getItem("stationId");

  // Fetch slots
  const fetchSlots = async () => {
    try {
      const res = await fetch(`http://localhost:5082/api/stations/${stationId}/slots`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to fetch slots");
      const data = await res.json();

      // Convert backend data to calendar events
      const formatted = data.map((s) => ({
        id: s.id,
        title: `${s.status}`,
        start: new Date(s.startTime),
        end: new Date(s.endTime),
        status: s.status,
      }));

      setSlots(formatted);
      setError("");
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchSlots();
  }, [stationId]);

  // Add new slot
  const addSlot = async () => {
    if (!newSlot.startTime || !newSlot.endTime) {
      setError("Please select both start and end time");
      return;
    }
    try {
      const res = await fetch(`http://localhost:5082/api/stations/${stationId}/slots`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(newSlot),
      });
      if (!res.ok) throw new Error("Failed to add slot");
      setSuccess("Slot added successfully");
      setShowAddModal(false);
      setNewSlot({ startTime: "", endTime: "" });
      fetchSlots();
    } catch (err) {
      setError(err.message);
    }
  };

  // Toggle slot status (block/unblock)
  const toggleStatus = async (slot) => {
    const newStatus = slot.status === "Available" ? "Blocked" : "Available";
    try {
      const res = await fetch(`http://localhost:5082/api/slots/${slot.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status: newStatus }),
      });
      if (!res.ok) throw new Error("Failed to update slot");
      setSuccess(`Slot ${slot.id} marked as ${newStatus}`);
      setSelectedEvent(null);
      fetchSlots();
    } catch (err) {
      setError(err.message);
    }
  };

  // Delete slot
  const deleteSlot = async (slotId) => {
    if (!window.confirm("Delete this slot?")) return;
    try {
      const res = await fetch(`http://localhost:5082/api/slots/${slotId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to delete slot");
      setSuccess("Slot deleted");
      setSelectedEvent(null);
      fetchSlots();
    } catch (err) {
      setError(err.message);
    }
  };

  // Custom slot colors
  const eventStyleGetter = (event) => {
    let bgColor = "#007bff";
    if (event.status === "Blocked") bgColor = "#dc3545";
    else if (event.status === "Available") bgColor = "#28a745";
    else if (event.status === "Booked") bgColor = "#ffc107";
    return {
      style: {
        backgroundColor: bgColor,
        color: "white",
        borderRadius: "8px",
        border: "none",
        padding: "4px",
      },
    };
  };

  return (
    <div>
      <h3 className="mb-4">🗓️ Station Slot Calendar</h3>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <div className="d-flex justify-content-between mb-3">
        <Button variant="primary" onClick={() => setShowAddModal(true)}>
          ➕ Add Slot
        </Button>
        <Button variant="outline-secondary" onClick={fetchSlots}>
          🔄 Refresh
        </Button>
      </div>

      <Calendar
        localizer={localizer}
        events={slots}
        startAccessor="start"
        endAccessor="end"
        style={{ height: 600 }}
        views={["day", "week", "month"]}
        eventPropGetter={eventStyleGetter}
        onSelectEvent={(event) => setSelectedEvent(event)}
      />

      {/* Event Details Modal */}
      <Modal show={!!selectedEvent} onHide={() => setSelectedEvent(null)}>
        <Modal.Header closeButton>
          <Modal.Title>Slot Details</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedEvent && (
            <>
              <p><strong>ID:</strong> {selectedEvent.id}</p>
              <p><strong>Status:</strong> {selectedEvent.status}</p>
              <p>
                <strong>Start:</strong>{" "}
                {new Date(selectedEvent.start).toLocaleString()}
              </p>
              <p>
                <strong>End:</strong>{" "}
                {new Date(selectedEvent.end).toLocaleString()}
              </p>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          {selectedEvent && (
            <>
              <Button
                variant={
                  selectedEvent.status === "Available" ? "warning" : "success"
                }
                onClick={() => toggleStatus(selectedEvent)}
              >
                {selectedEvent.status === "Available" ? "Block" : "Unblock"}
              </Button>
              <Button
                variant="danger"
                onClick={() => deleteSlot(selectedEvent.id)}
              >
                Delete
              </Button>
            </>
          )}
        </Modal.Footer>
      </Modal>

      {/* Add Slot Modal */}
      <Modal show={showAddModal} onHide={() => setShowAddModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Add New Slot</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Start Time</Form.Label>
              <Form.Control
                type="datetime-local"
                value={newSlot.startTime}
                onChange={(e) =>
                  setNewSlot({ ...newSlot, startTime: e.target.value })
                }
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>End Time</Form.Label>
              <Form.Control
                type="datetime-local"
                value={newSlot.endTime}
                onChange={(e) =>
                  setNewSlot({ ...newSlot, endTime: e.target.value })
                }
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowAddModal(false)}>
            Cancel
          </Button>
          <Button variant="primary" onClick={addSlot}>
            Add Slot
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}
