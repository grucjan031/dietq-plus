import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  // Jeśli użytkownik jest już zalogowany, przekieruj go na stronę główną
  useEffect(() => {
    if (isAuthenticated()) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!username.trim() || !password.trim()) {
      setError('Wprowadź nazwę użytkownika i hasło');
      return;
    }
    
    try {
      setError('');
      setLoading(true);
      
      console.log('Rozpoczynam logowanie...');
      const result = await login(username, password);
      console.log('Wynik logowania:', result);
      
      if (result.success) {
        console.log('Logowanie udane, przekierowuję...');
        
        // Użyj timeout, aby dać czas na aktualizację stanu
        setTimeout(() => {
          const from = location.state?.from?.pathname || "/";
          navigate(from, { replace: true });
        }, 100);
      } else {
        setError(result.error);
      }
    } catch (err) {
      console.error('Błąd logowania:', err);
      setError('Wystąpił błąd podczas logowania');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="container">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-4">
          <div className="card mt-5">
            <div className="card-header bg-primary text-white text-center">
              <h4>Panel Administracyjny</h4>
              <p className="mb-0">Logowanie do systemu</p>
            </div>
            <div className="card-body">
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">
                    Nazwa użytkownika
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">
                    Hasło
                  </label>
                  <input
                    type="password"
                    className="form-control"
                    id="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
                <div className="d-grid gap-2">
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? 'Logowanie...' : 'Zaloguj się'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;