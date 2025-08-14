import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import './UserProfile.css';

interface UserProfileProps {
  userId: string;
  onClose: () => void;
}

interface ProfileData {
  nickname?: string;
  occupation?: string;
  personality?: string;
  traits?: string[];
  additionalInfo?: string;
  preferredLanguage?: string;
  enableForNewChats?: boolean;
}

const UserProfile: React.FC<UserProfileProps> = ({ userId, onClose }) => {
  const { t } = useTranslation();
  const [profile, setProfile] = useState<ProfileData>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [advancedOpen, setAdvancedOpen] = useState(true);

  useEffect(() => {
    loadProfile();
  }, [userId]);

  const loadProfile = async () => {
    try {
      setIsLoading(true);
      const response = await fetch(`/api/profile/${userId}`);
      if (response.ok) {
        const data = await response.json();
        // Normalize incoming data to expected frontend shapes
        setProfile({
          ...data,
          traits: Array.isArray(data.traits)
            ? data.traits
            : (data.traits ? Array.from(data.traits as any) : []),
        });
      }
    } catch (error) {
      console.error('Error loading profile:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      const response = await fetch(`/api/profile/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(profile),
      });

      if (response.ok) {
        // Success - could show a toast notification here
        onClose();
      }
    } catch (error) {
      console.error('Error saving profile:', error);
    } finally {
      setIsSaving(false);
    }
  };

  const handleTraitToggle = (trait: string) => {
    const currentTraits = profile.traits || [];
    const newTraits = currentTraits.includes(trait)
      ? currentTraits.filter(t => t !== trait)
      : [...currentTraits, trait];
    
    setProfile(prev => ({ ...prev, traits: newTraits }));
  };

  const personalityOptions = [
    { value: 'default', label: t('profile.personalityOptions.default') },
    { value: 'friendly', label: t('profile.personalityOptions.friendly') },
    { value: 'professional', label: t('profile.personalityOptions.professional') },
    { value: 'casual', label: t('profile.personalityOptions.casual') },
    { value: 'formal', label: t('profile.personalityOptions.formal') },
  ];

  const traitOptions = [
    { value: 'chatty', label: t('profile.traitOptions.chatty') },
    { value: 'witty', label: t('profile.traitOptions.witty') },
    { value: 'straightShooting', label: t('profile.traitOptions.straightShooting') },
    { value: 'encouraging', label: t('profile.traitOptions.encouraging') },
    { value: 'genZ', label: t('profile.traitOptions.genZ') },
    { value: 'traditional', label: t('profile.traitOptions.traditional') },
    { value: 'forwardThinking', label: t('profile.traitOptions.forwardThinking') },
  ];

  if (isLoading) {
    return (
      <div className="profile-modal">
        <div className="profile-content">
          <div className="loading">Loading...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-modal">
      <div className="profile-content">
        <div className="profile-header">
          <h2>{t('profile.title')}</h2>
          <p className="profile-subtitle">{t('profile.subtitle')}</p>
          <button className="close-button" onClick={onClose}>×</button>
        </div>

        <div className="profile-sections">
          {/* Nickname Section */}
          <div className="profile-section">
            <label>{t('profile.nickname')}</label>
            <input
              type="text"
              placeholder={t('profile.nicknamePlaceholder')}
              value={profile.nickname || ''}
              onChange={(e) => setProfile(prev => ({ ...prev, nickname: e.target.value }))}
            />
          </div>

          {/* Occupation Section */}
          <div className="profile-section">
            <label>{t('profile.occupation')}</label>
            <input
              type="text"
              placeholder={t('profile.occupationPlaceholder')}
              value={profile.occupation || ''}
              onChange={(e) => setProfile(prev => ({ ...prev, occupation: e.target.value }))}
            />
          </div>

          {/* Personality Section */}
          <div className="profile-section">
            <label>
              {t('profile.personality')}
              <span className="info-icon">ℹ</span>
            </label>
            <select
              value={profile.personality || 'default'}
              onChange={(e) => setProfile(prev => ({ ...prev, personality: e.target.value }))}
            >
              {personalityOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {/* Traits Section */}
          <div className="profile-section">
            <label>
              {t('profile.traits')}
              <span className="info-icon">ℹ</span>
            </label>
            <div className="traits-tags">
              {traitOptions.map(trait => {
                const checked = !!profile.traits?.includes(trait.value);
                return (
                  <label
                    key={trait.value}
                    className={`trait-tag ${checked ? 'active' : ''}`}
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => handleTraitToggle(trait.value)}
                    />
                    {checked && <span className="trait-check">✓</span>}
                    {trait.label}
                  </label>
                );
              })}
              <button type="button" className="refresh-button">↻</button>
            </div>
          </div>

          {/* Additional Info Section */}
          <div className="profile-section">
            <label>
              {t('profile.additionalInfo')}
              <span className="info-icon">ℹ</span>
            </label>
            <textarea
              placeholder={t('profile.additionalInfoPlaceholder')}
              value={profile.additionalInfo || ''}
              onChange={(e) => setProfile(prev => ({ ...prev, additionalInfo: e.target.value }))}
            />
          </div>

          {/* Advanced Section */}
          <div className={`profile-section advanced ${advancedOpen ? 'open' : ''}`}>
            <div className="advanced-header" onClick={() => setAdvancedOpen((o) => !o)}>
              <span>{t('profile.advanced')}</span>
              <span className="arrow">▼</span>
            </div>
            {advancedOpen && (
            <div className="advanced-content">
              <label className="toggle-label">
                <span>{t('profile.enableNewChats')}</span>
                <div className="toggle-wrapper">
                  <input
                    type="checkbox"
                    checked={profile.enableForNewChats || false}
                    onChange={(e) => setProfile(prev => ({ ...prev, enableForNewChats: e.target.checked }))}
                  />
                  <span className="toggle-switch"></span>
                </div>
              </label>
            </div>
            )}
          </div>
        </div>

        <div className="profile-actions">
          <button className="cancel-button" onClick={onClose}>
            {t('common.cancel')}
          </button>
          <button 
            className="save-button" 
            onClick={handleSave}
            disabled={isSaving}
          >
            {isSaving ? t('common.loading') : t('common.save')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
