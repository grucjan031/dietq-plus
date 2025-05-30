import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api';
const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState(null);

  // Konfiguracja domyślnych nagłówków axios podczas ładowania
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      try {
        setCurrentUser(JSON.parse(storedUser));
        
        // Dodaj token do wszystkich żądań
        axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
      } catch (e) {
        console.error('Błąd parsowania danych użytkownika:', e);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }
    
    setLoading(false);
    
    // Dodaj interceptor do obsługi błędów autoryzacji (401, 403)
    const interceptor = axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response) {
          // Błąd autoryzacji - token wygasł lub jest nieprawidłowy
          if (error.response.status === 401 || error.response.status === 403) {
            setAuthError("Twoja sesja wygasła lub token jest nieprawidłowy. Zaloguj się ponownie.");
            
            // Automatyczne wylogowanie
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setToken(null);
            setCurrentUser(null);
            delete axios.defaults.headers.common['Authorization'];
          }
        }
        return Promise.reject(error);
      }
    );
    
    return () => {
      // Usuń interceptor przy odmontowaniu komponentu
      axios.interceptors.response.eject(interceptor);
    };
  }, []);

  // Funkcja logowania
  const login = async (username, password) => {
    try {
      const response = await axios.post(`${API_URL}/login`, {
        username,
        password
      });
      
      if (!response.data.token || !response.data.user) {
        console.error('Nieprawidłowa struktura odpowiedzi:', response.data);
        return { 
          success: false, 
          error: 'Nieprawidłowa odpowiedź z serwera'
        };
      }
      
      const { token, user } = response.data;
      
      // Zapisz do localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      
      // Ustaw w state
      setToken(token);
      setCurrentUser(user);
      
      // Dodaj token do wszystkich przyszłych żądań
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      return { success: true };
    } catch (error) {
      console.error('Błąd podczas logowania:', error);
      return { 
        success: false, 
        error: error.response?.data?.error || 'Wystąpił błąd podczas logowania' 
      };
    }
  };

  // Funkcja wylogowania
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setCurrentUser(null);
    setAuthError(null);
    
    // Usuń token z nagłówków
    delete axios.defaults.headers.common['Authorization'];
  };

  // Funkcja sprawdzająca czy użytkownik jest uwierzytelniony
  const isAuthenticated = () => {
    return !!token;
  };
  
  // Funkcja weryfikująca token na serwerze
  const verifyToken = async () => {
    if (!token) return false;
    
    try {
      const response = await axios.get(`${API_URL}/verify-token`);
      return response.data.valid;
    } catch (error) {
      console.error('Błąd weryfikacji tokenu:', error);
      return false;
    }
  };

  const value = {
    currentUser,
    login,
    logout,
    isAuthenticated,
    verifyToken,
    loading,
    authError,
    setAuthError
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}