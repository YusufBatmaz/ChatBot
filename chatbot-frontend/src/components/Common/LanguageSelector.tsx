import React from 'react';
import { useTranslation } from 'react-i18next';
import './LanguageSelector.css';
import ReactCountryFlag from 'react-country-flag';

type Props = { userId?: string };

const LanguageSelector: React.FC<Props> = ({ userId }) => {
  const { i18n } = useTranslation();

  const languages = [
    { code: 'en', name: 'English', countryCode: 'US' },
    { code: 'tr', name: 'Türkçe', countryCode: 'TR' },
    { code: 'de', name: 'Deutsch', countryCode: 'DE' }
  ];

  const handleLanguageChange = async (languageCode: string) => {
    i18n.changeLanguage(languageCode);
    // Save language preference to localStorage
    localStorage.setItem('i18nextLng', languageCode);
    
    // Persist preference to backend if logged in
    if (userId) {
      try {
        await fetch(`/api/profile/${userId}/language?language=${languageCode}`, {
          method: 'PUT'
        });
      } catch (e) {
        // Non-blocking: log and continue
        console.error('Failed to persist preferred language', e);
      }
    }
  };

  return (
    <div className="language-selector">
      <div className="language-dropdown">
        <button className="language-button">
          <span className="flag">
            {(() => {
              const current = languages.find(lang => lang.code === i18n.language);
              return current ? (
                <ReactCountryFlag svg countryCode={current.countryCode} aria-label={current.name} />
              ) : (
                '🌐'
              );
            })()}
          </span>
          <span className="language-name">
            {languages.find(lang => lang.code === i18n.language)?.name || 'English'}
          </span>
          <span className="arrow">▼</span>
        </button>
        
        <div className="language-options">
          {languages.map((language) => (
            <button
              key={language.code}
              className={`language-option ${i18n.language === language.code ? 'active' : ''}`}
              onClick={() => handleLanguageChange(language.code)}
            >
              <span className="flag">
                <ReactCountryFlag svg countryCode={language.countryCode} aria-label={language.name} />
              </span>
              <span className="language-name">{language.name}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LanguageSelector;
