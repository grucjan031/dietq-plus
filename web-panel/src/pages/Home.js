import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Home() {
  const { currentUser } = useAuth();

  return (
    <div className="jumbotron">
      <h1 className="display-4">System Zarządzania Przepisami</h1>
      <p className="lead">
        Witaj, <strong>{currentUser?.username}</strong>, w panelu administracyjnym do zarządzania przepisami i składnikami.
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

export default Home;