package fr.indianacroft.wildhunt;

/**
 * Created by apprenti on 9/26/17.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HomeJoueur_PlayerActivity extends Fragment {

    Button butNewChallenge;
    Button butNewChallenge2;
    ImageView imageViewCancel;
    ImageView imageViewCancel2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.homejoueur_playeractivity, container, false);

        return rootView;
    }
}