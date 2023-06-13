# BoardGameCollector
## Description
BoardGameCollector (BGC) is an Android application designed to help users manage their board game collections. It integrates with the BoardGameGeek (BGG) service to synchronize game information and provide additional details about the games.

## Features
Synchronize game collection with the user's BGG account
Add new games to the collection
Remove games that are no longer present in the user's BGG account
View game details, including title, original title, release year, BGG identifier, and thumbnail image
## Technical Details
The application is developed using Android Studio.
It utilizes an SQLite database to store game information.
The initial database is empty and gets populated with data during app usage.
The BGG API is used to fetch game information and collection data.
## BGG API
BoardGameGeek provides a public API for accessing board game information.
The API documentation can be found at: BGG XML API2.
The application uses a limited set of API functions, primarily focusing on game search and retrieving game details.
## User Interface
### Main Activity
Upon launching the application, the user is presented with a summary of their account, including username, the number of games owned, the number of expansions owned, and the last synchronization date.
The user can navigate to the game list or the expansion list by selecting the corresponding button.
The "Sync" button triggers the synchronization screen.
The "Clear Data" button allows the user to delete all data and exit the application.
### Game/Expansion List
The list view displays a table-like layout with game/expansion information.
Each entry shows the item number, thumbnail image (or a default image if unavailable), title, and release year.
The list can be sorted alphabetically by title or by release year.
### Game/Expansion Details
Selecting a game/expansion from the list opens the details screen.
The details screen provides information about the selected item, including title, original title, release year, BGG identifier, and thumbnail image.
### Synchronization Screen
The synchronization screen displays the last synchronization date and a button to initiate a new synchronization.
If the time elapsed since the last synchronization is less than 24 hours, the user is prompted for confirmation before starting a new synchronization.
During synchronization, a progress bar or relevant information about the process is displayed.
### Configuration Screen
The configuration screen is shown on the first app launch.
The user is prompted to enter their BGG username and initiate the initial data synchronization, which also initializes the database.
