'use strict';

// DOM elements
const userFullname = document.querySelector('#connected-user-fullname');
const userFullnameGame = document.querySelector('#connected-user-fullname-game');
const usernamePage = document.querySelector('#username-page'); // Page for entering username
const chatPage = document.querySelector('#chat-page'); // Page for chat
const usernameForm = document.querySelector('#usernameForm'); // Form for username input
const messageForm = document.querySelector('#messageForm'); // Form for sending messages
const globalMessageForm = document.querySelector('#globalMessageForm'); // Form for sending global messages
const messageInput = document.querySelector('#message'); // Input field for messages
const globalMessageInput = document.querySelector('#global-message'); // Input field for global messages
const connectingElement = document.querySelector('.connecting'); // Element showing connection status
const gameChatAreaMessages = document.querySelector('#chat-messages'); // Area for chat messages
const globalChatContainer = document.getElementById('global-chat-container');
const globalChatMessages = document.querySelector('#global-chat-messages'); // Area for global chat messages
const gameContainer = document.querySelector('#game-container'); // Container for the game
const username = document.querySelector('#username'); // Input field for username
const gameStatus = document.querySelector('#game-status'); // Element showing game status
const outerContainer = document.querySelector('#outerContainer'); // Outer container element
const currentPlayerTurn = document.getElementById('current-player-turn'); // Element showing current player's turn
const gameTextContainer = document.getElementById('game-text-container');
const chatAreaContainer = document.getElementById('chat-area');
const leaderboardContainer = document.getElementById('leader-container-div');
const historyContiner = document.getElementById('users-history-container-div');
const gameBoardContainer = document.getElementById('image-container');
const userStatsContainer = document.getElementById('user-stats-container');
const usersListContainer = document.getElementById('users-list-container-div');
const spinner = document.getElementById('spinner');

let stompClient = null; // STOMP client for WebSocket communication
let nickname = document.querySelector('#nickname'); // Nickname of the user
let fullname = document.querySelector('#fullname'); // Full name of the user
let password = document.querySelector('#password'); // Password of the user
let selectedUserId = null; // Selected user ID
let currGame = null; // Current game

// Buttons
const matchBtn = document.querySelector('#match'); // Button for matching
const cancelBtn = document.querySelector('#cancel'); // Button for canceling match
const leaderboardBtn = document.querySelector('#leaderboard'); // Button for showing leaderboard
const globalChatBtn = document.querySelector('#globalChat'); // Button for showing global chat
const historyBtn = document.querySelector('#history'); // Button for showing match history
const throwBtn = document.querySelector('#throw'); // Button for throwing dice
const sellBtn = document.getElementById('sell'); // Button for selling property
const leaveBtn = document.querySelector('#leave'); // Button for leaving the game
const logoutBtn = document.querySelector('#logout'); // Button for logging out

// Event Listeners
usernameForm.addEventListener('submit', connect, true); // Listener for username form submission
messageForm.addEventListener('submit', sendMessage, true); // Listener for message form submission
globalMessageForm.addEventListener('submit', sendGlobalMessage, true); // Listener for global message form submission

matchBtn.addEventListener('click', onMatch, true); // Listener for match button click
cancelBtn.addEventListener('click', onCancel, true); // Listener for cancel button click
leaderboardBtn.addEventListener('click', showLeaderBoard, true); // Listener for leaderboard button click
globalChatBtn.addEventListener('click', showGlobalChat, true); // Listener for global chat button click
historyBtn.addEventListener('click', onHistory, true); // Listener for history button click
throwBtn.addEventListener('click', throwDie, true); // Listener for throw button click
sellBtn.addEventListener('click', sellProperty, true); // Listener for sell button click
logoutBtn.addEventListener('click', onLogout, true); // Listener for logout button click
leaveBtn.addEventListener('click', onLeave, true); // Listener for leave button click

// Handle window unload event to trigger logout
window.onbeforeunload = () => onLogout();

// Connect to WebSocket server and authenticate user
async function connect(event) {
  event.preventDefault(); // Prevent form default submission behavior

  // Trim and retrieve input values
  nickname = nickname.value.trim();
  fullname = fullname.value.trim();
  password = password.value.trim();

  // Check if all required fields are filled
  if (nickname && fullname && password) {
    const user = { nickName: nickname, hashedPassword: password, fullName: fullname, status: 'ONLINE' };

    try {
      // Authenticate user
      const response = await fetch('/user/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(user),
      });

      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

      const data = await response.json();
      console.log('Success:', data);

      // Create WebSocket connection
      const socket = new SockJS('/ws');
      stompClient = Stomp.over(socket);
      stompClient.connect({}, onConnected, onError);
    } catch (error) {
      alert('username already taken or wrong password.');
      console.error('Error:', error);
    }
  }
}

// Handle successful WebSocket connection
function onConnected() {
  usernamePage.classList.add('hidden'); // Hide username page
  outerContainer.hidden = false; // Show main container

  // Subscribe to various channels
  stompClient.subscribe(`/topic/public`, onMessageReceived); // global public updates likes uer joined or left
  stompClient.subscribe(`/user/${nickname}/queue/messages`, onMessageReceived); // receiving game chat messages
  stompClient.subscribe(`/user/${nickname}/queue/gamemessages`, onGameMessageReceived); // receiving game update messsages
  stompClient.subscribe(`/user/${nickname}/queue/globalMessages`, onGlobalMessageReceived); // global chat messages

  // game start message
  stompClient.subscribe(`/user/${nickname}/queue/gamestart`, (message) => {
    handleGameStart(JSON.parse(message.body));
  });

  // game updates like throwing a die, selling etc
  stompClient.subscribe(`/user/${nickname}/queue/game`, (message) => {
    updateGameBoard(JSON.parse(message.body));
  });

  // Register the connected user
  const user = {
    nickName: nickname,
    hashedPassword: password,
    fullName: fullname,
    status: 'ONLINE',
  };
  stompClient.send('/app/user.addUser', {}, JSON.stringify(user)); // send update to all users about current user connected

  userFullname.textContent = fullname; // Update user fullname display
  fetchAndDisplayGlobalChat(); // Fetch and display global chat messages
  showLeaderBoard(); // Display leaderboard
}

async function showLeaderBoard() {
  try {
    const leaderboardRes = await fetch(`/user/leaderboard`);
    const leaderboard = await leaderboardRes.json();

    // hide game chat area and game text container and show leaderboard
    gameTextContainer.hidden = true;
    chatAreaContainer.hidden = true;
    leaderboardContainer.hidden = false;

    // add leaders to leaderboard
    const leaderList = document.getElementById('leaderList');
    leaderList.innerHTML = ''; // clearing leader llst so we won't append to already added list

    leaderboard.forEach((user, index) => {
      appendLeaderElement(user, leaderList, index + 1);
    });
  } catch (error) {
    console.error('Error fetching leaderboard:', error);
  }
}

function appendLeaderElement(user, leaderList, rank) {
  // Create a button element instead of an li element
  const listItem = document.createElement('button');
  listItem.classList.add('leader-item');

  // Create username element
  const usernameDiv = document.createElement('div');
  usernameDiv.classList.add('leader-stats');
  usernameDiv.textContent = `Rank : ${rank}  |  Username: ${user.nickname}`;

  // Create stats element
  const statsDiv = document.createElement('div');
  statsDiv.classList.add('leader-stats');
  statsDiv.textContent = `W: ${user.wins} | L: ${user.losses} | D: ${user.draws}`;

  // Append elements to listItem
  listItem.appendChild(usernameDiv);
  listItem.appendChild(statsDiv);

  // Add click event listener to listItem
  listItem.addEventListener('click', async () => {
    console.log(`Clicked on user with nickname: ${user.nickname}`);
    selectedUserId = user.nickname;
    await fetchAndDisplayUserStats();
  });

  // Add the button to the leader list
  leaderList.appendChild(listItem);
}

async function fetchAndDisplayUserStats() {
  try {
    console.log('Fetching stats  of user: ' + selectedUserId);
    const userStatsRes = await fetch(`/player/${selectedUserId}/stats`);
    const userStats = await userStatsRes.json();

    // Populate the user stats in the UI
    document.getElementById('nickname-stats').textContent = userStats.nickname;
    document.getElementById('fullname-stats').textContent = userStats.fullName;
    document.getElementById('games-played').textContent = userStats.gamesPlayed;
    document.getElementById('wins').textContent = userStats.wins;
    document.getElementById('losses').textContent = userStats.losses;
    document.getElementById('draws').textContent = userStats.draws;
    document.getElementById('stats-status').textContent = userStats.status;
    // convert epoch time to human readable time
    const joindate = new Date(userStats.joinedOn);
    const formattedJoinedOn = joindate.toLocaleString();
    document.getElementById('joined-on').textContent = formattedJoinedOn;
    // convert epoch time to human readable time
    const lastdate = new Date(userStats.lastOnline);
    const formattedLastOnlineDate = lastdate.toLocaleString();
    document.getElementById('last-online').textContent = formattedLastOnlineDate;

    // un-hide user stats div
    userStatsContainer.hidden = false;

    // hide global chat, history, game board
    globalChatContainer.hidden = true;
    historyContiner.hidden = true;
    gameBoardContainer.hidden = true;
  } catch (error) {
    console.error('Failed to fetch user chat:', error);
  }
}

// Fetch and display global chat messages
async function fetchAndDisplayGlobalChat() {
  try {
    console.log('Fetching global chat:');

    // Fetch global chat messages from the server
    const globalChatResponse = await fetch(`/global/messages`);
    if (!globalChatResponse.ok) throw new Error('Network response was not ok ' + globalChatResponse.statusText);

    const globalChat = await globalChatResponse.json();
    console.log('Messages received:', globalChat);

    globalChatMessages.innerHTML = ''; // Clear the chat area

    // Display each global chat message
    globalChat.forEach((chat) => {
      displayGlobalMessage(chat.senderId, chat.content);
    });

    // Scroll to the bottom of the chat area
    globalChatMessages.scrollTop = globalChatMessages.scrollHeight;
  } catch (error) {
    console.error('Failed to fetch global chat:', error);
  }
}

async function onGameMessageReceived(gameMsgs) {
  if (gameMsgs.body != null) {
    gameMsgs = JSON.parse(gameMsgs.body);
  }

  gameMsgs.forEach((msg, index) => {
    // create a li and append to text area
    const msgTxt = msg.textContent;
    // if msg contains take decision that means we have to show prompt and not append that msg
    if (msgTxt.includes('TAKE DECISION')) {
      // show prompt for buy or rent
      if (currGame.currentPlayer === nickname) {
        showDecisionPrompt(currGame);
      }
    } else {
      const textMsg = document.createElement('li');
      textMsg.classList.add('game-text-msg');
      textMsg.textContent = msg.textContent;

      const textList = document.getElementById('game-text-list');
      textList.appendChild(textMsg);

      // scroll to last
      const gameTextContainer = document.getElementById('game-text-container');
      gameTextContainer.scrollTop = gameTextContainer.scrollHeight;
    }
  });
}

async function handleGameStart(game) {
  console.log('Game started. Your game is :  ' + game);
  currGame = game;

  // hide start match and show leave match and hide spinner and hide online users
  spinner.hidden = true;
  leaveBtn.hidden = false;
  cancelBtn.hidden = true;
  leaderboardBtn.hidden = true;
  globalChatBtn.hidden = true;
  historyBtn.hidden = true;
  userStatsContainer.hidden = true;
  historyContiner.hidden = true;
  usersListContainer.hidden = true;
  globalChatContainer.hidden = true;
  leaderboardContainer.hidden = true;
  gameTextContainer.hidden = false;
  chatAreaContainer.hidden = false;
  gameBoardContainer.hidden = false;
  throwBtn.hidden = false;
  sellBtn.hidden = false;
  document.getElementById('status-and-turn').hidden = false;
  document.getElementById('dice-div').hidden = false;

  // show chat area
  gameChatAreaMessages.hidden = false;
  messageForm.hidden = false;

  // clear any previous game messages
  const textList = document.getElementById('game-text-list');
  textList.innerHTML = '';
  const chatMessages = document.getElementById('chat-messages');
  chatMessages.innerHTML = '';
  userFullnameGame.textContent = nickname;

  await updateGameBoard(game);
  await fetchAndDisplayUserChat(currGame.gameId);
}

async function updateGameBoard(game) {
  currGame = game;
  if (!game) {
    return;
  }
  console.log('updating game board');

  // Check if it is the current player's turn
  const isCurrentPlayerTurn = game.currentPlayer === nickname;

  // Determine if the game is won
  const isGameWon = game.status.includes('WINS');
  const sellBtn = document.getElementById('sell');

  // update UI elements on board
  currentPlayerTurn.hidden = false;

  currentPlayerTurn.textContent = 'Player Turn : ' + game.currentPlayer;
  gameStatus.hidden = false;
  gameStatus.textContent = 'Game Status : ' + game.status;

  // update dice images
  const diceImage1 = document.getElementById('dice-img1');
  const diceImage2 = document.getElementById('dice-img2');
  diceImage1.src = `/resources/images/${game.dice1Value}.jpg`;
  diceImage2.src = `/resources/images/${game.dice2Value}.jpg`;

  // take the list of player ids and update div text and div color
  game.playerIdList.forEach((player, index) => {
    // for each span playerName add player.Name and add color as game.playerTojenMap.get(index).tokenColor
    const playerNameSpan = document.getElementById(`player${index + 1}Name`);
    const playerToken = document.getElementById(`token${index + 1}`);
    const playerPropertyDiv = document.getElementById(`player${index + 1}PropertyDiv`);
    const playerMoneySpan = document.getElementById(`player${index + 1}Money`);

    // if any player left then make his div grey and update his status as left on board
    // also hide his token
    if (game.playerStatusList[index] == false) {
      console.log('player who left : ' + player);
      playerPropertyDiv.style.backgroundColor = 'Grey';
      playerToken.hidden = true;
      playerNameSpan.textContent = player + ' (Left)';
      playerMoneySpan.textContent = '';
    } else {
      // player name on board
      playerNameSpan.textContent = player + ' : $';

      // player money
      playerMoneySpan.textContent = game.playerMoneyList[index];

      // Update the player's token color and div color
      const tokenColor = game.playerTokenMap[player].tokenColor;
      console.log('player token color is ' + tokenColor + 'and player name is ' + player);
      playerPropertyDiv.style.backgroundColor = tokenColor;
      playerToken.style.backgroundColor = tokenColor;
      playerToken.hidden = false;

      // update tokens postion
      playerToken.style.left = game.playerTokenMap[player].location[0] + 'px';
      playerToken.style.top = game.playerTokenMap[player].location[1] + 'px';
    }
  });

  // update properties on board with propertymap
  updatePropertyDivs(game, nickname);

  // disable Throw die button if not player's turn
  const throwButton = document.getElementById('throw');
  if (isCurrentPlayerTurn) {
    throwButton.hidden = false;
    throwBtn.hidden = false;
    throwBtn.hidden = false;
  } else {
    throwButton.hidden = true;
  }

  if (isGameWon) {
    throwButton.hidden = true;
    sellBtn.hidden = true;
    throwBtn.hidden = true;
    sellBtn.hidden = true;
    throwBtn.hidden = true;
  }

  await fetchAndDisplayUserChat(currGame.gameId);
}

function updatePropertyDivs(game) {
  // Clear property lists for all players
  game.playerIdList.forEach((playerId, index) => {
    const propertyListId = `player${index + 1}PropertyList`;
    const propertyList = document.getElementById(propertyListId);
    propertyList.innerHTML = '';
  });

  // Iterate through the propertyOwnerMap and append properties for all players
  for (const [playerId, properties] of Object.entries(game.propertyOwnerMap)) {
    properties.forEach((property) => {
      const prop = document.createElement('li');
      prop.textContent = property;

      // Create the hr element
      const hr = document.createElement('hr');
      hr.style.width = '100%';
      hr.style.height = '2px';
      hr.style.border = 'none';
      hr.style.backgroundColor = 'black';
      hr.style.margin = '10px 0';

      // Find the correct player's property list
      const playerIndex = game.playerIdList.indexOf(playerId);
      const propertyListId = `player${playerIndex + 1}PropertyList`;
      const propertyList = document.getElementById(propertyListId);

      propertyList.appendChild(prop);
      propertyList.appendChild(hr);

      // Scroll to the last element
      propertyList.scrollTop = propertyList.scrollHeight;
    });
  }
}

async function showDecisionPrompt(game) {
  var gameRes = await fetch(`/game/${currGame.gameId}`);
  game = await gameRes.json();
  currGame = game;

  var currPlayer = game.currentPlayer;
  var locX = game.playerTokenMap[currPlayer].locX;
  var locY = game.playerTokenMap[currPlayer].locY;
  let playerIndex = 0;

  for (let pId of game.playerIdList) {
    if (pId === currPlayer) {
      break;
    }
    playerIndex++;
  }

  // fetch property at which user has landed currently
  const propertyRes = await fetch(`/property?x=${locX}&y=${locY}`);
  const currProperty = await propertyRes.json();

  var propOwner = '0';
  // GET PROPERTY OWNER IF IT IS OWNED BY ANY PLAYER
  for (const [playerId, properties] of Object.entries(game.propertyOwnerMap)) {
    if (properties.includes(currProperty.name)) {
      propOwner = playerId;
      break; // Return the playerId if found
    }
  }

  // variables for playerDecisionDTO
  var propertyBought = false;
  var rentPaid = false;

  // if property has no owner
  if (propOwner === '0') {
    let userInput = null;
    while (true) {
      userInput = prompt(
        `You have reached ${currProperty.name}.\n This property isn't owned by anyone.\nDo you want to buy the property for $${currProperty.cost} or pay the rent of $${currProperty.rent} for staying here.\n Enter 1 to buy and 2 to pay rent.`
      );

      if (userInput === '1' || userInput === '2') {
        break;
      } else {
        alert(`Invalid input, please enter 1 to buy ${currProperty.name} for $${currProperty.cost} or 2 to pay rent of $${currProperty.rent}.`);
      }
    }

    // player want to buy
    if (userInput === '1') {
      alert('Press Ok to buy ' + currProperty.name + ' for $' + currProperty.cost + '.');

      if (game.playerMoneyList[playerIndex] >= currProperty.cost) {
        alert('You have bought ' + currProperty.name + ' for $' + currProperty.cost + '.');
        propertyBought = true;
      } else if (game.playerMoneyList[playerIndex] >= currProperty.rent) {
        // pay rent if he cannot afford money
        alert("You don't have enough money to buy this property.\nPress OK to pay rent of this property.");
        rentPaid = true;
      } else {
        // he doesn't have enough money to buy or pay rent, therefore he is stuck
        alert("You are 'BROKE' and cannot buy or rent this property.\nUntil you have $" + currProperty.rent + ' to rent the place you cannot move.\n');
      }
    } else if (userInput === '2') {
      // user wants to pay rent
      alert('Press Ok to pay rent for ' + currProperty.name + ' of $' + currProperty.rent + '.');
      if (game.playerMoneyList[playerIndex] >= currProperty.rent) {
        alert('You have paid the rent for ' + currProperty.name + ' of $' + currProperty.rent + '.');
        rentPaid = true;
      } else {
        // he doesn't have enough money to pay rent, therefore he is stuck
        alert("You are 'BROKE' and cannot buy or rent this property.\nUntil you have $" + currProperty.rent + ' to rent the place you cannot move.\n');
      }
    }
  } else {
    // else if property has owner
    // check if property on which player landed is owned by same player
    if (propOwner === nickname) {
      // player doesnt have to pay any rent here, he can stay here for free since he owns the place
      // gameId, playerId, boughtProperty, paidRent, propertyLocX, propertyLocY
      alert("\n This property is already owned by you.\nYou don't have to pay anything here.\nYou can stay here for free.");
    } else {
      // player has to pay rent here
      alert('You have reached ' + currProperty.name + '.\n This property is owned by ' + propOwner + '. \nPress OK to pay $' + currProperty.rent + ' as rent for the property.');
      // if player has money to pay rent
      if (game.playerMoneyList[playerIndex] >= currProperty.rent) {
        rentPaid = true;
        alert('You have paid the rent for ' + currProperty.name + ' of $' + currProperty.rent + '.');
      } else {
        // he doesn't have enough money to pay rent, therefore he is stuck
        alert('You have reached ' + currProperty.name + '.\n This property is owned by ' + propOwner + '. \nPress OK to pay $' + currProperty.rent + ' as rent for the property.');
        alert("You are 'BROKE' and cannot rent this property.\nUntil you have $" + currProperty.rent + ' to rent the place you cannot move.\n');
      }
    }
  }

  stompClient.send(
    '/app/game.takeDecision',
    {},
    JSON.stringify({
      gameId: currGame.gameId,
      playerId: currPlayer,
      propertyBought: propertyBought,
      rentPaid: rentPaid,
      propertyLocX: locX,
      propertyLocY: locY,
    })
  );
}

async function fetchAndDisplayUserChat(gameId) {
  try {
    console.log('Fetching messages of user: ' + selectedUserId);
    const userChatResponse = await fetch(`/messages/${gameId}`);
    if (!userChatResponse.ok) {
      throw new Error('Network response was not ok ' + userChatResponse.statusText);
    }
    const userChat = await userChatResponse.json();

    gameChatAreaMessages.innerHTML = ''; // Clear the chat area
    userChat.forEach((chat) => {
      displayMessage(chat.senderId, chat.content);
    });

    gameChatAreaMessages.scrollTop = gameChatAreaMessages.scrollHeight; // Scroll to the bottom of the chat area
  } catch (error) {
    console.error('Failed to fetch user chat:', error);
  }
}

async function fetchAndDisplayGameMessages(gameId) {
  try {
    const gameMsgsResponse = await fetch(`/gamemessages/${gameId}`);
    if (!gameMsgsResponse.ok) {
      throw new Error('Network response was not ok ' + gameMsgsResponse.statusText);
    }

    // Parse JSON data from the response
    const gameMsgs = await gameMsgsResponse.json();

    const textList = document.getElementById('game-text-list');
    textList.innerHTML = ''; // Clear the chat area

    onGameMessageReceived(gameMsgs);
  } catch (error) {
    console.error('Failed to fetch user chat:', error);
  }
}

async function onGlobalMessageReceived(payload) {
  await findAndDisplayConnectedUsers();
  console.log('Message received', payload);
  const message = JSON.parse(payload.body);
  if (nickname && nickname !== message.senderId) {
    displayGlobalMessage(message.senderId, message.content);
    gameChatAreaMessages.scrollTop = gameChatAreaMessages.scrollHeight;
  }
}

async function onMessageReceived(payload) {
  await findAndDisplayConnectedUsers();
  console.log('Message received', payload);
  const message = JSON.parse(payload.body);
  if (nickname && nickname !== message.senderId) {
    displayMessage(message.senderId, message.content);
    gameChatAreaMessages.scrollTop = gameChatAreaMessages.scrollHeight;
  }
}

async function findAndDisplayConnectedUsers() {
  const connectedUsersResponse = await fetch('/user/connected');
  let connectedUsers = await connectedUsersResponse.json();
  const connectedUsersList = document.getElementById('connectedUsers');
  connectedUsersList.innerHTML = '';

  connectedUsers.forEach((user) => {
    appendUserElement(user, connectedUsersList);
    if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
      const separator = document.createElement('li');
      separator.classList.add('separator');
      connectedUsersList.appendChild(separator);
    }
  });
}

function appendUserElement(user, connectedUsersList) {
  const listItem = document.createElement('li');
  listItem.classList.add('user-item');
  listItem.id = user.nickName;

  const userImage = document.createElement('img');
  userImage.src = 'https://static.vecteezy.com/system/resources/previews/019/879/186/non_2x/user-icon-on-transparent-background-free-png.png';
  userImage.alt = user.fullName;

  const usernameSpan = document.createElement('span');
  usernameSpan.textContent = user.fullName;

  listItem.appendChild(userImage);
  listItem.appendChild(usernameSpan);

  listItem.addEventListener('click', userItemClick);

  connectedUsersList.appendChild(listItem);
}

function userItemClick(event) {
  document.querySelectorAll('.user-item').forEach((item) => {
    item.classList.remove('active');
  });
  gameTextContainer.hidden = true;
  chatAreaContainer.hidden = true;
  historyContiner.hidden = true;
  gameBoardContainer.hidden = true;
  leaderboardContainer.hidden = false;

  const clickedUser = event.currentTarget;
  clickedUser.classList.add('active');

  selectedUserId = clickedUser.getAttribute('id');
  fetchAndDisplayUserStats().then;
}

function displayMessage(senderId, content) {
  const messageContainer = document.createElement('div');
  const message = document.createElement('p');
  messageContainer.classList.add('message');
  if (senderId === nickname) {
    messageContainer.classList.add('sender');
    message.textContent = content;
  } else {
    messageContainer.classList.add('receiver');
    message.textContent = senderId + ' : ' + content;
  }
  messageContainer.appendChild(message);
  gameChatAreaMessages.appendChild(messageContainer);
}

function displayGlobalMessage(senderId, content) {
  const messageContainer = document.createElement('div');
  const message = document.createElement('p');
  messageContainer.classList.add('message');
  if (senderId === nickname) {
    messageContainer.classList.add('sender');
    message.textContent = content;
  } else {
    messageContainer.classList.add('receiver');
    message.textContent = senderId + ' : ' + content;
  }
  messageContainer.appendChild(message);
  globalChatMessages.appendChild(messageContainer);
  globalChatMessages.scrollTop = globalChatMessages.scrollHeight; // Scroll to the bottom of the chat area
}

function onError() {
  connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
  connectingElement.style.color = 'red';
}

function sendMessage(event) {
  const messageContent = messageInput.value.trim();
  if (messageContent && stompClient) {
    const chatMessage = {
      senderId: nickname,
      recipientIds: currGame.playerIdList,
      gameId: currGame.gameId,
      content: messageInput.value.trim(),
      timestamp: new Date(),
    };
    stompClient.send('/app/chat', {}, JSON.stringify(chatMessage));
    displayMessage(nickname, messageInput.value.trim());
    messageInput.value = '';
  }
  gameChatAreaMessages.scrollTop = gameChatAreaMessages.scrollHeight;
  event.preventDefault();
}

function sendGlobalMessage(event) {
  const messageContent = globalMessageInput.value.trim();
  if (messageContent && stompClient) {
    const chatMessage = {
      senderId: nickname,
      recipientIds: null,
      gameId: 'GLOBAL',
      content: globalMessageInput.value.trim(),
      timestamp: new Date(),
    };
    stompClient.send('/app/global/chat', {}, JSON.stringify(chatMessage));
    displayGlobalMessage(nickname, globalMessageInput.value.trim());
    globalMessageInput.value = '';
  }
  gameChatAreaMessages.scrollTop = gameChatAreaMessages.scrollHeight;
  event.preventDefault();
}

async function onHistory() {
  //hide user stats and game board
  userStatsContainer.hidden = true;
  globalChatContainer.hidden = true;
  gameTextContainer.hidden = true;
  chatAreaContainer.hidden = true;
  gameBoardContainer.hidden = true;

  // show history container
  historyContiner.hidden = false;

  // show leaderboard
  leaderboardContainer.hidden = false;

  // hide form area and chat area
  gameChatAreaMessages.hidden = true;

  const gameListRes = await fetch(`/game/history/${nickname}`);
  const gameList = await gameListRes.json();

  const gamesList = document.getElementById('gamesList');
  gamesList.innerHTML = '';

  gameList.forEach((game) => {
    appendGameElement(game, gamesList);
    if (gameList.indexOf(game) < gameList.length - 1) {
      const separator = document.createElement('li');
      separator.classList.add('separator');
      gamesList.appendChild(separator);
    }
  });
}

function appendGameElement(game, gamesList) {
  // Create a button element instead of an li element
  const listItem = document.createElement('button');
  listItem.classList.add('game-item');

  // Determine opponent names by filtering out the current user's nickname
  const opponents = game.playerIdList.filter((playerId) => playerId !== nickname);

  // Create opponent name element
  const opponentDiv = document.createElement('div');
  opponentDiv.classList.add('opponent-name');

  // Join all opponent names into a single string
  opponentDiv.textContent = `Opponents: ${opponents.join(', ')}`;

  // Create status element
  const statusDiv = document.createElement('div');
  statusDiv.classList.add('game-status');
  statusDiv.textContent = `Status : ${game.status}`;

  // Create game start time element
  // Convert startTime from epoch to readable date
  const date = new Date(game.startTime);
  const formattedDate = date.toLocaleString();
  const playTimeDiv = document.createElement('div');
  playTimeDiv.classList.add('play-time');
  playTimeDiv.textContent = `Played at : ${formattedDate}`;

  // Append elements to listItem
  listItem.appendChild(opponentDiv);
  listItem.appendChild(playTimeDiv);
  listItem.appendChild(statusDiv);

  // Add click event listener to listItem
  listItem.addEventListener('click', async () => {
    currGame = game;
    // Action to perform on click
    console.log(`Clicked on game with ID: ${game.gameId}`);
    await fetchAndDisplayUserChat(game.gameId);
    await fetchAndDisplayGameMessages(game.gameId);
    updateGameBoard(game);

    // update UI elements
    historyContiner.hidden = true;
    userStatsContainer.hidden = true;
    globalChatContainer.hidden = true;
    gameBoardContainer.hidden = false;

    // show chat area and game text updates
    gameChatAreaMessages.hidden = false;
    chatAreaContainer.hidden = false;
    gameTextContainer.hidden = false;
    messageForm.hidden = true;

    // hide leaderboard
    leaderboardContainer.hidden = true;
  });

  // Add the button to the games list
  gamesList.appendChild(listItem);
}

function onLogout() {
  stompClient.send(
    '/app/user.disconnectUser',
    {},
    JSON.stringify({
      nickName: nickname,
      fullName: fullname,
      status: 'OFFLINE',
    })
  );

  // send message to server that person leftGame
  if (currGame != null && nickname != null) {
    stompClient.send('/app/user.leaveGame', {}, JSON.stringify({ gameId: currGame.gameId, userId: nickname }));
  }

  window.location.href = 'http://localhost:8080';
}

function onLeave() {
  // UI elements show and hide
  leaveBtn.hidden = true;
  leaderboardBtn.hidden = false;
  leaderboardContainer.hidden = false;
  globalChatBtn.hidden = false;
  historyBtn.hidden = false;
  userStatsContainer.hidden = true;
  usersListContainer.hidden = false;
  globalChatContainer.hidden = false;
  gameTextContainer.hidden = true;
  leaderboardContainer.hidden = false;
  chatAreaContainer.hidden = true;
  gameBoardContainer.hidden = true;
  throwBtn.hidden = true;
  sellBtn.hidden = true;
  matchBtn.hidden = false;
  document.getElementById('dice-div').hidden = true;
  document.getElementById('status-and-turn').hidden = true;
  document.getElementById('game-status').hidden = true;
  document.getElementById('current-player-turn').hidden = true;

  // show chat area
  gameChatAreaMessages.hidden = true;
  messageForm.hidden = true;

  // send message to server about leaving game
  if (currGame != null && nickname != null) {
    stompClient.send('/app/user.leaveGame', {}, JSON.stringify({ gameId: currGame.gameId, playerId: nickname }));
  }
}

function onMatch(event) {
  stompClient.send('/app/user.matchUser', {}, JSON.stringify({ nickName: nickname, fullName: fullname, status: 'ONLINE' }));
  // show spinner and show cancel match
  spinner.hidden = false;
  cancelBtn.hidden = false;
  matchBtn.hidden = true;
  event.preventDefault();
}

async function onCancel() {
  const cancelStatus = await fetch(`/game/cancel/${nickname}`);
  console.log('Match find cancel status : ' + cancelStatus);
  spinner.hidden = true;
  cancelBtn.hidden = true;
  matchBtn.hidden = false;
}

function showGlobalChat() {
  userStatsContainer.hidden = true;
  historyContiner.hidden = true;
  gameBoardContainer.hidden = true;
  gameTextContainer.hidden = true;
  chatAreaContainer.hidden = true;
  gameBoardContainer.hidden = true;
  globalChatContainer.hidden = false;
  leaderboardContainer.hidden = false;
}

function throwDie() {
  console.log(nickname + ' threw the die');
  // hide after throwing so that player wont throw twice
  throwBtn.hidden = true;
  stompClient.send('/app/game.throwDie', {}, JSON.stringify({ gameId: currGame.gameId, playerId: nickname }));
}

function sellProperty() {
  const propName = prompt('Enter name of property which you want to sell.');

  // check if user owns the property or not
  // if owns then show alert, you have sold prop name for $propcost
  // else alert you dont prop name so you cant sell it

  let isOwner = false;
  let playerProperties = currGame.propertyOwnerMap[nickname];

  if (playerProperties && playerProperties.includes(propName)) {
    isOwner = true;
  }

  if (isOwner) {
    alert(`You have sold ${propName}.`);
    // send to server that user sold this property so it can be deleted from propertyOwnerMap and increase player money
    stompClient.send('/app/game.sellProperty', {}, JSON.stringify({ gameId: currGame.gameId, playerId: nickname, propertyName: propName }));
  } else {
    alert(`You don't own ${propName}, so you can't sell it.`);
  }
}
