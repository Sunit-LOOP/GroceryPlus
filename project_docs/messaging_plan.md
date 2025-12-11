# Messaging System Implementation Plan

## Goal
Implement a complete messaging system for user-admin communication in the GroceryPlus app.

## Database Schema

### Messages Table
```sql
CREATE TABLE messages (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender_id INTEGER,
    receiver_id INTEGER,
    message_text TEXT,
    is_read INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(sender_id) REFERENCES users(user_id),
    FOREIGN KEY(receiver_id) REFERENCES users(user_id)
)
```

## Proposed Changes

### Database Layer

#### [NEW] DatabaseContract.java
- Add `MessageEntry` class with column definitions
- Add `SQL_CREATE_MESSAGES_TABLE` statement
- Add `SQL_DELETE_MESSAGES_TABLE` statement

#### [MODIFY] DatabaseHelper.java
- Add messages table creation in `onCreate()`
- Add messages table deletion in `onUpgrade()`
- Add CRUD methods:
  - `sendMessage(senderId, receiverId, messageText)`
  - `getConversation(userId1, userId2)`
  - `getAllConversations(userId)`
  - `markMessageAsRead(messageId)`
  - `getUnreadCount(userId)`

---

### Model Layer

#### [NEW] models/Message.java
- Fields: messageId, senderId, receiverId, messageText, isRead, createdAt
- Constructors, getters, setters
- Helper methods

---

### Repository Layer

#### [NEW] MessageRepository.java
- `sendMessage(senderId, receiverId, text)`
- `getConversation(userId, otherUserId)`
- `getConversations(userId)` - List of chat threads
- `markAsRead(messageId)`
- `getUnreadCount(userId)`

---

### UI Components

#### [NEW] MessageActivity.java
- Display conversation with admin/user
- Send new messages
- Real-time message list
- Input field and send button

#### [NEW] activity_message.xml
- RecyclerView for messages
- EditText for input
- Send button
- Toolbar with recipient name

#### [NEW] item_message_sent.xml
- Layout for sent messages (right-aligned)
- Message bubble, timestamp

#### [NEW] item_message_received.xml
- Layout for received messages (left-aligned)
- Message bubble, timestamp

#### [NEW] adapters/MessageAdapter.java
- Display messages in RecyclerView
- Different view types for sent/received
- Timestamp formatting

---

### Integration Points

#### [MODIFY] UserHomeActivity.java
- Wire up message icon in bottom navigation
- Navigate to MessageActivity with admin user

#### [MODIFY] AndroidManifest.xml
- Register MessageActivity

---

## Implementation Steps

1. ✅ Update DatabaseContract with MessageEntry
2. ✅ Update DatabaseHelper with messages table
3. ✅ Create Message model class
4. ✅ Create MessageRepository
5. ✅ Create MessageAdapter
6. ✅ Create message item layouts
7. ✅ Create MessageActivity
8. ✅ Create activity_message.xml
9. ✅ Update AndroidManifest
10. ✅ Integrate with navigation

## Verification Plan

### Testing
- Send message from user to admin
- Receive message from admin
- View conversation history
- Mark messages as read
- Check unread count

### Manual Verification
- Test chat interface
- Verify message ordering
- Check timestamp display
- Test send functionality
