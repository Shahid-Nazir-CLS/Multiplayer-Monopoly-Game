<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Monopoly - Dark Knight Theme</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/main.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/userstats.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/userhistorycontainer.css">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
                integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
                crossorigin="anonymous">

        </head>

        <body style="background-color: #27292a">
            <%@ include file="login.jsp" %>
                <div class="outer-container" style="margin: 0 3%; margin-top: 2%;" id="outerContainer" hidden>
                    <h1 style="color:white; text-align:center; margin-bottom: 10px; font-weight: bolder;">Welcome to
                        Monopoly</h1>
                    <div class="row">
                        <!-- Left Panel: Buttons and Dice -->
                        <div class="col-md-2" style="margin-left: 1%;">
                            <div class="panel left-panel-content">
                                <div class="users-list">
                                    <div class="users-list-container" id="users-list-container-div">
                                        <h4>User : <span id="connected-user-fullname">test123</span></h4>
                                        <p></p>
                                        <hr
                                            style=" width: 100%; height: 2px; border: none; background-color: white; margin: 10px 0; " />
                                        <h2>Online Users</h2>
                                        <ul id="connectedUsers">
                                        </ul>
                                    </div>

                                    <div id="dice-div" hidden>
                                        <h4>User : <span id="connected-user-fullname-game">test123</span></h4>
                                        <p></p>
                                        <hr
                                            style=" width: 100%; height: 2px; border: none; background-color: white; margin: 10px 0; " />
                                        <div class="d-flex flex-block align-items-center">
                                            <img src="<c:url value='/resources/images/5.jpg'/>" class="dice-img"
                                                id="dice-img1">
                                            <img src="/resources/images/6.jpg" class="dice-img" id="dice-img2">
                                        </div>
                                    </div>
                                    <div id="status-and-turn" hidden>
                                        <p id="current-player-turn"></p>
                                        <p id="game-status"></p>
                                    </div>
                                    <div class="mt-3 w-100">
                                        <button class="btn-dark-knight home-btn" id="match">Start Game</button>
                                        <div class="spinner" id="spinner" hidden>
                                            <div class="spinner-circle"></div>
                                            <p>Finding match...</p>
                                        </div>
                                        <button class="btn-dark-knight home-btn" id="throw" hidden>Throw
                                            Die</button>
                                        <button class="btn-dark-knight home-btn" id="sell" hidden>Sell
                                            Property</button>
                                        <button class="btn-dark-knight home-btn" id="leave" hidden>Leave
                                            Game</button>
                                        <button class="btn-dark-knight home-btn" id="cancel" hidden>Cancel
                                            Search</button>
                                        <button class="btn-dark-knight home-btn" id="leaderboard">Leaderboard</button>
                                        <button class="btn-dark-knight home-btn" id="history">Game History</button>
                                        <button class="btn-dark-knight home-btn" id="globalChat">Global
                                            Chat</button>
                                        <button class="btn-dark-knight home-btn" id="logout">Logout</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Middle Panel: Board Image -->
                        <div class="col-md-6 image-panel">
                            <div class="panel center-panel">
                                <!-- <img src="<c:url value='/resources/images/board1.jpg'/>" id="board-img" class="board-img" alt="Monopoly Board"> -->
                                <%@ include file="globalchat.jsp" %>
                                    <%@ include file="gameboard.jsp" %>
                                        <%@ include file="userstats.jsp" %>
                                            <%@ include file="userhistorycontainer.jsp" %>
                            </div>
                        </div>

                        <!-- Right Panel: Text Area -->
                        <div class="col-md-3">
                            <div class="panel right-text-panel">
                                <%@ include file="leaderboard.jsp" %>
                                    <div class="form-control game-text-container mt-3" id="game-text-container" hidden>
                                        <p style="text-align: center; font-size: x-large;">Game Updates</p>
                                        <ul style="list-style-type: none; padding-left: 0;" id="game-text-list"></ul>
                                    </div>
                                    <div class="chat-area" id="chat-area" hidden>
                                        <div class="chat-area" id="chat-messages">
                                        </div>

                                        <form id="messageForm" name="messageForm">
                                            <div class="message-input">
                                                <input autocomplete="off" type="text" id="message"
                                                    placeholder="Type your message...">
                                                <button>Send</button>
                                            </div>
                                        </form>
                                    </div>
                            </div>
                        </div>
                    </div>
                    <!-- </div> -->

                    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"
                        integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r"
                        crossorigin="anonymous"></script>
                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"
                        integrity="sha384-0pUGZvbkm6XF6gxjEnlmuGrJXVbNuzT9qBBavbLwCsOGabYfZo0T0to5eqruptLy"
                        crossorigin="anonymous"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.4/sockjs.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
                    <script src="${pageContext.request.contextPath}/resources/js/main.js"></script>
        </body>

        </html>