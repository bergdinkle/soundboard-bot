<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Soundboard Bot</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        h4 {
            margin: 12px;
        }
        .container {
            width: 600px;
            margin: 0;
        }
        @media only screen and (orientation: portrait) {
            h4 {
                font-size: 4em;
                text-align: center;
            }
            .container {
                padding: 40px;
                max-width: 100%;
                width: auto;
            }
            input {
                font-size: 2.5em !important;
                border-width: 2px !important;
                --bs-list-group-border-radius: 0.5rem;
            }
            button {
                font-size: 2.5em !important;
                --bs-list-group-border-radius: 0.5rem;
            }
        }
    </style>
</head>
<body>
    <h4>Soundboard Bot - Token</h4>
    <div class="container">
        <div class="input-group">
            <input id="token-input" type="password" name="password" class="form-control" placeholder="Token">
            <button class="btn btn-primary" type="button" onclick="setToken()">Absenden</button>
        </div>
    </div>


    <script>
        function setToken() {
            let input = document.getElementById("token-input");
            let now = new Date();
            let time = now.getTime();
            let expireTime = time + 1000*2592000;
            now.setTime(expireTime);
            setCookie("Auth-Token", input.value, now)
            location.reload()
        }

        function setCookie(name, value, expire) {
            document.cookie = name + "=" + value + ";expires=" + expire.toUTCString()+ ";path=/";
        }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>