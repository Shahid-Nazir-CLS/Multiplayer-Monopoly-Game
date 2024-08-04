<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Tic Tac Toe Game</title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
        <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/home.css">
        <style>

        </style>
    </head>

    <body>
        <div class="container">
            <div class="title">
                Monopoly Game
            </div>
            <button class="start-btn" onclick="startGame()">Start Game</button>
        </div>

        <script>
            function startGame() {
                window.location = "/game";
            }
        </script>
    </body>

    </html>