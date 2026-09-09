var suits = ["heart", "diamond", "club", "spade"];
var cards = [];
var flippedCards = [];
var matchedCount = 0;
var totalPairs = 0;
var clickCount = 0;
var hasShuffled = false;
var isLocked = false;
var gameStarted = false;
var currentGridSize = "";

var timerInterval = null;
var timeSeconds = 0;

var timerEl = document.getElementById("timer");
var clickCountEl = document.getElementById("click-count");
var pairCountEl = document.getElementById("pair-count");
var totalPairsEl = document.getElementById("total-pairs");
var setupPanel = document.getElementById("setup-panel");
var actionButtons = document.getElementById("action-buttons");
var shuffleBtn = document.getElementById("shuffle-btn");
var startBtn = document.getElementById("start-btn");
var resetBtn = document.getElementById("reset-btn");
var gameBoard = document.getElementById("game-board");
var alertOverlay = document.getElementById("alert-overlay");
var alertOkBtn = document.getElementById("alert-ok-btn");
var winOverlay = document.getElementById("win-overlay");
var winMessage = document.getElementById("win-message");
var playAgainBtn = document.getElementById("play-again-btn");

var gridRadios = document.querySelectorAll('input[name="gridSize"]');
for (var i = 0; i < gridRadios.length; i++) {
    gridRadios[i].addEventListener("change", function () {
        selectGridSize(this.value);
    });
}

shuffleBtn.addEventListener("click", handleShuffle);
startBtn.addEventListener("click", handleStart);
resetBtn.addEventListener("click", resetGame);
alertOkBtn.addEventListener("click", function () {
    alertOverlay.style.display = "none";
});
playAgainBtn.addEventListener("click", resetGame);

function getGridDimensions(gridSize) {
    if (gridSize === "2x2") return { rows: 2, cols: 2 };
    if (gridSize === "4x4") return { rows: 4, cols: 4 };
    if (gridSize === "6x4") return { rows: 6, cols: 4 };
    return { rows: 4, cols: 4 };
}

function buildOrderedDeck(gridSize) {
    var dims = getGridDimensions(gridSize);
    var rows = dims.rows;
    var cols = dims.cols;
    var deck = [];
    var suitsToUse = suits.slice(0, cols);

    for (var row = 0; row < rows; row++) {
        for (var col = 0; col < cols; col++) {
            var suit = suitsToUse[col];
            var rank = row + 1;
            deck.push({
                suit: suit,
                rank: rank,
                image: "images/" + suit + "_" + rank + ".png",
                matchGroup: "rank_" + rank,
                isFlipped: false,
                isMatched: false
            });
        }
    }
    return deck;
}

function shuffleDeck(deck) {
    var shuffled = deck.slice();
    for (var i = shuffled.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var temp = shuffled[i];
        shuffled[i] = shuffled[j];
        shuffled[j] = temp;
    }
    return shuffled;
}

function selectGridSize(gridSize) {
    currentGridSize = gridSize;
    cards = buildOrderedDeck(gridSize);
    hasShuffled = false;
    gameStarted = false;
    clickCount = 0;
    matchedCount = 0;
    totalPairs = cards.length / 2;
    flippedCards = [];

    clickCountEl.textContent = "0";
    pairCountEl.textContent = "0";
    totalPairsEl.textContent = totalPairs;

    actionButtons.style.display = "flex";
    resetBtn.style.display = "none";

    renderBoard(true);
}

function renderBoard(showFaces) {
    gameBoard.innerHTML = "";
    gameBoard.className = "game-board grid-" + currentGridSize;

    for (var i = 0; i < cards.length; i++) {
        var card = cards[i];
        var cardEl = document.createElement("div");
        cardEl.className = "card";
        cardEl.setAttribute("data-index", i);

        if (showFaces || card.isFlipped || card.isMatched) {
            cardEl.classList.add("flipped");
        }
        if (card.isMatched) {
            cardEl.classList.add("matched");
        }

        var inner = document.createElement("div");
        inner.className = "card-inner";

        var back = document.createElement("div");
        back.className = "card-back";
        var backImg = document.createElement("img");
        backImg.src = "images/back.jpg";
        backImg.alt = "Card back";
        backImg.draggable = false;
        back.appendChild(backImg);

        var front = document.createElement("div");
        front.className = "card-front";
        var frontImg = document.createElement("img");
        frontImg.src = card.image;
        frontImg.alt = card.suit + " " + card.rank;
        frontImg.draggable = false;
        front.appendChild(frontImg);

        inner.appendChild(back);
        inner.appendChild(front);
        cardEl.appendChild(inner);

        cardEl.addEventListener("click", handleCardClick);
        gameBoard.appendChild(cardEl);
    }
}

function handleShuffle() {
    cards = shuffleDeck(cards);
    hasShuffled = true;
    renderBoard(true);
}

function handleStart() {
    if (!hasShuffled) {
        alertOverlay.style.display = "flex";
        return;
    }

    for (var i = 0; i < cards.length; i++) {
        cards[i].isFlipped = false;
        cards[i].isMatched = false;
    }

    gameStarted = true;
    clickCount = 0;
    matchedCount = 0;
    timeSeconds = 0;
    timerEl.textContent = "0s";
    if (timerInterval) clearInterval(timerInterval);
    timerInterval = setInterval(function () {
        timeSeconds++;
        timerEl.textContent = timeSeconds + "s";
    }, 1000);
    flippedCards = [];
    isLocked = false;

    clickCountEl.textContent = "0";
    pairCountEl.textContent = "0";

    setupPanel.style.display = "none";
    actionButtons.style.display = "none";
    resetBtn.style.display = "inline-block";

    renderBoard(false);
}

function handleCardClick() {
    if (!gameStarted || isLocked) return;

    var index = parseInt(this.getAttribute("data-index"));
    var card = cards[index];

    if (card.isFlipped || card.isMatched) return;

    clickCount++;
    clickCountEl.textContent = clickCount;

    card.isFlipped = true;
    this.classList.add("flipped");

    flippedCards.push({ index: index, element: this });

    if (flippedCards.length === 2) {
        isLocked = true;
        var first = flippedCards[0];
        var second = flippedCards[1];

        if (cards[first.index].matchGroup === cards[second.index].matchGroup) {
            setTimeout(function () {
                cards[first.index].isMatched = true;
                cards[second.index].isMatched = true;
                first.element.classList.add("matched");
                second.element.classList.add("matched");

                matchedCount++;
                pairCountEl.textContent = matchedCount;

                flippedCards = [];
                isLocked = false;

                if (matchedCount === totalPairs) {
                    if (timerInterval) clearInterval(timerInterval);
                    setTimeout(function () {
                        winMessage.textContent = "You completed the game in " + clickCount + " clicks and " + timeSeconds + " seconds!";
                        winOverlay.style.display = "flex";
                    }, 400);
                }
            }, 600);
        } else {
            setTimeout(function () {
                cards[first.index].isFlipped = false;
                cards[second.index].isFlipped = false;
                first.element.classList.remove("flipped");
                second.element.classList.remove("flipped");

                flippedCards = [];
                isLocked = false;
            }, 1000);
        }
    }
}

function resetGame() {
    winOverlay.style.display = "none";
    gameStarted = false;
    hasShuffled = false;
    currentGridSize = "";
    cards = [];
    flippedCards = [];
    clickCount = 0;
    matchedCount = 0;
    isLocked = false;

    if (timerInterval) clearInterval(timerInterval);
    timeSeconds = 0;
    timerEl.textContent = "0s";
    clickCountEl.textContent = "0";
    pairCountEl.textContent = "0";
    totalPairsEl.textContent = "0";

    gameBoard.innerHTML = "";
    gameBoard.className = "game-board";

    setupPanel.style.display = "block";
    actionButtons.style.display = "none";
    resetBtn.style.display = "none";

    var radios = document.querySelectorAll('input[name="gridSize"]');
    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = false;
    }
}
