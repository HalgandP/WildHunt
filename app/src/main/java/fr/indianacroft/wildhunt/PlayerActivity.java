package fr.indianacroft.wildhunt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import static java.security.AccessController.getContext;

public class PlayerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button butNewChallenge;
    Button butNewChallenge2;
    ImageView imageViewCancel;
    ImageView imageViewCancel2;
    private String mUserId;
    private String mUser_name;
    private String mUser_quest;
    private String mQuest_name;
    private String mUser_indice;
    private String mQuest_description;
    private String mLife_duration;
    private String mName_challenge;
    private String mDiff_challenge;
    private String mHint_challenge;
    private String mKey_challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        final Button buttonHint = (Button) findViewById(R.id.buttonHomeJoueurHint);
        final TextView textViewPlayerActivityHint = (TextView) findViewById(R.id.textViewPlayerActivityHint);

        // Pour recuperer la key d'un user (pour le lier a une quête)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserId = sharedPreferences.getString("mUserId", mUserId);
        Log.d("key", mUserId);
        /////////////////////////////////////////////////////////////////





        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Drawer Menu
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Avatar
        ImageView imageViewAvatar = (ImageView) findViewById(R.id.imageViewAvatar);
        imageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        // POUR CHANGER L'AVATAR SUR LA PAGE AVEC CELUI CHOISI
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Avatar").child(mUserId);
        // Load the image using Glide
        if (storageReference.getDownloadUrl().isSuccessful()){
            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageViewAvatar);
        }




        // On appele les methodes declarées plus bas (pour chercher l'user, la quete, les challenges)
        searchUser();

        Button buttonSendSolution = (Button) findViewById(R.id.buttonHomeJoueurSendSolution);

        buttonSendSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mUser_quest.equals("Pas de qûete pour l'instant")) {
                    Intent intent = new Intent(getApplicationContext(), HomeJoueur_PlayerPopUp.class);
                    intent.putExtra("mChallengeKey", mKey_challenge); //On envoie l'ID du challenge
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.error_noquest, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Drawer Menu
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        // TODO : remplacer les toasts par des liens ET faire en sorte qu'on arrive sur les pages de fragments
        if (id == R.id.nav_rules) {
            Intent intent = new Intent(getApplicationContext(), RulesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_play) {
            Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_create) {
            startActivity(new Intent(getApplicationContext(), HomeGameMasterActivity.class));
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(getApplicationContext(), HomeGameMasterActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_delete) {
            startActivity(new Intent(getApplicationContext(), ConnexionActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // METHODE POUR TROUVER USER
    private void searchUser() {
        final Button buttonHint = (Button) findViewById(R.id.buttonHomeJoueurHint);
        final TextView textViewPlayerActivityHint = (TextView) findViewById(R.id.textViewPlayerActivityHint);

        // On recupere toutes les données de l'user actuel
        final DatabaseReference refUser =
                FirebaseDatabase.getInstance().getReference().child("User").child(mUserId);
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUser_name = user.getUser_name();
                mUser_quest = user.getUser_quest();
                mUser_indice = user.getUser_indice();
                Log.d("indice", mUser_indice);

                // Indice a montrer si indice déja utilisé c'est a dire True dans la bdd
                if (mUser_indice.equalsIgnoreCase("true")) {
                    textViewPlayerActivityHint.setVisibility(View.VISIBLE);
                    buttonHint.setVisibility(View.GONE);
                } else {
                    textViewPlayerActivityHint.setVisibility(View.GONE);
                }
                searchQuest();

                // Indice au clic
                // TODO enlever les points au clic de l'indice
                buttonHint.setOnClickListener(new View.OnClickListener() {
                    boolean isClicked = false;

                    @Override
                    public void onClick(View view) {
                        //si l'indice est déclaré false dans la bdd cest qu'il n'a jamais été utilisé
                        if (mUser_indice.equalsIgnoreCase("false")) {
                            if (!isClicked) {
                                isClicked = true;
                                Toast.makeText(getApplicationContext(), R.string.warning_hint, Toast.LENGTH_SHORT).show();
                            } else if (isClicked) {
                                textViewPlayerActivityHint.setVisibility(View.VISIBLE);
                                buttonHint.setBackgroundColor(Color.RED);
                                refUser.child("user_indice").setValue("true");
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }


    // METHODE POUR TROUVER QUETE
    private void searchQuest() {
        //On recupere toutes les données de la quete de l'user
        final DatabaseReference refUserQuest = FirebaseDatabase.getInstance().getReference().child("Quest");
        refUserQuest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Quest quest = dsp.getValue(Quest.class);
                    // On recupere la qûete liée a un user
                    if (mUser_quest.equals(dsp.getKey())) {

                        mQuest_name = quest.getQuest_name();
                        Log.d(mQuest_name, "quest");
                        mQuest_description = quest.getQuest_description();
                        mLife_duration = quest.getLife_duration();

                        final TextView playerActivityQuestName = (TextView) findViewById(R.id.playerActivityNameQuestTitle);
                        playerActivityQuestName.setText(mQuest_name);
                        searcChallenges();
                        return;
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // METHODE POUR TROUVER CHALLENGE
    private void searcChallenges() {
        final TextView textViewPlayerActivityHint = (TextView) findViewById(R.id.textViewPlayerActivityHint);

        final Button playerActivityNumChallenge = (Button) findViewById(R.id.playerActivityNumChallenge);

        // On recupere les données des challenges
        DatabaseReference refUserChallenge = FirebaseDatabase.getInstance().getReference().child("Challenge");
        refUserChallenge.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Challenge challenge = dsp.getValue(Challenge.class);
                    // On recupere les challenges qui correspondent a la qûete
                    if (challenge.getChallenge_questId().equals(mUser_quest)) {
                        mKey_challenge = dsp.getKey();
                        mName_challenge = challenge.getChallenge_name();
                        Log.d(mName_challenge, "tag");
                        mHint_challenge = challenge.getHint_challenge();
                        mDiff_challenge = challenge.getChallenge_difficulty();


                        // On change la page dynamiquement !!


                        // Reference to an image file in Firebase Storage
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Quest").child(mUser_quest).child(mKey_challenge);
                        // ImageView in your Activity
                        final ImageView imageViewPhotoChallenge = (ImageView) findViewById(R.id.imageViewHomeJoueurToFind);
                        // Load the image using Glide
                        Glide.with(getApplicationContext())
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imageViewPhotoChallenge);



                        textViewPlayerActivityHint.setText(mHint_challenge);
                        playerActivityNumChallenge.setText(mName_challenge);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}