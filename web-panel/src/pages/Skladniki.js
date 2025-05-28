import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

const API_URL = 'http://localhost:5000/api';

function Skladniki() {
  const [skladniki, setSkladniki] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchSkladniki();
  }, []);

  const fetchSkladniki = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/skladniki`);
      setSkladniki(response.data);
      setError(null);
    } catch (err) {
      setError('Błąd podczas pobierania składników');
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Czy na pewno chcesz usunąć ten składnik?')) {
      try {
        await axios.delete(`${API_URL}/skladniki/${id}`);
        fetchSkladniki();
      } catch (err) {
        setError('Błąd podczas usuwania składnika');
        console.error('Błąd:', err);
      }
    }
  };

  if (loading) return <div className="text-center">Ładowanie...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Składniki</h2>
        <Link to="/skladniki/new" className="btn btn-primary">
          Dodaj Składnik
        </Link>
      </div>
      
      {skladniki.length === 0 ? (
        <div className="alert alert-info">Brak składników w bazie danych.</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nazwa</th>
                <th>Kalorie (kcal)</th>
                <th>Białko (g)</th>
                <th>Węglowodany (g)</th>
                <th>Tłuszcze (g)</th>
                <th>Akcje</th>
              </tr>
            </thead>
            <tbody>
              {skladniki.map((skladnik) => (
                <tr key={skladnik.id}>
                  <td>{skladnik.id}</td>
                  <td>{skladnik.nazwa_skladnika}</td>
                  <td>{skladnik.kcal}</td>
                  <td>{skladnik.bialko}</td>
                  <td>{skladnik.weglowodany}</td>
                  <td>{skladnik.tluszcze}</td>
                  <td>
                    <div className="btn-group">
                      <Link 
                        to={`/skladniki/${skladnik.id}/edit`} 
                        className="btn btn-sm btn-warning me-1"
                      >
                        Edytuj
                      </Link>
                      <button 
                        onClick={() => handleDelete(skladnik.id)} 
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

export default Skladniki;