import { Navbar, Container } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

export default function Header() {
  const navigate = useNavigate();

  return (
    <Navbar style={{ 
      background: 'linear-gradient(135deg, #1B263B 0%, #121212 100%)',
      borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
    }} variant="dark" expand="lg" className="px-3">
      <Container fluid>
        <Navbar.Brand 
          href="/" 
          style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '12px',
            cursor: 'pointer',
            fontWeight: '700',
            fontSize: '1.5rem'
          }}
          onClick={(e) => {
            e.preventDefault();
            navigate('/');
          }}
        >
          <div style={{ 
            fontSize: '1.8rem',
            background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text'
          }}>
            ⚡
          </div>
          EV Charge - Backoffice
        </Navbar.Brand>
      </Container>
    </Navbar>
  );
}