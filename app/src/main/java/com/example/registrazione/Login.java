package com.example.registrazione;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Login extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {


    TextInputEditText email1;
    TextInputEditText password;
    Thread clientThread;
    Editable email, pass;
    Handler ClientHandler;
    String e = "";
    String p = "";
    //Per mantenere loggato
    private ToggleButton toggleButton;
    private String logged = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        /*
        *** Salvo eMail
         */
        email1 =findViewById(R.id.email);
        email = email1.getText();

        /*
        *** Salvo password
         */
        password =findViewById(R.id.psw);
        pass = password.getText();

        /*
        ***Bottone per andare alla registrazione se non si è registrati
         */
        TextView ToReg = (TextView) findViewById(R.id.registr);
        ToReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // definisco l'intenzione
                Intent openPage1 = new Intent(Login.this,Registrazione.class);
                startActivity(openPage1);
            }
        });

        /*
         ***Bottone per andare alla mappa
         */
        ImageButton ToMap = findViewById(R.id.bottMap);
        ToMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                System.out.println("vado alla mappa");
            }
        });
        /*
        ***Toggle button
         */
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(this);

        /*
         *** Bottone per tornare ala mappa
         */
        ImageButton ToLog = findViewById(R.id.bottRitorno);
        ToLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Intent openPage1 = new Intent(Login.this, Manga.class); Metti l'activity della mappa dove c'e scritto manga
                //startActivity(openPage1);
            }
        });

        /*
        ***Bottone per Login
         */
        Button ToServerLog = findViewById(R.id.bottReg);
        ToServerLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean contrE = false;         //Controllo per email
                boolean contrP = false;         //Controllo per password
                //Editable ---> String
                e = email.toString();
                p = pass.toString();
                System.out.println("-------------------------------TASTO LOGIN------------------------------");
                /*
                ***Controllo se email è vuoto
                 */
                if(e.isEmpty()){
                    email1.setError("Immetti una mail corretta");
                }else{
                    contrE = true;  //Se è corretto contrE diventa true
                }
                /*
                ***Controllo se psw è vuoto, se è troppo lunga
                 */

                if(p.isEmpty()){
                        password.setError("Immetti una psw corretta");

                }else if(p.length()>16){
                    password.setError("Immetti una psw più corta");
                }else{
                    contrP = true;  //Se è corretto contrP diventa true
                }
                System.out.println(" email:"+e+" psw:"+p);

                /*
                *** Se non ci sono errori
                 */
                if(contrP && contrE){

                    clientThread = new Thread(new clientThread());
                    ClientHandler = new Handler();
                    clientThread.start();

                }
            }
        });

    }

    /*
    ***Toggle button
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(toggleButton.isChecked()){
            logged = "si";
            System.out.println("ciaoo");
        }else{
            logged = "no";
        }
    }

    /*
    ***Thread per socket
     */

    class clientThread implements Runnable{


        @Override
        public void run() {
            try {
                SSLSocketFactory factory =
                        (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket =
                        (SSLSocket) factory.createSocket("avofinder.altervista.org", 443);

                socket.startHandshake();

                BufferedWriter bw = null;
                /*
                 ***Applico Sha256 alla password (p)
                 */
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(p.getBytes(StandardCharsets.UTF_8));
                String pswCript = String.format("%064x", new BigInteger(1, hash));  //Viene inviata la password criptata

                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                bw.write("POST http://avofinder.altervista.org/index.php?email="+e+"&psw="+pswCript+"&contr=1 HTTP/1.1\r\n");
                bw.write("Host: altervista\r\n");
                bw.write("Cache-Control: no-cache\r\n");
                bw.write("\r\n");
                bw.flush();

                /* leggo risposta */
                String lineD;
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Le prime otto righe dovrebbero essere sempre inutili
                int c = 0;
                while ((lineD = br.readLine()) != null) {
                    c++;
                    //c indica la riga da leggere, se è uguale a 9 indica che contiene i parametri che ci interessano
                    if (c == 9) {
                        System.out.println(lineD);
                        ClientHandler.post(new Login.ClientHandler(lineD));
                    }
                }
                br.close();
                bw.close();
                socket.close();
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


        }
    }

    class ClientHandler implements Runnable{
        String ricevo;

        public ClientHandler(String ricevo){
            this.ricevo = ricevo;
        }

        @Override
        public void run() {

            switch(ricevo) {
                case "1":
                    System.out.println("Login effettuato con successo");
                    //Mettere intent
                    System.out.print(logged);
                    /*
                    ***Settare variabili per keep me logged
                     */
                    break;
                case "2":
                    password.setError("Errore nel login");
                    break;

            }

        }


    }

}
