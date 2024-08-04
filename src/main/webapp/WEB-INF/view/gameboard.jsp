<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Overlay Container on Image</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/gameboard.css">
</head>

<body>

    <div class="image-container" id="image-container" hidden>
        <img src="<c:url value='/resources/images/board1.jpg'/>" id="board-img" class="board-img" alt="Monopoly Board">
        <div class="overlay-container" id="player1PropertyDiv">
            <div class="overlay-item" style="color: black;">
                <span id="player1Name" style="color: black;">Player1</span><span class="money"
                    id="player1Money">1500</span>
                <hr style=" width: 100%; height: 2px; border: none; background-color: black; margin: 10px 0; ">
                <ul id="player1PropertyList" style="height: 100%; overflow-y: scroll; max-height: 470px; list-style-type: none;
padding-left: 0;">
                </ul>
            </div>
            <div class="overlay-item" id="player2PropertyDiv">
                <span id="player2Name" style="color: black;">Player2</span><span class="money"
                    id="player2Money">1500</span>
                <hr style=" width: 100%; height: 2px; border: none; background-color: black; margin: 10px 0; ">
                <ul id="player2PropertyList" style="height: 100%; overflow-y: scroll; max-height: 470px; list-style-type: none;
padding-left: 0;">
                </ul>
            </div>
            <div class="overlay-item" id="player3PropertyDiv">
                <span id="player3Name" style="color: black;">Player3</span><span class="money"
                    id="player3Money">1500</span>
                <hr style=" width: 100%; height: 2px; border: none; background-color: black; margin: 10px 0; ">
                <ul id="player3PropertyList" style="height: 100%; overflow-y: scroll; max-height: 470px; list-style-type: none;
padding-left: 0;">
                </ul>
            </div>
            <div class="overlay-item" id="player4PropertyDiv">
                <span id="player4Name" style="color: black;">Player4</span><span class="money"
                    id="player4Money">1500</span>
                <hr style=" width: 100%; height: 2px; border: none; background-color: black; margin: 10px 0; ">
                <ul id="player4PropertyList" style="height: 100%; overflow-y: scroll; max-height: 470px; list-style-type: none;
padding-left: 0;">
                </ul>
            </div>
        </div>
        <div class="token" id="token1"></div>
        <div class="token" id="token2"></div>
        <div class="token" id="token3"></div>
        <div class="token" id="token4"></div>
    </div>

</body>

</html>