package space.yakimov.firstapp;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FriendExpandableAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<FriendsClass> mGroups;
    private Typeface font;

    public FriendExpandableAdapter (Context context, ArrayList<FriendsClass> groups, Typeface font){
        mContext = context;
        mGroups = groups;
        this.font = font;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    public void updateList(ArrayList<FriendsClass> groups){
        mGroups = groups;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public FriendsClass getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
//        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.names, null);
            String name = getGroup(groupPosition).getName();

            TextView friendName = (TextView) convertView.findViewById(R.id.name_adapter);
            friendName.setTypeface(font, Typeface.NORMAL);
            friendName.setText(name);
//        }
        if(groupPosition == 0){
            View divider = convertView.findViewById(R.id.divider_header);
            divider.setVisibility(View.GONE);
        }

        if(getGroup(groupPosition).isOnline()){
            convertView.setAlpha(1);
        }else{
            convertView.setAlpha(0.5f);
        }
        if (isExpanded){
            //Изменяем что-нибудь, если текущая Group раскрыта
        }
        else{
            //Изменяем что-нибудь, если текущая Group скрыта
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.friend_list_item_layout, null);
        }

        //Если друг онлайн, альфа = 1
        if(getGroup(groupPosition).isOnline()){
            convertView.setAlpha(1);
        }else{
            convertView.setAlpha(0.5f);
        }


        Button buttInvite = (Button) convertView.findViewById(R.id.butt_friend_invite);
        buttInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mContext instanceof FriendListButtonsListener) {
                    ((FriendListButtonsListener) mContext).inviteToGame(groupPosition);
                }
            }
        });


        Button buttDelete = (Button) convertView.findViewById(R.id.butt_freind_delete);
        buttDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mContext instanceof FriendListButtonsListener) {
                    ((FriendListButtonsListener) mContext).deleteFriend(groupPosition);
                }
            }
        });

//
//        View dividerView = (View) convertView.findViewById(R.id.divider_child);
        if(groupPosition == getGroupCount() - 1 ){
            //Если последняя группа
            //Убрать дивайдер
//            dividerView.setVisibility(View.GONE);
        }
        else{
//            dividerView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public interface FriendListButtonsListener {
        void deleteFriend(int pos);
        void inviteToGame(int pos);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
