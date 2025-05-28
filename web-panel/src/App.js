import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Home from './pages/Home';
import Dania from './pages/Dania';
import DanieForm from './pages/DanieForm';
import DanieDetails from './pages/DanieDetails';
import Skladniki from './pages/Skladniki';
import SkladnikForm from './pages/SkladnikForm';
import SessionExpiredModal from './components/SessionExpiredModal';

// Konfiguracja API URL dla całej aplikacji
import axios from 'axios';
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api';
axios.defaults.baseURL = API_URL;

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

function AppContent() {
  return (
    <>
      <SessionExpiredModal />
      <Routes>
        {/* Publiczne ścieżki */}
        <Route path="/login" element={<Login />} />
        
        {/* Zabezpieczone ścieżki */}
        <Route element={<PrivateRoute />}>
          <Route path="/" element={<MainLayout><Home /></MainLayout>} />
          <Route path="/dania" element={<MainLayout><Dania /></MainLayout>} />
          <Route path="/dania/new" element={<MainLayout><DanieForm /></MainLayout>} />
          <Route path="/dania/:id" element={<MainLayout><DanieDetails /></MainLayout>} />
          <Route path="/dania/:id/edit" element={<MainLayout><DanieForm /></MainLayout>} />
          <Route path="/skladniki" element={<MainLayout><Skladniki /></MainLayout>} />
          <Route path="/skladniki/new" element={<MainLayout><SkladnikForm /></MainLayout>} />
          <Route path="/skladniki/:id/edit" element={<MainLayout><SkladnikForm /></MainLayout>} />
        </Route>
        
        {/* Przekierowanie dla nieobsługiwanych ścieżek */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

// Wrapper dla układu strony z paskiem nawigacyjnym
function MainLayout({ children }) {
  return (
    <div>
      <Navbar />
      <div className="container py-4">
        {children}
      </div>
    </div>
  );
}

export default App;