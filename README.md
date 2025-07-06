Unimol: Microservizio Gestione delle Iscrizioni ai Corsi
Documentazione
Panoramica 
Il servizio Gestione delle Iscrizioni ai Corsi si occupa della gestione delle richieste di iscrizione degli studenti ai corsi e del processo di approvazione. 

Funzionalità:
•	Amministrativi
o	Definire le modalità di iscrizione per ciascun corso (self-service, manuale, entrambe o disabilitata).
o	Impostare parametri come approvazione di richiesta, limiti di iscrizione e liste d’attesa.
o	Visualizzare e gestire tutte le iscrizioni per corso con possibilità di cancellazione.
o	Gestione di approvazione/rifiuto delle richieste di iscrizione pendenti.
o	Accesso a tutte le configurazioni e statistiche del sistema.
•	Docenti
o	Iscrizione manuale di uno o più studenti a un proprio corso.
o	Visualizzazione di tutte le iscrizioni ai propri corsi con possibilità di rimozione.
o	Approvazione o rifiuto delle richieste di iscrizione per i propri corsi.
o	Accesso alle statistiche dettagliate sulle iscrizioni dei propri corsi.
•	Studenti
o	Iscrizione self-service a un corso che supporta questa modalità.
o	Invio di richieste di iscrizione ai docenti/amministratori per corsi che richiedono approvazione
o	Controllo della disponibilità di iscrizione self-service per un corso.
o	Visualizzazione e cancellazione delle proprie iscrizioni attive.
o	Monitoraggio dello stato delle richieste di iscrizione inviate.
o	Possibilità di annullare richieste di iscrizione pendenti

Architettura del microservizio
Framework: SpringBoot
Linguaggio: Java
Build Tool: Maven
Database: MySQL 
Message Broker: RabbitMQ
Documentazione API: Swagger / OpenAPI



Modello dati (DTO) 
CourseEnrollmentDTO – DTO per l’iscrizione
Attributo	Tipo	Esempio	Descrizione
id	String	“1”	ID univoco dell’iscrizione
courseId	String 	“1”	ID del corso di riferimento
studentId	String	“178001”	ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli)
teacherId	String	“178002”	ID del docente che ha effettuato l’iscrizione manuale (riferimento al microservizio Utenti e Ruoli)
enrollmentType	EnrollmentType (Enum)	SELF_SERVICE	Tipo di iscrizione (SELF_SERVICE, MANUAL_BY_TEACHER)
status	EnrollmentStatus (Enum)	ACTIVE	Stato dell’iscrizione (PENDING, APPROVED, REJECTED, ACTIVE)
enrollmentDate	String	“2025-03-15T10:30:00”	Data e ora dell’iscrizione
approvedDate	String	“2025-03-16T09:15:00”	Data e ora approvazione
notes	String	“Iscrizione approvata”	Note aggiuntive sull’iscrizione (opzionale)

CourseEnrollmentSettingsDTO – DTO per le configurazioni di iscrizione per corso
Attributo	Tipo	Esempio	Descrizione
id	String	“1”	ID univoco della configurazione
courseId	String 	“1”	ID del corso configurato
enrollmentMode	EnrollmentMode (Enum)	SELF_SERVICE	Modalità di iscrizione (SELF_SERVICE, MANUAL, BOTH, DISABLED)
requiresApproval	Boolean	true	Indica se è richiesta l’approvazione
maxenrollment	Integer	50	Numero massimo di iscrizioni consentite
enrollmentStartDate	String	“2025-03-01T00:00:00”	Data inizio periodo iscrizioni
enrollmentEndDate	String	“2025-04-30T23:59:59”	Data dine periodo iscrizioni
allowWaitingList	Boolean	True	Permette lista d’attesa
createdBy	String	“178003”	ID dell'amministratore creatore (riferimento al microservizio Utenti e Ruoli)
createDate	String	“2025-02-28T14:20:00”	Data di creazione configurazione
LastModifyDate	String	“2025-03-10T16:45:00”	Data ultima modifica

EnrollmentRequestDTO – DTO per la richiesta di iscrizione
Attributo	Tipo	Esempio	Descrizione
id	String	“1”	ID univoco della richiesta
courseId	String 	“1"	ID del corso richiesto
studentId	String	“178001”	ID dello studente iscritto (riferimento al microservizio Utenti e Ruoli)
requestDate	String	“2025-03-14T11:20:00”	Data e ora della richiesta
status	RequestStatus (Enum)	PENDING	Stato della richiesta(PENDING, APPROVED, REJECTED, ACTIVE)
rejectionReason	String	“Corso al completo”	Motivo del rifiuto
processedBy	String	“178002”	ID di chi ha processato la richiesta (riferimento al microservizio Utenti)
processedDate	String	“2025-03-15T09:30:00”	Data e ora processamento


Struttura delle entità principali JPA
CourseEnrollment (Iscrizione)
- id (String) – ID iscrizione
- courseId (String) - ID corso // riferimento esterno al microservizio Corsi 
- studentId (String) – ID studente // riferimento esterno al microservizio Utenti 
- teacherId (String) – ID docente che ha effettuato l’iscrizione // riferimento esterno (chi ha fatto l'iscrizione manuale) 
- enrollmentType – Tipo iscrizione (SELF_SERVICE, MANUAL_BY_TEACHER) 
- status – Stato iscrizione (PENDING, APPROVED, REJECTED, ACTIVE) 
- enrollmentDate (LocalDateTime) – Data iscrizione
- approvedDate (LocalDateTime) – Data approvazione
- notes (String) // eventuali note

CourseEnrollmentSettings (Configurazioni di Iscrizione per Corso)
- id (String) – ID configurazione
- courseId (String) – ID corso// riferimento esterno 
- enrollmentMode – Modalità iscrizione (SELF_SERVICE, MANUAL, BOTH, DISABLED) 
- requiresApproval (Boolean) – Richiede approvazione
- maxEnrollments (Integer) – Limite massimo iscrizioni
- enrollmentStartDate (LocalDateTime) – Data inizio iscrizioni
- enrollmentEndDate (LocalDateTime) – Data fine iscrizioni
- allowWaitingList (Boolean) – Permetti lista d’attesa
- createdBy (String) – ID amministrativo creatore // amministrativo che ha configurato 
- createdDate (LocalDateTime) – Data creazione
- lastModifiedDate (LocalDateTime) – Data ultima modifica

EnrollmentRequest (Richiesta di Iscrizione)
- id (String) – ID richiesta
- courseId (String) – ID corso
- studentId (String) – ID studente
- requestDate (LocalDateTime) – Data richiesta
- status – Stato richiesta (PENDING, APPROVED, REJECTED) 
- rejectionReason (String) - Motivo rifiuto
- processedBy (String) – ID processore richiesta // docente/admin che ha processato 
- processedDate (LocalDateTime) – Data processamento


API REST 

Gestione configurazioni iscrizione (Amministrativi)
Endpoint	Metodo	Funzione	Parametri Input	Tipo Parametro	Return Type	Descrizione
/api/v1/admin/courses/{courseId}/enrollment-settings	POST	createEnrollmentSettings()	Authorization
courseId: String
settingsDTO: CourseEnrollmentSettingsDTO
Parametri:
id: String 
courseId: String
enrollmentMode: EnrollmentMode
requiresApproval: Boolean
maxEnrollments: Integer
enrollmentStartDate: LocalDateTime
enrollmentEndDate: LocalDateTime
allowWaitingList: Boolean
createdBy: String
createdDate: LocalDateTime
lastModifiedDate: LocalDateTime	Header, Path Variable, Request Body	CourseEnrollmentSettingsDTO	Crea una configurazione di iscrizione per un corso
/api/v1/admin/enrollment-settings	GET	getAllEnrollmentSettings()	Authorization	Header	List<CourseEnrollmentSettingsDTO>	Restituisce la lista di tutte le configurazioni di iscrizione
/api/v1/admin/courses/{courseId}/enrollment-settings-update	PUT	updateEnrollmentSettings()	Authorization
courseId: String
settingsDTO: CourseEnrollmentSettingsDTO
Parametri:
id: String
courseId: String enrollmentMode: EnrollmentMode
requiresApproval: Boolean 
maxEnrollments: Integer
enrollmentStartDate: LocalDateTime
enrollmentEndDate: LocalDateTime allowWaitingList: Boolean
createdBy: String
createdDate: LocalDateTime lastModifiedDate: LocalDateTime	Header, Path Variable, Request Body	CourseEnrollmentSettingsDTO	Aggiorna la configurazione di iscrizione per un corso
/api/v1/admin/courses/{courseId}/enrollment-settings-details	GET	getEnrollmentSettingsByCourse()	Authorization
courseId: String	Header, Path Variable	CourseEnrollmentSettingsDTO	Restituisce i dettagli della configurazione di iscrizione per un corso
/api/v1/admin/courses/{courseId}/enrollment-settings-delete	DELETE	deleteEnrollmentSettings()	Authorization, courseId: String	Header, Path Variable	Void	Elimina la configurazione di iscrizione per un corso
/api/v1/admin/courses/{courseId}/enrollments	GET	getCourseEnrollments()	Authorization. courseId: String	Header, Path Variable	List<CourseEnrollmentDTO>	Visualizza tutte le iscrizioni di un corso
/api/v1/admin/enrollments/{enrollmentId}	DELETE	deleteEnrollment()	Authorization, enrollmentId: String	Header, Path Variable	Void	Cancella una specifica iscrizione di uno studente


Gestione Richieste Iscrizione (Amministrativi)
Endpoint	Metodo	Funzione	Parametri Input	Tipo Parametro	Return Type	Descrizione
/api/v1/admin/courses/{courseId}/enrollment-request	GET	getPendingEnrollmentRequests()	Authorization, courseId: String	Header, Path Variable	List<EnrollmentRequestDTO>	Visualizza le richieste di iscrizione pendenti per un corso
/api/v1/admin/enrollment-request/{requestId}/approve	PUT	approveEnrollmentRequest()	Authorization, requestId: String	Header, Path Variable	EnrollmentRequestDTO	Approva una richiesta di iscrizione
/api/v1/admin/enrollment-request/{requestId}/reject	PUT	rejectEnrollmentRequest()	Authorization, requestId: String
rejectReason: String	Header, Path Variable,Request Body	EnrollmentRequestDTO	Rifiuta una richiesta di iscrizione specificando il motivo


Iscrizioni Manuali (Docenti)
Endpoint	Metodo	Funzione	Parametri Input	Tipo Parametro	Return Type	Descrizione
/api/v1/teachers/courses/{courseId}/enrollments/manual-enroll	POST	manualEnrollStudent()	Authorization, courseId: String
enrollmentDTO: CourseEnrollmentDTO
Parametri:
id: String
courseId: String
studentId: String
teacherId: String
enrollmentType: EnrollmentType
status: EnrollmentStatus
enrollmentDate: LocalDateTime
approvedDate: LocalDateTime
notes: String	Header, Path Variable, Request Body	CourseEnrollmentDTO	Iscrive manualmente uno studente a un corso
/api/v1/teachers/courses/{courseId}/enrollments/bulk-manual-enroll	POST	bulkManualEnrollStudents()	Authorization, courseId: String
enrollmentDTOs: List<CourseEnrollmentDTO
Ogni elemento contiene:
id: String
courseId: String
studentId: String
teacherId: String
enrollmentType: EnrollmentType
status: EnrollmentStatus
enrollmentDate: LocalDateTime
approvedDate: LocalDateTime
notes: String	Header, Path Variable, Request Body	List<CourseEnrollmentDTO>	Iscrive manualmente più studenti a un corso
/api/v1/teachers/courses/{courseId}/enrollments	GET	getOwnCourseEnrollments()	Authorization, courseId: String	Header, Path Variable	List<CourseEnrollmentDTO>	Visualizza le iscrizioni ad un corso gestito dal docente
/api/v1/teachers/courses/{courseId}/enrollment-request/pending	GET	getOwnCoursePendingRequests()	Authorization, courseId: String	Header, Path Variable	List<EnrollmentRequestDTO>	Visualizza richieste pendenti per un propriocorso
/api/v1/teachers/enrollment-request/{requestId}/approve	PUT	approveEnrollmentRequestByTeacher()	Authorization, requestId: String	Header, Path Variable	EnrollmentRequestDTO	Il docente approva una richiesta di iscrizione
/api/v1/teachers/enrollment-requests/{requestId}/reject	PUT	rejectEnrollmentRequestByTeacher()	Authorization, requestId: String rejectionReason: String	Header, Path Variable
Request Body	EnrollmentRequestDTO	Il docente rifiuta una richiesta specificando il motivo
/api/v1/teachers/enrollments/{enrollmentId}/remove	DELETE	deleteEnrollmentFromOwnCourse()	Authorization, enrollmentId: String	Header, Path Variable	Void	Rimuove una iscrizione dal proprio corso

Iscrizione Self-Service (Studenti)
Endpoint	Metodo	Funzione	Parametri Input	Tipo Parametro	Return Type	Descrizione
/api/v1/students/courses/{courseId}/self-enroll	POST	selfEnrollToCourse()	Authorization, courseId: String	Path Variable	CourseEnrollmentDTO	Iscrizione self-service a un corso
/api/v1/students/courses/isSelfService	GET	checkSelfServiceAvailability()	Authorization, courseId: String	Header, Request Parameter	Boolean	Controlla se un corso ha l’iscrizione self-service
/api/v1/students/enrollments	GET	getPersonalEnrollments()	Authorization	Header	List<CourseEnrollmentDTO>	Visualizza le proprie iscrizioni
/api/v1/students/enrollments/{enrollmentId}/delete	DELETE	cancelPersonalEnrollment()	Authorization,
enrollmentId: String	Header, Path Variable	Void	Cancella la propria iscrizione attiva
/api/v1/students/courses/{courseId}/enrollment-request	POST	requestEnrollmentToCourse()	Authorization,
courseId: String
requestDTO: EnrollmentRequestDTO
Parametri:
id: String
courseId: String
studentId: String 
requestDate: LocalDateTime
status: RequestStatus
rejectionReason: String
processedBy: String
processedDate:LocalDateTime	Header, Path Variable, Request Body	EnrollmentRequestDTO	Invia una richiesta di iscrizione a un corso 
/api/v1/students/enrollment-requests	GET	getPersonalEnrollmentRequests()	Authorization	Header	List<EnrollmentRequestDTO>	Visualizza le proprie iscrizioni inviate
/api/v1/students/enrollment-request/{requestId}/pending/delete	DELETE	cancelPendingEnrollmentRequest(	Authorization,
requestId: String	Header, Path Variable	Void	Annulla una richiesta di iscrizione pendente

Informazioni Pubbliche sul microservizio (per gli utenti autenticati)
Endpoint	Metodo	Funzione	Parametri Input	Tipo Parametro	Return Type	Descrizione
/api/v1/health	GET	healthCheck()	Authorization	Header	Boolean	Verifica lo stato di salute del microservizio


Integrazione con microservizi esterni
Il microservizio Gestione delle iscrizioni ai corsi interagisce con i seguenti microservizi:
•	Gestione Corsi: per verificare l’esistenza e la validità dei corsi, recuperare informazioni sui corsi e per verificare se un corso è attivo e disponibile per le iscrizioni.
•	Gestione Utenti e Ruoli: per verificare l’identità e i permessi degli utenti e per autorizzare le operazioni.

