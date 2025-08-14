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

Tüm kontroller ve optimizasyon çalışmaları tamamlandıktan sonra ChatBot projesi, modern bir kullanıcı arayüzü, güçlü backend altyapısı ve yapay zekâ entegrasyonu ile kullanılabilir hale geldi. Bu bölümde projenin son sürümündeki tüm ana ekranlar görselleri ve açıklamaları ile sunulmaktadır.

 <img width="915" height="478" alt="image" src="https://github.com/user-attachments/assets/031fb493-f13d-4b8a-b1e9-8a04678d0a4c" />

Şekil 19.1. Giriş Yap Ekranı
Şekil 19.1’de görüldüğü üzere giriş ekranı, kullanıcıların e-posta/şifre ile sisteme erişebilmesini sağlayan modern ve responsive bir tasarıma sahiptir. Mor-mavi gradyan arka plan ve düzenli form yapısı sayesinde kullanıcı dostu bir deneyim sunulmaktadır. Yanlış girişlerde hata mesajları anlık olarak ekranda görüntülenmektedir.

 <img width="915" height="472" alt="image" src="https://github.com/user-attachments/assets/9fbe870e-61ad-4d90-9f63-5e1764259f39" />

Şekil 19.2. Kaydol Ekranı - Dark Mod
Şekil 19.2’de gösterilen kayıt ekranı, ad, soyad, e-posta, şifre ve şifre tekrar alanlarını içermektedir. Form validasyonu sayesinde eksik veya hatalı girişlerde kullanıcı uyarılmaktadır. Kayıt işlemi tamamlandığında kullanıcı bilgileri PostgreSQL veritabanındaki User tablosuna kaydedilmektedir.

<img width="915" height="468" alt="image" src="https://github.com/user-attachments/assets/3baeaed0-a770-46cc-a8c6-3d8fa335956c" />

Şekil 19.3. Ana Sohbet Ekranı
Şekil 19.3’te yer alan ana sohbet ekranı, kullanıcı ile yapay zekâ arasındaki etkileşimin gerçekleştiği alandır. Kullanıcı mesajları mavi baloncuklarda, yapay zekâ yanıtları ise sarı baloncuklarda görüntülenmektedir. Dark mod seçildiğinde bu renkler değişir. Sohbet geçmişi ChatHistory tablosundan çekilmekte, yeni mesajlar anlık olarak ekrana yansıtılmaktadır.

<img width="915" height="466" alt="image" src="https://github.com/user-attachments/assets/792045be-e9ed-4b8d-b61d-174f0144c873" />

Şekil 19.4. Profil Özelleştirme Ekranı
Şekil 19.4’te gösterilen profil özelleştirme ekranı, kullanıcıların yapay zekâ ile etkileşimlerini kişiselleştirmesine olanak tanımaktadır. Kullanıcı adı, meslek, kişilik tipi, karakter özellikleri ve ek bilgiler alanları doldurularak yapay zekânın cevap verme biçimi dinamik olarak değiştirilmektedir.

<img width="915" height="216" alt="image" src="https://github.com/user-attachments/assets/8ab76d67-f05b-4445-b1eb-0d8efc8c7c5a" />

Şekil 19.5. Dil Seçimi Özelliği
Şekil 19.5’te görülen dil seçim ekranı, Navbar üzerinde yer alan bayrak ikonlu açılır menü aracılığıyla Türkçe, İngilizce ve Almanca dillerinde hizmet vermektedir. Dil seçimi anında arayüze ve yapay zekâ yanıt diline yansımaktadır.

<img width="915" height="468" alt="image" src="https://github.com/user-attachments/assets/076d2458-5bbc-4c6b-a7de-9562ef9d5338" />

Şekil 19.6. Dark Mode Özelliği
Şekil 19.6’da gösterilen dark mode özelliği, kullanıcıların gece ve gündüz kullanım tercihlerine uygun olarak arayüz temasını değiştirebilmelerini sağlamaktadır. Tema geçişi sırasında tüm sayfa elemanları anlık olarak uyum sağlamaktadır.
Bu ekranlar ile ChatBot projesi hem görsel açıdan modern hem de işlevsel açıdan zengin bir yapıya kavuşmuştur. Her bir özellik, kullanıcı deneyimini artırmak ve proje gereksinimlerini karşılamak amacıyla geliştirilmiştir.




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

