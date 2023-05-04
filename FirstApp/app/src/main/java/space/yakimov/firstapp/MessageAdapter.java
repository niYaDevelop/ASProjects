package space.yakimov.firstapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.text.format.DateFormat;

import com.github.library.bubbleview.BubbleTextView;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {

    Typeface font;

    public MessageAdapter(Context context, ArrayList<Message> messageList,  Typeface font) {
        super(context, R.layout.message, messageList);
        this.font = font;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.message, null);
        }
        TextView messUser =  convertView.findViewById(R.id.message_user);
        messUser.setTypeface(font, Typeface.NORMAL);
        messUser.setText(message.getMessageUser());

        BubbleTextView messMessage =  convertView.findViewById(R.id.message_text);
        messMessage.setTypeface(font, Typeface.NORMAL);
        messMessage.setText(message.getMessageText());

        TextView messTime =  convertView.findViewById(R.id.message_time);
        messTime.setTypeface(font, Typeface.NORMAL);
        messTime.setText(DateFormat.format("HH:mm:ss", message.getMessageTime()));


//        ((TextView) convertView.findViewById(R.id.message_user))
//                .setText(message.getMessageUser());
//        ((TextView) convertView.findViewById(R.id.message_time))
//                .setText(DateFormat.format("HH:mm:ss", message.getMessageTime()));
//        ((BubbleTextView) convertView.findViewById(R.id.message_text))
//                .setText(message.getMessageText());
        return convertView;
    }
}