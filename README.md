**Unimol: Microservizio Gestione delle Iscrizioni ai Corsi**

**Documentazione**

**Panoramica**

Il servizio Gestione delle Iscrizioni ai Corsi si occupa della gestione delle richieste di iscrizione degli studenti ai corsi e del processo di approvazione.

**Funzionalità:**

- **Amministrativi**
  - Definire le modalità di iscrizione per ciascun corso (self-service, manuale, entrambe o disabilitata).
  - Impostare parametri come approvazione di richiesta, limiti di iscrizione e liste d’attesa.
  - Visualizzare e gestire tutte le iscrizioni per corso con possibilità di cancellazione.
  - Gestione di approvazione/rifiuto delle richieste di iscrizione pendenti.
  - Accesso a tutte le configurazioni e statistiche del sistema.
- **Docenti**
  - Iscrizione manuale di uno o più studenti a un proprio corso.
  - Visualizzazione di tutte le iscrizioni ai propri corsi con possibilità di rimozione.
  - Approvazione o rifiuto delle richieste di iscrizione per i propri corsi.
  - Accesso alle statistiche dettagliate sulle iscrizioni dei propri corsi.
- **Studenti**
  - Iscrizione self-service a un corso che supporta questa modalità.
  - Invio di richieste di iscrizione ai docenti/amministratori per corsi che richiedono approvazione
  - Controllo della disponibilità di iscrizione self-service per un corso.
  - Visualizzazione e cancellazione delle proprie iscrizioni attive.
  - Monitoraggio dello stato delle richieste di iscrizione inviate.
  - Possibilità di annullare richieste di iscrizione pendenti

**Architettura del microservizio**

Framework: SpringBoot

Linguaggio: Java

Build Tool: Maven

Database: MySQL

Message Broker: RabbitMQ

Documentazione API: Swagger / OpenAPI

**Modello dati (DTO)**

**CourseEnrollmentDTO – DTO per l’iscrizione**

| **Attributo** | **Tipo** | **Esempio** | **Descrizione** |
| --- | --- | --- | --- |
| id  | String | “1” | ID univoco dell’iscrizione |
| courseId | String | “1” | ID del corso di riferimento |
| studentId | String | “178001” | ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli) |
| teacherId | String | “178002” | ID del docente che ha effettuato l’iscrizione manuale (riferimento al microservizio Utenti e Ruoli) |
| enrollmentType | EnrollmentType (Enum) | SELF_SERVICE | Tipo di iscrizione (SELF_SERVICE, MANUAL_BY_TEACHER) |
| status | EnrollmentStatus (Enum) | ACTIVE | Stato dell’iscrizione (PENDING, APPROVED, REJECTED, ACTIVE) |
| enrollmentDate | String | “2025-03-15T10:30:00” | Data e ora dell’iscrizione |
| approvedDate | String | “2025-03-16T09:15:00” | Data e ora approvazione |
| notes | String | “Iscrizione approvata” | Note aggiuntive sull’iscrizione (opzionale) |

**CourseEnrollmentSettingsDTO – DTO per le configurazioni di iscrizione per corso**

| **Attributo** | **Tipo** | **Esempio** | **Descrizione** |
| --- | --- | --- | --- |
| id  | String | “1” | ID univoco della configurazione |
| courseId | String | “1” | ID del corso configurato |
| enrollmentMode | EnrollmentMode (Enum) | SELF_SERVICE | Modalità di iscrizione (SELF_SERVICE, MANUAL, BOTH, DISABLED) |
| requiresApproval | Boolean | true | Indica se è richiesta l’approvazione |
| maxenrollment | Integer | 50  | Numero massimo di iscrizioni consentite |
| enrollmentStartDate | String | “2025-03-01T00:00:00” | Data inizio periodo iscrizioni |
| enrollmentEndDate | String | “2025-04-30T23:59:59” | Data dine periodo iscrizioni |
| allowWaitingList | Boolean | True | Permette lista d’attesa |
| createdBy | String | “178003” | ID dell'amministratore creatore (riferimento al microservizio Utenti e Ruoli) |
| createDate | String | “2025-02-28T14:20:00” | Data di creazione configurazione |
| LastModifyDate | String | “2025-03-10T16:45:00” | Data ultima modifica |

**EnrollmentRequestDTO – DTO per la richiesta di iscrizione**

| **Attributo** | **Tipo** | **Esempio** | **Descrizione** |
| --- | --- | --- | --- |
| id  | String | “1” | ID univoco della richiesta |
| courseId | String | “1" | ID del corso richiesto |
| studentId | String | “178001” | ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli) |
| requestDate | String | “2025-03-14T11:20:00” | Data e ora della richiesta |
| status | RequestStatus (Enum) | PENDING | Stato della richiesta(PENDING, APPROVED, REJECTED, ACTIVE) |
| rejectionReason | String | “Corso al completo” | Motivo del rifiuto |
| processedBy | String | “178002” | ID di chi ha processato la richiesta (riferimento al microservizio Utenti) |
| processedDate | String | “2025-03-15T09:30:00” | Data e ora processamento |

**Struttura delle entità principali JPA**

**CourseEnrollment (Iscrizione)**

\- id (String) – ID iscrizione

\- courseId (String) - ID corso _// riferimento esterno al microservizio Corsi_

\- studentId (String) – ID studente _// riferimento esterno al microservizio Utenti_

\- teacherId (String) – ID docente che ha effettuato l’iscrizione _// riferimento esterno (chi ha fatto l'iscrizione manuale)_

\- enrollmentType – Tipo iscrizione (SELF_SERVICE, MANUAL_BY_TEACHER)

\- status – Stato iscrizione (PENDING, APPROVED, REJECTED, ACTIVE)

\- enrollmentDate (LocalDateTime) – Data iscrizione

\- approvedDate (LocalDateTime) – Data approvazione

\- notes (String) _// eventuali note_

**CourseEnrollmentSettings (Configurazioni di Iscrizione per Corso)**

\- id (String) – ID configurazione

\- courseId (String) – ID corso_// riferimento esterno_

\- enrollmentMode – Modalità iscrizione (SELF_SERVICE, MANUAL, BOTH, DISABLED)

\- requiresApproval (Boolean) – Richiede approvazione

\- maxEnrollments (Integer) – Limite massimo iscrizioni

\- enrollmentStartDate (LocalDateTime) – Data inizio iscrizioni

\- enrollmentEndDate (LocalDateTime) – Data fine iscrizioni

\- allowWaitingList (Boolean) – Permetti lista d’attesa

\- createdBy (String) – ID amministrativo creatore _// amministrativo che ha configurato_

\- createdDate (LocalDateTime) – Data creazione

\- lastModifiedDate (LocalDateTime) – Data ultima modifica

**EnrollmentRequest (Richiesta di Iscrizione)**

\- id (String) – ID richiesta

\- courseId (String) – ID corso

\- studentId (String) – ID studente

\- requestDate (LocalDateTime) – Data richiesta

\- status – Stato richiesta (PENDING, APPROVED, REJECTED)

\- rejectionReason (String) - Motivo rifiuto

\- processedBy (String) – ID processore richiesta _// docente/admin che ha processato_

\- processedDate (LocalDateTime) – Data processamento

**API REST**

**Gestione configurazioni iscrizione (Amministrativi)**

| **Endpoint** | **Metodo** | **Funzione** | **Parametri Input** | **Tipo Parametro** | **Return Type** | **Descrizione** |
| --- | --- | --- | --- | --- | --- | --- |
| /api/v1/admin/courses/{courseId}/enrollment-settings | POST | createEnrollmentSettings() | Authorization<br><br>courseId: String<br><br>settingsDTO: CourseEnrollmentSettingsDTO<br><br>Parametri:<br><br>enrollmentMode: EnrollmentMode<br><br>requiresApproval: Boolean<br><br>maxEnrollments: Integer<br><br>enrollmentStartDate: LocalDateTime<br><br>enrollmentEndDate: LocalDateTime<br><br>allowWaitingList: Boolean | Header, Path Variable, Request Body | CourseEnrollmentSettingsDTO | Crea una configurazione di iscrizione per un corso |
| /api/v1/admin/enrollment-settings | GET | getAllEnrollmentSettings() | Authorization | Header | List&lt;CourseEnrollmentSettingsDTO&gt; | Restituisce la lista di tutte le configurazioni di iscrizione |
| /api/v1/admin/courses/{courseId}/enrollment-settings-update | PUT | updateEnrollmentSettings() | Authorization<br><br>courseId: String<br><br>settingsDTO: CourseEnrollmentSettingsDTO<br><br>Parametri:<br><br>id: String<br><br>courseId: String enrollmentMode: EnrollmentMode<br><br>requiresApproval: Boolean<br><br>maxEnrollments: Integer<br><br>enrollmentStartDate: LocalDateTime<br><br>enrollmentEndDate: LocalDateTime allowWaitingList: Boolean<br><br>createdBy: String<br><br>createdDate: LocalDateTime | Header, Path Variable, Request Body | CourseEnrollmentSettingsDTO | Aggiorna la configurazione di iscrizione per un corso |
| /api/v1/admin/courses/{courseId}/enrollment-settings-details | GET | getEnrollmentSettingsByCourse() | Authorization<br><br>courseId: String | Header, Path Variable | CourseEnrollmentSettingsDTO | Restituisce i dettagli della configurazione di iscrizione per un corso |
| /api/v1/admin/courses/{courseId}/enrollment-settings-delete | DELETE | deleteEnrollmentSettings() | Authorization, courseId: String | Header, Path Variable | Void | Elimina la configurazione di iscrizione per un corso |
| /api/v1/admin/courses/{courseId}/enrollments | GET | getCourseEnrollments() | Authorization. courseId: String | Header, Path Variable | List&lt;CourseEnrollmentDTO&gt; | Visualizza tutte le iscrizioni di un corso |
| /api/v1/admin/enrollments/{enrollmentId} | DELETE | deleteEnrollment() | Authorization, enrollmentId: String | Header, Path Variable | Void | Cancella una specifica iscrizione di uno studente |

**Gestione Richieste Iscrizione (Amministrativi)**

| **Endpoint** | **Metodo** | **Funzione** | **Parametri Input** | **Tipo Parametro** | **Return Type** | **Descrizione** |
| --- | --- | --- | --- | --- | --- | --- |
| /api/v1/admin/courses/{courseId}/enrollment-request | GET | getPendingEnrollmentRequests() | Authorization, courseId: String | Header, Path Variable | List&lt;EnrollmentRequestDTO&gt; | Visualizza le richieste di iscrizione pendenti per un corso |
| /api/v1/admin/enrollment-request/{requestId}/approve | PUT | approveEnrollmentRequest() | Authorization, requestId: String | Header, Path Variable | EnrollmentRequestDTO | Approva una richiesta di iscrizione |
| /api/v1/admin/enrollment-request/{requestId}/reject | PUT | rejectEnrollmentRequest() | Authorization, requestId: String<br><br>rejectionReason: String | Header, Path Variable,Request Body | EnrollmentRequestDTO | Rifiuta una richiesta di iscrizione specificando il motivo |

**Iscrizioni Manuali (Docenti)**

| **Endpoint** | **Metodo** | **Funzione** | **Parametri Input** | **Tipo Parametro** | **Return Type** | **Descrizione** |
| --- | --- | --- | --- | --- | --- | --- |
| /api/v1/teachers/courses/{courseId}/enrollments/manual-enroll | POST | manualEnrollStudent() | Authorization, courseId: String<br><br>enrollmentDTO: CourseEnrollmentDTO<br><br>Parametri:<br><br>studentId: String<br><br>notes: String (opzionale) | Header, Path Variable, Request Body | CourseEnrollmentDTO | Iscrive manualmente uno studente a un corso |
| /api/v1/teachers/courses/{courseId}/enrollments/bulk-manual-enroll | POST | bulkManualEnrollStudents() | Authorization, courseId: String<br><br>enrollmentDTOs: List<CourseEnrollmentDTO<br><br>Ogni elemento contiene:<br><br>id: String<br><br>courseId: String<br><br>studentId: String<br><br>teacherId: String<br><br>enrollmentType: EnrollmentType<br><br>status: EnrollmentStatus<br><br>enrollmentDate: LocalDateTime<br><br>approvedDate: LocalDateTime<br><br>notes: String | Header, Path Variable, Request Body | List&lt;CourseEnrollmentDTO&gt; | Iscrive manualmente più studenti a un corso |
| /api/v1/teachers/courses/{courseId}/enrollments | GET | getOwnCourseEnrollments() | Authorization, courseId: String | Header, Path Variable | List&lt;CourseEnrollmentDTO&gt; | Visualizza le iscrizioni ad un corso gestito dal docente |
| /api/v1/teachers/courses/{courseId}/enrollment-request/pending | GET | getOwnCoursePendingRequests() | Authorization, courseId: String | Header, Path Variable | List&lt;EnrollmentRequestDTO&gt; | Visualizza richieste pendenti per un propriocorso |
| /api/v1/teachers/enrollment-request/{requestId}/approve | PUT | approveEnrollmentRequestByTeacher() | Authorization, requestId: String | Header, Path Variable | EnrollmentRequestDTO | Il docente approva una richiesta di iscrizione |
| /api/v1/teachers/enrollment-requests/{requestId}/reject | PUT | rejectEnrollmentRequestByTeacher() | Authorization, requestId: String, rejectionReason: String | Header, Path Variable,<br><br>Request Body | EnrollmentRequestDTO | Il docente rifiuta una richiesta specificando il motivo |
| /api/v1/teachers/enrollments/{enrollmentId}/remove | DELETE | deleteEnrollmentFromOwnCourse() | Authorization, enrollmentId: String,<br><br>reason: String (opzionale) | Header, Path Variable, Request Body | Void | Rimuove una iscrizione dal proprio corso |

**Iscrizione Self-Service (Studenti)**

| **Endpoint** | **Metodo** | **Funzione** | **Parametri Input** | **Tipo Parametro** | **Return Type** | **Descrizione** |
| --- | --- | --- | --- | --- | --- | --- |
| /api/v1/students/courses/{courseId}/self-enroll | POST | selfEnrollToCourse() | Authorization, courseId: String | Path Variable | CourseEnrollmentDTO | Iscrizione self-service a un corso |
| /api/v1/students/courses/isSelfService | GET | checkSelfServiceAvailability() | Authorization, courseId: String | Header, Request Parameter | Boolean | Controlla se un corso ha l’iscrizione self-service |
| /api/v1/students/enrollments | GET | getPersonalEnrollments() | Authorization | Header | List&lt;CourseEnrollmentDTO&gt; | Visualizza le proprie iscrizioni |
| /api/v1/students/enrollments/{enrollmentId}/delete | DELETE | cancelPersonalEnrollment() | Authorization,<br><br>enrollmentId: String | Header, Path Variable | Void | Cancella la propria iscrizione attiva |
| /api/v1/students/courses/{courseId}/enrollment-request | POST | requestEnrollmentToCourse() | Authorization,<br><br>courseId: String | Header, Path Variable | EnrollmentRequestDTO | Invia una richiesta di iscrizione a un corso |
| /api/v1/students/enrollment-requests | GET | getPersonalEnrollmentRequests() | Authorization | Header | List&lt;EnrollmentRequestDTO&gt; | Visualizza le proprie iscrizioni inviate |
| /api/v1/students/enrollment-request/{requestId}/pending/delete | DELETE | cancelPendingEnrollmentRequest( | Authorization,<br><br>requestId: String | Header, Path Variable | Void | Annulla una richiesta di iscrizione pendente |

**Informazioni Pubbliche sul microservizio (per gli utenti autenticati)**

| **Endpoint** | **Metodo** | **Funzione** | **Parametri Input** | **Tipo Parametro** | **Return Type** | **Descrizione** |
| --- | --- | --- | --- | --- | --- | --- |
| /api/v1/health | GET | healthCheck() | Authorization | Header | Boolean | Verifica lo stato di salute del microservizio |

**Integrazione con microservizi esterni**

Il microservizio Gestione delle iscrizioni ai corsi interagisce con i seguenti microservizi:

- Gestione Corsi: per verificare l’esistenza e la validità dei corsi, recuperare informazioni sui corsi e per verificare se un corso è attivo e disponibile per le iscrizioni.
- Gestione Utenti e Ruoli: per verificare l’identità e i permessi degli utenti e per autorizzare le operazioni.
