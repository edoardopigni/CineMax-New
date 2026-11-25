/*
 * Progetto CineMax - Laboratorio Interdisciplinare A
 * Autori:
 * - Daniele Paoli, Matricola: 761614, Sede: VA
 * - Edoardo Pigni, Matricola: 761118, Sede: VA
 * - Anes Khaia, Matricola: 760038, Sede: VA
 */
package cinemax;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principale (Entry Point) dell'applicazione CineMax.
 * <p>
 * Gestisce l'interfaccia testuale da terminale (TUI), il menu iniziale,
 * i flussi Guest, Registrazione e Login, e le aree riservate per Cliente,
 * Proiezionista e Bigliettaio.
 * </p>
 *
 * @author Daniele Paoli
 * @author Edoardo Pigni
 * @author Anes Khaia
 * @version 1.1
 */
public class CineMax {

    /** Formatter per l'input di date senza orario (GG/MM/AAAA). */
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Numero massimo di proiezioni mostrate per singola pagina negli elenchi. */
    private static final int ELEMENTI_PER_PAGINA = 15;

    /**
     * Metodo di avvio principale dell'applicazione CineMax.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   BENVENUTO IN CINEMAX 2026");
        System.out.println("=================================");

        GestoreDati db = new GestoreDati();
        db.inizializzaSistema();

        Scanner scanner = new Scanner(System.in);
        boolean inEsecuzione = true;

        while (inEsecuzione) {
            System.out.println("\n=== MENU PRINCIPALE ===");
            System.out.println("1. Continua come Guest (Cerca e visualizza proiezioni)");
            System.out.println("2. Accedi alla piattaforma (Login)");
            System.out.println("3. Registrati come nuovo Cliente");
            System.out.println("0. Esci dal programma");
            System.out.print("> ");

            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1":
                    avviaModalitaGuest(scanner, db);
                    break;
                case "2":
                    eseguiLogin(scanner, db);
                    break;
                case "3":
                    eseguiRegistrazioneCliente(scanner, db);
                    break;
                case "0":
                    System.out.println("Uscita in corso. Grazie per aver usato CineMax!");
                    inEsecuzione = false;
                    break;
                default:
                    System.out.println("Scelta non valida. Riprova.");
            }
        }

        scanner.close();
    }

    // =========================================================================
    // LOGIN E REGISTRAZIONE
    // =========================================================================

    /**
     * Gestisce l'autenticazione e l'instradamento al menu del ruolo corrispondente.
     */
    private static void eseguiLogin(Scanner scanner, GestoreDati db) {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        Utente utenteLoggato = db.verificaCredenziali(user, pass);

        if (utenteLoggato != null) {
            System.out.println("\nLogin effettuato con successo! Bentornato, " + utenteLoggato.getNomeCompleto() + ".");

            switch (utenteLoggato.getRuolo().toUpperCase()) {
                case "CLIENTE":
                    menuCliente(scanner, db, utenteLoggato);
                    break;
                case "PROIEZIONISTA":
                    menuProiezionista(scanner, db, utenteLoggato);
                    break;
                case "BIGLIETTAIO":
                    menuBigliettaio(scanner, db, utenteLoggato);
                    break;
                default:
                    System.out.println("Errore: Ruolo non riconosciuto nel sistema.");
            }
        } else {
            System.out.println("\nErrore: Username o Password non corretti. Riprova.");
        }
        System.out.println("-------------");
    }

    /**
     * Gestisce la registrazione validata di un nuovo cliente ({@code registraCliente()}).
     */
    private static void eseguiRegistrazioneCliente(Scanner scanner, GestoreDati db) {
        System.out.println("\n--- REGISTRAZIONE NUOVO CLIENTE ---");

        String nome = leggiTestoObbligatorio(scanner, "Nome: ");
        String cognome = leggiTestoObbligatorio(scanner, "Cognome: ");

        String username;
        while (true) {
            username = leggiTestoObbligatorio(scanner, "Username: ");
            if (db.usernameGiaEsistente(username)) {
                System.out.println("Username già in uso! Scegline un altro.");
            } else {
                break;
            }
        }

        String password = leggiTestoObbligatorio(scanner, "Password: ");

        String dataNascita;
        while (true) {
            System.out.print("Data di Nascita (GG/MM/AAAA - facoltativa, premi Invio per saltare): ");
            dataNascita = scanner.nextLine().trim();
            if (dataNascita.isEmpty()) {
                break;
            }
            try {
                LocalDate parsed = LocalDate.parse(dataNascita, DATA_FORMATTER);
                if (parsed.isAfter(LocalDate.now())) {
                    System.out.println("Errore: la data di nascita non può essere nel futuro.");
                } else {
                    break;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Formato non valido. Usa GG/MM/AAAA (es. 15/03/2004) oppure premi Invio.");
            }
        }

        String domicilio = leggiTestoObbligatorio(scanner, "Luogo di Domicilio: ");

        Utente registrato = db.registraCliente(nome, cognome, username, password, dataNascita, domicilio);
        if (registrato != null) {
            System.out.println("\nRegistrazione completata con successo! Benvenuto, " + registrato.getNomeCompleto() + ".");
            System.out.println("Ora puoi accedere dal menu principale con il tuo username.");
        } else {
            System.out.println("Errore durante la registrazione: username già esistente.");
        }
        System.out.println("-----------------------------------");
    }

    // =========================================================================
    // MODALITÀ GUEST
    // =========================================================================

    /**
     * Avvia la modalità Guest chiedendo opzionalmente un titolo parziale all'ingresso
     * e aprendo il sottomenu di ricerca e consultazione proiezioni.
     */
    private static void avviaModalitaGuest(Scanner scanner, GestoreDati db) {
        System.out.println("\n--- MODALITÀ GUEST ---");
        System.out.print("Inserisci il titolo (anche parziale) di un film da consultare (oppure Invio per il menu Guest): ");
        String titoloIniziale = scanner.nextLine().trim();

        if (!titoloIniziale.isEmpty()) {
            List<Proiezione> risultati = db.cercaProiezione(titoloIniziale, null, null, null, null, null, true);
            gestisciRisultatiProiezioni(scanner, db, risultati, null);
        }

        boolean inGuest = true;
        while (inGuest) {
            System.out.println("\n=== AREA GUEST ===");
            System.out.println("1. Cerca proiezioni (titolo, genere, date, prezzo o combinazione)");
            System.out.println("2. Ricerca rapida per titolo");
            System.out.println("0. Torna al menu principale");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();
            switch (scelta) {
                case "1":
                    List<Proiezione> risultatiMulti = eseguiRicercaMultiCriterio(scanner, db, true);
                    gestisciRisultatiProiezioni(scanner, db, risultatiMulti, null);
                    break;
                case "2":
                    System.out.print("Inserisci titolo (anche parziale): ");
                    String titolo = scanner.nextLine().trim();
                    List<Proiezione> risultatiTitolo = db.cercaProiezione(titolo, null, null, null, null, null, true);
                    gestisciRisultatiProiezioni(scanner, db, risultatiTitolo, null);
                    break;
                case "0":
                    inGuest = false;
                    break;
                default:
                    System.out.println("Scelta non valida. Riprova.");
            }
        }
    }

    // =========================================================================
    // AREA CLIENTE
    // =========================================================================

    /**
     * Menu dedicato alle operazioni del Cliente autenticato.
     */
    private static void menuCliente(Scanner scanner, GestoreDati db, Utente cliente) {
        boolean inAreaRiservata = true;

        while (inAreaRiservata) {
            System.out.println("\n=== AREA CLIENTE: " + cliente.getNomeCompleto() + " (" + cliente.getUsername() + ") ===");
            System.out.println("1. Cerca proiezioni, visualizza dettagli e prenota");
            System.out.println("2. Visualizza le mie prenotazioni");
            System.out.println("3. Modifica data di una prenotazione");
            System.out.println("4. Cancella una prenotazione");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1":
                    List<Proiezione> risultati = eseguiRicercaMultiCriterio(scanner, db, true);
                    gestisciRisultatiProiezioni(scanner, db, risultati, cliente);
                    break;
                case "2":
                    visualizzaPrenotazioniCliente(scanner, db, cliente);
                    break;
                case "3":
                    modificaPrenotazioneCliente(scanner, db, cliente);
                    break;
                case "4":
                    eliminaPrenotazioneCliente(scanner, db, cliente);
                    break;
                case "0":
                    System.out.println("\nLogout effettuato con successo.");
                    inAreaRiservata = false;
                    break;
                default:
                    System.out.println("\nScelta non valida. Riprova.");
            }
        }
    }

    /**
     * Visualizza l'elenco delle prenotazioni del cliente con possibilità di aprire la scheda dettagliata.
     */
    private static void visualizzaPrenotazioniCliente(Scanner scanner, GestoreDati db, Utente cliente) {
        System.out.println("\n--- LE MIE PRENOTAZIONI ---");
        List<Prenotazione> mie = db.getPrenotazioniCliente(cliente.getUsername());

        if (mie.isEmpty()) {
            System.out.println("Non hai ancora effettuato nessuna prenotazione.");
            return;
        }

        for (int i = 0; i < mie.size(); i++) {
            System.out.println((i + 1) + ". " + mie.get(i).toString());
        }

        System.out.print("\nInserisci il numero della prenotazione per vedere i dettagli (0 per tornare indietro): ");
        int idx = leggiIntero(scanner);
        if (idx >= 1 && idx <= mie.size()) {
            Prenotazione scelta = mie.get(idx - 1);
            System.out.println("\n" + scelta.toDettaglioString(cliente.getNomeCompleto()));
        }
    }

    /**
     * Consente al cliente di spostare la data di una prenotazione futura su un'altra proiezione
     * futura dello stesso film ({@code modificaPrenotazione()}).
     */
    private static void modificaPrenotazioneCliente(Scanner scanner, GestoreDati db, Utente cliente) {
        System.out.println("\n--- MODIFICA DATA PRENOTAZIONE ---");
        List<Prenotazione> modificabili = getPrenotazioniFutureCliente(db, cliente);

        if (modificabili.isEmpty()) {
            System.out.println("Non hai prenotazioni future modificabili.");
            return;
        }

        for (int i = 0; i < modificabili.size(); i++) {
            System.out.println((i + 1) + ". " + modificabili.get(i).toString());
        }

        System.out.print("\nSeleziona la prenotazione da modificare (0 per annullare): ");
        int idx = leggiIntero(scanner);
        if (idx < 1 || idx > modificabili.size()) {
            return;
        }

        Prenotazione prenScelta = modificabili.get(idx - 1);

        // Cerca altre proiezioni future dello stesso film con posti sufficienti
        List<Proiezione> tutteDelFilm = db.cercaProiezione(prenScelta.getTitoloFilm(), null, null, null, null, null, true);
        List<Proiezione> alternative = new ArrayList<>();
        for (Proiezione p : tutteDelFilm) {
            boolean stessoTitolo = p.getTitoloFilm().equalsIgnoreCase(prenScelta.getTitoloFilm());
            boolean dataDiversa = !p.getDataOraProiezione().equals(prenScelta.getDataOraProiezione());
            boolean postiOk = db.calcolaPostiDisponibili(p) >= prenScelta.getNumeroPosti();
            if (stessoTitolo && dataDiversa && postiOk) {
                alternative.add(p);
            }
        }

        if (alternative.isEmpty()) {
            System.out.println("Spiacenti, non ci sono altre date future disponibili con almeno "
                    + prenScelta.getNumeroPosti() + " posti per '" + prenScelta.getTitoloFilm() + "'.");
            return;
        }

        System.out.println("\nSeleziona la nuova data per '" + prenScelta.getTitoloFilm() + "':");
        Proiezione nuovaProiezione = selezionaProiezionePaginata(scanner, db, alternative);
        if (nuovaProiezione == null) {
            System.out.println("Modifica annullata.");
            return;
        }

        if (db.modificaPrenotazione(prenScelta, nuovaProiezione)) {
            System.out.println("\nPrenotazione modificata con successo!");
            System.out.println(prenScelta.toDettaglioString(cliente.getNomeCompleto()));
        } else {
            System.out.println("Errore: impossibile completare la modifica della prenotazione.");
        }
    }

    /**
     * Consente al cliente di cancellare una propria prenotazione futura ({@code eliminaPrenotazione()}).
     */
    private static void eliminaPrenotazioneCliente(Scanner scanner, GestoreDati db, Utente cliente) {
        System.out.println("\n--- CANCELLA PRENOTAZIONE ---");
        List<Prenotazione> cancellabili = getPrenotazioniFutureCliente(db, cliente);

        if (cancellabili.isEmpty()) {
            System.out.println("Non hai prenotazioni future cancellabili.");
            return;
        }

        for (int i = 0; i < cancellabili.size(); i++) {
            System.out.println((i + 1) + ". " + cancellabili.get(i).toString());
        }

        System.out.print("\nSeleziona la prenotazione da cancellare (0 per annullare): ");
        int idx = leggiIntero(scanner);
        if (idx < 1 || idx > cancellabili.size()) {
            return;
        }

        Prenotazione prenScelta = cancellabili.get(idx - 1);
        System.out.println("\n" + prenScelta.toDettaglioString(cliente.getNomeCompleto()));
        System.out.print("Confermi la cancellazione di questa prenotazione? (S/N): ");
        String conferma = scanner.nextLine().trim();

        if (conferma.equalsIgnoreCase("S")) {
            if (db.eliminaPrenotazione(prenScelta)) {
                System.out.println("Prenotazione " + prenScelta.getCodicePrenotazione() + " cancellata con successo.");
            } else {
                System.out.println("Errore: impossibile cancellare la prenotazione (data già trascorsa).");
            }
        } else {
            System.out.println("Cancellazione annullata.");
        }
    }

    /**
     * Restituisce solo le prenotazioni future di uno specifico cliente.
     */
    private static List<Prenotazione> getPrenotazioniFutureCliente(GestoreDati db, Utente cliente) {
        List<Prenotazione> future = new ArrayList<>();
        LocalDateTime adesso = LocalDateTime.now();
        for (Prenotazione p : db.getPrenotazioniCliente(cliente.getUsername())) {
            if (p.getDataOraProiezione().isAfter(adesso)) {
                future.add(p);
            }
        }
        return future;
    }

    // =========================================================================
    // HELPER RICERCA, PAGINAZIONE, DETTAGLIO E PRENOTAZIONE
    // =========================================================================

    /**
     * Acquisisce i criteri opzionali di ricerca dall'utente ed esegue {@code cercaProiezione()}.
     */
    private static List<Proiezione> eseguiRicercaMultiCriterio(Scanner scanner, GestoreDati db, boolean soloFuture) {
        System.out.println("\n--- RICERCA PROIEZIONI ---");
        System.out.println("(Premi Invio su qualsiasi campo per saltare quel filtro)");

        System.out.print("Titolo film (anche parziale): ");
        String titolo = scanner.nextLine().trim();

        System.out.print("Genere (es. Drama, Action, Comedy): ");
        String genere = scanner.nextLine().trim();

        LocalDate dataDa = leggiDataOpzionale(scanner, "Data inizio (GG/MM/AAAA): ");
        LocalDate dataA = leggiDataOpzionale(scanner, "Data fine   (GG/MM/AAAA): ");
        if (dataDa != null && dataA != null && dataA.isBefore(dataDa)) {
            System.out.println("Avviso: la data di fine precede la data di inizio; intervallo scambiato automaticamente.");
            LocalDate tmp = dataDa;
            dataDa = dataA;
            dataA = tmp;
        }

        Double prezzoMin = leggiDoubleOpzionale(scanner, "Prezzo minimo (€): ");
        Double prezzoMax = leggiDoubleOpzionale(scanner, "Prezzo massimo (€): ");
        if (prezzoMin != null && prezzoMax != null && prezzoMax < prezzoMin) {
            System.out.println("Avviso: prezzo massimo inferiore al minimo; valori scambiati automaticamente.");
            Double tmp = prezzoMin;
            prezzoMin = prezzoMax;
            prezzoMax = tmp;
        }

        return db.cercaProiezione(titolo, genere, dataDa, dataA, prezzoMin, prezzoMax, soloFuture);
    }

    /**
     * Mostra i risultati di ricerca paginati, apre i dettagli della proiezione selezionata
     * ({@code visualizzaProiezione()}) e, se l'utente è un Cliente autenticato, permette di prenotare.
     */
    private static void gestisciRisultatiProiezioni(Scanner scanner, GestoreDati db,
                                                    List<Proiezione> risultati, Utente cliente) {
        if (risultati.isEmpty()) {
            System.out.println("\nNessuna proiezione trovata con i criteri specificati.");
            return;
        }

        while (true) {
            Proiezione scelta = selezionaProiezionePaginata(scanner, db, risultati);
            if (scelta == null) {
                return;
            }

            int postiLiberi = db.calcolaPostiDisponibili(scelta);
            System.out.println("\n" + scelta.toDettaglioString(postiLiberi));

            if (cliente != null) {
                if (postiLiberi <= 0) {
                    System.out.println("Spiacenti, questa proiezione è al completo (0 posti disponibili).");
                } else {
                    System.out.print("Vuoi prenotare posti per questa proiezione? (S/N): ");
                    String risposta = scanner.nextLine().trim();
                    if (risposta.equalsIgnoreCase("S")) {
                        eseguiPrenotazione(scanner, db, cliente, scelta, postiLiberi);
                        return;
                    }
                }
            }

            System.out.print("Vuoi consultare un'altra proiezione da questa ricerca? (S/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("S")) {
                return;
            }
        }
    }

    /**
     * Completa la creazione di una prenotazione verificando il numero di posti richiesti.
     */
    private static void eseguiPrenotazione(Scanner scanner, GestoreDati db, Utente cliente,
                                           Proiezione proiezione, int postiDisponibili) {
        System.out.print("Quanti posti vuoi prenotare? (1 - " + postiDisponibili + "): ");
        int numPosti = leggiIntero(scanner);

        if (numPosti <= 0) {
            System.out.println("Errore: devi prenotare almeno un posto.");
            return;
        }
        if (numPosti > postiDisponibili) {
            System.out.println("Spiacenti, sono disponibili solo " + postiDisponibili + " posti.");
            return;
        }

        Prenotazione nuova = db.creaPrenotazione(cliente, proiezione, numPosti);
        if (nuova != null) {
            System.out.println("\nPrenotazione confermata con successo!");
            System.out.println(nuova.toDettaglioString(cliente.getNomeCompleto()));
        } else {
            System.out.println("Errore: impossibile completare la prenotazione.");
        }
    }

    /**
     * Visualizza una lista di proiezioni in pagine da {@value #ELEMENTI_PER_PAGINA} elementi
     * e consente di selezionarne una per numero.
     *
     * @return la {@link Proiezione} scelta, oppure {@code null} se l'utente torna indietro
     */
    private static Proiezione selezionaProiezionePaginata(Scanner scanner, GestoreDati db, List<Proiezione> elenco) {
        int totale = elenco.size();
        int totalePagine = (int) Math.ceil((double) totale / ELEMENTI_PER_PAGINA);
        int paginaCorrente = 0;

        while (true) {
            int inizio = paginaCorrente * ELEMENTI_PER_PAGINA;
            int fine = Math.min(inizio + ELEMENTI_PER_PAGINA, totale);

            System.out.println("\n--- RISULTATI TROVATI (" + totale + ") | Pagina "
                    + (paginaCorrente + 1) + " di " + totalePagine + " ---");

            for (int i = inizio; i < fine; i++) {
                Proiezione p = elenco.get(i);
                int postiDisp = db.calcolaPostiDisponibili(p);
                System.out.println((i + 1) + ". " + p.toString() + " [Posti liberi: " + postiDisp + "]");
            }

            System.out.print("\nInserisci il numero della proiezione"
                    + (totalePagine > 1 ? ", 'N' (avanti), 'P' (indietro)" : "")
                    + " oppure '0' per uscire: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }
            if (input.equalsIgnoreCase("N") && paginaCorrente < totalePagine - 1) {
                paginaCorrente++;
                continue;
            }
            if (input.equalsIgnoreCase("P") && paginaCorrente > 0) {
                paginaCorrente--;
                continue;
            }

            try {
                int scelta = Integer.parseInt(input);
                if (scelta >= 1 && scelta <= totale) {
                    return elenco.get(scelta - 1);
                }
                System.out.println("Numero non valido. Scegli tra 1 e " + totale + ".");
            } catch (NumberFormatException e) {
                System.out.println("Input non valido.");
            }
        }
    }

    /**
     * Legge una stringa non vuota da console.
     */
    private static String leggiTestoObbligatorio(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String valore = scanner.nextLine().trim();
            if (!valore.isEmpty()) {
                return valore;
            }
            System.out.println("Questo campo è obbligatorio.");
        }
    }

    /**
     * Legge una data opzionale nel formato {@code dd/MM/yyyy} (Invio per saltare).
     */
    private static LocalDate leggiDataOpzionale(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, DATA_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Data non valida. Usa il formato GG/MM/AAAA (es. 25/12/2027) oppure premi Invio.");
            }
        }
    }

    /**
     * Legge un numero decimale opzionale non negativo (Invio per saltare).
     */
    private static Double leggiDoubleOpzionale(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(",", ".");
            if (input.isEmpty()) {
                return null;
            }
            try {
                double val = Double.parseDouble(input);
                if (val < 0) {
                    System.out.println("Inserisci un importo non negativo.");
                } else {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.out.println("Importo non valido. Inserisci un numero (es. 8.50) oppure premi Invio.");
            }
        }
    }

    /**
     * Legge un intero gestendo eventuali errori di formato.
     */
    private static int leggiIntero(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Errore: devi inserire un numero intero.");
            return -1;
        }
    }

    // =========================================================================
    // AREA PROIEZIONISTA E BIGLIETTAIO (verranno rifiniti nel Branch 4)
    // =========================================================================

    /**
     * Menu dedicato al Proiezionista.
     */
    private static void menuProiezionista(Scanner scanner, GestoreDati db, Utente proiezionista) {
        boolean inMenu = true;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        while (inMenu) {
            System.out.println("\n=== AREA PROIEZIONISTA: " + proiezionista.getNome() + " ===");
            System.out.println("1. Aggiungi una nuova proiezione");
            System.out.println("2. Modifica data/ora di una proiezione");
            System.out.println("3. Elimina una proiezione");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1":
                    try {
                        System.out.println("\n--- AGGIUNGI PROIEZIONE ---");
                        System.out.print("Titolo Film: ");
                        String titolo = scanner.nextLine().trim();
                        System.out.print("Genere: ");
                        String genere = scanner.nextLine().trim();
                        System.out.print("Regista: ");
                        String regista = scanner.nextLine().trim();

                        System.out.print("Anno: ");
                        int anno = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Durata (minuti): ");
                        int durata = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Età Minima: ");
                        int etaMin = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Prezzo Biglietto (€): ");
                        double prezzo = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));

                        System.out.print("Data e Ora Proiezione (es. 2026-12-25 21:00): ");
                        String dataOraStr = scanner.nextLine().trim();
                        LocalDateTime dataOra = LocalDateTime.parse(dataOraStr, formatter);

                        if (db.haSovrapposizione(dataOra, durata)) {
                            System.out.println("Errore: La proiezione si sovrappone ad un altro spettacolo già in palinsesto!");
                        } else {
                            Proiezione nuovaProiezione = new Proiezione(dataOra, titolo, genere, regista, anno, durata, etaMin, prezzo);
                            db.aggiungiProiezione(nuovaProiezione);
                            System.out.println("Proiezione aggiunta con successo!");
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Errore formato data: devi inserire data E ora nel formato YYYY-MM-DD HH:mm (es. 2026-12-25 21:00).");
                    } catch (NumberFormatException e) {
                        System.out.println("Errore formato numerico: controlla di aver inserito numeri validi per anno, durata, età o prezzo.");
                    }
                    break;

                case "2":
                    try {
                        System.out.println("\n--- MODIFICA PROIEZIONE ---");
                        List<Proiezione> proiezioni = db.getListaProiezioni();
                        for (int i = 0; i < proiezioni.size(); i++) {
                            System.out.println((i + 1) + ". " + proiezioni.get(i).toString());
                        }
                        System.out.print("Seleziona il numero della proiezione da modificare (0 annulla): ");
                        int idxMod = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (idxMod < 0 || idxMod >= proiezioni.size()) break;

                        Proiezione pMod = proiezioni.get(idxMod);
                        if (db.haPrenotazioniEsistenti(pMod)) {
                            System.out.println("Impossibile modificare: esistono già prenotazioni attive per questo spettacolo!");
                        } else {
                            System.out.print("Nuova Data e Ora (es. 2026-12-25 21:00): ");
                            String nuovaDataStr = scanner.nextLine().trim();
                            LocalDateTime nuovaDataOra = LocalDateTime.parse(nuovaDataStr, formatter);

                            if (!db.modificaProiezione(pMod, nuovaDataOra)) {
                                System.out.println("Errore: La nuova data si sovrappone a un'altra proiezione!");
                            } else {
                                System.out.println("Data/Ora modifica registrata con successo!");
                            }
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Errore formato data: usa il formato YYYY-MM-DD HH:mm (es. 2026-12-25 21:00).");
                    } catch (NumberFormatException e) {
                        System.out.println("Errore: inserisci un numero intero valido.");
                    }
                    break;

                case "3":
                    try {
                        System.out.println("\n--- ELIMINA PROIEZIONE ---");
                        List<Proiezione> listaDel = db.getListaProiezioni();
                        for (int i = 0; i < listaDel.size(); i++) {
                            System.out.println((i + 1) + ". " + listaDel.get(i).toString());
                        }
                        System.out.print("Seleziona il numero della proiezione da eliminare (0 annulla): ");
                        int idxDel = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (idxDel < 0 || idxDel >= listaDel.size()) break;

                        Proiezione pDel = listaDel.get(idxDel);
                        if (db.eliminaProiezione(pDel)) {
                            System.out.println("Proiezione eliminata con successo!");
                        } else {
                            System.out.println("Impossibile eliminare: ci sono prenotazioni attive per questa proiezione!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Errore: inserisci un numero intero valido.");
                    }
                    break;

                case "0":
                    System.out.println("Logout Proiezionista eseguito.");
                    inMenu = false;
                    break;

                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    /**
     * Menu dedicato al Bigliettaio.
     */
    private static void menuBigliettaio(Scanner scanner, GestoreDati db, Utente bigliettaio) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n=== AREA BIGLIETTAIO: " + bigliettaio.getNome() + " ===");
            System.out.println("1. Visualizza prenotazioni della data odierna");
            System.out.println("2. Cerca una prenotazione (Codice, Cliente, Film, Data)");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1":
                    System.out.println("\n--- PRENOTAZIONI ODIERNE ---");
                    List<Prenotazione> odierne = db.getPrenotazioniOdierne();
                    if (odierne.isEmpty()) {
                        System.out.println("Nessuna prenotazione trovata per la giornata di oggi.");
                    } else {
                        for (Prenotazione p : odierne) {
                            System.out.println(p.toString());
                        }
                    }
                    break;

                case "2":
                    System.out.println("\n--- CERCA PRENOTAZIONE ---");
                    System.out.print("Inserisci il codice, il nome cliente, il titolo film o la data: ");
                    String ricerca = scanner.nextLine().trim();

                    List<Prenotazione> trovate = db.cercaPrenotazioni(ricerca);
                    if (trovate.isEmpty()) {
                        System.out.println("Nessuna prenotazione corrisponde ai criteri immessi.");
                    } else {
                        System.out.println("\n--- RISULTATI TROVATI (" + trovate.size() + ") ---");
                        for (Prenotazione p : trovate) {
                            System.out.println(p.toString());
                        }
                    }
                    break;

                case "0":
                    System.out.println("Logout Bigliettaio eseguito.");
                    inMenu = false;
                    break;

                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }
}