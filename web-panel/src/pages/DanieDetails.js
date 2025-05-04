import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const API_URL = 'http://localhost:5000/api';


function DanieDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [photoUrl, setPhotoUrl] = useState('');
  const [danie, setDanie] = useState(null);
  const [skladniki, setSkladniki] = useState([]);
  const [dostepneSkladniki, setDostepneSkladniki] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [newSkladnik, setNewSkladnik] = useState({
    skladnik_id: '',
    ilosc: '',
    jednostka: 'g'
  });

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Pobierz szczegóły przepisu
      const danieResponse = await axios.get(`${API_URL}/dania/${id}`);
      setDanie(danieResponse.data);
      
      // Jeśli przepis ma zdjęcie, ustaw URL
      if (danieResponse.data.ma_zdjecie) {
        setPhotoUrl(`${API_URL}/photos/dish_${id}.jpg?${new Date().getTime()}`);
      } else {
        setPhotoUrl(`${API_URL}/photos/no-image.jpg`);
      }
      
      // Pobierz składniki tego przepisu
      if (danieResponse.data.skladniki) {
        setSkladniki(danieResponse.data.skladniki);
      }
      
      // Pobierz wszystkie dostępne składniki
      const skladnikiResponse = await axios.get(`${API_URL}/skladniki`);
      setDostepneSkladniki(skladnikiResponse.data);
      
      setError(null);
    } catch (err) {
      setError('Błąd podczas pobierania danych');
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Czy na pewno chcesz usunąć ten przepis?')) {
      try {
        await axios.delete(`${API_URL}/dania/${id}`);
        navigate('/dania');
      } catch (err) {
        setError('Błąd podczas usuwania przepisu');
        console.error('Błąd:', err);
      }
    }
  };

  const handleAddSkladnik = async (e) => {
    e.preventDefault();
    if (!newSkladnik.skladnik_id || !newSkladnik.ilosc) {
      alert('Wybierz składnik i podaj ilość');
      return;
    }

    try {
      await axios.post(`${API_URL}/dania-skladniki`, {
        danie_id: id,
        ...newSkladnik
      });

      // Reset form i odśwież dane
      setNewSkladnik({
        skladnik_id: '',
        ilosc: '',
        jednostka: 'g'
      });
      fetchData();
    } catch (err) {
      setError('Błąd podczas dodawania składnika');
      console.error('Błąd:', err);
    }
  };

  const handleRemoveSkladnik = async (skladnikId) => {
    try {
      await axios.delete(`${API_URL}/dania-skladniki/${skladnikId}`);
      fetchData();
    } catch (err) {
      setError('Błąd podczas usuwania składnika');
      console.error('Błąd:', err);
    }
  };

  const handleSkladnikChange = (e) => {
    const { name, value } = e.target;
    setNewSkladnik({
      ...newSkladnik,
      [name]: name === 'ilosc' ? parseFloat(value) : value
    });
  };

  if (loading) return <div className="text-center">Ładowanie...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!danie) return <div className="alert alert-warning">Przepis nie znaleziony</div>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>{danie.nazwa_dania}</h2>
        <div>
          <Link to={`/dania/${id}/edit`} className="btn btn-warning me-2">
            Edytuj Przepis
          </Link>
          <button
            onClick={handleDelete}
            className="btn btn-danger"
          >
            Usuń Przepis
          </button>
        </div>
      </div>

      <div className="row mb-4">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Informacje o przepisie</h5>
              {photoUrl && (
                <div className="text-center my-3">
                  <img
                    src={photoUrl}
                    alt={danie.nazwa_dania}
                    className="img-fluid rounded"
                    style={{ maxHeight: '300px' }}
                  />
                </div>
              )}
              <div className="mb-3">
                <h6>Opis:</h6>
                <p>{danie.opis || 'Brak opisu'}</p>
              </div>
              <div>
                <h6>Sposób przygotowania:</h6>
                <p style={{ whiteSpace: 'pre-line' }}>
                  {danie.sposob_przygotowania || 'Brak instrukcji'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <h4>Składniki</h4>
      <div className="row">
        <div className="col-md-6 mb-4">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Lista składników</h5>
              {skladniki.length === 0 ? (
                <p>Ten przepis nie ma jeszcze żadnych składników</p>
              ) : (
                <table className="table table-sm">
                  <thead>
                    <tr>
                      <th>Składnik</th>
                      <th>Ilość</th>
                      <th>Akcje</th>
                    </tr>
                  </thead>
                  <tbody>
                    {skladniki.map((s) => (
                      <tr key={s.id}>
                        <td>{s.nazwa_skladnika}</td>
                        <td>
                          {s.ilosc} {s.jednostka}
                        </td>
                        <td>
                          <button
                            onClick={() => handleRemoveSkladnik(s.id)}
                            className="btn btn-sm btn-danger"
                          >
                            Usuń
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Dodaj składnik</h5>
              <form onSubmit={handleAddSkladnik}>
                <div className="mb-3">
                  <label htmlFor="skladnik_id" className="form-label">
                    Wybierz składnik
                  </label>
                  <select
                    id="skladnik_id"
                    name="skladnik_id"
                    className="form-select"
                    value={newSkladnik.skladnik_id}
                    onChange={handleSkladnikChange}
                    required
                  >
                    <option value="">-- Wybierz składnik --</option>
                    {dostepneSkladniki.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.nazwa_skladnika}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label htmlFor="ilosc" className="form-label">
                      Ilość
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      id="ilosc"
                      name="ilosc"
                      className="form-control"
                      value={newSkladnik.ilosc}
                      onChange={handleSkladnikChange}
                      required
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label htmlFor="jednostka" className="form-label">
                      Jednostka
                    </label>
                    <select
                      id="jednostka"
                      name="jednostka"
                      className="form-select"
                      value={newSkladnik.jednostka}
                      onChange={handleSkladnikChange}
                    >
                      <option value="g">g</option>
                      <option value="kg">kg</option>
                      <option value="ml">ml</option>
                      <option value="l">l</option>
                      <option value="szt.">szt.</option>
                      <option value="łyżka">łyżka</option>
                      <option value="łyżeczka">łyżeczka</option>
                      <option value="szklanka">szklanka</option>
                    </select>
                  </div>
                </div>
                <button type="submit" className="btn btn-primary">
                  Dodaj składnik
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-3">
        <Link to="/dania" className="btn btn-secondary">
          Powrót do listy przepisów
        </Link>
      </div>
    </div>
  );
}

export default DanieDetails;