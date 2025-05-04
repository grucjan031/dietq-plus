import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api';

// Funkcja pomocnicza do konfiguracji axios z tokenem
const configureAxios = () => {
  const token = localStorage.getItem('token');
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }
};

const api = {
  // Dania (Przepisy)
  dania: {
    getAll: async () => {
      configureAxios();
      const response = await axios.get(`${API_URL}/dania`);
      return response.data;
    },
    
    getOne: async (id) => {
      configureAxios();
      const response = await axios.get(`${API_URL}/dania/${id}`);
      return response.data;
    },
    
    create: async (danieData) => {
      configureAxios();
      const response = await axios.post(`${API_URL}/dania`, danieData);
      return response.data;
    },
    
    update: async (id, danieData) => {
      configureAxios();
      const response = await axios.put(`${API_URL}/dania/${id}`, danieData);
      return response.data;
    },
    
    delete: async (id) => {
      configureAxios();
      const response = await axios.delete(`${API_URL}/dania/${id}`);
      return response.data;
    }
  },
  
  // Składniki
  skladniki: {
    getAll: async () => {
      configureAxios();
      const response = await axios.get(`${API_URL}/skladniki`);
      return response.data;
    },
    
    getOne: async (id) => {
      configureAxios();
      const response = await axios.get(`${API_URL}/skladniki/${id}`);
      return response.data;
    },
    
    create: async (skladnikData) => {
      configureAxios();
      const response = await axios.post(`${API_URL}/skladniki`, skladnikData);
      return response.data;
    },
    
    update: async (id, skladnikData) => {
      configureAxios();
      const response = await axios.put(`${API_URL}/skladniki/${id}`, skladnikData);
      return response.data;
    },
    
    delete: async (id) => {
      configureAxios();
      const response = await axios.delete(`${API_URL}/skladniki/${id}`);
      return response.data;
    }
  },
  
  // Relacje Dania-Składniki
  daniaSkladniki: {
    getAll: async () => {
      configureAxios();
      const response = await axios.get(`${API_URL}/dania-skladniki`);
      return response.data;
    },
    
    create: async (relacjaData) => {
      configureAxios();
      const response = await axios.post(`${API_URL}/dania-skladniki`, relacjaData);
      return response.data;
    },
    
    update: async (id, relacjaData) => {
      configureAxios();
      const response = await axios.put(`${API_URL}/dania-skladniki/${id}`, relacjaData);
      return response.data;
    },
    
    delete: async (id) => {
      configureAxios();
      const response = await axios.delete(`${API_URL}/dania-skladniki/${id}`);
      return response.data;
    }
  },
  
  // Profil użytkownika
  profile: {
    get: async () => {
      configureAxios();
      const response = await axios.get(`${API_URL}/profile`);
      return response.data;
    }
  },
  
  // Autoryzacja
  auth: {
    login: async (credentials) => {
      const response = await axios.post(`${API_URL}/login`, credentials);
      return response.data;
    },
    
    verifyToken: async () => {
      configureAxios();
      const response = await axios.get(`${API_URL}/verify-token`);
      return response.data;
    }
  }
};

export default api;