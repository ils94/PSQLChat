# About

PSQLChat is a straightforward Android application that utilizes a PostgreSQL database as the backend with a simple table schema to store and retrieve messages.

---

# Idea

The idea behind this project is to provide users with an easy way to set up a backend and have their own chat app, where they have total control. In a world where the most popular messaging apps are controlled by big companies, this app aims to introduce a decentralized approach for people to communicate with each other through the internet.

---

# Structure

The app runs in loops to check for new messages (local threads created) and performs basic select and insert operations into the database.

No web server is necessary; just create a PostgreSQL database, host it somewhere, and provide users with the database credentials. This leads to an insecure database, but the entire idea of this project is to make everything easier to set up!

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
- Save multiple databases to quickly switch between them.
- Edit saved databases.
- Adjust font size.

---

# How to setup the database structure

**create a table with this command:**

create table if not exists chat (id serial primary key, user_name varchar(10) not null, user_message varchar not null)

*By default, only the last 1000 rows will be displayed. The reason is that as the database grows, selecting and displaying more rows in the client may decrease performance over time. There is a way to select all messages from the database using the option in the app menu.*

---

# Imgur API Key

To upload directly to https://imgur.com/ and post the image into the database, you need your API Key. Learn how to get your API Key by reading this [documentation](https://apidocs.imgur.com/).

Once you acquire your API Key, paste and save it: **Menu - APIs - Imgur API Key**. Paste or type your API Key in the field, and click **SAVE**.

---

# Download

You can download the App from this [link](https://github.com/ils94/PSQLChat/releases/download/release/PSQLChat.apk).

---

# Free PostgreSQL Hosting Services

If you are looking for a free and easy way to set up a PostgreSQL database, I recommend using [ElephantSQL](https://customer.elephantsql.com/login).

They offer up to 5 created databases for free, with a maximum space of up to 20MB for each database, which is sufficient for this project since PSQLChat only stores strings in the database table.

20MB can store up to 20 million characters in just one database!
