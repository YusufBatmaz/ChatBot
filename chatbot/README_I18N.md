# ChatBot Internationalization (i18n) Setup

This document describes the comprehensive internationalization implementation for the ChatBot project, supporting multiple languages with intelligent language detection and user customization.

## 🌍 Supported Languages

- **English (en)** - Default language
- **Turkish (tr)** - Turkish language support
- **German (de)** - German language support

## 🏗️ Architecture Overview

### Backend Components

1. **UserProfile Model** - Stores user language preferences and persona settings
2. **LanguageDetectionService** - Detects user message language using pattern matching
3. **UserProfileService** - Manages user profile data and language preferences
4. **ChatService** - Enhanced with language-aware responses and system messages

### Frontend Components

1. **i18n Configuration** - React i18next setup with language detection
2. **LanguageSelector** - Dropdown for language switching
3. **UserProfile** - ChatGPT-style customization panel
4. **Translated Components** - All UI components support multiple languages

## 🚀 Key Features

### 1. Intelligent Language Detection
- **Automatic Detection**: Analyzes user messages to determine language
- **Pattern Matching**: Uses regex patterns for Turkish, German, and English
- **Fallback System**: Defaults to English if language cannot be determined

### 2. Dynamic Response Language
- **User Preference**: Respects user's preferred language setting
- **Context-Aware**: Responds in the language the user wrote in
- **Override Capability**: Users can force responses in specific languages

### 3. User Customization
- **Personality Settings**: Choose ChatBot personality (friendly, professional, etc.)
- **Trait Selection**: Select preferred ChatBot characteristics
- **Profile Information**: Set nickname, occupation, and additional context
- **Language Preferences**: Set preferred interface and response languages

### 4. Seamless Language Switching
- **Real-time Updates**: Interface language changes immediately
- **Persistent Storage**: Language preferences saved in localStorage
- **Browser Detection**: Automatically detects user's browser language

## 📁 File Structure

```
chatbot/
├── src/main/java/com/yusufbatmaz/chatbot/
│   ├── model/
│   │   └── UserProfile.java              # User profile and preferences
│   ├── repository/
│   │   └── UserProfileRepository.java    # Profile data access
│   ├── service/
│   │   ├── LanguageDetectionService.java # Language detection logic
│   │   ├── UserProfileService.java       # Profile management
│   │   └── ChatService.java              # Enhanced chat with i18n
│   └── controller/
│       └── UserProfileController.java    # Profile REST endpoints

chatbot-frontend/
├── src/
│   ├── i18n/
│   │   ├── index.ts                      # i18n configuration
│   │   └── locales/
│   │       ├── en.json                   # English translations
│   │       ├── tr.json                   # Turkish translations
│   │       └── de.json                   # German translations
│   └── components/
│       ├── Profile/
│       │   ├── UserProfile.tsx           # Profile customization panel
│       │   └── UserProfile.css           # Profile styling
│       └── Common/
│           ├── LanguageSelector.tsx      # Language switcher
│           └── LanguageSelector.css      # Selector styling
```

## 🔧 Setup Instructions

### Backend Setup

1. **Database Migration**: The `UserProfile` entity will be automatically created
2. **Dependencies**: No additional dependencies required
3. **Configuration**: Services are automatically configured via Spring dependency injection

### Frontend Setup

1. **Install Dependencies**:
   ```bash
   cd chatbot-frontend
   npm install react-i18next i18next i18next-browser-languagedetector
   ```

2. **Import i18n**: Add `import './i18n';` to your main App component

3. **Use Translations**: Use the `useTranslation` hook in components:
   ```tsx
   import { useTranslation } from 'react-i18next';
   
   const { t } = useTranslation();
   const message = t('common.welcome');
   ```

## 💡 Usage Examples

### Language Detection in Chat

```java
// Backend - Automatic language detection
String detectedLanguage = languageDetectionService.detectLanguage(userMessage);
String responseLanguage = languageDetectionService.determineResponseLanguage(
    userMessage, userPreferredLanguage, null);
```

### Frontend Language Switching

```tsx
// Component with language support
const { t, i18n } = useTranslation();

// Change language
i18n.changeLanguage('tr');

// Use translations
<h1>{t('home.heroTitle')}</h1>
```

### User Profile Management

```tsx
// Open profile customization
<UserProfile
  userId={user.id}
  onClose={() => setShowProfile(false)}
/>
```

## 🌐 Translation Management

### Adding New Languages

1. **Create Locale File**: Add new language file in `src/i18n/locales/`
2. **Update i18n Config**: Add language to resources in `src/i18n/index.ts`
3. **Add Language Option**: Update `LanguageSelector` component

### Adding New Translation Keys

1. **Update All Locale Files**: Add the new key to all language files
2. **Use in Components**: Replace hardcoded text with `t('key.name')`

## 🔒 Security Features

- **Input Validation**: All user inputs are validated and sanitized
- **Language Injection Protection**: Language codes are validated before use
- **User Isolation**: Users can only access their own profile data

## 📱 Responsive Design

- **Mobile-First**: All components are mobile-responsive
- **Touch-Friendly**: Optimized for touch devices
- **Accessibility**: Proper ARIA labels and keyboard navigation

## 🧪 Testing

### Backend Testing
- **Unit Tests**: Test language detection algorithms
- **Integration Tests**: Test profile management workflows
- **API Tests**: Test REST endpoints with different languages

### Frontend Testing
- **Component Tests**: Test translation rendering
- **Language Switching**: Test language change functionality
- **Profile Management**: Test user profile customization

## 🚀 Future Enhancements

1. **More Languages**: Add support for additional languages
2. **Advanced Detection**: Implement ML-based language detection
3. **Voice Support**: Add voice input/output with language detection
4. **Cultural Adaptation**: Adapt responses based on cultural context
5. **Translation Memory**: Cache common translations for performance

## 📞 Support

For questions or issues with the i18n implementation:

1. Check the existing translations in locale files
2. Verify language detection patterns in `LanguageDetectionService`
3. Ensure proper i18n initialization in your main component
4. Check browser console for any JavaScript errors

## 📚 Additional Resources

- [React i18next Documentation](https://react.i18next.com/)
- [i18next Language Detection](https://github.com/i18next/i18next-browser-languagedetector)
- [Spring Boot Internationalization](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.internationalization)
