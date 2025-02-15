# AnkiAI - Intelligent Word Training Platform

## Overview
AnkiAI is an intelligent platform designed to help users learn and reinforce vocabulary in different languages. By leveraging machine learning and cloud-based technologies, AnkiAI generates contextual sentences for words, translates them into different languages, and tracks user progress over time. The system integrates with Google Sheets for training data storage and analysis.

## Features
- **Word Management**: Users can add new words and track their learning progress.
- **Intelligent Sentence Generation**: Uses Hugging Face's NLP models to generate contextually relevant sentences.
- **Automated Translation**: Sentences are automatically translated into Portuguese using AI-powered translation services.
- **Training Mode**: Enables users to practice words through interactive sentence generation and repetition.
- **Cloud-based Storage**: Integrates with Google Sheets for centralized tracking of learning progress.
- **RESTful API**: Provides endpoints for managing words, generating sentences, and retrieving user training data.

## Technology Stack
- **Frontend**: Angular for an interactive and responsive user interface.
- **Backend**: Spring Boot with RESTful APIs to handle requests and process data.
- **Database**: MongoDB for storing words, sentences, and training statistics.
- **Machine Learning**: Hugging Face AI models for sentence generation and translation.
- **Cloud Services**: Google Sheets API for tracking user progress in the cloud.
- **Containerization**: Docker for seamless deployment and scalability.

## Project Structure
```
AnkiAI/
├── frontend/           # Angular-based UI for word management and training
├── backend/            # Spring Boot backend handling requests and AI processing
│   ├── controller/     # API controllers for handling requests
│   ├── service/        # Business logic and AI integrations
│   ├── datasource/     # Data persistence with MongoDB and Google Sheets
│   ├── model/          # Data models for sentences and user progress
│   ├── repository/     # MongoDB repositories for persistence
├── docker/             # Docker setup for containerized deployment
└── README.md           # Project documentation
```

## Installation & Setup
### Prerequisites
- Node.js & Angular CLI (for frontend development)
- Java 17+ & Maven (for backend development)
- MongoDB (for local database storage)
- Docker (for containerization)

### Running the Application
#### 1. Clone the Repository
```bash
git clone https://github.com/emerson-diego/ankiai.git
cd ankiai
```

#### 2. Start the Backend
```bash
cd backend
mvn spring-boot:run
```

#### 3. Start the Frontend
```bash
cd frontend
npm install
ng serve --open
```

#### 4. Run with Docker (Optional)
```bash
docker-compose up --build
```

## API Endpoints
### Word Management
- `POST /sentences` - Add a new word
- `GET /sentences` - Retrieve all words

### Sentence Generation & Training
- `GET /generate/{id}` - Generate and translate a sentence for a word
- `GET /generate/random` - Generate a sentence for a random word

## Contribution Guidelines
We welcome contributions to improve AnkiAI. To contribute:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature-name`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature-name`)
5. Create a pull request

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact
For any inquiries or suggestions, please reach out to us at [contact@ankiai.com](mailto:diegowebby@gmail.com).

