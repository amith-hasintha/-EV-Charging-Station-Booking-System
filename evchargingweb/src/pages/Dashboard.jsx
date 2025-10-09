//Dashboard.jsx
import { Container, Row, Col } from "react-bootstrap";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { Outlet } from "react-router-dom";

export default function Dashboard() {
  // --- Main Dashboard Layout ---
  // Renders Header, Sidebar, Footer, and a content area for nested routes (Outlet)
  return (
    <div className="d-flex flex-column vh-100">
      <Header />
      <Container fluid className="flex-grow-1">
        <Row className="h-100">
          <Col xs={2} className="p-0">
            <Sidebar />
          </Col>
          <Col xs={10} className="p-4 overflow-auto">
            <Outlet />
          </Col>
        </Row>
      </Container>
      <Footer />
    </div>
  );
}
