package cinemax;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class CineMax {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   BENVENUTO IN CINEMAX 2026");
        System.out.println("=================================");
        
        // Inizializza il "database" caricando proiezioni e utenti
        GestoreDati db = new GestoreDati();
        db.inizializzaSistema();
        
        Scanner scanner = new Scanner(System.in);
        boolean inEsecuzione = true;

        // Loop del menu iniziale come da specifiche
        while (inEsecuzione) {
            System.out.println("\nSeleziona un'operazione:");
            System.out.println("1. Continua come Guest (Cerca proiezioni)");
            System.out.println("2. Accedi alla piattaforma (Login)");
            System.out.println("3. Registrati come nuovo Cliente");
            System.out.println("0. Esci dal programma");
            System.out.print("> ");
            
            String scelta = scanner.nextLine().trim();
            
            switch (scelta) {
            case "1":
                System.out.println("\n--- NAVIGAZIONE GUEST ---");
                System.out.print("Inserisci un titolo, un genere o un regista da cercare: ");
                String ricerca = scanner.nextLine().trim();
                
                List<Proiezione> risultati = db.ricercaProiezioni(ricerca);
                
                if (risultati.isEmpty()) {
                    System.out.println("Nessun film trovato con la parola: '" + ricerca + "'");
                } else {
                    System.out.println("\n--- RISULTATI TROVATI (" + risultati.size() + ") ---");
                    for (Proiezione p : risultati) {
                        System.out.println(p.toString());
                    }
                }
                System.out.println("-------------------------\n");
                break;
                    
            case "2":
                System.out.println("\n--- LOGIN ---");
                System.out.print("Username: ");
                String user = scanner.nextLine().trim();
                System.out.print("Password: ");
                String pass = scanner.nextLine().trim();
                
                Utente utenteLoggato = db.verificaCredenziali(user, pass);
                
                if (utenteLoggato != null) {
                    System.out.println("\nLogin effettuato con successo! Bentornato, " + utenteLoggato.getNome() + ".");
                    
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
                System.out.println("-------------\n");
                break;
                    
                case "3":
                    System.out.println("\n--- REGISTRAZIONE NUOVO CLIENTE ---");
                    
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine().trim();
                    
                    System.out.print("Cognome: ");
                    String cognome = scanner.nextLine().trim();
                    
                    String username;
                    while (true) {
                        System.out.print("Username: ");
                        username = scanner.nextLine().trim();
                        if (username.isEmpty()) {
                            System.out.println("L'username non può essere vuoto.");
                        } else if (db.usernameGiaEsistente(username)) {
                            System.out.println("Username già in uso! Scegline un altro.");
                        } else {
                            break;
                        }
                    }
                    
                    String password;
                    while (true) {
                        System.out.print("Password: ");
                        password = scanner.nextLine().trim();
                        if (password.isEmpty()) {
                            System.out.println("La password non può essere vuota.");
                        } else {
                            break;
                        }
                    }
                    
                    System.out.print("Data di Nascita (es. GG/MM/AAAA): ");
                    String dataNascita = scanner.nextLine().trim();
                    
                    System.out.print("Luogo di Domicilio: ");
                    String domicilio = scanner.nextLine().trim();
                    
                    Utente nuovoCliente = new Utente(nome, cognome, username, password, dataNascita, domicilio, "CLIENTE");
                    
                    // REFACTORING: Uso del metodo sicuro per l'aggiunta
                    db.aggiungiUtente(nuovoCliente);
                    db.salvaUtenti();
                    
                    System.out.println("\nRegistrazione completata con successo! Benvenuto, " + username + ".");
                    System.out.println("-----------------------------------\n");
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

    /**
     * Menu dedicato alle operazioni del Cliente
     */
    private static void menuCliente(Scanner scanner, GestoreDati db, Utente cliente) {
        boolean inAreaRiservata = true;
        
        while (inAreaRiservata) {
            System.out.println("\n=== AREA CLIENTE: " + cliente.getUsername() + " ===");
            System.out.println("1. Visualizza il palinsesto e prenota");
            System.out.println("2. I miei biglietti");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine().trim();
            
            switch (scelta) {
                case "1":
                    System.out.println("\n--- PALINSESTO E PRENOTAZIONE ---");

                    // Recupera solo le proiezioni da oggi in poi
                    List<Proiezione> proiezioniDisponibili = db.getProiezioniDisponibili();

                    if (proiezioniDisponibili.isEmpty()) {
                        System.out.println("Nessuna proiezione disponibile per le date future.");
                        break;
                    }

                    // Stampa solo i film prenotabili
                    for (int i = 0; i < proiezioniDisponibili.size(); i++) {
                        Proiezione p = proiezioniDisponibili.get(i);
                        int postiDisp = db.calcolaPostiDisponibili(p);
                        System.out.println((i + 1) + ". " + p.toString() + " [Posti disponibili: " + postiDisp + "]");
                    }

                    System.out.print("\nInserisci il numero del film che vuoi prenotare (0 per annullare): ");
                    int sceltaFilm = 0;
                    try {
                        sceltaFilm = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Errore: devi inserire un numero intero.");
                        break;
                    }

                    if (sceltaFilm == 0) break;

                    if (sceltaFilm < 1 || sceltaFilm > proiezioniDisponibili.size()) {
                        System.out.println("Scelta non valida.");
                        break;
                    }

                    Proiezione filmScelto = proiezioniDisponibili.get(sceltaFilm - 1);
                    int postiDisponibili = db.calcolaPostiDisponibili(filmScelto);

                    System.out.print("Quanti posti vuoi prenotare? ");
                    int numPosti = 0;
                    try {
                        numPosti = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Errore: devi inserire un numero intero.");
                        break;
                    }

                    if (numPosti <= 0) {
                        System.out.println("Devi prenotare almeno un posto.");
                    } else if (numPosti > postiDisponibili) {
                        System.out.println("Spiacenti, per questo film ci sono solo " + postiDisponibili + " posti disponibili.");
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        String dataOraStr = filmScelto.getDataOraProiezione().format(formatter);

                        Prenotazione nuovaPren = new Prenotazione(
                                cliente.getUsername(),
                                filmScelto.getTitoloFilm(),
                                dataOraStr,
                                numPosti,
                                filmScelto.getPrezzoBiglietto()
                        );

                        db.aggiungiPrenotazione(nuovaPren);
                        db.salvaPrenotazioni();

                        System.out.println("\n✅ Prenotazione confermata con successo!");
                        System.out.println("Riepilogo: " + nuovaPren.toString());
                    }
                    System.out.println("---------------------------------\n");
                    break;
            case "2":
                System.out.println("\n--- I MIEI BIGLIETTI ---");
                boolean trovati = false;
                
                // REFACTORING: Uso del getter
                for (Prenotazione p : db.getListaPrenotazioni()) {
                    if (p.getUsernameCliente().equals(cliente.getUsername())) {
                        System.out.println(p.toString());
                        trovati = true;
                    }
                }
                
                if (!trovati) {
                    System.out.println("Non hai ancora effettuato nessuna prenotazione.");
                }
                System.out.println("------------------------\n");
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
     * Menu dedicato al Proiezionista (Task assegnato ad Anes)
     */
    private static void menuProiezionista(Scanner scanner, GestoreDati db, Utente proiezionista) {
            boolean inMenu = true;
            // Formato (es. 2026-12-25 21:00)
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
                                System.out.println("❌ Errore: La proiezione si sovrappone ad un altro spettacolo già in palinsesto!");
                            } else {
                                Proiezione nuovaProiezione = new Proiezione(dataOra, titolo, genere, regista, anno, durata, etaMin, prezzo);
                                db.aggiungiProiezione(nuovaProiezione);
                                System.out.println("✅ Proiezione aggiunta con successo!");
                            }
                        } catch (java.time.format.DateTimeParseException e) {
                            System.out.println("❌ Errore formato data: devi inserire data E ora nel formato YYYY-MM-DD HH:mm (es. 2026-12-25 21:00).");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Errore formato numerico: controlla di aver inserito numeri validi per anno, durata, età o prezzo.");
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
                                System.out.println("❌ Impossibile modificare: esistono già prenotazioni attive per questo spettacolo!");
                            } else {
                                System.out.print("Nuova Data e Ora (es. 2026-12-25 21:00): ");
                                String nuovaDataStr = scanner.nextLine().trim();
                                LocalDateTime nuovaDataOra = LocalDateTime.parse(nuovaDataStr, formatter);

                                if (db.haSovrapposizione(nuovaDataOra, pMod.getDurataMinuti())) {
                                    System.out.println("❌ Errore: La nuova data si sovrappone a un'altra proiezione!");
                                } else {
                                    pMod.setDataOraProiezione(nuovaDataOra);
                                    db.salvaProiezioniSuCSV();
                                    System.out.println("✅ Data/Ora modifica registrata con successo!");
                                }
                            }
                        } catch (java.time.format.DateTimeParseException e) {
                            System.out.println("❌ Errore formato data: usa il formato YYYY-MM-DD HH:mm (es. 2026-12-25 21:00).");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Errore: inserisci un numero intero valido.");
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
                                System.out.println("✅ Proiezione eliminata con successo!");
                            } else {
                                System.out.println("❌ Impossibile eliminare: ci sono prenotazioni attive per questa proiezione!");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Errore: inserisci un numero intero valido.");
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
     * Menu dedicato al Bigliettaio (Task assegnato ad Anes)
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