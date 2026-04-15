# SnapBill – Event-Driven Email Processing Pipeline

## 🚀 Overview

SnapBill is a backend system designed to automatically extract financial transactions from user emails. It connects to email providers (e.g., Gmail), syncs messages, processes them through an AI extraction pipeline, and stores structured transaction data.

This project is built with a strong focus on:

* Concurrency
* Reliability
* Idempotency
* Fault tolerance
* Clean architecture

---

## 🧠 Architecture Summary

The system follows an **event-driven pipeline**:

```
OAuth/Login/Scheduler
        ↓
Publish Event (EmailSyncRequested)
        ↓
Event Listener (Async)
        ↓
Email Sync (fetch emails from provider)
        ↓
Store Raw Emails
        ↓
Claim Emails for Processing
        ↓
Parallel Processing (LLM Extraction)
        ↓
Transaction Persistence
```

---

## 🧭 Architecture Diagrams

### 1. High-Level System Architecture

```
                ┌──────────────────────┐
                │   Client (UI/API)    │
                └─────────┬────────────┘
                          │
                          ▼
                ┌──────────────────────┐
                │   Application Layer  │
                │ (Controllers/Auth)   │
                └─────────┬────────────┘
                          │
                          ▼
                ┌──────────────────────┐
                │   Event Publisher    │
                └─────────┬────────────┘
                          │
                          ▼
                ┌──────────────────────┐
                │   Event Listeners    │
                │   (Async Workers)    │
                └─────────┬────────────┘
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
┌──────────────────────┐        ┌──────────────────────┐
│   Email Sync Service │        │ Email Processing Svc │
└─────────┬────────────┘        └─────────┬────────────┘
          │                               │
          ▼                               ▼
┌──────────────────────┐        ┌──────────────────────┐
│ Email Provider APIs  │        │     LLM (Groq)       │
│   (Gmail, etc.)      │        │  (AI Extraction)     │
└─────────┬────────────┘        └─────────┬────────────┘
          │                               │
          └───────────────┬───────────────┘
                          ▼
                ┌──────────────────────┐
                │      Database        │
                │ (Emails + Txns)      │
                └──────────────────────┘
```

---

### 2. Email Processing Pipeline (Detailed)

```
        Event Triggered (Login/Scheduler)
                        │
                        ▼
            EmailSyncRequested Event
                        │
                        ▼
            ┌──────────────────────┐
            │   Sync Email Account │
            └─────────┬────────────┘
                      │
                      ▼
        Fetch Message IDs (Gmail API)
                      │
                      ▼
        Fetch Metadata (Concurrent)
                      │
                      ▼
        Filter Financial Candidates
                      │
                      ▼
        Fetch Full Messages (Concurrent)
                      │
                      ▼
        Store Raw Emails (DB)
                      │
                      ▼
        Claim Emails for Processing
                      │
                      ▼
        Parallel Processing (Virtual Threads)
                      │
                      ▼
        ┌──────────────────────────────┐
        │  Clean Email Text            │
        │  Build Prompt                │
        │  Call LLM                   │
        │  Validate Result            │
        │  Build Transaction          │
        └────────────┬─────────────────┘
                     │
                     ▼
        Save Transaction + Update Status
```

---

### 3. Concurrency Model

```
                GLOBAL LIMIT (optional)
                        │
                        ▼
                USER LIMIT (future)
                        │
                        ▼
              ACCOUNT SEMAPHORE (current)
                        │
                        ▼
         ┌──────────────────────────────┐
         │  Virtual Threads Pool        │
         │  (pipelineExecutor)          │
         └────────────┬─────────────────┘
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
   Email 1       Email 2       Email 3
   (LLM call)    (LLM call)    (LLM call)
```

---

### 4. Claiming & Recovery Flow

```
        SELECT eligible emails
        (PENDING or STUCK)
                │
                ▼
        UPDATE → CLAIM
        - processed = PROCESSING
        - processingStartedAt = now
        - claimToken = UUID
                │
                ▼
        FETCH by claimToken (JOIN FETCH)
                │
                ▼
        PROCESS in parallel
                │
        ┌───────┴────────┐
        ▼                ▼
   SUCCESS           FAILURE
        │                │
        ▼                ▼
  PROCESSED        retryCount++
                    nextRetryAt set

        (If worker crashes)
                │
                ▼
processingStartedAt < cutoff
                │
                ▼
        RECLAIM and retry
```

---

## ⚙️ Key Concepts

### 1. Event-Driven Processing

* All heavy work is triggered via events
* Login and scheduler both publish events
* Processing happens asynchronously using virtual threads

---

### 2. Email Syncing

* Emails are fetched in batches
* Metadata and full messages are fetched separately
* Concurrency is controlled per account using semaphores

---

### 3. Claim-Based Processing (Core Design)

To prevent race conditions and duplicate processing, the system uses a **claiming mechanism**.

#### Flow:

1. Select eligible emails (PENDING or STUCK)
2. Claim them using a unique `claimToken`
3. Set:

    * `processed = PROCESSING`
    * `processingStartedAt = now`
4. Fetch claimed emails
5. Process in parallel

#### Why?

* Prevents multiple threads from processing the same email
* Enables safe retries
* Enables stuck job recovery

---

### 4. Processing States

Each email goes through:

* `PENDING` → Not yet processed
* `PROCESSING` → Claimed and being worked on
* `PROCESSED` → Successfully extracted
* `FAILED` → Failed after retries

---

### 5. Timeout Recovery

If a job gets stuck:

```
processingStartedAt < now - timeout
```

It is automatically reclaimed and retried.

---

### 6. Retry Mechanism

* Controlled via `retryCount` and `nextRetryAt`
* Exponential or fixed delay retry supported
* Prevents infinite retry loops

---

### 7. Idempotency

To prevent duplicate transactions:

* Each email has a `providerMessageId`
* Transaction table enforces uniqueness

This guarantees:

* No duplicate transactions
* Safe retries

---

### 8. Concurrency Model

The system uses **virtual threads + semaphores**:

* Per-account concurrency limit
* Parallel processing of emails
* Non-blocking I/O operations

---

### 9. Lazy Loading Safety

Because processing happens asynchronously:

* Entities are fetched using `JOIN FETCH`
* Prevents `LazyInitializationException`

---

## 🧩 Modules

### Domain Layer

* Pure Java (no framework dependencies)
* Contains business rules and entities

### Infrastructure Layer

* JPA repositories
* Email provider integration
* External API calls

### Application Layer

* Event publishing
* Event listeners
* Scheduler
* Configuration

---

## 📦 Core Components

### Email Sync Service

* Fetches emails from provider
* Stores raw emails

### Email Processing Service

* Claims emails
* Processes them in batches
* Handles retries and failures

### Expense Extraction Service

* Cleans email content
* Builds prompt
* Calls AI (LLM)
* Validates result
* Builds transaction

---

## 🤖 AI Extraction

* Each email is processed via an LLM
* Returns structured JSON
* Validated before persistence

---

## 🔒 Reliability Guarantees

The system ensures:

* No duplicate processing
* No lost jobs
* Automatic recovery from crashes
* Controlled retries
* Safe concurrent execution

---

## ⚠️ Known Constraints

* LLM rate limits (e.g., Groq free tier)
* External API latency
* Cost per LLM call

---

## 📈 Future Improvements

* Observability (metrics, tracing, dashboards)
* Queue system (Kafka / RabbitMQ)
* Multi-tenant fairness (user-level throttling)
* Local LLM integration (e.g., DeepSeek)
* Cost control and usage limits

---

## 🧪 Running the System

1. Connect email account via OAuth
2. Trigger sync (login or scheduler)
3. Emails are fetched and stored
4. Processing pipeline runs automatically

---

## 💡 Key Takeaways

* Built as a fault-tolerant pipeline
* Designed for scalability and concurrency
* Ready for SaaS evolution

---

## 🏁 Conclusion

SnapBill is not just an email parser — it is a resilient, event-driven processing system capable of handling real-world workloads with strong guarantees around consistency, concurrency, and recovery.

---

## 👨‍💻 Author Notes

This project demonstrates advanced backend engineering concepts including:

* Event-driven architecture
* Concurrency control
* Distributed system patterns (within a single service)
* Idempotent processing
* Fault tolerance design

---

**Next Step:**