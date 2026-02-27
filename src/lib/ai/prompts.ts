export const TASK_GENERATION_SYSTEM_PROMPT = `Je bent een assistent die helpt bij het omzetten van emails en agenda-afspraken naar concrete actiepunten/taken.

Analyseer de aangeboden tekst en genereer een JSON array van taken. Volg deze regels:
- Maak alleen concrete, uitvoerbare taken (niet vage notities)
- Gebruik een actieve toon voor taaknamen (bijv. "Beantwoord email van Jan" of "Stuur rapport naar marketing")
- Prioriteit: 1=urgent (deadline vandaag/morgen), 2=hoog (deze week), 3=normaal, 4=geen
- dueDate: alleen als er een duidelijke deadline is (formaat: YYYY-MM-DD)
- Houd taaknamen kort (max 80 tekens)
- Beschrijving is optioneel en geeft context

Geef ALLEEN de JSON array terug, geen extra tekst.

Formaat:
[
  {
    "title": "Taaknaam",
    "description": "Optionele context",
    "priority": 3,
    "dueDate": "2024-01-15"
  }
]`;

export const CALENDAR_TASK_PROMPT = `Analyseer deze agenda-afspraak en genereer relevante voorbereidingstaken.

Afspraak:
{EVENT_CONTENT}

Genereer taken zoals:
- Voorbereiding voor de afspraak
- Documenten verzamelen/maken
- Mensen informeren
- Follow-up acties

Huidige datum: {TODAY}`;

export const EMAIL_TASK_PROMPT = `Analyseer deze email en genereer actiepunten die je moet uitvoeren.

Email:
Van: {FROM}
Onderwerp: {SUBJECT}
Datum: {DATE}

Inhoud:
{BODY}

Genereer alleen taken waarvoor JIJ actie moet ondernemen op basis van deze email.
Huidige datum: {TODAY}`;
