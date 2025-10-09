//Users.jsx
import { useEffect, useState } from "react";
import { Table, Button, Modal, Form, Alert } from "react-bootstrap";

export default function Users() {
   // --- State Hooks ---
  const [users, setUsers] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const [nic, setNic] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [role, setRole] = useState("");
  const [isActive, setIsActive] = useState(false);

  const token = localStorage.getItem("token");

  // --- Fetch users from API ---
  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await fetch("http://localhost:5082/api/users", {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Failed to fetch users");
      const data = await res.json();
      setUsers(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // --- Load users on component mount ---
  useEffect(() => {
    if (!token) {
      setError("Not authorized. Please login.");
      return;
    }
    fetchUsers();
  }, [token]);

  // --- Delete a user ---
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;

    try {
      const res = await fetch(`http://localhost:5082/api/users/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) throw new Error("Failed to delete user");
      setSuccess("User deleted successfully");
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  // --- Toggle user active/inactive status ---
  const handleToggleStatus = async (id, currentStatus) => {
    const confirmMsg = currentStatus
      ? "Deactivate this user?"
      : "Activate this user?";
    if (!window.confirm(confirmMsg)) return;

    try {
      const res = await fetch(`http://localhost:5082/api/users/${id}/status`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(!currentStatus),
      });

      if (!res.ok) throw new Error("Failed to update status");
      setSuccess(`User ${currentStatus ? "deactivated" : "activated"} successfully`);
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  // --- Open modal to view user details ---
  const handleView = (user) => {
    setSelectedUser(user);
    setEditMode(false);
    setNic(user.nic);
    setFirstName(user.firstName);
    setLastName(user.lastName);
    setEmail(user.email);
    setPhoneNumber(user.phoneNumber || "");
    setRole(user.role);
    setIsActive(user.isActive);
    setShowModal(true);
  };

  // --- Open modal to edit user ---
  const handleEdit = (user) => {
    setSelectedUser(user);
    setEditMode(true);
    setNic(user.nic);
    setFirstName(user.firstName);
    setLastName(user.lastName);
    setEmail(user.email);
    setPhoneNumber(user.phoneNumber || "");
    setRole(user.role);
    setIsActive(user.isActive);
    setShowModal(true);
  };

  // --- Save updated user information ---
  const handleSave = async () => {
    if (!selectedUser) return;

    const payload = {
      nic,
      firstName,
      lastName,
      email,
      phoneNumber,
      role: Number(role),
      isActive,
    };

    try {
      const res = await fetch(
        `http://localhost:5082/api/users/${selectedUser.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      if (!res.ok) throw new Error("Failed to update user");
      setSuccess("User updated successfully");
      setShowModal(false);
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  // --- Clear error and success messages after 5 seconds ---
  useEffect(() => {
    if (error || success) {
      const timer = setTimeout(() => {
        setError("");
        setSuccess("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [error, success]);

  return (
    <div style={{ padding: '24px' }}>
      {/* Header Section */}
      <div style={{ 
        display: 'flex', 
        alignItems: 'center', 
        gap: '16px', 
        marginBottom: '32px' 
      }}>
        <div style={{
          fontSize: '2.5rem',
          background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text'
        }}>
          👥
        </div>
        <div>
          <h1 style={{ 
            color: '#121212', 
            margin: 0, 
            fontWeight: '700',
            fontSize: '2rem'
          }}>
            Manage Users
          </h1>
          <p style={{ 
            color: '#6C757D', 
            margin: 0,
            fontSize: '1.1rem'
          }}>
            View and manage all system users
          </p>
        </div>
      </div>

      {/* Alerts */}
      {error && (
        <div style={{
          background: 'rgba(255, 183, 3, 0.1)',
          color: '#FFB703',
          padding: '16px',
          borderRadius: '12px',
          border: '1px solid rgba(255, 183, 3, 0.3)',
          marginBottom: '24px',
          fontWeight: '500'
        }}>
          {error}
        </div>
      )}
      
      {success && (
        <div style={{
          background: 'rgba(0, 200, 83, 0.1)',
          color: '#00C853',
          padding: '16px',
          borderRadius: '12px',
          border: '1px solid rgba(0, 200, 83, 0.3)',
          marginBottom: '24px',
          fontWeight: '500'
        }}>
          {success}
        </div>
      )}

      {/* Users Table */}
      <div style={{
        background: '#F9FAFB',
        borderRadius: '20px',
        padding: '32px',
        boxShadow: '0 10px 40px rgba(0, 0, 0, 0.1)',
        border: '1px solid rgba(255, 255, 255, 0.1)'
      }}>
        {loading ? (
          <div style={{ 
            textAlign: 'center', 
            padding: '40px',
            color: '#6C757D'
          }}>
            <div className="spinner" style={{
              width: '40px',
              height: '40px',
              border: '3px solid transparent',
              borderTop: '3px solid #00C853',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 16px'
            }}></div>
            Loading users...
          </div>
        ) : (
          <Table hover responsive style={{ 
            margin: 0,
            border: 'none'
          }}>
            <thead>
              <tr style={{ 
                background: 'linear-gradient(135deg, #1B263B 0%, #121212 100%)',
                color: '#F9FAFB'
              }}>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>NIC</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Name</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Email</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Phone</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Role</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Status</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem',
                  textAlign: 'center'
                }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.length > 0 ? (
                users.map((u) => (
                  <tr key={u.id} style={{ 
                    borderBottom: '1px solid #E5E7EB',
                    transition: 'all 0.3s ease'
                  }}>
                    <td style={{ 
                      padding: '16px', 
                      fontWeight: '500',
                      color: '#121212'
                    }}>{u.nic}</td>
                    <td style={{ 
                      padding: '16px',
                      color: '#121212'
                    }}>{u.firstName} {u.lastName}</td>
                    <td style={{ 
                      padding: '16px',
                      color: '#121212'
                    }}>{u.email}</td>
                    <td style={{ 
                      padding: '16px',
                      color: '#121212'
                    }}>{u.phoneNumber || "N/A"}</td>
                    <td style={{ padding: '16px' }}>
                      <span style={{
                        padding: '6px 12px',
                        borderRadius: '20px',
                        fontSize: '0.85rem',
                        fontWeight: '500',
                        background: u.role === 0 
                          ? 'linear-gradient(135deg, #00C85320 0%, #00B4D820 100%)' 
                          : u.role === 1 
                          ? 'linear-gradient(135deg, #FFB70320 0%, #FF910020 100%)'
                          : 'linear-gradient(135deg, #7209B720 0%, #560BAD20 100%)',
                        color: u.role === 0 
                          ? '#00C853' 
                          : u.role === 1 
                          ? '#FFB703'
                          : '#7209B7'
                      }}>
                        {u.role === 0 ? "Backoffice" : u.role === 1 ? "Operator" : "EV Owner"}
                      </span>
                    </td>
                    <td style={{ padding: '16px' }}>
                      <span style={{
                        padding: '6px 12px',
                        borderRadius: '20px',
                        fontSize: '0.85rem',
                        fontWeight: '500',
                        background: u.isActive 
                          ? 'rgba(0, 200, 83, 0.1)' 
                          : 'rgba(108, 117, 125, 0.1)',
                        color: u.isActive ? '#00C853' : '#6C757D'
                      }}>
                        {u.isActive ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td style={{ 
                      padding: '16px',
                      textAlign: 'center'
                    }}>
                      <div style={{ 
                        display: 'flex', 
                        gap: '8px', 
                        justifyContent: 'center',
                        flexWrap: 'wrap'
                      }}>
                        <Button
                          variant="outline-info"
                          size="sm"
                          onClick={() => handleView(u)}
                          style={{
                            border: '2px solid #00B4D8',
                            color: '#00B4D8',
                            borderRadius: '8px',
                            fontWeight: '500',
                            padding: '6px 12px'
                          }}
                        >
                          👁️ View
                        </Button>
                        <Button
                          variant="outline-warning"
                          size="sm"
                          onClick={() => handleEdit(u)}
                          style={{
                            border: '2px solid #FFB703',
                            color: '#FFB703',
                            borderRadius: '8px',
                            fontWeight: '500',
                            padding: '6px 12px'
                          }}
                        >
                          ✏️ Edit
                        </Button>
                        <Button
                          variant={u.isActive ? "outline-secondary" : "outline-success"}
                          size="sm"
                          onClick={() => handleToggleStatus(u.id, u.isActive)}
                          style={{
                            border: u.isActive ? '2px solid #6C757D' : '2px solid #00C853',
                            color: u.isActive ? '#6C757D' : '#00C853',
                            borderRadius: '8px',
                            fontWeight: '500',
                            padding: '6px 12px'
                          }}
                        >
                          {u.isActive ? "⏸️ Deactivate" : "▶️ Activate"}
                        </Button>
                        <Button
                          variant="outline-danger"
                          size="sm"
                          onClick={() => handleDelete(u.id)}
                          style={{
                            border: '2px solid #DC3545',
                            color: '#DC3545',
                            borderRadius: '8px',
                            fontWeight: '500',
                            padding: '6px 12px'
                          }}
                        >
                          🗑️ Delete
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" style={{ 
                    padding: '40px', 
                    textAlign: 'center',
                    color: '#6C757D'
                  }}>
                    <div style={{ fontSize: '3rem', marginBottom: '16px' }}>👥</div>
                    <h4 style={{ color: '#121212', marginBottom: '8px' }}>No Users Found</h4>
                    <p>There are no users in the system yet.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        )}
      </div>

      {/* View/Edit Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)} centered>
        <Modal.Header style={{ 
          background: 'linear-gradient(135deg, #1B263B 0%, #121212 100%)',
          color: '#F9FAFB',
          border: 'none'
        }}>
          <Modal.Title style={{ fontWeight: '600' }}>
            {editMode ? "✏️ Edit User" : "👁️ User Details"}
          </Modal.Title>
          <button 
            onClick={() => setShowModal(false)}
            style={{
              background: 'none',
              border: 'none',
              color: '#F9FAFB',
              fontSize: '1.5rem',
              cursor: 'pointer'
            }}
          >
            ×
          </button>
        </Modal.Header>
        <Modal.Body style={{ padding: '24px', background: '#F9FAFB' }}>
          {selectedUser && (
            <Form>
              <Form.Group className="mb-3">
                <Form.Label style={{ fontWeight: '600', color: '#121212' }}>NIC</Form.Label>
                <Form.Control 
                  value={nic} 
                  readOnly 
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    border: '2px solid #E5E7EB',
                    background: '#FFFFFF'
                  }}
                />
              </Form.Group>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <Form.Group className="mb-3">
                  <Form.Label style={{ fontWeight: '600', color: '#121212' }}>First Name</Form.Label>
                  <Form.Control
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    readOnly={!editMode}
                    style={{
                      padding: '12px',
                      borderRadius: '8px',
                      border: '2px solid #E5E7EB',
                      background: '#FFFFFF'
                    }}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Last Name</Form.Label>
                  <Form.Control
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    readOnly={!editMode}
                    style={{
                      padding: '12px',
                      borderRadius: '8px',
                      border: '2px solid #E5E7EB',
                      background: '#FFFFFF'
                    }}
                  />
                </Form.Group>
              </div>

              <Form.Group className="mb-3">
                <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Email</Form.Label>
                <Form.Control
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  readOnly={!editMode}
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    border: '2px solid #E5E7EB',
                    background: '#FFFFFF'
                  }}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Phone Number</Form.Label>
                <Form.Control
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  readOnly={!editMode}
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    border: '2px solid #E5E7EB',
                    background: '#FFFFFF'
                  }}
                />
              </Form.Group>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <Form.Group className="mb-3">
                  <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Role</Form.Label>
                  <Form.Control
                    value={
                      role === 0
                        ? "Backoffice"
                        : role === 1
                        ? "Station Operator"
                        : "EV Owner"
                    }
                    readOnly
                    style={{
                      padding: '12px',
                      borderRadius: '8px',
                      border: '2px solid #E5E7EB',
                      background: '#FFFFFF'
                    }}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Status</Form.Label>
                  <Form.Control
                    value={isActive ? "Active" : "Inactive"}
                    readOnly
                    style={{
                      padding: '12px',
                      borderRadius: '8px',
                      border: '2px solid #E5E7EB',
                      background: '#FFFFFF'
                    }}
                  />
                </Form.Group>
              </div>
            </Form>
          )}
        </Modal.Body>
        <Modal.Footer style={{ 
          background: '#F9FAFB', 
          border: 'none',
          padding: '16px 24px 24px'
        }}>
          <Button 
            variant="outline-secondary" 
            onClick={() => setShowModal(false)}
            style={{
              border: '2px solid #6C757D',
              color: '#6C757D',
              borderRadius: '8px',
              fontWeight: '500',
              padding: '8px 20px'
            }}
          >
            Close
          </Button>
          {editMode && (
            <Button 
              variant="primary" 
              onClick={handleSave}
              style={{
                background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
                border: 'none',
                borderRadius: '8px',
                fontWeight: '600',
                padding: '8px 24px'
              }}
            >
              💾 Save Changes
            </Button>
          )}
        </Modal.Footer>
      </Modal>

      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        
        tr:hover {
          background: rgba(0, 200, 83, 0.02) !important;
          transform: translateY(-1px);
        }
        
        .btn:hover {
          transform: translateY(-2px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
      `}</style>
    </div>
  );
}