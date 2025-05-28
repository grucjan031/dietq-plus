import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

const API_URL = 'http://localhost:5000/api';
const DEFAULT_IMAGE = `${API_URL}/photos/no-image.jpg`;

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
  
  // Nowy stan dla obsługi zdjęcia
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [uploadProgress, setUploadProgress] = useState(0);
  const [hasImage, setHasImage] = useState(false);
  const [photoUrl, setPhotoUrl] = useState('');

  useEffect(() => {
    if (isEditing) {
      const fetchDanie = async () => {
        try {
          setLoading(true);
          const response = await axios.get(`${API_URL}/dania/${id}`);
          const danie = response.data;
          
          setFormData({
            nazwa_dania: danie.nazwa_dania,
            opis: danie.opis,
            sposob_przygotowania: danie.sposob_przygotowania
          });
          
          if (danie.ma_zdjecie) {
            // Ustaw URL zdjęcia z timestampem, aby uniknąć problemów z cache
            setPreviewUrl(`${API_URL}/photos/dish_${id}.jpg?${new Date().getTime()}`);
            setHasImage(true);
          } else {
            setPreviewUrl(DEFAULT_IMAGE);
            setHasImage(false);
          }
          
          setLoading(false);
        } catch (err) {
          console.error('Błąd podczas pobierania danych przepisu:', err);
          setError('Błąd podczas pobierania danych przepisu');
          setLoading(false);
        }
      };
      
      fetchDanie();
    } else {
      // Dla nowego przepisu pokaż domyślny obrazek
      setPreviewUrl(DEFAULT_IMAGE);
    }
  }, [id, isEditing]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // Obsługa zmiany pliku
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      const file = e.target.files[0];
      setSelectedFile(file);
      
      // Pokaż podgląd wybranego pliku
      const reader = new FileReader();
      reader.onload = () => {
        setPhotoUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      
      let danieId = id;
      
      // Jeśli to nowy przepis, najpierw go utwórz
      if (!isEditing) {
        const response = await axios.post(`${API_URL}/dania`, formData);
        danieId = response.data.id;
      } else {
        // Aktualizuj istniejący przepis
        await axios.put(`${API_URL}/dania/${id}`, formData);
      }
      
      // Jeśli wybrano nowy plik, wyślij go
      if (selectedFile) {
        try {
          console.log('Wgrywam zdjęcie dla przepisu ID:', danieId);
          
          // Sprawdź czy plik jest prawidłowy
          if (!selectedFile.type.match('image/jpeg') && !selectedFile.type.match('image/jpg')) {
            throw new Error('Dozwolone są tylko pliki JPG/JPEG!');
          }
          
          // Sprawdź rozmiar pliku (max 5MB)
          if (selectedFile.size > 5 * 1024 * 1024) {
            throw new Error('Plik jest zbyt duży. Maksymalny rozmiar to 5MB.');
          }
          
          const formDataFile = new FormData();
          formDataFile.append('photo', selectedFile);
          
          console.log('Rozmiar pliku:', selectedFile.size);
          console.log('Typ pliku:', selectedFile.type);
          
          await axios.post(`${API_URL}/dania/${danieId}/photo`, formDataFile, {
            headers: {
              'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (progressEvent) => {
              const percentCompleted = Math.round(
                (progressEvent.loaded * 100) / progressEvent.total
              );
              setUploadProgress(percentCompleted);
              console.log('Postęp wgrywania:', percentCompleted);
            }
          });
          
          console.log('Wgrywanie pliku zakończone sukcesem');
        } catch (uploadErr) {
          console.error('Błąd wgrywania:', uploadErr);
          setError(`Błąd podczas przesyłania zdjęcia: ${uploadErr.message}`);
          // Mimo błędu kontynuuj, żeby przepis został zapisany
        }
      }
      
      // Po wszystkim przekieruj do listy przepisów
      navigate('/dania');
    } catch (err) {
      setError(`Błąd podczas ${isEditing ? 'aktualizacji' : 'dodawania'} przepisu`);
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
      setUploadProgress(0);
    }
  };
  
  // Obsługa usuwania zdjęcia
  const handleRemoveImage = async () => {
    if (!isEditing) {
      // Jeśli to nowy przepis, po prostu wyczyść lokalne dane
      setSelectedFile(null);
      setPreviewUrl('');
      return;
    }
    
    try {
      setLoading(true);
      await axios.delete(`${API_URL}/dania/${id}/photo`);
      setHasImage(false);
      setPreviewUrl('');
      setSelectedFile(null);
    } catch (err) {
      setError('Błąd podczas usuwania zdjęcia');
      console.error('Błąd:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && isEditing && !selectedFile) return <div className="text-center">Ładowanie...</div>;

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
        
        {/* Sekcja do wgrywania zdjęcia */}
        <div className="mb-3">
          <label htmlFor="photo" className="form-label">Zdjęcie Przepisu</label>
          <input
            type="file"
            className="form-control"
            id="photo"
            accept="image/jpeg,image/jpg"
            onChange={handleFileChange}
          />
          <small className="form-text text-muted">
            Akceptowane są tylko pliki JPG. Maksymalny rozmiar pliku: 5MB.
          </small>
        </div>
        
        {/* Podgląd zdjęcia - zawsze pokazuj ten blok */}
        <div className="mb-3">
          <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span>Zdjęcie przepisu</span>
              {hasImage && (
                <button 
                  type="button" 
                  className="btn btn-sm btn-danger" 
                  onClick={handleRemoveImage}
                >
                  Usuń zdjęcie
                </button>
              )}
            </div>
            <div className="card-body text-center">
              <img 
                src={previewUrl || photoUrl || DEFAULT_IMAGE} 
                alt="Podgląd przepisu" 
                className="img-fluid" 
                style={{ maxHeight: '300px' }} 
              />
              {!hasImage && !selectedFile && (
                <p className="text-muted mt-2">Brak zdjęcia. Dodaj zdjęcie aby ulepszyć przepis.</p>
              )}
            </div>
          </div>
        </div>
        
        {/* Pasek postępu wgrywania */}
        {uploadProgress > 0 && uploadProgress < 100 && (
          <div className="mb-3">
            <div className="progress">
              <div 
                className="progress-bar" 
                role="progressbar" 
                style={{ width: `${uploadProgress}%` }}
                aria-valuenow={uploadProgress} 
                aria-valuemin="0" 
                aria-valuemax="100"
              >
                {uploadProgress}%
              </div>
            </div>
          </div>
        )}
        
        <button 
          type="submit" 
          className="btn btn-primary" 
          disabled={loading}
        >
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