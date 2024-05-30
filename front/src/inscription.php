<!DOCTYPE html>
<html>
<head>
    <title>Page d'inscription</title>
    <meta charset="utf-8" />
    <link href="stylesheet.css" rel="stylesheet" />
</head>
<body>
    <div class="container">
        <h1>Inscription</h1>
        <form action="inscription.php" method="post">
            <ul>
                <li>
                    <label for="email">Entrez un email</label>
                    <input type="email" id="email" name="user_email" required />
                </li>
                <li>
                    <label for="password">Entrez un mot de passe</label>
                    <input type="password" id="password" name="user_password" required />
                </li>
                <li>
                    <button type="submit">S'inscrire</button>
                </li>
            </ul>
        </form>
    </div>
</body>
</html>
