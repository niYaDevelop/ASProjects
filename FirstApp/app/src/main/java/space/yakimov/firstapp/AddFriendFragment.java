package space.yakimov.firstapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AddFriendFragment extends Fragment {

    private Button buttonAdd;
    private Button buttonShare;
    private TextInputLayout inpLay;
    private TextInputEditText editText;
    Typeface font;
    private FriendList parentFrag;

    public AddFriendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        buttonShare.setEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_friend,
                container, false);

        //родительский фрагмент
        parentFrag = ((FriendList)AddFriendFragment.this.getParentFragment());

        font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/comic.ttf");
        buttonAdd = (Button) view.findViewById(R.id.add_friend_name_button);
        buttonAddListener();

        buttonShare = (Button) view.findViewById(R.id.share_button);
        buttonShareListener();

        editText = (TextInputEditText) view.findViewById(R.id.add_friend_edit_text);
        inpLay = (TextInputLayout) view.findViewById(R.id.add_friend_input_layout);
        editText.setTypeface(font);

        return view;
    }

    public final static boolean isValidNickName(String target) {

        //Длина никнейма 2-10 символов
        //Разрешено использовать латаницу, кириллицу, цифры, символы ".", "_", "-"
        //Символы  ".", "_", "-" не должны быть в начале или конце никнейма

        return Pattern.compile("^[a-zа-яА-ЯA-Z0-9]([._-](?![._-])|[a-zа-яА-ЯA-Z0-9]){0,8}[a-zа-яА-ЯA-Z0-9]$").matcher(target).matches();
    }


    private void buttonShareListener(){
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareIntentToFriend();
                parentFrag.hideKeyboard();
                //выключили, вернем в onResume
                buttonShare.setEnabled(false);
                //hideKeyboard();
            }
        });
    }

    private void shareIntentToFriend(){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Игры Разума - поиск слов [Онлайн]");
            String shareMessage= "\nЗаходи в игру! Здесь можно играть вдвоем. Мой никнейм: " + parentFrag.getUserName() + "\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Куда отправим?"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    private void buttonAddListener(){
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //получили имя из строки
                String nameAdd = editText.getText().toString();

                if(nameAdd.equals(parentFrag.getUserName())){                           //Если ввёл своё имя
                    try{
                        Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                                "Свой никнейм нельзя!", Toast.LENGTH_SHORT);
                        ViewGroup group = (ViewGroup) toast.getView();
                        TextView messageTextView = (TextView) group.getChildAt(0);
                        messageTextView.setTextSize(20);
                        messageTextView.setTypeface(font);
                        toast.show();
                    }catch (Exception e){
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Свой никнейм нельзя!", Toast.LENGTH_SHORT).show();
                    }
                }   else if(parentFrag.checkFriendContain(nameAdd)) {                   //Если ввёл уже существующего друга
                    try{
                        Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                                nameAdd + " уже в списке!", Toast.LENGTH_SHORT);
                        ViewGroup group = (ViewGroup) toast.getView();
                        TextView messageTextView = (TextView) group.getChildAt(0);
                        messageTextView.setTextSize(20);
                        messageTextView.setTypeface(font);
                        toast.show();
                    }catch (Exception e){
                        Toast.makeText(getActivity().getApplicationContext(),
                                nameAdd + " уже в списке!", Toast.LENGTH_SHORT).show();
                    }
                }   else if(!isValidNickName(nameAdd)){                                 //Некорректно ввёл
                    try{
                        Toast toast =Toast.makeText(getActivity().getApplicationContext(),
                                "Игрока " + nameAdd + " не существует!", Toast.LENGTH_SHORT);
                        ViewGroup group = (ViewGroup) toast.getView();
                        TextView messageTextView = (TextView) group.getChildAt(0);
                        messageTextView.setTextSize(20);
                        messageTextView.setTypeface(font);
                        toast.show();
                    }catch (Exception e){
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Игрока " + nameAdd + " не существует!", Toast.LENGTH_SHORT).show();
                    }
                }   else{                                                               //Если ввёл не своё имя и в списке еще нет и ввёл корректно
                    //Вызвать метод проверки на сервере из активити
                    parentFrag.checkFriendName(nameAdd);
                }
                editText.setText("");
                parentFrag.hideKeyboard();
            }
        });
    }
}