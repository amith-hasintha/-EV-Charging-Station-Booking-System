import { Container } from "react-bootstrap";

export default function Footer() {
  return (
    <footer style={{
      background: 'linear-gradient(135deg, #1B263B 0%, #121212 100%)',
      color: '#F9FAFB',
      textAlign: 'center',
      padding: '20px 0',
      borderTop: '1px solid rgba(255, 255, 255, 0.1)',
      marginTop: 'auto'
    }}>
      <Container>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          gap: '8px',
          marginBottom: '8px'
        }}>
          <div style={{
            fontSize: '1.2rem',
            background: 'linear-gradient(135deg, #00C853 0%, #00B4D8 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text'
          }}>
            ⚡
          </div>
          <span style={{ fontWeight: '600' }}>
            EV Charge
          </span>
        </div>
        <p style={{ 
          margin: 0, 
          color: '#9CA3AF',
          fontSize: '0.9rem'
        }}>
          &copy; {new Date().getFullYear()} EV Charging Station Booking System
        </p>
        <p style={{ 
          margin: '4px 0 0 0', 
          color: '#6C757D',
          fontSize: '0.8rem'
        }}>
          Power Your Journey
        </p>
      </Container>
    </footer>
  );
}