# 🧠 ChatBot Web Uygulaması (React + Spring Boot + PostgreSQL)

Bu proje, kullanıcıların giriş/kayıt olduktan sonra yapay zekâ destekli bir sohbet ekranında AI ile konuşabildiği tam işlevsel bir web uygulamasıdır. Arka uç Java (Spring Boot), ön yüz ise React + TypeScript ile geliştirilmiştir. Tüm kullanıcı ve mesaj verileri PostgreSQL veritabanında saklanır.

## 🚀 Özellikler

- ✅ E-posta ve şifre ile kullanıcı kaydı
- ✅ Giriş yapma ve çıkış yapma
- ✅ Chat ekranında kullanıcı mesajı gönderme
- ✅ OpenRouter API üzerinden AI'dan yanıt alma
- ✅ Mesaj ve yanıtları veritabanına kaydetme
- ✅ Modern ve responsive arayüz
- ✅ Tüm backend logic Spring Boot ile yazıldı (Firebase veya hazır servis kullanılmadı)

> 📌 Not: Projede sadece chatbot yanıtları dış servisten alınmaktadır (OpenRouter API). Tüm diğer sistemler geliştiriciye aittir.

## 🛠️ Kullanılan Teknolojiler

### Backend
- Java 17+
- Spring Boot
- Spring Web, Spring Data JPA
- PostgreSQL
- Lombok
- Maven

### Frontend
- React 18+
- TypeScript
- MUI (Material UI)
- Axios

### Diğer
- OpenRouter API (chatbot engine)
- Git & GitHub


## 🧪 Nasıl Çalışır?

1. Kullanıcı giriş yapar veya kayıt olur.
2. Giriş başarılı olursa chat ekranı açılır.
3. Kullanıcı mesaj yazar, gönder butonuna basar.
4. API’ye istek atılır, AI cevabı alınır.
5. Mesaj ve cevap veritabanına kaydedilir.
6. Kullanıcı çıkış yaptığında oturum sonlanır.

## 🖼️ Ekran Görüntüleri

### 🔐 Giriş Sayfası
<img width="1918" height="983" alt="Giris" src="https://github.com/user-attachments/assets/e80778b0-e156-479e-b21b-24322f581ec8" />


### 📝 Kayıt Sayfası
<img width="1917" height="986" alt="kaydol" src="https://github.com/user-attachments/assets/1057cc7e-6bd9-4f1d-a90e-9438b0c03b54" />


### 💬 Chat Ekranı
<img width="1915" height="982" alt="chatEkranı" src="https://github.com/user-attachments/assets/cb99405a-beab-4a12-8763-ca94825e0b25" />



## 📦 Kurulum ve Çalıştırma

### Backend (Spring Boot)

```bash
cd chat
./mvnw spring-boot:run

### Frontend (React)
bash
cd chat-frontend
npm install
npm run dev

