import { useEffect, useState } from "react";
import { Card, Button, Form, Row, Col, Alert } from "react-bootstrap";

export default function StationProfile() {
  const [station, setStation] = useState({
    id: "",
    name: "",
    location: "",
    contactNumber: "",
    email: "",
    status: "",
  });

  const [editMode, setEditMode] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");
  const stationId = localStorage.getItem("stationId");

  // Fetch station details
  const fetchStationDetails = async () => {
    try {
      const res = await fetch(
        `http://localhost:5082/api/bookings/station/${stationId}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!res.ok) throw new Error("Failed to fetch station details");
      const data = await res.json();
      setStation(data);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchStationDetails();
  }, []);

  // Update station profile
  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch(`http://localhost:5082/api/stations/${stationId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(station),
      });
      if (!res.ok) throw new Error("Failed to update profile");
      setSuccess("Station profile updated successfully");
      setEditMode(false);
      fetchStationDetails();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="p-4">
      <h3 className="mb-4">🏭 Station Profile</h3>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Card className="shadow-sm border-0 p-4">
        <Form onSubmit={handleUpdate}>
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Station Name</Form.Label>
                <Form.Control
                  type="text"
                  value={station.name}
                  readOnly={!editMode}
                  onChange={(e) => setStation({ ...station, name: e.target.value })}
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Location</Form.Label>
                <Form.Control
                  type="text"
                  value={station.location}
                  readOnly={!editMode}
                  onChange={(e) => setStation({ ...station, location: e.target.value })}
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Contact Number</Form.Label>
                <Form.Control
                  type="text"
                  value={station.contactNumber}
                  readOnly={!editMode}
                  onChange={(e) =>
                    setStation({ ...station, contactNumber: e.target.value })
                  }
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Email</Form.Label>
                <Form.Control
                  type="email"
                  value={station.email}
                  readOnly={!editMode}
                  onChange={(e) => setStation({ ...station, email: e.target.value })}
                />
              </Form.Group>
            </Col>

            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Status</Form.Label>
                <Form.Control
                  type="text"
                  value={station.status}
                  readOnly
                />
              </Form.Group>
            </Col>
          </Row>

          <div className="d-flex justify-content-end gap-2 mt-3">
            {!editMode ? (
              <Button variant="primary" onClick={() => setEditMode(true)}>
                ✏️ Edit
              </Button>
            ) : (
              <>
                <Button variant="success" type="submit">
                  💾 Save
                </Button>
                <Button variant="secondary" onClick={() => setEditMode(false)}>
                  ❌ Cancel
                </Button>
              </>
            )}
          </div>
        </Form>
      </Card>
    </div>
  );
}
