# Chatbot Startup Guide

## Prerequisites
- Java 17 or higher
- Node.js 14 or higher
- PostgreSQL running on localhost:5432
- Database `chatbotdb3` created

## Step 1: Start the Backend (Spring Boot)

1. **Navigate to the backend directory:**
   ```bash
   cd chatbot
   ```

2. **Start the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. **Verify the backend is running:**
   - Open: http://localhost:8080/api/chat/test
   - You should see: "Backend is running!"

## Step 2: Start the Frontend (React)

1. **Open a new terminal and navigate to the frontend directory:**
   ```bash
   cd chatbot-frontend
   ```

2. **Install dependencies (if not already done):**
   ```bash
   npm install
   ```

3. **Start the React development server:**
   ```bash
   npm start
   ```

4. **Verify the frontend is running:**
   - Open: http://localhost:3000
   - You should see the chat interface

## Troubleshooting

### Backend Issues:
- **Port 8080 already in use:** Change the port in `application.properties`
- **Database connection failed:** Ensure PostgreSQL is running and database exists
- **CORS errors:** The CORS configuration should handle this automatically

### Frontend Issues:
- **Port 3000 already in use:** React will automatically suggest another port
- **"Failed to fetch" error:** Ensure the backend is running on port 8080

### Database Setup:
```sql
CREATE DATABASE chatbotdb3;
```

## Testing the Application

1. **Backend Test:**
   ```bash
   curl -X GET http://localhost:8080/api/chat/test
   ```

2. **Frontend Test:**
   - Open http://localhost:3000
   - Type a message and press Enter
   - You should receive a response from the AI

## Common Commands

### Backend:
```bash
# Start backend
./mvnw spring-boot:run

# Clean and rebuild
./mvnw clean install

# Run tests
./mvnw test
```

### Frontend:
```bash
# Start frontend
npm start

# Build for production
npm run build

# Run tests
npm test
```

## Environment Variables

### Backend (.env or application.properties):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chatbotdb3
spring.datasource.username=postgres
spring.datasource.password=1234
```

### Frontend (.env):
```env
REACT_APP_API_URL=http://localhost:8080
``` 