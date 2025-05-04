const multer = require('multer');
const path = require('path');
const fs = require('fs-extra');

// Upewnij się, że katalog do przechowywania zdjęć istnieje
const photoDir = path.join(__dirname, 'photos');
fs.ensureDirSync(photoDir);

// Konfiguracja storage dla Multer
const storage = multer.diskStorage({
  destination: function(req, file, cb) {
    cb(null, photoDir);
  },
  filename: function(req, file, cb) {
    // Użyj tymczasowej nazwy pliku z unikalnym znacznikiem czasu
    const timestamp = new Date().getTime();
    const randomString = Math.random().toString(36).substring(2, 10);
    cb(null, `temp_${timestamp}_${randomString}.jpg`);
  }
});

// Filtr plików - akceptujemy tylko obrazy JPG/JPEG
const fileFilter = (req, file, cb) => {
  if (file.mimetype === 'image/jpeg' || file.mimetype === 'image/jpg') {
    cb(null, true);
  } else {
    cb(new Error('Dozwolone są tylko pliki JPG/JPEG!'), false);
  }
};

// Konfiguracja multer
const upload = multer({ 
  storage: storage,
  fileFilter: fileFilter,
  limits: {
    fileSize: 5 * 1024 * 1024  // Limit 5MB
  }
});

module.exports = {
  upload,
  photoDir
};