const express = require('express');
const cors = require('cors');
const db = require('./db');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

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
    const result = await db.query(
      'INSERT INTO Dania (nazwa_dania, opis, sposob_przygotowania) VALUES ($1, $2, $3) RETURNING *',
      [nazwa_dania, opis, sposob_przygotowania]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/dania/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { nazwa_dania, opis, sposob_przygotowania } = req.body;
    const result = await db.query(
      'UPDATE Dania SET nazwa_dania = $1, opis = $2, sposob_przygotowania = $3 WHERE id = $4 RETURNING *',
      [nazwa_dania, opis, sposob_przygotowania, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }

    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/dania/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await db.query('DELETE FROM Dania WHERE id = $1 RETURNING *', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Przepis nie znaleziony' });
    }
    
    res.json({ message: 'Przepis usunięty', danie: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// API dla Składników
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
    const result = await db.query(
      'INSERT INTO Skladniki (nazwa_skladnika, kcal, bialko, weglowodany, tluszcze) VALUES ($1, $2, $3, $4, $5) RETURNING *',
      [nazwa_skladnika, kcal, bialko, weglowodany, tluszcze]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { nazwa_skladnika, kcal, bialko, weglowodany, tluszcze } = req.body;
    const result = await db.query(
      'UPDATE Skladniki SET nazwa_skladnika = $1, kcal = $2, bialko = $3, weglowodany = $4, tluszcze = $5 WHERE id = $6 RETURNING *',
      [nazwa_skladnika, kcal, bialko, weglowodany, tluszcze, id]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Składnik nie znaleziony' });
    }
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await db.query('DELETE FROM Skladniki WHERE id = $1 RETURNING *', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Składnik nie znaleziony' });
    }
    
    res.json({ message: 'Składnik usunięty', skladnik: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// API dla relacji Dania-Składniki
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
    const result = await db.query(
      'INSERT INTO Dania_Skladniki (danie_id, skladnik_id, ilosc, jednostka) VALUES ($1, $2, $3, $4) RETURNING *',
      [danie_id, skladnik_id, ilosc, jednostka]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put('/api/dania-skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { ilosc, jednostka } = req.body;
    const result = await db.query(
      'UPDATE Dania_Skladniki SET ilosc = $1, jednostka = $2 WHERE id = $3 RETURNING *',
      [ilosc, jednostka, id]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Relacja nie znaleziona' });
    }
    
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/dania-skladniki/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await db.query('DELETE FROM Dania_Skladniki WHERE id = $1 RETURNING *', [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Relacja nie znaleziona' });
    }
    
    res.json({ message: 'Relacja usunięta', relacja: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`Serwer działa na porcie: ${PORT}`);
});