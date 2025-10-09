//chargingStation.jsx
import { useEffect, useState } from "react";
import { Table, Button, Modal, Form, Alert } from "react-bootstrap";

export default function ChargingStations() {
  // --- State variables ---
  const [stations, setStations] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedStation, setSelectedStation] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  // Form fields
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [type, setType] = useState("AC");
  const [totalSlots, setSlots] = useState(1);
  const [pricePerHour, setPricePerHour] = useState(0.01);
  const [status, setStatus] = useState(0);

  const token = localStorage.getItem("token");

  // --- Fetch all stations from API ---
  const fetchStations = async () => {
    setLoading(true);
    try {
      const res = await fetch("http://localhost:5082/api/chargingstations", {
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to fetch stations");
      const data = await res.json();
      setStations(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // --- Effect: load stations on component mount ---
  useEffect(() => {
    fetchStations();
  }, []);

  // --- Delete a station ---
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

  // --- Change station status (activate/deactivate) ---
  const handleChangeStatus = async (station, newStatus) => {
    try {
      if (newStatus === 1) {
        // Deactivate station
        const res = await fetch(`http://localhost:5082/api/chargingstations/${station.id}/deactivate`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) throw new Error("Failed to deactivate station");
        setSuccess("Station deactivated successfully");
      } else {
        // Activate station
        const payload = {
          Name: station.name,
          Location: station.location,
          Type: station.type,
          TotalSlots: station.totalSlots,
          PricePerHour: station.pricePerHour,
          Status: 0,
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

  // --- Open modal for add/edit ---
  const handleOpenModal = (station = null) => {
    if (station) {
      // Edit mode
      setSelectedStation(station);
      setEditMode(true);
      setName(station.name);
      setLocation(station.location);
      setType(station.type === 0 ? "AC" : "DC");
      setSlots(station.totalSlots);
      setPricePerHour(station.pricePerHour || 0.01);
      setStatus(station.status);
    } else {
      // Add new mode
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

  // --- Save station (create or update) ---
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

  // --- Render status badge for table ---
  const renderStatusBadge = (status) => {
    switch (status) {
      case 0: return <span style={{ padding: '6px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', background: 'rgba(0, 200, 83, 0.1)', color: '#00C853' }}>⚡ Active</span>;
      case 1: return <span style={{ padding: '6px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', background: 'rgba(108, 117, 125, 0.1)', color: '#6C757D' }}>⏸️ Inactive</span>;
      case 2: return <span style={{ padding: '6px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', background: 'rgba(255, 183, 3, 0.1)', color: '#FFB703' }}>🔧 Maintenance</span>;
      default: return <span style={{ padding: '6px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', background: 'rgba(108, 117, 125, 0.1)', color: '#6C757D' }}>❓ Unknown</span>;
    }
  };

  // --- Auto-clear success/error messages ---
  useEffect(() => {
    if (error || success) {
      const timer = setTimeout(() => {
        setError("");
        setSuccess("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [error, success]);

  // --- Render component UI ---
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
          🏢
        </div>
        <div>
          <h1 style={{ 
            color: '#121212', 
            margin: 0, 
            fontWeight: '700',
            fontSize: '2rem'
          }}>
            Charging Stations
          </h1>
          <p style={{ 
            color: '#6C757D', 
            margin: 0,
            fontSize: '1.1rem'
          }}>
            Manage all charging stations and their availability
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

      {/* Action Bar */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '24px'
      }}>
        <div style={{ fontSize: '1rem', color: '#6C757D', fontWeight: '500' }}>
          Total Stations: <strong style={{ color: '#121212' }}>{stations.length}</strong>
        </div>
        <Button 
          onClick={() => handleOpenModal()}
          style={{
            background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
            border: 'none',
            borderRadius: '12px',
            fontWeight: '600',
            padding: '12px 24px',
            fontSize: '1rem'
          }}
        >
          ➕ Add New Station
        </Button>
      </div>

      {/* Stations Table */}
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
            Loading charging stations...
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
                }}>Station Name</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Location</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Type</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Slots</th>
                <th style={{ 
                  padding: '16px', 
                  border: 'none',
                  fontWeight: '600',
                  fontSize: '0.95rem'
                }}>Price/Hour</th>
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
              {stations.length > 0 ? (
                stations.map((s) => (
                  <tr key={s.id} style={{ 
                    borderBottom: '1px solid #E5E7EB',
                    transition: 'all 0.3s ease'
                  }}>
                    <td style={{ 
                      padding: '16px', 
                      fontWeight: '600',
                      color: '#121212'
                    }}>{s.name}</td>
                    <td style={{ 
                      padding: '16px',
                      color: '#121212'
                    }}>{s.location}</td>
                    <td style={{ padding: '16px' }}>
                      <span style={{
                        padding: '6px 12px',
                        borderRadius: '20px',
                        fontSize: '0.85rem',
                        fontWeight: '500',
                        background: s.type === 0 
                          ? 'linear-gradient(135deg, #00C85320 0%, #00B4D820 100%)' 
                          : 'linear-gradient(135deg, #FFB70320 0%, #FF910020 100%)',
                        color: s.type === 0 ? '#00C853' : '#FFB703'
                      }}>
                        {s.type === 0 ? "🔌 AC" : "⚡ DC"}
                      </span>
                    </td>
                    <td style={{ 
                      padding: '16px',
                      color: '#121212',
                      fontWeight: '500',
                      textAlign: 'center'
                    }}>{s.totalSlots}</td>
                    <td style={{ 
                      padding: '16px',
                      color: '#121212',
                      fontWeight: '600'
                    }}>
                      ${s.pricePerHour?.toFixed(2) || '0.00'}
                    </td>
                    <td style={{ padding: '16px' }}>
                      {renderStatusBadge(s.status)}
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
                        {s.status === 0 && (
                          <Button
                            variant="outline-secondary"
                            size="sm"
                            onClick={() => handleChangeStatus(s, 1)}
                            style={{
                              border: '2px solid #6C757D',
                              color: '#6C757D',
                              borderRadius: '8px',
                              fontWeight: '500',
                              padding: '6px 12px'
                            }}
                          >
                            ⏸️ Deactivate
                          </Button>
                        )}
                        {(s.status === 1 || s.status === 2) && (
                          <Button
                            variant="outline-success"
                            size="sm"
                            onClick={() => handleChangeStatus(s, 0)}
                            style={{
                              border: '2px solid #00C853',
                              color: '#00C853',
                              borderRadius: '8px',
                              fontWeight: '500',
                              padding: '6px 12px'
                            }}
                          >
                            ▶️ Activate
                          </Button>
                        )}
                        <Button
                          variant="outline-warning"
                          size="sm"
                          onClick={() => handleOpenModal(s)}
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
                          variant="outline-danger"
                          size="sm"
                          onClick={() => handleDelete(s.id)}
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
                    <div style={{ fontSize: '3rem', marginBottom: '16px' }}>🏢</div>
                    <h4 style={{ color: '#121212', marginBottom: '8px' }}>No Stations Found</h4>
                    <p>Get started by adding your first charging station.</p>
                    <Button 
                      onClick={() => handleOpenModal()}
                      style={{
                        background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
                        border: 'none',
                        borderRadius: '8px',
                        fontWeight: '500',
                        padding: '10px 20px',
                        marginTop: '16px'
                      }}
                    >
                      ➕ Add First Station
                    </Button>
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        )}
      </div>

      {/* Add/Edit Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)} centered>
        <Modal.Header style={{ 
          background: 'linear-gradient(135deg, #1B263B 0%, #121212 100%)',
          color: '#F9FAFB',
          border: 'none'
        }}>
          <Modal.Title style={{ fontWeight: '600' }}>
            {editMode ? "✏️ Edit Station" : "➕ Add New Station"}
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
          <Form>
            <Form.Group className="mb-3">
              <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Station Name</Form.Label>
              <Form.Control 
                value={name} 
                onChange={(e) => setName(e.target.value)}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  border: '2px solid #E5E7EB',
                  background: '#FFFFFF'
                }}
                placeholder="Enter station name"
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Location</Form.Label>
              <Form.Control 
                value={location} 
                onChange={(e) => setLocation(e.target.value)}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  border: '2px solid #E5E7EB',
                  background: '#FFFFFF'
                }}
                placeholder="Enter station location"
              />
            </Form.Group>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <Form.Group className="mb-3">
                <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Charger Type</Form.Label>
                <Form.Select 
                  value={type} 
                  onChange={(e) => setType(e.target.value)}
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    border: '2px solid #E5E7EB',
                    background: '#FFFFFF'
                  }}
                >
                  <option value="AC">🔌 AC Charger</option>
                  <option value="DC">⚡ DC Fast Charger</option>
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Total Slots</Form.Label>
                <Form.Control 
                  type="number" 
                  min={1} 
                  max={50} 
                  value={totalSlots} 
                  onChange={(e) => setSlots(Number(e.target.value))}
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    border: '2px solid #E5E7EB',
                    background: '#FFFFFF'
                  }}
                />
              </Form.Group>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <Form.Group className="mb-3">
                <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Price Per Hour ($)</Form.Label>
                <Form.Control 
                  type="number" 
                  step={0.01} 
                  min={0.01} 
                  max={1000} 
                  value={pricePerHour} 
                  onChange={(e) => setPricePerHour(Number(e.target.value))}
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
                <Form.Select
                  value={status}
                  onChange={(e) => setStatus(Number(e.target.value))}
                  disabled={editMode}
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    border: '2px solid #E5E7EB',
                    background: '#FFFFFF'
                  }}
                >
                  <option value={0}>⚡ Active</option>
                  <option value={1}>⏸️ Inactive</option>
                  <option value={2}>🔧 Maintenance</option>
                </Form.Select>
              </Form.Group>
            </div>
          </Form>
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
            Cancel
          </Button>
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
            {editMode ? "💾 Save Changes" : "➕ Add Station"}
          </Button>
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