const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const pool = require('./db');

// Sekret JWT powinien być przechowywany w zmiennych środowiskowych
const JWT_SECRET = process.env.JWT_SECRET || 'twoj_sekret_jwt';

/**
 * Generuje token JWT dla użytkownika
 * @param {Object} user - Obiekt użytkownika
 * @returns {String} Token JWT
 */
const generateToken = (user) => {
  return jwt.sign(
    { id: user.id, username: user.username, rola: user.rola },
    JWT_SECRET,
    { expiresIn: '24h' } // Token wygasa po 24 godzinach
  );
};

/**
 * Haszuje hasło użytkownika
 * @param {String} password - Hasło w formie tekstowej
 * @returns {Promise<String>} Haszowane hasło
 */
const hashPassword = async (password) => {
  const salt = await bcrypt.genSalt(10);
  return await bcrypt.hash(password, salt);
};

/**
 * Middleware do weryfikacji tokenu JWT
 * @param {Object} req - Obiekt żądania
 * @param {Object} res - Obiekt odpowiedzi
 * @param {Function} next - Funkcja następna
 */
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  
  if (!token) {
    return res.status(401).json({ 
      error: 'Dostęp zabroniony - brak tokenu autoryzacyjnego',
      code: 'MISSING_TOKEN' 
    });
  }

  try {
    const verified = jwt.verify(token, JWT_SECRET);
    req.user = verified;
    next();
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(403).json({ 
        error: 'Token autoryzacyjny wygasł, zaloguj się ponownie', 
        code: 'TOKEN_EXPIRED'
      });
    }
    
    return res.status(403).json({ 
      error: 'Nieprawidłowy token autoryzacyjny', 
      code: 'INVALID_TOKEN'
    });
  }
};

module.exports = {
  generateToken,
  hashPassword,
  authenticateToken
};