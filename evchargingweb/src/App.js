import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Users from "./pages/Users";
import EVOwnerManagement from "./pages/EVOwnerManagement";
import ChargingStations from "./pages/ChargingStations";
import StationDashboard from "./pages/StationDashboard";
import OperatorBookings from "./pages/OperatorBookings";
import StationProfile from "./pages/StationProfile";
import SlotCalendar from "./pages/SlotCalendar";


function App() {
  return (
    <Router>
      <Routes>
        {/* Auth Pages */}
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Backoffice Dashboard (Admin) */}
        <Route path="/dashboard" element={<Dashboard />}>
          <Route path="users" element={<Users />} />
          <Route path="ev-owners" element={<EVOwnerManagement />} />
          <Route path="stations" element={<ChargingStations />} />
        </Route>

        {/* ✅ Station Dashboard (for operators) */}
      

<Route path="/station-dashboard" element={<StationDashboard />}>
  <Route path="bookings" element={<OperatorBookings />} />
  <Route path="slot-calendar" element={<SlotCalendar />} />
  <Route path="profile" element={<StationProfile />} />
</Route>
      </Routes>
    </Router>
  );
}

export default App;
