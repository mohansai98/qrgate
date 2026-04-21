# QrGate - QR Code Authentication Gateway

QrGate is a Spring Boot application that enables secure, passwordless authentication using QR codes. It allows users already authenticated on a mobile device to log into a web application by scanning a QR code, similar to the login flows of WhatsApp Web or Discord.

## Features

- **QR Code Generation:** Dynamic generation of URL-embedded QR codes.
- **Mobile Authorization:** Secure bridging of mobile identity to desktop sessions using Spring Security's `Principal`.
- **User Registration:** Built-in registration system for creating and managing test accounts.
- **Session Exchange:** Programmatic login transition from QR scan to authenticated session.
- **SaaS Ready Architecture:** Designed to act as an identity proxy for existing authentication systems.
- **H2 In-Memory Database:** Pre-configured with automatic schema updates and initial data seeding.

## Prerequisites

- **Java 21**
- **Maven 3.x**
- **Mobile Device:** Must be on the same local network as the server.

## Getting Started

### 1. Configure Local IP
For mobile scanning to work, the QR code must contain your computer's local IP address rather than `localhost`.

1. Find your local IP (e.g., `192.168.1.98`).
2. Update `src/main/resources/application.properties`:
   ```properties
   qrgate.base-url=http://192.168.1.98:8080
   ```

### 2. Run the Application
```bash
./mvnw clean spring-boot:run
```

### 3. Test the Flow
1. **Desktop:** Open `http://localhost:8080/login`.
2. **Mobile:** Scan the QR code displayed on the screen.
3. **Mobile Login:** You will be prompted to log in on your phone.
   - Use seeded accounts: `admin / admin123` or `user / user123`.
   - Or register a new account via the **Register** link.
4. **Authorize:** Once logged in on mobile, click **Authorize Login**.
5. **Success:** Your desktop browser will automatically redirect to the Dashboard.

## Technical Stack

- **Framework:** Spring Boot 3.4.1
- **Security:** Spring Security 6 (Session & JWT)
- **Database:** H2 (In-memory) / Spring Data JPA
- **View Engine:** Thymeleaf
- **QR Library:** ZXing (Zebra Crossing)
- **Token Support:** JJWT

## License

This project is open-source and available under the MIT License.
