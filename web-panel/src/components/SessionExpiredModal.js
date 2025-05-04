import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function SessionExpiredModal() {
  const { authError, setAuthError } = useAuth();
  const navigate = useNavigate();
  
  const handleConfirm = () => {
    setAuthError(null);
    navigate('/login');
  };
  
  // Automatyczne przekierowanie po 5 sekundach
  useEffect(() => {
    if (authError) {
      const timer = setTimeout(() => {
        handleConfirm();
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [authError]);
  
  if (!authError) return null;
  
  return (
    <div className="modal show d-block" tabIndex="-1" style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header bg-warning">
            <h5 className="modal-title">Sesja wygasła</h5>
          </div>
          <div className="modal-body">
            <p>{authError}</p>
            <p className="small text-muted">Zostaniesz automatycznie przekierowany do strony logowania za 5 sekund...</p>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-primary" onClick={handleConfirm}>
              Przejdź do logowania
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SessionExpiredModal;