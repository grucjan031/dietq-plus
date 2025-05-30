import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

const API_URL = 'http://localhost:5000/api';

function SkladnikForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = !!id;

  const [formData, setFormData] = useState({
    nazwa_skladnika: '',
    kcal: 0,
    bialko: 0,
    weglowodany: 0,
    tluszcze: 0
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEditing) {
      fetchSkladnikDetails();
    }
  }, [isEditing, id]);

  const fetchSkladnikDetails = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/skladniki/${id}`);
      setFormData({
        nazwa_skladnika: response.data.nazwa_skladnika,
        kcal: response.data.kcal,
        bialko: response.data.bialko,
        weglowodany: response.data.weglowodany,
        tluszcze: response.data.tluszcze
      });
    } catch (err) {
      setError('Błąd podczas pobierania szczegółów składnika');
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ 
      ...formData, 
      [name]: name === 'nazwa_skladnika' ? value : parseInt(value, 10) 
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      if (isEditing) {
        await axios.put(`${API_URL}/skladniki/${id}`, formData);
      } else {
        await axios.post(`${API_URL}/skladniki`, formData);
      }
      navigate('/skladniki');
    } catch (err) {
      setError(`Błąd podczas ${isEditing ? 'aktualizacji' : 'dodawania'} składnika`);
      console.error('Błąd:', err);
      setLoading(false);
    }
  };

  if (loading && isEditing) return <div className="text-center">Ładowanie...</div>;

  return (
    <div>
      <h2>{isEditing ? 'Edytuj Składnik' : 'Dodaj Nowy Składnik'}</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="nazwa_skladnika" className="form-label">Nazwa Składnika</label>
          <input
            type="text"
            className="form-control"
            id="nazwa_skladnika"
            name="nazwa_skladnika"
            value={formData.nazwa_skladnika}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="row">
          <div className="col-md-3 mb-3">
            <label htmlFor="kcal" className="form-label">Kalorie (kcal)</label>
            <input
              type="number"
              min="0"
              className="form-control"
              id="kcal"
              name="kcal"
              value={formData.kcal}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="col-md-3 mb-3">
            <label htmlFor="bialko" className="form-label">Białko (g)</label>
            <input
              type="number"
              min="0"
              className="form-control"
              id="bialko"
              name="bialko"
              value={formData.bialko}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="col-md-3 mb-3">
            <label htmlFor="weglowodany" className="form-label">Węglowodany (g)</label>
            <input
              type="number"
              min="0"
              className="form-control"
              id="weglowodany"
              name="weglowodany"
              value={formData.weglowodany}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="col-md-3 mb-3">
            <label htmlFor="tluszcze" className="form-label">Tłuszcze (g)</label>
            <input
              type="number"
              min="0"
              className="form-control"
              id="tluszcze"
              name="tluszcze"
              value={formData.tluszcze}
              onChange={handleChange}
              required
            />
          </div>
        </div>
        
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Zapisywanie...' : 'Zapisz'}
        </button>
        <Link to="/skladniki" className="btn btn-secondary ms-2">
          Anuluj
        </Link>
      </form>
    </div>
  );
}

export default SkladnikForm;