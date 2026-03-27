import React from 'react';
import { Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage/LandingPage.jsx';
import Sidebar from "./components/sidebar/sidebar.jsx"; // Importa o componente da página

function App() {
    return (
        <div className="App">
            <Sidebar /> {/* Menu sempre disponível */}
            <Routes>
                {/* O App apenas decide QUAL componente mostrar */}
                <Route path="/" element={<LandingPage />} />

                <Route path="/browse" element={<div>Página do Catálogo em breve...</div>} />
                <Route path="/register" element={<div>Página de Registo em breve...</div>} />
                <Route path="/login" element={<div>Página de Login em breve...</div>} />
            </Routes>
        </div>
    );
}

export default App;