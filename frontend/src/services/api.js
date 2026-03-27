import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080', // O endereço do teu Spring Boot
});

export default api;