import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

const API_URL = 'http://localhost:5000/api';

function DanieForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = !!id;

  const [formData, setFormData] = useState({
    nazwa_dania: '',
    opis: '',
    sposob_przygotowania: '',
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEditing) {
      fetchDanieDetails();
    }
  }, [isEditing, id]);

  const fetchDanieDetails = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/dania/${id}`);
      setFormData({
        nazwa_dania: response.data.nazwa_dania,
        opis: response.data.opis,
        sposob_przygotowania: response.data.sposob_przygotowania,
      });
    } catch (err) {
      setError('Błąd podczas pobierania szczegółów przepisu');
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      if (isEditing) {
        await axios.put(`${API_URL}/dania/${id}`, formData);
      } else {
        await axios.post(`${API_URL}/dania`, formData);
      }
      navigate('/dania');
    } catch (err) {
      setError(`Błąd podczas ${isEditing ? 'aktualizacji' : 'dodawania'} przepisu`);
      console.error('Błąd:', err);
      setLoading(false);
    }
  };

  if (loading && isEditing) return <div className="text-center">Ładowanie...</div>;

  return (
    <div>
      <h2>{isEditing ? 'Edytuj Przepis' : 'Dodaj Nowy Przepis'}</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="nazwa_dania" className="form-label">Nazwa Przepisu</label>
          <input
            type="text"
            className="form-control"
            id="nazwa_dania"
            name="nazwa_dania"
            value={formData.nazwa_dania}
            onChange={handleChange}
            required
          />
        </div>
        <div className="mb-3">
          <label htmlFor="opis" className="form-label">Opis</label>
          <textarea
            className="form-control"
            id="opis"
            name="opis"
            value={formData.opis}
            onChange={handleChange}
            rows="3"
          ></textarea>
        </div>
        <div className="mb-3">
          <label htmlFor="sposob_przygotowania" className="form-label">Sposób Przygotowania</label>
          <textarea
            className="form-control"
            id="sposob_przygotowania"
            name="sposob_przygotowania"
            value={formData.sposob_przygotowania}
            onChange={handleChange}
            rows="5"
            required
          ></textarea>
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Zapisywanie...' : 'Zapisz'}
        </button>
        <Link to="/dania" className="btn btn-secondary ms-2">
          Anuluj
        </Link>
      </form>
    </div>
  );
}

export default DanieForm;