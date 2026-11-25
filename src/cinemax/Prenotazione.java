/*
 * Progetto CineMax - Laboratorio Interdisciplinare A
 * Autori:
 * - Daniele Paoli, Matricola: [MATRICOLA], Sede: VA
 * - Edoardo Pigni, Matricola: 761118, Sede: VA
 * - Anes Khaia, Matricola: 760038, Sede: VA
 */
package cinemax;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Rappresenta la prenotazione di biglietti effettuata da un Cliente per una specifica Proiezione.
 *
 * @author Daniele Paoli
 * @author Edoardo Pigni
 * @author Anes Khaia
 * @version 1.1
 */
public class Prenotazione implements Serializable {

    /** Identificativo univoco di versione per la serializzazione. */
    private static final long serialVersionUID = 2L;

    /** Formatter standard per la rappresentazione testuale di data e ora. */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Codice identificativo alfanumerico univoco della prenotazione. */
    private String codicePrenotazione;

    /** Username del cliente titolare della prenotazione. */
    private String usernameCliente;

    /** Titolo del film prenotato. */
    private String titoloFilm;

    /** Data e ora della proiezione prenotata. */
    private LocalDateTime dataOraProiezione;

    /** Numero di posti riservati. */
    private int numeroPosti;

    /** Costo unitario del singolo biglietto in Euro. */
    private double costoSingolo;

    /** Importo totale della prenotazione in Euro. */
    private double costoTotale;

    /**
     * Costruttore per creare una nuova prenotazione con codice univoco generato automaticamente.
     *
     * @param usernameCliente   lo username del cliente
     * @param titoloFilm        il titolo del film
     * @param dataOraProiezione la data e l'ora della proiezione
     * @param numeroPosti       il numero di biglietti richiesti
     * @param costoSingolo      il costo unitario del singolo biglietto
     */
    public Prenotazione(String usernameCliente, String titoloFilm, LocalDateTime dataOraProiezione,
                        int numeroPosti, double costoSingolo) {
        this.codicePrenotazione = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.usernameCliente = usernameCliente;
        this.titoloFilm = titoloFilm;
        this.dataOraProiezione = dataOraProiezione;
        this.numeroPosti = numeroPosti;
        this.costoSingolo = costoSingolo;
        this.costoTotale = numeroPosti * costoSingolo;
    }

    /** @return il codice univoco a 8 caratteri della prenotazione */
    public String getCodicePrenotazione() {
        return codicePrenotazione;
    }

    /** @param codicePrenotazione il nuovo codice prenotazione */
    public void setCodicePrenotazione(String codicePrenotazione) {
        this.codicePrenotazione = codicePrenotazione;
    }

    /** @return lo username del cliente titolare */
    public String getUsernameCliente() {
        return usernameCliente;
    }

    /** @param usernameCliente il nuovo username del cliente */
    public void setUsernameCliente(String usernameCliente) {
        this.usernameCliente = usernameCliente;
    }

    /** @return il titolo del film prenotato */
    public String getTitoloFilm() {
        return titoloFilm;
    }

    /** @param titoloFilm il nuovo titolo del film */
    public void setTitoloFilm(String titoloFilm) {
        this.titoloFilm = titoloFilm;
    }

    /** @return data e ora della proiezione come {@link LocalDateTime} */
    public LocalDateTime getDataOraProiezione() {
        return dataOraProiezione;
    }

    /** @param dataOraProiezione la nuova data e ora della proiezione */
    public void setDataOraProiezione(LocalDateTime dataOraProiezione) {
        this.dataOraProiezione = dataOraProiezione;
    }

    /**
     * Restituisce data e ora della proiezione formattate come stringa ({@code dd/MM/yyyy HH:mm}).
     *
     * @return la data e ora in formato testuale
     */
    public String getDataOraStringa() {
        return dataOraProiezione != null ? dataOraProiezione.format(FORMATTER) : "";
    }

    /** @return il numero di posti prenotati */
    public int getNumeroPosti() {
        return numeroPosti;
    }

    /**
     * Aggiorna il numero di posti e ricalcola automaticamente il costo totale.
     *
     * @param numeroPosti la nuova quantità di posti
     */
    public void setNumeroPosti(int numeroPosti) {
        this.numeroPosti = numeroPosti;
        this.costoTotale = this.numeroPosti * this.costoSingolo;
    }

    /** @return il costo unitario del singolo biglietto */
    public double getCostoSingolo() {
        return costoSingolo;
    }

    /**
     * Aggiorna il costo unitario e ricalcola automaticamente il costo totale.
     *
     * @param costoSingolo il nuovo prezzo unitario del biglietto
     */
    public void setCostoSingolo(double costoSingolo) {
        this.costoSingolo = costoSingolo;
        this.costoTotale = this.numeroPosti * this.costoSingolo;
    }

    /** @return il costo totale della prenotazione */
    public double getCostoTotale() {
        return costoTotale;
    }

    /** @param costoTotale il nuovo costo totale */
    public void setCostoTotale(double costoTotale) {
        this.costoTotale = costoTotale;
    }

    /**
     * Restituisce una scheda dettagliata della prenotazione per {@code visualizzaPrenotazione()}.
     *
     * @param nomeCompletoCliente nome e cognome del cliente titolare
     * @return stringa multi-riga con tutti i campi richiesti dalle specifiche
     */
    public String toDettaglioString(String nomeCompletoCliente) {
        return String.format(Locale.ITALY,
                "========================================%n" +
                "Codice Prenotazione: %s%n" +
                "Cliente:             %s (%s)%n" +
                "Film:                %s%n" +
                "Data e Ora:          %s%n" +
                "Numero Biglietti:    %d%n" +
                "Costo Unitario:      %.2f€%n" +
                "Costo Totale:        %.2f€%n" +
                "========================================",
                codicePrenotazione, nomeCompletoCliente, usernameCliente,
                titoloFilm, getDataOraStringa(), numeroPosti, costoSingolo, costoTotale);
    }

    /**
     * Restituisce una rappresentazione compatta della prenotazione.
     *
     * @return stringa riassuntiva della prenotazione
     */
    @Override
    public String toString() {
        return String.format(Locale.ITALY,
                "Prenotazione %s: %s il %s - Posti: %d (Unitario: %.2f€ | Totale: %.2f€)",
                codicePrenotazione, titoloFilm, getDataOraStringa(), numeroPosti, costoSingolo, costoTotale);
    }
}