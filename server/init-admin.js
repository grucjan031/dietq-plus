const pool = require('./db');
const { hashPassword } = require('./auth');

const ADMIN_PASS = process.env.ADMIN_PASS;

async function createInitialAdmin() {
  try {
    // Sprawdź czy istnieje już jakiś użytkownik
    const existingUsers = await pool.query('SELECT COUNT(*) FROM Uzytkownicy');
    
    if (parseInt(existingUsers.rows[0].count) > 0) {
      console.log('Użytkownicy już istnieją. Pomijanie tworzenia administratora.');
      return;
    }
    
    // Stwórz konto administratora
    const username = 'admin';
    const password = ADMIN_PASS; 
    
    const hashedPassword = await hashPassword(password);
    
    await pool.query(
      'INSERT INTO Uzytkownicy (username, password, rola) VALUES ($1, $2, $3)',
      [username, hashedPassword, 'admin']
    );
    
    console.log('Stworzono konto administratora');
  } catch (error) {
    console.error('Błąd podczas tworzenia konta administratora:', error);
  }
}

// Wywołaj funkcję jeśli plik jest uruchamiany bezpośrednio
if (require.main === module) {
  createInitialAdmin()
    .then(() => process.exit(0))
    .catch(err => {
      console.error(err);
      process.exit(1);
    });
}

module.exports = { createInitialAdmin };