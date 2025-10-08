import { useEffect, useState } from "react";
import { Table, Button, Modal, Form, Alert } from "react-bootstrap";

export default function EVOwnerManagement() {
  const [users, setUsers] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // User fields
  const [nic, setNic] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [isActive, setIsActive] = useState(false);

  const token = localStorage.getItem("token");

  // ---------------- Fetch EV Owners ----------------
  const fetchEVOwners = async () => {
    try {
      const res = await fetch("http://localhost:5082/api/users", {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });
      if (!res.ok) throw new Error("Failed to fetch users");
      const data = await res.json();
      setUsers(data.filter(u => u.role === 2)); // Only EV Owners
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    if (!token) {
      setError("Not authorized. Please login.");
      return;
    }
    fetchEVOwners();
  }, [token]);

  // ---------------- Delete EV Owner ----------------
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this EV Owner?")) return;

    try {
      const res = await fetch(`http://localhost:5082/api/users/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to delete EV Owner");
      setSuccess("EV Owner deleted successfully");
      fetchEVOwners();
    } catch (err) {
      setError(err.message);
    }
  };

  // ---------------- View EV Owner ----------------
  const handleView = (user) => {
    setSelectedUser(user);
    setEditMode(false);
    fillUserFields(user);
    setShowModal(true);
  };

  // ---------------- Edit EV Owner ----------------
  const handleEdit = (user) => {
    setSelectedUser(user);
    setEditMode(true);
    fillUserFields(user);
    setShowModal(true);
  };

  // ---------------- Fill Form Fields ----------------
  const fillUserFields = (user) => {
    setNic(user.nic);
    setFirstName(user.firstName);
    setLastName(user.lastName);
    setEmail(user.email);
    setPhoneNumber(user.phoneNumber || "");
    setIsActive(user.isActive);
  };

  // ---------------- Save Updated EV Owner ----------------
  const handleSave = async () => {
    if (!selectedUser) return;

    const payload = {
      nic,
      firstName,
      lastName,
      email,
      phoneNumber,
      role: 2, // Always EV Owner
      isActive,
    };

    try {
      const res = await fetch(`http://localhost:5082/api/users/${selectedUser.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error("Failed to update EV Owner");
      setSuccess("EV Owner updated successfully");
      setShowModal(false);
      fetchEVOwners();
    } catch (err) {
      setError(err.message);
    }
  };

  // ---------------- Toggle Status ----------------
  const handleToggleStatus = async (id, currentStatus) => {
    const confirmMsg = currentStatus
      ? "Deactivate this EV Owner?"
      : "Activate this EV Owner?";
    if (!window.confirm(confirmMsg)) return;

    try {
      const res = await fetch(`http://localhost:5082/api/users/${id}/status`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(!currentStatus), // must send boolean
      });
      if (!res.ok) throw new Error("Failed to update status");
      setSuccess(`EV Owner ${currentStatus ? "deactivated" : "activated"} successfully`);
      fetchEVOwners();
    } catch (err) {
      setError(err.message);
    }
  };

  // ---------------- Render ----------------
  return (
    <div>
      <h3 className="mb-4">EV Owner Management</h3>
      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>NIC</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.length > 0 ? (
            users.map(u => (
              <tr key={u.id}>
                <td>{u.nic}</td>
                <td>{u.firstName}</td>
                <td>{u.lastName}</td>
                <td>{u.email}</td>
                <td>{u.phoneNumber || "N/A"}</td>
                <td>
                  <span className={`badge ${u.isActive ? "bg-success" : "bg-secondary"}`}>
                    {u.isActive ? "Active" : "Inactive"}
                  </span>
                </td>
                <td>
                  <Button variant="info" size="sm" onClick={() => handleView(u)}>View</Button>{" "}
                  <Button variant="warning" size="sm" onClick={() => handleEdit(u)}>Edit</Button>{" "}
                  <Button variant="danger" size="sm" onClick={() => handleDelete(u.id)}>Delete</Button>{" "}
                  <Button 
                    variant={u.isActive ? "secondary" : "success"} 
                    size="sm" 
                    onClick={() => handleToggleStatus(u.id, u.isActive)}
                  >
                    {u.isActive ? "Deactivate" : "Activate"}
                  </Button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7" className="text-center">No EV Owners found.</td>
            </tr>
          )}
        </tbody>
      </Table>

      {/* View/Edit Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>{editMode ? "Edit EV Owner" : "EV Owner Details"}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedUser && (
            <Form>
              <Form.Group className="mb-3">
                <Form.Label>NIC</Form.Label>
                <Form.Control value={nic} readOnly />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>First Name</Form.Label>
                <Form.Control
                  value={firstName}
                  onChange={e => setFirstName(e.target.value)}
                  readOnly={!editMode}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Last Name</Form.Label>
                <Form.Control
                  value={lastName}
                  onChange={e => setLastName(e.target.value)}
                  readOnly={!editMode}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Email</Form.Label>
                <Form.Control
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  readOnly={!editMode}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Phone Number</Form.Label>
                <Form.Control
                  value={phoneNumber}
                  onChange={e => setPhoneNumber(e.target.value)}
                  readOnly={!editMode}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Status</Form.Label>
                <Form.Control
                  value={isActive ? "Active" : "Inactive"}
                  readOnly
                />
              </Form.Group>
            </Form>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>Close</Button>
          {editMode && (
            <Button variant="primary" onClick={handleSave}>Save Changes</Button>
          )}
        </Modal.Footer>
      </Modal>
    </div>
  );
}
