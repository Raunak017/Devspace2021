package com.panshul.devspace.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.panshul.devspace.Activity.Profile_Page;
import com.panshul.devspace.Model.TaskModel;
import com.panshul.devspace.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PomodoroFragment extends Fragment {

    View view;
    ImageView cancel,profile;
    TextView timer,state,name;
    CountDownTimer countDownTimer;
    private int quiztime=12000;
    private int breaktime =9000;
    DatabaseReference myref;
    public int points;
    public boolean isCompleted;
    List<TaskModel> taskList;
    SharedPreferences pref;
    String taskId,uid;
    int sessions;
    int sess;
    int time,index;
    SharedPreferences.Editor editor,editor1;
    long timeRemaining;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_pomodoro, container, false);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        myref = db.getReference("Users");
        isCompleted=false;

        SharedPreferences pref1 = view.getContext().getSharedPreferences("com.panshul.devspace.userdata",Context.MODE_PRIVATE);
        editor1 = pref1.edit();
        points = Integer.valueOf(pref1.getString("points","0"));
        uid = pref1.getString("uid","");
        Log.i("uid",uid);
        loadData();
        cancel = view.findViewById(R.id.clockCancelImageview);
        timer = view.findViewById(R.id.timerTextView);
        state = view.findViewById(R.id.timerTextView2);
        name=view.findViewById(R.id.textView3);
        profile = view.findViewById(R.id.profile4);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), Profile_Page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCompleted){

                }
                else {
                    points = points - 1;
                    editor1.putString("points", String.valueOf(points));
                    editor1.apply();
                    myref.child(uid).child("points").setValue(points);
                    countDownTimer.cancel();
                }
                ClockFragment fragment = new ClockFragment();
                FragmentManager manager = ((FragmentActivity) v.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.frameLayout, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        pref= view.getContext().getSharedPreferences("com.panshul.devspace.clock", Context.MODE_PRIVATE);
        taskId = pref.getString("taskId","");
        for (int i=0;i<taskList.size();i++){
            Log.i("taskIds",taskList.get(i).getTaskId());
            if (taskId.equals(taskList.get(i).getTaskId())){
                Log.i("Index",String.valueOf(i));
                index=i;
                name.setText(taskList.get(i).getTaskName().toString());
                time = taskList.get(i).getTime();
                sessions = time/25;
                sess=sessions;
                break;
            }
        }

        startTimer();
        return view;
    }
    public void loadData(){
        SharedPreferences preferences = view.getContext().getSharedPreferences("com.panshul.devspace.tasklist",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("taskList","");
        Type type = new TypeToken<ArrayList<TaskModel>>() {}.getType();
        taskList =gson.fromJson(json,type);
        if(taskList==null){
            taskList =new ArrayList<>();
        }
    }
    public void startTimer(){
        countDownTimer=new CountDownTimer(quiztime,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTextView(millisUntilFinished/1000);
                state.setText("Task Time");
            }

            @Override
            public void onFinish() {
                startTimerBreak();
                state.setText("Break Time");
            }
        }.start();
    }
    public void startTimerBreak(){
        countDownTimer=new CountDownTimer(breaktime,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTextView(millisUntilFinished/1000);
                timeRemaining=millisUntilFinished;
            }

            @Override
            public void onFinish() {
                sessions=sessions-1;
                if (sessions==0){
                    isCompleted=true;
                    points=points+1;
                    myref.child(uid).child("points").setValue(String.valueOf(points));
                    Log.i("Session End Point",String.valueOf(points));
                    editor1.putString("points",String.valueOf(points));
                    editor1.apply();

                    Toast.makeText(view.getContext(), "Task Completed", Toast.LENGTH_SHORT).show();
                    taskList.get(index).setIsCompleted("true");

                    saveData();

                    ClockFragment fragment = new ClockFragment();
                    FragmentManager manager = ((FragmentActivity) view.getContext()).getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.frameLayout, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();


                }
                else {
                    isCompleted=true;
                    points=points+1;
                    myref.child(uid).child("points").setValue(String.valueOf(points));
                    taskList.get(index).setIsCompleted("true");
                    Log.i("Session Mid Point",String.valueOf(points));
                    editor1.putString("points",String.valueOf(points));
                    editor1.apply();
                    Log.i("Points1",String.valueOf(points));
                    Toast.makeText(view.getContext(), "Next Session started", Toast.LENGTH_SHORT).show();
                    startTimer();
                }

            }
        }.start();
    }
    public void saveData(){
        SharedPreferences preferences = view.getContext().getSharedPreferences("com.panshul.devspace.tasklist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("taskList",json);
        editor.apply();
    }
    void updateTextView(long secondsLeft){
        int min = (int) (secondsLeft/60);
        int seconds = (int) (secondsLeft-(min*60));
        String secondString;
        secondString = Integer.toString(seconds);
        if(seconds<=9){
            secondString="0"+secondString;
        }
        if(min>=1) {
           // Log.i("timer",Integer.toString(min) + ":" + secondString);
            timer.setText(Integer.toString(min) + ":" + secondString);
        }
        else{
            timer.setText("00"+":"+secondString);
            //Log.i("timer2","00"+":"+secondString);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.i("Pause","Pomodoro Fragment");
        if (isCompleted){

        }
        else {
            points = points - 1;
            editor1.putString("points", String.valueOf(points));
            editor1.apply();
            myref.child(uid).child("points").setValue(points);
            //Log.i("points",String.valueOf(points));
            countDownTimer.cancel();
        }
    }
}