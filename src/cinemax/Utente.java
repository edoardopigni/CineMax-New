/*
 * Progetto CineMax - Laboratorio Interdisciplinare A
 * Autori:
 * - Daniele Paoli, Matricola: [MATRICOLA], Sede: VA
 * - Edoardo Pigni, Matricola: 761118, Sede: VA
 * - Anes Khaia, Matricola: 760083, Sede: VA
 */
package cinemax;

import java.io.Serializable;

/**
 * Rappresenta un utente registrato all'interno della piattaforma CineMax.
 * <p>
 * La classe modella le informazioni anagrafiche, le credenziali di autenticazione
 * (con password salvata in formato cifrato) e il ruolo operativo nel sistema.
 * </p>
 *
 * @author Daniele Paoli
 * @author Edoardo Pigni
 * @author Anes Khaia
 * @version 1.1
 */
public class Utente implements Serializable {

    /** Identificativo univoco di versione per la serializzazione. */
    private static final long serialVersionUID = 2L;

    /** Nome anagrafico dell'utente. */
    private String nome;

    /** Cognome anagrafico dell'utente. */
    private String cognome;

    /** Identificativo univoco utilizzato per l'accesso al sistema. */
    private String username;

    /** Hash cifrato della password dell'account. */
    private String password;

    /** Data di nascita dell'utente in formato testuale (facoltativa). */
    private String dataNascita;

    /** Comune di domicilio dell'utente. */
    private String luogoDomicilio;

    /** Ruolo applicativo assegnato ({@code "CLIENTE"}, {@code "PROIEZIONISTA"}, {@code "BIGLIETTAIO"}). */
    private String ruolo;

    /**
     * Costruisce una nuova istanza di {@code Utente}.
     *
     * @param nome           il nome dell'utente
     * @param cognome        il cognome dell'utente
     * @param username       l'identificativo univoco di login
     * @param password       la password in formato cifrato
     * @param dataNascita    la data di nascita (può essere vuota)
     * @param luogoDomicilio il luogo di domicilio
     * @param ruolo          il ruolo applicativo dell'utente
     */
    public Utente(String nome, String cognome, String username, String password,
                  String dataNascita, String luogoDomicilio, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
        this.ruolo = ruolo;
    }

    /** @return il nome anagrafico dell'utente */
    public String getNome() {
        return nome;
    }

    /** @param nome il nuovo nome da impostare */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /** @return il cognome anagrafico dell'utente */
    public String getCognome() {
        return cognome;
    }

    /** @param cognome il nuovo cognome da impostare */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Restituisce nome e cognome concatenati.
     *
     * @return stringa nel formato {@code "Nome Cognome"}
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    /** @return lo username univoco dell'account */
    public String getUsername() {
        return username;
    }

    /** @param username il nuovo username da impostare */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return l'hash cifrato della password */
    public String getPassword() {
        return password;
    }

    /** @param password il nuovo hash cifrato della password */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return la data di nascita (o stringa vuota se non specificata) */
    public String getDataNascita() {
        return dataNascita;
    }

    /** @param dataNascita la nuova data di nascita */
    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }

    /** @return il luogo di domicilio */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /** @param luogoDomicilio il nuovo luogo di domicilio */
    public void setLuogoDomicilio(String luogoDomicilio) {
        this.luogoDomicilio = luogoDomicilio;
    }

    /** @return il ruolo applicativo assegnato */
    public String getRuolo() {
        return ruolo;
    }

    /** @param ruolo il nuovo ruolo da assegnare */
    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    /**
     * Restituisce una rappresentazione sintetica dell'utente.
     *
     * @return stringa riassuntiva con username, nominativo e ruolo
     */
    @Override
    public String toString() {
        return "Utente [" + username + "] (" + getNomeCompleto() + ") - Ruolo: " + ruolo;
    }
}