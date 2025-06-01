# Gestione-delle-iscrizioni-ai-corsi
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

|**Attributo**|**Tipo**|**Esempio**|**Descrizione**|
| :- | :- | :- | :- |
|id|String|“1”|ID univoco dell’iscrizione|
|courseId|String |“1”|ID del corso di riferimento|
|studentId|String|“178001”|ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli)|
|teacherId|String|“178002”|ID del docente che ha effettuato l’iscrizione manuale (riferimento al microservizio Utenti e Ruoli)|
|enrollmentType|EnrollmentType (Enum)|SELF\_SERVICE|Tipo di iscrizione (SELF\_SERVICE, MANUAL\_BY\_TEACHER)|
|status|EnrollmentStatus (Enum)|ACTIVE|Stato dell’iscrizione (PENDING, APPROVED, REJECTED, ACTIVE)|
|enrollmentDate|String|“2024-03-15T10:30:00”|Data e ora dell’iscrizione|
|approvedDate|String|“2024-03-16T09:15:00”|Data e ora approvazione|
|notes|String|“Iscrizione approvata”|Note aggiuntive sull’iscrizione (opzionale)|

**CourseEnrollmentSettingsDTO – DTO per le configurazioni di iscrizione per corso**

|**Attributo**|**Tipo**|**Esempio**|**Descrizione**|
| :- | :- | :- | :- |
|id|String|“1”|ID univoco della configurazione|
|courseId|String |“1”|ID del corso configurato|
|enrollmentMode|EnrollmentMode (Enum)|SELF\_SERVICE|Modalità di iscrizione (SELF\_SERVICE, MANUAL, BOTH, DISABLED)|
|requiresApproval|Boolean|true|Indica se è richiesta l’approvazione|
|maxenrollment|Integer|50|Numero massimo di iscrizioni consentite|
|enrollmentStartDate|String|“2024-03-01T00:00:00”|Data inizio periodo iscrizioni|
|enrollmentEndDate|String|“2024-04-30T23:59:59”|Data dine periodo iscrizioni|
|allowWaitingList|Boolean|true|Permette lista d’attesa|
|createdBy|String|“178003”|ID dell'amministratore creatore (riferimento al microservizio Utenti e Ruoli)|
|createDate|String|“2024-02-28T14:20:00”|Data di creazione configurazione|
|LastModifyDate|String|“2024-03-10T16:45:00”|Data ultima modifica|

**EnrollmentRequestDTO – DTO per la richiesta di iscrizione**

|**Attributo**|**Tipo**|**Esempio**|**Descrizione**|
| :- | :- | :- | :- |
|id|String|“1”|ID univoco della richiesta|
|courseId|String |“1"|ID del corso richiesto|
|studentId|String|“178001”|ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli)|
|requestDate|String|“2024-03-14T11:20:00”|Data e ora della richiesta|
|status|RequestStatus (Enum)|PENDING|Stato della richiesta(PENDING, APPROVED, REJECTED, ACTIVE)|
|rejectionReason|String|“Corso al completo”|Motivo del rifiuto|
|processedBy|String|“178002”|ID di chi ha processato la richiesta (riferimento al microservizio Utenti)|
|processedDate|String|“2024-03-15T09:30:00”|Data e ora processamento|


**Struttura delle entità principali JPA**

**CourseEnrollment (Iscrizione)**

\- id (Long) – ID iscrizione

\- courseId (Long) - ID corso *// riferimento esterno al microservizio Corsi* 

\- studentId (Long) – ID studente *// riferimento esterno al microservizio Utenti* 

\- teacherId (Long) – ID docente che ha effettuato l’iscrizione *// riferimento esterno (chi ha fatto l'iscrizione manuale)* 

\- enrollmentType – Tipo iscrizione (SELF\_SERVICE, MANUAL\_BY\_TEACHER) 

\- status – Stato iscrizione (PENDING, APPROVED, REJECTED, ACTIVE) 

\- enrollmentDate (LocalDateTime) – Data iscrizione

\- approvedDate (LocalDateTime) – Data approvazione

\- notes (String) *// eventuali note*

**CourseEnrollmentSettings (Configurazioni di Iscrizione per Corso)**

\- id (Long) – ID configurazione

\- courseId (Long) – ID corso*// riferimento esterno* 

\- enrollmentMode – Modalità iscrizione (SELF\_SERVICE, MANUAL, BOTH, DISABLED) 

\- requiresApproval (Boolean) – Richiede approvazione

` `- maxEnrollments (Integer) – Limite massimo iscrizioni *// limite studenti* 

\- enrollmentStartDate (LocalDateTime) – Data inizio iscrizioni

\- enrollmentEndDate (LocalDateTime) – Data fine iscrizioni

\- allowWaitingList (Boolean) – Permetti lista d’attesa

\- createdBy (Long) – ID amministrativo creatore *// amministrativo che ha configurato* 

\- createdDate (LocalDateTime) – Data creazione

\- lastModifiedDate (LocalDateTime) – Data ultima modifica

**EnrollmentRequest (Richiesta di Iscrizione)**

\- id (Long) – ID richiesta

\- courseId (Long) – ID corso

\- studentId (Long) – ID studente

\- requestDate (LocalDateTime) – Data richiesta

\- status – Stato richiesta (PENDING, APPROVED, REJECTED) 

\- rejectionReason (String) - Motivo rifiuto

\- processedBy (Long) – ID processore richiesta *// docente/admin che ha processato* 

\- processedDate (LocalDateTime) – Data processamento


**API REST** 

**Gestione configurazioni iscrizione (Amministrativi)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/admin/courses/{courseId}/enrollment-settings|POST|createEnrollmentSettings()|<p>Authorization</p><p>courseId: String</p><p>settingsDTO: CourseEnrollmentSettingsDTO</p><p>Parametri:</p><p>id: String </p><p>courseId: String</p><p>enrollmentMode: EnrollmentMode</p><p>requiresApproval: Boolean</p><p>maxEnrollments: Integer</p><p>enrollmentStartDate: LocalDateTime</p><p>enrollmentEndDate: LocalDateTime</p><p>allowWaitingList: Boolean</p><p>createdBy: String</p><p>createdDate: LocalDateTime</p><p>lastModifiedDate: LocalDateTime</p>|Header, Path Variable, Request Body|CourseEnrollmentSettingsDTO|Crea una configurazione di iscrizione per un corso|
|/api/v1/admin/enrollment-settings|GET|getAllEnrollmentSettings()|Authorization|Header|List<CourseEnrollmentSettingsDTO>|Restituisce la lista di tutte le configurazioni di iscrizione|
|/api/v1/admin/courses/{courseId}/enrollment-settings|PUT|updateEnrollmentSettings()|<p>Authorization</p><p>courseId: String</p><p>settingsDTO: CourseEnrollmentSettingsDTO</p><p>Parametri:</p><p>id: String</p><p>courseId: String enrollmentMode: EnrollmentMode</p><p>requiresApproval: Boolean </p><p>maxEnrollments: Integer</p><p>enrollmentStartDate: LocalDateTime</p><p>enrollmentEndDate: LocalDateTime allowWaitingList: Boolean</p><p>createdBy: String</p><p>createdDate: LocalDateTime lastModifiedDate: LocalDateTime</p>|Header, Path Variable, Request Body|CourseEnrollmentSettingsDTO|Aggiorna la configurazione di iscrizione per un corso|
|/api/v1/admin/courses/{courseId}/enrollment-settings|GET|getEnrollmentSettingsByCourse()|<p>Authorization</p><p>courseId: String</p>|Header, Path Variable|CourseEnrollmentSettingsDTO|Restituisce i dettagli della configurazione di iscrizione per un corso|
|/api/v1/admin/courses/{courseId}/enrollment-settings|DELETE|deleteEnrollmentSettings()|Authorization, courseId: String|Header, Path Variable|Void|Elimina la configurazione di iscrizione per un corso|
|/api/v1/admin/courses/{courseId}/enrollments|GET|getCourseEnrollments()|Authorization. courseId: String|Header, Path Variable|List<CourseEnrollmentDTO>|Visualizza tutte le iscrizioni di un corso|
|/api/v1/admin/enrollments/{enrollmentId}|DELETE|deleteEnrollment()|Authorization, enrollmentId: String|Header, Path Variable|Void|Cancella una specifica iscrizione di uno studente|


**Gestione Richieste Iscrizione (Amministrativi)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/admin/courses/{courseId}/enrollment-request|GET|getPendingEnrollmentRequests()|Authorization, courseId: String|Header, Path Variable|List<EnrollmentRequestDTO>|Visualizza le richieste di iscrizione pendenti per un corso|
|/api/v1/admin/enrollment-request/{requestId}/approve|PUT|approveEnrollmentRequest()|Authorization, requestId: String|Header, Path Variable|EnrollmentRequestDTO|Approva una richiesta di iscrizione|
|/api/v1/admin/enrollment-request/{requestId}/reject|PUT|rejectEnrollmentRequest()|<p>Authorization, requestId: String</p><p>rejectReason: String</p>|Header, Path Variable,Request Body|EnrollmentRequestDTO|Rifiuta una richiesta di iscrizione specificando il motivo|


**Iscrizioni Manuali (Docenti)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/teachers/courses/{courseId}/enrollments/manual-enroll|POST|manualEnrollStudent()|<p>Authorization, courseId: String</p><p>enrollmentDTO: CourseEnrollmentDTO</p><p>Parametri:</p><p>id: String</p><p>courseId: String</p><p>studentId: String</p><p>teacherId: String</p><p>enrollmentType: EnrollmentType</p><p>status: EnrollmentStatus</p><p>enrollmentDate: LocalDateTime</p><p>approvedDate: LocalDateTime</p><p>notes: String</p>|Header, Path Variable, Request Body|CourseEnrollmentDTO|Iscrive manualmente uno studente a un corso|
|/api/v1/teachers/courses/{courseId}/enrollments/bulk-manual-enroll|POST|bulkManualEnrollStudents()|<p>Authorization, courseId: String</p><p>enrollmentDTOs: List<CourseEnrollmentDTO</p><p>Ogni elemento contiene:</p><p>id: String</p><p>courseId: String</p><p>studentId: String</p><p>teacherId: String</p><p>enrollmentType: EnrollmentType</p><p>status: EnrollmentStatus</p><p>enrollmentDate: LocalDateTime</p><p>approvedDate: LocalDateTime</p><p>notes: String</p>|Header, Path Variable, Request Body|List<CourseEnrollmentDTO>|Iscrive manualmente più studenti a un corso|
|/api/v1/teachers/courses/{courseId}/enrollments|GET|getOwnCourseEnrollments()|Authorization, courseId: String|Header, Path Variable|List<CourseEnrollmentDTO>|Visualizza le iscrizioni ad un corso gestito dal docente|
|/api/v1/teachers/courses/{courseId}/enrollment-request|GET|getOwnCoursePendingRequests()|Authorization, courseId: String|Header, Path Variable|List<EnrollmentRequestDTO>|Visualizza richieste pendenti per i propri corsi|
|/api/v1/teachers/enrollment-request/{requestId}/approve|PUT|approveEnrollmentRequestByTeacher()|Authorization, requestId: String|Header, Path Variable|EnrollmentRequestDTO|Il docente approva una richiesta di iscrizione|
|/api/v1/teachers/enrollment-requests/{requestId}/reject|PUT|rejectEnrollmentRequestByTeacher()|Authorization, requestId: String rejectionReason: String|<p>Header, Path Variable</p><p>Request Body</p>|EnrollmentRequestDTO|Il docente rifiuta una richiesta specificando il motivo|
|/api/v1/teachers/enrollments/{enrollmentId}|DELETE|deleteEnrollmentFromOwnCourse()|Authorization, enrollmentId: String|Header, Path Variable|Void|Rimuove una iscrizione dal proprio corso|
|/api/v1/teachers/courses/{courseId}/enrollment-stats|GET|getOwnCourseEnrollm|Authorization, courseId: String|Header, Path Variable|Map<String, Object>|Restituisce le statistiche di iscrizione per un corso gestito|

**Iscrizione Self-Service (Studenti)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/students/courses/{courseId}/enroll|POST|selfEnrollToCourse()|Authorization, courseId: String|Path Variable|CourseEnrollmentDTO|Iscrizione self-service a un corso|
|/api/v1/student/courses/isSelfService|GET|checkSelfServiceAvailability()|Authorization, courseId: String|Header, Query Parameter|Boolean|Controlla se un corso ha l’iscrizione self-service|
|/api/v1/student/enrollments|GET|getPersonalEnrollments()|Authorization|Header|List<CourseEnrollmentDTO>|Visualizza le proprie iscrizioni|
|/api/v1/students/enrollments/{enrollmentId}|DELETE|cancelPersonalEnrollment()|Authorization,enrollmentId: String|Header, Path Variable|Void|Cancella la propria iscrizione attiva|
|/api/v1/students/courses/{courseId}/enrollment-request|POST|requestEnrollmentToCourse()|<p>Authorization,courseId: String</p><p>requestDTO: EnrollmentRequestDTO</p><p>Parametri:</p><p>id: String</p><p>courseId: String</p><p>studentId: String requestDate: LocalDateTime</p><p>status: RequestStatus</p><p>rejectionReason: String</p><p>processedBy: String</p><p>processedDate:LocalDateTime</p>|Header, Path Variable, Request Body|EnrollmentRequestDTO|Invia una richiesta di iscrizione a un corso |
|/api/v1/students/enrollment-requests|GET|getPersonalEnrollmentRequests()|Authorization|Header|List<EnrollmentRequestDTO>|Visualizza le proprie iscrizione inviate|
|/api/v1/students/enrollment-request/{requestId}|DELETE|cancelPendingEnrollmentRequest(|Authorization,requestId: String|Authorization, Path Variable|Void|Annulla una richiesta di iscrizione pendente|

**Informazioni Pubbliche (Utenti Autenticati)**

|**Endpoint**|**Metodo**|**Funzione**|**Parametri Input**|**Tipo Parametro**|**Return Type**|**Descrizione**|
| :- | :- | :- | :- | :- | :- | :- |
|/api/v1/health|GET|healthCheck()|-|-|Boolean|Verifica lo stato di salute del microservizio|


**Integrazione con microservizi esterni**

Il microservizio Gestione delle iscrizioni ai corsi interagisce con i seguenti microservizi:

- Gestione Corsi: per verificare l’esistenza e la validità dei corsi, recuperare informazioni sui corsi e per verificare se un corso è attivo e disponibile per le iscrizioni.
- Gestione Utenti e Ruoli: per verificare l’identità e i permessi degli utenti e per autorizzare le operazioni
- Comunicazione e notifiche: per inviare notifiche quando un’iscrizione viene approvata/rifiutata.


