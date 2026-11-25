/*
 * Progetto CineMax - Laboratorio Interdisciplinare A
 * Autori:
 * - Daniele Paoli, Matricola: [MATRICOLA], Sede: VA
 * - Edoardo Pigni, Matricola: 761118, Sede: VA
 * - Anes Khaia, Matricola: 760038, Sede: VA
 */
package cinemax;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Classe Data Access Object (DAO) e gestore della logica applicativa di CineMax.
 * <p>
 * Centralizza caricamento, persistenza (CSV UTF-8 e file binari serializzati),
 * cifratura delle password, ricerche multi-criterio e validazioni di dominio
 * su utenti, proiezioni e prenotazioni.
 * </p>
 *
 * @author Daniele Paoli
 * @author Edoardo Pigni
 * @author Anes Khaia
 * @version 1.1
 */
public class GestoreDati {

    /** Directory contenente i file di persistenza. */
    private static final String DIR_DATA = "data";

    /** Percorso del file binario degli utenti. */
    private static final String FILE_UTENTI = "data/utenti.dat";

    /** Percorso del file binario delle prenotazioni. */
    private static final String FILE_PRENOTAZIONI = "data/prenotazioni.dat";

    /** Percorso del file CSV del palinsesto proiezioni. */
    private static final String FILE_PROIEZIONI_CSV = "data/proiezioni.csv";

    /** Formato data/ora usato nel file CSV delle proiezioni. */
    private static final DateTimeFormatter CSV_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Lista in memoria degli utenti registrati. */
    private List<Utente> listaUtenti = new ArrayList<>();

    /** Lista in memoria delle prenotazioni effettuate. */
    private List<Prenotazione> listaPrenotazioni = new ArrayList<>();

    /** Lista in memoria delle proiezioni in palinsesto. */
    private List<Proiezione> listaProiezioni = new ArrayList<>();

    // =========================================================================
    // SICUREZZA / CIFRATURA PASSWORD
    // =========================================================================

    /**
     * Cifra una password in chiaro tramite algoritmo di hashing SHA-256.
     *
     * @param passwordInChiaro la password inserita dall'utente
     * @return rappresentazione esadecimale dell'hash SHA-256
     */
    public static String cifraPassword(String passwordInChiaro) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(passwordInChiaro.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 non disponibile nella JVM", e);
        }
    }

    // =========================================================================
    // GETTER INCAPSULATI (SOLA LETTURA)
    // =========================================================================

    /**
     * Restituisce una vista non modificabile delle proiezioni caricate.
     *
     * @return lista non modificabile di {@link Proiezione}
     */
    public List<Proiezione> getListaProiezioni() {
        return Collections.unmodifiableList(listaProiezioni);
    }

    /**
     * Restituisce una vista non modificabile delle prenotazioni caricate.
     *
     * @return lista non modificabile di {@link Prenotazione}
     */
    public List<Prenotazione> getListaPrenotazioni() {
        return Collections.unmodifiableList(listaPrenotazioni);
    }

    /**
     * Restituisce una vista non modificabile degli utenti registrati.
     *
     * @return lista non modificabile di {@link Utente}
     */
    public List<Utente> getListaUtenti() {
        return Collections.unmodifiableList(listaUtenti);
    }

    // =========================================================================
    // INIZIALIZZAZIONE E PERSISTENZA I/O
    // =========================================================================

    /**
     * Assicura l'esistenza della cartella {@code data/} prima delle operazioni di scrittura.
     */
    private void assicuraDirectoryData() {
        File dir = new File(DIR_DATA);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Inizializza il sistema caricando proiezioni, utenti e prenotazioni da disco.
     * <p>
     * Se non esistono utenti salvati, popola automaticamente gli account di default
     * richiesti dalle specifiche (2 proiezionisti e 5 bigliettai) con password cifrata.
     * </p>
     */
    public void inizializzaSistema() {
        caricaProiezioniDalCSV();
        caricaUtenti();
        caricaPrenotazioni();

        if (listaUtenti.isEmpty()) {
            System.out.println("Inizializzazione utenti di default in corso...");
            String passDefault = cifraPassword("pass123");

            listaUtenti.add(new Utente("Mario", "Rossi", "proiezionista1", passDefault, "01/01/1980", "Varese", "PROIEZIONISTA"));
            listaUtenti.add(new Utente("Luigi", "Verdi", "proiezionista2", passDefault, "02/02/1985", "Como", "PROIEZIONISTA"));

            for (int i = 1; i <= 5; i++) {
                listaUtenti.add(new Utente("Bigliettaio", "Num" + i, "bigliettaio" + i, passDefault, "01/01/1990", "Varese", "BIGLIETTAIO"));
            }
            salvaUtenti();
        }
    }

    /**
     * Carica il palinsesto delle proiezioni dal file CSV utilizzando codifica UTF-8.
     */
    public void caricaProiezioniDalCSV() {
        Path csvPath = Paths.get(FILE_PROIEZIONI_CSV);
        if (!Files.exists(csvPath)) {
            System.err.println("Attenzione: file " + FILE_PROIEZIONI_CSV + " non trovato nella cartella data!");
            return;
        }

        listaProiezioni.clear();
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = br.readLine(); // Salta l'intestazione

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                if (values.length >= 8) {
                    String dataOra = values[0].replace("\"", "").trim();
                    String titolo = values[1].replace("\"", "").trim();
                    String genere = values[2].replace("\"", "").trim();
                    String regista = values[3].replace("\"", "").trim();
                    int anno = Integer.parseInt(values[4].replace("\"", "").trim());
                    int durata = Integer.parseInt(values[5].replace("\"", "").trim());
                    int etaMin = Integer.parseInt(values[6].replace("\"", "").trim());
                    double prezzo = Double.parseDouble(values[7].replace("\"", "").trim().replace(",", "."));

                    Proiezione p = new Proiezione(
                            LocalDateTime.parse(dataOra, CSV_FORMATTER),
                            titolo, genere, regista, anno, durata, etaMin, prezzo
                    );
                    listaProiezioni.add(p);
                }
            }
            System.out.println("Caricate " + listaProiezioni.size() + " proiezioni dal CSV con successo!");
        } catch (Exception e) {
            System.err.println("Errore durante la lettura del CSV: " + e.getMessage());
        }
    }

    /**
     * Salva l'intera lista delle proiezioni sul file CSV in formato UTF-8 con {@link Locale#US}
     * per garantire il punto come separatore decimale del prezzo.
     */
    public void salvaProiezioniSuCSV() {
        assicuraDirectoryData();
        Path csvPath = Paths.get(FILE_PROIEZIONI_CSV);

        try (BufferedWriter bw = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println("data_ora,titolo,genere,regista,anno,durata,eta_minima,prezzo");
            for (Proiezione p : listaProiezioni) {
                pw.printf(Locale.US, "\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%.2f%n",
                        p.getDataOraProiezione().format(CSV_FORMATTER),
                        p.getTitoloFilm().replace("\"", ""),
                        p.getGenere().replace("\"", ""),
                        p.getRegista().replace("\"", ""),
                        p.getAnno(),
                        p.getDurataMinuti(),
                        p.getEtaMinima(),
                        p.getPrezzoBiglietto()
                );
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del CSV proiezioni: " + e.getMessage());
        }
    }

    /**
     * Deserializza la lista degli utenti dal file binario {@code data/utenti.dat}.
     */
    @SuppressWarnings("unchecked")
    public void caricaUtenti() {
        File f = new File(FILE_UTENTI);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                listaUtenti = (List<Utente>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Errore nel caricamento utenti: " + e.getMessage());
            }
        }
    }

    /**
     * Serializza la lista degli utenti su {@code data/utenti.dat}.
     */
    public void salvaUtenti() {
        assicuraDirectoryData();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_UTENTI))) {
            oos.writeObject(listaUtenti);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio utenti: " + e.getMessage());
        }
    }

    /**
     * Deserializza la lista delle prenotazioni dal file binario {@code data/prenotazioni.dat}.
     */
    @SuppressWarnings("unchecked")
    public void caricaPrenotazioni() {
        File f = new File(FILE_PRENOTAZIONI);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                listaPrenotazioni = (List<Prenotazione>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Errore nel caricamento prenotazioni: " + e.getMessage());
            }
        }
    }

    /**
     * Serializza la lista delle prenotazioni su {@code data/prenotazioni.dat}.
     */
    public void salvaPrenotazioni() {
        assicuraDirectoryData();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PRENOTAZIONI))) {
            oos.writeObject(listaPrenotazioni);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio prenotazioni: " + e.getMessage());
        }
    }

    // =========================================================================
    // GESTIONE UTENTI (REGISTRAZIONE E LOGIN)
    // =========================================================================

    /**
     * Aggiunge un utente alla lista in memoria.
     *
     * @param u l'utente da aggiungere
     */
    public void aggiungiUtente(Utente u) {
        if (u != null) {
            listaUtenti.add(u);
        }
    }

    /**
     * Registra un nuovo cliente cifrandone la password e salvando su disco.
     *
     * @param nome           nome del cliente
     * @param cognome        cognome del cliente
     * @param username       username univoco scelto
     * @param passwordChiaro password in chiaro (verrà cifrata in SHA-256)
     * @param dataNascita    data di nascita (facoltativa)
     * @param luogoDomicilio luogo di domicilio
     * @return il nuovo {@link Utente} registrato, oppure {@code null} se lo username esiste già
     */
    public Utente registraCliente(String nome, String cognome, String username,
                                  String passwordChiaro, String dataNascita, String luogoDomicilio) {
        if (usernameGiaEsistente(username)) {
            return null;
        }
        String hashPassword = cifraPassword(passwordChiaro);
        Utente nuovoCliente = new Utente(nome, cognome, username.trim(), hashPassword,
                dataNascita, luogoDomicilio, "CLIENTE");
        listaUtenti.add(nuovoCliente);
        salvaUtenti();
        return nuovoCliente;
    }

    /**
     * Verifica se uno username è già presente nell'anagrafica utenti (case-insensitive).
     *
     * @param username lo username da verificare
     * @return {@code true} se già in uso, {@code false} altrimenti
     */
    public boolean usernameGiaEsistente(String username) {
        if (username == null) {
            return false;
        }
        String target = username.trim();
        for (Utente u : listaUtenti) {
            if (u.getUsername().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Autentica un utente confrontando username e hash SHA-256 della password.
     *
     * @param username lo username inserito
     * @param password la password in chiaro inserita
     * @return l'oggetto {@link Utente} se autenticato, {@code null} altrimenti
     */
    public Utente verificaCredenziali(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String userTrim = username.trim();
        String hashInserito = cifraPassword(password.trim());
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(userTrim) && u.getPassword().equals(hashInserito)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Restituisce nome e cognome di un utente a partire dal suo username.
     *
     * @param username lo username del cliente
     * @return {@code "Nome Cognome"} se trovato, altrimenti lo username stesso
     */
    public String getNomeCompletoUtente(String username) {
        for (Utente u : listaUtenti) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u.getNomeCompleto();
            }
        }
        return username;
    }

    // =========================================================================
    // RICERCA E GESTIONE PROIEZIONI (GUEST, CLIENTE, PROIEZIONISTA)
    // =========================================================================

    /**
     * Esegue una ricerca multi-criterio sulle proiezioni ({@code cercaProiezione()}).
     * <p>
     * Tutti i parametri di filtro sono opzionali: passando {@code null} o stringa vuota
     * il relativo criterio viene ignorato, permettendo ricerche singole o combinate.
     * </p>
     *
     * @param titolo     titolo o parte del titolo del film (opzionale)
     * @param genere     genere o tipologia del film (opzionale)
     * @param dataDa     data iniziale dell'intervallo inclusa (opzionale)
     * @param dataA      data finale dell'intervallo inclusa (opzionale)
     * @param prezzoMin  prezzo minimo incluso in Euro (opzionale)
     * @param prezzoMax  prezzo massimo incluso in Euro (opzionale)
     * @param soloFuture {@code true} per includere solo proiezioni successive all'istante attuale
     * @return lista delle proiezioni che soddisfano tutti i criteri attivi
     */
    public List<Proiezione> cercaProiezione(String titolo, String genere,
                                            LocalDate dataDa, LocalDate dataA,
                                            Double prezzoMin, Double prezzoMax,
                                            boolean soloFuture) {
        List<Proiezione> risultati = new ArrayList<>();
        LocalDateTime adesso = LocalDateTime.now();

        String titoloFiltro = (titolo != null && !titolo.trim().isEmpty()) ? titolo.trim().toLowerCase() : null;
        String genereFiltro = (genere != null && !genere.trim().isEmpty()) ? genere.trim().toLowerCase() : null;

        for (Proiezione p : listaProiezioni) {
            if (soloFuture && !p.getDataOraProiezione().isAfter(adesso)) {
                continue;
            }
            if (titoloFiltro != null && !p.getTitoloFilm().toLowerCase().contains(titoloFiltro)) {
                continue;
            }
            if (genereFiltro != null && !p.getGenere().toLowerCase().contains(genereFiltro)) {
                continue;
            }
            LocalDate dataProiezione = p.getDataOraProiezione().toLocalDate();
            if (dataDa != null && dataProiezione.isBefore(dataDa)) {
                continue;
            }
            if (dataA != null && dataProiezione.isAfter(dataA)) {
                continue;
            }
            if (prezzoMin != null && p.getPrezzoBiglietto() < prezzoMin) {
                continue;
            }
            if (prezzoMax != null && p.getPrezzoBiglietto() > prezzoMax) {
                continue;
            }
            risultati.add(p);
        }
        return risultati;
    }

    /**
     * Esegue una ricerca rapida per parola chiave su titolo, genere o regista
     * (solo proiezioni future).
     *
     * @param chiaveDiRicerca stringa da cercare
     * @return lista delle proiezioni future corrispondenti
     */
    public List<Proiezione> ricercaProiezioni(String chiaveDiRicerca) {
        List<Proiezione> risultati = new ArrayList<>();
        String query = chiaveDiRicerca.toLowerCase().trim();
        LocalDateTime adesso = LocalDateTime.now();

        for (Proiezione p : listaProiezioni) {
            if (p.getDataOraProiezione().isAfter(adesso)) {
                if (p.getTitoloFilm().toLowerCase().contains(query) ||
                        p.getGenere().toLowerCase().contains(query) ||
                        p.getRegista().toLowerCase().contains(query)) {
                    risultati.add(p);
                }
            }
        }
        return risultati;
    }

    /**
     * Restituisce tutte le proiezioni future rispetto all'istante corrente.
     *
     * @return lista delle proiezioni prenotabili
     */
    public List<Proiezione> getProiezioniDisponibili() {
        return cercaProiezione(null, null, null, null, null, null, true);
    }

    /**
     * Calcola il numero di posti ancora liberi per una specifica proiezione.
     *
     * @param p la proiezione da verificare
     * @return il numero di posti liberi rimanenti
     */
    public int calcolaPostiDisponibili(Proiezione p) {
        int postiOccupati = 0;
        for (Prenotazione pren : listaPrenotazioni) {
            if (pren.getTitoloFilm().equalsIgnoreCase(p.getTitoloFilm()) &&
                    pren.getDataOraProiezione().equals(p.getDataOraProiezione())) {
                postiOccupati += pren.getNumeroPosti();
            }
        }
        return Math.max(0, Proiezione.POSTI_TOTALI - postiOccupati);
    }

    /**
     * Verifica se esistono prenotazioni attive per una determinata proiezione.
     *
     * @param p la proiezione da controllare
     * @return {@code true} se esiste almeno una prenotazione associata
     */
    public boolean haPrenotazioniEsistenti(Proiezione p) {
        for (Prenotazione pren : listaPrenotazioni) {
            if (pren.getTitoloFilm().equalsIgnoreCase(p.getTitoloFilm()) &&
                    pren.getDataOraProiezione().equals(p.getDataOraProiezione())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se una nuova proiezione si sovrappone ad altre già in palinsesto.
     *
     * @param nuovaInizio        data e ora di inizio
     * @param nuovaDurataMinuti  durata in minuti
     * @return {@code true} se esiste una sovrapposizione oraria
     */
    public boolean haSovrapposizione(LocalDateTime nuovaInizio, int nuovaDurataMinuti) {
        return haSovrapposizione(nuovaInizio, nuovaDurataMinuti, null);
    }

    /**
     * Verifica se un intervallo di proiezione si sovrappone ad altre proiezioni in palinsesto,
     * escludendo opzionalmente la proiezione stessa in caso di modifica orario.
     *
     * @param nuovaInizio        data e ora di inizio
     * @param nuovaDurataMinuti  durata in minuti
     * @param daEscludere        proiezione da ignorare nel controllo (o {@code null})
     * @return {@code true} se esiste una sovrapposizione con un'altra proiezione
     */
    public boolean haSovrapposizione(LocalDateTime nuovaInizio, int nuovaDurataMinuti, Proiezione daEscludere) {
        LocalDateTime nuovaFine = nuovaInizio.plusMinutes(nuovaDurataMinuti);

        for (Proiezione p : listaProiezioni) {
            if (p == daEscludere) {
                continue;
            }
            LocalDateTime pInizio = p.getDataOraProiezione();
            LocalDateTime pFine = pInizio.plusMinutes(p.getDurataMinuti());

            if (nuovaInizio.isBefore(pFine) && nuovaFine.isAfter(pInizio)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Aggiunge una nuova proiezione al palinsesto e aggiorna il CSV.
     *
     * @param p la proiezione da aggiungere
     */
    public void aggiungiProiezione(Proiezione p) {
        if (p != null) {
            listaProiezioni.add(p);
            salvaProiezioniSuCSV();
        }
    }

    /**
     * Modifica data e ora di una proiezione esistente se priva di prenotazioni
     * e senza sovrapposizioni con altri spettacoli ({@code modificaProiezione()}).
     *
     * @param p            la proiezione da modificare
     * @param nuovaDataOra la nuova data e ora proposta
     * @return {@code true} se la modifica è riuscita, {@code false} altrimenti
     */
    public boolean modificaProiezione(Proiezione p, LocalDateTime nuovaDataOra) {
        if (p == null || nuovaDataOra == null) {
            return false;
        }
        if (haPrenotazioniEsistenti(p)) {
            return false;
        }
        if (haSovrapposizione(nuovaDataOra, p.getDurataMinuti(), p)) {
            return false;
        }
        p.setDataOraProiezione(nuovaDataOra);
        salvaProiezioniSuCSV();
        return true;
    }

    /**
     * Elimina una proiezione dal palinsesto se non ha prenotazioni attive ({@code eliminaProiezione()}).
     *
     * @param p la proiezione da eliminare
     * @return {@code true} se eliminata con successo, {@code false} se bloccata o non trovata
     */
    public boolean eliminaProiezione(Proiezione p) {
        if (p == null || haPrenotazioniEsistenti(p)) {
            return false;
        }
        boolean rimossa = listaProiezioni.remove(p);
        if (rimossa) {
            salvaProiezioniSuCSV();
        }
        return rimossa;
    }

    // =========================================================================
    // GESTIONE E RICERCA PRENOTAZIONI (CLIENTE E BIGLIETTAIO)
    // =========================================================================

    /**
     * Aggiunge una prenotazione direttamente alla lista in memoria.
     *
     * @param p la prenotazione da aggiungere
     */
    public void aggiungiPrenotazione(Prenotazione p) {
        if (p != null) {
            listaPrenotazioni.add(p);
        }
    }

    /**
     * Genera un codice prenotazione alfanumerico di 8 caratteri garantendone l'univocità.
     *
     * @return codice univoco mai usato in precedenza
     */
    private String generaCodiceUnivoco() {
        while (true) {
            String candidato = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            boolean duplicato = false;
            for (Prenotazione p : listaPrenotazioni) {
                if (p.getCodicePrenotazione().equalsIgnoreCase(candidato)) {
                    duplicato = true;
                    break;
                }
            }
            if (!duplicato) {
                return candidato;
            }
        }
    }

    /**
     * Crea una nuova prenotazione per un cliente verificando data futura e capienza sala
     * ({@code creaPrenotazione()}).
     *
     * @param cliente    il cliente che effettua la prenotazione
     * @param proiezione la proiezione scelta
     * @param numPosti   il numero di posti richiesti
     * @return la {@link Prenotazione} creata e salvata, oppure {@code null} se non valida
     */
    public Prenotazione creaPrenotazione(Utente cliente, Proiezione proiezione, int numPosti) {
        if (cliente == null || proiezione == null || numPosti <= 0) {
            return null;
        }
        if (!proiezione.getDataOraProiezione().isAfter(LocalDateTime.now())) {
            return null;
        }
        if (numPosti > calcolaPostiDisponibili(proiezione)) {
            return null;
        }

        Prenotazione nuova = new Prenotazione(
                cliente.getUsername(),
                proiezione.getTitoloFilm(),
                proiezione.getDataOraProiezione(),
                numPosti,
                proiezione.getPrezzoBiglietto()
        );
        nuova.setCodicePrenotazione(generaCodiceUnivoco());
        listaPrenotazioni.add(nuova);
        salvaPrenotazioni();
        return nuova;
    }

    /**
     * Restituisce tutte le prenotazioni effettuate da uno specifico cliente.
     *
     * @param username lo username del cliente
     * @return lista delle prenotazioni del cliente
     */
    public List<Prenotazione> getPrenotazioniCliente(String username) {
        List<Prenotazione> mie = new ArrayList<>();
        for (Prenotazione p : listaPrenotazioni) {
            if (p.getUsernameCliente().equalsIgnoreCase(username)) {
                mie.add(p);
            }
        }
        return mie;
    }

    /**
     * Modifica la data/proiezione di una prenotazione esistente ({@code modificaPrenotazione()}).
     * <p>
     * Verifica che sia la vecchia sia la nuova data siano successive all'istante attuale
     * e che la nuova proiezione disponga di posti sufficienti.
     * </p>
     *
     * @param pren            la prenotazione da modificare
     * @param nuovaProiezione la nuova proiezione di destinazione
     * @return {@code true} se la modifica è andata a buon fine, {@code false} altrimenti
     */
    public boolean modificaPrenotazione(Prenotazione pren, Proiezione nuovaProiezione) {
        if (pren == null || nuovaProiezione == null) {
            return false;
        }
        LocalDateTime adesso = LocalDateTime.now();
        if (!pren.getDataOraProiezione().isAfter(adesso) || !nuovaProiezione.getDataOraProiezione().isAfter(adesso)) {
            return false;
        }
        if (pren.getTitoloFilm().equalsIgnoreCase(nuovaProiezione.getTitoloFilm()) &&
                pren.getDataOraProiezione().equals(nuovaProiezione.getDataOraProiezione())) {
            return false;
        }
        if (calcolaPostiDisponibili(nuovaProiezione) < pren.getNumeroPosti()) {
            return false;
        }

        pren.setTitoloFilm(nuovaProiezione.getTitoloFilm());
        pren.setDataOraProiezione(nuovaProiezione.getDataOraProiezione());
        pren.setCostoSingolo(nuovaProiezione.getPrezzoBiglietto());
        salvaPrenotazioni();
        return true;
    }

    /**
     * Cancella una prenotazione futura non ancora trascorsa ({@code eliminaPrenotazione()}).
     * <p>
     * Consente l'annullamento solo se la data di proiezione è successiva all'istante attuale.
     * </p>
     *
     * @param pren la prenotazione da eliminare
     * @return {@code true} se eliminata con successo, {@code false} se già passata o non trovata
     */
    public boolean eliminaPrenotazione(Prenotazione pren) {
        if (pren == null || !pren.getDataOraProiezione().isAfter(LocalDateTime.now())) {
            return false;
        }
        boolean rimossa = listaPrenotazioni.remove(pren);
        if (rimossa) {
            salvaPrenotazioni();
        }
        return rimossa;
    }

    /**
     * Restituisce tutte le prenotazioni previste per la data odierna.
     *
     * @return lista delle prenotazioni di oggi
     */
    public List<Prenotazione> getPrenotazioniOdierne() {
        List<Prenotazione> odierne = new ArrayList<>();
        LocalDate oggi = LocalDate.now();

        for (Prenotazione p : listaPrenotazioni) {
            if (p.getDataOraProiezione().toLocalDate().equals(oggi)) {
                odierne.add(p);
            }
        }
        return odierne;
    }

    /**
     * Esegue una ricerca multi-criterio sulle prenotazioni per il Bigliettaio ({@code cercaPrenotazione()}).
     * <p>
     * Tutti i parametri sono opzionali e combinabili: codice prenotazione, nome e cognome del cliente,
     * titolo (anche parziale) del film e intervallo di date.
     * </p>
     *
     * @param codice      codice prenotazione o sua parte (opzionale)
     * @param nomeCognome nome e/o cognome del cliente (opzionale)
     * @param titoloFilm  titolo o parte del titolo del film (opzionale)
     * @param dataDa      data iniziale inclusa (opzionale)
     * @param dataA       data finale inclusa (opzionale)
     * @return lista delle prenotazioni corrispondenti ai criteri
     */
    public List<Prenotazione> cercaPrenotazione(String codice, String nomeCognome,
                                                String titoloFilm, LocalDate dataDa, LocalDate dataA) {
        List<Prenotazione> risultati = new ArrayList<>();

        String codFiltro = (codice != null && !codice.trim().isEmpty()) ? codice.trim().toLowerCase() : null;
        String cliFiltro = (nomeCognome != null && !nomeCognome.trim().isEmpty()) ? nomeCognome.trim().toLowerCase() : null;
        String titFiltro = (titoloFilm != null && !titoloFilm.trim().isEmpty()) ? titoloFilm.trim().toLowerCase() : null;

        for (Prenotazione p : listaPrenotazioni) {
            if (codFiltro != null && !p.getCodicePrenotazione().toLowerCase().contains(codFiltro)) {
                continue;
            }
            if (cliFiltro != null) {
                String nomeCompleto = getNomeCompletoUtente(p.getUsernameCliente()).toLowerCase();
                if (!nomeCompleto.contains(cliFiltro) && !p.getUsernameCliente().toLowerCase().contains(cliFiltro)) {
                    continue;
                }
            }
            if (titFiltro != null && !p.getTitoloFilm().toLowerCase().contains(titFiltro)) {
                continue;
            }
            LocalDate dataPren = p.getDataOraProiezione().toLocalDate();
            if (dataDa != null && dataPren.isBefore(dataDa)) {
                continue;
            }
            if (dataA != null && dataPren.isAfter(dataA)) {
                continue;
            }
            risultati.add(p);
        }
        return risultati;
    }

    /**
     * Ricerca generica su prenotazioni con singola chiave testuale (compatibilità rapida).
     *
     * @param criterio testo da cercare in codice, nominativo cliente, titolo o data
     * @return lista delle prenotazioni trovate
     */
    public List<Prenotazione> cercaPrenotazioni(String criterio) {
        List<Prenotazione> risultati = new ArrayList<>();
        String query = criterio.toLowerCase().trim();

        for (Prenotazione p : listaPrenotazioni) {
            String nomeCompleto = getNomeCompletoUtente(p.getUsernameCliente()).toLowerCase();
            if (p.getCodicePrenotazione().toLowerCase().contains(query) ||
                    nomeCompleto.contains(query) ||
                    p.getUsernameCliente().toLowerCase().contains(query) ||
                    p.getTitoloFilm().toLowerCase().contains(query) ||
                    p.getDataOraStringa().toLowerCase().contains(query)) {
                risultati.add(p);
            }
        }
        return risultati;
    }
}