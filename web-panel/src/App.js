import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Dania from './pages/Dania';
import DanieForm from './pages/DanieForm';
import DanieDetails from './pages/DanieDetails';
import Skladniki from './pages/Skladniki';
import SkladnikForm from './pages/SkladnikForm';

function App() {
  return (
    <Router>
      <div className="container py-4">
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
            </div>
          </div>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/dania" element={<Dania />} />
          <Route path="/dania/new" element={<DanieForm />} />
          <Route path="/dania/:id" element={<DanieDetails />} />
          <Route path="/dania/:id/edit" element={<DanieForm />} />
          <Route path="/skladniki" element={<Skladniki />} />
          <Route path="/skladniki/new" element={<SkladnikForm />} />
          <Route path="/skladniki/:id/edit" element={<SkladnikForm />} />
        </Routes>
      </div>
    </Router>
  );
}

function Home() {
  return (
    <div className="jumbotron">
      <h1 className="display-4">System Zarządzania Przepisami</h1>
      <p className="lead">
        Witaj w panelu administracyjnym do zarządzania przepisami i składnikami.
      </p>
      <hr className="my-4" />
      <div className="row">
        <div className="col-md-6 mb-4">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Zarządzanie Przepisami</h5>
              <p className="card-text">
                Dodawaj, edytuj i usuwaj przepisy oraz zarządzaj ich składnikami.
              </p>
              <Link to="/dania" className="btn btn-primary">
                Przejdź do Przepisów
              </Link>
            </div>
          </div>
        </div>
        <div className="col-md-6 mb-4">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Zarządzanie Składnikami</h5>
              <p className="card-text">
                Dodawaj, edytuj i usuwaj składniki oraz ich wartości odżywcze.
              </p>
              <Link to="/skladniki" className="btn btn-primary">
                Przejdź do Składników
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;