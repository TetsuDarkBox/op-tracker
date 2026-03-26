import { useEffect, useState } from 'react'
import axios from 'axios'
import './App.css'

function App() {
    const [cards, setCards] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        // Tenta ir buscar as cartas ao Backend Java
        axios.get('http://localhost:8080/api/cards')
            .then(response => {
                setCards(response.data)
                setLoading(false)
            })
            .catch(err => {
                console.error("Erro:", err)
                setError("Erro ao ligar ao Backend Java. O servidor está ligado?")
                setLoading(false)
            })
    }, [])

    if (loading) return <h2>A carregar dados... 🏴‍☠️</h2>
    if (error) return <h2 style={{color: 'red'}}>{error}</h2>

    return (
        <div style={{padding: '20px'}}>
            <h1>One Piece Card Tracker</h1>
            <div style={{display: 'flex', gap: '20px', flexWrap: 'wrap'}}>

                {cards.map(card => (
                    <div key={card.code} style={{
                        border: '1px solid #ccc',
                        borderRadius: '10px',
                        padding: '10px',
                        width: '220px',
                        backgroundColor: '#222',
                        color: 'white'
                    }}>
                        {/* Imagem da carta */}
                        <img
                            src={card.imageUrl}
                            alt={card.name}
                            style={{width: '100%', borderRadius: '5px'}}
                        />

                        <h3>{card.name}</h3>
                        <p><strong>{card.code}</strong> | {card.rarity}</p>
                        <p style={{color: card.color === 'Red' ? '#ff6b6b' : 'cyan'}}>
                            {card.color} - {card.type}
                        </p>
                        <p style={{fontSize: '0.8em'}}>{card.effect}</p>
                    </div>
                ))}

            </div>
        </div>
    )
}

export default App