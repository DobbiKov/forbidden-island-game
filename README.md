# Forbidden Island - Java Swing Implementation

A graphical desktop application implementing the cooperative board game "Forbidden Island" using Java Swing. This project follows a classic Model-View-Controller (MVC) architecture to separate concerns.

## Project Overview

This project provides a playable version of the Forbidden Island board game with a graphical user interface built using Java Swing. It encompasses the core game rules, player actions, island state, and winning/losing conditions.

## Authors
- Yehor KOROTENKO [yehor.korotenko@etu-upsaclay.fr]( mailto:yehor.korotenko@etu-upsaclay.fr )
- Ivan KHARKOV [ivan.kharkov@etu-upsaclay.fr](mailto:ivan.kharkov@etu-upsaclay.fr])

## Features

*   **Full Game Simulation:** Play through a complete game of Forbidden Island.
*   **Variable Player Count:** Supports 2 to 4 players.
*   **Player Roles:** Each player is assigned a unique role (Pilot, Engineer, Diver, Messenger, Navigator, Explorer) with its specific abilities correctly implemented.
*   **Island Board:** Dynamically generated board layout with 24 unique zones (tiles).
*   **Zone States:** Zones can be Normal, Flooded, or Inaccessible (sunk).
*   **Player Actions:** Core actions are available:
    *   Move to adjacent or special zones (depending on role).
    *   Shore Up (drain) adjacent or current flooded zones.
    *   Give Treasure Cards to other players (Messenger role allows giving to distant players).
    *   Use special Action Cards (Helicopter Lift, Sandbags).
    *   Take Artefacts when conditions are met (standing on an Artefact Zone with 4 matching Treasure Cards).
    *   Discard excess cards.
    *   Run from Inaccessible Zones (forced movement when a tile sinks with players on it).
*   **Deck Management:** Implements Treasure Deck and Flood Deck mechanics, including drawing, discarding, reshuffling, and Water Rise card effects simulation a real card deck.
*   **Water Meter:** Tracks the rising water level and its impact on the flood rate.
*   **Win/Loss Conditions:** Checks for victory (all artefacts collected, all players on Fools' Landing, Helicopter Lift card played) and various defeat conditions (water meter max, island tiles sinking, artefacts lost, players stranded).
*   **Graphical User Interface:** Interactive Swing GUI displaying the board, zone states, player positions, player hands, available actions, water meter level, and collected artefacts.
*   **Error Handling:** Provides informative error and information dialogs for invalid actions or game events (like Water Rise).

## Game Components Represented

*   **Model:**
    *   `BoardGame`: Manages the overall game state, board, players, decks, and turn progression. Contains the core game logic and rules.
    *   `Zone`: Represents a tile on the island, tracking its state (Normal, Flooded, Inaccessible) and players on it. Subtypes like `ArtefactZone`, `PlayerStartZone`, `HelicopterZone` define special tiles.
    *   `Player`: Represents a player, tracking their role, position, hand of cards, collected artefacts, and remaining actions.
    *   `Deck` (`TreasureDeck`, `FloodDeck`): Manages drawing, discarding, and shuffling cards.
    *   `Card` (`CardType`): Represents cards from the decks (Treasures, Actions, Water Rise).
    *   `WaterMeter`: Tracks the flood level and rate.
    *   `PlayerRole`, `Artefact`, `ZoneState`, `ZoneType`, `CardType`: Enums defining game elements.
    *   Custom Exceptions: Define various error conditions specific to the game logic.
*   **View (Swing):**
    *   `GUI`: The main JFrame, orchestrating the display of the game board and player panels. Implements the `GameView` interface.
    *   `FilteredImagePanel`: Custom JPanel for displaying individual zone tiles with background images, state overlays (flooded/inaccessible), player pawns, and artefacts. Handles user clicks on zones.
    *   `PlayerPanel`: Custom JPanel for displaying player information, their hand of cards, collected artefacts, and action buttons. Handles user clicks on player actions or cards.
    *   `ResourceMapper`: Helper class for loading and caching image resources used by the GUI.
    *   `AddPlayerDialog`, `ErrorDialog`, `InfoDialog`: Custom JDialogs for user interaction and feedback.
    *   `PawnPanel`: Custom JPanel for drawing player pawns on zone tiles.
    *   `WrapLayout`: Custom LayoutManager for wrapping components (used in PlayerPanel for buttons and cards).
*   **Controller:**
    *   `GameController`: Connects the View and Model. Receives user input from the View (button clicks, zone clicks, card clicks), translates them into game actions by calling methods in the Model, and tells the View to update its display based on the new Model state.
    *   `modelActionHandler`: A static utility class that wraps calls to Model methods, providing centralized exception handling and displaying appropriate messages via the View's dialog methods.
*   **Helper:**
    *   `AddPlayerCallback`, `ChoosablePlayerCallback`: Functional interfaces used for passing callbacks from dialogs and choosable panels back to the Controller.

## Getting Started

Requires Java 8 or higher.

1.  **Clone the Repository:**
    ```bash
    git clone https://gitlab.dsi.universite-paris-saclay.fr/yehor.korotenko/l-ile-interdite
    cd l-ile-interdite
    ```
2.  **Resource Location:** Ensure the image resources (like those in `src/main/resources/roles_images`, `src/main/resources/island_card_images`, etc. if using a standard Maven/Gradle structure, or directly in the classpath root if not) are accessible. The `ResourceMapper` class relies on `getClass().getResource()` or `ClassLoader.getSystemResource()` to load these images from the classpath.
3.  **Compile:** Compile all Java source files (`.java`) located in the `src` directory.
    ```bash
    javac src/*/*/*.java src/*/*/*/*.java src/*/*/*/*/*.java
    ```
    *(Note: This command might need adjustment depending on the exact depth of your package structure and if you are not using a build tool like Maven or Gradle.)*
4.  **Run:** Execute the compiled `Main` class.
    ```bash
    java Main
    ```
    Most modern Java IDEs (like IntelliJ IDEA, Eclipse, VS Code with Java extensions) can handle steps 2-4 automatically if you open the project root directory.

## How to Play

Upon running the application, you will see a GUI window.

1.  **Add Players:** Click the "Add Player" button to add players. Enter a name for each player (up to 4 players are allowed). Each player will be assigned a random role and starting tile.
2.  **Start Game:** Once 2 or more players are added, the "Start Game" button will become active. Click it to begin the game.
3.  **Gameplay:** Follow the turn structure:
    *   The current player's panel will be highlighted, showing available actions and remaining actions.
    *   Click action buttons to perform actions (Move, Drain, etc.). The board tiles or other players might become highlighted, indicating valid choices for the action. Click a highlighted tile/player to complete the action.
    *   Click cards in your hand to use Action Cards (Sandbags, Helicopter Lift) or initiate giving a Treasure Card.
    *   Click the "Fin de Tour" (End Turn) button when you have used your actions or wish to end your turn. The game will then proceed through drawing treasure cards (handling Water Rise) and flooding tiles.
4.  **Winning/Losing:** The game will end and display a message when a win or loss condition is met.

Refer to the official Forbidden Island rules for detailed gameplay mechanics and strategy.

## Architecture Notes

The project adheres to the **Model-View-Controller (MVC)** architectural pattern, separating data and logic (Model), user interface (View), and the intermediary handling user input and updating the view based on model changes (Controller).

*   **Model (`Model` package):** This is the core game engine.
    *   `BoardGame` acts as the central orchestrator, holding the entire game state (board, players, decks, water level). It contains the primary game logic for turns, actions, flooding, and win/loss checks.
    *   Components like `Zone`, `Player`, `Hand`, `Deck`, and `WaterMeter` represent the game's data and granular state elements. Enums like `GameState`, `ZoneState`, `PlayerRole`, `Artefact`, and `CardType` define the various types and states within the game, often used by `BoardGame` to control logic flow.
    *   The `GameState` enum and the conditional logic within `BoardGame` methods that check and transition the `gameState` effectively implement the **State Pattern**. This ensures that certain actions (like moving or shoring up) are only valid when the game is in the correct state (e.g., `PlayerChooseWhereToMove`).
    *   The `Zone` class uses **Inheritance** with subclasses (`ArtefactZone`, `HelicopterZone`, `PlayerStartZone`) to represent specialized tile types that have unique properties (like holding an artefact or being a starting point).
    *   **Factory Pattern:** `ZoneFactory` and `PlayerFactory` are used to abstract and centralize the creation of `Zone` and `Player` objects, respectively. This hides the complexity of initializing different zone types (with unique cards and properties) and players (with random unique roles), ensuring that rules like "each zone card is used once" and "each player role is used once" are enforced during setup.

*   **View (`View.SwingView` package, implementing `View.contract.GameView`):** This package is responsible solely for presenting the game state to the user and capturing user input.
    *   The `GameView` interface defines the contract that the `GUI` must implement, allowing the `GameController` to interact with the View without needing to know the specific Swing implementation details (promoting **Loose Coupling**).
    *   `GUI` is the main window. It contains and lays out specialized JPanels (`FilteredImagePanel` for zones, `PlayerPanel` for players) which handle displaying specific parts of the game state.
    *   `FilteredImagePanel` handles rendering the zone's background image, state overlays, player pawns, and artefacts. It also contains the MouseListener logic to detect when a user clicks on a zone, delegating this event back to the Controller.
    *   `PlayerPanel` displays player-specific information, their hand, artefacts, and action buttons. It detects clicks on buttons or cards, also delegating these events to the Controller.
    *   `ResourceMapper` acts as a centralized **Registry** and **Cache** for image resources, preventing redundant loading and providing a single point of access for all UI elements needing images.

*   **Controller (`Controller` package):** This is the bridge between the Model and the View.
    *   `GameController` receives events from the View (e.g., "Move button clicked", "Zone clicked"). It then queries the Model to determine the current state and validate if the requested action is permissible (`boardGame.isPlayerChoosingZoneToMove()`, `boardGame.getPossiblePlayerActions(...)`).
    *   If the action is valid, the Controller calls the appropriate method in the Model (`boardGame.movePlayerToZone(...)`) to update the game state.
    *   After the Model's state changes (or if an action requires a view update), the Controller calls methods on the `GameView` interface (`gameView.updateZonePanels()`, `gameView.updatePlayerPanels()`) to instruct the View to refresh itself.
    *   `modelActionHandler` utilizes the **Command Pattern** conceptually by accepting a `Runnable` representing a Model action. It wraps the execution of this command in centralized exception handling logic, catching specific game-related exceptions (`GameOverException`, `InvalidActionException`, etc.) and using the `GameView` to display user-friendly messages. This prevents scattered `try-catch` blocks throughout the `GameController`.
    *   The `Helper` package interfaces (`AddPlayerCallback`, `ChoosablePlayerCallback`) facilitate communication back from dialogs and panels to the Controller using the **Callback Pattern**, which is a common way to handle asynchronous responses in event-driven GUI programming.

This combination of patterns helps ensure a structured, modular, and maintainable codebase where different parts of the application have clear responsibilities.

## Dependencies

*   Standard Java libraries (java.awt, javax.swing, java.util etc.).
*   No external third-party libraries are required.

---

*This project is a fan-made implementation of the Forbidden Island board game for educational and personal use. It is not affiliated with, endorsed, or sponsored by the game's original creators, Matt Leacock and Gamewright.*