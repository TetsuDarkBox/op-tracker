import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './LandingPage.scss';

// --- A MAGIA DO VITE ACONTECE AQUI ---
// import.meta.glob cria um mapa de todas as imagens nas subpastas de assets/cards.
// O 'eager: true' carrega as referências imediatamente.
const imagesMap = import.meta.glob('../../assets/cards/**/*.png', { eager: true });

function LandingPage() {
    const navigate = useNavigate();
    const [card, setCard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [cardImageUrl, setCardImageUrl] = useState(null);

    useEffect(() => {
        // Vamos buscar o Luffy ST10-001 como exemplo
        axios.get('http://localhost:8080/api/cards/code/ST10-001')
            .then(res => {
                const cardData = res.data;
                setCard(cardData);

                // --- LÓGICA DE DETEÇÃO DA IMAGEM ---
                if (cardData.setId && cardData.imageName) {
                    // Construímos o caminho relativo que o Vite entende internamente
                    // Ex: ../../assets/cards/ST10/ST10-001.png
                    const expectedPath = `../../assets/cards/${cardData.setId}/${cardData.imageName}`;

                    // Verificamos se este caminho existe no nosso mapa de imagens
                    if (imagesMap[expectedPath]) {
                        // Se existir, pegamos no URL final gerado pelo Vite
                        setCardImageUrl(imagesMap[expectedPath].default);
                    } else {
                        console.error("❌ Imagem não encontrada no mapa do Vite:", expectedPath);
                        // Opcional: define uma imagem de erro/placeholder
                        setCardImageUrl(null);
                    }
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("❌ Erro ao carregar carta:", err);
                setLoading(false);
            });
    }, []);

    const scrollToAnatomy = () => {
        document.getElementById('anatomy-section')?.scrollIntoView({ behavior: 'smooth' });
    };

    return (
        <div className="landing-viewport">
            <section className="hero-section">
                <div className="hero-content">
                    <span className="badge">Project Grand Line v1.0</span>
                    <h1 className="hero-title">OP TRACKER</h1>
                    <p className="hero-subtitle">A base de dados definitiva para o One Piece TCG.</p>
                    <button className="cta-button" onClick={scrollToAnatomy}>Explorar Anatomia 🃏</button>
                </div>
            </section>

            <section id="anatomy-section" className="anatomy-section">
                <div className="anatomy-container">

                    {/* Custo */}
                    <div className="annotation-group cost-ref">
                        <div className="box">
                            <h4>Custo (Cost)</h4>
                            <p>Valor de DON!!: <strong>{card?.cost ?? '10'}</strong></p>
                        </div>
                        <div className="line"></div>
                    </div>

                    {/* Nome */}
                    <div className="annotation-group name-ref">
                        <div className="line"></div>
                        <div className="box">
                            <h4>Nome & Cor</h4>
                            <p><strong>{card?.name || 'Carregando...'}</strong> ({card?.color || 'Red/Purple'})</p>
                        </div>
                    </div>

                    {/* CARTA CENTRAL COM IMAGEM LOCAL CORRIGIDA */}
                    <div className="central-card-anatomy" onClick={() => navigate('/catalog')}>
                        <div className="card-frame">
                            {loading ? (
                                <div className="placeholder-content">Navegando...</div>
                            ) : cardImageUrl ? (
                                <img
                                    src={cardImageUrl}
                                    alt={card?.name || 'Carta'}
                                    className="card-image"
                                    onError={(e) => {
                                        console.error("Falha ao carregar o URL da imagem:", cardImageUrl);
                                        e.target.style.display = 'none';
                                        e.target.nextSibling.style.display = 'flex';
                                    }}
                                />
                            ) : null}

                            {/* Placeholder caso a imagem falhe */}
                            <div className="placeholder-content" style={{ display: (cardImageUrl || loading) ? 'none' : 'flex' }}>
                                <span>Carta Não Encontrada</span>
                                {card && <small>{card.setId}/{card.imageName}</small>}
                            </div>

                            <div className="cost-overlay">{card?.cost ?? '10'}</div>
                        </div>
                        <div className="hover-hint">Ver no Catálogo →</div>
                    </div>

                    {/* Atributo */}
                    <div className="annotation-group attribute-ref">
                        <div className="box">
                            <h4>Atributo</h4>
                            <p>Tipo: <strong>{card?.attribute || 'Slash'}</strong></p>
                        </div>
                        <div className="line"></div>
                    </div>

                    {/* Poder */}
                    <div className="annotation-group power-ref">
                        <div className="line"></div>
                        <div className="box">
                            <h4>Poder (Power)</h4>
                            <p>Força: <strong>{card?.power ?? '5000'}</strong></p>
                        </div>
                    </div>

                    {/* Efeito */}
                    <div className="annotation-group effect-ref">
                        <div className="box">
                            <h4>Efeito / Texto</h4>
                            <p>{card?.effect || 'Habilidades da carta.'}</p>
                        </div>
                        <div className="line"></div>
                    </div>

                </div>
            </section>
        </div>
    );
}

export default LandingPage;