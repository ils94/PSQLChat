# About

PSQLChat is a straightforward Android application that utilizes a PostgreSQL database as the backend with a simple table schema to store and retrieve messages.

---

# Idea

The idea behind this project is to provide users with an easy way to set up a backend and have their own chat app, where they have total control. In a world where the most popular messaging apps are controlled by big companies, this app aims to introduce a decentralized approach for people to communicate with each other through the internet.

---

# Structure

The app runs in loops to check for new messages (local threads created) and performs basic select and insert operations into the database.

No web server is necessary; just create a PostgreSQL database, host it somewhere, and provide users with the database credentials. This leads to an insecure database, but the entire idea of this project is to make everything easier to set up! If you are more advanced, you can create an user that has SELECT and INSERT permissions only.

---

# Features

- Send and store messages in a database.
- Retrieve messages from the database and display them.
- Easily send images using the Imgur API.
- Open Imgur images within the app.
- Save your own Imgur API Key.
- Pause/Resume Chat loop to see all chat messages.
- Show all database messages and allow searching within the database.
- Turn on notifications and receive the last 10 messages sent to the database (may vary depending on the phone).
- Share the database link for easier login.
- Generate a QR Code for an easier and secure way to share database credentials.
- Save multiple databases to quickly switch between them.
- Edit saved databases.
- Adjust font size.
- Message Encryptation and Decryptation using AES-256.
- Able to setup a password to lock the app's chat.
- Filter out the Decrypt errors from the chat (in case you are in a database where others have different encrypt keys).

---

# How to setup the database structure

**create a table with this command:**

create table if not exists chat (id serial primary key, user_name varchar(10) not null, user_message varchar not null)

*By default, only the last 1000 rows will be displayed. The reason is that as the database grows, selecting and displaying more rows in the client may decrease performance over time. You can alter how many rows will be selected in your app in the **Menu - Miscs.***

---

# Imgur API Key

To upload directly to https://imgur.com/ and post the image into the database, you need your API Key. Learn how to get your API Key by reading this [documentation](https://apidocs.imgur.com/).

Once you acquire your API Key, paste and save it: **Menu - APIs - Imgur API Key**. Paste or type your API Key in the field, and click **SAVE**.

---
# Message Encryptation/Decryptation

Before inserting a message into the database, you must possess an encryption key. You can generate one when saving a new database, but it is crucial that everyone in the chat has this key. Otherwise, you won't be able to decrypt each other's messages.

This feature provides a basic encryption/decryption mechanism to ensure the security of messages stored in the database, protecting them from unauthorized access.

---

# Download

You can download the App from this [link](https://github.com/ils94/PSQLChat/releases/download/release/PSQLChat.apk).

---

# Free PostgreSQL Hosting Services

If you are looking for a free and easy way to set up a PostgreSQL database, I recommend using [alwaysdata](https://www.alwaysdata.com/en/register/).
