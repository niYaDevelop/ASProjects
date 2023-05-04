package space.yakimov.firstapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendList extends Fragment {


    private ArrayList<FriendsClass> namesString = new ArrayList<>();
    private ArrayList<String> friendsOnline = new ArrayList<>();
    private FriendExpandableAdapter usersAdapter;
    private ExpandableListView namesView;
    Typeface font;
    private int lastPosition = -1;
    ToggleButton addFriendButton;
    AddFriendFragment addFriendFragment;
    private FriendListFragmentListener friendListFragmentListener;
    private SharedPreferences nSettings;


    public FriendList() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/comic.ttf");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_list,
                container, false);

        //нашли кнопку и повесили листенер
        addFriendButton = view.findViewById(R.id.add_friend_butt);
        addFriendButtonListener();
        //добавили шрифт к слову
        TextView friendText = (TextView) view.findViewById(R.id.friends_word);
        friendText.setTypeface(font, Typeface.NORMAL);

        //инициализация фрагемента
        addFriendFragment = (AddFriendFragment) getChildFragmentManager().findFragmentById(R.id.window_add_friend_fragment);
        //fragment show
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                .hide(addFriendFragment)
                .commit();

        loadSavedFriendList();

        usersAdapter = new FriendExpandableAdapter(getActivity(), namesString, font);
        namesView = view.findViewById(R.id.friends_list_view);
        namesView.setAdapter(usersAdapter);


        namesView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {


            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastPosition != -1
                        && groupPosition != lastPosition) {
                    namesView.collapseGroup(lastPosition);
                }
                hideKeyboard();
                if(addFriendButton.isChecked()) {
                    addFriendButton.setChecked(false);
                }
                lastPosition = groupPosition;
            }
        });

        return view;
    }

//    public void setAddButtonChecked(boolean state){
//        addFriendButton.setChecked(state);
//    }

    private void loadSavedFriendList(){
        //Прочитали сохраненных друзей
        try {
            nSettings = getActivity().getSharedPreferences("ssettings", Context.MODE_PRIVATE);
            Set<String> set = nSettings.getStringSet("friendList", null);
            ArrayList<String> arr = new ArrayList<>(set);
            for(String line : arr){
                FriendsClass fc = new FriendsClass(line, false);
                namesString.add(fc);
            }
        }catch (Exception e){}
    }

    private void saveFriendList(){
        try {
            Set<String> set = new HashSet<String>();
            ArrayList<String> arr = new ArrayList<>();
            for(int i = 0; i < namesString.size(); i++){
                arr.add(namesString.get(i).getName());
            }
            set.addAll(arr);
            SharedPreferences.Editor editor = nSettings.edit();
            editor.putStringSet("friendList", set);
            editor.commit();
        }catch(Exception e){}
    }


    private void addFriendButtonListener(){
        addFriendButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        //fragment show
                        FragmentManager fm = getChildFragmentManager();
                        fm.beginTransaction()
                                .setCustomAnimations(R.anim.add_friend_frag_in, R.anim.add_friend_frag_out)
                                .show(addFriendFragment)
                                .commit();
                    } else {
                        //fragment hide
                        hideAddFriendFragment();
                        //Убрали клаву
                        hideKeyboard();
                    }
                    namesView.collapseGroup(lastPosition);
                }catch (Exception e){}
            }
        });
    }

    public boolean checkFriendContain(String name){
        return namesString.contains(name);
    }

    public void hideAddFriendFragment(){
        //fragment hide
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                .hide(addFriendFragment)
                .commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FriendListFragmentListener) {
            friendListFragmentListener = (FriendListFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }
    
    public String getUserName(){
        return friendListFragmentListener.onGetUserName();
    }

    public void hideKeyboard(){
        friendListFragmentListener.onAddFragmentClosedHideKeyboard();
    }

    public void checkFriendName(String friendName){
        //запросить сервер о сущестоввании имени
        friendListFragmentListener.onCheckFriendName(friendName);
    }

    public void setOnlineFriends(ArrayList<String> onlineList){
        //почистили список онлайна
        friendsOnline.clear();
        //обновили список онлайна
        for(int i = 0; i < namesString.size(); i++){
            if(onlineList.contains(namesString.get(i).getName())){
                namesString.get(i).setOnline(true);
            }
            else{
                namesString.get(i).setOnline(false);
            }
        }
        //Обновили адаптер
        usersAdapter.updateList(namesString);
    }

    public void addFriend(String name){
        FriendsClass fc = new FriendsClass(name, false);
        namesString.add(fc);
        Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                "Игрок " + name + " добавлен в список друзей", Toast.LENGTH_SHORT);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(20);
        messageTextView.setTypeface(font);
        toast.show();
        usersAdapter.updateList(namesString);
        saveFriendList();
    }

    public void fakeName(String name){
        Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                "Игрока " + name + " не существует!", Toast.LENGTH_SHORT);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(20);
        messageTextView.setTypeface(font);
        toast.show();
    }

    public void inviteFriend (int pos){
        //Если игрок оффлайн, выводим сообщение
        if(!namesString.get(pos).isOnline()) {
            Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                    namesString.get(pos).getName() + " сейчас оффлайн", Toast.LENGTH_SHORT);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(20);
            messageTextView.setTypeface(font);
            toast.show();
        }
        //Если онлайн, приглашаем
        else{
            friendListFragmentListener.emitInvite(namesString.get(pos).getName());
        }
    }

    public void deleteFriend(int pos){
        Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                "Игрок " + namesString.get(pos).getName() + " удален из списка друзей", Toast.LENGTH_SHORT);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(20);
        messageTextView.setTypeface(font);
        toast.show();
        namesString.remove(pos);
        usersAdapter.updateList(namesString);
        saveFriendList();
    }

    interface FriendListFragmentListener{
        void emitInvite(String name);
        void onCheckFriendName(String name);
        void onAddFragmentClosedHideKeyboard();
        String onGetUserName();
    }
}