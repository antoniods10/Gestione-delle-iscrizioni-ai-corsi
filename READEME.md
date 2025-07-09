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

**Porta di default**

Server: <http://localhost:8080>

**Documentazione API**

Swagger UI: <http://localhost:8080>/swagger-ui/index.html

**Comunicazione** 

È stato usato RabbitMQ come sistema di messaggistica asincrono. Il microservizio Gestione delle Iscrizioni ai Corsi pubblica le modifiche al database con 6 routing key specifiche.

Exchange: enrollment.exchange

- enrollment.created 
- enrollment.approved
- enrollment.rejected
- enrollment.deleted
- enrollment.updated
- enrollment.request.submitted

**Modello dati (DTO)** 

**CourseEnrollmentDTO – DTO per l’iscrizione**

|**Attributo**|**Tipo**|**Esempio**|**Descrizione**|
| :- | :- | :- | :- |
|id|String|“1”|ID univoco dell’iscrizione|
|courseId|String |“1”|ID del corso di riferimento|
|studentId|String|“178001”|ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli)|
|teacherId|String|“178002”|ID del docente che ha effettuato l’iscrizione manuale (riferimento al microservizio Utenti e Ruoli)|
|enrollmentType|EnrollmentType (Enum)|SELF\_SERVICE|Tipo di iscrizione (SELF\_SERVICE, MANUAL\_BY\_TEACHER)|
|status|EnrollmentStatus (Enum)|ACTIVE|Stato dell’iscrizione (PENDING, APPROVED, REJECTED, ACTIVE)|
|enrollmentDate|String|“2025-03-15T10:30:00”|Data e ora dell’iscrizione|
|approvedDate|String|“2025-03-16T09:15:00”|Data e ora approvazione|
|notes|String|“Iscrizione approvata”|Note aggiuntive sull’iscrizione (opzionale)|

**CourseEnrollmentSettingsDTO – DTO per le configurazioni di iscrizione per corso**

|**Attributo**|**Tipo**|**Esempio**|**Descrizione**|
| :- | :- | :- | :- |
|id|String|“1”|ID univoco della configurazione|
|courseId|String |“1”|ID del corso configurato|
|enrollmentMode|EnrollmentMode (Enum)|SELF\_SERVICE|Modalità di iscrizione (SELF\_SERVICE, MANUAL, BOTH, DISABLED)|
|requiresApproval|Boolean|true|Indica se è richiesta l’approvazione|
|maxenrollment|Integer|50|Numero massimo di iscrizioni consentite|
|enrollmentStartDate|String|“2025-03-01T00:00:00”|Data inizio periodo iscrizioni|
|enrollmentEndDate|String|“2025-04-30T23:59:59”|Data dine periodo iscrizioni|
|allowWaitingList|Boolean|True|Permette lista d’attesa|
|createdBy|String|“178003”|ID dell'amministratore creatore (riferimento al microservizio Utenti e Ruoli)|
|createDate|String|“2025-02-28T14:20:00”|Data di creazione configurazione|
|LastModifyDate|String|“2025-03-10T16:45:00”|Data ultima modifica|

**EnrollmentRequestDTO – DTO per la richiesta di iscrizione**

|**Attributo**|**Tipo**|**Esempio**|**Descrizione**|
| :- | :- | :- | :- |
|id|String|“1”|ID univoco della richiesta|
|courseId|String |“1"|ID del corso richiesto|
|studentId|String|“178001”|ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli)|
|requestDate|String|“2025-03-14T11:20:00”|Data e ora della richiesta|
|status|RequestStatus (Enum)|PENDING|Stato della richiesta(PENDING, APPROVED, REJECTED, ACTIVE)|
|rejectionReason|String|“Corso al completo”|Motivo del rifiuto|
|processedBy|String|“178002”|ID di chi ha processato la richiesta (riferimento al microservizio Utenti)|
|processedDate|String|“2025-03-15T09:30:00”|Data e ora processamento|


**Struttura delle entità principali JPA**

**CourseEnrollment (Iscrizione)**

\- id (String) – ID iscrizione

\- courseId (String) - ID corso *// riferimento esterno al microservizio Corsi* 

\- studentId (String) – ID studente *// riferimento esterno al microservizio Utenti* 

\- teacherId (String) – ID docente che ha effettuato l’iscrizione *// riferimento esterno (chi ha fatto l'iscrizione manuale)* 

\- enrollmentType – Tipo iscrizione (SELF\_SERVICE, MANUAL\_BY\_TEACHER) 

\- status – Stato iscrizione (PENDING, APPROVED, REJECTED, ACTIVE) 

\- enrollmentDate (LocalDateTime) – Data iscrizione

\- approvedDate (LocalDateTime) – Data approvazione

\- notes (String) *// eventuali note*

**CourseEnrollmentSettings (Configurazioni di Iscrizione per Corso)**

\- id (String) – ID configurazione

\- courseId (String) – ID corso*// riferimento esterno* 

\- enrollmentMode – Modalità iscrizione (SELF\_SERVICE, MANUAL, BOTH, DISABLED) 

\- requiresApproval (Boolean) – Richiede approvazione

\- maxEnrollments (Integer) – Limite massimo iscrizioni

\- enrollmentStartDate (LocalDateTime) – Data inizio iscrizioni

\- enrollmentEndDate (LocalDateTime) – Data fine iscrizioni

\- allowWaitingList (Boolean) – Permetti lista d’attesa

\- createdBy (String) – ID amministrativo creatore *// amministrativo che ha configurato* 

\- createdDate (LocalDateTime) – Data creazione

\- lastModifiedDate (LocalDateTime) – Data ultima modifica

**EnrollmentRequest (Richiesta di Iscrizione)**

\- id (String) – ID richiesta

\- courseId (String) – ID corso

\- studentId (String) – ID studente

\- requestDate (LocalDateTime) – Data richiesta

\- status – Stato richiesta (PENDING, APPROVED, REJECTED) 

\- rejectionReason (String) - Motivo rifiuto

\- processedBy (String) – ID processore richiesta *// docente/admin che ha processato* 

\- processedDate (LocalDateTime) – Data processamento


**API REST** 

**Gestione configurazioni iscrizione (Amministrativi)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/admin/courses/{courseId}/enrollment-settings|POST|createEnrollmentSettings()|<p>Authorization</p><p>courseId: String</p><p>settingsDTO: CourseEnrollmentSettingsDTO</p><p>Parametri:</p><p>enrollmentMode: EnrollmentMode</p><p>requiresApproval: Boolean</p><p>enrollmentStartDate: LocalDateTime</p><p>enrollmentEndDate: LocalDateTime</p><p>allowWaitingList: Boolean</p>|Header, Path Variable, Request Body|CourseEnrollmentSettingsDTO|Crea una configurazione di iscrizione per un corso|
|/api/v1/admin/enrollment-settings|GET|getAllEnrollmentSettings()|Authorization|Header|List<CourseEnrollmentSettingsDTO>|Restituisce la lista di tutte le configurazioni di iscrizione|
|/api/v1/admin/courses/{courseId}/enrollment-settings-update|PUT|updateEnrollmentSettings()|<p>Authorization</p><p>courseId: String</p><p>settingsDTO: CourseEnrollmentSettingsDTO</p><p>Parametri:</p><p>courseId: String enrollmentMode: EnrollmentMode</p><p>requiresApproval: Boolean </p><p>maxEnrollments: Integer</p><p>enrollmentStartDate: LocalDateTime</p><p>enrollmentEndDate: LocalDateTime allowWaitingList: Boolean </p>|Header, Path Variable, Request Body|CourseEnrollmentSettingsDTO|Aggiorna la configurazione di iscrizione per un corso|
|/api/v1/admin/courses/{courseId}/enrollment-settings-details|GET|getEnrollmentSettingsByCourse()|<p>Authorization</p><p>courseId: String</p>|Header, Path Variable|CourseEnrollmentSettingsDTO|Restituisce i dettagli della configurazione di iscrizione per un corso|
|/api/v1/admin/courses/{courseId}/enrollment-settings-delete|DELETE|deleteEnrollmentSettings()|Authorization, courseId: String|Header, Path Variable|Void|Elimina la configurazione di iscrizione per un corso|
|/api/v1/admin/courses/{courseId}/enrollments|GET|getCourseEnrollments()|Authorization. courseId: String|Header, Path Variable|List<CourseEnrollmentDTO>|Visualizza tutte le iscrizioni di un corso|
|/api/v1/admin/enrollments/{enrollmentId}|DELETE|deleteEnrollment()|Authorization, enrollmentId: String|Header, Path Variable|Void|Cancella una specifica iscrizione di uno studente|


**Gestione Richieste Iscrizione (Amministrativi)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/admin/courses/{courseId}/enrollment-request|GET|getPendingEnrollmentRequests()|Authorization, courseId: String|Header, Path Variable|List<EnrollmentRequestDTO>|Visualizza le richieste di iscrizione pendenti per un corso|
|/api/v1/admin/enrollment-request/{requestId}/approve|PUT|approveEnrollmentRequest()|Authorization, requestId: String|Header, Path Variable|EnrollmentRequestDTO|Approva una richiesta di iscrizione|
|/api/v1/admin/enrollment-request/{requestId}/reject|PUT|rejectEnrollmentRequest()|<p>Authorization, requestId: String</p><p>rejectionReason: String</p>|Header, Path Variable,Request Body|EnrollmentRequestDTO|Rifiuta una richiesta di iscrizione specificando il motivo|


**Iscrizioni Manuali (Docenti)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/teachers/courses/{courseId}/enrollments/manual-enroll|POST|manualEnrollStudent()|<p>Authorization, courseId: String</p><p>enrollmentDTO: CourseEnrollmentDTO</p><p>Parametri:</p><p>studentId: String</p><p>notes: String (opzionale)</p>|Header, Path Variable, Request Body|CourseEnrollmentDTO|Iscrive manualmente uno studente a un corso|
|/api/v1/teachers/courses/{courseId}/enrollments/bulk-manual-enroll|POST|bulkManualEnrollStudents()|<p>Authorization, courseId: String</p><p>Array di stringhe di studentId ["178001", "178002", "178003"]</p>|Header, Path Variable, Request Body|List<CourseEnrollmentDTO>|Iscrive manualmente più studenti a un corso|
|/api/v1/teachers/courses/{courseId}/enrollments|GET|getOwnCourseEnrollments()|Authorization, courseId: String|Header, Path Variable|List<CourseEnrollmentDTO>|Visualizza le iscrizioni ad un corso gestito dal docente|
|/api/v1/teachers/courses/{courseId}/enrollment-request/pending|GET|getOwnCoursePendingRequests()|Authorization, courseId: String|Header, Path Variable|List<EnrollmentRequestDTO>|Visualizza richieste pendenti per un proprio corso|
|/api/v1/teachers/enrollment-request/{requestId}/approve|PUT|approveEnrollmentRequestByTeacher()|Authorization, requestId: String|Header, Path Variable|EnrollmentRequestDTO|Il docente approva una richiesta di iscrizione|
|/api/v1/teachers/enrollment-requests/{requestId}/reject|PUT|rejectEnrollmentRequestByTeacher()|Authorization, requestId: String, rejectionReason: String|<p>Header, Path Variable,</p><p>Request Body</p>|EnrollmentRequestDTO|Il docente rifiuta una richiesta specificando il motivo|
|/api/v1/teachers/enrollments/{enrollmentId}/remove|DELETE|deleteEnrollmentFromOwnCourse()|<p>Authorization, enrollmentId: String,</p><p>reason: String (opzionale)</p>|Header, Path Variable, Request Body|Void|Rimuove una iscrizione dal proprio corso|

**Iscrizione Self-Service (Studenti)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/students/courses/{courseId}/self-enroll|POST|selfEnrollToCourse()|Authorization, courseId: String|Path Variable|CourseEnrollmentDTO|Iscrizione self-service a un corso|
|/api/v1/students/courses/isSelfService|GET|checkSelfServiceAvailability()|Authorization, courseId: String|Header, Request Parameter|Boolean|Controlla se un corso ha l’iscrizione self-service|
|/api/v1/students/enrollments|GET|getPersonalEnrollments()|Authorization|Header|List<CourseEnrollmentDTO>|Visualizza le proprie iscrizioni|
|/api/v1/students/enrollments/{enrollmentId}/delete|DELETE|cancelPersonalEnrollment()|<p>Authorization,</p><p>enrollmentId: String</p>|Header, Path Variable|Void|Cancella la propria iscrizione attiva|
|/api/v1/students/courses/{courseId}/enrollment-request|POST|requestEnrollmentToCourse()|<p>Authorization,</p><p>courseId: String</p>|Header, Path Variable|EnrollmentRequestDTO|Invia una richiesta di iscrizione a un corso |
|/api/v1/students/enrollment-requests|GET|getPersonalEnrollmentRequests()|Authorization|Header|List<EnrollmentRequestDTO>|Visualizza le proprie iscrizioni inviate|
|/api/v1/students/enrollment-request/{requestId}/pending/delete|DELETE|cancelPendingEnrollmentRequest(|<p>Authorization,</p><p>requestId: String</p>|Header, Path Variable|Void|Annulla una richiesta di iscrizione pendente|

**Informazioni Pubbliche sul microservizio (per gli utenti autenticati)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/health|GET|healthCheck()|Authorization|Header|Boolean|Verifica lo stato di salute del microservizio|


**Integrazione con microservizi esterni**

Il microservizio Gestione delle iscrizioni ai corsi interagisce con i seguenti microservizi:

- Gestione Corsi: per verificare l’esistenza e la validità dei corsi, recuperare informazioni sui corsi e per verificare se un corso è attivo e disponibile per le iscrizioni.
- Gestione Utenti e Ruoli: per verificare l’identità e i permessi degli utenti e per autorizzare le operazioni.

