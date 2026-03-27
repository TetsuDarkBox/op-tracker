import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './LandingPage.scss';

function LandingPage() {
    const navigate = useNavigate();
    const [card, setCard] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Vamos buscar o Luffy ST10-001 como exemplo de anatomia
        axios.get('http://localhost:8080/api/cards/code/ST10-001')
            .then(res => {
                setCard(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Erro ao carregar carta:", err);
                setLoading(false);
            });
    }, []);

    // Função vital para carregar a imagem da pasta correta do Set
    const getCardImage = (setId, imageName) => {
        if (!setId || !imageName) return null;
        try {
            // Procura em src/assets/cards/[SET]/[CODE].png
            return new URL(`../../assets/cards/${setId}/${imageName}`, import.meta.url).href;
        } catch (e) {
            return null;
        }
    };

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

                    {/* CARTA CENTRAL COM IMAGEM LOCAL */}
                    <div className="central-card-anatomy" onClick={() => navigate('/catalog')}>
                        <div className="card-frame">
                            {card ? (
                                <img
                                    src={getCardImage(card.setId, card.imageName)}
                                    alt={card.name}
                                    className="card-image"
                                    onError={(e) => e.target.style.display = 'none'}
                                />
                            ) : (
                                <div className="placeholder-content">Carregando...</div>
                            )}
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