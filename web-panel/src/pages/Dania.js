import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

const API_URL = 'http://localhost:5000/api';

function Dania() {
  const [dania, setDania] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDania();
  }, []);

  const fetchDania = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/dania`);
      setDania(response.data);
      setError(null);
    } catch (err) {
      setError('Błąd podczas pobierania przepisów');
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Czy na pewno chcesz usunąć ten przepis?')) {
      try {
        await axios.delete(`${API_URL}/dania/${id}`);
        fetchDania();
      } catch (err) {
        setError('Błąd podczas usuwania przepisu');
        console.error('Błąd:', err);
      }
    }
  };

  if (loading) return <div className="text-center">Ładowanie...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Przepisy</h2>
        <Link to="/dania/new" className="btn btn-primary">
          Dodaj Przepis
        </Link>
      </div>

      {dania.length === 0 ? (
        <div className="alert alert-info">Brak przepisów w bazie danych.</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nazwa</th>
                <th>Opis</th>
                <th>Akcje</th>
              </tr>
            </thead>
            <tbody>
              {dania.map((danie) => (
                <tr key={danie.id}>
                  <td>{danie.id}</td>
                  <td>
                    <div className="d-flex align-items-center">
                      <img
                        src={danie.ma_zdjecie 
                          ? `${API_URL}/photos/dish_${danie.id}.jpg?${new Date().getTime()}` 
                          : `${API_URL}/photos/no-image.jpg`}
                        alt={danie.nazwa_dania}
                        className="me-2 rounded"
                        style={{ width: '50px', height: '50px', objectFit: 'cover' }}
                      />
                      {danie.nazwa_dania}
                    </div>
                  </td>
                  <td>
                    {danie.opis && danie.opis.length > 100
                      ? `${danie.opis.substring(0, 100)}...`
                      : danie.opis}
                  </td>
                  <td>
                    <div className="btn-group">
                      <Link
                        to={`/dania/${danie.id}`}
                        className="btn btn-sm btn-info me-1"
                      >
                        Szczegóły
                      </Link>
                      <Link
                        to={`/dania/${danie.id}/edit`}
                        className="btn btn-sm btn-warning me-1"
                      >
                        Edytuj
                      </Link>
                      <button
                        onClick={() => handleDelete(danie.id)}
                        className="btn btn-sm btn-danger"
                      >
                        Usuń
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default Dania;