import { useEffect, useState } from "react";
import { Table, Button, Form, Row, Col, Alert, Modal } from "react-bootstrap";

export default function OperatorBookings() {
  const [bookings, setBookings] = useState([]);
  const [filteredBookings, setFilteredBookings] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const [selectedBookingId, setSelectedBookingId] = useState(null);

  const token = localStorage.getItem("token");
  const stationId = localStorage.getItem("stationId");

  const statusMap = {
    0: "Active",
    1: "Confirmed",
    2: "Completed",
    3: "Cancelled",
    4: "NoShow",
  };

  const fetchBookings = async () => {
    if (!stationId) {
      setError("Station ID is missing");
      return;
    }

    setLoading(true);
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
      if (!res.ok) throw new Error("Failed to fetch bookings");
      const data = await res.json();
      setBookings(data);
      setFilteredBookings(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, [stationId]);

  // Filter bookings
  useEffect(() => {
    let filtered = [...bookings];

    if (statusFilter) {
      filtered = filtered.filter(
        (b) =>
          statusMap[b.status]?.toLowerCase() === statusFilter.toLowerCase()
      );
    }

    if (dateFrom) {
      filtered = filtered.filter(
        (b) => new Date(b.startTime) >= new Date(dateFrom)
      );
    }

    if (dateTo) {
      filtered = filtered.filter(
        (b) => new Date(b.startTime) <= new Date(dateTo)
      );
    }

    setFilteredBookings(filtered);
  }, [statusFilter, dateFrom, dateTo, bookings]);

  // Confirm booking
  const handleConfirm = async (booking) => {
    const bookingId = booking.id;

    if (!window.confirm("Confirm this booking?")) return;

    try {
      const res = await fetch(
        `http://localhost:5082/api/bookings/${bookingId}/confirm`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!res.ok) throw new Error("Failed to confirm booking");

      // Send notification
      const notifRes = await fetch(`http://localhost:5082/api/notifications`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: "Booking Confirmed",
          message: "Your booking has been confirmed by the operator",
          recipientNIC: booking.ownerNIC,
          type: 0,
          relatedEntityId: booking.id,
          relatedEntityType: "Booking"
        })
      });

      if (!notifRes.ok) {
        const errData = await notifRes.json();
        throw new Error(errData?.title || "Failed to send notification");
      }

      setSuccess("Booking confirmed and notification sent successfully");
      setError("");
      fetchBookings();
    } catch (err) {
      setError(err.message);
    }
  };

  // Cancel booking (open modal)
  const handleCancelClick = (id) => {
    setSelectedBookingId(id);
    setCancelReason("");
    setShowCancelModal(true);
  };

  // Confirm cancel booking
  const handleCancelBooking = async () => {
    if (!cancelReason.trim()) {
      setError("Please enter a reason for cancellation");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:5082/api/bookings/${selectedBookingId}/cancel-by-operator`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ reason: cancelReason }),
        }
      );

      if (!res.ok) throw new Error("Failed to cancel booking");

      const booking = bookings.find((b) => b.id === selectedBookingId);
      if (!booking) throw new Error("Booking not found for notification");

      // Send cancellation notification
      const notifRes = await fetch(`http://localhost:5082/api/notifications`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: "Booking Cancelled",
          message: `Your booking at ${booking.stationName} has been cancelled. Reason: ${cancelReason}`,
          recipientNIC: booking.ownerNIC,
          type: 1,
          relatedEntityId: booking.id,
          relatedEntityType: "Booking",
        }),
      });

      if (!notifRes.ok) {
        const errData = await notifRes.json();
        throw new Error(errData?.title || "Failed to send notification");
      }

      setSuccess("Booking cancelled and notification sent successfully");
      setError("");
      setShowCancelModal(false);
      fetchBookings();
    } catch (err) {
      setError(err.message);
    }
  };

  // Render booking status badge
  const renderStatusBadge = (status) => {
    const statusStr = statusMap[status] || "Unknown";
    switch (statusStr.toLowerCase()) {
      case "active":
        return (
          <span style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '500',
            background: 'rgba(0, 200, 83, 0.1)',
            color: '#00C853'
          }}>
            ⏳ Pending
          </span>
        );
      case "confirmed":
        return (
          <span style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '500',
            background: 'rgba(0, 123, 255, 0.1)',
            color: '#007BFF'
          }}>
            ✅ Confirmed
          </span>
        );
      case "completed":
        return (
          <span style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '500',
            background: 'rgba(40, 167, 69, 0.1)',
            color: '#28A745'
          }}>
            🏁 Completed
          </span>
        );
      case "cancelled":
        return (
          <span style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '500',
            background: 'rgba(108, 117, 125, 0.1)',
            color: '#6C757D'
          }}>
            ❌ Cancelled
          </span>
        );
      case "noshow":
        return (
          <span style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '500',
            background: 'rgba(220, 53, 69, 0.1)',
            color: '#DC3545'
          }}>
            🚫 No Show
          </span>
        );
      default:
        return (
          <span style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '500',
            background: 'rgba(255, 193, 7, 0.1)',
            color: '#FFC107'
          }}>
            ❓ Unknown
          </span>
        );
    }
  };

  // Clear messages after 5 seconds
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
          📅
        </div>
        <div>
          <h1 style={{ 
            color: '#000000ff', 
            margin: 0, 
            fontWeight: '700',
            fontSize: '2rem'
          }}>
            Station Bookings
          </h1>
          <p style={{ 
            color: '#9CA3AF', 
            margin: 0,
            fontSize: '1.1rem'
          }}>
            Manage and monitor charging station bookings
          </p>
        </div>
      </div>

      {/* Alerts */}
      {error && (
        <div style={{
          background: 'rgba(248, 247, 245, 0.1)',
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

      {/* Filters Card */}
      <div style={{
        background: '#F9FAFB',
        borderRadius: '20px',
        padding: '24px',
        boxShadow: '0 10px 40px rgba(0, 0, 0, 0.1)',
        border: '1px solid rgba(255, 255, 255, 0.1)',
        marginBottom: '24px'
      }}>
        <Form>
          <Row className="align-items-end">
            <Col md={3}>
              <Form.Label style={{ fontWeight: '600', color: '#121212' }}>Status</Form.Label>
              <Form.Select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  border: '2px solid #E5E7EB',
                  background: '#FFFFFF'
                }}
              >
                <option value="">All Status</option>
                <option value="Active">⏳ Pending</option>
                <option value="Confirmed">✅ Confirmed</option>
                <option value="Completed">🏁 Completed</option>
                <option value="Cancelled">❌ Cancelled</option>
                <option value="NoShow">🚫 No Show</option>
              </Form.Select>
            </Col>

            <Col md={3}>
              <Form.Label style={{ fontWeight: '600', color: '#121212' }}>From Date</Form.Label>
              <Form.Control
                type="date"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  border: '2px solid #E5E7EB',
                  background: '#FFFFFF'
                }}
              />
            </Col>

            <Col md={3}>
              <Form.Label style={{ fontWeight: '600', color: '#121212' }}>To Date</Form.Label>
              <Form.Control
                type="date"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  border: '2px solid #E5E7EB',
                  background: '#FFFFFF'
                }}
              />
            </Col>

            <Col md={3}>
              <Button
                className="w-100"
                onClick={fetchBookings}
                disabled={loading}
                style={{
                  background: loading 
                    ? 'linear-gradient(135deg, #00B4D8 0%, #0096C7 100%)' 
                    : 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
                  border: 'none',
                  borderRadius: '12px',
                  fontWeight: '600',
                  padding: '12px',
                  fontSize: '1rem'
                }}
              >
                {loading ? (
                  <>
                    <div className="spinner" style={{
                      width: '16px',
                      height: '16px',
                      border: '2px solid transparent',
                      borderTop: '2px solid white',
                      borderRadius: '50%',
                      animation: 'spin 1s linear infinite',
                      display: 'inline-block',
                      marginRight: '8px'
                    }}></div>
                    Refreshing...
                  </>
                ) : (
                  '🔄 Refresh'
                )}
              </Button>
            </Col>
          </Row>
        </Form>
      </div>

      {/* Bookings Table Card */}
      <div style={{
        background: '#F9FAFB',
        borderRadius: '20px',
        padding: '32px',
        boxShadow: '0 10px 40px rgba(0, 0, 0, 0.1)',
        border: '1px solid rgba(255, 255, 255, 0.1)'
      }}>
        {loading && filteredBookings.length === 0 ? (
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
            Loading bookings...
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
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>ID</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>User NIC</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>Start Time</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>End Time</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>Total Amount</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>QR Code</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600' }}>Status</th>
                <th style={{ padding: '16px', border: 'none', fontWeight: '600', textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredBookings.length > 0 ? (
                filteredBookings.map((b) => (
                  <tr key={b._id} style={{ 
                    borderBottom: '1px solid #E5E7EB',
                    transition: 'all 0.3s ease'
                  }}>
                    <td style={{ padding: '16px', fontWeight: '500', color: '#121212' }}>
                      {b._id?.substring(0, 8)}...
                    </td>
                    <td style={{ padding: '16px', color: '#121212' }}>{b.ownerNIC}</td>
                    <td style={{ padding: '16px', color: '#121212' }}>
                      {new Date(b.startTime).toLocaleString("en-GB", {
                        dateStyle: "medium",
                        timeStyle: "short",
                      })}
                    </td>
                    <td style={{ padding: '16px', color: '#121212' }}>
                      {new Date(b.endTime).toLocaleString("en-GB", {
                        dateStyle: "medium",
                        timeStyle: "short",
                      })}
                    </td>
                    <td style={{ padding: '16px', color: '#121212', fontWeight: '600' }}>
                      ${b.totalAmount?.toFixed(2) || '0.00'}
                    </td>
                    <td style={{ padding: '16px', color: '#121212' }}>{b.qrCode}</td>
                    <td style={{ padding: '16px' }}>
                      {renderStatusBadge(b.status)}
                    </td>
                    <td style={{ padding: '16px', textAlign: 'center' }}>
                      <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', flexWrap: 'wrap' }}>
                        {b.status === 0 && (
                          <>
                            <Button
                              size="sm"
                              onClick={() => handleConfirm(b)}
                              style={{
                                background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
                                border: 'none',
                                borderRadius: '8px',
                                fontWeight: '500',
                                padding: '6px 12px'
                              }}
                            >
                              ✅ Confirm
                            </Button>
                            <Button
                              size="sm"
                              onClick={() => handleCancelClick(b.id)}
                              style={{
                                background: 'linear-gradient(135deg, #DC3545 0%, #C82333 100%)',
                                border: 'none',
                                borderRadius: '8px',
                                fontWeight: '500',
                                padding: '6px 12px'
                              }}
                            >
                              ❌ Cancel
                            </Button>
                          </>
                        )}
                        {b.status === 1 && (
                          <Button
                            size="sm"
                            onClick={() => handleCancelClick(b.id)}
                            style={{
                              background: 'linear-gradient(135deg, #DC3545 0%, #C82333 100%)',
                              border: 'none',
                              borderRadius: '8px',
                              fontWeight: '500',
                              padding: '6px 12px'
                            }}
                          >
                            ❌ Cancel
                          </Button>
                        )}
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
                    <div style={{ fontSize: '3rem', marginBottom: '16px' }}>📅</div>
                    <h4 style={{ color: '#121212', marginBottom: '8px' }}>No Bookings Found</h4>
                    <p>No bookings match your current filters.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        )}
      </div>

      {/* Cancel Modal */}
      <Modal show={showCancelModal} onHide={() => setShowCancelModal(false)} centered>
        <Modal.Header style={{ 
          background: 'linear-gradient(135deg, #1B263B 0%, #121212 100%)',
          color: '#F9FAFB',
          border: 'none'
        }}>
          <Modal.Title style={{ fontWeight: '600' }}>
            ❌ Cancel Booking
          </Modal.Title>
          <button 
            onClick={() => setShowCancelModal(false)}
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
          <Form.Group>
            <Form.Label style={{ fontWeight: '600', color: '#121212' }}>
              Reason for Cancellation
            </Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Please provide a reason for cancellation..."
              style={{
                padding: '12px',
                borderRadius: '8px',
                border: '2px solid #E5E7EB',
                background: '#FFFFFF'
              }}
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer style={{ 
          background: '#F9FAFB', 
          border: 'none',
          padding: '16px 24px 24px'
        }}>
          <Button 
            variant="outline-secondary" 
            onClick={() => setShowCancelModal(false)}
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
          <Button 
            onClick={handleCancelBooking}
            style={{
              background: 'linear-gradient(135deg, #DC3545 0%, #C82333 100%)',
              border: 'none',
              borderRadius: '8px',
              fontWeight: '600',
              padding: '8px 24px'
            }}
          >
            🗑️ Confirm Cancel
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