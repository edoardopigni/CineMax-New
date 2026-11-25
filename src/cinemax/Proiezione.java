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

/**
 * Rappresenta una singola proiezione cinematografica programmata nel palinsesto del cinema.
 *
 * @author Daniele Paoli
 * @author Edoardo Pigni
 * @author Anes Khaia
 * @version 1.1
 */
public class Proiezione implements Serializable {

    /** Identificativo univoco di versione per la serializzazione. */
    private static final long serialVersionUID = 2L;

    /** Capienza massima standard di posti a sedere per la sala di proiezione. */
    public static final int POSTI_TOTALI = 200;

    /** Data e ora di programmazione della proiezione. */
    private LocalDateTime dataOraProiezione;

    /** Titolo dell'opera cinematografica proiettata. */
    private String titoloFilm;

    /** Genere cinematografico del film. */
    private String genere;

    /** Nome e cognome del regista del film. */
    private String regista;

    /** Anno di uscita del film. */
    private int anno;

    /** Durata complessiva in minuti. */
    private int durataMinuti;

    /** Età minima consigliata o vincolante per la visione. */
    private int etaMinima;

    /** Costo unitario del biglietto in Euro. */
    private double prezzoBiglietto;

    /**
     * Costruttore predefinito privo di argomenti.
     */
    public Proiezione() {
    }

    /**
     * Costruttore completo per inizializzare una proiezione cinematografica.
     *
     * @param dataOraProiezione data e ora dello spettacolo
     * @param titoloFilm        titolo del film
     * @param genere            genere cinematografico
     * @param regista           regista del film
     * @param anno              anno di produzione
     * @param durataMinuti      durata in minuti
     * @param etaMinima         età minima richiesta
     * @param prezzoBiglietto   tariffa unitaria del biglietto
     */
    public Proiezione(LocalDateTime dataOraProiezione, String titoloFilm, String genere, String regista,
                      int anno, int durataMinuti, int etaMinima, double prezzoBiglietto) {
        this.dataOraProiezione = dataOraProiezione;
        this.titoloFilm = titoloFilm;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durataMinuti = durataMinuti;
        this.etaMinima = etaMinima;
        this.prezzoBiglietto = prezzoBiglietto;
    }

    /** @return data e ora della proiezione */
    public LocalDateTime getDataOraProiezione() {
        return dataOraProiezione;
    }

    /** @param dataOraProiezione la nuova data e ora della proiezione */
    public void setDataOraProiezione(LocalDateTime dataOraProiezione) {
        this.dataOraProiezione = dataOraProiezione;
    }

    /** @return il titolo del film */
    public String getTitoloFilm() {
        return titoloFilm;
    }

    /** @param titoloFilm il nuovo titolo del film */
    public void setTitoloFilm(String titoloFilm) {
        this.titoloFilm = titoloFilm;
    }

    /** @return il genere del film */
    public String getGenere() {
        return genere;
    }

    /** @param genere il nuovo genere del film */
    public void setGenere(String genere) {
        this.genere = genere;
    }

    /** @return il regista del film */
    public String getRegista() {
        return regista;
    }

    /** @param regista il nuovo regista del film */
    public void setRegista(String regista) {
        this.regista = regista;
    }

    /** @return l'anno di uscita del film */
    public int getAnno() {
        return anno;
    }

    /** @param anno il nuovo anno di uscita */
    public void setAnno(int anno) {
        this.anno = anno;
    }

    /** @return la durata del film in minuti */
    public int getDurataMinuti() {
        return durataMinuti;
    }

    /** @param durataMinuti la nuova durata in minuti */
    public void setDurataMinuti(int durataMinuti) {
        this.durataMinuti = durataMinuti;
    }

    /** @return l'età minima richiesta */
    public int getEtaMinima() {
        return etaMinima;
    }

    /** @param etaMinima la nuova età minima */
    public void setEtaMinima(int etaMinima) {
        this.etaMinima = etaMinima;
    }

    /** @return il prezzo unitario del biglietto */
    public double getPrezzoBiglietto() {
        return prezzoBiglietto;
    }

    /** @param prezzoBiglietto il nuovo prezzo del biglietto */
    public void setPrezzoBiglietto(double prezzoBiglietto) {
        this.prezzoBiglietto = prezzoBiglietto;
    }

    /**
     * Restituisce una scheda completa con tutti i dettagli della proiezione e i posti liberi.
     *
     * @param postiLiberi il numero di posti ancora disponibili in sala
     * @return stringa multi-riga formattata per la funzione {@code visualizzaProiezione()}
     */
    public String toDettaglioString(int postiLiberi) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format(Locale.ITALY,
                "========================================%n" +
                "Titolo:         %s%n" +
                "Genere:         %s%n" +
                "Regista:        %s%n" +
                "Anno:           %d%n" +
                "Durata:         %d min%n" +
                "Età minima:     %d+%n" +
                "Data e Ora:     %s%n" +
                "Prezzo:         %.2f€%n" +
                "Posti liberi:   %d / %d%n" +
                "========================================",
                titoloFilm, genere, regista, anno, durataMinuti, etaMinima,
                dataOraProiezione.format(formatter), prezzoBiglietto,
                postiLiberi, POSTI_TOTALI);
    }

    /**
     * Restituisce una rappresentazione compatta della proiezione per gli elenchi.
     *
     * @return stringa nel formato {@code "GG/MM/AAAA HH:mm | Titolo (Anno) [Genere] - Prezzo€"}
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format(Locale.ITALY, "%s | %s (%d) [%s] - %.2f€",
                dataOraProiezione.format(formatter), titoloFilm, anno, genere, prezzoBiglietto);
    }
}