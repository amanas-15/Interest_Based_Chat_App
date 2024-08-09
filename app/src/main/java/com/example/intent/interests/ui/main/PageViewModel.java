package com.example.intent.interests.ui.main;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.intent.interests.model.Interest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class PageViewModel extends ViewModel {

    private final MutableLiveData<HashMap<String, Object>> mMap = new MutableLiveData<>();
    private static final String FILE_EXTENSION = ".txt";
    private static final String IMAGE_FILE_EXTENSION = "_images.txt";

    private final LiveData<ArrayList<Interest>> mInterestList = Transformations.switchMap(mMap, inputMap -> {
        ArrayList<Interest> interestsList = new ArrayList<>();
        Context context = (Context) inputMap.get("context");
        String input = (String) inputMap.get("type");
        if (context == null || input == null) {
            return new MutableLiveData<>(interestsList);
        }

        try (BufferedReader br1 = new BufferedReader(new InputStreamReader(context.getAssets().open(input + FILE_EXTENSION)));
             BufferedReader br2 = new BufferedReader(new InputStreamReader(context.getAssets().open(input + IMAGE_FILE_EXTENSION)))) {

            String hobby;
            String hobbyImage;
            while ((hobby = br1.readLine()) != null) {
                hobbyImage = br2.readLine();
                Interest currentInterest = new Interest(hobbyImage, hobby, input);
                interestsList.add(currentInterest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MutableLiveData<>(interestsList);
    });

    public LiveData<ArrayList<Interest>> getmInterestList() {
        return mInterestList;
    }

    public void setmMap(HashMap<String, Object> hashMap) {
        mMap.setValue(hashMap);
    }

    public MutableLiveData<HashMap<String, Object>> getmMap() {
        return mMap;
    }
}
