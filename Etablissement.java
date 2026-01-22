/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package StationLavage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

/**
 *
 * @author atton
 */

public class Etablissement {

    private String nom;
    private int nbClient;
    private int max_nb_client;
    private Client[] tab_Client;
    private RendezVous[][] planning;
    private LocalDateTime calendrier_first_date;
    private LocalDateTime calendrier_last_date;
    private int diff_day_start_end;
    
    // Constructeur
    public Etablissement(String nom, int max_nb_client) {
        this.nom = nom;
        this.max_nb_client = max_nb_client;
        this.nbClient = 0;
        this.tab_Client = new Client[max_nb_client];

        // 7 jours x 16 créneaux (10h → 18h, toutes les 30 min)
        this.calendrier_first_date = LocalDate.now().atTime(10, 0);
        this.calendrier_last_date = LocalDate.now().plusMonths(1).atTime(18, 0);
        
        int nb_day_start_and_end = (int) ChronoUnit.DAYS.between(calendrier_first_date, calendrier_last_date);
        this.diff_day_start_end = nb_day_start_and_end;
        this.planning = new RendezVous[nb_day_start_and_end][16];
        
    }

    // Recherche d'un client par nom et numéro
    public Client rechercher(String nom, String numeroTel) {
        for (int i = 0; i < nbClient; i++) {
            if (tab_Client[i].getNom().equalsIgnoreCase(nom)
                && tab_Client[i].getNumeroTel().equals(numeroTel)) {
                return tab_Client[i];
            }
        }
        return null;
    }
    
    private Client rechercher(int id) {
        for (int i = 0; i < nbClient; i++) {
            if (tab_Client[i].getIdClient() == id){
                return tab_Client[i];
            }
        }
        return null;
    }

    private Client ajouter(Client client) {
        Client existant = rechercher(client.getIdClient());
        if (existant == null){
            int i = nbClient - 1;
            while (i >= 0 && tab_Client[i].placerApres(client)) {
            tab_Client[i + 1] = tab_Client[i];
            i--;
            }
            tab_Client[i + 1] = client;
            nbClient++;
        }
        return client;
        
    }
    // Ajouter un client sans email
    public Client ajouter(String nom, String numeroTel) {

        Client existant = rechercher(nom, numeroTel);
        if (existant != null) {
            System.out.println("Le client existe déjà : " + existant);
            return existant;
        }

        if (nbClient >= max_nb_client) {
            System.out.println("Nombre maximum de clients atteint");
            return null;
        }

        Client nouveau = new Client(nom, numeroTel);

        int i = nbClient - 1;
        while (i >= 0 && tab_Client[i].placerApres(nouveau)) {
            tab_Client[i + 1] = tab_Client[i];
            i--;
        }

        tab_Client[i + 1] = nouveau;
        nbClient++;
        return nouveau;
    }

    // Ajouter un client avec email
    public Client ajouter(String nom, String numeroTel, String email) {

        Client existant = rechercher(nom, numeroTel);
        if (existant != null) {
            System.out.println("Le client existe déjà : " + existant);
            return existant;
        }

        if (nbClient >= max_nb_client) {
            System.out.println("Nombre maximum de clients atteint");
            return null;
        }

        Client nouveau = new Client( nom,  numeroTel, email);
        // Regarder et documenter l'erreur

        int i = nbClient - 1;
        while (i >= 0 && tab_Client[i].placerApres(nouveau)) {
            tab_Client[i + 1] = tab_Client[i];
            i--;
        }

        tab_Client[i + 1] = nouveau;
        nbClient++;
        return nouveau;
    }
    private int indiceJour(LocalDateTime dateTime) {
        return (int) ChronoUnit.DAYS.between(calendrier_first_date, dateTime);
    } 
    
    private int indiceCreneau(LocalDateTime dateTime) {
        int heure = dateTime.getHour();
        int minute = dateTime.getMinute();
        return (heure - 10) * 2 + (minute >= 30 ? 1 : 0);
    }
    
    public LocalDateTime rechercher(LocalDate date){
        LocalDateTime datetime = date.atTime(10, 0);
        int diff_day= indiceJour(datetime);
        
        if(diff_day<0 ){
            System.out.println("La date doit être après : " + calendrier_first_date.toString());
            return null;
        }
        
        if(diff_day>this.diff_day_start_end){
            System.out.println("La date doit être avant : " + calendrier_first_date.toString());
            return null;
        }
        
        String sb = "";
        Boolean as_date = false;
        int[] heure_dispo = new int[17];
        heure_dispo[16] = 1;
        for(int i=0; i<16;i++){
            if(planning[diff_day][i]==null){
                sb += i+") "+(i*30/60+10)+"h "+ (i*30%60) +"min\n";
                as_date = true;
                heure_dispo[i] = 1;
            }else{
                heure_dispo[i]=-1;
            }
        }
        sb += 16+") aucune de ces heures";
        if(as_date){
            System.out.println("Selectioner une des heures disponilbe");
            System.out.println(sb);
            Scanner scan = new Scanner(System.in);
            int input = scan.nextInt();
            while((input>=0 && input<=16 ) && heure_dispo[input]== -1){
                System.out.println("choisicer une valeur donnée");
            }
            if(input==16){
                return null;
            }
            return datetime.plusMinutes(input*30);
        }
        System.out.println("Il n'y a aucune heure disponible");
        return null;
    }

    
    public LocalDateTime rechercher(LocalTime time){
        time = time.withMinute(time.getMinute()< 30 ? 0 : 30);
        LocalDateTime datetime = time.atDate(LocalDate.now());
       
        int diff_time=  indiceCreneau(datetime);
        if(diff_time<0 || diff_time>16 ){
            System.out.println("l'heure doit être comprise entre 10h et 18h");
        }
        
        String sb = "";
        Boolean as_time = false;
        int diff_day_start_to_now = this.indiceJour(datetime);
        
        int[] date_dispo = new int[diff_day_start_end-diff_day_start_to_now+1];
        date_dispo[diff_day_start_end] = 1;
        for(int i=0; i<diff_day_start_end-diff_day_start_to_now; i++){
            if(planning[i][diff_time]==null){
                sb += i+") "+datetime.plusDays(i)+"\n";
                as_time = true;
                date_dispo[i] = 1;
            }else{
                date_dispo[i] = -1;
            }
        }
        sb += diff_day_start_end-diff_day_start_to_now+") aucune de ces heures";
        if(as_time){
            System.out.println("Selectioner un des jour disponible");
            System.out.println(sb);
            Scanner scan = new Scanner(System.in);
            int input = scan.nextInt();
            while((input>=0 && input<=diff_day_start_end-diff_day_start_to_now ) && (date_dispo[input]==-1)){
                System.out.println("choisicer une valeur donnée");
            }
            if(input==diff_day_start_end-diff_day_start_to_now){
                return null;
            }
            return calendrier_first_date.plusDays(input).plusMinutes(diff_time);
        }
        System.out.println("Il n'y a aucune heure disponible");
        return null;
    }
    public RendezVous ajouter(Client client, LocalDateTime dateTime, char classeVehicule, boolean lavageInterne) {
 
        int jour = indiceJour(dateTime);
        int creneau = indiceCreneau(dateTime);
 
        Prestation prestation = new PrestationExpress(classeVehicule, lavageInterne);
 
        RendezVous rdv = new RendezVous(client, prestation);
        System.out.println(dateTime);
        System.out.println(jour);
        System.out.println(creneau);
        planning[jour][creneau] = rdv;
 
        return rdv;
    }
    
    public RendezVous ajouter(Client client, LocalDateTime dateTime, char classeVehicule) {
    int jour = indiceJour(dateTime);
    int creneau = indiceCreneau(dateTime);
    Prestation prestation = new PrestationSale(classeVehicule);
    RendezVous rdv = new RendezVous(client, prestation);
    planning[jour][creneau] = rdv;
    return rdv;
    } 
    
    
    public RendezVous ajouter(Client client, LocalDateTime datetime, char classeVehicule, String salissure) {
    int jour = indiceJour(datetime);
    int creneau = indiceCreneau(datetime);
    Prestation prestation = new PrestationTresSale(classeVehicule, salissure);
    RendezVous rdv = new RendezVous(client, prestation);
    planning[jour][creneau] = rdv;
    return rdv;
    }
    
    public void planifier() {
 
    Scanner sc = new Scanner(System.in);
 
    System.out.print("Nom du client : ");
    String nom_client = sc.nextLine();
    System.out.print("Numéro de téléphone : ");
    String tel = sc.nextLine();
 
    Client client = rechercher(nom_client, tel);
 
    if (client == null) {

        System.out.println("Nouveau client.");
        System.out.print("Email (laisser vide si aucun) : ");
        String email = sc.nextLine();
 
        if (email.isEmpty()) {
            client = ajouter(nom_client, tel);
        } else {

            client = ajouter(nom_client, tel, email);
        }

    } else {
        System.out.println("Client existant : " + client);
    }
    
    // si on demande un date directement
    /*
    System.out.println("\nDate du rendez-vous yyyy-MM-dd'T'HH:mm");//(année mois jour heure minute)");
    String datetime = sc.nextLine();
    LocalDateTime dateTime = LocalDateTime.parse(datetime);
    int indice_jour = this.indiceJour(LocalDateTime.now());
    */
    
    
    LocalDateTime current_datetime = LocalDateTime.now();
    int indice_jour = indiceJour(current_datetime);
    // on suppose qu'on ne peux pas reserve le jour même
    Boolean as_time = false;
    int[] heure_dispo = new int[16];
    String sb;
    int indice_time = 0;
    int max_day = indice_jour+8<diff_day_start_end ? indice_jour+8 : diff_day_start_end;
    for(int i=indice_jour+1;i<max_day;i++){
        sb="choisicer une heure pour le : " +current_datetime.plusDays(i).toLocalDate() +"\n";
        for(int j=0; j<16;j++){
            if(planning[i][j]==null){
                sb += j+") "+(j*30/60+10)+"h "+ (j*30%60) +"min\n";
                as_time = true;
                heure_dispo[j] = 1;
            }else{
               heure_dispo[j]=-1; 
            }
        }
        sb += 16+") aucune de ces heures";
        if(as_time){
            System.out.println("Selectioner un des créneaux disponible");
            System.out.println(sb);
            Scanner scan = new Scanner(System.in);
            int input = scan.nextInt();
            while(input<0 || input>16 || (input!= 16 && heure_dispo[input]==-1)){
                System.out.println("choisicer une valeur donnée");
            }
            if(input==16){
                System.out.println("___________________________________________");
            }else{
                indice_jour = i;
                indice_time = input;
                break;
            }
        }else{
            System.out.println("Il n'y a aucune heure disponible le "+current_datetime.plusDays(i).toLocalDate());
        } 
    }
    LocalDateTime date_time = current_datetime.plusDays(indice_jour).toLocalDate().atTime(10,0).plusMinutes(indice_time*30);
    

    //int annee = sc.nextInt();
    //int mois = sc.nextInt();
    //int jour = sc.nextInt();
    //int heure = sc.nextInt();
    //int minute = sc.nextInt();
    
    //LocalDateTime dateTime = LocalDateTime.of(annee, mois, jour, heure, minute);
    
    
    
  
    //  int indice_time = this.indiceCreneau(dateTime);
    System.out.println("\nType de prestation :");
    System.out.println("1 - Express");
    System.out.println("2 - Véhicule sale");
    System.out.println("3 - Véhicule très sale");

    int choix = sc.nextInt();
    
    System.out.print("Catégorie du véhicule (A, B ou C) : ");
    char classe = sc.next().charAt(0);
 
    RendezVous rdv = null;
 
    switch (choix) {
        case 1 -> {
            System.out.print("Lavage intérieur ? (true / false) : ");
            boolean interieur = sc.nextBoolean();
            rdv = ajouter(client, date_time, classe, interieur);
            }

        case 2 -> rdv = ajouter(client, date_time, classe);

        case 3 -> {
            sc.nextLine(); 
            System.out.print("Type de salissure : ");
            String salissure = sc.nextLine();
            rdv = ajouter(client, date_time, classe, salissure);
            }
        
        default -> System.out.println("Choix invalide");
    }
 

    if (rdv != null) {
        System.out.println("\nRendez-vous planifié avec succès !");
        System.out.println("Prix de la prestation : " + rdv.getPrix() + " €");
        this.planning[indice_jour][indice_time] = rdv;
    }
}
    public void afficher( LocalDate date){
        int idjour = indiceJour (date.atTime(10,0));
        String sb = "";
        for(int i=0; i<16;i++){
            if(planning[idjour][i]!=null){
                RendezVous rdv = planning[idjour][i];
                sb += (i*30/60+10)+"h "+ (i*30%60) +"min : "+rdv.toString()+"\n";
            }
        }
        System.out.println(sb);
    }
    public static boolean estNumeroTelephone(String tel) {
        String regex = "^(\\+33|0)[1-9](\\s|-)?(\\d{2}(\\s|-)?){4}$";
    return tel.matches(regex);
    }
    public void afficher( String info_client){
        boolean estnum = estNumeroTelephone(info_client);
        Client cur_client;
        if(estnum){
            for(int i=0; i<this.nbClient; i++){
                cur_client = tab_Client[i];
                if(cur_client.getNumeroTel().equals(info_client)){
                    System.out.println(cur_client.toString());
                }
            }
        }else{
            for(int i=0; i<this.nbClient; i++){
                cur_client = tab_Client[i];
                if(cur_client.getNom().equals(info_client)){
                    System.out.println(cur_client.toString());
                }
            }
        }
    }
    
    public void afficher( int id_client){
        for(int i=0; i<this.diff_day_start_end; i++){
            for(int j=0; j<16;j++){
                if(this.planning[i][j]!=null&&this.planning[i][j].getClient().getIdClient() == id_client){
                    System.out.println(this.planning[i][j].toString());
                }
            }
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Etablissement : ").append(nom).append("\n");
        sb.append("Clients :\n");

        for (int i = 0; i < nbClient; i++) {
            sb.append(tab_Client[i]).append("\n");
        }

        return sb.toString();
    }
    
    public void versFichierClient(String file_name) throws IOException {
        String fichier = "";
        
        for(int i=0;i<this.nbClient-1;i++)
            fichier += this.tab_Client[i] +"\n";
        fichier += this.tab_Client[this.nbClient-1];
        
        FileWriter save = new FileWriter(file_name);
        save.write(fichier);
        save.close();
        
    }
    
    public void versFichierRDV(String file_name) throws IOException {
        String fichier = "";
        LocalDateTime cur;
        int max_indice_date = indiceJour(this.calendrier_last_date);
        int max_indice_time = this.indiceCreneau(this.calendrier_last_date);
        for(int i=0;i<max_indice_date;i++)
            for(int j=0; j<max_indice_time;j++)
                if(this.planning[i][j] != null){
                    cur = this.calendrier_first_date.plusDays(i).plusMinutes(j*30);
                    fichier += cur +"\n";
                    fichier += this.planning[i][j].versFichier() +"\n";
                }

        FileWriter save = new FileWriter( file_name);
        save.write(fichier);
        save.close();
        
    }
    
    public void depuisFichierClient(String file_name) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file_name));
        String ligne;
        String[] splited_ligne;
        Client client;
        int id_client;
        while ((ligne = reader.readLine()) != null) {
            splited_ligne = ligne.split(" : ", 4);

            if(splited_ligne.length == 3){
                id_client = Integer.parseInt(splited_ligne[0]);
                client = new Client(id_client,splited_ligne[1],splited_ligne[2]);
                ajouter(client);
            }else if(splited_ligne.length == 4){
                id_client = Integer.parseInt(splited_ligne[0]);
                client = new Client(id_client,splited_ligne[1],splited_ligne[2],splited_ligne[4]);
                ajouter(client);
            }
        }  
    }
    
    public void depuisFichierRDV(String file_name) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file_name));
        String ligne;
        String[] splited_ligne;
        Client client;
        int id_client;
        LocalDateTime dateTime;
        boolean nettoyage;
        int indice_time;
        int indice_day;
        Prestation presta;
        int prix;
        //if (value.equals("true") || value.equals("false")) {
        //return Boolean.parseBoolean(value);
    
        while ((ligne = reader.readLine()) != null) {
            dateTime = LocalDateTime.parse(ligne);
            indice_day = this.indiceJour(dateTime);
            indice_time = this.indiceCreneau(dateTime);
                    
            ligne = reader.readLine();
            id_client = Integer.parseInt(ligne);

            client = new Client(id_client);
            
            ligne = reader.readLine();
            splited_ligne = ligne.split(" : ", 3);
                
            if(splited_ligne.length == 2){
                presta = new PrestationSale(splited_ligne[0].charAt(0));
                this.planning[indice_day][indice_time] = new RendezVous(client, presta);
            }else if(splited_ligne.length == 3){
                if (splited_ligne[1].equals("true") || splited_ligne[1].equals("false")) {
                    nettoyage = Boolean.parseBoolean(splited_ligne[1]);
                    presta = new PrestationExpress(splited_ligne[0].charAt(0), nettoyage);
                    this.planning[indice_day][indice_time] = new RendezVous(client, presta);
                }else{
                    presta = new PrestationTresSale(splited_ligne[0].charAt(0), splited_ligne[1]);
                    this.planning[indice_day][indice_time] = new RendezVous(client, presta);
                }
            }
        }
    }  
    
}
