# About

PSQLChat is a simple Android application that uses PostgreSQL database as backend with a simple table scheme to store and retrieve messages.

---

# Idea

The idea behind this project is to give users a way to easy setup a backend and have a chat app of their own, where they have control of it. We live in a world where the most known message apps are controlled by big companies, and this app tries to add some kinda of decentralized power for people to communicate with each other through the internet.

---

# Structure

The app basically run in Loops to check for new messages  (local threads created), and very basic select and insert into the database.

No webserver is necessary, just create yourself a PostgreSQL database, host it somewhere and provide the users with the database crendentials. This leads to an insecure database, but the whole idea of this project is to make everything easier to setup!

---

# Features

- Send and store messages into a database;
- Retrive messages from the database and display them;
- Easy to send images using Imgur API;
- Imgur images are opened within the app;
- Save your own Imgur API Key;
- Pause/Resume Chat loop to be able to see all chat messages;
- Show all database messages, and let you search messages within the database;
- Turn on notifications and receive the last 10 messages sent to the database (may vary depending on the phone);
- Share database link for easier login into the database;
- Save multiple databases to quickly switch between them;
- Edit saved databases;
- Adjust font size.

---

# How to setup the database structure

**create a table with this command:**

CREATE TABLE IF NOT EXISTS CHAT (ID SERIAL PRIMARY KEY, USER_NAME VARCHAR(10) NOT NULL, USER_MESSAGE VARCHAR NOT NULL)

*By default, only the last 1000 rows will be displayed. The reason is that once the database gets bigger and bigger, more and more rows will be selected and displayed in the client, and thus, it may decrease performance overtime. There is a way to select all messages from the database using the option in the app menu.*

---

# Imgur API Key

To be able to upload direct to https://imgur.com/ and post the image into the database, you need your API Key. To know how to get your own API Key, please, read this [documentation](https://apidocs.imgur.com/).

Once you acquire your API Key, paste and save it: **Menu**, **Login...**, **Imgur API Key**, and click **SAVE**.

Or when you try to upload an image to Imgur without the API Key set, it will prompt you to save your API Key.

---

# Download

You can download the App from this [link](https://github.com/ils94/PSQLChat/releases/download/release/PSQLChat.apk).

---

# Free PostgreSQL Hosting Services

If you are looking for a free and easy way to setup PostgreSQL database, I recommend using [ElephantSQL](https://customer.elephantsql.com/login).

They offer up to 5 created databases for free, and up to 20mb maximum space for free for each database, which would be enough for this project, since PSQLChat only store Strings on the database table.

20mb can store up to 20 millions characters in just one database!
