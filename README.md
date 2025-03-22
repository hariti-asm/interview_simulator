# 🤖 AI Interview Simulator 🎯

## 📋 Overview

The AI Interview Simulator is an intelligent platform designed to help job seekers ace their technical interviews! 🚀

Using advanced AI technology, this platform creates realistic interview scenarios tailored to specific job positions, technical specializations, and experience levels. Practice makes perfect, and our simulator provides a safe environment to build confidence and improve your technical interviewing skills before the real thing.

## ✨ Features

- 🎯 **Personalized Interview Sessions**: Custom interviews based on job role, tech stack, and experience level
- 🧠 **Dynamic Question Generation**: AI generates relevant technical questions that match real-world scenarios
- ⚖️ **Intelligent Answer Evaluation**: Get your responses automatically assessed with detailed scoring
- 📊 **Comprehensive Feedback**: Receive actionable insights on strengths and areas for improvement
- 📈 **Skill-Based Assessment**: Track your performance across different technical competencies
- 👥 **User Management**: Different access levels for candidates and administrators
- 📆 **Progress Tracking**: Monitor your improvement over time with detailed statistics

## 🔧 Technical Architecture

The system is built with a robust technology stack:

- **Backend**: Java with Spring Boot framework
- **Persistence**: JPA/Hibernate with relational database support
- **Entity Model**:
    - 👤 User management (User, Admin, Candidate)
    - 💬 Interview sessions
    - ❓ Questions and answers repository
    - 🛠️ Skills and competencies tracking
    - 📏 Assessment metrics and algorithms

## 🚀 Getting Started

### Prerequisites

- ☕ Java 17 or higher
- 🔨 Maven
- 🗄️ PostgreSQL database
- 📧 Email account for notifications

### Environment Variables

The application requires the following environment variables to be set:

```
# API Keys
OPENAI_API_KEY=your_openai_api_key

# Security
JWT_SECRET=your_jwt_secret_key

# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/simulator
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Email Configuration
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_app_password
MAIL_HOST=smtp.example.com
MAIL_PORT=587
```

You can set these variables in:
- A `.env` file (add to `.gitignore`)
- Your hosting environment configuration
- Your IDE's run configuration

> ⚠️ **NEVER commit actual credentials to your repository**

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/hariti-asm/interview_simulator.git
   ```

2. Configure environment variables as described above

3. Build the project:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   java -jar target/interview-simulator.jar
   ```

## 🎮 How It Works

1. **Create Your Profile** 📝
    - Register as a candidate
    - Set up your experience level and technical background

2. **Start an Interview Session** 🎬
    - Select your target job position
    - Choose technical specialization
    - Set experience level for appropriate question difficulty

3. **Answer Questions** 💭
    - Respond to a series of AI-generated technical questions
    - Questions adjust based on your performance

4. **Get Instant Feedback** 📋
    - Receive scores for each answer
    - Get detailed explanations of correct approaches
    - Identify knowledge gaps and misconceptions

5. **Review and Improve** 📚
    - See comprehensive session summary
    - Get suggestions for study resources
    - Track progress across multiple sessions

## 👑 Admin Features

Administrators have access to additional capabilities:

- 🛠️ Manage the skills database and question repository
- ➕ Add or modify job positions and required skills
- 📊 View analytics on user performance and system usage
- ⚙️ Configure system parameters and AI behavior

## 🧩 Project Structure

The project follows a standard Spring Boot application structure:

- **entity**: Data models representing domain objects
- **repository**: Data access layer interfaces
- **service**: Business logic implementation
- **controller**: REST API endpoints for frontend communication
- **dto**: Data transfer objects for client-server exchange
- **mapper**: Object mapping utilities
- **util**: Helper classes and common functionality
- **config**: Application configuration classes
- **exception**: Custom exception handlers for error management

## 🤝 Contributing

We welcome contributions to improve the AI Interview Simulator! Here's how:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 🔮 Future Enhancements

We're constantly working to make the simulator even better:

- 📹 Integration with video conferencing for mock interviews with facial expression analysis
- 🧠 Support for non-technical interviews (behavioral, leadership, etc.)
- 🗣️ Enhanced AI capabilities for more natural conversation flow
- 📱 Mobile application for on-the-go practice
- 🔗 Integration with job posting platforms

## 📝 Feedback & Improvements

We're constantly enhancing the AI Interview Simulator based on user feedback. If you have suggestions or find any issues, please:

- Open an issue on GitHub
- Submit a pull request with improvements
- Contact the development team directly

Your input helps make this tool more effective for everyone preparing for technical interviews!

## 👏 Acknowledgments

- Special thanks to all contributors and testers
- Inspired by real-world interview experiences and challenges faced by job seekers
- Built with ❤️ to help candidates land their dream tech jobs