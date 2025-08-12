# PartyMaker - UML Diagrams

## 📊 Architecture Overview

This directory contains UML class diagrams for the PartyMaker application, organized by architectural layers and components.

---

## 🗂️ UML Diagrams

### 📱 User Interface Layer

**[UML_Activities.md](UML_Activities.md)**  
All Activity classes representing app screens - login, main dashboard, party management, chat, and settings.

**[UML_ViewModels.md](UML_ViewModels.md)**  
ViewModel classes that handle business logic and state management between UI and data layers.

**[UML_Adapters.md](UML_Adapters.md)**  
Adapter classes for displaying lists - parties, chat messages, users, and UI components.

---

### 🗄️ Data Layer

**[UML_Data_Models.md](UML_Data_Models.md)**  
Core data models - Group, User, ChatMessage, and ChatMessageGpt entities.

**[UML_Repositories.md](UML_Repositories.md)**  
Repository pattern implementation with local and remote data sources for offline/online synchronization.

---

### 🛠️ Infrastructure Layer

**[UML_Managers_Utils.md](UML_Managers_Utils.md)**  
Manager classes for specialized tasks - UI components, file handling, memory management, and business logic utilities.

**[UML_Security.md](UML_Security.md)**  
Security components including encryption, authentication, SSL pinning, and secure storage.

**[UML_Networking.md](UML_Networking.md)**  
Network layer with HTTP clients, connectivity management, and API communication.

---

### 🔥 Backend Integration

**[UML_Server.md](UML_Server.md)**  
Spring Boot server architecture with REST controllers, services, and configuration.

**[UML_Firebase.md](UML_Firebase.md)**  
Firebase integration for real-time database, authentication, and cloud services.

---

## 🏗️ Architecture Structure

### Client-Side Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Activities    │◄──►│   ViewModels    │◄──►│  Repositories   │
│   (24 screens)  │    │ (20 assistants) │    │ (data managers) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│    Adapters     │    │    Managers     │    │  Data Models    │
│  (6 organizers) │    │(23+ specialists)│    │  (4 templates)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Server & Integration
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Server      │◄──►│   Networking    │◄──►│   Firebase      │
│  (Spring Boot)  │    │  (HTTP/API)     │    │ (Real-time DB)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                  │
                                  ▼
                       ┌─────────────────┐
                       │    Security     │
                       │  (Encryption)   │
                       └─────────────────┘
```

---

## 📋 Component Summary

| Layer | Components | Count | Description |
|-------|------------|-------|-------------|
| **UI** | Activities, ViewModels, Adapters | 50 classes | User interface and presentation logic |
| **Data** | Models, Repositories, Data Sources | 10 classes | Data management and persistence |
| **Infrastructure** | Managers, Security, Networking | 40+ classes | Core services and utilities |
| **Backend** | Server, Firebase Integration | 7 classes | Server-side architecture |

**Total**: 100+ classes organized across all application layers

---

## 🎯 Key Features

- **MVVM Architecture**: Clean separation between UI, business logic, and data layers
- **Repository Pattern**: Unified data access with offline/online synchronization
- **Security First**: Comprehensive encryption and secure storage implementation
- **Real-time Communication**: Firebase integration for live chat and updates
- **Modular Design**: Well-organized components for maintainability and scalability

---

*Complete UML documentation for PartyMaker application architecture*
