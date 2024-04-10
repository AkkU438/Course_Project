import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test extends Application {

    private Deck deck;
    private List<Card> playerCards;
    private List<Card> dealerCards;

    private Label playerLabel = new Label("Player: ");
    private Label statusLabel = new Label();

    private HBox playerCardsBox = new HBox(10);
    private HBox dealerCardsBox = new HBox(10);

    private boolean gameOver = false;

    private MediaPlayer mediaPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize deck and shuffle cards
        deck = new Deck();
        deck.shuffle();

        // Deal initial cards
        playerCards = new ArrayList<>();
        dealerCards = new ArrayList<>();
        dealInitialCards();

        // Buttons
        Button hitButton = new Button("Hit");
        Button standButton = new Button("Stand");
        Button newGameButton = new Button("New Game");

        hitButton.setOnAction(e -> {
            if (!gameOver) {
                hit(playerCards);
                updatePlayerCards();
                int playerScore = calculateScore(playerCards);
                if (playerScore > 21) {
                    endGame("Player busts! Dealer wins.");
                }
            }
        });

        standButton.setOnAction(e -> {
            if (!gameOver) {
                dealerPlay();
                updateDealerCards();
                int playerScore = calculateScore(playerCards);
                int dealerScore = calculateScore(dealerCards);
                if (dealerScore > 21 || playerScore > dealerScore) {
                    endGame("Player wins!");
                } else if (playerScore < dealerScore) {
                    endGame("Dealer wins!");
                } else {
                    endGame("It's a tie!");
                }
            }
        });

        newGameButton.setOnAction(e -> {
            reset();
            dealInitialCards();
            updatePlayerCards();
            updateDealerCards();
            statusLabel.setText("");
            gameOver = false;
        });

        // Set player score label style to bold
        playerLabel.setStyle("-fx-font-weight: bold;");

        // Layout
        HBox buttonsBox = new HBox(10, hitButton, standButton, newGameButton);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, playerLabel, playerCardsBox, dealerCardsBox, buttonsBox, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        // Calculate background size to match the scene dimensions
        double sceneWidth = 800; // Width of the scene
        double sceneHeight = 600; // Height of the scene

        // Load background image
        Image backgroundImage = new Image("file:PNG-cards-1.3/Background.jpg", sceneWidth, sceneHeight, false, true);
        if (backgroundImage.isError()) {
            System.out.println("Error loading background image: " + backgroundImage.getUrl());
        }
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        root.setBackground(new Background(background));

        // Scene
        Scene scene = new Scene(root, 800, 600);

        // Stage
        primaryStage.setTitle("Blackjack");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial card display
        updatePlayerCards();
        updateDealerCards();

        // Background Music
        String musicFile = "Jazz.mp3"; // Change to your music file name
        Media sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // To loop the music indefinitely
        mediaPlayer.play(); // Start playing the music
    }

    private void dealInitialCards() {
        hit(playerCards);
        hit(playerCards);
        hit(dealerCards);
        Card secondDealerCard = deck.draw();
        secondDealerCard.setFaceDown();
        dealerCards.add(secondDealerCard);
    }

    
    private void hit(List<Card> hand) {
        hand.add(deck.draw());
    }

    private void updatePlayerCards() {
        playerCardsBox.getChildren().clear();
        int playerScore = calculateScore(playerCards);
        for (Card card : playerCards) {
            ImageView imageView = new ImageView(new Image(card.getImagePath()));
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            playerCardsBox.getChildren().add(imageView);
        }
        playerLabel.setText("Player: Score - " + playerScore);
    }

    private void updateDealerCards() {
        dealerCardsBox.getChildren().clear();
        for (Card card : dealerCards) {
            ImageView imageView;
            if (card.isFaceDown()) {
                imageView = new ImageView(new Image("file:PNG-cards-1.3/back_of_card.png"));
            } else {
                imageView = new ImageView(new Image(card.getImagePath()));
            }
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            dealerCardsBox.getChildren().add(imageView);
        }
    }

private void endGame(String message) {
    statusLabel.setText(message);
    gameOver = true;
    // Reveal dealer's facedown card
    dealerCards.get(1).setFaceDown(false);
    updateDealerCards();
    displayEndScreen(message);
}

    private void reset() {
        deck = new Deck();
        deck.shuffle();
        playerCards.clear();
        dealerCards.clear();
        gameOver = false;
    }

    private void dealerPlay() {
        dealerCards.get(1).setFaceDown(false); // Reveal dealer's facedown card
        while (calculateScore(dealerCards) < 17) {
            hit(dealerCards);
            updateDealerCards();
        }
    }

    private int calculateScore(List<Card> hand) {
        int score = 0;
        int aceCount = 0;
        for (Card card : hand) {
            int value = card.getValue();
            if (value == 1) {
                aceCount++;
            }
            score += value;
        }
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        return score;
    }

    private void displayEndScreen(String message) {
        Stage endStage = new Stage();
        VBox endLayout = new VBox(10);
        endLayout.setAlignment(Pos.CENTER);
        endLayout.setPadding(new Insets(20));

        Label endMessage = new Label(message);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> endStage.close());

        endLayout.getChildren().addAll(endMessage, closeButton);

        Scene endScene = new Scene(endLayout, 300, 200);
        endStage.setScene(endScene);
        endStage.setTitle("Game Over");
        endStage.show();
    }

    private static class Card {
        private final String suit;
        private final String rank;
        private final int value;
        private boolean faceDown;

        public Card(String suit, String rank, int value) {
            this.suit = suit;
            this.rank = rank;
            this.value = value;
            this.faceDown = false;
        }

        public int getValue() {
            return value;
        }

        public String getImagePath() {
            if (faceDown) {
                return "file:PNG-cards-1.3/back_of_card.png";
            } else {
                return "file:PNG-cards-1.3/" + rank + "_of_" + suit + ".png";
            }
        }

        public boolean isFaceDown() {
            return faceDown;
        }

        public void setFaceDown(boolean faceDown) {
            this.faceDown = faceDown;
        }

        public void setFaceDown() {
            this.faceDown = true;
        }
    }

    private static class Deck {
        private final List<Card> cards;

        public Deck() {
            cards = new ArrayList<>();
            String[] suits = {"hearts", "diamonds", "clubs", "spades"};
            String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace"};
            for (String suit : suits) {
                for (int i = 0; i < ranks.length; i++) {
                    int value = i < 9 ? i + 2 : (i == 12 ? 11 : 10);
                    cards.add(new Card(suit, ranks[i], value));
                }
            }
        }

        public void shuffle() {
            Collections.shuffle(cards);
        }

        public Card draw() {
            if (cards.isEmpty()) {
                throw new IllegalStateException("Deck is empty");
            }
            return cards.remove(0);
        }
    }
}