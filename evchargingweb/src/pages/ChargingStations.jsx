import { useEffect, useState } from "react";
import { Table, Button, Modal, Form, Alert } from "react-bootstrap";

export default function ChargingStations() {
  const [stations, setStations] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedStation, setSelectedStation] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [type, setType] = useState("AC");
  const [totalSlots, setSlots] = useState(1);
  const [pricePerHour, setPricePerHour] = useState(0.01);
  const [status, setStatus] = useState(0); // 0=Active, 1=Inactive, 2=Maintenance

  const token = localStorage.getItem("token");

  const fetchStations = async () => {
    try {
      const res = await fetch("http://localhost:5082/api/chargingstations", {
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to fetch stations");
      const data = await res.json();
      setStations(data);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchStations();
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this station?")) return;
    try {
      const res = await fetch(`http://localhost:5082/api/chargingstations/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to delete station");
      setSuccess("Station deleted successfully");
      fetchStations();
    } catch (err) {
      setError(err.message);
    }
  };

const handleChangeStatus = async (station, newStatus) => {
  try {
    if (newStatus === 1) { // Deactivate
      const res = await fetch(`http://localhost:5082/api/chargingstations/${station.id}/deactivate`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to deactivate station");
      setSuccess("Station deactivated successfully");
    } else { // Activate (or change from maintenance)
      const payload = {
        Name: station.name,
        Location: station.location,
        Type: station.type,
        TotalSlots: station.totalSlots,
        PricePerHour: station.pricePerHour,
        Status: 0, // Active
      };
      const res = await fetch(`http://localhost:5082/api/chargingstations/${station.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error("Failed to activate station");
      setSuccess("Station activated successfully");
    }
    fetchStations();
  } catch (err) {
    setError(err.message);
  }
};



  const handleOpenModal = (station = null) => {
    if (station) {
      setSelectedStation(station);
      setEditMode(true);
      setName(station.name);
      setLocation(station.location);
      setType(station.type);
      setSlots(station.totalSlots);
      setPricePerHour(station.pricePerHour || 0.01);
      setStatus(station.status);
    } else {
      setSelectedStation(null);
      setEditMode(false);
      setName("");
      setLocation("");
      setType("AC");
      setSlots(1);
      setPricePerHour(0.01);
      setStatus(0);
    }
    setShowModal(true);
  };

  const handleSave = async () => {
    const payload = {
      Name: name,
      Location: location,
      Type: type === "AC" ? 0 : 1,
      TotalSlots: totalSlots,
      PricePerHour: pricePerHour,
      Status: status,
    };

    const url = editMode
      ? `http://localhost:5082/api/chargingstations/${selectedStation.id}`
      : "http://localhost:5082/api/chargingstations";
    const method = editMode ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error("Failed to save station");
      setSuccess(editMode ? "Station updated successfully" : "Station created successfully");
      setShowModal(false);
      fetchStations();
    } catch (err) {
      setError(err.message);
    }
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case 0: return <span className="badge bg-success">Active</span>;
      case 1: return <span className="badge bg-secondary">Inactive</span>;
      case 2: return <span className="badge bg-warning">Maintenance</span>;
      default: return <span className="badge bg-secondary">Unknown</span>;
    }
  };

  return (
    <div>
      <h3 className="mb-4">Charging Stations</h3>
      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Button className="mb-3" onClick={() => handleOpenModal()}>Add Station</Button>

      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>Name</th>
            <th>Location</th>
            <th>Type</th>
            <th>Slots</th>
            <th>Price/Hour</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {stations.length > 0 ? (
            stations.map((s) => (
              <tr key={s.id}>
                <td>{s.name}</td>
                <td>{s.location}</td>
                <td>{s.type === 0 ? "AC" : "DC"}</td>
                <td>{s.totalSlots}</td>
                <td>{s.pricePerHour}</td>
                <td>{renderStatusBadge(s.status)}</td>
                <td>
      {s.status === 0 && ( // Active
        <Button size="sm" variant="secondary" onClick={() => handleChangeStatus(s, 1)}>Deactivate</Button>
      )}
      {(s.status === 1 || s.status === 2) && ( // Inactive or Maintenance
        <Button size="sm" variant="success" onClick={() => handleChangeStatus(s, 0)}>Activate</Button>
      )}
      <Button size="sm" variant="warning" onClick={() => handleOpenModal(s)}>Edit</Button>{" "}
      <Button size="sm" variant="danger" onClick={() => handleDelete(s.id)}>Delete</Button>
    </td>

              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7" className="text-center">No stations found.</td>
            </tr>
          )}
        </tbody>
      </Table>

      {/* Add/Edit Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>{editMode ? "Edit Station" : "Add Station"}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Name</Form.Label>
              <Form.Control value={name} onChange={(e) => setName(e.target.value)} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Location</Form.Label>
              <Form.Control value={location} onChange={(e) => setLocation(e.target.value)} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Type</Form.Label>
              <Form.Select value={type} onChange={(e) => setType(e.target.value)}>
                <option value="AC">AC</option>
                <option value="DC">DC</option>
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Slots</Form.Label>
              <Form.Control type="number" min={1} max={50} value={totalSlots} onChange={(e) => setSlots(Number(e.target.value))} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Price Per Hour</Form.Label>
              <Form.Control type="number" step={0.01} min={0.01} max={1000} value={pricePerHour} onChange={(e) => setPricePerHour(Number(e.target.value))} />
            </Form.Group>
            <Form.Group className="mb-3">
  <Form.Label>Status</Form.Label>
  <Form.Select
    value={status}
    onChange={(e) => setStatus(Number(e.target.value))}
    disabled={editMode} // <-- make readonly when editing
  >
    <option value={0}>Active</option>
    <option value={1}>Inactive</option>
    <option value={2}>Maintenance</option>
  </Form.Select>
</Form.Group>

          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>Close</Button>
          <Button variant="primary" onClick={handleSave}>{editMode ? "Save Changes" : "Add Station"}</Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}
