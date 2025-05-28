import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
      <div className="container-fluid">
        <Link className="navbar-brand" to="/">
          Panel Administratora Przepisów
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav">
            <li className="nav-item">
              <Link className="nav-link" to="/dania">
                Przepisy
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/skladniki">
                Składniki
              </Link>
            </li>
          </ul>
          <ul className="navbar-nav ms-auto">
            <li className="nav-item dropdown">
              <a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                {currentUser?.username || 'Użytkownik'}
              </a>
              <ul className="dropdown-menu dropdown-menu-end">
                <li>
                  <button className="dropdown-item" onClick={handleLogout}>
                    Wyloguj się
                  </button>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;