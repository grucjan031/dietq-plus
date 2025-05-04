import React, { useState, useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function PrivateRoute() {
  const { isAuthenticated, verifyToken } = useAuth();
  const location = useLocation();
  const [isVerifying, setIsVerifying] = useState(true);
  const [isValid, setIsValid] = useState(false);
  
  useEffect(() => {
    const checkToken = async () => {
      if (isAuthenticated()) {
        try {
          const valid = await verifyToken();
          setIsValid(valid);
        } catch (error) {
          setIsValid(false);
        }
      } else {
        setIsValid(false);
      }
      setIsVerifying(false);
    };
    
    checkToken();
  }, [isAuthenticated, verifyToken]);
  
  // Pokaż loading spinner podczas weryfikacji tokenu
  if (isVerifying) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '100vh' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Weryfikacja sesji...</span>
        </div>
      </div>
    );
  }
  
  // Przekieruj do logowania jeśli token jest nieprawidłowy
  if (!isValid) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  // Kontynuuj renderowanie komponentów potomnych jeśli token jest prawidłowy
  return <Outlet />;
}

export default PrivateRoute;