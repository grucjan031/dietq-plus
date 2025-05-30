const express = require('express');
const cors = require('cors');
const db = require('./db');
const auth = require('./auth');
const bcrypt = require('bcrypt');
const fs = require('fs-extra');

const app = express();
const PORT = process.env.PORT || 5000;

const path = require('path');
const { upload, photoDir } = require('./upload');

// Middleware
app.use(cors({
  origin: ['http://localhost:3000', 'http://192.168.0.*:3000'],
    credentials: true
}));
app.use(express.json());

// Endpoint do pobierania zdjęcia przepisu - bez wymagania autoryzacji
app.get('/api/photos/:fileName', async (req, res) => {
  try {
    const { fileName } = req.params;
    const filePath = path.join(photoDir, fileName);
    
    console.log(`Żądanie zdjęcia: ${fileName}`);
    console.log(`Szukam pliku w: ${filePath}`);
    
    // Sprawdź czy plik istnieje
    if (!fs.existsSync(filePath)) {
      console.log(`Plik nie znaleziony: ${filePath}`);
      // Zwróć domyślny obrazek zamiast błędu
      const defaultImagePath = path.join(__dirname, 'no-image.jpg');
      if (fs.existsSync(defaultImagePath)) {
        return res.sendFile(defaultImagePath);
      } else {
        return res.status(404).json({ error: 'Zdjęcie nie znalezione' });
      }
    }
    
    // Wyślij plik
    res.sendFile(filePath);
    
  } catch (err) {
    console.error('Błąd przy pobieraniu zdjęcia:', err);
    res.status(500).json({ error: err.message });
  }
});

// Publiczne endpointy (nie wymagają autoryzacji)
app.post('/api/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    
    // Znajdź użytkownika w bazie
    const result = await db.query('SELECT * FROM Uzytkownicy WHERE username = $1', [username]);
    
    if (result.rows.length === 0) {
      return res.status(400).json({ error: 'Nieprawidłowa nazwa użytkownika lub hasło' });
    }
    
    const user = result.rows[0];
    
    // Sprawdź hasło
    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      return res.status(400).json({ error: 'Nieprawidłowa nazwa użytkownika lub hasło' });
    }
    
    // Generuj token
    const token = auth.generateToken(user);
    
    res.json({ 
      user: { id: user.id, username: user.username, rola: user.rola },
      token 
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Opcjonalny endpoint rejestracji (używany tylko jeśli chcemy pozwolić na rejestrację nowych użytkowników)
app.post('/api/register', async (req, res) => {
  try {
    const { username, password } = req.body;
    
    // Sprawdź czy użytkownik już istnieje
    const userExists = await db.query('SELECT * FROM Uzytkownicy WHERE username = $1', [username]);
    
    if (userExists.rows.length > 0) {
      return res.status(400).json({ error: 'Użytkownik o takiej nazwie już istnieje' });
    }
    
    // Hashowanie hasła
    const hashedPassword = await auth.hashPassword(password);
    
    // Zapisz nowego użytkownika
    const result = await db.query(
      'INSERT INTO Uzytkownicy (username, password) VALUES ($1, $2) RETURNING id, username, rola',
      [username, hashedPassword]
    );
    
    const user = result.rows[0];
    
    // Generuj token JWT
    const token = auth.generateToken(user);
    
    res.status(201).json({ user, token });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Weryfikacja tokenu - pomocny endpoint do sprawdzania ważności tokenu
app.get('/api/verify-token', auth.authenticateToken, (req, res) => {
  res.json({ valid: true, user: req.user });
});

// Endpoint do pobierania wszystkich dań ze składnikami - BEZ AUTORYZACJI
app.get('/api/getalldishes', async (req, res) => {
  try {
    // Pobierz wszystkie dania
    const daniaResult = await db.query('SELECT * FROM Dania ORDER BY id');
    
    // Dla każdego dania pobierz składniki
    const daniaZeSkladnikami = await Promise.all(
      daniaResult.rows.map(async (danie) => {
        // Pobierz składniki dla tego dania z pełnymi informacjami
        const skladnikiQuery = `
          SELECT 
            ds.ilosc,
            ds.jednostka,
            s.nazwa_skladnika,
            s.kcal as kcal_na_100g,
            s.bialko as bialko_na_100g,
            s.weglowodany as weglowodany_na_100g,
            s.tluszcze as tluszcze_na_100g
          FROM Dania_Skladniki ds
          JOIN Skladniki s ON ds.skladnik_id = s.id
          WHERE ds.danie_id = $1
          ORDER BY s.nazwa_skladnika
        `;
        
        const skladnikiResult = await db.query(skladnikiQuery, [danie.id]);
        
        // Stwórz tablicę składników z wartościami na 100g (dla dynamicznego przeliczania)
        const skladniki = skladnikiResult.rows.map(skladnik => ({
          nazwa: skladnik.nazwa_skladnika,
          ilosc: parseFloat(skladnik.ilosc) || 0,
          jednostka: skladnik.jednostka,
          // Wartości odżywcze na 100g (base values)
          wartosci_na_100g: {
            kcal: parseFloat(skladnik.kcal_na_100g) || 0,
            bialko: parseFloat(skladnik.bialko_na_100g) || 0,
            weglowodany: parseFloat(skladnik.weglowodany_na_100g) || 0,
            tluszcze: parseFloat(skladnik.tluszcze_na_100g) || 0
          }
        }));
        
        // Funkcja pomocnicza do obliczania wartości odżywczych
        const obliczWartosci = (skladniki) => {
          return skladniki.reduce((suma, skladnik) => {
            const mnoznik = skladnik.ilosc / 100;
            return {
              kcal: suma.kcal + (skladnik.wartosci_na_100g.kcal * mnoznik),
              bialko: suma.bialko + (skladnik.wartosci_na_100g.bialko * mnoznik),
              weglowodany: suma.weglowodany + (skladnik.wartosci_na_100g.weglowodany * mnoznik),
              tluszcze: suma.tluszcze + (skladnik.wartosci_na_100g.tluszcze * mnoznik)
            };
          }, { kcal: 0, bialko: 0, weglowodany: 0, tluszcze: 0 });
        };
        
        const aktualne_wartosci = obliczWartosci(skladniki);
        
        return {
          id: danie.id,
          nazwa: danie.nazwa_dania,
          opis: danie.opis,
          sposob_przygotowania: danie.sposob_przygotowania,
          ma_zdjecie: danie.ma_zdjecie,
          skladniki: skladniki,
          // Aktualne wartości odżywcze (bazując na obecnych ilościach)
          wartosci_odzywcze: {
            kcal: Math.round(aktualne_wartosci.kcal),
            bialko: Math.round(aktualne_wartosci.bialko * 10) / 10,
            weglowodany: Math.round(aktualne_wartosci.weglowodany * 10) / 10,
            tluszcze: Math.round(aktualne_wartosci.tluszcze * 10) / 10
          }
        };
      })
    );
    
    res.json(daniaZeSkladnikami);
    
  } catch (err) {
    console.error('Błąd przy pobieraniu wszystkich dań:', err);
    res.status(500).json({ error: err.message });
  }
});

// =================================================================
// WSZYSTKIE PONIŻSZE ENDPOINTY WYMAGAJĄ AUTORYZACJI
// =================================================================

// Middleware do weryfikacji tokenu dla wszystkich pozostałych endpointów /api/
app.use('/api', auth.authenticateToken);

// API dla Dania (Przepisy)
app.get('/api/dania', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM Dania ORDER BY id');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/dania/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const danieResult = await db.query('SELECT * FROM Dania WHERE id = $1', [id]);
    
    if (danieResult.rows.length === 0) {
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }

    const danie = danieResult.rows[0];
    
    // Pobierz składniki dla tego dania
    const skladnikiQuery = `
      SELECT ds.*, s.nazwa_skladnika, s.kcal, s.bialko, s.weglowodany, s.tluszcze 
      FROM Dania_Skladniki ds
      JOIN Skladniki s ON ds.skladnik_id = s.id
      WHERE ds.danie_id = $1
    `;
    const skladnikiResult = await db.query(skladnikiQuery, [id]);
    
    res.json({
      ...danie,
      skladniki: skladnikiResult.rows
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/dania', async (req, res) => {
  try {
    const { nazwa_dania, opis, sposob_przygotowania } = req.body;
    
    // Zapisz informacje o użytkowniku dodającym przepis
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query(
      'INSERT INTO Dania (nazwa_dania, opis, sposob_przygotowania) VALUES ($1, $2, $3) RETURNING *',
      [nazwa_dania, opis, sposob_przygotowania]
    );
    
    // Możesz tu dodać logowanie operacji
    console.log(`Przepis "${nazwa_dania}" dodany przez użytkownika ${username} (ID: ${userId})`);
    
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/dania/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { nazwa_dania, opis, sposob_przygotowania } = req.body;
    
    // Zapisz informacje o użytkowniku modyfikującym przepis
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query(
      'UPDATE Dania SET nazwa_dania = $1, opis = $2, sposob_przygotowania = $3 WHERE id = $4 RETURNING *',
      [nazwa_dania, opis, sposob_przygotowania, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }

    // Możesz tu dodać logowanie operacji
    console.log(`Przepis ID: ${id} zaktualizowany przez użytkownika ${username} (ID: ${userId})`);
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/dania/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    // Zapisz informacje o użytkowniku usuwającym przepis
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query('DELETE FROM Dania WHERE id = $1 RETURNING *', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }
    
    // Możesz tu dodać logowanie operacji
    console.log(`Przepis ID: ${id} usunięty przez użytkownika ${username} (ID: ${userId})`);
    
    res.json({ message: 'Przepis usunięty', danie: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// API dla Składników - również zabezpieczone
app.get('/api/skladniki', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM Skladniki ORDER BY nazwa_skladnika');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await db.query('SELECT * FROM Skladniki WHERE id = $1', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Składnik nie znaleziony' });
    }
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/skladniki', async (req, res) => {
  try {
    const { nazwa_skladnika, kcal, bialko, weglowodany, tluszcze } = req.body;
    
    // Zapisz informacje o użytkowniku dodającym składnik
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query(
      'INSERT INTO Skladniki (nazwa_skladnika, kcal, bialko, weglowodany, tluszcze) VALUES ($1, $2, $3, $4, $5) RETURNING *',
      [nazwa_skladnika, kcal, bialko, weglowodany, tluszcze]
    );
    
    // Możesz tu dodać logowanie operacji
    console.log(`Składnik "${nazwa_skladnika}" dodany przez użytkownika ${username} (ID: ${userId})`);
    
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { nazwa_skladnika, kcal, bialko, weglowodany, tluszcze } = req.body;
    
    // Zapisz informacje o użytkowniku modyfikującym składnik
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query(
      'UPDATE Skladniki SET nazwa_skladnika = $1, kcal = $2, bialko = $3, weglowodany = $4, tluszcze = $5 WHERE id = $6 RETURNING *',
      [nazwa_skladnika, kcal, bialko, weglowodany, tluszcze, id]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Składnik nie znaleziony' });
    }
    
    // Możesz tu dodać logowanie operacji
    console.log(`Składnik ID: ${id} zaktualizowany przez użytkownika ${username} (ID: ${userId})`);
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    // Zapisz informacje o użytkowniku usuwającym składnik
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query('DELETE FROM Skladniki WHERE id = $1 RETURNING *', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Składnik nie znaleziony' });
    }
    
    // Możesz tu dodać logowanie operacji
    console.log(`Składnik ID: ${id} usunięty przez użytkownika ${username} (ID: ${userId})`);
    
    res.json({ message: 'Składnik usunięty', skladnik: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// API dla relacji Dania-Składniki - również zabezpieczone
app.get('/api/dania-skladniki', async (req, res) => {
  try {
    const result = await db.query(`
      SELECT ds.*, d.nazwa_dania, s.nazwa_skladnika 
      FROM Dania_Skladniki ds
      JOIN Dania d ON ds.danie_id = d.id
      JOIN Skladniki s ON ds.skladnik_id = s.id
    `);
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/dania-skladniki', async (req, res) => {
  try {
    const { danie_id, skladnik_id, ilosc, jednostka } = req.body;
    
    // Zapisz informacje o użytkowniku dodającym relację
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query(
      'INSERT INTO Dania_Skladniki (danie_id, skladnik_id, ilosc, jednostka) VALUES ($1, $2, $3, $4) RETURNING *',
      [danie_id, skladnik_id, ilosc, jednostka]
    );
    
    // Możesz tu dodać logowanie operacji
    console.log(`Relacja danie_id: ${danie_id}, skladnik_id: ${skladnik_id} dodana przez użytkownika ${username} (ID: ${userId})`);
    
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/dania-skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { ilosc, jednostka } = req.body;
    
    // Zapisz informacje o użytkowniku modyfikującym relację
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query(
      'UPDATE Dania_Skladniki SET ilosc = $1, jednostka = $2 WHERE id = $3 RETURNING *',
      [ilosc, jednostka, id]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Relacja nie znaleziona' });
    }
    
    // Możesz tu dodać logowanie operacji
    console.log(`Relacja ID: ${id} zaktualizowana przez użytkownika ${username} (ID: ${userId})`);
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/dania-skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    // Zapisz informacje o użytkowniku usuwającym relację
    const userId = req.user.id;
    const username = req.user.username;
    
    const result = await db.query('DELETE FROM Dania_Skladniki WHERE id = $1 RETURNING *', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Relacja nie znaleziona' });
    }
    
    // Możesz tu dodać logowanie operacji
    console.log(`Relacja ID: ${id} usunięta przez użytkownika ${username} (ID: ${userId})`);
    
    res.json({ message: 'Relacja usunięta', relacja: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Endpoint do sprawdzania aktualnie zalogowanego użytkownika
app.get('/api/profile', async (req, res) => {
  try {
    // req.user zawiera informacje o zalogowanym użytkowniku (dodane przez middleware authenticateToken)
    const userId = req.user.id;
    
    // Pobierz pełne dane użytkownika z bazy (bez hasła)
    const result = await db.query(
      'SELECT id, username, rola, data_utworzenia FROM Uzytkownicy WHERE id = $1',
      [userId]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Użytkownik nie znaleziony' });
    }
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Endpoint do wgrywania zdjęcia dla przepisu (podczas tworzenia lub aktualizacji)
app.post('/api/dania/:id/photo', auth.authenticateToken, upload.single('photo'), async (req, res) => {
  try {
    const { id } = req.params;
    
    console.log(`Rozpoczęcie wgrywania zdjęcia dla przepisu ID: ${id}`);
    
    if (!req.file) {
      console.log('Błąd: Nie przesłano pliku');
      return res.status(400).json({ error: 'Nie przesłano pliku' });
    }
    
    console.log('Informacje o pliku:', req.file);
    
    // Pobierz informacje o przepisie
    const danieResult = await db.query('SELECT * FROM Dania WHERE id = $1', [id]);
    
    if (danieResult.rows.length === 0) {
      console.log(`Przepis o ID ${id} nie znaleziony`);
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }
    
    const danie = danieResult.rows[0];
    console.log('Dodaję zdjęcie dla przepisu:', danie.nazwa_dania);
    
    // Pobierz ścieżkę pliku tymczasowego
    const tempPath = req.file.path;
    console.log('Plik tymczasowy:', tempPath);
    
    // Utwórz prostą nazwę pliku opartą na ID, unikając problemów z nazwami
    const fileName = `dish_${id}.jpg`;
    const targetPath = path.join(photoDir, fileName);
    
    console.log('Ścieżka docelowa:', targetPath);
    
    // Usuń stary plik jeśli istnieje
    try {
      if (fs.existsSync(targetPath)) {
        fs.unlinkSync(targetPath);
        console.log('Usunięto stare zdjęcie');
      }
    } catch (err) {
      console.error('Błąd podczas usuwania starego zdjęcia:', err);
      // Kontynuuj mimo błędu
    }
    
    // Przenieś plik używając fs.renameSync zamiast copyFileSync
    try {
      fs.copyFileSync(tempPath, targetPath);
      console.log('Skopiowano plik');
      
      // Usuń plik tymczasowy po skopiowaniu
      fs.unlinkSync(tempPath);
      console.log('Usunięto plik tymczasowy');
    } catch (err) {
      console.error('Błąd podczas operacji na pliku:', err);
      return res.status(500).json({ error: `Błąd podczas zapisywania zdjęcia: ${err.message}` });
    }
    
    // Zaktualizuj przepis w bazie danych
    try {
      await db.query('UPDATE Dania SET ma_zdjecie = true WHERE id = $1', [id]);
      console.log('Zaktualizowano flagę ma_zdjecie w bazie danych');
    } catch (dbErr) {
      console.error('Błąd podczas aktualizacji bazy danych:', dbErr);
      return res.status(500).json({ error: `Błąd podczas aktualizacji bazy danych: ${dbErr.message}` });
    }
    
    res.json({
      success: true,
      message: 'Zdjęcie zapisane pomyślnie',
      fileName: fileName
    });
    
  } catch (err) {
    console.error('Błąd ogólny:', err);
    res.status(500).json({ error: `Błąd podczas przetwarzania: ${err.message}` });
  }
});

// Endpoint do usuwania zdjęcia przepisu
app.delete('/api/dania/:id/photo', auth.authenticateToken, async (req, res) => {
  try {
    const { id } = req.params;
    
    // Pobierz informacje o przepisie
    const danieResult = await db.query('SELECT * FROM Dania WHERE id = $1', [id]);
    
    if (danieResult.rows.length === 0) {
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }
    
    // Utwórz nazwę pliku bazując na ID
    const fileName = `dish_${id}.jpg`;
    const filePath = path.join(photoDir, fileName);
    
    // Usuń plik jeśli istnieje
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath);
      console.log(`Usunięto zdjęcie ${fileName}`);
    }
    
    // Aktualizuj przepis, aby nie zawierał informacji o zdjęciu
    await db.query(
      'UPDATE Dania SET ma_zdjecie = false WHERE id = $1',
      [id]
    );
    
    res.json({ success: true, message: 'Zdjęcie usunięte' });
    
  } catch (err) {
    console.error('Błąd przy usuwaniu zdjęcia:', err);
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`Serwer działa na porcie: ${PORT}`);
});