import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './catalog.scss';

// Carrega o mapa de todas as imagens locais dinamicamente
const imagesMap = import.meta.glob('../../assets/cards/**/*.png', { eager: true });

function Catalog() {
    const [cards, setCards] = useState([]);
    const [filteredCards, setFilteredCards] = useState([]);
    const [loading, setLoading] = useState(true);

    // Filtros
    const [selectedSet, setSelectedSet] = useState('ALL');
    const [selectedColor, setSelectedColor] = useState('ALL');
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        axios.get('http://localhost:8080/api/cards')
            .then(res => {
                setCards(res.data);
                setFilteredCards(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Erro ao carregar catálogo:", err);
                setLoading(false);
            });
    }, []);

    // Aplicação dos Filtros
    useEffect(() => {
        let result = cards;

        if (selectedSet !== 'ALL') {
            result = result.filter(c => c.setId === selectedSet);
        }

        if (selectedColor !== 'ALL') {
            result = result.filter(c => c.color && c.color.toLowerCase().includes(selectedColor.toLowerCase()));
        }

        if (searchTerm.trim() !== '') {
            result = result.filter(c =>
                c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                c.code.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        setFilteredCards(result);
    }, [selectedSet, selectedColor, searchTerm, cards]);

    // Função para resolver a imagem no mapa do Vite
    const getCardImage = (setId, imagesList) => {
        if (!setId || !imagesList || !Array.isArray(imagesList) || imagesList.length === 0) return null;

        for (const imageName of imagesList) {
            const expectedPath = `../../assets/cards/${setId}/${imageName}`;
            if (imagesMap[expectedPath]) {
                return {
                    url: imagesMap[expectedPath].default,
                    isAA: imageName.includes('_AA'),
                    total: imagesList.length
                };
            }
        }
        return null;
    };

    // Extrair lista única de Sets para o dropdown
    // const availableSets = ['ALL', ...new Set(cards.map(c => c.setId).filter(Boolean))];

    // --- 1. ORDENAÇÃO DOS SETS ---
    // Pega em todos os Sets únicos, remove valores nulos e ordena alfabeticamente/numéricamente
    const availableSets = [
        'ALL',
        ...new Set((cards || []).map(c => c?.setId).filter(Boolean))
    ].sort((a, b) => {
        if (a === 'ALL') return -1; // 'ALL' fica sempre no topo
        if (b === 'ALL') return 1;
        return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
    });

// --- 2. ORDENAÇÃO OFICIAL DAS CORES (OP TCG) ---
    const colorOrder = ['ALL', 'Red', 'Green', 'Blue', 'Purple', 'Black', 'Yellow'];

    return (
        <div className="catalog-container">
            <header className="catalog-header">
                <h1>Catálogo de Cartas</h1>
                <p>Coleção completa organizada por Sets</p>
            </header>

            {/* BARRA DE FILTROS */}
            <div className="filter-bar">
                <input
                    type="text"
                    placeholder="Pesquisar por nome ou código (ex: Luffy, OP01-001)..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="search-input"
                />

                {/* SELECT DE SETS (Agora Ordenado) */}
                <select value={selectedSet} onChange={(e) => setSelectedSet(e.target.value)} className="filter-select">
                    {availableSets.map(setId => (
                        <option key={setId} value={setId}>
                            {setId === 'ALL' ? 'Todos os Sets' : setId}
                        </option>
                    ))}
                </select>

                {/* SELECT DE CORES (Na Ordem Oficial) */}
                <select value={selectedColor} onChange={(e) => setSelectedColor(e.target.value)}
                        className="filter-select">
                    {colorOrder.map(color => (
                        <option key={color} value={color}>
                            {color === 'ALL' ? 'Todas as Cores' : color}
                        </option>
                    ))}
                </select>
            </div>

            {/* GRELHA DE CARTAS */}
            {loading ? (
                <div className="loading-state">⚓ A carregar coleção da Grand Line...</div>
            ) : (
                <div className="cards-grid">
                    {filteredCards.map(card => {
                        const imgData = getCardImage(card.setId, card.images);

                        return (
                            <div key={card.code} className="card-item">
                                <div className="card-image-wrapper">
                                    {imgData ? (
                                        <>
                                            <img src={imgData.url} alt={card.name} loading="lazy"/>
                                            {/* Badge visual se a imagem exibida for Alternative Art */}
                                            {imgData.isAA && <span className="aa-badge">Alt Art</span>}
                                        </>
                                    ) : (
                                        <div className="no-image">
                                            <span>{card.code}</span>
                                            <small>{card.name}</small>
                                        </div>
                                    )}
                                </div>
                                <div className="card-info">
                                    <span className="card-code">{card.code}</span>
                                    <span className="card-name">{card.name}</span>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default Catalog;