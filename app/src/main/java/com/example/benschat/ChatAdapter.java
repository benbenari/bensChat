package com.example.benschat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

  private Context context;
  private List<ChatMessage> messages;
  private String currentUser;
  private List<User> userList;

  public ChatAdapter(Context context, List<ChatMessage> messages, String currentUser, List<User> userList) {
    this.context = context;
    this.messages = messages;
    this.currentUser = currentUser;
    this.userList = userList;
  }

  public void setMessages(List<ChatMessage> messages) {
    this.messages = messages;
    notifyDataSetChanged();
  }

  public void addMessage(ChatMessage message) {
    messages.add(message);
    notifyItemInserted(messages.size() - 1);
  }

  @NonNull
  @Override
  public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view;
    if (viewType == ViewType.OWN_MESSAGE) {
      view = LayoutInflater.from(context).inflate(R.layout.item_chat_own, parent, false);
    } else {
      view = LayoutInflater.from(context).inflate(R.layout.item_chat_other, parent, false);
    }
    return new ChatViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
    ChatMessage message = messages.get(position);

    User sender = getUserById(message.getUserId());
    if (sender != null) {
      holder.textViewSenderName.setText(sender.getDisplayName());
      Glide.with(context)
          .load(sender.getPhotoUrl())
          .apply(RequestOptions.bitmapTransform(new CircleCrop()))
          .placeholder(R.drawable.default_avatar)
          .into(holder.imageViewSenderPhoto);
    }

    holder.textViewMessage.setText(message.getMessage());

    if (holder.getItemViewType() == ViewType.OWN_MESSAGE) {
      // Apply styling for own messages
      holder.itemView.setBackgroundResource(R.drawable.bg_own_message);
    } else {
      // Apply styling for other messages
      holder.itemView.setBackgroundResource(R.drawable.bg_other_message);
    }
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }

  @Override
  public int getItemViewType(int position) {
    ChatMessage message = messages.get(position);
    if (message.getUserId().equals(currentUser)) {
      return ViewType.OWN_MESSAGE;
    } else {
      return ViewType.OTHER_MESSAGE;
    }
  }

  static class ChatViewHolder extends RecyclerView.ViewHolder {
    TextView textViewSenderName;
    ImageView imageViewSenderPhoto;
    TextView textViewMessage;

    ChatViewHolder(View itemView) {
      super(itemView);
      textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
      imageViewSenderPhoto = itemView.findViewById(R.id.imageViewSenderPhoto);
      textViewMessage = itemView.findViewById(R.id.textViewMessage);
    }
  }

  private User getUserById(String userId) {
    for (User user : userList) {
      if (user.getUserId().equals(userId)) {
        return user;
      }
    }
    return null;
  }

  private static class ViewType {
    static final int OWN_MESSAGE = 0;
    static final int OTHER_MESSAGE = 1;
  }
}
