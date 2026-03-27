import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Sidebar.scss';

function Sidebar() {
    const [isOpen, setIsOpen] = useState(false);
    const navigate = useNavigate();

    const toggleSidebar = () => setIsOpen(!isOpen);

    const menuItems = [
        { name: 'Início', icon: '🏠', path: '/' },
        { name: 'Catálogo', icon: '🃏', path: '/catalog' },
        { name: 'Tripulação', icon: '⚔️', path: '/auth/login' },
        { name: 'Registo', icon: '📜', path: '/auth/register' },
    ];

    return (
        <>
            {/* Botão para abrir/fechar (Hambúrguer) */}
            <button className={`sidebar-toggle ${isOpen ? 'open' : ''}`} onClick={toggleSidebar}>
                {isOpen ? '✕' : '☰'}
            </button>

            {/* O Menu em si */}
            <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
                <div className="sidebar-logo">
                    OP<span>TRACKER</span>
                </div>

                <nav className="sidebar-nav">
                    {menuItems.map((item) => (
                        <div
                            key={item.name}
                            className="nav-item"
                            onClick={() => { navigate(item.path); setIsOpen(false); }}
                        >
                            <span className="icon">{item.icon}</span>
                            <span className="label">{item.name}</span>
                        </div>
                    ))}
                </nav>
            </aside>

            {/* Overlay para fechar ao clicar fora */}
            {isOpen && <div className="sidebar-overlay" onClick={toggleSidebar}></div>}
        </>
    );
}

export default Sidebar;