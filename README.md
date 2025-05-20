# 📱 Question Quizz - Aplicativo Mobile  

## 📌 Visão Geral  

O **Question Quizz** é um aplicativo de aprendizado interativo desenvolvido em **Kotlin** com **Jetpack Compose** e **Spring Boot**, projetado para oferecer uma experiência moderna de quizzes educacionais.  

🔗 **Repositório da API (Backend):** [https://github.com/sergio-rocha1/question_quizz.git](https://github.com/sergio-rocha1/question_quizz.git)  

---

## 🛠️ Pré-requisitos  

- **Android Studio Flamingo** ou versão mais recente  
- **JDK 17** (Java Development Kit)  
- **Docker** e **Docker Compose** (para ambiente de desenvolvimento)  
- **Gradle**  
- **Chave da API do Google Maps**

---

## ⚙️ Configuração do Ambiente  

### 1. Instalar o Android Studio  
- Baixe e instale o [Android Studio](https://developer.android.com/studio) (versão Flamingo ou superior).  
- Certifique-se de instalar o **Android SDK** e o **emulador** (opcional).  

### 2. Instalar o JDK 17+  
- Faça o download do [JDK 17+](https://www.oracle.com/java/technologies/javase-downloads.html) e configure-o no seu sistema.  

### 3. Clonar o Repositório  
```bash
git clone https://github.com/seu-usuario/question_quizz_app.git
cd question_quizz_app
```  

### 4. Configurar a Chave da API do Google Maps (Opcional)  
1. Crie um arquivo `local.properties` na raiz do projeto (caso não exista).  
2. Adicione sua chave da API do Google Maps:  
```properties
MAPS_API_KEY=sua_chave_aqui
```  
🔗 [Como obter uma chave da API do Google Maps](https://developers.google.com/maps/documentation/android-sdk/get-api-key)  

### 5. Abrir o Projeto no Android Studio  
- Selecione a pasta `question_quizz_app` no Android Studio.  
- Aguarde o **Gradle** sincronizar todas as dependências.  

### 6. Executar o Aplicativo  
- Conecte um **dispositivo físico** (Android 7.0+) ou inicie um **emulador**.  
- Clique em **"Run"** (▶️) no Android Studio para compilar e executar.  

---

## 📂 Estrutura do Projeto  

```
question_quizz_app/
└── app/
    └── src/
        └── main/
            ├── java/com/example/appestudos/
            │    ├── features/          # Funcionalidades principais
            │    │    ├── flashcards/   # Flashcards de estudo
            │    │    ├── quizz/        # Sistema de quizzes
            │    │    ├── map/          # Integração com mapas (Google Maps)
            │    │    ├── profile/      # Perfil do usuário
            │    │    ├── intro/        # Telas iniciais (onboarding)
            │    │    └── auth/         # Autenticação (login/cadastro)
            │    ├── navigation/        # Navegação entre telas
            │    └── ui/                # Componentes de interface
            ├── res/                    # Recursos (imagens, strings, cores)
            └── AndroidManifest.xml     # Configuração do app
```  

---

## 🚀 Tecnologias Utilizadas  

- **Kotlin** + **Jetpack Compose** → Interface moderna e reativa  
- **Room** → Armazenamento local de dados  
- **Google Maps & Places API** → Funcionalidades de localização  
- **Ktor** → Comunicação com a API (HTTP)  
- **Firebase** (Crashlytics, DataConnect) → Monitoramento e backend  
- **MVVM (Model-View-ViewModel)** → Arquitetura limpa  
- **Material Design 3** → Design moderno e responsivo  

---
