import { useEffect, useState } from "react";
import { Table, Button, Form, Row, Col, Alert } from "react-bootstrap";

export default function OperatorBookings() {
  const [bookings, setBookings] = useState([]);
  const [filteredBookings, setFilteredBookings] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

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
        (b) => statusMap[b.status]?.toLowerCase() === statusFilter.toLowerCase()
      );
    }

    if (dateFrom) {
      filtered = filtered.filter((b) => new Date(b.startTime) >= new Date(dateFrom));
    }

    if (dateTo) {
      filtered = filtered.filter((b) => new Date(b.startTime) <= new Date(dateTo));
    }

    setFilteredBookings(filtered);
  }, [statusFilter, dateFrom, dateTo, bookings]);

  // Confirm booking
  const handleConfirm = async (id) => {
    if (!window.confirm("Confirm this booking?")) return;
    try {
      const res = await fetch(`http://localhost:5082/api/bookings/${id}/confirm`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!res.ok) throw new Error("Failed to confirm booking");
      setSuccess("Booking confirmed successfully");
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
        return <span className="badge bg-info text-dark">Active</span>;
      case "confirmed":
        return <span className="badge bg-success">Confirmed</span>;
      case "completed":
        return <span className="badge bg-primary">Completed</span>;
      case "cancelled":
        return <span className="badge bg-secondary">Cancelled</span>;
      case "noshow":
        return <span className="badge bg-danger">No Show</span>;
      default:
        return <span className="badge bg-light text-dark">{statusStr}</span>;
    }
  };

  return (
    <div>
      <h3 className="mb-4">📅 Station Bookings</h3>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      {/* Filters */}
      <Form className="mb-4">
        <Row className="align-items-end">
          <Col md={3}>
            <Form.Label>Status</Form.Label>
            <Form.Select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All</option>
              <option value="Active">Active</option>
              <option value="Confirmed">Confirmed</option>
              <option value="Completed">Completed</option>
              <option value="Cancelled">Cancelled</option>
              <option value="NoShow">No Show</option>
            </Form.Select>
          </Col>

          <Col md={3}>
            <Form.Label>From</Form.Label>
            <Form.Control
              type="date"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
            />
          </Col>

          <Col md={3}>
            <Form.Label>To</Form.Label>
            <Form.Control
              type="date"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
            />
          </Col>

          <Col md={3}>
            <Button
              className="w-100"
              variant="primary"
              onClick={fetchBookings}
              disabled={loading}
            >
              {loading ? "Refreshing..." : "🔄 Refresh"}
            </Button>
          </Col>
        </Row>
      </Form>

      {/* Table */}
      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>ID</th>
            <th>User NIC</th>
            <th>Start Time</th>
            <th>End Time</th>
            <th>Total Amount</th>
            <th>Status</th>
            <th>QR Code</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filteredBookings.length > 0 ? (
            filteredBookings.map((b) => (
              <tr key={b._id}>
                <td>{b._id}</td>
                <td>{b.ownerNIC}</td>
                <td>{new Date(b.startTime).toLocaleString("en-GB", { dateStyle: "medium", timeStyle: "short" })}</td>
                <td>{new Date(b.endTime).toLocaleString("en-GB", { dateStyle: "medium", timeStyle: "short" })}</td>
                <td>{b.totalAmount.toFixed(2)}</td>
                <td>{renderStatusBadge(b.status)}</td>
                <td>{b.qrCode}</td>
                <td>
                {b.status === 0 && (
                  <Button
                    size="sm"
                    variant="success"
                    onClick={() => handleConfirm(b.id)}
                  >
                    ✅ Confirm
                  </Button>
                )}
              </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="8" className="text-center">
                No bookings found.
              </td>
            </tr>
          )}
        </tbody>
      </Table>
    </div>
  );
}
